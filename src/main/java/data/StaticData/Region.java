package data.StaticData;

import java.io.Serial;
import java.io.Serializable;

public enum Region implements Serializable {
    NONE("Без типа"),
    VLADIMIRSKAYA("Владимирская"),
    VOLOGODSKAYA("Вологодская"),
    IVANOVSKAYA("Ивановская"),
    KALUZHSKAYA("Калужская"),
    KOSTROMSKAYA("Костромская"),
    MOSKOVSKAYA("Москва/МО"),
    NIZHEGORODSKAYA("Нижегородская"),
    RYAZANSKAYA("Рязанская"),
    SMOLENSKAYA("Смоленская"),
    TVERSKAYA("Тверская"),
    TULSKAYA("Тульская"),
    CHEBOKSARY("Чебоксары"),
    YAROSLAVSKAYA("Ярославская");

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
