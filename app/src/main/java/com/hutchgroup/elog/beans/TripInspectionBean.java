package com.hutchgroup.elog.beans;

import java.io.Serializable;
import java.util.ArrayList;

public class TripInspectionBean implements Serializable {
	private int id;
	private String dateTime;
	private String latitude;
	private String longitude;
	private String location;
	private String odometerReading;
	private String comments;

	private int type;
	private int defect;
    private int defectRepaired;
    private int safeToDrive;
	private int driverId;
	private String driverName;
	private String defectItems;
	private String truckNumber;
	private String trailerNumber;

    private String pictures = "";

    private int SyncFg;

    public int getSyncFg() {
        return SyncFg;
    }

    public void setSyncFg(int syncFg) {
        SyncFg = syncFg;
    }

    public String getPictures() {
        return pictures;
    }

    public void setPictures(String pictures) {
        this.pictures = pictures;
    }

    /**
     * @return the defect
     */
    public int getDefect() {
        return defect;
    }

    /**
     * @param defect
     *            the defect to set
     */
    public void setDefect(int defect) {
        this.defect = defect;
    }

    public int getDefectRepaired() {
        return defectRepaired;
    }

    public void setDefectRepaired(int defectRepaired) {
        this.defectRepaired = defectRepaired;
    }

    public int getSafeToDrive() {
        return safeToDrive;
    }

    public void setSafeToDrive(int safeToDrive) {
        this.safeToDrive = safeToDrive;
    }

    /**
     * @return the driverId
     */
    public int getDriverId() {
        return driverId;
    }

    /**
     * @param driverId
     *            the driverId to set
     */
    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the truckNumber
     */
    public String getTruckNumber() {
        return truckNumber;
    }

    /**
     * @param truckNumber
     *            the truckNumber to set
     */
    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    /**
     * @return the trailerNumber
     */
    public String getTrailerNumber() {
        return trailerNumber;
    }

    /**
     * @param trailerNumber
     *            the trailerNumber to set
     */
    public void setTrailerNumber(String trailerNumber) {
        this.trailerNumber = trailerNumber;
    }

    /**
     * @return the defectItems
     */
    public String getDefectItems() {
        return defectItems;
    }

    /**
     * @param defectItems
     *            the defectItems to set
     */
    public void setDefectItems(String defectItems) {
        this.defectItems = defectItems;
    }

	/**
	 * @return the driverName
	 */
	public String getDriverName() {
		return driverName;
	}

	/**
	 * @param driverName
	 *            the driverName to set
	 */
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the tripInspectionId
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the inspectionDateTime
	 */
	public String getInspectionDateTime() {
		return dateTime;
	}

	/**
	 * @param inspectionDateTime
	 *            the inspectionDateTime to set
	 */
	public void setInspectionDateTime(String inspectionDateTime) {
		this.dateTime = inspectionDateTime;
	}

	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the odometerReading
	 */
	public String getOdometerReading() {
		return odometerReading;
	}

	/**
	 * @param odometerReading
	 *            the odometerReading to set
	 */
	public void setOdometerReading(String odometerReading) {
		this.odometerReading = odometerReading;
	}


}
