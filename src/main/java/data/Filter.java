package data;

import data.StaticData.BuyerType;
import data.StaticData.ClientCategory;
import data.StaticData.Region;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

public record Filter(BuyerType[] type, ClientCategory[] category, Region[] region, String[] managers) implements Serializable {

    @Serial
    private static final long serialVersionUID = -8820169599431792419L;

    @Override
    public String toString() {
        return Arrays.toString(type) + "\n" + Arrays.toString(category) + "\n" + Arrays.toString(region) + "\n" + Arrays.toString(managers) + "\n";
    }
}
