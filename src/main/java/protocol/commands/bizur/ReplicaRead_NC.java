package protocol.commands.bizur;

import protocol.commands.NetworkCommand;

public class ReplicaRead_NC extends NetworkCommand {

    private int index;
    private int electId;

    public int getIndex() {
        return index;
    }

    public ReplicaRead_NC setIndex(int index) {
        this.index = index;
        return this;
    }

    public int getElectId() {
        return electId;
    }

    public ReplicaRead_NC setElectId(int electId) {
        this.electId = electId;
        return this;
    }
}