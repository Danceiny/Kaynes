package cc.cannot.dict.business.tree.impl.mq.receiver;

import cc.cannot.dict.business.tree.impl.mq.event.DeleteNodeEvent;
import cc.cannot.dict.business.tree.impl.TreeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableBinding(DeleteNodeEvent.Input.class)
public class DeleteNodeHandler {

    private final TreeServiceImpl treeService;

    @Autowired
    public DeleteNodeHandler(TreeServiceImpl treeService) {
        this.treeService = treeService;
    }

    @StreamListener(DeleteNodeEvent.CHANNEL_NAME)
    public void consume(DeleteNodeEvent.DeleteNodeEventContext context) {
        treeService.deleteNode(context.getType(), context.getBid());
    }
}
