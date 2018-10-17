package ee.ut.jbizur.protocol.commands.bizur;

public class ClientApiDelete_NC extends ClientRequest_NC {
    private String key;

    public String getKey() {
        return key;
    }

    public ClientApiDelete_NC setKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public String toString() {
        return "ClientApiDelete_NC{" +
                "key='" + key + '\'' +
                "} " + super.toString();
    }
}
