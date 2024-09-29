package msc_info_distsys24_u3.nodes;

import org.oxoo2a.sim4da.Message;

import java.util.LinkedList;

public class Learner extends BaseNode {
    public Learner(String name) {
        super(name);
    }

    @Override
    protected void handleMessage(Message message) {
        if (message.getHeader().get("type").equals("success")) handleSuccess(message);
    }

    private void handleSuccess(Message message) {
        String value = message.getPayload().get("value");
        logger.info("[" + name + "] value: " + value + "Successfully accepted");
        keepRunning = false;
    }
}
