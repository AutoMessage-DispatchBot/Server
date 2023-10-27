package settings;

import gate.ConnectionGate;
import sender.Queue;
import sender.email.GmailSender;
import sender.whatsapp.WhatsAppSender;

public class Main {
    public static final String sleep = "";

    public static void main(String[] args) {
        new ConnectionGate().start();
        new Queue().start();
        new WhatsAppSender().start();
        new GmailSender().start();
    }
}
