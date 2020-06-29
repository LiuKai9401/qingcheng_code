package com.qingcheng.pojo.goods;

import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "tb_audit")
public class Audit implements Serializable {

    private Date auditDate;

    private String auditName;

    private String auditStatus;

    private String auditMessage;

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    public String getAuditName() {
        return auditName;
    }

    public void setAuditName(String auditName) {
        this.auditName = auditName;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditMessage() {
        return auditMessage;
    }

    public void setAuditMessage(String auditMessage) {
        this.auditMessage = auditMessage;
    }
}
