package gate;

import senderData.MessageToClient;
import settings.Main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class Output extends Thread {
    private final ObjectOutputStream outputStream;
    private final List<MessageToClient> messagesQueue = new LinkedList<>();
    private final Connection connection;
    private List<MessageToClient> readyMessageForAll = new LinkedList<>();

    private static final List<MessageToClient> messageForAll = new LinkedList<>();

    private MessageToClient getMessageFromQueue() {
        if(connection.authorized) {
            synchronized (messageForAll) {

                for (MessageToClient message : messageForAll) {
                    if (readyMessageForAll.contains(message)) {
                        continue;
                    }
                    readyMessageForAll.add(message);
                    return message;
                }

                synchronized (readyMessageForAll) {
                    readyMessageForAll = new LinkedList<>(messageForAll);
                }
            }
        }
        synchronized (readyMessageForAll) {
            while (readyMessageForAll.size() > 5)
                readyMessageForAll.remove(0);
        }


        synchronized (messageForAll) {
            while (messageForAll.size() > 5)
                messageForAll.remove(0);

            if (messageForAll.size() == 0)
                readyMessageForAll.clear();
        }

        synchronized (messagesQueue) {
            if (messagesQueue.size() > 0)
                return messagesQueue.remove(0);
        }

        return null;
    }

    public static void forAll(MessageToClient message) {
        synchronized (messageForAll) {
            messageForAll.add(message);
        }
        synchronized (Main.sleep) {
            Main.sleep.notifyAll();
        }
    }

    public static void clearForAll() {
        synchronized (messageForAll) {
            messageForAll.clear();
        }
    }

    public void addMessageToQueue(MessageToClient message) {
        synchronized (messagesQueue) {
            messagesQueue.add(message);
        }

        synchronized (Main.sleep) {
            Main.sleep.notifyAll();
        }
    }

    private synchronized boolean isNewMessages() {
        return (messagesQueue.size() > 0) || ((readyMessageForAll.size() < messageForAll.size()) && connection.authorized);
    }

    public Output(OutputStream outputStream, Connection connection) throws IOException {
        this.outputStream = new ObjectOutputStream(outputStream);
        this.connection = connection;
    }

    @Override
    public void run() {
        while (connection.isConnection) {
            while (!isNewMessages()) {
                try {
                    synchronized (Main.sleep) {
                        Main.sleep.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            MessageToClient message = getMessageFromQueue();
            if(message == null)
                continue;

            if(message != null)
                System.out.println(message);

            try {
                outputStream.writeObject(message);
            } catch (IOException e) {
                connection.isConnection = false;

                try {
                    outputStream.close();
                }
                catch (IOException ignored) {}
            }
        }

        try {
            outputStream.close();
        }
        catch (IOException ignored) {}
    }
}
