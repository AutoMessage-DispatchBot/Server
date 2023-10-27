package data.StaticData;

import java.io.Serial;
import java.io.Serializable;

public enum SourceType implements Serializable {
    NONE("Без типу"),
    CALL("Дзвінок"),
    SOCIAL_MEDIA("смі"),
    INTERNET("інтернет");

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
