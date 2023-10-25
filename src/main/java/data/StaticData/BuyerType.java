package data.StaticData;

import java.io.Serial;
import java.io.Serializable;

public enum BuyerType implements Serializable {
    NONE("Без типа"),
    PRIVATE_BUYER("Частное лицо"),
    TRADING_HOUSE("Торговый дом"),
    CONSTRUCTION_COMPANY("Стройка"),
    MARKET("Рынок");

    public final String type;

    BuyerType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    @Serial
    private static final long serialVersionUID = 3891407755999750454L;

    public static BuyerType fromString(String s) {
        for(BuyerType type: BuyerType.values()) {
            if(type.type.equalsIgnoreCase(s))
                return type;
        }

        return BuyerType.NONE;
    }
}
