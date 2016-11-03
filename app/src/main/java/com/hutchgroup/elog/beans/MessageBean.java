package com.hutchgroup.elog.beans;

import java.io.Serializable;

/**
 * Created by Dev-1 on 4/14/2016.
 */
public class MessageBean implements Serializable {
    int id;
    int MessageToId;
    int CreatedById;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMessageToId() {
        return MessageToId;
    }

    public void setMessageToId(int messageToId) {
        MessageToId = messageToId;
    }

    public int getCreatedById() {
        return CreatedById;
    }

    public void setCreatedById(int createdById) {
        CreatedById = createdById;
    }

    public int getDeliveredFg() {
        return DeliveredFg;
    }

    public void setDeliveredFg(int deliveredFg) {
        DeliveredFg = deliveredFg;
    }

    public int getReadFg() {
        return ReadFg;
    }

    public void setReadFg(int readFg) {
        ReadFg = readFg;
    }

    public int getSyncFg() {
        return SyncFg;
    }

    public void setSyncFg(int syncFg) {
        SyncFg = syncFg;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getMessageDate() {
        return MessageDate;
    }

    public void setMessageDate(String messageDate) {
        MessageDate = messageDate;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    int DeliveredFg;
    int ReadFg;
    int SyncFg;


    public int getSendFg() {
        return SendFg;
    }

    public void setSendFg(int sendFg) {
        SendFg = sendFg;
    }

    int SendFg;
    String Message;
    String MessageDate;
    String DeviceId;

    public String getFlag() {
        return Flag;
    }

    public void setFlag(String flag) {
        Flag = flag;
    }

    String Flag;

}
