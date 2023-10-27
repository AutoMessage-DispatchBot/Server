package sender;

import data.Buyer;
import data.Correspondence;
import settings.Settings;
import settings.Main;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

public class Queue extends Thread {

    private static boolean isNewWhatsApp = false;
    private static boolean isNewGmail = false;

    private static final String newMessagesWhatsAppName = Settings.properties.getProperty("new.messages.whatsapp.name");
    private static final String newMessagesGmailName = Settings.properties.getProperty("new.messages.email.name");

    private static final Map<Correspondence[], TreeSet<Buyer>> newMessagesWhatsApp = readMap(newMessagesWhatsAppName);
    private static final Map<Correspondence[], TreeSet<Buyer>> newMessagesGmail = readMap(newMessagesGmailName);

    private static final List<File> toDelete = new LinkedList<>();

    @Override
    public synchronized void run() {
        while (true) {

            LocalTime currentTime = LocalTime.now();
            LocalTime sleepStartTime = LocalTime.of(5, 0);
            LocalTime sleepEndTime = LocalTime.of(22, 0);

            if (currentTime.isAfter(sleepStartTime) || currentTime.isBefore(sleepEndTime)) {
                for(File file: toDelete) {
                    if(file.exists()) {
                        if(file.delete())
                            toDelete.remove(file);
                    } else
                        toDelete.remove(file);
                }
            }




            try {
                Thread.sleep(60*60*1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addNewMessagesWhatsApp(Correspondence[] message, Buyer[] buyers) {
        if (isGoodMessage(message)) {
            synchronized (newMessagesWhatsApp) {
                addNewMessages(newMessagesWhatsApp, message, buyers);
                isNewWhatsApp = true;
                writeMap(newMessagesWhatsApp, newMessagesWhatsAppName);
            }
        }
    }

    public static void addNewMessagesGmail(Correspondence[] message, Buyer[] buyers) {
        if (isGoodMessage(message)) {
            synchronized (newMessagesGmail) {
                addNewMessages(newMessagesGmail, message, buyers);
                isNewGmail = true;
                writeMap(newMessagesGmail, newMessagesGmailName);
            }
        }
    }

    private static synchronized boolean isGoodMessage(Correspondence[] message) {
        if(message.length == 0)
            return false;

        int messageLength = message.length;
        for(int i = 0; i < message.length; i++) {
            if(message[i].file() == null) {
                if (message[i].message() == null)
                    messageLength--;
                else if (message[i].message().length() == 0) {
                    messageLength--;
                }
            }
        }

        return messageLength != 0;
    }

    private static synchronized void addNewMessages(Map<Correspondence[], TreeSet<Buyer>> map, Correspondence[] message, Buyer[] buyers) {
        Set<Correspondence[]> keys = map.keySet();
        boolean isNotHere = true;

        for(Correspondence[] key: keys) {
            if(key.length != message.length)
                continue;

            int good = key.length;
            for(int i = 0; i < key.length; i++) {
                if(key[i].message() == null && message[i].message() == null)
                    good--;
                else if((key[i].message() == null && message[i].message() != null) || (key[i].message() != null && message[i].message() == null))
                    break;
                else if (key[i].message().contains(message[i].message())) {
                    good--;
                } else {
                    break;
                }
            }

            if(good == 0) {
                TreeSet<Buyer> treeSet = map.get(key);
                treeSet.addAll(List.of(buyers));

                map.put(key, treeSet);
                isNotHere = false;

                break;
            }
        }

        if(isNotHere) {
            for(int i = 0; i < message.length; i++) {
                if(message[i].file() != null) {
                    Correspondence newCorr = new Correspondence(message[i].type(), message[i].saveFile(), message[i].message(), null);
                    message[i] = newCorr;
                }
            }

            TreeSet<Buyer> treeSet = map.getOrDefault(message, new TreeSet<>());
            treeSet.addAll(List.of(buyers));

            map.put(message, treeSet);
        }

        synchronized (Main.sleep) {
            Main.sleep.notifyAll();
        }
    }

    public static void deleteMessagesWhatsApp(Correspondence[] message, Buyer[] buyers) {
        synchronized (newMessagesWhatsApp) {
            deleteMessages(newMessagesWhatsApp, message, buyers);
            writeMap(newMessagesWhatsApp, newMessagesWhatsAppName);
        }
    }

    public static void deleteMessagesGmail(Correspondence[] message, Buyer[] buyers) {
        synchronized (newMessagesGmail) {
            deleteMessages(newMessagesGmail, message, buyers);
            writeMap(newMessagesGmail, newMessagesGmailName);
        }
    }

    private static synchronized void deleteMessages(Map<Correspondence[], TreeSet<Buyer>> map, Correspondence[] message, Buyer[] buyers) {
        for(Correspondence[] correspondences: map.keySet()) {
            if (correspondences.length != message.length)
                continue;
            int isThat = correspondences.length;

            for (int i = 0; i < correspondences.length; i++) {
                if (correspondences[i].message() == null && message[i].message() == null)
                    isThat--;

                else if ((correspondences[i].message() == null && message[i].message() != null) ||
                        (correspondences[i].message() != null && message[i].message() == null))
                    break;

                else if (correspondences[i].message().contains(message[i].message()))
                    isThat--;
            }

            if (isThat == 0) {
                TreeSet<Buyer> treeSet = map.getOrDefault(correspondences, new TreeSet<>());
                List.of(buyers).forEach(treeSet::remove);

                map.put(correspondences, treeSet);
            }
        }
    }

    public static synchronized ForSender getWhatsAppMessage() {
        ForSender buyer;
        synchronized (newMessagesWhatsApp) {
            buyer = getMessage(newMessagesWhatsApp);
            writeMap(newMessagesWhatsApp, newMessagesWhatsAppName);
        }
        if(buyer == null)
            isNewWhatsApp = false;

        return buyer;
    }

    public static synchronized ForSender getGmailMessage() {
        ForSender buyer;
        synchronized (newMessagesGmail) {
            buyer = getMessage(newMessagesGmail);
            writeMap(newMessagesGmail, newMessagesGmailName);
        }
        if(buyer == null)
            isNewGmail = false;

        return buyer;
    }

    public static synchronized ForSender getMessage(Map<Correspondence[], TreeSet<Buyer>> map) {
        Set<Correspondence[]> keys = map.keySet();
        for (Correspondence[] s: keys) {
            TreeSet<Buyer> buyers = map.get(s);
            if(buyers.size() == 0) {

                for(Correspondence corr: s) {
                    if(corr.file() != null)
                        if(corr.file().exists())
                            toDelete.add(corr.file());
                }

                map.remove(s);
                continue;
            }

            Buyer buyer = buyers.first();
            buyers.remove(buyer);
            map.put(s, buyers);

            return new ForSender(s, buyer);
        }

        return null;
    }

    public static synchronized boolean isNewWhatsApp() {
        return isNewWhatsApp;
    }

    public static synchronized boolean isNewGmail() {
        return isNewGmail;
    }

    public static Map<Correspondence[], TreeSet<Buyer>> listMessagesWhatsApp() {
        synchronized (newMessagesWhatsApp) {
            isEmpty(newMessagesWhatsApp);
        }

        return newMessagesWhatsApp;
    }

    public static synchronized Map<Correspondence[], TreeSet<Buyer>> listMessagesGmail() {
        synchronized (newMessagesGmail) {
            isEmpty(newMessagesGmail);
        }

        return newMessagesGmail;
    }


    private static void isEmpty(Map<Correspondence[], TreeSet<Buyer>> map) {
        synchronized (map) {
            for (Correspondence[] s : map.keySet()) {
                TreeSet<Buyer> buyers = map.get(s);
                if (buyers.size() == 0) {

                    for (Correspondence corr : s) {
                        if (corr.file() != null)
                            if (corr.file().exists())
                                toDelete.add(corr.file());
                    }

                    map.remove(s);
                }
            }
        }
    }

    private static Map<Correspondence[], TreeSet<Buyer>> readMap(String name) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(name));
            return (Map<Correspondence[], TreeSet<Buyer>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new LinkedHashMap<>();
    }

    private static void writeMap(Map<Correspondence[], TreeSet<Buyer>> map, String name) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(name));
            oos.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}