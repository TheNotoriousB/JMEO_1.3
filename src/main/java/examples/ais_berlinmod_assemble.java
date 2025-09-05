package examples;

import functions.functions;
import jnr.ffi.Pointer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BerlinMOD assembler (strict: only uses functions present in functions.java).
 * Builds per-object tgeompoint sequences via textual parser.
 *
 * Expected CSV: timestamp,object_id,latitude,longitude
 *   e.g., 2008-02-02 12:00:01+00,123,52.5200,13.4050
 *
 * Output: data/berlinmod_trips.csv with columns:
 *   object_id,trip_text
 */
public class ais_berlinmod_assemble {

    // Mirroring previous structure/constants
    private static final int MAX_INSTANTS      = 50_000;
    private static final int NO_INSTANTS_BATCH = 10_000;
    private static final int MAX_TRIPS         = 5;
    private static final int INTERP_LINEAR     = 3; // linear interpolation

    private static final String INPUT_PATH  = "src/main/java/examples/data/berlinmod.csv";
    private static final String OUTPUT_PATH = "src/main/java/examples/data/berlinmod_trips.csv";

    private static final class TripBuf {
        final long objId;
        final StringBuilder geomSeq = new StringBuilder("SRID=4326;[");
        int count = 0;

        TripBuf(long objId) { this.objId = objId; }

        void append(double lon, double lat, String tsOut) {
            if (count > 0) geomSeq.append(", ");
            // Textual format accepted by tgeompointseq_in:
            //   SRID=4326;[Point(lon lat)@<t>, ...]
            geomSeq.append("Point(").append(lon).append(' ').append(lat).append(")@").append(tsOut);
            count++;
        }

        String geomSeqString() { return geomSeq.append(']').toString(); }
    }

    public static void main(String[] args) {
        // Initialize MEOS per the C samples
        functions.meos_initialize();
        functions.meos_initialize_timezone("UTC");

        long tStart = System.nanoTime();

        Map<Long, TripBuf> trips = new LinkedHashMap<>(8, 0.75f, false);

        int noRecords = 0;
        int noNulls   = 0;

        System.out.printf("Reading the instants (one '*' marker every %d instants)%n", NO_INSTANTS_BATCH);

        // Read CSV: timestamp,object_id,latitude,longitude
        try (BufferedReader br = new BufferedReader(new FileReader(INPUT_PATH, StandardCharsets.UTF_8))) {
            // skip header if present
            br.mark(4096);
            String first = br.readLine();
            if (first == null) {
                System.out.println("Empty input file");
                functions.meos_finalize();
                return;
            }
            // If the first line doesn't look like data, treat as header; else process it
            boolean treatAsHeader = first.contains("timestamp") || first.contains("object") || first.contains("lat");
            if (!treatAsHeader) {
                // handle first line as data
                processLine(first, trips);
                noRecords++;
                if (noRecords % NO_INSTANTS_BATCH == 0) {
                    System.out.print("*");
                    System.out.flush();
                }
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                try {
                    processLine(line, trips);
                    noRecords++;
                    if (noRecords % NO_INSTANTS_BATCH == 0) {
                        System.out.print("*");
                        System.out.flush();
                    }
                } catch (Exception ex) {
                    noNulls++;
                }
            }
        } catch (IOException e) {
            System.out.println("Error opening input file");
            functions.meos_finalize();
            return;
        }

        System.out.printf("%n%d records read.%n%d incomplete records ignored.%n", noRecords, noNulls);
        System.out.printf("%d trips read.%n", trips.size());

        // Assemble and print stats using only available functions
        Map<Long, Pointer> tripTemporal = new LinkedHashMap<>();
        for (TripBuf buf : trips.values()) {
            if (buf.count == 0) continue;

            Pointer trip = functions.tgeompointseq_in(buf.geomSeqString(), INTERP_LINEAR);
            tripTemporal.put(buf.objId, trip);

            int    nInst = functions.temporal_num_instants(trip);
            double dist  = functions.tpoint_length(trip);

            System.out.printf("Object: %d, Number of input instants: %d%n", buf.objId, buf.count);
            System.out.printf("  Trip -> Number of instants: %d, Distance travelled %f%n", nInst, dist);
        }

        // Write output CSV: object_id,trip_text
        try (FileWriter fw = new FileWriter(OUTPUT_PATH, StandardCharsets.UTF_8)) {
            fw.write("object_id,trip\n");
            for (Map.Entry<Long, Pointer> e : tripTemporal.entrySet()) {
                long objId = e.getKey();
                Pointer trip = e.getValue();

                String tripStr = functions.tgeo_out(trip, 6);
                fw.write(objId + "," + tripStr + "\n");
            }
        } catch (IOException ioe) {
            System.out.println("Error writing output file: " + ioe.getMessage());
            functions.meos_finalize();
            return;
        }

        double elapsedSec = (System.nanoTime() - tStart) / 1_000_000_000.0;
        System.out.printf("The program took %f seconds to execute%n", elapsedSec);

        functions.meos_finalize();
    }

    // ------- helpers (only Java stdlib + functions.* allowed) -------

    private static void processLine(String line, Map<Long, TripBuf> trips) {
        // Expected: timestamp,object_id,latitude,longitude
        String[] parts = line.split(",", -1);
        if (parts.length != 4) throw new IllegalArgumentException("bad column count");

        String tsStr   = parts[0];
        long   objId   = Long.parseLong(parts[1]);
        double lat     = Double.parseDouble(parts[2]);
        double lon     = Double.parseDouble(parts[3]);

        // Parse timestamptz using provided wrappers, then format to canonical text
        var tsTz  = functions.pg_timestamptz_in(tsStr, -1); // OffsetDateTime
        String ts = functions.timestamptz_out(tsTz);        // String

        TripBuf buf = trips.get(objId);
        if (buf == null) {
            if (trips.size() == MAX_TRIPS) {
                throw new IllegalStateException("MAX_TRIPS exceeded");
            }
            buf = new TripBuf(objId);
            trips.put(objId, buf);
        }
        if (buf.count >= MAX_INSTANTS) return; // soft truncate

        buf.append(lon, lat, ts);
    }
}
