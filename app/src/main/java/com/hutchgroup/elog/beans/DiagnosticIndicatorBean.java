package com.hutchgroup.elog.beans;

/**
 * Created by Dev-1 on 7/13/2016.
 */
public class DiagnosticIndicatorBean {
    // diagnostic indicator
    public static boolean PowerDiagnosticFg;
    public static boolean EngineSynchronizationDiagnosticFg;
    public static boolean MissingElementDiagnosticFg;
    public static boolean DataTransferDiagnosticFg;
    public static boolean UnidentifiedDrivingDiagnosticFg;
    public static boolean OtherELDIdentifiedDiagnosticFg;

    // malfunction indicator
    public static boolean PowerMalfunctionFg;
    public static boolean EngineSynchronizationMalfunctionFg;
    public static boolean TimingMalfunctionFg;
    public static boolean PositioningMalfunctionFg;
    public static boolean DataRecordingMalfunctionFg;
    public static boolean DataTransferMalfunctionFg;
    public static boolean OtherELDDetectedMalfunctionFg;
}
