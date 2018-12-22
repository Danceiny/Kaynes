package cc.cannot.dict.business.tree.impl.mq.event;

import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import lombok.Data;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

import java.io.Serializable;

public interface DeleteNodeEvent {

    String CHANNEL_NAME_OUT = "deleteNodeOutput";
    String CHANNEL_NAME = "deleteNode";

    interface Input {
        @org.springframework.cloud.stream.annotation.Input(CHANNEL_NAME)
        SubscribableChannel consume();
    }

    interface Output {
        @org.springframework.cloud.stream.annotation.Output(CHANNEL_NAME_OUT)
        MessageChannel deleteNode();
    }

    @Data
    class DeleteNodeEventContext implements Serializable {

        private static final long serialVersionUID = 5180127301543790442L;

        private DictTypeEnum type;

        /**
         * string/int
         */
        private Object bid;
    }
}
