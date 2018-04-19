package role;

import config.GlobalConfig;
import datastore.bizur.Bucket;
import datastore.bizur.Ver;
import network.address.Address;
import network.messenger.SyncMessageListener;
import org.pmw.tinylog.Logger;
import protocol.commands.NetworkCommand;
import protocol.commands.bizur.*;

import java.util.concurrent.ConcurrentHashMap;

public class BizurNode extends Role {

    private int electId = 0;
    private int votedElectId = 0;
    private boolean isLeader = false;
    private Address leaderAddress = null;

    private Bucket[] localBuckets = new Bucket[1];

    public BizurNode(Address baseAddress) throws InterruptedException {
        super(baseAddress);

        initBuckets(localBuckets.length);
    }

    private void initBuckets(int count) {
        for (int i = 0; i < count; i++) {
            Ver v = new Ver()
                    .setCounter(0)
                    .setElectId(0);
            Bucket b = new Bucket()
                    .setIndex(i)
                    .setVer(v)
                    .setBucketMap(new ConcurrentHashMap<>());
            localBuckets[i] = b;
        }
    }

    /* ***************************************************************************
     * Algorithm 1 - Leader Election
     * ***************************************************************************/

    public void startElection() {
        electId++;

        String msgId = GlobalConfig.getInstance().generateMsgId();

        SyncMessageListener listener = new SyncMessageListener(msgId) {
            @Override
            public void handleMessage(NetworkCommand command) {
                if (command instanceof AckVote_NC) {
                    getProcessesLatch().countDown();
                    getAckCount().getAndIncrement();
                    if(isMajorityAcked()){
                        end();
                    }
                }
                if(command instanceof NackVote_NC){
                    getProcessesLatch().countDown();
                }
            }
        };
        attachMsgListener(listener);

        GlobalConfig.getInstance().getAddresses().forEach(receiverAddress -> {
            NetworkCommand pleaseVote = new PleaseVote_NC()
                    .setElectId(electId)
                    .setAssocMsgId(msgId)
                    .setSenderId(getRoleId())
                    .setReceiverAddress(receiverAddress)
                    .setSenderAddress(getAddress());
            sendMessage(pleaseVote);
        });

        try {
            listener.getProcessesLatch().await();
            isLeader = listener.isMajorityAcked();
        } catch (InterruptedException e) {
            Logger.error(e);
        }
        detachMsgListener(listener);
    }

    private void pleaseVote(PleaseVote_NC pleaseVoteNc) {
        int electId = pleaseVoteNc.getElectId();
        Address source = pleaseVoteNc.resolveSenderAddress();

        NetworkCommand vote;
        if (electId > votedElectId) {
            votedElectId = electId;
            leaderAddress = source;
            vote = new AckVote_NC()
                    .setAssocMsgId(pleaseVoteNc.getAssocMsgId())
                    .setSenderId(getRoleId())
                    .setReceiverAddress(source)
                    .setSenderAddress(getAddress());
        } else if(electId == votedElectId && source == leaderAddress) {
            vote = new AckVote_NC()
                    .setAssocMsgId(pleaseVoteNc.getAssocMsgId())
                    .setSenderId(getRoleId())
                    .setReceiverAddress(source)
                    .setSenderAddress(getAddress());
        } else {
            vote = new NackVote_NC()
                    .setAssocMsgId(pleaseVoteNc.getAssocMsgId())
                    .setSenderId(getRoleId())
                    .setReceiverAddress(source)
                    .setSenderAddress(getAddress());
        }
        sendMessage(vote);
    }

    /* ***************************************************************************
     * Algorithm 2 - Bucket Replication: Write
     * ***************************************************************************/

    private boolean write(Bucket bucket) {
        bucket.getVer().setElectId(electId);
        bucket.getVer().setCounter(bucket.getVer().getCounter() + 1);

        String msgId = GlobalConfig.getInstance().generateMsgId();

        SyncMessageListener listener = new SyncMessageListener(msgId) {
            @Override
            public void handleMessage(NetworkCommand command) {
                if(command instanceof AckWrite_NC){
                    getProcessesLatch().countDown();
                    getAckCount().incrementAndGet();
                    if(isMajorityAcked()){
                        end();
                    }
                }
                if(command instanceof NackWrite_NC){
                    getProcessesLatch().countDown();
                }
            }
        };
        attachMsgListener(listener);

        GlobalConfig.getInstance().getAddresses().forEach(receiverAddress -> {
            NetworkCommand replicaWrite = new ReplicaWrite_NC()
                    .setBucket(bucket)
                    .setAssocMsgId(msgId)
                    .setSenderId(getRoleId())
                    .setReceiverAddress(receiverAddress)
                    .setSenderAddress(getAddress());
            sendMessage(replicaWrite);
        });

        try {
            listener.getProcessesLatch().await();
            if(listener.isMajorityAcked()){
                return true;
            } else {
                isLeader = false;
            }
        } catch (InterruptedException e) {
            Logger.error(e);
        }
        detachMsgListener(listener);
        return false;
    }

    private void replicaWrite(ReplicaWrite_NC replicaWriteNc){
        Bucket bucket = replicaWriteNc.getBucket();
        Address source = replicaWriteNc.resolveSenderAddress();

        NetworkCommand response;
        if(bucket.getVer().getElectId() < votedElectId){
            response = new NackWrite_NC()
                    .setAssocMsgId(replicaWriteNc.getAssocMsgId())
                    .setSenderId(getRoleId())
                    .setReceiverAddress(source)
                    .setSenderAddress(getAddress());
        } else {
            votedElectId = bucket.getVer().getElectId();
            leaderAddress = source;
            localBuckets[bucket.getIndex()] = bucket;

            response = new AckWrite_NC()
                    .setAssocMsgId(replicaWriteNc.getAssocMsgId())
                    .setSenderId(getRoleId())
                    .setReceiverAddress(source)
                    .setSenderAddress(getAddress());
        }
        sendMessage(response);
    }



    /* ***************************************************************************
     * Algorithm 3 - Bucket Replication: Read
     * ***************************************************************************/

    private Bucket read(int index) {
        if(!ensureRecovery(electId, index)){
            return null;
        }

        String msgId = GlobalConfig.getInstance().generateMsgId();

        SyncMessageListener listener = new SyncMessageListener(msgId) {
            @Override
            public void handleMessage(NetworkCommand command) {
                if(command instanceof AckRead_NC){
                    getProcessesLatch().countDown();
                    getAckCount().incrementAndGet();
                    if(isMajorityAcked()){
                        end();
                    }
                }
                if(command instanceof NackRead_NC){
                    getProcessesLatch().countDown();
                }
            }
        };
        attachMsgListener(listener);

        GlobalConfig.getInstance().getAddresses().forEach(receiverAddress -> {
            NetworkCommand replicaRead = new ReplicaRead_NC()
                    .setIndex(index)
                    .setElectId(electId)
                    .setSenderAddress(getAddress())
                    .setReceiverAddress(receiverAddress)
                    .setAssocMsgId(msgId);
            sendMessage(replicaRead);
        });

        try {
            listener.getProcessesLatch().await();
            if(listener.isMajorityAcked()){
                return localBuckets[index];
            } else {
                isLeader = false;
            }
        } catch (InterruptedException e) {
            Logger.error(e);
        }
        detachMsgListener(listener);
        return null;
    }

    public void replicaRead(ReplicaRead_NC replicaReadNc){
        int index = replicaReadNc.getIndex();
        int electId = replicaReadNc.getElectId();
        Address source = replicaReadNc.resolveSenderAddress();

        if(electId < votedElectId){
            NetworkCommand nackRead = new NackRead_NC()
                    .setAssocMsgId(replicaReadNc.getAssocMsgId())
                    .setSenderId(getRoleId())
                    .setSenderAddress(getAddress())
                    .setReceiverAddress(source);
            sendMessage(nackRead);
        } else {
            votedElectId = electId;
            leaderAddress = source;

            NetworkCommand ackRead = new AckRead_NC()
                    .setBucket(localBuckets[index])
                    .setAssocMsgId(replicaReadNc.getAssocMsgId())
                    .setSenderId(getRoleId())
                    .setSenderAddress(getAddress())
                    .setReceiverAddress(source);
            sendMessage(ackRead);
        }
    }

    /* ***************************************************************************
     * Algorithm 4 - Bucket Replication: Recovery
     * ***************************************************************************/

    private boolean ensureRecovery(int electId, int index) {
        if(electId == localBuckets[index].getVer().getElectId()) {
            return true;
        }

        String msgId = GlobalConfig.getInstance().generateMsgId();

        final Bucket[] maxVerBucket = {null};
        SyncMessageListener listener = new SyncMessageListener(msgId) {
            @Override
            public void handleMessage(NetworkCommand command) {
                if(command instanceof AckRead_NC){
                    getProcessesLatch().countDown();

                    AckRead_NC ackRead = ((AckRead_NC) command);
                    Bucket bucket = ackRead.getBucket();

                    if(maxVerBucket[0] == null){
                        maxVerBucket[0] = bucket;
                    } else {
                        if((bucket.getVer().getElectId() + bucket.getVer().getCounter()) >
                                ((maxVerBucket[0].getVer().getElectId() + maxVerBucket[0].getVer().getCounter()))){
                            maxVerBucket[0] = bucket;
                        }
                    }

                    if(isMajorityAcked()){
                        end();
                    }
                }
                if(command instanceof NackRead_NC){
                    getProcessesLatch().countDown();
                }
            }
        };
        attachMsgListener(listener);

        GlobalConfig.getInstance().getAddresses().forEach(receiverAddress -> {
            NetworkCommand replicaRead = new ReplicaRead_NC()
                    .setIndex(index)
                    .setElectId(electId)
                    .setSenderAddress(getAddress())
                    .setReceiverAddress(receiverAddress)
                    .setAssocMsgId(msgId);
            sendMessage(replicaRead);
        });

        try {
            listener.getProcessesLatch().await();

            if(listener.isMajorityAcked()){
                maxVerBucket[0].getVer().setElectId(electId);
                maxVerBucket[0].getVer().setCounter(0);
                return write(maxVerBucket[0]);
            } else {
                isLeader = false;
            }
        } catch (InterruptedException e) {
            Logger.error(e);
        }
        detachMsgListener(listener);
        return false;
    }

    /* ***************************************************************************
     * Algorithm 5 - Key-Value API
     * ***************************************************************************/

    public String get(String key) {
        int index = 0;  //fixme
        Bucket bucket = read(index);
        return bucket != null ? bucket.getBucketMap().get(key) : null;
    }

    public boolean set(String key, String value){
        int index = 0; //fixme
        Bucket bucket = read(index);
        if (bucket != null) {
            bucket.getBucketMap().put(key, value);
            return write(bucket);
        }
        return false;
    }

    @Override
    public void handleMessage(NetworkCommand message) {
        super.handleMessage(message);

        if (message instanceof ReplicaWrite_NC){
            replicaWrite((ReplicaWrite_NC) message);
        }
        if (message instanceof ReplicaRead_NC){
            replicaRead(((ReplicaRead_NC) message));
        }
        if (message instanceof PleaseVote_NC) {
            pleaseVote(((PleaseVote_NC) message));
        }
    }

}