package examples;

import functions.functions;
import jnr.ffi.Pointer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Java port of 03_ais_assemble.c (results-equivalent).
 * Reads data/ais_instants.csv and writes data/ais_trips_new.csv
 * Columns: timestamp,mmsi,lat,lon,sog
 */
public class ais_assemble {
    private static final int NO_INSTANTS_BATCH = 10_000;
    private static final String INPUT = "src/main/java/examples/data/ais_instants.csv";
    private static final String OUTPUT = "src/main/java/examples/data/ais_trips_new.csv";

    static final class TripRecord {
        final long mmsi;
        final List<String> tripParts = new ArrayList<>(); // "Point(lon lat)@timestamp"
        final List<String> sogParts  = new ArrayList<>(); // "speed@timestamp"

        TripRecord(long mmsi) { this.mmsi = mmsi; }
        int size() { return tripParts.size(); }
    }

    public static void main(String[] args) {
        // Initialize MEOS (UTC like in C code)
        functions.meos_initialize();
        functions.meos_initialize_timezone("UTC");

        long t0 = System.nanoTime();

        // Read CSV and group by MMSI
        Map<Long, TripRecord> byShip = new LinkedHashMap<>();
        int noRecords = 0;
        int noNulls   = 0;

        // Ensure input exists
        if (!Files.exists(Path.of(INPUT))) {
            System.out.println("Error opening input file");
            functions.meos_finalize();
            System.exit(1);
        }

        System.out.printf("Reading the instants (one '*' marker every %d instants)%n", NO_INSTANTS_BATCH);

        try (BufferedReader br = new BufferedReader(new FileReader(INPUT))) {
            // header
            String header = br.readLine(); // drop; mirrors fscanf of header line
            String line;

            while ((line = br.readLine()) != null) {
                // Expected: timestamp,mmsi,lat,lon,sog
                // We keep parsing tight like the C code and skip malformed rows.
                String[] parts = line.split(",", -1);
                if (parts.length != 5) {
                    System.out.println("Record with missing values ignored");
                    noNulls++;
                    continue;
                }
                String ts   = parts[0].trim();
                String mmsi = parts[1].trim();
                String lat  = parts[2].trim();
                String lon  = parts[3].trim();
                String sog  = parts[4].trim();

                // Basic validation like fscanf’s success check
                if (ts.isEmpty() || mmsi.isEmpty() || lat.isEmpty() || lon.isEmpty() || sog.isEmpty()) {
                    System.out.println("Record with missing values ignored");
                    noNulls++;
                    continue;
                }

                long key;
                double dLat, dLon, dSog;
                try {
                    key  = Long.parseLong(mmsi);
                    dLat = Double.parseDouble(lat);
                    dLon = Double.parseDouble(lon);
                    dSog = Double.parseDouble(sog);
                } catch (NumberFormatException nfe) {
                    System.out.println("Record with parse error ignored");
                    noNulls++;
                    continue;
                }

                // Track progress markers like the C code
                noRecords++;
                if (noRecords % NO_INSTANTS_BATCH == 0) {
                    System.out.print("*");
                    System.out.flush();
                }

                // Group by MMSI; keep order of arrival (like arrays in C)
                TripRecord tr = byShip.computeIfAbsent(key, TripRecord::new);

                // Build temporal text atoms (MobilityDB textual format).
                // Geography SRID=4326; exact like geogpoint_make2d in C.
                // We preserve the timestamp as-is; MobilityDB accepts ISO-like forms.
                tr.tripParts.add("Point(" + dLon + " " + dLat + ")@" + ts);
                tr.sogParts.add(dSog + "@" + ts);
            }
        } catch (IOException ioe) {
            System.out.println("Error reading input file");
            functions.meos_finalize();
            System.exit(1);
        }

        System.out.printf("%n%d records read.%n%d incomplete records ignored.%n", noRecords, noNulls);
        System.out.printf("%d trips read.%n", byShip.size());

        // Build temporals and print stats (same as C)
        // Interp is linear by default for numeric and tpoint sequences; we prefix explicitly to be crystal.
        final int PREC = 6; // for output formatting (like C tgeo_out(..., 6))

        // Open output
        try (PrintWriter pw = new PrintWriter(new FileWriter(OUTPUT))) {
            pw.println("mmsi,trip,sog");

            for (TripRecord tr : byShip.values()) {
                if (tr.size() == 0) continue;

                // Build temporal strings
                String tripTxt = "SRID=4326;Interp=Linear;[" + String.join(",", tr.tripParts) + "]";
                String sogTxt  = "Interp=Linear;[" + String.join(",", tr.sogParts) + "]";

                // Parse to native MEOS temporals
                Pointer trip = functions.tgeogpoint_in(tripTxt);
                Pointer sog  = functions.tfloat_in(sogTxt);

                // Stats like in C
                int nInstTrip = functions.temporal_num_instants(trip);
                double length = functions.tpoint_length(trip);
                int nInstSog  = functions.temporal_num_instants(sog);
                double twAvg  = functions.tnumber_twavg(sog);

                System.out.printf("MMSI: %d, Number of input instants: %d%n", tr.mmsi, tr.size());
                System.out.printf("  Trip -> Number of instants: %d, Distance travelled %f%n", nInstTrip, length);
                System.out.printf("  SOG  -> Number of instants: %d, Time-weighted average %f%n", nInstSog, twAvg);

                // CSV output (same functions as C)
                String tripOut = functions.tgeo_out(trip, PREC);
                String sogOut  = functions.tfloat_out(sog, PREC);
                pw.printf("%d,%s,%s%n", tr.mmsi, tripOut, sogOut);
            }
        } catch (IOException ioe) {
            System.out.println("Error writing output file");
            functions.meos_finalize();
            System.exit(1);
        }

        double seconds = (System.nanoTime() - t0) / 1e9;
        System.out.printf("The program took %f seconds to execute%n", seconds);

        functions.meos_finalize();
    }
}