package com.hutchgroup.elog.beans;

import java.io.Serializable;

public class DefectBean implements Serializable {

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String comment, defect;

    public boolean isRepairFg() {
        return repairFg;
    }

    public void setRepairFg(boolean repairFg) {
        this.repairFg = repairFg;
    }

    public boolean isDefectFg() {
        return defectFg;
    }

    public void setDefectFg(boolean defectFg) {
        this.defectFg = defectFg;
    }

    private boolean repairFg, defectFg;
    private int defectId;



    /**
     * @return the defectId
     */
    public int getDefectId() {
        return defectId;
    }

    /**
     * @param defectId the defectId to set
     */
    public void setDefectId(int defectId) {
        this.defectId = defectId;
    }

    /**
     * @return the defect
     */
    public String getDefect() {
        return defect;
    }

    /**
     * @param defect the defect to set
     */
    public void setDefect(String defect) {
        this.defect = defect;
    }


}
