package ee.ut.jbizur.protocol.commands.ping;

import ee.ut.jbizur.protocol.commands.NetworkCommand;

/**
 * Signals the processes to stop waiting for further requests.
 */
public class SignalEnd_NC extends NetworkCommand{
    @Override
    public String toString() {
        return "SignalEnd_NC{} " + super.toString();
    }
}
