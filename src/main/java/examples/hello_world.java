package examples;

import functions.functions;
import types.basic.tpoint.tgeom.TGeomPointInst;
import types.basic.tpoint.tgeom.TGeomPointSeq;
import types.basic.tpoint.tgeom.TGeomPointSeqSet;


public class hello_world {
    public static void main(String[] args) {

        functions.meos_initialize();

        // Input temporal points in WKT format
        String instWKT = "POINT(1 1)@2000-01-01";
        String seqDiscWKT = "{POINT(1 1)@2000-01-01, POINT(2 2)@2000-01-02}";
        String seqLinearWKT = "[POINT(1 1)@2000-01-01, POINT(2 2)@2000-01-02]";
        String seqStepWKT = "Interp=Step;[POINT(1 1)@2000-01-01, POINT(2 2)@2000-01-02]";
        String ssLinearWKT = "{[POINT(1 1)@2000-01-01, POINT(2 2)@2000-01-02],"
                           + "[POINT(3 3)@2000-01-03, POINT(3 3)@2000-01-04]}";
        String ssStepWKT = "Interp=Step;{[POINT(1 1)@2000-01-01, POINT(2 2)@2000-01-02],"
                         + "[POINT(3 3)@2000-01-03, POINT(3 3)@2000-01-04]}";

        // Create temporal objects from WKT
        TGeomPointInst inst = new TGeomPointInst(instWKT);
        TGeomPointSeq seqDisc = new TGeomPointSeq(seqDiscWKT);
        TGeomPointSeq seqLinear = new TGeomPointSeq(seqLinearWKT);
        TGeomPointSeq seqStep = new TGeomPointSeq(seqStepWKT);
        TGeomPointSeqSet ssLinear = new TGeomPointSeqSet(ssLinearWKT);
        TGeomPointSeqSet ssStep = new TGeomPointSeqSet(ssStepWKT);

        // Print MF-JSON outputs

        printResult("Temporal Instant",instWKT,functions.temporal_as_mfjson(inst.getPointInner(),true, 3, 6, null));
        printResult("Temporal Sequence with Discrete Interpolation",seqDiscWKT,functions.temporal_as_mfjson(seqDisc.getPointInner(),true, 3, 6, null));
        printResult("Temporal Sequence with Linear Interpolation",seqLinearWKT,functions.temporal_as_mfjson(seqLinear.getPointInner(),true, 3, 6, null));
        printResult("Temporal Sequence with Step Interpolation",seqStepWKT,functions.temporal_as_mfjson(seqStep.getPointInner(),true, 3, 6, null));
        printResult("Temporal Sequence Set with Linear Interpolation",ssLinearWKT,functions.temporal_as_mfjson(ssLinear.getPointInner(),true, 3, 6, null));
        printResult("Temporal Sequence Set with Step Interpolation",ssStepWKT,functions.temporal_as_mfjson(ssStep.getPointInner(),true, 3, 6, null));

        functions.meos_finalize();
    }

    private static void printResult(String title, String wkt, String mfjson) {
        System.out.println("\n" + "-".repeat(title.length() + 4));
        System.out.println("| " + title + " |");
        System.out.println("-".repeat(title.length() + 4));
        System.out.println("WKT:\n----\n" + wkt + "\n");
        System.out.println("MF-JSON:\n--------\n" + mfjson + "\n");
    }
}
