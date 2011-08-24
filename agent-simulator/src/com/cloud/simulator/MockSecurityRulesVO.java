package com.cloud.simulator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="mocksecurityrules")
public class MockSecurityRulesVO {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private long id;
    
    @Column(name="vmid")
    private Long vmId;
    
    @Column(name="signature")
    private String signature;
    
    @Column(name="seqnum")
    private Long seqnum;
    
    @Column(name="ruleset")
    private String ruleset;
    
    @Column(name="hostid")
    private Long hostId;
    
    @Column(name="vmname")
    public String vmName;
    
    public String getVmName() {
        return this.vmName;
    }
    
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }
    
    public Long getHostId() {
        return this.hostId;
    }
    
    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }
    
    public long getId() {
        return this.id;
    }
    
    public Long getVmId() {
        return this.vmId;
    }
    
    public void setVmId(Long vmId) {
        this.vmId = vmId;
    }
    
    public String getSignature() {
        return this.signature;
    }
    
    public void setSignature(String sig) {
        this.signature = sig;
    }
    
    public Long getSeqNum() {
        return this.seqnum;
    }
    
    public void setSeqNum(Long seqNum) {
        this.seqnum = seqNum;
    }
    
    public String getRuleSet() {
        return this.ruleset;
    }
    
    public void setRuleSet(String ruleset) {
        this.ruleset = ruleset;
    }
}
