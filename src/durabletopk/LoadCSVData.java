/**
 * DataLoader
 * ------------
 * Utility class for reading CSV datasets and parsing them into a list of TemporalObject instances.
 *
 * Each row in the dataset should have format: object_id, timestamp, value
 *
 * Supports datasets with arbitrary time resolutions and object counts.
 */

package durabletopk;

import java.io.*;
import java.util.*;

public class LoadCSVData {
    public static List<TemporalObject> loadFromCSV(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine(); // header
        Map<Integer, TemporalObject> objectMap = new HashMap<>();

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            int id = Integer.parseInt(tokens[0]);
            int time = Integer.parseInt(tokens[1]);
            double value = Double.parseDouble(tokens[2]);

            objectMap.putIfAbsent(id, new TemporalObject(id));
            objectMap.get(id).addValue(time, value);
        }

        reader.close();
        return new ArrayList<>(objectMap.values());
    }
}