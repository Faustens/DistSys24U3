package msc_info_distsys24_u3.nodes;

import msc_info_distsys24_u3.PaxosNetwork;
import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;

import java.util.Iterator;
import java.util.logging.Logger;

public class Client extends Node {
    PaxosNetwork network = PaxosNetwork.getInstance();
    Logger logger = network.getLogger();
    String name;
    boolean keepRunning = true;

    public Client(String name) {
        super(name);
        this.name = name;
    }

    public void engage() {
        logger.info("[" + name + "] Engaging");
        sleep(1000);
        requestProposal("test");
        logger.info("[" + name + "] Disengaging");
    }
    /** Method: requestProposal
     * @param value value to be proposed to the network
     * @return true iff a proposer promises to promote the requested value
     */
    public boolean requestProposal(String value) {
        logger.info("[" + name + "] initiating proposal process with value " + value);
        Iterator<String> proposerIterator = network.getProposers().iterator();
        Message proposalReqMsg = new Message()
                .addHeader("type","proposal_req")
                .addHeader("name",name)
                .add("value",value);
        while(proposerIterator.hasNext()) {
            sendBlindly(proposalReqMsg, proposerIterator.next());
            Message answerMsg = null;
            while(answerMsg == null) answerMsg = receive();
            if(answerMsg.getPayload().get("ack").equals("ack")) {
                logger.info("[" + name + "] proposal request accepted with value " + value);
                return true;
            }
        }
        logger.info("[" + name + "] proposal request rejected with value " + value);
        return false;
    }


}
