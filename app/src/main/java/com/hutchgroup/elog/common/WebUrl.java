package com.hutchgroup.elog.common;

/**
 * Created by Deepak.Sharma on 7/15/2015.
 */
public class WebUrl {
    public final static String WS_BASE_URL = "http://207.194.137.58:3393/";// "http://10.0.2.2:1364/";
    public final static String BASE_URL = WS_BASE_URL + "ELogService.svc/";
    public final static String GET_CARRIER_INFO = BASE_URL + "Carrier/Get/?serialNo=";
    public final static String GET_ACCOUNT= BASE_URL+ "Account/Get/?serialNo=";
    public final static String GET_STATE= BASE_URL+ "State/Get/";
    public final static String GET_PLACE= BASE_URL+ "Places/Get/";
    public final static String GET_LOG_DATA= BASE_URL+ "LogData/Get/?driverId=";

    public final static String GET_LOG_EVENT_DATA= BASE_URL+ "LogEventData/Get/?driverId=";

    public final static String GET_ASSIGNED_EVENT= BASE_URL+ "AssignedEventGet/Get/?driverId=";
    public final static String GET_MESSAGE= BASE_URL+ "Message/Get/?driverId=";
    public final static String GET_TRAILER_INFO= BASE_URL+ "TrailerInfo/Get/?companyId=";
    public final static String GET_AXLE_INFO= BASE_URL+ "AxleInfo/Get/?companyId=";

    public final static String POST_ALL= BASE_URL+ "Post/All";

    public final static String GET_UPDATE = BASE_URL+ "Version/Get/?version=";
    public final static String POST_EVENT_SYNC= BASE_URL+ "Post/EventSync";
    public final static String POST_MESSAGE_SYNC= BASE_URL+ "Post/MessageSync";
    public final static String POST_ACCOUNT= BASE_URL+ "Account/Post";
    public final static String POST_DVIR= BASE_URL+ "DVIR/Post";
    public final static String POST_DTC= BASE_URL+ "DTC/Post";
    public final static String POST_ALERT= BASE_URL+ "Alert/Post";

    public final static String POST_TPMS= BASE_URL+ "TPMS/Post";

}
