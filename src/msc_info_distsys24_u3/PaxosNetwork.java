package msc_info_distsys24_u3;

import msc_info_distsys24_u3.nodes.Acceptor;
import msc_info_distsys24_u3.nodes.BaseNode;
import msc_info_distsys24_u3.nodes.Learner;
import msc_info_distsys24_u3.nodes.Proposer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Logger;

/** Class PaxosNetwork
 * The class PaxosNetwork is designed to simulate the overarching network structure of a paxos network, meaning it
 * manages the "addresses" (i.e. Node names) of all nodes relevant to the agreement protocol and gives
 * access to said addresses to allow for communication between nodes.
 * This results in a slightly simplified version of an actual network which is deemed 'sufficient'
 * by the author of this project
 * The class is realized as a Singleton class, which simplifies some parts of the code in regard
 * to the networks inclusion in all PaxosNodes. This allows for only one Network to be simulated at once.
 */
public class PaxosNetwork {
    // Class variables ============================================================================
    static PaxosNetwork network = null;
    LinkedList<String> proposers, acceptors, learners, clients;
    static int proposerIdCnt,acceptorIdCnt,learnerIdCnt;
    final static Logger logger = Logger.getLogger("msc_info_distsys24_u3");
    // Constructor and getInstance method =========================================================
    private PaxosNetwork() {
        proposerIdCnt = 0;
        acceptorIdCnt = 0;
        learnerIdCnt = 0;
        proposers = new LinkedList<>();
        acceptors = new LinkedList<>();
        learners = new LinkedList<>();
        clients = new LinkedList<>();
        logger.info("[NETWORK] new PaxosNetwork created");
    }
    public static PaxosNetwork getInstance() {
        if (network == null) network = new PaxosNetwork();
        return network;
    }
    // Getter Methods for proposers, acceptors, learners and clients ==============================
    public LinkedList<String> getAcceptors() {
        return acceptors;
    }
    public LinkedList<String> getProposers() {
        return proposers;
    }
    public LinkedList<String> getLearners() {
        return learners;
    }
    public LinkedList<String> getClients() {
        return clients;
    }
    public Logger getLogger() {
        return logger;
    }
    // Methods for adding paxos nodes and clients to the network ==================================
    public void addProposer() {
        String name = "Prop" + proposerIdCnt++;
        Proposer proposer = new Proposer(name);
        proposers.add(name);
        //paxosNodes.add(proposer);
        logger.info("[NETWORK] Proposer " + name + " added to Network");
    }
    public void addProposers(int num) {
        for (int i=0; i<num ; i++) {
            addProposer();
        }
    }
    public void addAcceptor() {
        String name = "Acc" + acceptorIdCnt++;
        Acceptor acceptor = new Acceptor(name);
        acceptors.add(name);
        //paxosNodes.add(acceptor);
        logger.info("[NETWORK] Acceptor " + name + " added to Network");
    }
    public void addAcceptors(int num) {
        for (int i=0; i<num ; i++) {
            addAcceptor();
        }
    }
    public void addLearner() {
        String name = "Learn" + learnerIdCnt++;
        Learner learner = new Learner(name);
        learners.add(name);
        //paxosNodes.add(learner);
        logger.info("[NETWORK] Learner " + name + " added to Network");
    }
    // A client gets only registered if it's address has not already been registered
    // The network having knowledge of clients currently
    // serves no purpose and exists only for completeness
    public boolean registerClient(String clientAddr) {
        if(clients.stream().anyMatch(existingName -> existingName.equals(clientAddr))) {
            logger.info("[NETWORK] Client " + clientAddr + " already in Network");
            return false;
        }
        clients.add(clientAddr);
        logger.info("[NETWORK] Client " + clientAddr + "added to Network");
        return true;
    }
    //Additional methods ==========================================================================
    /** Method: generateQuorum()
     * @return LinkedList<String>:  List with the addresses of exactly more than half of all acceptors
     */
    public LinkedList<String> generateQuorum() {
        int size = (network.acceptors.size()/2) + 1;
        LinkedList<String> shuffledList = new LinkedList<>(network.acceptors);
        Collections.shuffle(shuffledList);
        // Zufällige Teilmenge der Länge floor(n/2) + 1 zurückgeben
        return new LinkedList<>(shuffledList.subList(0,size-1));
    }
}

