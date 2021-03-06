package ee.ut.jbizur.role.bizur;

import ee.ut.jbizur.config.PropertiesLoader;
import ee.ut.jbizur.network.address.Address;
import ee.ut.jbizur.network.address.MulticastAddress;

import java.io.File;
import java.util.Set;

public class BizurBuilder {

    private BizurSettings settings;

    protected BizurBuilder(){
        settings = new BizurSettings();
    }

    public static BizurBuilder builder() {
        return new BizurBuilder();
    }

    public BizurBuilder withMemberId(String memberId) {
        settings.setRoleId(memberId);
        return this;
    }

    public BizurBuilder withAddress(Address address) {
        settings.setAddress(address);
        return this;
    }

    public BizurBuilder withMulticastAddress(MulticastAddress multicastAddress) {
        settings.setMulticastAddress(multicastAddress);
        return this;
    }

    public BizurBuilder withNumBuckets(int bucketCount) {
        settings.setNumBuckets(bucketCount);
        return this;
    }

    public BizurBuilder withMemberAddresses(Set<Address> addresses) {
        settings.setMemberAddresses(addresses);
        return this;
    }

    public BizurBuilder loadPropertiesFrom(File workingDirFile) {
        PropertiesLoader.loadProperties(workingDirFile);
        settings.defaults();
        return this;
    }

    public BizurBuilder loadPropertiesFrom(Class resourceClass, String fileName) {
        PropertiesLoader.loadProperties(resourceClass, fileName);
        settings.defaults();
        return this;
    }

    protected BizurSettings getSettings() {
        return this.settings;
    }

    public BizurNode build() throws InterruptedException {
        BizurNode bizurNode = new BizurNode(settings);
        settings.registerRoleRef(bizurNode);
        return bizurNode;
    }

    public BizurClient buildClient() throws InterruptedException {
        BizurClient bizurClient = new BizurClient(settings);
        settings.registerRoleRef(bizurClient);
        return bizurClient;
    }

}
