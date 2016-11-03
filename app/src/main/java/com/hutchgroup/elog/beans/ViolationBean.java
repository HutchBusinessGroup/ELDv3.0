package com.hutchgroup.elog.beans;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by Deepak.Sharma on 7/15/2015.
 */
public class ViolationBean {
    private String rule, title, explanation;
    private int timeLeft11, timeLeft13, timeLeft14, timeLeft14US, timeLeft16;

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public boolean isCanadaFg() {
        return canadaFg;
    }

    public void setCanadaFg(boolean canadaFg) {
        this.canadaFg = canadaFg;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    private Date startTime;
    private boolean canadaFg;
    private int totalMinutes;

    /*Comparator for sorting the list by dateAsc*/
    public static Comparator<ViolationBean> dateAsc = new Comparator<ViolationBean>() {
        public int compare(ViolationBean s1, ViolationBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getStartTime().getTime() / 1000 * 60);
                date2 = (int) (s2.getStartTime().getTime() / 1000 * 60);

            } catch (Exception exe) {
            }

            return date1 - date2;
        }
    };


    /*Comparator for sorting the list by dateAsc*/
    public static Comparator<ViolationBean> dateDesc = new Comparator<ViolationBean>() {
        public int compare(ViolationBean s1, ViolationBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (s1.getStartTime().getTime() / 1000 * 60);
                date2 = (int) (s2.getStartTime().getTime() / 1000 * 60);

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };

    public int getTimeLeft11() {
        return timeLeft11;
    }

    public void setTimeLeft11(int timeLeft11) {
        this.timeLeft11 = timeLeft11;
    }

    public int getTimeLeft13() {
        return timeLeft13;
    }

    public void setTimeLeft13(int timeLeft13) {
        this.timeLeft13 = timeLeft13;
    }

    public int getTimeLeft14() {
        return timeLeft14;
    }

    public void setTimeLeft14(int timeLeft14) {
        this.timeLeft14 = timeLeft14;
    }

    public int getTimeLeft14US() {
        return timeLeft14US;
    }

    public void setTimeLeft14US(int timeLeft14US) {
        this.timeLeft14US = timeLeft14US;
    }

    public int getTimeLeft16() {
        return timeLeft16;
    }

    public void setTimeLeft16(int timeLeft16) {
        this.timeLeft16 = timeLeft16;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
