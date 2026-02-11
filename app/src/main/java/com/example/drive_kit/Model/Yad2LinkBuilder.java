package
com.example.drive_kit.Model;

import com.example.drive_kit.Model.CarModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds a Yad2 filtered search URL for a car.
 * We use fixed IDs from Yad2 (manufacturer + model).
 * If a mapping is missing -> return empty string (button stays disabled).
 */
public class Yad2LinkBuilder {

    private static final String BASE = "https://www.yad2.co.il/vehicles/cars";

    // Manufacturer enum -> Yad2 manufacturer id
    private static final Map<CarModel, Integer> MANUFACTURER_ID = new HashMap<>();

    // Key: "MANUFACTURER:MODEL" (MODEL is enum name like I10, TUCSON, MAZDA3...)
    private static final Map<String, Integer> MODEL_ID = new HashMap<>();

    static {
        // Manufacturers
        MANUFACTURER_ID.put(CarModel.TOYOTA, 19);
        MANUFACTURER_ID.put(CarModel.MAZDA, 27);
        MANUFACTURER_ID.put(CarModel.HONDA, 17);
        MANUFACTURER_ID.put(CarModel.HYUNDAI, 21);

        // Toyota
        MODEL_ID.put("TOYOTA:COROLLA", 10226);
        MODEL_ID.put("TOYOTA:CAMRY", 10222);

        // Mazda
        MODEL_ID.put("MAZDA:MAZDA3", 10332);
        MODEL_ID.put("MAZDA:CX5", 10342);

        // Honda
        MODEL_ID.put("HONDA:CIVIC", 10182);
        MODEL_ID.put("HONDA:CRV", 10183);

        // Hyundai
        MODEL_ID.put("HYUNDAI:I10", 10272);
        MODEL_ID.put("HYUNDAI:TUCSON", 10291);
    }

    private Yad2LinkBuilder() {
        // no instances
    }

    /**
     * Build full URL:
     * https://www.yad2.co.il/vehicles/cars?manufacturer=XX&model=YYYY&year=2014-2014
     */
    public static String build(CarModel manufacturer, String modelEnumName, int year) {
        if (manufacturer == null || modelEnumName == null || year <= 0) return "";

        Integer manId = MANUFACTURER_ID.get(manufacturer);
        Integer modId = MODEL_ID.get(manufacturer.name() + ":" + modelEnumName);

        if (manId == null || modId == null) return "";

        return BASE
                + "?manufacturer=" + manId
                + "&model=" + modId
                + "&year=" + year + "-" + year;
    }
}
