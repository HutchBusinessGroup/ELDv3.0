package com.hutchgroup.elog.bll;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.hutchgroup.elog.ElogActivity;
import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.beans.DutyStatusBean;
import com.hutchgroup.elog.beans.ViolationBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.HourOfServiceDB;
import com.hutchgroup.elog.fragments.ELogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Deepak.Sharma on 7/15/2015.
 */
public class HourOfService {
    public static IViolation mListener;
    static String fullFormat = "yyyy-MM-dd HH:mm:ss";
    public static ArrayList<ViolationBean> violations = new ArrayList<>();
    public static ArrayList<DutyStatusBean> listDutyStatus = new ArrayList<>();
    static ArrayList<DutyStatusBean> listCoDriverDutyStatus = new ArrayList<>();
    public static boolean deferedFg = false;
    public static String coDriverIds = "";
    public static Date splitTime = null, ExcludeSleeperStartTime = null, deferedDate = null;

    public static void InvokeRule(Date logDate, int driverId) {
        listDutyStatus = HourOfServiceDB.DutyStatusGet15Days(logDate, driverId + "", false);

        if (listDutyStatus.size() == 0) {

            ELogFragment.ViolationDT = null;
            MainActivity.ViolationDT = null;
            ELogFragment.ViolationDescription = "";
            return;
        }
        try {
            listCoDriverDutyStatus = DailyLogDB.getCoDriverList(driverId, logDate);
            DutyStatusBean lastDutyStatus = listDutyStatus.get(0);
            int status = lastDutyStatus.getStatus();

            if (1 == 2 && status != 2 && Utility.dateOnlyGet(logDate).equals(Utility.dateOnlyGet((Utility.newDate()))) && !deferedFg) {
                if (listDutyStatus.size() > 1 && listDutyStatus.get(1).getStatus() == 2) {
                    final AlertDialog ad = new AlertDialog.Builder(Utility.context)
                            .create();
                    ad.setCancelable(true);
                    ad.setCanceledOnTouchOutside(false);
                    ad.setTitle("Exclude Sleeper Hours ?");
                    //ad.setIcon(R.drawable.la);
                    ad.setMessage(" Do you want to exclude sleeper hours from 16 hours?");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (listDutyStatus.get(1).getTotalMinutes() >= 2 * 60 && listDutyStatus.get(1).getTotalMinutes() <= 8 * 60) {
                                        try {
                                            ExcludeSleeperStartTime = Utility.parse(listDutyStatus.get(1).getStartTime());
                                        } catch (Exception exe) {

                                        }
                                    }
                                }
                            });
                    ad.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ad.cancel();
                                }
                            });
                    ad.show();
                }
            }

            if (Utility.activeUserId == Utility.onScreenUserId) {
                MainActivity.activeCurrentDutyStatus = status;
                if (status == 1 && Utility.statusFlag == 1 && lastDutyStatus.getPersonalUse() == 1) {
                    MainActivity.activeCurrentDutyStatus = 5;
                }
            }
            MainActivity.currentDutyStatus = status;


            if (status == 1 && Utility.statusFlag == 1 && lastDutyStatus.getPersonalUse() == 1) {
                MainActivity.currentDutyStatus = 5;

            }
            ElogActivity.resetDT = status == 2 ? Utility.addMinutes(Utility.parse(lastDutyStatus.getStartTime()), ElogActivity.activeCycle == 2 ? 72 : 36) : null;
            if (status == 3) {
                String date = Utility.getCurrentDateTime();
                String dateOnly = Utility.format(Utility.newDateOnly(), fullFormat);
                violations = new ArrayList<>();
                if (ELogFragment.currentRule == 3) {

                    Violation395(date, false);
                    Violation395_3B(dateOnly);

                    if (listCoDriverDutyStatus.size() > 0) {
                        TeamOffDutyUSA(dateOnly);
                    }

                } else {
                    Violation12(dateOnly);
                    Violation14(dateOnly);
                    Violation13(date, true);
                    Violation25(dateOnly);
                    if (ELogFragment.currentRule == 1) {
                        Violation26(dateOnly);

                    } else {
                        Violation27(dateOnly);
                    }
                }
                if (violations.size() > 0) {
                    Collections.sort(violations, ViolationBean.dateAsc);
                    ViolationBean vBean = violations.get(0);

                    ELogFragment.ViolationDT = vBean.getStartTime();
                    ELogFragment.ViolationDescription = vBean.getRule() + ": " + vBean.getTitle();
                    MainActivity.ViolationDT = vBean.getStartTime();
                    ELogFragment.ViolationTitle = vBean.getTitle();
                    ELogFragment.ViolationExplanation = vBean.getExplanation();
                    if (mListener != null) {
                        mListener.onUpdateViolation(vBean.getStartTime().before(Utility.newDate()));
                    }

                } else {
                    ELogFragment.ViolationDT = null;
                    MainActivity.ViolationDT = null;
                    ELogFragment.ViolationDescription = "";
                    if (mListener != null) {
                        mListener.onUpdateViolation(false);
                    }
                }

            }
        } catch (Exception exe) {
        }
    }

    public static void ViolationCalculation(Date logDate, int driverId) {
        listDutyStatus = HourOfServiceDB.DutyStatusGet15Days(logDate, driverId + "", false);

        if (listDutyStatus.size() == 0) {
            return;
        }
        try {
            DutyStatusBean lastDutyStatus = listDutyStatus.get(0);
            int status = lastDutyStatus.getStatus();

            String date = Utility.format(logDate, fullFormat);
            String dateOnly = Utility.format(Utility.dateOnlyGet(logDate), fullFormat);
            violations = new ArrayList<>();


            if (ELogFragment.currentRule == 3) {
                Violation395(date, false);
                Violation395_3B(dateOnly);

                if (listCoDriverDutyStatus.size() > 0) {
                    TeamOffDutyUSA(dateOnly);
                }
            } else {

                Violation12(dateOnly);
                Violation14(dateOnly);
                Violation13(date, true);
                Violation25(dateOnly);
                if (ELogFragment.currentRule == 1) {
                    Violation26(dateOnly);

                } else {
                    Violation27(dateOnly);
                }

            }
            Collections.sort(violations, ViolationBean.dateAsc);


        } catch (Exception exe) {
        }

    }

    public static void ViolationAdd(String rule, Date startDate, int duration, boolean canadaFg, String title, String explanation) {
        if (duration <= 0)
            return;
        ViolationBean bean = new ViolationBean();
        bean.setRule(rule);
        bean.setStartTime(startDate);
        bean.setTotalMinutes(duration);
        bean.setCanadaFg(canadaFg);
        bean.setTitle(title);
        bean.setExplanation(explanation);
        violations.add(bean);
    }

    public static ArrayList<ViolationBean> Violation12(String date) {
        try {
            Date currentDate = Utility.parse(date);
            Date nextDay = Utility.addDays(currentDate, 1);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusDrivingGet(currentDate, listDutyStatus);
            int drivingMinutes = 0;
            int onDutyMinutes = 0;

            for (DutyStatusBean dutyStatus : data) {
                Date startDate = Utility.parse(dutyStatus.getStartTime());
                startDate = Utility.addSeconds(startDate, -startDate.getSeconds());

                Date endDate = Utility.parse(dutyStatus.getEndTime());
                endDate = Utility.addSeconds(endDate, -endDate.getSeconds());

                Date statusStartTime = startDate.before(currentDate) ? currentDate : startDate;
                statusStartTime = Utility.addSeconds(statusStartTime, -statusStartTime.getSeconds());

                Date statusEndTime = endDate.after(nextDay) ? nextDay : endDate;
                statusEndTime = Utility.addSeconds(statusEndTime, -statusEndTime.getSeconds());

                int statusMinutes = (int) ((statusEndTime.getTime() - statusStartTime.getTime())
                        / (1000 * 60));

                if ((drivingMinutes + statusMinutes) > 13 * 60) {
                    Date violationDate = Utility.addMinutes(statusStartTime, (13 * 60 - drivingMinutes));
                    int violationMinutes = (int) ((statusEndTime.getTime() - violationDate.getTime())
                            / (1000 * 60));
                    ViolationAdd("12A", violationDate, violationMinutes, true, "Maximum 13 hours of driving in a day", "Canadian law permits you to drive for a maximum of 13 hours in any 24 hours period based on when your 24 hours period starts. Most drivers prefer mid-night to mid-night as their 24 hours period.");
                } else
                    drivingMinutes += statusMinutes;

                if ((onDutyMinutes + statusMinutes) > 14 * 60) {

                    Date violationDate = Utility.addMinutes(statusStartTime, (14 * 60 - drivingMinutes));
                    int violationMinutes = (int) ((statusEndTime.getTime() - violationDate.getTime())
                            / (1000 * 60));
                    ViolationAdd("12B", violationDate, violationMinutes, true, "No driving after 14 hours on-duty in a day", "Canadian law permits you to be on-duty (on-duty and driving) for 14 hours in a 24 hours period based on when your 24 hours period starts. You cannot drive after 14 hours of on-duty in a day. Most drivers prefer a mid-night to mid-night period.");
                } else
                    onDutyMinutes += statusMinutes;

            }
        } catch (Exception exe) {
        }

        return violations;
    }

    public static ArrayList<ViolationBean> Violation13(String date, boolean excludeFg) {
        int restMinutes = 0;
        int sleeperMinutes = 0;

        int drivingMinutes = 0;
        int onDutyMinutes = 0;
        int elapsedHours = 0;
        boolean teamFg = false;
        try {
            Date currentDate = Utility.parse(date);
            Date nextDay = Utility.addDays(currentDate, 1);

            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet(currentDate, listDutyStatus);
            Date startDate = null;
            boolean setStartDate = false;
            int i = 0;
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                int status = item.getStatus();
                Date statusStartDate = Utility.parse(item.getStartTime());
                statusStartDate = Utility.addSeconds(statusStartDate, -statusStartDate.getSeconds());

                Date endDate = Utility.parse(item.getEndTime());
                endDate = Utility.addSeconds(endDate, -endDate.getSeconds());

                if (statusStartDate.before(currentDate) && status <= 2) {
                    Date prevDate = (i == 0 ? statusStartDate : Utility.parse(data.get(i - 1).getStartTime()));
                    prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                    if (i > 0 && item.getEndTime().equals(prevDate)) {
                        restMinutes += item.getTotalMinutes();
                    } else
                        restMinutes = item.getTotalMinutes();

                    if (restMinutes >= 8 * 60) {
                        setStartDate = true;
                        startDate = Utility.addMinutes(statusStartDate, restMinutes);
                        i++;
                        break;
                    }

                    //split logic
                    if (status == 2 && !deferedFg) {
                        if (item.getTotalMinutes() >= (teamFg ? 10 : 8) * 60 - sleeperMinutes) {
                            setStartDate = true;
                            startDate = endDate;
                            splitTime = endDate;
                            i++;
                            break;
                        }
                        DutyStatusBean coDriverCurrentStatus = null;
                        for (int j = 0; j < listCoDriverDutyStatus.size(); j++) {
                            if (Utility.parse(listCoDriverDutyStatus.get(j).getStartTime()).before(statusStartDate)) {
                                coDriverCurrentStatus = listCoDriverDutyStatus.get(j);
                                break;
                            }
                        }
                        teamFg = (coDriverCurrentStatus != null && coDriverCurrentStatus.isStatusFg());
                        if (item.getTotalMinutes() > (teamFg ? 4 : 2) * 60) {
                            sleeperMinutes = item.getTotalMinutes();
                        }
                    }

                }
            }

            if (i == 0) {
                i++;
            }

            restMinutes = 0;

            if (!setStartDate) {
                data = new ArrayList<>();
                for (int j = 0; j < listDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(listDutyStatus.get(j).getStartTime());
                    startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                    if (startTime.before(nextDay)) {
                        data.add(listDutyStatus.get(j));
                    }
                }
            } else {
                data = new ArrayList<>();
                for (int j = 0; j < listDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(listDutyStatus.get(j).getStartTime());
                    startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                    if (startTime.after(startDate) || startTime.equals(startDate)) {
                        data.add(listDutyStatus.get(j));
                    }
                }
            }

            Collections.sort(data, DutyStatusBean.dateAsc);

            Date s2Split = null;
            Date s1Split = null;

            for (int j = 0; j < data.size(); j++) {
                DutyStatusBean item = data.get(j);
                Date startTime = Utility.parse(item.getStartTime());
                startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                Date endTime = Utility.parse(item.getEndTime());
                // endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;

                int totalMinutes = item.getTotalMinutes();//(int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);
                int status = item.getStatus();

                if (Utility.dateOnlyGet(Utility.format(startTime, fullFormat)).after(Utility.dateOnlyGet(Utility.format(currentDate, fullFormat)))) {
                    break;
                }
                Date prevDate = j == 0 ? startTime : Utility.parse(data.get(j - 1).getEndTime());
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                // consective 8 hours rest logic (sleeper/ offduty)
                if (j > 0 && startTime.equals(prevDate)
                        && status <= 2 && data.get(j - 1).getStatus() <= 2) {
                    restMinutes += totalMinutes;
                } else if (status <= 2)
                    restMinutes = totalMinutes;

                if (restMinutes >= 8 * 60) {
                    if (j < data.size() - 1 && data.get(j + 1).getStatus() <= 2) {
                        continue;
                    }

                    drivingMinutes = 0;
                    onDutyMinutes = 0;
                    elapsedHours = 0;
                    restMinutes = 0;
                    continue;
                }

                if (s2Split != null && s2Split == startTime && !deferedFg) {
                    for (int k = 0; k < data.size(); k++) {
                        if (Utility.parse(data.get(k).getStartTime()).after(s1Split) && Utility.parse(data.get(k).getStartTime()).before(s2Split)) {


                            if (data.get(k).getStatus() >= 3) {
                                if (data.get(k).getStatus() == 3) {
                                    drivingMinutes += data.get(k).getTotalMinutes();
                                }
                                onDutyMinutes += data.get(k).getTotalMinutes();
                            }
                            elapsedHours += data.get(k).getTotalMinutes();
                        }
                    }
                }

                drivingMinutes += status == 3 && drivingMinutes != -1 ? totalMinutes : 0;
                onDutyMinutes += status >= 3 && onDutyMinutes != -1 ? totalMinutes : 0;
                elapsedHours += elapsedHours == -1 ? 0 : totalMinutes;

                if (ExcludeSleeperStartTime != null && startTime.equals(ExcludeSleeperStartTime) && status == 2 && totalMinutes >= 2 * 60 && excludeFg) {
                    elapsedHours -= totalMinutes;
                }

                //split logic
                if (status == 2 && totalMinutes >= 2 * 60 && !deferedFg) {
                    sleeperMinutes = totalMinutes;
                    ArrayList<DutyStatusBean> s2List = new ArrayList<>();
                    for (int k = 0; k < data.size(); k++) {
                        if (Utility.parse(data.get(k).getStartTime()).after(startTime) && ((status == 1 && totalMinutes > 8 * 60) || status == 2)) {
                            s2List.add(data.get(k));
                        }
                    }

                    Collections.sort(s2List, DutyStatusBean.dateAsc);

                    int k = 0;
                    for (k = 0; k < s2List.size(); k++) {
                        if (s2List.get(k).getStatus() == 1)
                            break;

                        DutyStatusBean codriverBeforess2 = null;
                        Collections.sort(listCoDriverDutyStatus, DutyStatusBean.dateDesc);
                        for (int l = 0; l < listCoDriverDutyStatus.size(); l++) {
                            if (Utility.parse(listCoDriverDutyStatus.get(l).getStartTime()).before(Utility.parse(s2List.get(k).getEndTime()))) {
                                codriverBeforess2 = listCoDriverDutyStatus.get(l);
                                break;
                            }
                        }
                        boolean cdStatus = (codriverBeforess2 != null && codriverBeforess2.isStatusFg());

                        if (cdStatus) {
                            if (sleeperMinutes >= 4 * 60 && s2List.get(k).getTotalMinutes() >= 4 * 60) {
                                if ((sleeperMinutes + s2List.get(k).getTotalMinutes()) == 8 * 60) {
                                    s1Split = startTime;
                                    s2Split = Utility.parse(s2List.get(k).getStartTime());
                                    for (int l = 0; l < data.size(); l++) {
                                        if (Utility.parse(data.get(l).getStartTime()).after(startTime) && Utility.parse(data.get(l).getStartTime()).before(s2Split) && data.get(l).getStatus() == 2
                                                && data.get(l).getTotalMinutes() >= 8 * 60 - s2List.get(k).getTotalMinutes() && data.get(l).getTotalMinutes() >= 4 * 60) {
                                            s2List.set(k, data.get(l));
                                            break;
                                        }
                                    }

                                    if (s2List.get(k) == null) {
                                        if (elapsedHours != -1) {
                                            elapsedHours -= totalMinutes;
                                        }
                                        break;
                                    } else {
                                        s2Split = null;
                                    }
                                }
                            }
                        } else {
                            if (sleeperMinutes >= 2 * 60) {
                                if ((sleeperMinutes + s2List.get(k).getTotalMinutes()) >= 10 * 60) {
                                    s1Split = startTime;
                                    s2Split = Utility.parse(s2List.get(k).getStartTime());

                                    for (int l = 0; l < data.size(); l++) {
                                        if (Utility.parse(data.get(l).getStartTime()).after(startTime) && Utility.parse(data.get(l).getStartTime()).before(s2Split) && data.get(l).getStatus() == 2
                                                && data.get(l).getTotalMinutes() >= 10 * 60 - s2List.get(k).getTotalMinutes() && data.get(l).getTotalMinutes() >= 2 * 60) {
                                            s2List.set(k, data.get(l));
                                            break;
                                        }
                                    }

                                    if (s2List.get(k) == null) {
                                        if (elapsedHours != -1) {
                                            elapsedHours -= totalMinutes;
                                        }
                                        break;
                                    } else {
                                        s2Split = null;
                                    }
                                }
                            }
                        }
                    }
                }

                if (status == 3) {
                    Date vStart;
                    if (drivingMinutes > 13 * 60) {
                        vStart = Utility.addMinutes(startTime, (13 * 60 - (drivingMinutes - totalMinutes)));
                        if (vStart.before(startTime)) {
                            vStart = startTime;
                        }
                        int duration = (int) (Utility.parse(item.getEndTime()).getTime() - vStart.getTime()) / (1000 * 60);
                        ViolationAdd("13A", vStart, duration, true, "Must not drive beyond 13 hours without taking 8 consecutive hours off-duty", "Canadian law permits you to only drive 13 hours after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 8 consecutive hours off before driving is permitted.");
                        drivingMinutes = -1;
                    } else if (drivingMinutes == -1) {
                        ViolationAdd("13A", startTime, totalMinutes, true, "Must not drive beyond 13 hours without taking 8 consecutive hours off-duty", "Canadian law permits you to only drive 13 hours after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 8 consecutive hours off before driving is permitted.");
                    }

                    if (onDutyMinutes > 14 * 60) {
                        vStart = Utility.addMinutes(startTime, (14 * 60 - (onDutyMinutes - totalMinutes)));
                        if (vStart.before(startTime)) {
                            vStart = startTime;
                        }
                        int duration = (int) (Utility.parse(item.getEndTime()).getTime() - vStart.getTime()) / (1000 * 60);
                        ViolationAdd("13B", vStart, duration, true, "Must not drive after 14 hours of on-duty time unless 8 consecutive hours of off-duty time is taken.", "Canadian law permits you to only be on-duty for 14 hours after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You will be required to take another 8 consecutive hours off before driving is permitted.");
                        onDutyMinutes = -1;
                    } else if (onDutyMinutes == -1) {
                        ViolationAdd("13B", startTime, totalMinutes, true, "Must not drive after 14 hours of on-duty time unless 8 consecutive hours of off-duty time is taken.", "Canadian law permits you to only be on-duty for 14 hours after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You will be required to take another 8 consecutive hours off before driving is permitted.");
                    }

                    if (elapsedHours > 16 * 60) {
                        vStart = Utility.addMinutes(startTime, (16 * 60 - (elapsedHours - totalMinutes)));
                        if (vStart.before(startTime)) {
                            vStart = startTime;
                        }

                        int duration = (int) (Utility.parse(item.getEndTime()).getTime() - vStart.getTime()) / (1000 * 60);
                        ViolationAdd("13C", vStart, duration, true, "Must not drive beyond 16 hours of elapsed time after last 8 consecutive hours off-duty", "Canadian law permits you to only have 16 hours of elapsed time after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 8 consecutive hours off before driving is permitted. Drivers normally refer to this as their work-shift.");
                        elapsedHours = -1;
                    } else if (elapsedHours == -1) {
                        ViolationAdd("13C", startTime, totalMinutes, true, "Must not drive beyond 16 hours of elapsed time after last 8 consecutive hours off-duty", "Canadian law permits you to only have 16 hours of elapsed time after taking 8 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 8 consecutive hours off before driving is permitted. Drivers normally refer to this as their work-shift.");

                    }
                }
            }
        } catch (Exception exe) {
        }
        return violations;
    }

    public static ArrayList<ViolationBean> Violation14(String date) {
        try {
            Date currentDate = Utility.parse(date);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusGet(currentDate, listDutyStatus); // get duty status logic

            if (data.size() == 0) {
                return violations;
            }
            Date firstStart = Utility.parse(data.get(0).getStartTime());
            if (firstStart.equals(Utility.addMinutes(currentDate, 30)) || firstStart.after(Utility.addMinutes(currentDate, 30))) {
                int firstMinutes = (int) (firstStart.getTime() - currentDate.getTime()) / (60 * 1000);
                //(int status, Date startTime, Date endTime, int totalMinutes, int personalUseFg)
                data.add(HourOfServiceDB.DutyStatusObjectGet(1, currentDate, firstStart, firstMinutes, 0));
                Collections.sort(data, DutyStatusBean.dateAsc);
            }
            // 30 min logic skips
            ArrayList<DutyStatusBean> offDutyStatusInDay = new ArrayList<DutyStatusBean>();
            int minutesExcluding8Hours = 0;
            int offdutyMinutes = 0;
            int offdutySleeperHour = 0;
            int i = 0;
            Date nextDay = Utility.addDays(currentDate, 1);

            for (DutyStatusBean item : data) {
                Date startDate = Utility.parse(item.getStartTime());
                Date endDate = Utility.parse(item.getEndTime());

                Date statusStartTime = startDate.before(currentDate) ? currentDate : startDate;
                statusStartTime = Utility.addSeconds(statusStartTime, -statusStartTime.getSeconds());

                Date statusEndTime = endDate.after(nextDay) ? nextDay : endDate;
                statusEndTime = Utility.addSeconds(statusEndTime, -statusEndTime.getSeconds());

                int statusMinutes = (int) ((statusEndTime.getTime() - statusStartTime.getTime())
                        / (1000 * 60));
                if (item.getStatus() <= 2) {

                    Date prevDate = (i == 0 ? startDate : Utility.parse(data.get(i - 1).getStartTime()));
                    prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                    if (i > 0 && statusStartTime.equals(prevDate)
                            && (data.get(i - 1).getStatus() <= 2)) {
                        offdutySleeperHour = statusMinutes + data.get(i - 1).getTotalMinutes();
                    } else {
                        offdutySleeperHour = 0;
                    }

                    if ((statusStartTime.after(currentDate) || statusStartTime.equals(currentDate))
                            && (statusEndTime.before(nextDay) || statusEndTime.equals(nextDay)) && statusMinutes >= 30) {
                        DutyStatusBean offDuty = new DutyStatusBean();
                        offDuty.setStartTime(Utility.format(statusStartTime, fullFormat));
                        offDuty.setEndTime(Utility.format(statusEndTime, fullFormat));
                        offDuty.setTotalMinutes(statusMinutes);
                        offDutyStatusInDay.add(offDuty);
                        offdutyMinutes += statusMinutes;

                        if ((statusEndTime.getTime() - statusStartTime.getTime()) / (1000 * 60) >= 8 * 60) {
                            minutesExcluding8Hours += ((statusEndTime.getTime() - statusStartTime.getTime()) / (1000 * 60) - 8 * 60);
                        } else if (offdutySleeperHour >= 8 * 60) {
                            minutesExcluding8Hours += (offdutySleeperHour - 8 * 60);
                        } else {
                            minutesExcluding8Hours += statusMinutes;
                        }
                    }

                }
                i++;
            }

            if (data.size() == 1 && data.get(0).getStatus() <= 2 && Utility.parse(data.get(0).getEndTime()).getSeconds() == 59) {
                DutyStatusBean offDuty = new DutyStatusBean();
                offDuty.setStartTime(data.get(0).getStartTime());
                offDuty.setEndTime(Utility.format(nextDay, fullFormat));
                offDuty.setTotalMinutes(24 * 60);
                offDutyStatusInDay.add(offDuty);
                minutesExcluding8Hours = 2 * 60;
            }

            if (offdutyMinutes < 10 * 60) {
                int vMinutes = (10 * 60 - offdutyMinutes);
                Date vStartDate = Utility.addMinutes(nextDay, -vMinutes);
                offdutyMinutes = 0;
                for (DutyStatusBean item : offDutyStatusInDay) {
                    offdutyMinutes += item.getTotalMinutes();
                    int iEndMinute = (int) (nextDay.getTime() - Utility.parse(item.getEndTime()).getTime()) / (1000 * 60);
                    if (offdutyMinutes + iEndMinute < 10 * 60) {
                        vStartDate = currentDate.equals(Utility.dateOnlyGet(item.getEndTime())) ? currentDate : Utility.addMinutes(vStartDate, -(item.getTotalMinutes() + iEndMinute));
                        break;
                    }
                }
                ViolationAdd("14A", vStartDate, vMinutes, true, "Must have minimum 10 hours off-duty in a day.", "Canadian law requires you to have a minimum of 10 hours off-duty or sleeper-berth or any combination of off-duty and sleeper-berth time in a 24 hours period depending on when you consider your 24 hours to start. Most drivers prefer mid-night to mid-night as their 24 hours period. ");

            }

            if (minutesExcluding8Hours < 2 * 60) {

                int vMinutes = (2 * 60 - minutesExcluding8Hours);
                ViolationAdd("14C", Utility.addMinutes(nextDay, -vMinutes), vMinutes, true, "Must have minimum 2 hours off-duty in a day that is not part of 8 consecutive required hours.", "Canadian law requires minimum 2 hours off-duty or sleeper-berth or any combination of off-duty and sleeper-berth that is not part of the 8 consecutive hours. Off-duty time must be in blocks of not less than 30 minutes each to be considered towards minimum of 10 hours of required off-duty in a day.");

            }
        } catch (Exception exe) {
        }

        return violations;
    }

    public static ArrayList<ViolationBean> Violation16(String date) {
        try {
            Date currentDate = Utility.parse(date);
            Date nextDay = Utility.addDays(currentDate, 1);
            Date nextTwoDay = Utility.addDays(currentDate, 2);
            int drivingMinutes = 0, onDutyMinutes = 0, offDutyMinutes = 0;
            ArrayList<DutyStatusBean> day1Status = new ArrayList<>();
            ArrayList<DutyStatusBean> day2Status = new ArrayList<>();
            for (DutyStatusBean item : listDutyStatus) {
                Date startTime = Utility.parse(item.getStartTime());
                Date endTime = Utility.parse(item.getEndTime());

                startTime = startTime.before(currentDate) ? currentDate : startTime;
                endTime = endTime.after(nextDay) ? nextDay : endTime;
                int totalMinutes = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);

                if ((startTime.after(deferedDate) || startTime.equals(deferedDate)) && startTime.before(nextDay)) {
                    DutyStatusBean bean = new DutyStatusBean();
                    bean.setStartTime(Utility.format(startTime, fullFormat));
                    bean.setEndTime(Utility.format(endTime, fullFormat));
                    bean.setTotalMinutes(totalMinutes);
                    bean.setStatusFg(item.isStatusFg());
                    day1Status.add(item);
                }


                if ((startTime.after(Utility.addDays(deferedDate, 1)) || startTime.equals(Utility.addDays(deferedDate, 1))) && startTime.before(nextTwoDay)) {

                    startTime = startTime.before(nextDay) ? nextDay : startTime;
                    endTime = endTime.after(nextTwoDay) ? nextTwoDay : endTime;
                    totalMinutes = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);

                    DutyStatusBean bean = new DutyStatusBean();
                    bean.setStartTime(Utility.format(startTime, fullFormat));
                    bean.setEndTime(Utility.format(endTime, fullFormat));
                    bean.setTotalMinutes(totalMinutes);
                    bean.setStatusFg(item.isStatusFg());
                    day2Status.add(item);
                }


            }


            ArrayList<DutyStatusBean> day1OffDuty = new ArrayList<>();
            ArrayList<DutyStatusBean> data = new ArrayList<>();
            int day1TotalOffDutyMinutes = 0;
            for (DutyStatusBean item : day1Status) {
                if (item.getStatus() <= 2) {
                    day1TotalOffDutyMinutes += item.getTotalMinutes();
                    day1OffDuty.add(item);
                }
                data.add(item);
            }

            int day2TotalOffDutyMinutes = 0;
            ArrayList<DutyStatusBean> day2OffDuty = new ArrayList<>();
            for (DutyStatusBean item : day2Status) {
                if (item.getStatus() <= 2) {
                    day2TotalOffDutyMinutes += item.getTotalMinutes();
                    day2OffDuty.add(item);
                }
                data.add(item);
            }

            int restMinutes = 0;
            for (int i = 0; i < day1OffDuty.size(); i++) {
                Date startTime = Utility.parse(day1OffDuty.get(i).getStartTime());

                if (i > 0 && startTime.equals(Utility.parse(day1OffDuty.get(i - 1).getEndTime()))) {
                    restMinutes += day1OffDuty.get(i - 1).getTotalMinutes();
                } else {
                    restMinutes = day1OffDuty.get(i).getTotalMinutes();
                }
            }

            if (Utility.dateOnlyGet(currentDate).equals(Utility.dateOnlyGet(deferedDate))) {
                for (DutyStatusBean item : day1Status) {
                    if (item.getStatus() == 3) {
                        if ((drivingMinutes + item.getTotalMinutes()) > 15 * 60) {
                            Date vStart = Utility.addMinutes(Utility.parse(item.getStartTime()), (15 * 60 - drivingMinutes));
                            int duration = (int) (Utility.parse(item.getEndTime()).getTime() - vStart.getTime()) / (1000 * 60);
                            ViolationAdd("16A", vStart, duration, true, "", "");

                        } else
                            drivingMinutes += item.getTotalMinutes();
                    }
                }
            }

            drivingMinutes = 0;
            if (restMinutes >= 8 * 60 && day1TotalOffDutyMinutes < 10 * 60) {
                int day1DefferedMinutes = 10 * 60 - day1TotalOffDutyMinutes;
                offDutyMinutes = day1TotalOffDutyMinutes + day2TotalOffDutyMinutes;
                if (offDutyMinutes < 20 * 60) {
                    offDutyMinutes = 20 * 60 - offDutyMinutes;
                    Date vStart = Utility.addMinutes(Utility.addDays(deferedDate, 2), -offDutyMinutes);
                    ViolationAdd("16B", vStart, offDutyMinutes, true, "Must take 20 hours off-duty in 2 days when deferring off-duty time.", "Canadian law requires minimum 20 hours off-duty in 2 days if you are deferring off-duty time. This is the total of Day 1 and Day 2. You cannot defer any part of the 8 consecutive hours of required off-duty time.");
                }
                restMinutes = 0;
                for (int i = 0; i < day2OffDuty.size(); i++) {
                    if (i > 0 && Utility.parse(day2OffDuty.get(i - 1).getEndTime()).equals(Utility.parse(day2OffDuty.get(i).getStartTime()))) {
                        restMinutes += day2OffDuty.get(i - 1).getTotalMinutes();
                    } else
                        restMinutes = day2OffDuty.get(i).getTotalMinutes();
                }
                offDutyMinutes = (8 * 60 + day1DefferedMinutes) - restMinutes;
                if (offDutyMinutes > 0) {
                    Date vStart = Utility.addMinutes(deferedDate, (48 * 60 - offDutyMinutes));

                    ViolationAdd("16C", vStart, offDutyMinutes, true, "", "");
                }

                for (DutyStatusBean item : data) {
                    if (item.getStatus() <= 4) {
                        if (item.getStatus() == 3) {
                            if (onDutyMinutes + item.getTotalMinutes() > 28 * 60) {
                                Date vStart = Utility.addMinutes(Utility.parse(item.getStartTime()), (28 * 60 - drivingMinutes));
                                int duration = (int) (Utility.parse(item.getEndTime()).getTime() - vStart.getTime()) / (1000 * 60);
                                ViolationAdd("16 (28Hrs Onduty)", vStart, duration, true, "", "");
                                onDutyMinutes = -1;
                            } else if (onDutyMinutes == -1) {
                                ViolationAdd("16 (28Hrs Onduty)", Utility.parse(item.getStartTime()), item.getTotalMinutes(), true, "", "");
                            }

                            if (drivingMinutes + item.getTotalMinutes() > 26 * 60) {
                                Date vStart = Utility.addMinutes(Utility.parse(item.getStartTime()), (26 * 60 - drivingMinutes));
                                int duration = (int) (Utility.parse(item.getEndTime()).getTime() - vStart.getTime()) / (1000 * 60);
                                ViolationAdd("16D", vStart, duration, true, "Must not exceed 26 hours of drive time in 2 days.", "Canadian law permits you to only have 26 hours of drive time in 2 days if you are deferring off-duty time. This is your Day 1 and Day 2 total.");
                                drivingMinutes = -1;
                            } else if (drivingMinutes == -1) {
                                ViolationAdd("16D", Utility.parse(item.getStartTime()), item.getTotalMinutes(), true, "Must not exceed 26 hours of drive time in 2 days.", "Canadian law permits you to only have 26 hours of drive time in 2 days if you are deferring off-duty time. This is your Day 1 and Day 2 total.");

                            } else
                                drivingMinutes += item.getTotalMinutes();

                            if (onDutyMinutes != -1) {
                                onDutyMinutes += item.getTotalMinutes();
                            }
                        }
                    }
                }

            } else {
                Violation12(date);
                Violation14(date);
            }


        } catch (Exception exe) {
        }

        return violations;
    }

    public static ArrayList<ViolationBean> Violation25(String date) {
        try {
            Date currentDate = Utility.parse(date);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet25(currentDate, listDutyStatus); // get duty status logic

            int restMinutes = 0;
            int i = 0;

            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date endDate = Utility.parse(item.getEndTime());
                endDate = Utility.addSeconds(endDate, -endDate.getSeconds());

                Date prevDate = (i == 0 ? endDate : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                if (i > 0 && endDate.equals(prevDate)) {
                    restMinutes += item.getTotalMinutes();
                } else {
                    restMinutes = item.getTotalMinutes();
                }
                if (restMinutes >= 24 * 60) {
                    break;
                }
            }

            ArrayList<DutyStatusBean> drivingData = HourOfServiceDB.DutyStatusDrivingGet25(currentDate, listDutyStatus); // get duty status logic driving records of the day
            if (restMinutes >= 24 * 60) {
                Date startDate = Utility.addDays(Utility.addMinutes(Utility.parse(data.get(i).getStartTime()), restMinutes), 14);

                for (DutyStatusBean item : drivingData) {
                    Date statusStartDate = Utility.parse(item.getStartTime());
                    Date statusEndDate = Utility.parse(item.getEndTime());
                    if (statusStartDate.after(startDate) || statusStartDate.equals(startDate)) {
                        ViolationAdd("25", statusStartDate, item.getTotalMinutes(), true, "Must not drive unless 24 consecutive off-duty hours are taken in the preceding 14 days.", "Canadian law requires a minimum 24 consecutive off-duty hours or sleeper-berth hours or any combination of off-duty and sleeper-berth to be taken every 14 days. Your 24 hours can be part of cycle reset if you wish.");
                    } else if (statusStartDate.before(startDate) && statusEndDate.after(startDate)) {
                        int duration = (int) (statusEndDate.getTime() - startDate.getTime()) / (1000 * 60);
                        ViolationAdd("25", statusStartDate, duration, true, "Must not drive unless 24 consecutive off-duty hours are taken in the preceding 14 days.", "Canadian law requires a minimum 24 consecutive off-duty hours or sleeper-berth hours or any combination of off-duty and sleeper-berth to be taken every 14 days. Your 24 hours can be part of cycle reset if you wish.");

                    }
                }
            } else {
                Date startDate = data.size() > 0 ? Utility.addDays(Utility.parse(data.get(i - 1).getStartTime()), 14) : Utility.addDays(currentDate, 14);
                if (startDate.equals(currentDate) || startDate.before(currentDate)) {
                    for (DutyStatusBean item : drivingData) {
                        Date statusStartDate = Utility.parse(item.getStartTime());
                        if (item.getStatus() == 3) {
                            ViolationAdd("25", statusStartDate, item.getTotalMinutes(), true, "Must not drive unless 24 consecutive off-duty hours are taken in the preceding 14 days.", "Canadian law requires a minimum 24 consecutive off-duty hours or sleeper-berth hours or any combination of off-duty and sleeper-berth to be taken every 14 days. Your 24 hours can be part of cycle reset if you wish.");
                        }
                    }

                }
            }


        } catch (Exception exe) {
        }
        return violations;
    }

    public static ArrayList<ViolationBean> Violation26(String date) {

        try {

            Date currentDate = Utility.parse(date);
            Date nextDay = Utility.addDays(currentDate, 1);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet26(currentDate, listDutyStatus); // get duty status logic
            int restMinutes = 0;
            int i = 0;

            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date endDate = Utility.parse(item.getEndTime());
                endDate = Utility.addSeconds(endDate, -endDate.getSeconds());

                Date prevDate = (i == 0 ? endDate : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());
                if (i > 0 && endDate.equals(prevDate)) {
                    restMinutes += item.getTotalMinutes();
                } else {
                    restMinutes = item.getTotalMinutes();
                }

                if (restMinutes >= 36 * 60) {
                    break;
                }
            }

            int onDutyMinutes = 0;
            if (restMinutes >= 36 * 60) {
                Date startDate = Utility.addMinutes(Utility.parse(data.get(i).getStartTime()), restMinutes);
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);
                // calculate sum of the onduty status minutes
            } else {
                Date startDate = Utility.addDays(currentDate, -6);
                // calculate sum of the onduty status minutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);

            }
            //calculate driving and on duty data for the day
            data = HourOfServiceDB.DutyStatusDrivingOnDutyGet26(currentDate, listDutyStatus);

            for (DutyStatusBean item : data) {
                Date startDate = Utility.parse(item.getStartTime());
                Date endDate = Utility.parse(item.getEndTime());


                Date statusStartTime = startDate.before(currentDate) ? currentDate : startDate;
                statusStartTime = Utility.addSeconds(statusStartTime, -statusStartTime.getSeconds());

                Date statusEndTime = endDate.after(nextDay.after(Utility.newDate()) ? Utility.newDate() : nextDay) ? (nextDay.after(Utility.newDate()) ? Utility.newDate() : nextDay) : endDate;
                statusEndTime = Utility.addSeconds(statusEndTime, -statusEndTime.getSeconds());

                int statusMinutes = (int) ((statusEndTime.getTime() - statusStartTime.getTime())
                        / (1000 * 60));
                if (item.getStatus() == 3) {
                    if (onDutyMinutes + statusMinutes > 70 * 60) {
                        Date vStart = Utility.addMinutes(statusStartTime, (70 * 60 - onDutyMinutes));
                        vStart = vStart.before(statusStartTime) ? statusStartTime : vStart;
                        int duration = (int) ((statusEndTime.getTime() - vStart.getTime()) / (1000 * 60));
                        ViolationAdd("26", vStart, duration, true, "Must not drive after 70 hours on-duty in 7 days.", "Canadian law does not permit you to drive after 70 hours on-duty time in any period of 7 days. You can take 36 hours off-duty or sleeper-berth or any combination of to reset your cycle 1 or you can wait until the 8th day to gain hours back. ");
                        onDutyMinutes = -1;
                    } else if (onDutyMinutes == -1) {
                        ViolationAdd("26", statusStartTime, statusMinutes, true, "Must not drive after 70 hours on-duty in 7 days.", "Canadian law does not permit you to drive after 70 hours on-duty time in any period of 7 days. You can take 36 hours off-duty or sleeper-berth or any combination of to reset your cycle 1 or you can wait until the 8th day to gain hours back. ");
                    }
                }
                onDutyMinutes += onDutyMinutes == -1 ? 0 : statusMinutes;

            }

        } catch (Exception exe) {
        }
        return violations;
    }

    public static ArrayList<ViolationBean> Violation27(String date) {
        int restMinutes = 0;
        Date start24HoursTime = null;
        Date start120HoursTime = null;
        int i = 0;
        try {

            Date currentDate = Utility.parse(date);
            Date nextDay = Utility.addDays(currentDate, 1);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet26(currentDate, listDutyStatus); // get duty status logic
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startDate = Utility.parse(item.getStartTime());
                startDate = Utility.addSeconds(startDate, -startDate.getSeconds());

                Date endDate = Utility.parse(item.getEndTime());
                endDate = Utility.addSeconds(endDate, -endDate.getSeconds());

                Date prevDate = (i == 0 ? startDate : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());
                if (i > 0 && endDate.equals(prevDate)) {
                    restMinutes += item.getTotalMinutes();
                } else {
                    restMinutes = item.getTotalMinutes();
                }
                if (restMinutes >= 24 * 60) {
                    if (start24HoursTime == null) {
                        start24HoursTime = Utility.addMinutes(startDate, restMinutes);
                    }

                    if (restMinutes >= 72 * 60) {
                        start120HoursTime = Utility.addMinutes(startDate, restMinutes);
                        break;
                    }
                }
            }

            int onDutyMinutesA = 0;
            int onDutyMinutesB = 0;

            if (restMinutes >= 72 * 60) {
                Date startDate = Utility.addMinutes(Utility.parse(data.get(i).getStartTime()), restMinutes);
                // calculate sum of the onduty status minutes onDutyMinutesA
                onDutyMinutesA = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);
            } else {
                Date startDate = Utility.addDays(currentDate, -13);
                // calculate sum of the onduty status minutes onDutyMinutesA
                onDutyMinutesA = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);
            }

            if (start24HoursTime != null) {
                // calculate sum of the onduty status minutes onDutyMinutesB
                onDutyMinutesB = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(start24HoursTime, currentDate, listDutyStatus);
            } else {
                Date startDate = Utility.addDays(currentDate, -15);
                // calculate sum of the onduty status minutes onDutyMinutesB
                onDutyMinutesB = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(start24HoursTime, currentDate, listDutyStatus);
            }


            data = HourOfServiceDB.DutyStatusDrivingGet(currentDate, listDutyStatus); // get duty status logic


            for (DutyStatusBean item : data) {
                Date startDate = Utility.parse(item.getStartTime());
                Date endDate = Utility.parse(item.getEndTime());


                Date statusStartTime = startDate.before(currentDate) ? currentDate : startDate;
                Date statusEndTime = endDate.after(nextDay.after(Utility.newDate()) ? Utility.newDate() : nextDay) ? (nextDay.after(Utility.newDate()) ? Utility.newDate() : nextDay) : endDate;
                int statusMinutes = (int) ((statusEndTime.getTime() - statusStartTime.getTime())
                        / (1000 * 60));
                if (item.getStatus() == 3) {
                    if (onDutyMinutesB + statusMinutes > 70 * 60) {
                        Date vStart = Utility.addMinutes(statusStartTime, (70 * 60 - onDutyMinutesB)); //doubtfull case
                        vStart = vStart.before(statusStartTime) ? statusStartTime : vStart;
                        int duration = (int) ((statusEndTime.getTime() - vStart.getTime()) / (1000 * 60));
                        ViolationAdd("27B", vStart, duration, true, "Must not drive after 70 hours on-duty unless 24 consecutive off-duty hours is taken.", "Canadian law does not permit you to drive after 70 hours on-duty unless you have taken 24 consecutive hours off-duty or in sleeper-berth or any combination thereof.  Your 24 hours can be part of cycle reset if you wish.");
                        onDutyMinutesB = -1;
                    } else if (onDutyMinutesB == -1) {
                        ViolationAdd("27B", statusStartTime, statusMinutes, true, "Must not drive after 70 hours on-duty unless 24 consecutive off-duty hours is taken.", "Canadian law does not permit you to drive after 70 hours on-duty unless you have taken 24 consecutive hours off-duty or in sleeper-berth or any combination thereof.  Your 24 hours can be part of cycle reset if you wish.");
                    } else if (onDutyMinutesA + statusMinutes > 120 * 60 && onDutyMinutesB != -1) {
                        Date vStart = Utility.addMinutes(statusStartTime, (120 * 60 - onDutyMinutesA));
                        vStart = vStart.before(statusStartTime) ? statusStartTime : vStart;
                        int duration = (int) ((statusEndTime.getTime() - vStart.getTime()) / (1000 * 60));
                        ViolationAdd("27A", vStart, duration, true, "Must not drive after 120 hours on-duty in 14 days.", "Canadian law does not permit you to drive after 120 hours on-duty time in any period of 14 days. You can take 72 hours off-duty or sleeper-berth or any combination thereof to reset your cycle 2 or you can wait until the 15th day to gain hours back.");
                        onDutyMinutesA = -1;
                    } else if (onDutyMinutesA == -1) {
                        ViolationAdd("27A", statusStartTime, statusMinutes, true, "Must not drive after 120 hours on-duty in 14 days.", "Canadian law does not permit you to drive after 120 hours on-duty time in any period of 14 days. You can take 72 hours off-duty or sleeper-berth or any combination thereof to reset your cycle 2 or you can wait until the 15th day to gain hours back.");
                    }
                }
                onDutyMinutesA += onDutyMinutesA == -1 ? 0 : statusMinutes;
                onDutyMinutesB += onDutyMinutesB == -1 ? 0 : statusMinutes;


            }

        } catch (Exception exe) {
        }
        return violations;
    }

    public static ArrayList<ViolationBean> Violation29(String date, int prevCycle) {
        try {
            Date currentDate = Utility.parse(date);

            ArrayList<DutyStatusBean> data = new ArrayList<>();
            for (DutyStatusBean item : listDutyStatus) {
                Date startTime = Utility.parse(item.getStartTime());
                if (startTime.before(currentDate)) {
                    data.add(item);
                }
            }
            Collections.sort(data, DutyStatusBean.dateDesc);
            int restMinutes = 0;
            for (int i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                Date endTime = Utility.parse(item.getEndTime());
                endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;
                int totalMinutes = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);

                if (item.getStatus() <= 4) {
                    if (startTime.before(Utility.dateOnlyGet(date))) {
                        break;
                    }
                    continue;
                }

                if (i > 0 && endTime.equals(data.get(i - 1).getStartTime()) && data.get(i - 1).getStatus() <= 2 && item.getStatus() <= 2) {
                    restMinutes += totalMinutes;
                } else
                    restMinutes = totalMinutes;

                if (prevCycle == 1 && restMinutes >= 36 * 60)
                    break;

                if (prevCycle == 2 && restMinutes >= 72 * 60)
                    break;

            }

            ArrayList<DutyStatusBean> drivings = HourOfServiceDB.DutyStatusDrivingGet(currentDate, listDutyStatus);
            DutyStatusBean driving = null;
            if (drivings.size() > 0) {
                driving = drivings.get(0);
            }

            if (prevCycle == 1 && restMinutes < 36 * 60) {
                if (driving != null) {
                    Date startTime = Utility.parse(driving.getStartTime());
                    startTime = startTime.before(Utility.dateOnlyGet(date)) ? Utility.dateOnlyGet(date) : startTime;
                    Date endTime = Utility.parse(driving.getEndTime());
                    endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;
                    int totalTime = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);
                    ViolationAdd("29A Cycle 1 to 2 wo 36hrs off-duty", startTime, totalTime, true, "Must take minimum 36 consecutive hours off-duty to switch to cycle 2 from cycle 1.", "Canadian law requires a minimum of 36 consecutive hours off-duty or sleeper-berth or any combination of off-duty and sleeper before switching to cycle 2 from cycle 1.");
                }
            } else if (prevCycle == 2 && restMinutes < 72 * 60) {
                if (driving != null) {
                    Date startTime = Utility.parse(driving.getStartTime());
                    startTime = startTime.before(Utility.dateOnlyGet(date)) ? Utility.dateOnlyGet(date) : startTime;
                    Date endTime = Utility.parse(driving.getEndTime());
                    endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;
                    int totalTime = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);
                    ViolationAdd("29B Cycle 2 to 1 wo 72hrs off-duty", startTime, totalTime, true, "Must take minimum 72 consecutive hours off-duty to switch from cycle 2 to cycle 1.", "Canadian law requires you to take a minimum of 72 consecutive hours off-duty or sleeper-berth or any combination of off-duty and sleeper before switching from cycle 2 to cycle 1.");
                }
            }


        } catch (Exception exe) {
        }
        return violations;
    }

    /**
     * *************************************************USA Rules**************************************************************************************
     */

    public static ArrayList<ViolationBean> Violation395(String date, boolean excludeFg) {

        int restMinutes = 0;
        int sleeperMinutes = 0;

        int drivingMinutes = 0;
        int elapsedMinutes = 0;
        int elapsedMinutes8 = 0;

        try {

            Date currentDate = Utility.parse(date);
            Date nextDay = Utility.addDays(currentDate, 1);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet(currentDate, listDutyStatus);
            int i = 0;
            Date startDate = null;
            boolean setStartDate = false;
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date statusStartDate = Utility.parse(item.getStartTime());
                statusStartDate = Utility.addSeconds(statusStartDate, -statusStartDate.getSeconds());

                Date endDate = Utility.parse(item.getEndTime());
                endDate = Utility.addSeconds(endDate, -endDate.getSeconds());

                Date prevDate = (i == 0 ? statusStartDate : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                if (i > 0 && endDate.equals(prevDate)) {
                    restMinutes += item.getTotalMinutes();
                } else {
                    restMinutes = item.getTotalMinutes();
                }

                if (restMinutes >= 10 * 60) {
                    startDate = Utility.addMinutes(statusStartDate, restMinutes);
                    setStartDate = true;
                    i++;
                    break;
                }

                // split logic
                if (item.getStatus() == 2) {
                    if (item.getTotalMinutes() >= 10 * 60 - sleeperMinutes && (item.getTotalMinutes() >= 8 * 60 || sleeperMinutes >= 8 * 60)) {
                        splitTime = endDate;
                        setStartDate = true;
                        startDate = splitTime;
                        i++;
                        break;
                    }
                }


                if (restMinutes > 2 * 60) {
                    sleeperMinutes = restMinutes;
                }
            }

            if (!setStartDate) {
                data = new ArrayList<>();
                for (int j = 0; j < listDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(listDutyStatus.get(j).getStartTime());
                    startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                    if (startTime.before(nextDay)) {
                        data.add(listDutyStatus.get(j));
                    }
                }
            } else {
                data = new ArrayList<>();
                for (int j = 0; j < listDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(listDutyStatus.get(j).getStartTime());
                    startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                    if (startTime.after(startDate) || startTime.equals(startDate)) {
                        data.add(listDutyStatus.get(j));
                    }
                }
            }

            Date s1Split = null;
            Date s2Split = null;
            sleeperMinutes = 0;
            restMinutes = 0;
            Collections.sort(data, DutyStatusBean.dateAsc);
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                Date endTime = Utility.parse(item.getEndTime());
                int status = item.getStatus();
                int totalMinutes = item.getTotalMinutes();

                if (Utility.dateOnlyGet(startTime).after(Utility.dateOnlyGet(Utility.format(currentDate, fullFormat)))) {
                    break;
                }

                // consective 8 hours rest logic (sleeper/ offduty)
                if (i > 0 && startTime.equals(Utility.parse(data.get(i - 1).getEndTime()))
                        && status <= 2 && data.get(i - 1).getStatus() <= 2) {
                    restMinutes += totalMinutes;
                } else if (status <= 2)
                    restMinutes = totalMinutes;

                if (restMinutes >= 10 * 60) {
                    drivingMinutes = 0;
                    elapsedMinutes = 0;
                    elapsedMinutes8 = 0;
                    restMinutes = 0;
                    continue;
                } else if (restMinutes >= 30 && status <= 2) {
                    elapsedMinutes8 = 0;
                } else {

                    elapsedMinutes8 += elapsedMinutes8 == -1 ? 0 : totalMinutes;
                }
                elapsedMinutes += elapsedMinutes == -1 ? 0 : totalMinutes;

                if (s2Split != null && s2Split == startTime) {
                    for (int k = 0; k < data.size(); k++) {
                        if (Utility.parse(data.get(k).getStartTime()).after(s1Split) && (Utility.parse(data.get(k).getStartTime()).before(s2Split)
                                || Utility.parse(data.get(k).getStartTime()).equals(s2Split))) {

                            if (data.get(k).getStatus() == 3 && Utility.parse(data.get(k).getStartTime()).before(s2Split)) {
                                drivingMinutes += data.get(k).getTotalMinutes();
                            }
                            elapsedMinutes += data.get(k).getTotalMinutes();
                        }
                    }

                    if (status == 2 && totalMinutes >= 8 * 60) {
                        elapsedMinutes -= totalMinutes;
                    }
                }

                if (ExcludeSleeperStartTime != null && startTime == ExcludeSleeperStartTime && status == 2 && totalMinutes >= 2 * 60 && excludeFg) {
                    elapsedMinutes -= totalMinutes;
                }

                // split logic
                if (status <= 2) {
                    if ((restMinutes > 2 * 60 && restMinutes <= 8 * 60) || (status == 2 && totalMinutes >= 8 * 60)) {
                        s1Split = startTime;
                        sleeperMinutes = status == 2 && totalMinutes >= 8 * 60 ? totalMinutes : restMinutes;

                        ArrayList<DutyStatusBean> s2List = new ArrayList<>();
                        for (int k = 0; k < data.size(); k++) {
                            if (Utility.parse(data.get(k).getStartTime()).after(startTime) && (status <= 2)) {
                                s2List.add(data.get(k));
                            }
                        }

                        Collections.sort(s2List, DutyStatusBean.dateAsc);
                        int s2Minutes = 0;
                        for (int k = 0; k < s2List.size(); k++) {
                            // consective 10 hours rest logic (sleeper/offduty)
                            if (k > 0 && Utility.parse(s2List.get(k).getStartTime()).equals(Utility.parse(s2List.get(k - 1).getEndTime()))) {
                                s2Minutes += s2List.get(k).getTotalMinutes();
                            } else
                                s2Minutes = s2List.get(k).getTotalMinutes();

                            if ((sleeperMinutes >= 60 && s2Minutes >= 2 * 60) || (s2List.get(k).getTotalMinutes() >= 8 * 60 && s2List.get(k).getStatus() == 2)) {
                                if (sleeperMinutes >= 8 * 60 && s2Minutes >= 2 * 60) {
                                    elapsedMinutes -= sleeperMinutes;
                                }
                                s2Split = Utility.parse(s2List.get(k).getStartTime());
                                break;
                            }
                        }
                    }
                }

                if (status == 3) {
                    drivingMinutes += drivingMinutes == -1 ? 0 : totalMinutes;
                    Date vStart;

                    if (drivingMinutes > 13 * 60) {
                        vStart = Utility.addMinutes(startTime, (13 * 60 - (drivingMinutes - totalMinutes)));
                        int duration = (int) ((endTime.getTime() - vStart.getTime()) / (1000 * 60));
                        ViolationAdd("11 Hrs 395.3(a)(3)(i) ***EGREGIOUS***", Utility.addMinutes(vStart, -2 * 60), duration - 2 * 60, false, "Must not drive beyond 11 hours without taking 10 consecutive hours off-duty", "US law permits you to only drive 11 hours after taking 10 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 10 consecutive hours off before driving is permitted.");
                      /*  if (Utility.addMinutes(vStart, -2 * 60).after(startTime) || Utility.addMinutes(vStart, -2 * 60).equals(startTime)) {

                            ViolationAdd("11 Hrs 395.3(a)(3)(i)", Utility.addMinutes(vStart, -2 * 60), duration - 2 * 60, false);
                        }*/
                        drivingMinutes = -1;
                    } else if (drivingMinutes > 11 * 60) {
                        vStart = Utility.addMinutes(startTime, (11 * 60 - (drivingMinutes - totalMinutes)));
                        int duration = (int) ((endTime.getTime() - vStart.getTime()) / (1000 * 60));
                        ViolationAdd("11 Hrs 395.3(a)(3)(i)", vStart, duration, false, "Must not drive beyond 11 hours without taking 10 consecutive hours off-duty", "US law permits you to only drive 11 hours after taking 10 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 10 consecutive hours off before driving is permitted.");

                    } else if (drivingMinutes == -1) {

                        ViolationAdd("11 Hrs 395.3(a)(3)(i) ***EGREGIOUS***", startTime, totalMinutes, false, "Must not drive beyond 11 hours without taking 10 consecutive hours off-duty", "US law permits you to only drive 11 hours after taking 10 consecutive hours off-duty or sleeper-berth or any combination of off-duty and/or sleeper-berth. You are required to take another 10 consecutive hours off before driving is permitted.");
                    }

                    if (elapsedMinutes8 > 8 * 60) {
                        vStart = Utility.addMinutes(startTime, (8 * 60 - (elapsedMinutes8 - totalMinutes)));
                        vStart = vStart.before(startTime) ? startTime : vStart;
                        int duration = (int) (endTime.getTime() - vStart.getTime()) / (1000 * 60);
                        ViolationAdd("Take 30 min Rest", vStart, duration, false, "Must not drive if more than 8 hours have passed since the end of last minimum of 30 minutes of off-duty or sleeper.", "US law only permits you to drive for a maximum of 8 hours since the last 30 minutes of off-duty or sleeper-berth time. You must take a minimum of 30 consecutive minute off-duty or sleeper-berth or any combination off-duty and sleeper-berth.");
                        elapsedMinutes8 = -1;

                    } else if (elapsedMinutes8 == -1) {
                        ViolationAdd("Take 30 min Rest", startTime, totalMinutes, false, "Must not drive if more than 8 hours have passed since the end of last minimum of 30 minutes of off-duty or sleeper.", "US law only permits you to drive for a maximum of 8 hours since the last 30 minutes of off-duty or sleeper-berth time. You must take a minimum of 30 consecutive minute off-duty or sleeper-berth or any combination off-duty and sleeper-berth."); //Missing 30 min Rest Break 395.3(a)(3)(ii)

                    }

                    if (elapsedMinutes > 14 * 60) {
                        vStart = Utility.addMinutes(startTime, (14 * 60 - (elapsedMinutes - totalMinutes)));
                        vStart = vStart.before(startTime) ? startTime : vStart;
                        int duration = (int) (endTime.getTime() - vStart.getTime()) / (1000 * 60);
                        ViolationAdd("14 Hrs 395.3(a)(2)", vStart, duration, false, "Must not drive after 14 hours following 10 consecutive hours off-duty ", "US law permits you to only have 14 hours of elapsed time after 10 consecutive hours off-duty or sleeper-berth or any combination off-duty and/or sleeper-berth. You are required to take another 10 consecutive hours off before driving is permitted. Drivers normally refer to this as their work-shift.");
                        elapsedMinutes = -1;

                    } else if (elapsedMinutes == -1) {

                        ViolationAdd("14 Hrs 395.3(a)(2)", startTime, totalMinutes, false, "Must not drive after 14 hours following 10 consecutive hours off-duty ", "US law permits you to only have 14 hours of elapsed time after 10 consecutive hours off-duty or sleeper-berth or any combination off-duty and/or sleeper-berth. You are required to take another 10 consecutive hours off before driving is permitted. Drivers normally refer to this as their work-shift.");
                    }
                }
            }

        } catch (Exception exe) {
        }
        return violations;
    }

    public static ArrayList<ViolationBean> Violation395_3B(String date) {
        try {
            Date currentDate = Utility.parse(date);
            Date nextDay = Utility.addDays(currentDate, 1);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet395_B(currentDate, listDutyStatus);
            int restMinutes = 0;
            int i = 0;
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                Date endTime = Utility.parse(item.getEndTime());

                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());
                Date prevDate = (i == 0 ? startTime : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                if (i > 0 && endTime.equals(prevDate)) {
                    restMinutes += item.getTotalMinutes();
                } else {
                    restMinutes = item.getTotalMinutes();
                }

                if (restMinutes >= 34 * 60) {
                    break;
                }
            }

            int onDutyMinutes = 0;
            if (restMinutes >= 34 * 60) {
                Date startDate = Utility.addMinutes(Utility.parse(data.get(i).getStartTime()), restMinutes);
                // calculate sum of the onduty status minutes onDutyMinutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);
            } else {
                Date startDate = Utility.addDays(currentDate, -7);
                // calculate sum of the onduty status minutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);

            }

            data = HourOfServiceDB.DutyStatusDrivingGet(currentDate, listDutyStatus); // get duty status logic

            for (DutyStatusBean item : data) {
                Date statusStartTime = Utility.parse(item.getStartTime());
                statusStartTime = statusStartTime.before(currentDate) ? currentDate : statusStartTime;

                statusStartTime = Utility.addSeconds(statusStartTime, -statusStartTime.getSeconds());

                Date statusEndTime = Utility.parse(item.getEndTime());
                statusEndTime = statusEndTime.after(nextDay.after(Utility.newDate()) ? Utility.newDate() : nextDay) ? (nextDay.after(Utility.newDate()) ? Utility.newDate() : nextDay) : statusEndTime;

                statusEndTime = Utility.addSeconds(statusEndTime, -statusEndTime.getSeconds());
                int totalMinutes = (int) (statusEndTime.getTime() - statusStartTime.getTime()) / (1000 * 60);
                if (item.getStatus() == 3) {
                    if (onDutyMinutes + totalMinutes > 70 * 60) {
                        Date vStart = Utility.addMinutes(statusStartTime, (70 * 60 - onDutyMinutes));
                        vStart = vStart.before(statusStartTime) ? statusStartTime : vStart;
                        int duration = (int) (statusEndTime.getTime() - vStart.getTime()) / (1000 * 60);
                        ViolationAdd("70 Hrs 395.3(b)(2)", vStart, duration, false, "Must not drive after 70 hours on-duty within 8 days.", "US law does not permit you to drive after 70 hours on-duty time in any 8 days period. You can take 34 hours off-duty or sleeper-berth or any such combination to reset or you can wait until the 9th day to gain the hours back. ");
                        onDutyMinutes = -1;
                    } else if (onDutyMinutes == -1) {
                        ViolationAdd("70 Hrs 395.3(b)(2)", statusStartTime, totalMinutes, false, "Must not drive after 70 hours on-duty within 8 days.", "US law does not permit you to drive after 70 hours on-duty time in any 8 days period. You can take 34 hours off-duty or sleeper-berth or any such combination to reset or you can wait until the 9th day to gain the hours back. ");
                    }
                }

            }

        } catch (Exception exe) {
        }
        return violations;
    }

    public static ArrayList<ViolationBean> TeamOffDutyUSA(String logDate) {
        try {
            Date currentDate = Utility.parse(logDate);
            Date nextDay = Utility.addDays(currentDate, 1);
            ArrayList<DutyStatusBean> coDriverDutyStatus = HourOfServiceDB.DutyStatusGet15Days(currentDate, coDriverIds, true);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusGet(currentDate, coDriverDutyStatus, 3);

            for (int i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date statusStartTime = Utility.parse(item.getStartTime());
                statusStartTime = statusStartTime.before(currentDate) ? currentDate : statusStartTime;
                Date statusEndTime = Utility.parse(item.getEndTime());
                statusEndTime = statusEndTime.after(nextDay) ? nextDay : statusEndTime;
                int totalMinutes = (int) (statusEndTime.getTime() - statusStartTime.getTime()) / (1000 * 60);
                ArrayList<DutyStatusBean> coDriverDutyStatusList = new ArrayList<>();
                for (int j = 0; j < coDriverDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(coDriverDutyStatus.get(i).getStartTime());
                    Date endTime = Utility.parse(coDriverDutyStatus.get(i).getEndTime());

                    if ((((startTime.before(statusStartTime) || startTime.equals(statusStartTime)) && (endTime.after(statusStartTime) || endTime.equals(statusStartTime)))
                            || ((startTime.before(statusEndTime) || startTime.equals(statusEndTime)) && (endTime.after(statusEndTime) || endTime.equals(statusEndTime)))
                            || ((startTime.after(statusStartTime) || startTime.equals(statusStartTime)) && (endTime.before(statusEndTime) || endTime.equals(statusEndTime)))
                    )
                            && startTime.before(nextDay) && coDriverDutyStatus.get(i).getStatus() == 1) {
                        data.add(coDriverDutyStatus.get(i));
                    }
                }

                for (DutyStatusBean cdOffDuty : coDriverDutyStatusList) {
                    Date offDutyStart = Utility.parse(cdOffDuty.getStartTime());
                    Date offDutyEnd = Utility.parse(cdOffDuty.getEndTime());
                    DutyStatusBean sleeperBefore = null, sleeperAfter = null, offDutyCheck = null;
                    for (DutyStatusBean bean : coDriverDutyStatus) {
                        Date endTime = Utility.parse(bean.getEndTime());
                        if (endTime.equals(offDutyStart) && bean.getStatus() == 2 && bean.getTotalMinutes() >= 8 * 60) {
                            sleeperBefore = bean;
                            break;
                        }

                    }

                    for (DutyStatusBean bean : coDriverDutyStatus) {
                        Date startTime = Utility.parse(bean.getStartTime());
                        if (startTime.equals(offDutyEnd) && bean.getStatus() == 2 && bean.getTotalMinutes() >= 8 * 60) {
                            sleeperAfter = bean;
                            break;
                        }

                    }

                    if (sleeperBefore != null) {
                        for (DutyStatusBean bean : coDriverDutyStatus) {
                            Date endTime = Utility.parse(bean.getEndTime());
                            if (endTime.equals(Utility.parse(sleeperBefore.getStartTime())) && bean.getStatus() == 1) {
                                offDutyCheck = bean;
                                break;
                            }

                        }
                        if (offDutyCheck != null) {
                            if (offDutyCheck.getTotalMinutes() < 2 * 60) {
                                offDutyStart = Utility.addMinutes(Utility.parse(cdOffDuty.getStartTime()), (2 * 60 - offDutyCheck.getTotalMinutes()));
                            }
                        } else {
                            if (cdOffDuty.getTotalMinutes() > 2 * 60) {
                                offDutyStart = Utility.addMinutes(Utility.parse(cdOffDuty.getStartTime()), 2 * 60);
                            } else
                                continue;
                        }
                    }

                    if (sleeperAfter != null) {
                        if (cdOffDuty.getTotalMinutes() > 2 * 60) {
                            offDutyEnd = Utility.addMinutes(Utility.parse(cdOffDuty.getEndTime()), 2 * 60);
                        } else
                            continue;
                    }

                    statusStartTime = statusStartTime.before(offDutyStart) ? offDutyStart : statusStartTime;
                    statusEndTime = statusEndTime.after(offDutyEnd) ? offDutyEnd : statusEndTime;

                    ArrayList<DutyStatusBean> cdStatus = new ArrayList<>();
                    DutyStatusBean cdLastStatus = null;

                    for (DutyStatusBean bean : coDriverDutyStatus) {
                        Date startTime = Utility.parse(bean.getStartTime());
                        if (startTime.before(nextDay) && (statusStartTime.after(offDutyStart) || statusStartTime.equals(offDutyStart)) && startTime.before(offDutyEnd)) {
                            cdStatus.add(bean);
                        }

                    }
                    Collections.sort(cdStatus, DutyStatusBean.dateAsc);

                    for (DutyStatusBean bean : coDriverDutyStatus) {
                        Date startTime = Utility.parse(bean.getStartTime());
                        if (startTime.before(offDutyStart)) {
                            cdLastStatus = bean;
                            break;
                        }

                    }

                    if (cdLastStatus != null && cdLastStatus.isStatusFg()) {
                        cdStatus.add(cdLastStatus);
                        Collections.sort(cdStatus, DutyStatusBean.dateAsc);

                    }
                    for (int k = 0; k < cdStatus.size(); k++) {
                        Date startTime = Utility.parse(cdStatus.get(k).getStartTime());
                        if (cdStatus.get(k).isStatusFg()) {
                            statusStartTime = statusStartTime.before(startTime) ? startTime : statusStartTime;
                            if (k + 1 < cdStatus.size()) {
                                k++;
                                statusEndTime = statusEndTime.after(startTime) ? startTime : statusEndTime;
                            }
                            totalMinutes = (int) (statusEndTime.getTime() - statusStartTime.getTime()) / (1000 * 60);
                            ViolationAdd("Team Off Duty USA", startTime, totalMinutes, false, "", "");
                        }
                    }
                }
            }
        } catch (Exception exe) {
        }
        return violations;
    }

    /**
     * *************************************************Hours of summary**************************************************************************************
     */
    public static int CanadaHours70(String logDate) {
        int onDutyMinutes = 0;
        try {
            Date currentDate = Utility.parse(logDate);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet26(currentDate, listDutyStatus);
            int restMinutes = 0;
            int i = 0;
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                Date endTime = Utility.parse(item.getEndTime());
                endTime = endTime.after(currentDate) ? currentDate : endTime;
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                int totalMinutes = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);

                Date prevDate = (i == 0 ? startTime : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                if (i > 0 && endTime.equals(prevDate)) {
                    restMinutes += totalMinutes;
                } else
                    restMinutes = totalMinutes;

                if (restMinutes >= 36 * 60)
                    break;
            }

            if (restMinutes >= 36 * 60) {
                Date startDate = Utility.addMinutes(Utility.parse(data.get(i).getStartTime()), restMinutes);
                // calculate sum of the onduty status minutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);

            } else {
                Date startDate = Utility.addDays(currentDate, -6);
                // calculate sum of the onduty status minutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);

            }

            if (onDutyMinutes < 0) {
                onDutyMinutes = 70 * 60;
            }

        } catch (Exception exe) {
        }
        return (70 * 60 - onDutyMinutes);
    }

    public static int CanadaHours120(String logDate) {
        int onDutyMinutes = 0;
        ArrayList<DutyStatusBean> data = new ArrayList<DutyStatusBean>();
        try {
            Date currentDate = Utility.parse(logDate);
            Date endDate = Utility.addDays(currentDate, -13);
            Date nextDay = Utility.addDays(currentDate, 1);

            for (int i = 0; i < listDutyStatus.size(); i++) {
                Date startDate = Utility.parse(listDutyStatus.get(i).getStartTime());
                Date endTime = Utility.parse(listDutyStatus.get(i).getEndTime());

                if ((startDate.before(nextDay) && endTime.before(nextDay) && startDate.after(endDate) && listDutyStatus.get(i).getStatus() <= 2)) {
                    data.add(listDutyStatus.get(i));
                }

                if (startDate.before(endDate)) {
                    data.add(listDutyStatus.get(i));
                    break;
                }
            }
            Collections.sort(data, DutyStatusBean.dateDesc);
            int i = 0, restMinutes = 0;
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                Date endTime = Utility.parse(item.getEndTime());
                endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                int totalTime = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);

                Date prevDate = (i == 0 ? startTime : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                if (i > 0 && endTime.equals(prevDate)) {
                    restMinutes += totalTime;
                } else
                    restMinutes = totalTime;

                if (restMinutes >= 72 * 60)
                    break;

            }

            if (restMinutes >= 72 * 60) {
                Date startDate = Utility.addMinutes(Utility.parse(data.get(i).getStartTime()), restMinutes);
                // calculate sum of the onduty status minutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);

            } else {
                Date startDate = Utility.addDays(currentDate, -13);
                // calculate sum of the onduty status minutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);

            }

            if (onDutyMinutes < 0) {
                onDutyMinutes = 120 * 60;
            }

        } catch (Exception exe) {

        }
        return (120 * 60 - onDutyMinutes);
    }

    public static ViolationBean CanadaHoursSummary(String logDate, boolean excludeFg) {
        int restMinutes = 0;
        int sleeperMinutes = 0;

        int drivingMinutes = 0;
        int onDutyMinutes = 0;
        int elapsedHours = 0;
        ViolationBean bean = new ViolationBean();
        try {
            Date currentDate = Utility.parse(logDate);
            Date nextDay = Utility.addDays(currentDate, 1);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet(currentDate, listDutyStatus);
            int i = 0;
            boolean teamFg = false;
            Date startDate = null;
            boolean setStartDate = false;

            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                Date endTime = Utility.parse(item.getEndTime());
                endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                int totalMinutes = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);

                Date prevDate = (i == 0 ? startTime : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                if (i > 0 && endTime.equals(prevDate)) {
                    restMinutes += totalMinutes;
                } else
                    restMinutes = totalMinutes;

                if (restMinutes >= 8 * 60) {
                    setStartDate = true;
                    startDate = Utility.addMinutes(startTime, restMinutes);
                    i++;
                    break;
                }
                //split logic
                if (item.getStatus() == 2 && !deferedFg) {
                    if (totalMinutes > (teamFg ? 10 : 8) * 60 - sleeperMinutes) {
                        setStartDate = true;
                        startDate = endTime;
                        i++;
                        break;
                    }

                    DutyStatusBean coDriverCurrentStatus = null;
                    for (int j = 0; j < listCoDriverDutyStatus.size(); j++) {
                        if (Utility.parse(listCoDriverDutyStatus.get(j).getStartTime()).before(startTime)) {
                            coDriverCurrentStatus = listCoDriverDutyStatus.get(j);
                            break;
                        }
                    }
                    teamFg = (coDriverCurrentStatus != null && coDriverCurrentStatus.isStatusFg());
                    if (totalMinutes > (teamFg ? 4 : 2) * 60) {
                        sleeperMinutes = item.getTotalMinutes();
                    }
                }

            }

            if (i == 0)
                i++;
            restMinutes = sleeperMinutes = 0;

            if (!setStartDate) {
                data = new ArrayList<>();
                for (int j = 0; j < listDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(listDutyStatus.get(j).getStartTime());
                    startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                    if (startTime.before(nextDay)) {
                        data.add(listDutyStatus.get(j));
                    }
                }
            } else {
                data = new ArrayList<>();
                for (int j = 0; j < listDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(listDutyStatus.get(j).getStartTime());
                    startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                    if (startTime.after(startDate) || startTime.equals(startDate)) {
                        data.add(listDutyStatus.get(j));
                    }
                }
            }


            Collections.sort(data, DutyStatusBean.dateAsc);


            Date s2Split = null;
            Date s1Split = null;

            for (int j = 0; j < data.size(); j++) {
                DutyStatusBean item = data.get(j);
                Date startTime = Utility.parse(item.getStartTime());
                startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                Date endTime = Utility.parse(item.getEndTime());
                endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                int totalMinutes = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);
                int status = item.getStatus();
                if (Utility.dateOnlyGet(startTime).after(Utility.dateOnlyGet(currentDate))) {
                    break;
                }

                Date prevDate = j == 0 ? endTime : Utility.parse(data.get(j - 1).getEndTime());
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());
                // consective 8 hours rest logic (sleeper/ offduty)
                if (j > 0 && startTime.equals(prevDate)
                        && status <= 2 && data.get(j - 1).getStatus() <= 2) {
                    restMinutes += totalMinutes;
                } else if (status <= 2)
                    restMinutes = totalMinutes;

                if (restMinutes >= 8 * 60) {
                    if (j < data.size() - 1 && data.get(j + 1).getStatus() <= 2) {
                        continue;
                    }

                    drivingMinutes = 0;
                    onDutyMinutes = 0;
                    elapsedHours = 0;
                    restMinutes = 0;
                    continue;
                }

                if (s2Split != null && s2Split == startTime && !deferedFg) {
                    for (int k = 0; k < data.size(); k++) {
                        if (Utility.parse(data.get(k).getStartTime()).after(s1Split) && Utility.parse(data.get(k).getStartTime()).before(s2Split)) {


                            if (data.get(k).getStatus() >= 3) {
                                if (data.get(k).getStatus() == 3) {
                                    drivingMinutes += data.get(k).getTotalMinutes();
                                }
                                onDutyMinutes += data.get(k).getTotalMinutes();
                            }
                            elapsedHours += data.get(k).getTotalMinutes();
                        }
                    }
                }

                drivingMinutes += status == 3 && drivingMinutes != -1 ? totalMinutes : 0;
                onDutyMinutes += status >= 3 && onDutyMinutes != -1 ? totalMinutes : 0;
                elapsedHours += elapsedHours == -1 ? 0 : totalMinutes;

                if (ExcludeSleeperStartTime != null && startTime.equals(ExcludeSleeperStartTime) && status == 2 && totalMinutes >= 2 * 60 && excludeFg) {
                    elapsedHours -= totalMinutes;
                }

                //split logic
                if (status == 2 && totalMinutes >= 2 * 60 && !deferedFg) {
                    sleeperMinutes = totalMinutes;
                    ArrayList<DutyStatusBean> s2List = new ArrayList<>();
                    for (int k = 0; k < data.size(); k++) {
                        if (Utility.parse(data.get(k).getStartTime()).after(startTime) && ((status == 1 && totalMinutes > 8 * 60) || status == 2)) {
                            s2List.add(data.get(k));
                        }
                    }

                    Collections.sort(s2List, DutyStatusBean.dateAsc);

                    int k = 0;
                    for (k = 0; k < s2List.size(); k++) {
                        if (s2List.get(k).getStatus() == 1)
                            break;

                        DutyStatusBean codriverBeforess2 = null;
                        Collections.sort(listCoDriverDutyStatus, DutyStatusBean.dateDesc);
                        for (int l = 0; l < listCoDriverDutyStatus.size(); l++) {
                            if (Utility.parse(listCoDriverDutyStatus.get(l).getStartTime()).before(Utility.parse(s2List.get(k).getEndTime()))) {
                                codriverBeforess2 = listCoDriverDutyStatus.get(l);
                                break;
                            }
                        }
                        boolean cdStatus = (codriverBeforess2 != null && codriverBeforess2.isStatusFg());

                        if (cdStatus) {
                            if (sleeperMinutes >= 4 * 60 && s2List.get(k).getTotalMinutes() >= 4 * 60) {
                                if ((sleeperMinutes + s2List.get(k).getTotalMinutes()) == 8 * 60) {
                                    s1Split = startTime;
                                    s2Split = Utility.parse(s2List.get(k).getStartTime());
                                    for (int l = 0; l < data.size(); l++) {
                                        if (Utility.parse(data.get(l).getStartTime()).after(startTime) && Utility.parse(data.get(l).getStartTime()).before(s2Split) && data.get(l).getStatus() == 2
                                                && data.get(l).getTotalMinutes() >= 8 * 60 - s2List.get(k).getTotalMinutes() && data.get(l).getTotalMinutes() >= 4 * 60) {
                                            s2List.set(k, data.get(l));
                                            break;
                                        }
                                    }

                                    if (s2List.get(k) == null) {
                                        if (elapsedHours != -1) {
                                            elapsedHours -= totalMinutes;
                                        }
                                        break;
                                    } else {
                                        s2Split = null;
                                    }
                                }
                            }
                        } else {
                            if (sleeperMinutes >= 2 * 60) {
                                if ((sleeperMinutes + s2List.get(k).getTotalMinutes()) >= 10 * 60) {
                                    s1Split = startTime;
                                    s2Split = Utility.parse(s2List.get(k).getStartTime());

                                    for (int l = 0; l < data.size(); l++) {
                                        if (Utility.parse(data.get(l).getStartTime()).after(startTime) && Utility.parse(data.get(l).getStartTime()).before(s2Split) && data.get(l).getStatus() == 2
                                                && data.get(l).getTotalMinutes() >= 10 * 60 - s2List.get(k).getTotalMinutes() && data.get(l).getTotalMinutes() >= 2 * 60) {
                                            s2List.set(k, data.get(l));
                                            break;
                                        }
                                    }

                                    if (s2List.get(k) == null) {
                                        if (elapsedHours != -1) {
                                            elapsedHours -= totalMinutes;
                                        }
                                        break;
                                    } else {
                                        s2Split = null;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            drivingMinutes = (13 * 60) - drivingMinutes;
            drivingMinutes = drivingMinutes < 0 ? 0 : drivingMinutes;

            onDutyMinutes = (14 * 60) - onDutyMinutes;
            onDutyMinutes = onDutyMinutes < 0 ? 0 : onDutyMinutes;

            elapsedHours = (16 * 60) - elapsedHours;
            elapsedHours = elapsedHours < 0 ? 0 : elapsedHours;

            bean.setTimeLeft13(drivingMinutes);
            bean.setTimeLeft14(onDutyMinutes);
            bean.setTimeLeft16(elapsedHours);

        } catch (Exception exe) {
        }
        return bean;
    }

    public static ViolationBean USHoursSummaryGet(String logDate, boolean excludeFg) {
        int restMinutes = 0;
        int sleeperMinutes = 0;

        int drivingMinutes = 0;
        int elapsedMinutes = 0;
        int elapsedMinutes8 = 0;

        ViolationBean bean = new ViolationBean();
        try {
            Date currentDate = Utility.parse(logDate);
            Date nextDay = Utility.addDays(currentDate, 1);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet(currentDate, listDutyStatus);
            int i = 0;
            Date startDate = null;
            boolean setStartDate = false;
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                Date endTime = Utility.parse(item.getEndTime());
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                Date prevDate = (i == 0 ? startTime : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());
                if (i > 0 && endTime.equals(prevDate)) {
                    restMinutes += item.getTotalMinutes();
                } else {
                    restMinutes = item.getTotalMinutes();
                }

                if (restMinutes >= 10 * 60) {
                    startDate = Utility.addMinutes(startTime, restMinutes);
                    setStartDate = true;
                    i++;
                    break;
                }

                // split logic
                if (item.getStatus() == 2) {
                    if (item.getTotalMinutes() >= 10 * 60 - sleeperMinutes && (item.getTotalMinutes() >= 8 * 60 || sleeperMinutes >= 8 * 60)) {
                        splitTime = endTime;
                        setStartDate = true;
                        startDate = splitTime;
                        i++;
                        break;
                    }
                }


                if (restMinutes > 2 * 60) {
                    sleeperMinutes = restMinutes;
                }
            }

            if (!setStartDate) {
                data = new ArrayList<>();
                for (int j = 0; j < listDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(listDutyStatus.get(j).getStartTime());
                    startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                    if (startTime.before(nextDay)) {
                        data.add(listDutyStatus.get(j));
                    }
                }
            } else {
                data = new ArrayList<>();
                for (int j = 0; j < listDutyStatus.size(); j++) {
                    Date startTime = Utility.parse(listDutyStatus.get(j).getStartTime());

                    startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                    if (startTime.after(startDate) || startTime.equals(startDate)) {
                        data.add(listDutyStatus.get(j));
                    }
                }
            }

            Date s1Split = null;
            Date s2Split = null;
            sleeperMinutes = 0;
            restMinutes = 0;
            Collections.sort(data, DutyStatusBean.dateAsc);

            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                Date endTime = Utility.parse(item.getEndTime());
                endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                int totalMinutes = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);

                int status = item.getStatus();

                if (Utility.dateOnlyGet(startTime).after(Utility.dateOnlyGet(currentDate))) {
                    break;
                }

                Date prevDate = i == 0 ? startTime : Utility.parse(data.get(i - 1).getEndTime());
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                // consective 8 hours rest logic (sleeper/ offduty)
                if (i > 0 && startTime.equals(prevDate)
                        && status <= 2 && data.get(i - 1).getStatus() <= 2) {
                    restMinutes += totalMinutes;
                } else if (status <= 2)
                    restMinutes = totalMinutes;

                if (restMinutes >= 10 * 60) {
                    drivingMinutes = 0;
                    elapsedMinutes = 0;
                    elapsedMinutes8 = 0;
                    restMinutes = 0;
                    continue;
                } else if (restMinutes >= 30 && status <= 2) {
                    elapsedMinutes8 = 0;
                } else {

                    elapsedMinutes8 += elapsedMinutes8 == -1 ? 0 : totalMinutes;
                }
                elapsedMinutes += elapsedMinutes == -1 ? 0 : totalMinutes;

                if (s2Split != null && s2Split == startTime) {
                    for (int k = 0; k < data.size(); k++) {
                        if (Utility.parse(data.get(k).getStartTime()).after(s1Split) && (Utility.parse(data.get(k).getStartTime()).before(s2Split)
                                || Utility.parse(data.get(k).getStartTime()).equals(s2Split))) {

                            if (data.get(k).getStatus() == 3 && Utility.parse(data.get(k).getStartTime()).before(s2Split)) {
                                drivingMinutes += data.get(k).getTotalMinutes();
                            }
                            elapsedMinutes += data.get(k).getTotalMinutes();
                        }
                    }

                    if (status == 2 && totalMinutes >= 8 * 60) {
                        elapsedMinutes -= totalMinutes;
                    }
                }

                if (ExcludeSleeperStartTime != null && startTime == ExcludeSleeperStartTime && status == 2 && totalMinutes >= 2 * 60 && excludeFg) {
                    elapsedMinutes -= totalMinutes;
                }

                // split logic
                if (status <= 2) {
                    if ((restMinutes > 2 * 60 && restMinutes <= 8 * 60) || (status == 2 && totalMinutes >= 8 * 60)) {
                        s1Split = startTime;
                        sleeperMinutes = status == 2 && totalMinutes >= 8 * 60 ? totalMinutes : restMinutes;

                        ArrayList<DutyStatusBean> s2List = new ArrayList<>();
                        for (int k = 0; k < data.size(); k++) {
                            if (Utility.parse(data.get(k).getStartTime()).after(startTime) && (status <= 2)) {
                                s2List.add(data.get(k));
                            }
                        }

                        Collections.sort(s2List, DutyStatusBean.dateAsc);
                        int s2Minutes = 0;
                        for (int k = 0; k < s2List.size(); k++) {

                            startTime = Utility.parse(s2List.get(k).getStartTime());
                            startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                            prevDate = (k == 0 ? startTime : Utility.parse(s2List.get(k - 1).getEndTime()));
                            prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());
                            // consective 10 hours rest logic (sleeper/offduty)
                            if (k > 0 && startTime.equals(prevDate)) {
                                s2Minutes += s2List.get(k).getTotalMinutes();
                            } else
                                s2Minutes = s2List.get(k).getTotalMinutes();

                            if ((sleeperMinutes >= 60 && s2Minutes >= 2 * 60) || (s2List.get(k).getTotalMinutes() >= 8 * 60 && s2List.get(k).getStatus() == 2)) {
                                if (sleeperMinutes >= 8 * 60 && s2Minutes >= 2 * 60) {
                                    elapsedMinutes -= sleeperMinutes;
                                }
                                s2Split = Utility.parse(s2List.get(k).getStartTime());
                                break;
                            }
                        }
                    }
                }

                if (status == 3) {
                    drivingMinutes += drivingMinutes == -1 ? 0 : totalMinutes;
                }
            }
            drivingMinutes = (11 * 60) - drivingMinutes;
            drivingMinutes = drivingMinutes < 0 ? 0 : drivingMinutes;

            elapsedMinutes = (14 * 60) - elapsedMinutes;
            elapsedMinutes = elapsedMinutes < 0 ? 0 : elapsedMinutes;
            bean.setTimeLeft11(drivingMinutes);
            bean.setTimeLeft14US(elapsedMinutes);

        } catch (Exception exe) {
        }
        return bean;
    }

    public static int US70HoursGet(String logDate) {
        int onDutyMinutes = 0;
        try {
            Date currentDate = Utility.parse(logDate);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet395_B(currentDate, listDutyStatus);
            int restMinutes = 0;
            int i = 0;
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                Date endTime = Utility.parse(item.getEndTime());
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                Date prevDate = (i == 0 ? startTime : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());

                if (i > 0 && endTime.equals(prevDate)) {
                    restMinutes += item.getTotalMinutes();
                } else {
                    restMinutes = item.getTotalMinutes();
                }

                if (restMinutes >= 34 * 60) {
                    break;
                }
            }

            if (restMinutes >= 34 * 60) {
                Date startDate = Utility.addMinutes(Utility.parse(data.get(i).getStartTime()), restMinutes);
                startDate = Utility.addSeconds(startDate, -startDate.getSeconds());
                // calculate sum of the onduty status minutes onDutyMinutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);
            } else {
                Date startDate = Utility.addDays(currentDate, -7);
                startDate = Utility.addSeconds(startDate, -startDate.getSeconds());
                // calculate sum of the onduty status minutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);
            }

            if (onDutyMinutes < 0)
                onDutyMinutes = 70 * 60;

        } catch (Exception exe) {
        }
        return (70 * 60 - onDutyMinutes);
    }

    public static int US60HoursGet(String logDate) {
        int onDutyMinutes = 0;
        try {
            Date currentDate = Utility.parse(logDate);
            ArrayList<DutyStatusBean> data = HourOfServiceDB.DutyStatusOffDutyGet395_B(currentDate, listDutyStatus);
            int restMinutes = 0;
            int i = 0;
            for (i = 0; i < data.size(); i++) {
                DutyStatusBean item = data.get(i);
                Date startTime = Utility.parse(item.getStartTime());
                Date endTime = Utility.parse(item.getEndTime());
                endTime = Utility.addSeconds(endTime, -endTime.getSeconds());

                Date prevDate = (i == 0 ? startTime : Utility.parse(data.get(i - 1).getStartTime()));
                prevDate = Utility.addSeconds(prevDate, -prevDate.getSeconds());
                if (i > 0 && endTime.equals(prevDate)) {
                    restMinutes += item.getTotalMinutes();
                } else {
                    restMinutes = item.getTotalMinutes();
                }

                if (restMinutes >= 34 * 60) {
                    break;
                }
            }

            if (restMinutes >= 34 * 60) {
                Date startDate = Utility.addMinutes(Utility.parse(data.get(i).getStartTime()), restMinutes);
                // calculate sum of the onduty status minutes onDutyMinutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);
            } else {
                Date startDate = Utility.addDays(currentDate, -6);
                // calculate sum of the onduty status minutes
                onDutyMinutes = HourOfServiceDB.DutyStatusOnDutyMinuteGet26(startDate, currentDate, listDutyStatus);
            }

            if (onDutyMinutes < 0)
                onDutyMinutes = 60 * 60;

        } catch (Exception exe) {
        }
        return (60 * 60 - onDutyMinutes);
    }

    public interface IViolation {
        void onUpdateViolation(boolean status);
    }
}
