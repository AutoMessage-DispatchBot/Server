package data.StaticData;

import java.io.Serial;
import java.io.Serializable;

public enum SourceType implements Serializable {
    NONE("Без типа"),
    CALL("Звонок"),
    VISIT("Объезд"),
    SOCIAL_MEDIA("СМИ"),
    INTERNET("Интернет");

    public final String source;

    SourceType(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return source;
    }

    public static SourceType fromString(String s) {
        for(SourceType source: SourceType.values()) {
            if(source.source.equalsIgnoreCase(s))
                return source;
        }

        return SourceType.NONE;
    }

    @Serial
    private static final long serialVersionUID = 1013611202103003013L;
}
