package examples;

import functions.functions;
import jnr.ffi.Pointer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;

public class ais_read {
    public static void main(String[] args) {
        functions.meos_initialize();
        
        functions.meos_initialize_timezone("UTC");

        String filePath = "src/main/java/examples/data/ais_instants.csv";
        int noRecords = 0;
        int noNulls = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine(); // Skip header
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    System.out.println("Record with missing values ignored");
                    noNulls++;
                    continue;
                }

                try {
                    String timestampStr = parts[0];
                    long mmsi = Long.parseLong(parts[1]);
                    double latitude = Double.parseDouble(parts[2]);
                    double longitude = Double.parseDouble(parts[3]);
                    double sog = Double.parseDouble(parts[4]);
                    
                    LocalDateTime lt = functions.pg_timestamp_in(timestampStr, -1);
                    OffsetDateTime ot = functions.pg_timestamptz_in(timestampStr, -1);
                    noRecords++;

                    if (noRecords % 1000 == 0) {
                        String pointWKT = String.format("SRID=4326;Point(%f %f)@%s+00", longitude, latitude,
                                functions.pg_timestamp_out(lt));

                        Pointer location = functions.tgeogpoint_in(pointWKT);
                        String locationText = functions.tspatial_as_text(location, 2);

                        Pointer sogInst = functions.tfloatinst_make(sog,ot);
                        String sogText = functions.tfloat_out(sogInst, 2);

                        System.out.printf("MMSI: %d, Location: %s SOG: %s%n", mmsi, locationText, sogText);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Malformed record ignored");
                    noNulls++;
                }
            }

        } catch (IOException e) {
            System.out.println("Error opening input file");
            return;
        }

        System.out.printf("\n%d records read.\n%d incomplete records ignored.\n", noRecords, noNulls);

        functions.meos_finalize();
    }
}
