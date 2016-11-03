package com.hutchgroup.elog.common;

public class Ascii {

    public Ascii() {

    }

    public static int getAscii(char ch) {
        int code = 0;

        int ascii = (int) ch;
        if ((ascii >= 49 && ascii <= 57) || (ascii >= 65 && ascii <= 90) || (ascii >= 97 && ascii <= 122))
            code = ascii - 48;
        else
            code = 0;

        return code;
    }

    public static int getSum(String str) {
        if (str.equals(""))
            return 0;
        String temp = replaceCarriageReturn(str);
        int sum = 0;
        for (int i = 0; i < temp.length(); i++) {
            int code = getAscii(temp.charAt(i));
            sum += code;
        }
        return sum;
    }

    public static String replaceCarriageReturn(String str) {
        String result = str.replace("\r", ";");
        result = result.replace(",", ";");
        return result;
    }

    public static String calculateCheckSum(int number) {
        int checksum = number & 0xFF; //get lower 8-bit byte
        int shiftNum = checksum << 3;
        int xorNum = shiftNum ^ 195;
        checksum = xorNum & 0xFF;
        return Integer.toHexString(checksum);
    }

    // Created By: Deepak Sharma
    // Created Date: 7 April 2016
    // Purpose: calcualte line data check value
    public static String getLineDataCheckValue(String data) {
        int sum = getSum(data); // calculate line check sum
        int checksum = sum & 0xFF; // get lower 8-bit byte
        checksum = (checksum << 3); // rotate three consecutive left
        checksum = checksum ^ 150; // xor with number 150 as per document
        checksum = checksum & 0xFF; // get lower 8-bit byte
        String lineDataCheckValue = Integer.toHexString(checksum);
        lineDataCheckValue=String.format("%2s", lineDataCheckValue).replace(' ', '0'); // padding 0 if length less than 2
        return lineDataCheckValue;
    }

    // Created By: Deepak Sharma
    // Created Date: 7 April 2016
    // Purpose: calcualte event data check value
    public static String getEventDataCheckValue(String data) {
        int sum = getSum(data); // calculate event check sumlineDataCheckValue
        int checksum = sum & 0xFF; // get lower 8-bit byte
        checksum = (checksum << 3); // rotate three consecutive left
        checksum = checksum ^ 195; // xor with number 195 as per document
        checksum = checksum & 0xFF; // get lower 8-bit byte
        String eventDataCheckValue = Integer.toHexString(checksum);
        eventDataCheckValue=String.format("%2s", eventDataCheckValue).replace(' ', '0');// padding 0 if length less than 2

        return eventDataCheckValue;
    }

    // Created By: Deepak Sharma
    // Created Date: 7 April 2016
    // Purpose: calcualte file data check value
    public static String getFileDataCheckValue(String data) {
        int sum = getSum(data); // calculate file check sum
        int checksum = sum & 0xFFFF; // get 2 lower 8-bit byte
        checksum = (checksum << 3); // rotate three consecutive left
        checksum = checksum ^ 38556; // xor with number 38556 as per document
        checksum = checksum & 0xFFFF; // get 2 lower 8-bit byte
        String fileDataCheckValue = Integer.toHexString(checksum);
        fileDataCheckValue=  String.format("%4s", fileDataCheckValue).replace(' ', '0');// padding 0 if length less than 4
        return fileDataCheckValue;
    }


}

