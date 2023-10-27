package sender.email;

import data.Buyer;
import sender.ForSender;
import sender.Queue;
import settings.Settings;
import settings.Main;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class GmailSender extends Thread {
    private static final String username = Settings.properties.getProperty("email.username");
    private static final String password = Settings.properties.getProperty("email.password");

    private int sendedMessages = 0;
    private LocalDate savedDate = LocalDate.now();


    @Override
    public synchronized void run() {
        while (true) {
            while(!Queue.isNewGmail()) {
                try {
                    synchronized (Main.sleep) {
                        Main.sleep.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            LocalDate currentDate = LocalDate.now();

            if(currentDate.isAfter(savedDate)) {
                sendedMessages = 0;
                savedDate = LocalDate.now();
            }



            if(sendedMessages > Integer.parseInt(Settings.properties.getProperty("email.max.messages"))) {
                try {
                    Thread.sleep(60 * 60 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                continue;
            }


            ForSender toSend = Queue.getGmailMessage();
            List<Buyer> buyersList = new ArrayList<>();
            buyersList.add(toSend.buyer());
            sendedMessages += toSend.buyer().emails().length;

            while (Queue.isNewGmail()) {
                if(sendedMessages > Integer.parseInt(Settings.properties.getProperty("email.max.messages")))
                    break;

                ForSender sendLoop = Queue.getGmailMessage();
                if(sendLoop == null)
                    break;

                if(!sendLoop.message().equals(toSend.message())) {
                    Queue.addNewMessagesGmail(sendLoop.message(), new Buyer[]{sendLoop.buyer()});
                    break;
                }

                buyersList.add(sendLoop.buyer());
                sendedMessages += toSend.buyer().emails().length;
            }

            List<String> recipients = new ArrayList<>();
            for(Buyer buyer: buyersList) {
                for(String email: buyer.emails()) {
                    if(email.contains("@"))
                        recipients.add(email);
                }
            }

            String subject = toSend.message()[0].message();
            String body = toSend.message()[1].message();
            File[] files = null;
            if(toSend.message().length > 2) {
                files = new File[toSend.message().length - 2];
                for(int i = 2; i < toSend.message().length; i++) {
                    files[i-2] = toSend.message()[i].file();
                }
            }

            try {
                sendEmail(recipients.toArray(new String[0]), subject, body, files);
            } catch (MessagingException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void sendEmail(String[] recipients, String subject, String body, File[] files) throws MessagingException, IOException {
        // Настройки SMTP сервера Yandex
        String host = Settings.properties.getProperty("smtp.host");
        int port = Integer.parseInt(Settings.properties.getProperty("smtp.port"));


        // Настройка свойств для соединения с SMTP сервером
        Properties prop = new Properties();
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.port", port);
        prop.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        InternetAddress[] recipientAddresses = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            recipientAddresses[i] = new InternetAddress(recipients[i]);
        }

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(username));
        msg.setRecipients(Message.RecipientType.BCC, recipientAddresses);

        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // Создание основной части письма
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body, "UTF-8");

        // Создание вложений
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        if (files != null) {
            for (File file : files) {
                if (file.exists()) {
                    MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                    attachmentBodyPart.attachFile(file);
                    multipart.addBodyPart(attachmentBodyPart);
                }
            }
        }

        msg.setContent(multipart);

        Transport.send(msg);
    }
}
