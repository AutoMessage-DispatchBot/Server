package gate;

import data.Buyer;
import data.Correspondence;
import data.Filter;
import senderData.MessageToClient;
import senderData.MessageToServer;
import senderData.MessageTypeToClient;
import database.Database;
import sender.Queue;
import sender.whatsapp.WhatsAppSender;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.TreeSet;

public class Input extends Thread {
    private final ObjectInputStream inputStream;
    private final Connection connection;

    public Input(InputStream inputStream, Connection connection) throws IOException {
        this.inputStream = new ObjectInputStream(inputStream);
        this.connection = connection;
    }

    @Override
    public void run() {
        while (connection.isConnection) {
            try {
                MessageToServer message = (MessageToServer) inputStream.readObject();
                if(message != null)
                    System.out.println(message);

                switch (message.type()) {
                    case GMAIL_MESSAGE_SENDING -> {
                        if(!connection.authorized)
                            throw new IOException("Not Authorized");

                        Correspondence[] textMessage = (Correspondence[]) message.message();
                        Buyer[] buyers = (Buyer[]) message.additionalInfo();

                        Queue.addNewMessagesGmail(textMessage, buyers);
                    }

                    case WHATSAPP_MESSAGE_SENDING -> {
                        if(!connection.authorized)
                            throw new IOException("Not Authorized");

                        Correspondence[] textMessage = (Correspondence[]) message.message();
                        Buyer[] buyers = (Buyer[]) message.additionalInfo();

                        Queue.addNewMessagesWhatsApp(textMessage, buyers);
                    }

                    case WHATSAPP_AUTHORIZED -> {
                        if(!connection.authorized)
                            throw new IOException("Not Authorized");

                        WhatsAppSender.setAuthorized();
                        Output.clearForAll();
                    }

                    case GMAIL_MESSAGE_CANCEL -> {
                        if(!connection.authorized)
                            throw new IOException("Not Authorized");

                        Correspondence[] textMessage = (Correspondence[]) message.message();
                        Buyer[] buyers = (Buyer[]) message.additionalInfo();

                        Queue.deleteMessagesGmail(textMessage, buyers);


                        Map<Correspondence[], TreeSet<Buyer>> map = Queue.listMessagesGmail();
                        byte[] serializedData;

                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                            oos.writeObject(map);
                            serializedData = baos.toByteArray();
                        }

                        MessageToClient toClient = new MessageToClient(MessageTypeToClient.GMAIL_MESSAGE_IN_QUEUE,
                                serializedData, null);

                        connection.output.addMessageToQueue(toClient);
                    }

                    case WHATSAPP_MESSAGE_CANCEL -> {
                        if(!connection.authorized)
                            throw new IOException("Not Authorized");

                        Correspondence[] textMessage = (Correspondence[]) message.message();
                        Buyer[] buyers = (Buyer[]) message.additionalInfo();

                        Queue.deleteMessagesWhatsApp(textMessage, buyers);


                        Map<Correspondence[], TreeSet<Buyer>> map = Queue.listMessagesWhatsApp();
                        byte[] serializedData;

                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                            oos.writeObject(map);
                            serializedData = baos.toByteArray();
                        }

                        connection.output.addMessageToQueue(new MessageToClient(
                                MessageTypeToClient.WHATSAPP_MESSAGE_IN_QUEUE,
                                serializedData,
                                null
                        ));
                    }

                    case GMAIL_MESSAGE_LIST -> {
                        if(!connection.authorized)
                            throw new IOException("Not Authorized");


                        Map<Correspondence[], TreeSet<Buyer>> map = Queue.listMessagesGmail();
                        byte[] serializedData;

                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                            oos.writeObject(map);
                            serializedData = baos.toByteArray();
                        }

                        MessageToClient toClient = new MessageToClient(MessageTypeToClient.GMAIL_MESSAGE_IN_QUEUE,
                                serializedData, null);

                        connection.output.addMessageToQueue(toClient);
                    }

                    case WHATSAPP_MESSAGE_LIST -> {
                        if(!connection.authorized)
                            throw new IOException("Not Authorized");

                        Map<Correspondence[], TreeSet<Buyer>> map = Queue.listMessagesWhatsApp();
                        byte[] serializedData;

                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                            oos.writeObject(map);
                            serializedData = baos.toByteArray();
                        }

                        MessageToClient toClient = new MessageToClient(MessageTypeToClient.WHATSAPP_MESSAGE_IN_QUEUE,
                                serializedData, null);

                        connection.output.addMessageToQueue(toClient);

                        if(!WhatsAppSender.isAuthorizedPerem()) {
                            connection.output.addMessageToQueue(new MessageToClient(
                                    MessageTypeToClient.WHATSAPP_NEED_AUTHORIZATION,
                                    null,
                                    null
                            ));
                        }
                    }

                    case WHATSAPP_READY_TO_AUTHORIZE -> {
                        if (!connection.authorized)
                            throw new IOException("Not Authorized");

                        BufferedImage qrCodeImage = WhatsAppSender.getQrCode();

                        byte[] imageBytes = null;
                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            ImageIO.write(qrCodeImage, "png", baos);
                            baos.flush();
                            imageBytes = baos.toByteArray();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        connection.output.addMessageToQueue(new MessageToClient(MessageTypeToClient.WHATSAPP_AUTHORIZATION,
                                imageBytes, null));
                    }

                    case GIVE_CLIENT_LIST_WITH_FILTER -> {
                        if (!connection.authorized)
                            throw new IOException("Not Authorized");

                        connection.output.addMessageToQueue(new MessageToClient(MessageTypeToClient.CLIENTS_LIST,
                                Database.getBuyersList((Filter) message.message()).toArray(new Buyer[0]), null));
                    }

                    case GIVE_MANAGERS_LIST -> {
                        if(!connection.authorized)
                            throw new IOException("Not Authorized");

                        connection.output.addMessageToQueue(new MessageToClient(MessageTypeToClient.MANAGERS_LIST,
                                Database.getManagersList().toArray(new String[0]), null));
                    }

                    case ADD_MANAGER_TO_LIST -> {
                        if (!connection.authorized)
                            throw new IOException("Not Authorized");

                        Database.addNewManager((String) message.message());

                        Output.forAll(new MessageToClient(MessageTypeToClient.MANAGERS_LIST,
                                Database.getManagersList().toArray(new String[0]), null));
                    }

                    case DELETE_CLIENT -> {
                        if (!connection.authorized)
                            throw new IOException("Not Authorized");

                        Database.deleteBuyer((Buyer) message.message());
                        Output.forAll(new MessageToClient(MessageTypeToClient.CLIENTS_LIST,
                                Database.getBuyersList(null).toArray(new Buyer[0]), null));
                    }

                    case AUTHORIZATION_PASSWORD -> {
                        boolean isGood = Database.isGoodPassword((String) message.message());
                        if(isGood)
                            connection.authorized = true;


                        connection.output.addMessageToQueue(
                                new MessageToClient(MessageTypeToClient.AUTHORIZATION, isGood, null)
                        );
                    }

                    case ADD_NEW_CLIENT -> {
                        if (!connection.authorized)
                            throw new IOException("Not Authorized");

                        Database.addNewBuyer((Buyer) message.message());

                        Output.forAll(new MessageToClient(MessageTypeToClient.CLIENTS_LIST,
                                Database.getBuyersList(null).toArray(new Buyer[0]), null));
                    }

                    case REDACT_CLIENT -> {
                        if (!connection.authorized)
                            throw new IOException("Not Authorized");

                        Database.editBuyer((Buyer) message.message());
                        Output.forAll(new MessageToClient(MessageTypeToClient.CLIENTS_LIST,
                                Database.getBuyersList(null).toArray(new Buyer[0]), null));
                    }

                    default -> connection.isConnection = false;
                }

            } catch (IOException | ClassNotFoundException e) {
                connection.isConnection = false;

                try {
                    inputStream.close();
                }
                catch (IOException ignored) {}
            }
        }

        try {
            inputStream.close();
        }
        catch (IOException ignored) {}
    }
}
