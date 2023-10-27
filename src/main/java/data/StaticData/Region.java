package data.StaticData;

import java.io.Serial;
import java.io.Serializable;

public enum Region implements Serializable {
    NONE("Без типа"),
    KYIVSKA("Киівська"),
    CHERNIGIVSKA("Чернігівська"),
    POLTAVSKA("Полтавська"),
    SUMSKA("Сумська"),
    CHERKASSKA("Черкаська"),
    ZHYTOMYRSKA("Житомирська"),
    VINNITSKA("Вінницька"),
    DNIPROPETROVSKA("Дніпропетровська"),
    HMELNYTSKA("Хмельницька"),
    TERNOPILSKA("Тернопільська");

    public final String region;

    Region(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return region;
    }

    public static Region fromString(String s) {
        for(Region region: Region.values()) {
            if(region.region.equalsIgnoreCase(s))
                return region;
        }

        return Region.NONE;
    }

    @Serial
    private static final long serialVersionUID = 5579281987458478920L;
}
