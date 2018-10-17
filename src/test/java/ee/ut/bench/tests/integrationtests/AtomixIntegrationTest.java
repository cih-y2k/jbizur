package ee.ut.bench.tests.integrationtests;

import ee.ut.bench.config.Config;
import ee.ut.bench.db.AbstractDBClientWrapper;
import ee.ut.bench.db.AtomixDBClientWrapper;
import ee.ut.bench.db.DBWrapperFactory;
import io.atomix.core.Atomix;
import io.atomix.primitive.partition.ManagedPartitionGroup;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import io.atomix.storage.StorageLevel;
import io.atomix.utils.net.Address;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@Ignore
public class AtomixIntegrationTest extends AbstractIntegrationTest {

    private static final int NODE_COUNT = Config.getMemberCount();

    private Atomix[] nodes = new Atomix[NODE_COUNT];

    @Override
    void initNodes() {
        Config.reset();
        CountDownLatch latch = new CountDownLatch(NODE_COUNT);
        for (int i = 0; i < NODE_COUNT; i++) {
            nodes[i] = initNode(i);
            int finalI = i;
            new Thread(() -> {
                nodes[finalI].start().join();
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Atomix initNode(int index) {
        String address = Config.compileTCPAddress(index);
        String multicastAddr = Config.compileMulticastAddress();
        String[] members = Config.getMemberIds();

        ManagedPartitionGroup managementGroup = RaftPartitionGroup.builder("system")
                .withMembers(members)
                .withNumPartitions(1)
                .withStorageLevel(StorageLevel.MEMORY)
                .withDataDirectory(Config.getSystemDataDirFor(index))
                .build();
        RaftPartitionGroup primitiveGroup = RaftPartitionGroup.builder("data")
                .withMembers(members)
                .withNumPartitions(1)
                .withStorageLevel(StorageLevel.MEMORY)
                .withDataDirectory(Config.getPrimitiveDataDirFor(index))
                .build();

        return Atomix.builder()
                .withMemberId(members[index])
                .withAddress(address)
                .withMulticastEnabled()
                .withMulticastAddress(Address.from(multicastAddr))
                .withManagementGroup(managementGroup)
                .withPartitionGroups(primitiveGroup)
                .build();
    }

    @Override
    AbstractDBClientWrapper initClient() throws Exception {
        return DBWrapperFactory.buildAndInit(AtomixDBClientWrapper.class);
    }

    @Test
    @Override
    public void runTest() {
        run();
    }
}