package ee.ut.jbizur.protocol.internal;

import ee.ut.jbizur.protocol.commands.NetworkCommand;

public class SendFail_IC extends InternalCommand {
    private final NetworkCommand networkCommand;

    public SendFail_IC(NetworkCommand networkCommand) {
        this.networkCommand = networkCommand;
    }

    public NetworkCommand getNetworkCommand() {
        return networkCommand;
    }
}
