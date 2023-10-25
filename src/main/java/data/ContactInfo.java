package data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record ContactInfo(
        String name,
        String surname,
        String jobTitle,

        long phone,
        boolean isWhatsApp

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 6431916692900786293L;

    public JSONObject toJson() {
        JSONObject object = new JSONObject();

        object.put("name", this.name);
        object.put("surname", this.surname);
        object.put("jobTitle", this.jobTitle);
        object.put("phone", this.phone);
        object.put("isWhatsApp", this.isWhatsApp);

        return object;
    }

    public static ContactInfo fromJson(JSONObject object) {
        String name = object.getString("name");
        String surname = object.getString("surname");
        String jobTitle = object.getString("jobTitle");

        long phone = object.getLong("phone");
        boolean isWhatsApp = object.getBoolean("isWhatsApp");

        return new ContactInfo(name, surname, jobTitle, phone, isWhatsApp);
    }

    public static JSONArray toJsonArray(ContactInfo[] infos) {
        JSONArray array = new JSONArray();
        for (ContactInfo info: infos) {
            array.put(info.toJson());
        }

        return array;
    }

    public static ContactInfo[] fromJsonArray(JSONArray array) {
        List<ContactInfo> infoList = new ArrayList<>(array.length());
        for(int i = 0; i < array.length(); i++) {
            infoList.add(ContactInfo.fromJson(array.getJSONObject(i)));
        }

        return infoList.toArray(new ContactInfo[array.length()]);
    }
}
