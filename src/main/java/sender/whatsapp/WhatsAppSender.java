package sender.whatsapp;

import data.Buyer;
import data.ContactInfo;
import data.Correspondence;
import senderData.MessageToClient;
import senderData.MessageTypeToClient;
import gate.Output;
import sender.ForSender;
import sender.Queue;
import settings.Settings;
import settings.Main;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

public class WhatsAppSender extends Thread {

    private static WebDriver driver;
    private static long lastUpdate;
    private static boolean isAuthorized = false;
    private int sendedMessages = 0;
    private LocalDate savedDate = LocalDate.now();

    @Override
    public synchronized void run() {
        load();
        Random random = new Random();

        while (true) {
            while (!isAuthorized) {
                Output.forAll(new MessageToClient(MessageTypeToClient.WHATSAPP_NEED_AUTHORIZATION, null, null));
                try {
                    synchronized (Main.sleep) {
                        Main.sleep.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if(!isAuthorized())
                continue;

            LocalTime currentTime = LocalTime.now();
            LocalDate currentDate = LocalDate.now();
            LocalTime sleepStartTime = LocalTime.of(20, 0);
            LocalTime sleepEndTime = LocalTime.of(8, 0);

            if(currentDate.isAfter(savedDate)) {
                sendedMessages = 0;
                savedDate = LocalDate.now();
            }

            if (currentTime.isAfter(sleepStartTime) || currentTime.isBefore(sleepEndTime)) {
                try {
                    Thread.sleep(10 * 60 * 1000);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(sendedMessages > Integer.parseInt(Settings.properties.getProperty("whatsapp.max.messages"))) {
                try {
                    Thread.sleep(60 * 60 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                continue;
            }

            if(lastUpdate + 3600 * 1000 < System.currentTimeMillis())
                setupRefresh();

            if(Queue.isNewWhatsApp()) {
                ForSender sender = Queue.getWhatsAppMessage();

                if(sender == null)
                    continue;

                ContactInfo[] contacts = sender.buyer().contactInfos();
                Correspondence[] correspondence = sender.message();

                for(int i = 0; i < contacts.length; i++) {
                    try {
                        if (!contacts[i].isWhatsApp())
                            continue;
                        sendedMessages++;

                        String number = "+" + contacts[i].phone();
                        WebElement inputField = driver.findElement(By.cssSelector("[data-testid='chat-list-search']"));

                        for (char c : number.toCharArray()) {
                            inputField.sendKeys(String.valueOf(c));
                            Thread.sleep(random.nextInt(50, 100));
                        }

                        inputField.sendKeys(Keys.ENTER);
                    } catch (NoSuchElementException | ElementNotInteractableException e) {
                        e.printStackTrace();
                        if(!isAuthorized()) {
                            Queue.addNewMessagesWhatsApp(correspondence, new Buyer[]{sender.buyer()});
                            break;
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        Thread.sleep(random.nextInt(5500, 7500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }



                    for(Correspondence message: correspondence) {
                        try {
                            switch (message.type()) {
                                case NONE -> {
                                    if(message.message() == null || message.message().length() == 0)
                                        continue;

                                    WebElement messageField = driver.findElement(By.cssSelector("[data-testid='conversation-compose-box-input']"));

                                    for (char c : message.message().toCharArray()) {
                                        messageField.sendKeys(String.valueOf(c));
                                        Thread.sleep(random.nextInt(50, 100));
                                    }

                                    messageField.sendKeys(Keys.ENTER);
                                    Thread.sleep(random.nextInt(4500, 7500));

                                }

                                case PHOTO_VIDEO -> {
                                    WebElement plusButton = driver.findElement(By.cssSelector("div[data-tab='10']._3ndVb"));
                                    plusButton.click();
                                    Thread.sleep(random.nextInt(1500, 3000));

                                    WebElement videoInput = driver.findElement(By.xpath("(//input[@type='file'])"));
                                    videoInput.sendKeys(message.file().getAbsolutePath());

                                    Thread.sleep(random.nextInt(3500, 6500));

                                    WebElement messageField = driver.findElement(By.cssSelector("[data-testid='media-caption-input-container']"));

                                    if(!(message.message() == null || message.message().length() == 0)) {
                                        for (char c : message.message().toCharArray()) {
                                            messageField.sendKeys(String.valueOf(c));
                                            Thread.sleep(random.nextInt(50, 100));
                                        }
                                    }

                                    messageField.sendKeys(Keys.ENTER);
                                    Thread.sleep(random.nextInt(3500, 6500));
                                }

                                case FILE -> {
                                    WebElement plusButton = driver.findElement(By.cssSelector("div[data-tab='10']._3ndVb"));
                                    plusButton.click();
                                    Thread.sleep(random.nextInt(1500, 2500));

                                    WebElement fileInput = driver.findElement(By.xpath("(//input[@type='file'])[2]"));
                                    fileInput.sendKeys(message.file().getAbsolutePath());

                                    Thread.sleep(random.nextInt(4500, 7000));

                                    WebElement messageField = driver.findElement(By.cssSelector("[data-testid='media-caption-input-container']"));

                                    if(!(message.message() == null || message.message().length() == 0)) {
                                        for (char c : message.message().toCharArray()) {
                                            messageField.sendKeys(String.valueOf(c));
                                            Thread.sleep(random.nextInt(50, 100));
                                        }
                                    }

                                    messageField.sendKeys(Keys.ENTER);
                                    Thread.sleep(random.nextInt(3000, 6500));
                                }

                                case CONTACT -> {
                                    WebElement plusButton = driver.findElement(By.cssSelector("div[data-tab='10']._3ndVb"));
                                    plusButton.click();
                                    Thread.sleep(random.nextInt(1500, 3500));

                                    WebElement contact = driver.findElement(By.cssSelector("[data-testid='mi-attach-contact']"));
                                    contact.click();
                                    Thread.sleep(random.nextInt(2000, 2800));

                                    WebElement contactField = driver.findElement(By.cssSelector("[data-testid='chat-list-search']"));
                                    for (char c : message.message().toCharArray()) {
                                        contactField.sendKeys(String.valueOf(c));
                                        Thread.sleep(random.nextInt(50, 100));
                                    }
                                    Thread.sleep(random.nextInt(1700, 3000));

                                    contactField.sendKeys(Keys.ENTER);
                                    Thread.sleep(random.nextInt(1700, 3000));

                                    WebElement sendButton = driver.findElement(By.cssSelector("div[data-animate-btn='true'] div[role='button']"));
                                    sendButton.click();
                                    Thread.sleep(random.nextInt(1700, 3000));

                                    sendButton = driver.findElement(By.cssSelector(".lhggkp7q.druapeav .p357zi0d.gndfcl4n.ac2vgrno"));
                                    sendButton.click();
                                    Thread.sleep(random.nextInt(1700, 3000));
                                }
                            }

                        } catch (NoSuchElementException | ElementNotInteractableException e) {
                            e.printStackTrace();

                            isAuthorized();

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    driver.navigate().refresh();
                    try {
                        Thread.sleep(random.nextInt(9500, 11500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            } else {
                try {
                    synchronized (Main.sleep) {
                        Main.sleep.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static synchronized void setupRefresh() {
        driver.get("https://web.whatsapp.com/");

        lastUpdate = System.currentTimeMillis();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static synchronized void reload() {
        driver.quit();
        load();
    }

    private static synchronized void load() {
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);

        driver = new FirefoxDriver(options);

        driver.get("https://web.whatsapp.com/");

        lastUpdate = System.currentTimeMillis();

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized BufferedImage getQrCode() {
        driver.navigate().refresh();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            WebElement qrCodeElement = driver.findElement(By.cssSelector("div[data-testid='qrcode']"));
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            BufferedImage fullScreenImage = ImageIO.read(screenshotFile);

            BufferedImage qrCodeImage = fullScreenImage.getSubimage(qrCodeElement.getLocation().getX()-10,
                    qrCodeElement.getLocation().getY()-10,
                    qrCodeElement.getSize().getWidth()+20,
                    qrCodeElement.getSize().getHeight()+20);

            lastUpdate = System.currentTimeMillis();

            return qrCodeImage;
        } catch (IOException e) {
            reload();
            return getQrCode();
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            e.printStackTrace();

            isAuthorized = true;
            synchronized (Main.sleep) {
                Main.sleep.notifyAll();
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            if(isAuthorized) {
                try {
                    return ImageIO.read(new File(Settings.properties.getProperty("icon.image")));
                } catch (IOException ex) {
                    e.printStackTrace();
                    return null;
                }
            }
            else
                return getQrCode();
        }
    }

    private synchronized boolean isAuthorized() {
        try {
            driver.findElement(By.xpath("//p[contains(@class, 'selectable-text copyable-text iq0m558w')]"));
            isAuthorized = true;
            return true;
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            e.printStackTrace();

            isAuthorized = false;
            return false;
        }
    }

    public static synchronized boolean isAuthorizedPerem() {
        return isAuthorized;
    }

    public static synchronized void setAuthorized() {
        isAuthorized = true;
        synchronized (Main.sleep) {
            Main.sleep.notifyAll();
        }
    }
}
