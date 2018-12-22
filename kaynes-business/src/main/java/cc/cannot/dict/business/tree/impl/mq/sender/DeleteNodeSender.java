package cc.cannot.dict.business.tree.impl.mq.sender;

import cc.cannot.dict.business.tree.impl.mq.event.DeleteNodeEvent;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;

@EnableBinding(DeleteNodeEvent.Output.class)
public class DeleteNodeSender {

    private final DeleteNodeEvent.Output output;

    public DeleteNodeSender(DeleteNodeEvent.Output output) {
        this.output = output;
    }

    public void sendProcess(final DictTypeEnum type, final Object bid) {
        DeleteNodeEvent.DeleteNodeEventContext context = new DeleteNodeEvent.DeleteNodeEventContext();
        context.setType(type);
        context.setBid(bid);
        output.deleteNode().send(MessageBuilder.withPayload(context).build());
    }
}