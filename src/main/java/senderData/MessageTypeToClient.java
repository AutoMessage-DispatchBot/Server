package senderData;

import java.io.Serial;
import java.io.Serializable;

public enum MessageTypeToClient implements Serializable {
    AUTHORIZATION,

    GMAIL_MESSAGE_IN_QUEUE,
    WHATSAPP_MESSAGE_IN_QUEUE,

    WHATSAPP_AUTHORIZATION,
    WHATSAPP_NEED_AUTHORIZATION,

    CLIENTS_LIST,
    MANAGERS_LIST;

    @Serial
    private static final long serialVersionUID = 1030966780631079404L;
}
