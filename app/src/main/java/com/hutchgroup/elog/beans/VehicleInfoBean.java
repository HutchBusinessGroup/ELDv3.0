package com.hutchgroup.elog.beans;

/**
 * Created by SAMSUNG on 16-01-2017.
 */

public class VehicleInfoBean {
    int _Id;
    String OdometerReading = "0";
    String Speed = "0";
    String RPM = "0";
    String Average = "0";
    String EngineHour = "0";
    String FuelUsed = "0";
    String IdleFuelUsed = "0";
    String IdleHours = "0";
    String Boost = "0";
    String CoolantTemperature = "0";
    String CoolantLevel = "0";
    String BatteryVoltage = "0";
    String WasherFluidLevel = "0";
    String EngineLoad = "0";
    String EngineOilLevel = "0";
    String CruiseSpeed = "0";
    String MaxRoadSpeed = "0";
    String AirSuspension = "0";
    String TransmissionOilLevel = "0";
    String DEFTankLevel = "0";
    String DEFTankLevelLow = "0";
    String EngineSerialNo = "0";
    String EngineRatePower = "0";
    String PTOFuelUsed = "0";
    String CreatedDate;

    String FuelPressure = "0";
    String AirInletTemperature = "0";
    String BarometricPressure = "0";
    String EngineOilPressure = "0";

    String CuriseTime = "0";
    String PTOHours = "0";


    int CruiseSetFg;
    int PowerUnitABSFg;
    int TrailerABSFg;
    int DerateFg;
    int BrakeApplication;
    int RegenerationRequiredFg;
    int WaterInFuelFg;
    int PTOEngagementFg;
    int SeatBeltFg;
    int TransmissionGear;
    int ActiveDTCFg;
    int InActiveDTCFg;
    int TPMSWarningFg;
    int FuelLevel;
    int SyncFg;

    public int getFuelLevel() {
        return FuelLevel;
    }

    public void setFuelLevel(int fuelLevel) {
        FuelLevel = fuelLevel;
    }

    public int get_Id() {
        return _Id;
    }

    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    public String getOdometerReading() {
        return OdometerReading;
    }

    public void setOdometerReading(String odometerReading) {
        OdometerReading = odometerReading;
    }

    public String getSpeed() {
        return Speed;
    }

    public void setSpeed(String speed) {
        Speed = speed;
    }

    public String getRPM() {
        return RPM;
    }

    public void setRPM(String RPM) {
        this.RPM = RPM;
    }

    public String getAverage() {
        return Average;
    }

    public void setAverage(String average) {
        Average = average;
    }

    public String getEngineHour() {
        return EngineHour;
    }

    public void setEngineHour(String engineHour) {
        EngineHour = engineHour;
    }

    public String getFuelUsed() {
        return FuelUsed;
    }

    public void setFuelUsed(String fuelUsed) {
        FuelUsed = fuelUsed;
    }

    public String getIdleFuelUsed() {
        return IdleFuelUsed;
    }

    public void setIdleFuelUsed(String idleFuelUsed) {
        IdleFuelUsed = idleFuelUsed;
    }

    public String getIdleHours() {
        return IdleHours;
    }

    public void setIdleHours(String idleHours) {
        IdleHours = idleHours;
    }

    public String getBoost() {
        return Boost;
    }

    public void setBoost(String boost) {
        Boost = boost;
    }

    public String getCoolantTemperature() {
        return CoolantTemperature;
    }

    public void setCoolantTemperature(String coolantTemperature) {
        CoolantTemperature = coolantTemperature;
    }

    public String getCoolantLevel() {
        return CoolantLevel;
    }

    public void setCoolantLevel(String coolantLevel) {
        CoolantLevel = coolantLevel;
    }

    public String getBatteryVoltage() {
        return BatteryVoltage;
    }

    public void setBatteryVoltage(String batteryVoltage) {
        BatteryVoltage = batteryVoltage;
    }

    public String getWasherFluidLevel() {
        return WasherFluidLevel;
    }

    public void setWasherFluidLevel(String washerFluidLevel) {
        WasherFluidLevel = washerFluidLevel;
    }

    public String getEngineLoad() {
        return EngineLoad;
    }

    public void setEngineLoad(String engineLoad) {
        EngineLoad = engineLoad;
    }

    public String getEngineOilLevel() {
        return EngineOilLevel;
    }

    public void setEngineOilLevel(String engineOilLevel) {
        EngineOilLevel = engineOilLevel;
    }

    public String getCruiseSpeed() {
        return CruiseSpeed;
    }

    public void setCruiseSpeed(String cruiseSpeed) {
        CruiseSpeed = cruiseSpeed;
    }

    public String getMaxRoadSpeed() {
        return MaxRoadSpeed;
    }

    public void setMaxRoadSpeed(String maxRoadSpeed) {
        MaxRoadSpeed = maxRoadSpeed;
    }

    public String getAirSuspension() {
        return AirSuspension;
    }

    public void setAirSuspension(String airSuspension) {
        AirSuspension = airSuspension;
    }

    public String getTransmissionOilLevel() {
        return TransmissionOilLevel;
    }

    public void setTransmissionOilLevel(String transmissionOilLevel) {
        TransmissionOilLevel = transmissionOilLevel;
    }

    public String getDEFTankLevel() {
        return DEFTankLevel;
    }

    public void setDEFTankLevel(String DEFTankLevel) {
        this.DEFTankLevel = DEFTankLevel;
    }

    public String getDEFTankLevelLow() {
        return DEFTankLevelLow;
    }

    public void setDEFTankLevelLow(String DEFTankLevelLow) {
        this.DEFTankLevelLow = DEFTankLevelLow;
    }

    public String getEngineSerialNo() {
        return EngineSerialNo;
    }

    public void setEngineSerialNo(String engineSerialNo) {
        EngineSerialNo = engineSerialNo;
    }

    public String getEngineRatePower() {
        return EngineRatePower;
    }

    public void setEngineRatePower(String engineRatePower) {
        EngineRatePower = engineRatePower;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public int getCruiseSetFg() {
        return CruiseSetFg;
    }

    public void setCruiseSetFg(int cruiseSetFg) {
        CruiseSetFg = cruiseSetFg;
    }

    public int getPowerUnitABSFg() {
        return PowerUnitABSFg;
    }

    public void setPowerUnitABSFg(int powerUnitABSFg) {
        PowerUnitABSFg = powerUnitABSFg;
    }

    public int getTrailerABSFg() {
        return TrailerABSFg;
    }

    public void setTrailerABSFg(int trailerABSFg) {
        TrailerABSFg = trailerABSFg;
    }

    public int getDerateFg() {
        return DerateFg;
    }

    public void setDerateFg(int derateFg) {
        DerateFg = derateFg;
    }

    public int getBrakeApplication() {
        return BrakeApplication;
    }

    public void setBrakeApplication(int brakeApplication) {
        BrakeApplication = brakeApplication;
    }

    public int getRegenerationRequiredFg() {
        return RegenerationRequiredFg;
    }

    public void setRegenerationRequiredFg(int regenerationRequiredFg) {
        RegenerationRequiredFg = regenerationRequiredFg;
    }

    public int getWaterInFuelFg() {
        return WaterInFuelFg;
    }

    public void setWaterInFuelFg(int waterInFuelFg) {
        WaterInFuelFg = waterInFuelFg;
    }

    public int getPTOEngagementFg() {
        return PTOEngagementFg;
    }

    public void setPTOEngagementFg(int PTOEngagementFg) {
        this.PTOEngagementFg = PTOEngagementFg;
    }

    public String getCuriseTime() {
        return CuriseTime;
    }

    public void setCuriseTime(String curiseTime) {
        CuriseTime = curiseTime;
    }

    public int getSeatBeltFg() {
        return SeatBeltFg;
    }

    public void setSeatBeltFg(int seatBeltFg) {
        SeatBeltFg = seatBeltFg;
    }

    public int getTransmissionGear() {
        return TransmissionGear;
    }

    public void setTransmissionGear(int transmissionGear) {
        TransmissionGear = transmissionGear;
    }

    public int getActiveDTCFg() {
        return ActiveDTCFg;
    }

    public void setActiveDTCFg(int activeDTCFg) {
        ActiveDTCFg = activeDTCFg;
    }

    public int getInActiveDTCFg() {
        return InActiveDTCFg;
    }

    public void setInActiveDTCFg(int inActiveDTCFg) {
        InActiveDTCFg = inActiveDTCFg;
    }

    public String getPTOHours() {
        return PTOHours;
    }

    public void setPTOHours(String PTOHours) {
        this.PTOHours = PTOHours;
    }

    public int getTPMSWarningFg() {
        return TPMSWarningFg;
    }

    public void setTPMSWarningFg(int TPMSWarningFg) {
        this.TPMSWarningFg = TPMSWarningFg;
    }

    public int getSyncFg() {
        return SyncFg;
    }

    public void setSyncFg(int syncFg) {
        SyncFg = syncFg;
    }

    public String getPTOFuelUsed() {
        return PTOFuelUsed;
    }

    public void setPTOFuelUsed(String PTOFuelUsed) {
        this.PTOFuelUsed = PTOFuelUsed;
    }

    public String getFuelPressure() {
        return FuelPressure;
    }

    public void setFuelPressure(String fuelPressure) {
        FuelPressure = fuelPressure;
    }

    public String getAirInletTemperature() {
        return AirInletTemperature;
    }

    public void setAirInletTemperature(String airInletTemperature) {
        AirInletTemperature = airInletTemperature;
    }

    public String getBarometricPressure() {
        return BarometricPressure;
    }

    public void setBarometricPressure(String barometricPressure) {
        BarometricPressure = barometricPressure;
    }

    public String getEngineOilPressure() {
        return EngineOilPressure;
    }

    public void setEngineOilPressure(String engineOilPressure) {
        EngineOilPressure = engineOilPressure;
    }
}
