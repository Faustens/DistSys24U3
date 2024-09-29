package msc_info_distsys24_u3;

import msc_info_distsys24_u3.nodes.Client;
import org.oxoo2a.sim4da.Simulator;

import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Simulator simulator = Simulator.getInstance();
        PaxosNetwork network = PaxosNetwork.getInstance();
        Logger logger = network.getLogger();
        network.addProposers(1);
        network.addAcceptors(5);
        network.addLearner();
        Client client = new Client("client");
        simulator.simulate();
    }
}
