package msc_info_distsys24_u3.nodes;

import org.oxoo2a.sim4da.Message;

public class Acceptor extends BaseNode {
    private int currentPNum = -1;
    private String currentValue = null;

    public Acceptor(String name) {
        super(name);
    }
    // Abstract Method Implementations
    @Override
    protected void handleMessage(Message message) {
        String type = message.getHeader().get("type");
        if (type.equals("proposal")) handleProposal(message);
        else if (type.equals("accept")) handleAccept(message);
    }

    // Class Methods

    /** Method: handleProposal
     * @param message Proposal message sent by proposer.
     * Processes 'message' and constructs an answer message based on internal and message Data
     */
    private void handleProposal(Message message) {
        String targetAddr = message.getHeader().get("name");
        //Prepare base answer with type and nodeName
        Message proposalAnsMsg = new Message()
                .addHeader("type", "proposal_ans")
                .addHeader("name", name);
        int newPNum = Integer.parseInt(message.getPayload().get("pNum"));
        // Check if the new pNum is the highest yet. Sends promise only if yes.
        if (newPNum < currentPNum) {
            proposalAnsMsg.add("ack", "nack");
            logger.info("[" + name + "] Proposal rejected from " + targetAddr + " with pNum " + newPNum);
        } else {
            proposalAnsMsg.add("ack", "ack")
                    .add("pNum", newPNum)
                    .add("pNumOld", currentPNum) // only relevant if currentValue != null, otherwise ignored by proposer
                    .add("value", currentValue); // != null if accept has already been made
            currentPNum = newPNum;
            logger.info("[" + name + "] Proposal accepted from " + targetAddr + " with pNum " + newPNum);
        }
        sendBlindly(proposalAnsMsg, targetAddr);
    }

    private void handleAccept(Message message) {
        int pNum = Integer.parseInt(message.getPayload().get("pNum"));
        String targetAddr = message.getHeader().get("name");
        Message acceptAnsMsg = new Message()
                .addHeader("type", "accept_ans")
                .addHeader("name", name);
        if (pNum < currentPNum) {
            acceptAnsMsg.add("ack", "nack");
            sendBlindly(acceptAnsMsg, targetAddr);
            logger.info("[" + name + "] Accept rejected from " + targetAddr + " with pNum " + pNum);
            return;
        }
        // If pNum > currentPNum then something has gone horribly wrong. This will currently not be handeled
        currentPNum = pNum;
        currentValue = message.getPayload().get("value");
        acceptAnsMsg.add("ack", "ack")
                .add("pNum", currentPNum)
                .add("value", currentValue);
        sendBlindly(acceptAnsMsg, targetAddr);
        logger.info("[" + name + "] Proposal accepted from " + targetAddr + " with pNum " + currentPNum + " and value " + currentValue);
    }
}
