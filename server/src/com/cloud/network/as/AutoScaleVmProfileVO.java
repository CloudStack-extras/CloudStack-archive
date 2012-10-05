// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.network.as;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.cloud.api.Identity;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.NetUtils;

@Entity
@Table(name = "autoscale_vmprofiles")
@Inheritance(strategy = InheritanceType.JOINED)
public class AutoScaleVmProfileVO implements AutoScaleVmProfile, Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected long id;

    @Column(name = "uuid")
    protected String uuid;

    @Column(name = "zone_id", updatable = true, nullable = false)
    protected Long zoneId;

    @Column(name = "domain_id", updatable = true)
    private long domainId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "autoscale_user_id")
    private long autoscaleUserId;

    @Column(name = "service_offering_id", updatable = true, nullable = false)
    private Long serviceOfferingId;

    @Column(name = "template_id", updatable = true, nullable = false, length = 17)
    private Long templateId;

    @Column(name = "other_deploy_params", updatable = true, length = 1024)
    private String otherDeployParams;

    @Column(name = "destroy_vm_grace_period", updatable = true)
    private Integer destroyVmGraceperiod = NetUtils.DEFAULT_AUTOSCALE_VM_DESTROY_TIME;

    @Column(name = "snmp_community", updatable = true)
    private String snmpCommunity = NetUtils.DEFAULT_SNMP_COMMUNITY;

    @Column(name = "snmp_port", updatable = true)
    private Integer snmpPort = NetUtils.DEFAULT_SNMP_PORT;

    @Column(name = GenericDao.REMOVED_COLUMN)
    protected Date removed;

    @Column(name = GenericDao.CREATED_COLUMN)
    protected Date created;

    public AutoScaleVmProfileVO() {
    }

    public AutoScaleVmProfileVO(long zoneId, long domainId, long accountId, long serviceOfferingId, long templateId, String otherDeployParams, String snmpCommunity, Integer snmpPort, Integer destroyVmGraceperiod,
            long autoscaleUserId) {
        this.uuid = UUID.randomUUID().toString();
        this.zoneId = zoneId;
        this.domainId = domainId;
        this.accountId = accountId;
        this.serviceOfferingId = serviceOfferingId;
        this.templateId = templateId;
        this.otherDeployParams = otherDeployParams;
        this.autoscaleUserId = autoscaleUserId;
        if (destroyVmGraceperiod != null) {
            this.destroyVmGraceperiod = destroyVmGraceperiod;
        }
        if (snmpCommunity != null) {
            this.snmpCommunity = snmpCommunity;
        }
        if (snmpPort != null) {
            this.snmpPort = snmpPort;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("AutoScaleVMProfileVO[").append("id").append(id).append("-").append("templateId").append("-").append(templateId).append("]").toString();
    }

    @Override
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    @Override
    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    @Override
    public String getOtherDeployParams() {
        return otherDeployParams;
    }

    public void setOtherDeployParams(String otherDeployParams) {
        this.otherDeployParams = otherDeployParams;
    }

    @Override
    public String getSnmpCommunity() {
        return snmpCommunity;
    }

    public void setSnmpCommunity(String snmpCommunity) {
        this.snmpCommunity = snmpCommunity;
    }

    @Override
    public Integer getSnmpPort() {
        return snmpPort;
    }

    public void setSnmpPort(Integer snmpPort) {
        this.snmpPort = snmpPort;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setAutoscaleUserId(long autoscaleUserId) {
        this.autoscaleUserId = autoscaleUserId;
    }

    @Override
    public Long getZoneId() {
        return zoneId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Integer getDestroyVmGraceperiod() {
        return destroyVmGraceperiod;
    }

    public void setDestroyVmGraceperiod(Integer destroyVmGraceperiod) {
        this.destroyVmGraceperiod = destroyVmGraceperiod;
    }

    @Override
    public long getAutoScaleUserId() {
        return autoscaleUserId;
    }
}
