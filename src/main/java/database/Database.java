package database;

import data.Buyer;
import data.Filter;
import settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String JSON_FILE_NAME = Settings.properties.getProperty("buyers.database");
    private static final String MANAGERS_JSON = Settings.properties.getProperty("managers.database");
    private static final String PASSWORDS_JSON = Settings.properties.getProperty("passwords.database");

    private static int maxId = 0;

    private static final List<Buyer> buyers = readBuyers();
    private static final List<String> managers = readString(MANAGERS_JSON);
    private static final List<String> passwords = readString(PASSWORDS_JSON);

    private Database() {}

    public static boolean isGoodPassword(String password) {
        synchronized (password) {
            return passwords.contains(password) || Settings.properties.getProperty("system.password").contains(password);
        }
    }

    public static void addNewBuyer(Buyer buyer) {
        if (buyer.id() < maxId)
            buyer = new Buyer(maxId++, buyer.companyName(), buyer.address(), buyer.emails(), buyer.site(),
                    buyer.contactInfos(), buyer.manager(), buyer.type(), buyer.region(), buyer.source(), buyer.category(), buyer.additionalInfo());
        else if (buyer.id() == maxId)
            maxId++;

        synchronized (buyers) {
            buyers.add(buyer);
        }
        saveJson(JSON_FILE_NAME, saveBuyers().toString(2));
    }

    public static void editBuyer(Buyer buyer) {
        synchronized (buyers) {
            for (int i = 0; i < buyers.size(); i++) {
                if (buyers.get(i).id() == buyer.id()) {
                    buyers.remove(i);
                    buyers.add(i, buyer);
                    break;
                }
            }
        }
        saveJson(JSON_FILE_NAME, saveBuyers().toString(2));

    }

    public static void deleteBuyer(Buyer buyer) {
        synchronized (buyers) {

            for (int i = 0; i < buyers.size(); i++) {
                if (buyers.get(i).id() == buyer.id()) {
                    buyers.remove(i);
                    break;
                }
            }
        }
        saveJson(JSON_FILE_NAME, saveBuyers().toString(2));
    }


    public static void addNewManager(String manager) {
        synchronized (managers) {
            managers.add(manager);
        }
        saveJson(MANAGERS_JSON, saveString(managers).toString(2));

    }

    public static List<Buyer> getBuyersList(Filter filter) {
        List<Buyer> filteredBuyers = new ArrayList<>();
        if (filter == null)
            return buyers;

        synchronized (buyers) {
            for (Buyer buyer : buyers) {
                if (
                        containsValue(filter.type(), buyer.type())
                                && containsValue(filter.category(), buyer.category())
                                && containsValue(filter.region(), buyer.region())
                                && containsValue(filter.managers(), buyer.manager())
                ) {
                    filteredBuyers.add(buyer);
                }
            }

            return filteredBuyers;

        }
    }

    public static List<String> getManagersList() {
        synchronized (managers) {
            return managers;
        }
    }


    private static  <T> boolean containsValue(T[] array, T value) {
        if (array == null || array.length == 0) {
            return true;
        }

        for (T item : array) {
            if (item.equals(value)) {
                return true;
            }
        }

        return false;
    }

    private static synchronized void saveJson(String fileName, String whatWrite) {
        try {
            File file = new File(fileName);

            PrintWriter pw = new PrintWriter(file);

            pw.print(whatWrite);

            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized JSONArray saveBuyers() {
        JSONArray array = new JSONArray();

        for(Buyer buyer: buyers) {
            array.put(buyer.toJson());
        }

        return array;
    }

    public static synchronized @NotNull JSONArray fromJson(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            File file = new File(fileName);

            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }

            br.close();

            if(stringBuilder.length() > 0)
                return new JSONArray(stringBuilder.toString());

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return new JSONArray();
        }

        return new JSONArray();
    }

    private static synchronized @NotNull List<Buyer> readBuyers() {
        JSONArray array = fromJson(JSON_FILE_NAME);

        List<Buyer> list = new ArrayList<>(array.length());

        for(int i = 0; i < array.length(); i++) {
            Buyer buyer = Buyer.fromJson(array.getJSONObject(i));
            list.add(buyer);
            if (buyer.id() >= maxId)
                maxId = buyer.id() + 1;
        }

        return list;
    }

    private static synchronized @NotNull List<String> readString(String path) {
        JSONArray array = fromJson(path);

        List<String> list = new ArrayList<>(array.length());

        for(int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }

        return list;
    }

    private static synchronized @NotNull JSONArray saveString(List<String> list) {
        JSONArray array = new JSONArray();

        for(String part: list) {
            array.put(part);
        }

        return array;
    }
}
