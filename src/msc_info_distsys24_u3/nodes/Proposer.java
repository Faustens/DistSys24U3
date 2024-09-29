package msc_info_distsys24_u3.nodes;

import org.oxoo2a.sim4da.Message;

import java.util.LinkedList;

public class Proposer extends BaseNode {
    private static int pNumCnt = 0;
    private int currentPNum = -1;
    private int highestOldPNum = -1;
    private String currentValue = null;
    private int promises = 0;
    private int accepts = 0;
    LinkedList<String> quorum;

    public Proposer(String name) {
        super(name);
    }

    // Abstract Method Implementations
    @Override
    protected void handleMessage(Message message) {
        logger.info("[" + name + "] Message received from " + message.getHeader().get("name"));
        String type = message.getHeader().get("type");
        switch (type) {
            case "proposal_ans" -> handleProposalAnswer(message);
            case "accept_ans" -> handleAcceptAnswer(message);
            case "proposal_req" -> handleProposalRequest(message);
        }
    }

    // Class Methods
    private void handleProposalRequest(Message message) {
        String clientAddr = message.getHeader().get("name");
        String value = message.getPayload().get("value");
        // Create base message
        Message proposalReqAns = new Message()
                .addHeader("name", name)
                .addHeader("type","proposal_req_ans");
        // If the proposer already promotes a value, new requests are denied
        if(currentPNum >= 0) {
            proposalReqAns.add("ack","nack");
            sendBlindly(proposalReqAns,clientAddr);
            logger.info("[" + name + "] Proposal request rejected from " + clientAddr + " with value " + value);
            return;
        } else proposalReqAns.add("ack","ack");
        // create proposal message
        currentPNum = pNumCnt++;
        currentValue = value;
        Message proposalMsg = new Message()
                .addHeader("type", "proposal")
                .addHeader("name", name)
                .add("pNum", currentPNum);
        // Send proposal to a quorum
        if (quorum == null) quorum = network.generateQuorum();
        for (String acceptorAddr : quorum) {
            sendBlindly(proposalMsg, acceptorAddr);
        }
        sendBlindly(proposalReqAns,clientAddr);
        logger.info("[" + name + "] Proposal request accepted from " + clientAddr + " with value " + value);
    }

    private void handleProposalAnswer(Message message) {
        // Exactly 'at least more than half' acceptors receives a proposal, any nack means that the proposal is rejected
        // In that case the proposer "freezes". New incoming messages are still handled, but because there will never be
        // quorum.size() promises no further progress will be made.
        if (message.getPayload().get("ack").equals("nack")) {
            logger.info("[" + name + "] Proposal nack received from " + message.getHeader().get("name"));
            return;
        }
        promises += 1;
        // If an acceptor has accepted any previous proposal, this proposer will take over the role of propagating the
        // highest numbered value (And therefore the role of the previous proposer which my or may not have stopped working)
        if (message.getPayload().get("value") != null) {
            int pNumOld = Integer.parseInt(message.getPayload().get("pNumOld"));
            if (pNumOld > highestOldPNum) {
                highestOldPNum = pNumOld;
                currentValue = message.getPayload().get("value");
            }
            logger.info("[" + name + "] New value set for propagation:" + currentValue );
        }
        if (promises >= (network.getAcceptors().size() / 2)) {  // Can only happen when every member of the quorum has answered.
            Message acceptMsg = new Message()
                    .addHeader("type", "accept")
                    .addHeader("name", name)
                    .add("pNum", currentPNum)
                    .add("value", currentValue);
            // Because we sent our proposal to exactly n/2+1 nodes, we can assume that every node sent a promise
            for (String acceptorAddr : quorum) {
                sendBlindly(acceptMsg, acceptorAddr);
                logger.info("[" + name + "] Accept sent to " + acceptorAddr + " with pNum " + currentPNum + " and value " + currentValue);
            }
        }
    }

    private void handleAcceptAnswer(Message message) {
        if (message.getPayload().get("ack").equals("ack")) accepts++;
        if (accepts >= (network.getAcceptors().size() / 2)) {
            logger.info("[" + name + "] Proposal with pNum " + currentPNum + " and value " + currentValue + "successfully accepted");
            Message successMsg = new Message()
                    .addHeader("type", "success")
                    .addHeader("name", name)
                    .add("pNum", message.getPayload().get("pNum"))
                    .add("value", message.getPayload().get("value"));
            for (String learnerAddr : network.getLearners()) {
                sendBlindly(successMsg, learnerAddr);
            }
        }
    }
}
