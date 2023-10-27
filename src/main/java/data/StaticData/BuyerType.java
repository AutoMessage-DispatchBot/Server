package data.StaticData;

import java.io.Serial;
import java.io.Serializable;

public enum BuyerType implements Serializable {
    NONE("Без типу"),
    PRIVATE_BUYER("Часний"),
    TRADING_HOUSE("Торговий дім"),
    CONSTRUCTION_COMPANY("Будівництво"),
    MARKET("Ринок");

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
