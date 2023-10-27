package data;

import data.StaticData.BuyerType;
import data.StaticData.ClientCategory;
import data.StaticData.Region;
import data.StaticData.SourceType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

public record Buyer(
        int id,
        String companyName,
        String address,

        String[] emails,
        String site,

        ContactInfo[] contactInfos,
        String manager,

        BuyerType type,
        Region region,
        SourceType source,
        ClientCategory category,

        String additionalInfo

) implements Serializable, Comparable<Buyer> {

    @Serial
    private static final long serialVersionUID = -843256654065831224L;

    @Override
    public int compareTo(Buyer o) {
        return Integer.compare(this.id, o.id);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("id", this.id);
        json.put("companyName", this.companyName);
        json.put("address", this.address);

        JSONArray emailArray = new JSONArray();
        for(String email: emails)
            emailArray.put(email);

        json.put("emails", emailArray);
        json.put("site", this.site);

        json.put("contactInfo", ContactInfo.toJsonArray(this.contactInfos));
        json.put("manager", this.manager);

        json.put("type", this.type.toString());
        json.put("region", this.region.toString());
        json.put("source", this.source.toString());
        json.put("category", this.category.toString());

        json.put("additionalInfo", this.additionalInfo);

        return json;
    }

    public Object[] getLine() {
        StringBuilder emails = new StringBuilder();
        for(String s: this.emails)
            emails.append(s).append("\n");

        StringBuilder contacts = new StringBuilder();
        for(ContactInfo s: this.contactInfos)
            contacts.append(s.name()).append(" - ").append(s.phone()).append("\n").append(s.jobTitle());

        return new Object[]
                {false, "Редагувати", "Видалити", "Перегляд", this.id, this.companyName, this.type.toString(),
                        this.region.toString(), this.manager, this.category.toString(), this.source.toString(),
                        emails.toString(), contacts.toString(), this.additionalInfo};
    }

    public static Buyer fromJson(JSONObject object) {
        int id = object.getInt("id");
        String companyName = object.getString("companyName");
        String address = object.getString("address");

        JSONArray emailArray = object.getJSONArray("emails");
        String[] emails = new String[emailArray.length()];
        for(int i = 0; i < emailArray.length(); i++)
            emails[i] = emailArray.getString(i);

        String site = object.getString("site");

        ContactInfo[] contactInfos = ContactInfo.fromJsonArray(object.getJSONArray("contactInfo"));
        String manager = object.getString("manager");

        BuyerType type = BuyerType.fromString(object.getString("type"));
        Region region = Region.fromString(object.getString("region"));
        SourceType source = SourceType.fromString(object.getString("source"));
        ClientCategory category = ClientCategory.fromString(object.getString("category"));

        String additionalInfo = object.getString("additionalInfo");

        return new Buyer(id, companyName, address, emails, site, contactInfos, manager, type, region, source, category, additionalInfo);
    }










    public static class COMPARE_BY_ID implements Comparator<Buyer> {
        @Override
        public int compare(Buyer o1, Buyer o2) {
            return Integer.compare(o1.id, o2.id);
        }
    }

    public static class COMPARE_BY_NUMBER implements Comparator<Buyer> {
        @Override
        public int compare(Buyer o1, Buyer o2) {
            int one = Integer.MAX_VALUE;
            int two = Integer.MAX_VALUE;

            if(o1.contactInfos.length > 0)
                one = (int) o1.contactInfos[0].phone();

            if(o2.contactInfos.length > 0)
                two = (int) o2.contactInfos[0].phone();

            return Integer.compare(one, two);
        }
    }

    public static class COMPARE_BY_ALPHABET implements Comparator<Buyer> {
        @Override
        public int compare(Buyer o1, Buyer o2) {
            return o1.companyName.compareTo(o2.companyName);
        }
    }

    public static class COMPARE_BY_MANAGER implements  Comparator<Buyer> {
        @Override
        public int compare(Buyer o1, Buyer o2) {
            return o1.manager.compareTo(o2.manager);
        }
    }
}
