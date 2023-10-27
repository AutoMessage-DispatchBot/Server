package data.StaticData;

import java.io.Serial;
import java.io.Serializable;

public enum CorrespondenceType implements Serializable {
    NONE("Текст"),
    PHOTO_VIDEO("Фото/Відео"),
    FILE("Файл"),
    CONTACT("Контакт");

    private final String name;

    CorrespondenceType(String name) {
        this.name = name;
    }

    @Serial
    private static final long serialVersionUID = -4528960001115879765L;

    @Override
    public String toString() {
        return this.name;
    }
}
