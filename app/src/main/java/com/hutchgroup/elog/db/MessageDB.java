package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;

import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.common.ChatClient;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Dev-1 on 4/14/2016.
 */
public class MessageDB {

    public static MessageBean CreateMessage(String message, int fromUserId, int toUserId, String flag) {
        MessageBean bean = new MessageBean();
        bean.setMessage(message);
        bean.setCreatedById(fromUserId);
        bean.setMessageToId(toUserId);
        bean.setMessageDate(Utility.getCurrentUTCDateTime());
        bean.setDeliveredFg(0);
        bean.setReadFg(0);
        bean.setSendFg(1);
        bean.setDeviceId(Utility.IMEI);
        bean.setSyncFg(1);
        bean.setFlag(flag);

        return bean;

    }

    public static void Send(MessageBean bean) {
        try {


            JSONObject obj = new JSONObject();
            obj.put("MessageToId", bean.getMessageToId());
            obj.put("CreatedById", bean.getCreatedById());
            obj.put("MessageDate", bean.getMessageDate());
            obj.put("Message", bean.getMessage());
            obj.put("Flag", bean.getFlag());
            obj.put("DeviceId", bean.getDeviceId());
            obj.put("CompanyId", Utility.companyId);
            ChatClient.send(obj.toString());
            if (bean.getFlag().equals("Message"))
                Save(bean);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::Send Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }

    }

    private static int getMessageId(String messageDate, int createdBy) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int _id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id from "
                    + MySQLiteOpenHelper.TABLE_Message
                    + " where CreatedById=? and MessageDate=?"
                    + " LIMIT 1", new String[]{createdBy + "", messageDate});

            if (cursor.moveToFirst()) {
                _id = cursor.getInt(0);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::getCurrentMessage Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return _id;
    }

    public static void Save(ArrayList<MessageBean> messageList) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (MessageBean bean : messageList) {

                ContentValues values = new ContentValues();
                values.put("Message", bean.getMessage());
                values.put("MessageToId", bean.getMessageToId());
                values.put("CreatedById", bean.getCreatedById());
                values.put("MessageDate", bean.getMessageDate());
                values.put("DeliveredFg", bean.getDeliveredFg());
                values.put("ReadFg", bean.getReadFg());
                values.put("DeviceId", bean.getDeviceId());
                values.put("SyncFg", bean.getSyncFg());
                int messageId = getMessageId(bean.getMessageDate(), bean.getCreatedById());

                if (messageId == 0) {
                    database.insertOrThrow(MySQLiteOpenHelper.TABLE_Message,
                            "_id", values);
                } else {
                    database.update(MySQLiteOpenHelper.TABLE_Message, values,
                            " _id= ?", new String[]{messageId + ""});
                }

            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(MessageDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: save messages
    public static void Save(MessageBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SendFg", bean.getSendFg());
            values.put("DeliveredFg", bean.getDeliveredFg());
            values.put("ReadFg", bean.getReadFg());
            values.put("SyncFg", bean.getSyncFg());
            if (bean.getId() == 0) {
                values.put("Message", bean.getMessage());
                values.put("MessageToId", bean.getMessageToId());
                values.put("CreatedById", bean.getCreatedById());
                values.put("MessageDate", bean.getMessageDate());
                values.put("DeviceId", bean.getDeviceId());
                long id = database.insertOrThrow(MySQLiteOpenHelper.TABLE_Message,

                        "_id", values);
                System.out.print(id);
            } else {
                database.update(MySQLiteOpenHelper.TABLE_Message, values,
                        " AccountId= ?", new String[]{bean.getId()
                                + ""});
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(UserDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }


    // Created By: Deepak Sharma
    // Created Date: 08 Aug 2016
    // Purpose: update message status
    public static void MessageStatusUpdate(int fromId, int toId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ReadFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_Message, values,
                    " CreatedById=? and MessageToId=? and ReadFg=0 ", new String[]{fromId + "", toId + ""});

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(UserDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(UserDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: check duplicate account
    public static ArrayList<UserBean> getUserList(String search) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<UserBean> userList = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select AccountId,FirstName,LastName,Username from "
                    + MySQLiteOpenHelper.TABLE_ACCOUNT
                    + " where AccountId<>? and AccountType<>2 and StatusId=1 order by FirstName,LastName", new String[]{Utility.onScreenUserId + ""});
            while (cursor.moveToNext()) {
                UserBean bean = new UserBean();
                int userId = cursor.getInt(0);
                bean.setAccountId(userId);
                bean.setFirstName(cursor.getString(1));
                bean.setLastName(cursor.getString(2));
                bean.setUserName(cursor.getString(3));

                MessageBean messageBean = getCurrentMessage(userId);
                bean.setCurrentMessage(messageBean.getMessage());
                bean.setMessageDateTime(messageBean.getMessageDate());
                bean.setReadFg(messageBean.getReadFg());
                bean.setIsOnline(Utility.onlineUserList.contains(userId + ""));
                String name = (bean.getFirstName() + " " + bean.getLastName()).toLowerCase();

                int unreadCount = getUnreadCount(userId);
                bean.setUnreadCount(unreadCount);
                if (name.startsWith(search))
                    userList.add(bean);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::getUserList Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        Collections.sort(userList, UserBean.dateDesc);
        return userList;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: get current message of selected user
    public static MessageBean getCurrentMessage(int userId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        MessageBean bean = new MessageBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select Message,ReadFg,MessageDate from "
                    + MySQLiteOpenHelper.TABLE_Message
                    + " where ((CreatedById=? and MessageToId=?) or (CreatedById=? and MessageToId=?))"
                    + " order by MessageDate desc LIMIT 1", new String[]{userId + "", Utility.onScreenUserId + "", Utility.onScreenUserId + "", userId + ""});
            if (cursor.moveToFirst()) {
                bean.setMessage(cursor.getString(0));
                bean.setReadFg(cursor.getInt(1));
                String messageDate = Utility.convertUTCToLocalDateTime(cursor.getString(2));
                bean.setMessageDate(messageDate);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::getCurrentMessage Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return bean;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: get mesages of user
    public static ArrayList<MessageBean> getMessage(int userId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<MessageBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String fromDate = Utility.getPreviousDate(-15);
            cursor = database.rawQuery("select _id,Message,ReadFg,MessageToId,CreatedById,MessageDate,DeliveredFg,SendFg from "
                    + MySQLiteOpenHelper.TABLE_Message
                    + " where ((CreatedById=? and MessageToId=?) or (CreatedById=? and MessageToId=?)) and MessageDate>=?"
                    + " order by MessageDate", new String[]{userId + "", Utility.onScreenUserId + "", Utility.onScreenUserId + "", userId + "", fromDate});

            while (cursor.moveToNext()) {
                MessageBean bean = new MessageBean();
                bean.setId(cursor.getInt(0));
                bean.setMessage(cursor.getString(1));
                bean.setReadFg(cursor.getInt(2));
                bean.setMessageToId(cursor.getInt(3));
                bean.setCreatedById(cursor.getInt(4));
                String messageDate = Utility.convertUTCToLocalDateTime(cursor.getString(5));
                bean.setMessageDate(messageDate);
                bean.setDeliveredFg(cursor.getInt(6));
                bean.setSendFg(cursor.getInt(7));
                list.add(bean);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::getMessage Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 8 Aug 2016
    // Purpose: check if there any unread message
    public static boolean getUnreadStatus(int userId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select Message,ReadFg,MessageDate from "
                    + MySQLiteOpenHelper.TABLE_Message
                    + " where CreatedById=? and MessageToId=? and ReadFg=0 "
                    + " LIMIT 1", new String[]{userId + "", Utility.onScreenUserId + ""});
            if (cursor.moveToFirst()) {
                status = true;
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::getCurrentMessage Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 8 Aug 2016
    // Purpose: check if there any unread message
    public static int getUnreadCount(int userId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int count = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select 1 from "
                            + MySQLiteOpenHelper.TABLE_Message
                            + " where CreatedById=? and MessageToId=? and ReadFg=0 "
                    , new String[]{userId + "", Utility.onScreenUserId + ""});

            count = cursor.getCount();

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::getCurrentMessage Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return count;
    }
    // Created By: Deepak Sharma
    // Created Date: 8 Aug 2016
    // Purpose: check if there any unread message
    public static int getUnreadCount() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int count = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select 1 from "
                            + MySQLiteOpenHelper.TABLE_Message
                            + " where MessageToId=? and ReadFg=0 "
                    , new String[]{ Utility.onScreenUserId + ""});

            count = cursor.getCount();

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(MessageDB.class.getName() + "::getCurrentMessage Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return count;
    }
}
