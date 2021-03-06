package ee.ut.jbizur.datastore.bizur;

public class BucketLock {
    private boolean isLocked = false;

    public synchronized void lock() throws InterruptedException{
        while(isLocked){
            wait();
        }
        isLocked = true;
    }

    public synchronized void unlock(){
        isLocked = false;
        notify();
    }

    public boolean isLocked() {
        return isLocked;
    }
}
