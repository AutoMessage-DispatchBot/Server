package data;

import data.StaticData.CorrespondenceType;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Random;

public record Correspondence(CorrespondenceType type, File file, String message, byte[] fileBytes) implements Serializable {
    private static Random random = new Random();

    @Serial
    private static final long serialVersionUID = 7329305886893443047L;

    public File saveFile() {
        File file = new File(random.nextInt(Integer.MAX_VALUE) + "." + this.file.getName());
        try {
            Files.write(file.toPath(), fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
