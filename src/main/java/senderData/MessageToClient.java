package senderData;

import java.io.Serial;
import java.io.Serializable;
import java.util.Random;

public class MessageToClient implements Serializable {

    private final Integer hashcode;

    @Serial
    private static final long serialVersionUID = 8219482076867784532L;
    private final MessageTypeToClient type;
    private final Object message;
    private final Object additionalInfo;

    public MessageToClient(MessageTypeToClient type, Object message, Object additionalInfo) {
        this.type = type;
        this.message = message;
        this.additionalInfo = additionalInfo;
        this.hashcode = new Random().nextInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MessageToClient other = (MessageToClient) obj;
        return this.hashCode() == other.hashCode();
    }


    @Override
    public int hashCode() {
        return hashcode != null ? hashcode : 0;
    }


    public MessageTypeToClient type() {
        return type;
    }

    public Object message() {
        return message;
    }

    public Object additionalInfo() {
        return additionalInfo;
    }

    @Override
    public String toString() {
        return "MessageToClient[" +
                "type=" + type + ", " +
                "message=" + message + ", " +
                "additionalInfo=" + additionalInfo + ']';
    }

}
