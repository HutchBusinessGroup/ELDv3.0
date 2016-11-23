package com.hutchgroup.elog.common;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.db.MySQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Deepak Sharma on 3/31/2016.
 */
public class OutputFile {
    private static DateFormat dateFormat = new SimpleDateFormat("MMddyyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("HHmmss");
    public static String fileName = "";

    // Created By: Deepak Sharma
    // Created Date: 1 April 2016
    // Purpose: get header for output file
    public static String getHeader(String fileComment) {
        //get current date time with Date()
        Date currentDT =Utility.newDate();
        String currentDate = dateFormat.format(currentDT);
        String currentTime = timeFormat.format(currentDT);
        UserBean user1 = Utility.user1;
        UserBean user2 = Utility.user2;
        String driverInfo, coDriverInfo = "", vehicleInfo, carrierInfo, licenseNo = "";
        String checksum = ""; // it should be calculated separately for each line and in last for entire file. its line checksum
        fileName = "";
        String exempt = "0";
        if (user1.isOnScreenFg()) {
            checksum = Ascii.getLineDataCheckValue(user1.getLastName() + user1.getFirstName() + user1.getUserName() + user1.getDlIssueState() + user1.getDrivingLicense());
            driverInfo = user1.getLastName() + "," + user1.getFirstName() + "," + user1.getUserName() + "," + user1.getDlIssueState() + "," + user1.getDrivingLicense() + "," + checksum;
            licenseNo = user1.getDrivingLicense();
            if (user2.getAccountId() > 0) {
                checksum = Ascii.getLineDataCheckValue(user2.getLastName() + user2.getFirstName() + user2.getUserName());
                coDriverInfo = "\n" + user2.getLastName() + "," + user2.getFirstName() + "," + user2.getUserName() + "," + checksum;
            }

            if (user1.getExemptELDUseFg() == 1) {
                exempt = "E";
            }
            // first five character of Last name
            if (user1.getLastName().length() >= 5) {
                fileName = user1.getLastName().substring(0, 5);
            } else {
                // if last name shorter than five character then pad with _
                fileName = String.format("%-5s", user1.getLastName()).replace(' ', '_');
            }
            fileName += user1.getDrivingLicense().substring(user1.getDrivingLicense().length() - 2);
        } else {
            checksum = Ascii.getLineDataCheckValue(user2.getLastName() + user2.getFirstName() + user2.getUserName() + user2.getDlIssueState() + user2.getDrivingLicense());
            driverInfo = user2.getLastName() + "," + user2.getFirstName() + "," + user2.getUserName() + "," + user2.getDlIssueState() + "," + user2.getDrivingLicense() + "," + checksum;

            checksum = Ascii.getLineDataCheckValue(user1.getLastName() + user1.getFirstName() + user1.getUserName());
            coDriverInfo = "\n" + user1.getLastName() + "," + user1.getFirstName() + "," + user1.getUserName() + checksum;
            licenseNo = user2.getDrivingLicense();
            if (user2.getExemptELDUseFg() == 1) {
                exempt = "E";
            }

            // first five character of Last name
            if (user1.getLastName().length() >= 5) {
                fileName = user2.getLastName().substring(0, 5);
            } else {
                // if last name shorter than five character then pad with _
                fileName = String.format("%-5s", user2.getLastName()).replace(' ', '_');
            }
            fileName += user2.getDrivingLicense().substring(user2.getDrivingLicense().length() - 2);
        }
        int sumLicense = 0;
        for (char c : licenseNo.toCharArray()) {
            if (Character.isDigit(c)) {
                sumLicense += Integer.valueOf(c);
            }
        }
        //sum of all digits of driving license no. complite its static value;
        if (sumLicense > 99) {
            sumLicense -= 100;
            fileName += sumLicense;
        } else if (sumLicense < 10) {
            fileName += "0" + sumLicense;
        } else {
            fileName += sumLicense;

        }

        fileName += currentDate + "-000" + currentTime + ".csv";
        checksum = Ascii.getLineDataCheckValue(Utility.UnitNo + Utility.VIN + Utility.TrailerNumber);
        vehicleInfo = "\n" + Utility.UnitNo + "," + Utility.VIN + "," + Utility.TrailerNumber + "," + checksum;

        checksum = Ascii.getLineDataCheckValue(Utility.USDOT + Utility.CarrierName + Utility.multiDayBasisUsed + "000000" + Utility.TimeZoneOffsetUTC);
        carrierInfo = "\n" + Utility.USDOT + "," + Utility.CarrierName + "," + Utility.multiDayBasisUsed + "," + "000000" + "," + Utility.TimeZoneOffsetUTC + "," + checksum;
        checksum = Ascii.getLineDataCheckValue(Utility.ShippingNumber + exempt);
        carrierInfo += "\n" + Utility.ShippingNumber + "," + exempt + "," + checksum;

        checksum = Ascii.getLineDataCheckValue(currentDate + currentTime + String.format("%.2f", Utility.currentLocation.getLatitude()) + String.format("%.2f", Utility.currentLocation.getLongitude()) + Double.valueOf(CanMessages.OdometerReading).intValue()
                + String.format("%.1f", Double.valueOf(CanMessages.EngineHours == null ? "0.0" : CanMessages.EngineHours)));

        carrierInfo += "\n" + currentDate + "," + currentTime + "," + String.format("%.2f", Utility.currentLocation.getLatitude()) + "," + String.format("%.2f", Utility.currentLocation.getLongitude()) + "," + Double.valueOf(CanMessages.OdometerReading).intValue()
                + "," + String.format("%.1f", Double.valueOf(CanMessages.EngineHours == null ? "0.0" : CanMessages.EngineHours)) + "," + checksum; // location info and vehicle engine info and miles info

        // get authentication value
        String authValue = Utility.getAuthenticationValue();
        checksum = Ascii.getLineDataCheckValue(Utility.RegistrationId + Utility.EldIdentifier + authValue+ fileComment);
        carrierInfo += "\n" + Utility.RegistrationId + "," + Utility.EldIdentifier + "," + authValue + "," + fileComment + "," + checksum; //device registration info
        String header = driverInfo + coDriverInfo + vehicleInfo + carrierInfo;
        return header;
    }

    // Created By: Deepak Sharma
    // Created Date: 17 June 2016
    // Purpose: get output file
    public static String getOutputFile(String comment) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("ELD File Header Segment:").append("\n");
            sb.append(getHeader(comment)).append("\n");
            sb.append("User List:").append("\n");

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String fromDate = Utility.addDate(Utility.getCurrentDate(), -Utility.multiDayBasisUsed);
            int driverId = Utility.onScreenUserId;

            // get userList query
            String query = "select e.DriverId,a.AccountType,a.FirstName,a.LastName,max(EventDateTime) from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on  e.DriverId=a.AccountId" +
                    " where EventDateTime>=? and EventType=5 and EventCode=1 group by e.DriverId,a.AccountType,a.FirstName,a.LastName order by max(EventDateTime) desc";
            cursor = database.rawQuery(query, new String[]{fromDate});
            int i = 1;
            String lineCheckValue = "";
            String fileDataCheckValue = "";
            Map<Integer, Integer> userList = new LinkedHashMap<>();
            while (cursor.moveToNext()) {
                userList.put(cursor.getInt(0), i);
                sb.append(i).append(",");
                String accountType = (cursor.getInt(cursor.getColumnIndex("AccountType")) == 1 ? "S" : "D");
                String lastName = cursor.getString(cursor.getColumnIndex("LastName"));
                String firstName = cursor.getString(cursor.getColumnIndex("FirstName"));
                sb.append(accountType).append(",");
                sb.append(lastName).append(",");
                sb.append(firstName).append(",");
                lineCheckValue = Ascii.getLineDataCheckValue(i + accountType + lastName + firstName);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");
                i++;
            }

            cursor.close();

            // get CMV List
            sb.append("CMV List:").append("\n");
            query = "select distinct e.VehicleId,c.UnitNo,c.VIN,max(EventDateTime)  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_CARRIER + " c on  e.VehicleId=c.VehicleId" +
                    " where EventDateTime>=? group by e.VehicleId,c.UnitNo,c.VIN order by max(EventDateTime)  desc";
            cursor = database.rawQuery(query, new String[]{fromDate});

            i = 1;

            Map<Integer, Integer> cmvList = new LinkedHashMap<>();
            while (cursor.moveToNext()) {

                userList.put(cursor.getInt(0), i);
                sb.append(i).append(",");
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String vinNo = cursor.getString(cursor.getColumnIndex("VIN"));
                sb.append(unitNo).append(",");
                sb.append(vinNo).append(",");
                lineCheckValue = Ascii.getLineDataCheckValue(i + unitNo + vinNo);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");
                i++;
            }
            cursor.close();

            // get ELD Event List
            sb.append("ELD Event List:").append("\n");
            query = "select EventSequenceId,EventRecordStatus,EventRecordOrigin,EventType,EventCode,EventDateTime,AccumulatedVehicleMiles ,ElaspsedEngineHour,Latitude ,Longitude" +
                    ",DistanceSinceLastValidCoordinate,MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg,e.VehicleId,e.DriverId,c.UnitNo,a.Username from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId join " + MySQLiteOpenHelper.TABLE_CARRIER +
                    " c on c.VehicleId=e.VehicleId " +
                    " where EventType<=3  and EventDateTime>=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate});

            while (cursor.moveToNext()) {
                String eventSequenceId = cursor.getString(cursor.getColumnIndex("EventSequenceId"));
                String eventRecordStatus = cursor.getString(cursor.getColumnIndex("EventRecordStatus"));
                String eventRecordOrigin = cursor.getString(cursor.getColumnIndex("EventRecordOrigin"));
                String eventType = cursor.getString(cursor.getColumnIndex("EventType"));
                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                String accumulatedVehicleMiles = cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles"));
                String elapsedEngineHours = cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour"));
                String latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                String distanceSinceLastValidCoordinate = cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate"));
                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));
                String vehicleOrderNo = cmvList.get(vehicleId) + "";
                String userOrderNo = userList.get(cursor.getInt(cursor.getColumnIndex("DriverId"))) + "";
                String malFunctionIndicatorFg = cursor.getString(cursor.getColumnIndex("MalfunctionIndicatorFg"));
                String dataDiagnosticIndicatorFg = cursor.getString(cursor.getColumnIndex("DataDiagnosticIndicatorFg"));
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String userName = cursor.getString(cursor.getColumnIndex("Username"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventRecordStatus).append(",");
                sb.append(eventRecordOrigin).append(",");
                sb.append(eventType).append(",");
                sb.append(eventCode).append(",");
                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");
                sb.append(accumulatedVehicleMiles).append(",");
                sb.append(elapsedEngineHours).append(",");
                sb.append(latitude).append(",");
                sb.append(longitude).append(",");
                sb.append(distanceSinceLastValidCoordinate).append(",");

                sb.append(vehicleOrderNo).append(","); // CMV order no
                sb.append(userOrderNo).append(","); // User order no

                sb.append(malFunctionIndicatorFg).append(",");
                sb.append(dataDiagnosticIndicatorFg).append(",");

                String eventDataCheckValue = Ascii.getEventDataCheckValue(eventType + eventCode + eventDate + eventTime + accumulatedVehicleMiles + elapsedEngineHours + latitude + longitude + unitNo + userName);

                sb.append(eventDataCheckValue).append(",");

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventRecordStatus + eventRecordOrigin + eventType + eventCode + eventDate + eventTime + accumulatedVehicleMiles + elapsedEngineHours + latitude + longitude + distanceSinceLastValidCoordinate + vehicleOrderNo + userOrderNo + malFunctionIndicatorFg + dataDiagnosticIndicatorFg + eventDataCheckValue);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");

            }
            cursor.close();

            // get ELD Event Annotations or Comments:
            sb.append("ELD Event Annotations or Comments:").append("\n");
            query = "select EventSequenceId,EventDateTime,LocationDescription,Annotation,a.Username from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId where EventType<=3  and EventDateTime>=? and ((LocationDescription <> '' and LocationDescription is not null) or (Annotation <> '' and Annotation is not null))  order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate});

            while (cursor.moveToNext()) {
                String eventSequenceId = cursor.getString(cursor.getColumnIndex("EventSequenceId"));
                String userName = cursor.getString(cursor.getColumnIndex("Username"));
                String annotation = cursor.getString(cursor.getColumnIndex("Annotation"));
                String locationDescription = cursor.getString(cursor.getColumnIndex("LocationDescription"));

                sb.append(eventSequenceId).append(",");
                sb.append(userName).append(",");
                sb.append(annotation).append(",");

                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");
                sb.append(locationDescription).append(",");
                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + userName + annotation + eventDate + eventTime + locationDescription);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");
                i++;
            }

            cursor.close();

            // get Driver's Certification/Recertification Actions:
            sb.append("Driver's Certification/Recertification Actions:").append("\n");

            query = "select EventSequenceId,EventCode,EventDateTime,LogDate,VehicleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_DAILYLOG + " d on d._id=e.DailyLogId where EventType=4  and EventDateTime>=? and e.DriverId=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            while (cursor.moveToNext()) {
                String eventSequenceId = cursor.getString(cursor.getColumnIndex("EventSequenceId"));
                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                sb.append(eventSequenceId).append(",");
                sb.append(eventCode).append(",");
                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));
                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                String logDate = dateFormat.format(new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(cursor.getColumnIndex("LogDate"))));
                String cmvOrderNo = cmvList.get(vehicleId) + "";

                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");
                sb.append(logDate).append(",");
                sb.append(cmvOrderNo).append(","); // CMV order no

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventCode + eventDate + eventTime + logDate + cmvOrderNo);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");
            }

            cursor.close();

            // get Malfunctions and Data Diagnostic Events:
            sb.append("Malfunctions and Data Diagnostic Events:").append("\n");

            query = "select EventSequenceId,EventCode,DiagnosticCode,EventDateTime,OdometerReading,EngineHour,VehicleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " where EventType=7  and EventDateTime>=? and DriverId=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            while (cursor.moveToNext()) {
                String eventSequenceId = cursor.getString(cursor.getColumnIndex("EventSequenceId"));
                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                String diagnosticCode = cursor.getString(cursor.getColumnIndex("DiagnosticCode"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventCode).append(",");
                sb.append(diagnosticCode).append(",");

                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));

                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                String odometerReading = Double.valueOf(cursor.getString(cursor.getColumnIndex("OdometerReading"))).intValue() + "";
                String engineHour = String.format("%.1f", Double.valueOf(cursor.getString(cursor.getColumnIndex("EngineHour"))));

                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");

                sb.append(odometerReading).append(",");
                sb.append(engineHour).append(",");
                String cmvOrderNo = cmvList.get(vehicleId) + "";
                sb.append(cmvOrderNo).append(","); // CMV order no

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventCode + diagnosticCode + eventDate + eventTime + odometerReading + engineHour + cmvOrderNo);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");
            }

            cursor.close();

            // get ELD Login/Logout Report:
            sb.append("ELD Login/Logout Report:").append("\n");

            query = "select EventSequenceId,EventCode,a.Username,EventDateTime,OdometerReading,EngineHour from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on a.AccountId=e.DriverId where EventType=5  and EventDateTime>=? and e.DriverId=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate, driverId + ""});

            while (cursor.moveToNext()) {
                String eventSequenceId = cursor.getString(cursor.getColumnIndex("EventSequenceId"));
                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                String username = cursor.getString(cursor.getColumnIndex("Username"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventCode).append(",");
                sb.append(username).append(",");

                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                String odometerReading = Double.valueOf(cursor.getString(cursor.getColumnIndex("OdometerReading"))).intValue() + "";
                String engineHour = String.format("%.1f", Double.valueOf(cursor.getString(cursor.getColumnIndex("EngineHour"))));

                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");

                sb.append(odometerReading).append(",");
                sb.append(engineHour).append(",");

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventCode + username + eventDate + eventTime + odometerReading + engineHour);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");
            }

            cursor.close();

            // get CMV Engine Power-Up and Shut Down Activity:
            sb.append("CMV Engine Power-Up and Shut Down Activity:").append("\n");

            query = "select EventSequenceId,EventCode,EventDateTime,OdometerReading,EngineHour,Latitude ,Longitude,UnitNo,VIN,ShippingDocumentNo,TrailerNo from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_CARRIER + " c on e.VehicleId=c.VehicleId where EventType=6  and EventDateTime>=?  order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate});

            while (cursor.moveToNext()) {
                String eventSequenceId = cursor.getString(cursor.getColumnIndex("EventSequenceId"));
                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                String latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String VIN = cursor.getString(cursor.getColumnIndex("VIN"));
                String shippingDocumentNo = cursor.getString(cursor.getColumnIndex("ShippingDocumentNo"));
                String trailerNo = cursor.getString(cursor.getColumnIndex("TrailerNo"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventCode).append(",");

                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);

                String odometerReading = Double.valueOf(cursor.getString(cursor.getColumnIndex("OdometerReading"))).intValue() + "";
                String engineHour = String.format("%.1f", Double.valueOf(cursor.getString(cursor.getColumnIndex("EngineHour"))));

                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");

                sb.append(odometerReading).append(",");
                sb.append(engineHour).append(",");

                sb.append(latitude).append(",");
                sb.append(longitude).append(",");
                sb.append(unitNo).append(",");
                sb.append(VIN).append(",");
                sb.append(shippingDocumentNo).append(",");
                sb.append(trailerNo).append(",");

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventCode + eventDate + eventTime + odometerReading + engineHour + latitude + longitude + unitNo + VIN + trailerNo + shippingDocumentNo);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");
            }

            cursor.close();

            //get Unidentified Driver Profile Records:
            sb.append("Unidentified Driver Profile Records:").append("\n");
//            query = "select EventSequenceId,EventRecordStatus,EventRecordOrigin,EventType,EventCode,EventDateTime,AccumulatedVehicleMiles ,ElaspsedEngineHour,Latitude ,Longitude" +
//                    ",DistanceSinceLastValidCoordinate,MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg,VehicleId,DriverId,UnitNo,Username  from " + MySQLiteOpenHelper.TABLE_CARRIER + " c join " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
//                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId and e.VehicleId=c.VehicleId and a.AccountType=2 where EventType<=3  and EventDateTime>=? order by EventDateTime desc";
            query = "select *  from " + MySQLiteOpenHelper.TABLE_CARRIER + " c join " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on c.VehicleId=e.VehicleId and e.DriverId=a.AccountId and a.AccountType=2 where EventType<=3  and EventDateTime>=? order by EventDateTime desc";
            cursor = database.rawQuery(query, new String[]{fromDate});

            while (cursor.moveToNext()) {

                String eventSequenceId = cursor.getString(cursor.getColumnIndex("EventSequenceId"));
                String eventRecordStatus = cursor.getString(cursor.getColumnIndex("EventRecordStatus"));
                String eventRecordOrigin = cursor.getString(cursor.getColumnIndex("EventRecordOrigin"));
                String eventType = cursor.getString(cursor.getColumnIndex("EventType"));
                String eventCode = cursor.getString(cursor.getColumnIndex("EventCode"));
                Date eventDateTime = Utility.parse(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                String eventDate = dateFormat.format(eventDateTime);
                String eventTime = timeFormat.format(eventDateTime);
                String accumulatedVehicleMiles = cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles"));
                String elapsedEngineHours = cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour"));
                String latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
                String longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
                String distanceSinceLastValidCoordinate = cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate"));
                int vehicleId = cursor.getInt(cursor.getColumnIndex("VehicleId"));
                String vehicleOrderNo = cmvList.get(vehicleId) + "";

                String malFunctionIndicatorFg = cursor.getString(cursor.getColumnIndex("MalfunctionIndicatorFg"));
                String dataDiagnosticIndicatorFg = cursor.getString(cursor.getColumnIndex("DataDiagnosticIndicatorFg"));
                String unitNo = cursor.getString(cursor.getColumnIndex("UnitNo"));
                String userName = cursor.getString(cursor.getColumnIndex("Username"));

                sb.append(eventSequenceId).append(",");
                sb.append(eventRecordStatus).append(",");
                sb.append(eventRecordOrigin).append(",");
                sb.append(eventType).append(",");
                sb.append(eventCode).append(",");
                sb.append(eventDate).append(",");
                sb.append(eventTime).append(",");
                sb.append(accumulatedVehicleMiles).append(",");
                sb.append(elapsedEngineHours).append(",");
                sb.append(latitude).append(",");
                sb.append(longitude).append(",");
                sb.append(distanceSinceLastValidCoordinate).append(",");

                sb.append(vehicleOrderNo).append(","); // CMV order no

                sb.append(malFunctionIndicatorFg).append(",");
                sb.append(dataDiagnosticIndicatorFg).append(",");

                String eventDataCheckValue = Ascii.getEventDataCheckValue(eventType + eventCode + eventDate + eventTime + accumulatedVehicleMiles + elapsedEngineHours + latitude + longitude + unitNo + userName);

                sb.append(eventDataCheckValue).append(",");

                lineCheckValue = Ascii.getLineDataCheckValue(eventSequenceId + eventRecordStatus + eventRecordOrigin + eventType + eventCode + eventDate + eventTime + accumulatedVehicleMiles + elapsedEngineHours + latitude + longitude + distanceSinceLastValidCoordinate + vehicleOrderNo + malFunctionIndicatorFg + dataDiagnosticIndicatorFg + eventDataCheckValue);
                fileDataCheckValue += lineCheckValue;
                sb.append(lineCheckValue).append("\n");
            }
            sb.append("End of File:").append("\n");
            fileDataCheckValue = Ascii.getFileDataCheckValue(fileDataCheckValue);
            sb.append(fileDataCheckValue).append("\n");

        } catch (Exception e) {
            //Utility.printError(e.getMessage());
            LogFile.write(OutputFile.class.getName() + "::getOutputFile: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
                LogFile.write(OutputFile.class.getName() + "::getOutputFile1: " + e2.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            }
        }
        return sb.toString();
    }

}

