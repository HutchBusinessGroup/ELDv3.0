package com.hutchgroup.elog.beans;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class CarrierInfoBean {
    int companyId, statusId, vehicleId;
    String CarrierName;
    String ELDManufacturer;
    String USDOT;
    String UnitNo;
    String PlateNo;
    String VIN;
    String MACAddress;

    public String getSerailNo() {
        return SerailNo;
    }

    public void setSerailNo(String serailNo) {
        SerailNo = serailNo;
    }

    String SerailNo;

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getCarrierName() {
        return CarrierName;
    }

    public void setCarrierName(String carrierName) {
        CarrierName = carrierName;
    }

    public String getELDManufacturer() {
        return ELDManufacturer;
    }

    public void setELDManufacturer(String ELDManufacturer) {
        this.ELDManufacturer = ELDManufacturer;
    }

    public String getUSDOT() {
        return USDOT;
    }

    public void setUSDOT(String USDOT) {
        this.USDOT = USDOT;
    }

    public String getUnitNo() {
        return UnitNo;
    }

    public void setUnitNo(String unitNo) {
        UnitNo = unitNo;
    }

    public String getVIN() {
        return VIN;
    }

    public void setVIN(String VIN) {
        this.VIN = VIN;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    public String getPlateNo() {
        return PlateNo;
    }

    public void setPlateNo(String plateNo) {
        PlateNo = plateNo;
    }
}
