package com.hutchgroup.elog.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Deepak.Sharma on 7/20/2015.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String TABLE_CARRIER = "Carrier";
    public static final String TABLE_ACCOUNT = "Account";
    public static final String TABLE_STATE = "State";
    public static final String TABLE_PLACE = "Place";
    public static final String TABLE_DAILYLOG = "DailyLog";
    public static final String TABLE_DAILYLOG_EVENT = "DailyLog_Event";
    public static final String TABLE_DAILYLOG_CODRIVER = "DailyLog_CoDriver";
    public static final String TABLE_RULE = "Rule";
    public static final String TABLE_DAILYLOG_RULE = "DailyLog_Rule";
    public static final String TABLE_EVENT_TYPE = "Event_Type";
    public static final String TABLE_TRACKING = "Event_Tracking";
    public static final String TABLE_GpsLocation = "GpsLocation";
    public static final String TABLE_Message = "Message";
    public static final String TABLE_TRIP_INSPECTION = "Trip_Inspection";
    public static final String TABLE_VERSION_INFORMATION = "Version_Information";
    public static final String TABLE_SETTINGS = "Settings";
    public static final String TABLE_DIAGNOSTIC_INDICATOR = "DiagnosticIndicator";
    public static final String TABLE_DTC = "DTCCODE";

    public static final String COLUMN_ID = "_id";
    private static final String DATABASE_NAME = "EDL.db";
    private static final int DATABASE_VERSION = 7;

    private static final String TABLE_CREATE_CARRIER = "create table "
            + TABLE_CARRIER
            + "(CompanyId INTEGER,CarrierName text,ELDManufacturer text,USDOT text,VehicleId INTEGER,UnitNo text,PlateNo text,VIN text,StatusId INTEGER,SerialNo text,MACAddress text,TimeZoneId text)";

    private static final String TABLE_CREATE_ACCOUNT = "create table "
            + TABLE_ACCOUNT
            + "(AccountId INTEGER,FirstName text,LastName text,EmailId text,MobileNo text,AccountType INTEGER,DrivingLicense text,DLIssueState text,LicenseExpiryDate text,Username text," +
            "Password text,DotPassword text,Salt text,ExemptELDUseFg INTEGER,ExemptionRemarks text,SpecialCategory INTEGER,CurrentRule INTEGER,TimeZoneOffsetUTC text,LicenseAcceptFg INTEGER,StatusId INTEGER,SyncFg INTEGER)";

    private static final String TABLE_CREATE_PLACE = "create table "
            + TABLE_PLACE
            + "(FEATURE_ID INTEGER PRIMARY KEY,FEATURE_NAME text ,FEATURE_CLASS text ,STATE_ALPHA text ,STATE_NUMERIC text ,COUNTY_NAME text ,COUNTY_NUMERIC text ,PRIMARY_LAT_DMS text ,PRIM_LONG_DMS text ,PRIM_LAT_DEC text ,PRIM_LONG_DEC text ,SOURCE_LAT_DMS text ,SOURCE_LONG_DMS text ,SOURCE_LAT_DEC text ,SOURCE_LONG_DEC text ,ELEV_IN_M text ,ELEV_IN_FT text ,MAP_NAME text ,DATE_CREATED text ,DATE_EDITED text)";

    private static final String TABLE_CREATE_STATE = "create table "
            + TABLE_STATE
            + "(StateId INTEGER,CountryId INTEGER,StateName text,ShortStateName text)";
    private static final String TABLE_CREATE_DAILYLOG = "create table "
            + TABLE_DAILYLOG
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,OnlineDailyLogId INTEGER,LogDate text,DriverId INTEGER,ShippingId text,TrailerId text,Commodity text,StartTime text,StartOdometerReading text,EndOdometerReading text," +
            "CertifyFG INTEGER,certifyCount INTEGER,Signature text,DrivingTimeRemaining INTEGER,WorkShiftTimeRemaining INTEGER,TimeRemaining70 INTEGER,TimeRemaining120 INTEGER,TimeRemainingUS70 INTEGER,TimeRemainingReset INTEGER,CreatedBy INTEGER,CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,StatusId INTEGER,SyncFg INTEGER)";

    private static final String TABLE_CREATE_DAILYLOG_EVENT = "create table "
            + TABLE_DAILYLOG_EVENT
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,DriverId INTEGER,OnlineEventId INTEGER,EventSequenceId INTEGER,EventType INTEGER,EventCode INTEGER,EventCodeDescription text,OdometerReading text,EngineHour text," +
            "EventRecordOrigin INTEGER,EventRecordStatus INTEGER,EventDateTime text,Latitude text,Longitude text,LocationDescription text,DailyLogId INTEGER," +
            "CreatedBy INTEGER,CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,StatusId INTEGER,SyncFg INTEGER,VehicleId INTEGER, CheckSum text,DistanceSinceLastValidCoordinate text,MalfunctionIndicatorFg int,DataDiagnosticIndicatorFg int,DiagnosticCode text,Annotation text,AccumulatedVehicleMiles text,ElaspsedEngineHour text," +
            "MotorCarrier text,ShippingDocumentNo text,TrailerNo text,TimeZoneOffsetUTC text,CoDriverId INTEGER)";

    private static final String TABLE_CREATE_DAILYLOG_CODRIVER = "create table "
            + TABLE_DAILYLOG_CODRIVER
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,DriverId INTEGER,DriverId2 INTEGER,LoginTime text,LogoutTime text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_RULE = "create table "
            + TABLE_RULE
            + "(RuleId INTEGER,RuleName text,RuleDescription text,CreatedBy INTEGER,CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,StatusId INTEGER)";

    private static final String TABLE_CREATE_DAILYLOG_RULE = "create table "
            + TABLE_DAILYLOG_RULE
            + "(_Id INTEGER PRIMARY KEY AUTOINCREMENT,DailyLogId INTEGER,RuleId INTEGER,RuleStartTime text,RuelEndTime text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_EVENT_TYPE = "create table "
            + TABLE_EVENT_TYPE
            + "(EventTypeId INTEGER,EventType text,EventCode INTEGER,EventCodeDescription text,CreatedBy INTEGER,CreatedDate text,ModifiedBy INTEGER,ModifiedDate text,StatusId INTEGER)";

    private static final String TABLE_CREATE_TRACKING = "create table "
            + TABLE_TRACKING
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,LocationDate text,Latitude text,Longitude text,DriverId INTEGER,VehicleId INTEGER,Speed text,OdometerReading text,EngineHours text,RPM text,Variation text,Heading text,LocationAddress text,StateName text,CreatedBy INTEGER,CreatedDate text,StatusId INTEGER,SyncFg INTEGER)";

    private static final String TABLE_CREATE_GpsLocation = "create table "
            + TABLE_GpsLocation
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,signal text)";

    private static final String TABLE_CREATE_Message = "create table "
            + TABLE_Message
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,Message text,MessageToId INTEGER,CreatedById INTEGER,MessageDate text,SendFg INTEGER,DeliveredFg INTEGER,ReadFg INTEGER,DeviceId text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_TRIP_INSPECTION = "create table "
            + TABLE_TRIP_INSPECTION
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, DateTime text, DriverId INTEGER, DriverName text, Type INTEGER, Defect INTEGER, DefectRepaired INTEGER, SafeToDrive INTEGER, DefectItems text, Latitude text, Longitude text, LocationDescription text, Odometer text, TruckNumber text, TrailerNumber text, Comments text, Pictures text,SyncFg INTEGER)";

    private static final String TABLE_CREATE_VERSION_INFORMATION = "create table "
            + TABLE_VERSION_INFORMATION
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, AutoDownloadFg INTEGER, AutoUpdateFg INTEGER, CurrentVersion text, DownloadDate text, DownloadFg INTEGER, LiveFg INTEGER, PreviousVersion text, SerialNo text,"
            + " UpdateArchiveName text, UpdateDate text, UpdateUrl text, UpdatedFg INTEGER, VersionDate text, SyncFg INTEGER)";

    private static final String TABLE_CREATE_SETTINGS = "create table "
            + TABLE_SETTINGS
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, TimeZone text, DefaultRule INTEGER, GraphLine INTEGER, ColorLineUS INTEGER, ColorLineCanada INTEGER,"
            + " TimeFormat INTEGER, ViolationReading INTEGER, MessageReading INTEGER, StartTime INTEGER,"
            + " Orientation INTEGER, VisionMode INTEGER, CopyTrailer INTEGER, ShowViolation INTEGER, SyncTime INTEGER, AutomaticRuleChange INTEGER,"
            + " ViolationOnGrid INTEGER, FontSize INTEGER, DutyStatusReading INTEGER, DriverId INTEGER)";

    private static final String TABLE_CREATE_DIAGNOSTIC_INDICATOR = "create table "
            + TABLE_DIAGNOSTIC_INDICATOR
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,PowerDiagnosticFg INTEGER, EngineSynchronizationDiagnosticFg INTEGER, MissingElementDiagnosticFg INTEGER, DataTransferDiagnosticFg INTEGER, UnidentifiedDrivingDiagnosticFg INTEGER, OtherELDIdentifiedDiagnosticFg INTEGER, PowerMalfunctionFg INTEGER, EngineSynchronizationMalfunctionFg INTEGER, TimingMalfunctionFg INTEGER, PositioningMalfunctionFg INTEGER, DataRecordingMalfunctionFg INTEGER, DataTransferMalfunctionFg INTEGER, OtherELDDetectedMalfunctionFg INTEGER)";

    private static final String TABLE_CREATE_DTC = "create table "
            + TABLE_DTC
            + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,DateTime text,spn INTEGER, Protocol text, spnDescription text, fmi INTEGER, fmiDescription text,Occurrence INTEGER, SyncFg INTEGER, status INTEGER)";

    private static final String DATABASE_ALTER_DAILYLOG_DRIVINGTIMEREMAINING = "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN DrivingTimeRemaining INTEGER";
    private static final String DATABASE_ALTER_DAILYLOG_WORKSHIFTTIMEREMAINING = "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN WorkShiftTimeRemaining INTEGER";
    private static final String DATABASE_ALTER_DAILYLOG_TIMEREMAINING70 = "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN TimeRemaining70 INTEGER";
    private static final String DATABASE_ALTER_DAILYLOG_TIMEREMAINING120 = "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN TimeRemaining120 INTEGER";
    private static final String DATABASE_ALTER_DAILYLOG_TIMEREMAININGUS70= "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN TimeRemainingUS70 INTEGER";
    private static final String DATABASE_ALTER_DAILYLOG_TIMEREMAININGRESET = "ALTER TABLE " + TABLE_DAILYLOG + " ADD COLUMN TimeRemainingReset INTEGER";
    private static final String DATABASE_ALTER_CARRIER_PLATENO = "ALTER TABLE " + TABLE_CARRIER + " ADD COLUMN PlateNo text";
    private static final String DATABASE_ALTER_TABLE_ACCOUNT = "ALTER TABLE " + TABLE_ACCOUNT + " ADD COLUMN DotPassword text";
    private static final String DATABASE_DELETE_EVENTS_DIAGNOSTIC = "DELETE FROM " + TABLE_DAILYLOG_EVENT + " WHERE EventType=7";
    private static final String DATABASE_ALTER_CARRIER_TIMEZONEID = "ALTER TABLE " + TABLE_CARRIER + " ADD COLUMN TimeZoneId text";


    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_CARRIER);
        db.execSQL(TABLE_CREATE_ACCOUNT);
        db.execSQL(TABLE_CREATE_STATE);
        db.execSQL(TABLE_CREATE_PLACE);
        db.execSQL(TABLE_CREATE_RULE);
        db.execSQL(TABLE_CREATE_EVENT_TYPE);
        db.execSQL(TABLE_CREATE_DAILYLOG);
        db.execSQL(TABLE_CREATE_DAILYLOG_EVENT);
        db.execSQL(TABLE_CREATE_DAILYLOG_CODRIVER);
        db.execSQL(TABLE_CREATE_DAILYLOG_RULE);
        db.execSQL(TABLE_CREATE_TRACKING);
        db.execSQL(TABLE_CREATE_GpsLocation);
        db.execSQL(TABLE_CREATE_Message);
        db.execSQL(TABLE_CREATE_TRIP_INSPECTION);
        db.execSQL(TABLE_CREATE_VERSION_INFORMATION);
        db.execSQL(TABLE_CREATE_SETTINGS);
        db.execSQL(TABLE_CREATE_DIAGNOSTIC_INDICATOR);
        db.execSQL(TABLE_CREATE_DTC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {

            db.execSQL(DATABASE_ALTER_DAILYLOG_DRIVINGTIMEREMAINING);
            db.execSQL(DATABASE_ALTER_DAILYLOG_WORKSHIFTTIMEREMAINING);
            db.execSQL(DATABASE_ALTER_DAILYLOG_TIMEREMAINING70);
            db.execSQL(DATABASE_ALTER_DAILYLOG_TIMEREMAINING120);
            db.execSQL(DATABASE_ALTER_DAILYLOG_TIMEREMAININGUS70);
            db.execSQL(DATABASE_ALTER_DAILYLOG_TIMEREMAININGRESET);
        }

        if (oldVersion<3)
        {
            db.execSQL(DATABASE_ALTER_CARRIER_PLATENO);
        }

        if (oldVersion<4)
        {
            db.execSQL(DATABASE_ALTER_TABLE_ACCOUNT);
        }


        if (oldVersion<5)
        {
            db.execSQL(DATABASE_DELETE_EVENTS_DIAGNOSTIC);
        }

        if (oldVersion<6)
        {
            db.execSQL(TABLE_CREATE_DTC);
        }


        if (oldVersion<7)
        {
            db.execSQL(DATABASE_ALTER_CARRIER_TIMEZONEID);
            CarrierInfoDB.UpdateTimeZone();
        }
    }
}