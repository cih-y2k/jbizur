package ee.ut.jbizur.protocol.commands.bizur;

import ee.ut.jbizur.protocol.commands.NetworkCommand;

public class ApiDelete_NC extends NetworkCommand {
    private String key;

    public String getKey() {
        return key;
    }

    public ApiDelete_NC setKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public String toString() {
        return "ApiDelete_NC{" +
                "key='" + key + '\'' +
                "} " + super.toString();
    }
}
