package com.hutchgroup.elog.beans;

import com.hutchgroup.elog.common.Utility;

import java.util.Comparator;

public class UserBean {
    int accountId, accountType, exemptELDUseFg, statusId;

    String firstName;
    String lastName;
    String emailId;
    String mobileNo;
    String drivingLicense, dlIssueState;
    String userName = "";
    String password;
    String salt;
    String exemptionRemark;
    String specialCategory, TimeZoneOffsetUTC, LicenseExpiryDate;
    int CurrentRule, LicenseAcceptFg;
    String DotPassword;
    int unreadCount;

    public String getMessageDateTime() {
        return messageDateTime;
    }

    public void setMessageDateTime(String messageDateTime) {
        this.messageDateTime = messageDateTime;
    }

    String messageDateTime;

    public boolean isFirstLoginFg() {
        return firstLoginFg;
    }

    public void setFirstLoginFg(boolean firstLoginFg) {
        this.firstLoginFg = firstLoginFg;
    }

    boolean firstLoginFg;

    public int getReadFg() {
        return readFg;
    }

    public void setReadFg(int readFg) {
        this.readFg = readFg;
    }

    int readFg;

    public String getCurrentMessage() {
        return currentMessage;
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    String currentMessage;
    boolean isOnline;

    boolean isActive;

    public boolean isOnScreenFg() {
        return onScreenFg;
    }

    public void setOnScreenFg(boolean onScreenFg) {
        this.onScreenFg = onScreenFg;
    }

    boolean onScreenFg;


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public String getDlIssueState() {
        return dlIssueState;
    }

    public void setDlIssueState(String dlIssueState) {
        this.dlIssueState = dlIssueState;
    }

    public int getExemptELDUseFg() {
        return exemptELDUseFg;
    }

    public void setExemptELDUseFg(int exemptELDUseFg) {
        this.exemptELDUseFg = exemptELDUseFg;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(String drivingLicense) {
        this.drivingLicense = drivingLicense;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getExemptionRemark() {
        return exemptionRemark;
    }

    public void setExemptionRemark(String exemptionRemark) {
        this.exemptionRemark = exemptionRemark;
    }

    public String getSpecialCategory() {
        return specialCategory;
    }

    public void setSpecialCategory(String specialCategory) {
        this.specialCategory = specialCategory;
    }

    public int getCurrentRule() {
        return CurrentRule;
    }

    public void setCurrentRule(int currentRule) {
        CurrentRule = currentRule;
    }

    public String getTimeZoneOffsetUTC() {
        return TimeZoneOffsetUTC;
    }

    public void setTimeZoneOffsetUTC(String timeZoneOffsetUTC) {
        TimeZoneOffsetUTC = timeZoneOffsetUTC;
    }

    public int getLicenseAcceptFg() {
        return LicenseAcceptFg;
    }

    public void setLicenseAcceptFg(int licenseAcceptFg) {
        LicenseAcceptFg = licenseAcceptFg;
    }

    public String getLicenseExpiryDate() {
        return LicenseExpiryDate;
    }

    public void setLicenseExpiryDate(String licenseExpiryDate) {
        LicenseExpiryDate = licenseExpiryDate;
    }

    public String getDotPassword() {
        return DotPassword;
    }

    public void setDotPassword(String dotPassword) {
        DotPassword = dotPassword;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /*Comparator for sorting the list by dateDesc*/
    public static Comparator<UserBean> dateDesc = new Comparator<UserBean>() {
        public int compare(UserBean s1, UserBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (Utility.parse(s1.getMessageDateTime() == null ? "1970-01-01 00:00:00" : s1.getMessageDateTime()).getTime() / 1000);
                date2 = (int) (Utility.parse(s2.getMessageDateTime() == null ? "1970-01-01 00:00:00" : s2.getMessageDateTime()).getTime() / 1000);

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };
}
