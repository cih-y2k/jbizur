package ee.ut.jbizur.datastore.bizur;

import ee.ut.jbizur.network.address.Address;

import java.io.Serializable;
import java.util.Map;

public class BucketView implements Serializable {

    private Map<String, String> bucketMap;
    private int index;

    private int verElectId;
    private int verCounter;

    private Address leaderAddress;

    public Map<String, String> getBucketMap() {
        return bucketMap;
    }

    public BucketView setBucketMap(Map<String, String> bucketMap) {
        this.bucketMap = bucketMap;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public BucketView setIndex(int index) {
        this.index = index;
        return this;
    }

    public int getVerElectId() {
        return verElectId;
    }

    public BucketView setVerElectId(int verElectId) {
        this.verElectId = verElectId;
        return this;
    }

    public int getVerCounter() {
        return verCounter;
    }

    public BucketView setVerCounter(int verCounter) {
        this.verCounter = verCounter;
        return this;
    }

    public Address getLeaderAddress() {
        return leaderAddress;
    }

    public BucketView setLeaderAddress(Address leaderAddress) {
        this.leaderAddress = leaderAddress;
        return this;
    }

    public Bucket createBucket(BucketContainer bucketContainer) {
        return new Bucket(bucketContainer)
                .setIndex(getIndex())
                .setBucketMap(getBucketMap())
                .setIndex(getIndex())
                .setVerElectId(getVerElectId())
                .setVerCounter(getVerCounter())
                .setLeaderAddress(getLeaderAddress(), false);
    }

    public int compareVersion(BucketView other) {
        return compareVersions(this, other);
    }

    public static int compareVersions(BucketView mainBucketView, BucketView otherBucketView) {
        if(mainBucketView.getVerElectId() > otherBucketView.getVerElectId()){
            return 1;
        } else if (mainBucketView.getVerElectId() == otherBucketView.getVerElectId()){
            return Integer.compare(mainBucketView.getVerCounter(), otherBucketView.getVerCounter());
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "BucketView{" +
                "bucketMap=" + bucketMap +
                ", index=" + index +
                ", verElectId=" + verElectId +
                ", verCounter=" + verCounter +
                ", leaderAddress=" + leaderAddress +
                '}';
    }
}
