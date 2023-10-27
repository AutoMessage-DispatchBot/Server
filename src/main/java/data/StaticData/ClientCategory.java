package data.StaticData;

import java.io.Serial;
import java.io.Serializable;

public enum ClientCategory implements Serializable {
    NONE("Без типу"),
    SMALL("Малий бзнес"),
    MEDIUM("Середній бізнес"),
    LARGE("Крупний бізнес"),
    VIP("VIP");

    public final String category;

    ClientCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return category;
    }

    public static ClientCategory fromString(String s) {
        for(ClientCategory category: ClientCategory.values()) {
            if(category.category.equalsIgnoreCase(s))
                return category;
        }

        return ClientCategory.NONE;
    }

    @Serial
    private static final long serialVersionUID = -4038714168171180637L;
}
