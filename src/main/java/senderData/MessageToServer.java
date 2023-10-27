package senderData;

import java.io.Serial;
import java.io.Serializable;
import java.util.Random;

public class MessageToServer implements Serializable {


    @Serial
    private static final long serialVersionUID = 1799035792606214037L;
    private final MessageTypeToServer type;
    private final Object message;
    private final Object additionalInfo;
    private final Integer hashcode;

    public MessageToServer(MessageTypeToServer type, Object message, Object additionalInfo) {
        this.type = type;
        this.message = message;
        this.additionalInfo = additionalInfo;
        this.hashcode = new Random().nextInt();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MessageToServer other = (MessageToServer) obj;
        return this.hashCode() == other.hashCode();
    }


    @Override
    public int hashCode() {
        return hashcode != null ? hashcode : 0;
    }

    public MessageTypeToServer type() {
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
        return "MessageToServer[" +
                "type=" + type + ", " +
                "message=" + message + ", " +
                "additionalInfo=" + additionalInfo + ']';
    }

}
