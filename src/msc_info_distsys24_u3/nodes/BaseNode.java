package msc_info_distsys24_u3.nodes;

import msc_info_distsys24_u3.PaxosNetwork;
import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;

import java.util.LinkedList;
import java.util.logging.Logger;

/** Class: BaseNode
 * Base class for nodes in a paxos network. Contains standardized functionalities for
 * message handling and predefines the engage method.
 */
public abstract class BaseNode extends Node {
    // Class Variables ============================================================================
    LinkedList<Message> messageBuffer = new LinkedList<>();
    PaxosNetwork network = PaxosNetwork.getInstance();
    String name;
    static boolean keepRunning = true;
    Logger logger = network.getLogger();
    // Class Constructor ==========================================================================
    protected BaseNode(String name) {
        super(name);
        this.name = name;
        new Thread(() -> {
            while (keepRunning) {
                Message message = receive();
                if (message != null) messageBuffer.add(message);
            }
        }).start();
    }
    // Class Methods ==============================================================================
    public void engage() {
        logger.info("[STATUS] Engaging " + name);
        while (keepRunning) {
            if (messageBuffer.isEmpty()) {
                sleep(1000);
                //logger.info("[" + name + "] No messages received");
                continue;
            }
            handleMessage(messageBuffer.removeFirst());
        }
        logger.info("[STATUS] Disengaging " + name);
    }
    // Abstract Methods ===========================================================================
    abstract protected void handleMessage(Message message);
}
