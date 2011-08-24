/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.cloud.api.commands.GenerateUsageRecordsCmd;
import com.cloud.api.commands.GetUsageRecordsCmd;
import com.cloud.domain.dao.DomainDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.usage.UsageIPAddressVO;
import com.cloud.usage.UsageJobVO;
import com.cloud.usage.UsageVO;
import com.cloud.usage.dao.UsageDao;
import com.cloud.usage.dao.UsageIPAddressDao;
import com.cloud.usage.dao.UsageJobDao;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.UserContext;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;

public class ManagementServerExtImpl extends ManagementServerImpl implements ManagementServerExt {
    private final AccountDao _accountDao;
    private final DomainDao _domainDao;
    private final UsageDao _usageDao;
    private final UsageJobDao _usageJobDao;
    private final UsageIPAddressDao _usageIpAddressDao;
    private final TimeZone _usageTimezone;

    protected ManagementServerExtImpl() {
        super();

        ComponentLocator locator = ComponentLocator.getLocator(ManagementServer.Name);
        _accountDao = locator.getDao(AccountDao.class);
        _domainDao = locator.getDao(DomainDao.class);
        _usageDao = locator.getDao(UsageDao.class);
        _usageJobDao = locator.getDao(UsageJobDao.class);
        _usageIpAddressDao = locator.getDao(UsageIPAddressDao.class);

        Map<String, String> configs = getConfigs();
        String timeZoneStr = configs.get("usage.aggregation.timezone");
        if (timeZoneStr == null) {
            timeZoneStr = "GMT";
        }
        _usageTimezone = TimeZone.getTimeZone(timeZoneStr);
    }

    @Override
    public boolean generateUsageRecords(GenerateUsageRecordsCmd cmd) {
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        try {
            UsageJobVO immediateJob = _usageJobDao.getNextImmediateJob();
            if (immediateJob == null) {
                UsageJobVO job = _usageJobDao.getLastJob();

                String host = null;
                int pid = 0;
                if (job != null) {
                    host = job.getHost();
                    pid = ((job.getPid() == null) ? 0 : job.getPid().intValue());
                }
                _usageJobDao.createNewJob(host, pid, UsageJobVO.JOB_TYPE_SINGLE);
            }
        } finally {
            txn.close();

            // switch back to VMOPS_DB
            Transaction swap = Transaction.open(Transaction.CLOUD_DB);
            swap.close();
        }
        return true;
    }

    @Override
    public List<Object> getUsageRecords(GetUsageRecordsCmd cmd) {
        Long accountId = cmd.getAccountId();
        Long domainId = cmd.getDomainId();
        String accountName = cmd.getAccountName();
        Account userAccount = null;
        Account account = (Account)UserContext.current().getCaller();
        
        //if accountId is not specified, use accountName and domainId
        if ((accountId == null) && (accountName != null) && (domainId != null)) {
            if (_domainDao.isChildDomain(account.getDomainId(), domainId)) {
                Filter filter = new Filter(AccountVO.class, "id", Boolean.FALSE, null, null);
                List<AccountVO> accounts = _accountDao.listAccounts(accountName, domainId, filter); 
                if(accounts.size() > 0){
                    userAccount = accounts.get(0);
                }
                if (userAccount != null) {
                    accountId = userAccount.getId();
                } else {
                    throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
                }
            } else {
                throw new PermissionDeniedException("Invalid Domain Id or Account");
            }
        } 

        boolean isAdmin = false;
        
        //If accountId couldn't be found using accountName and domainId, get it from userContext
        if(accountId == null){
            accountId = account.getId();
            //List records for all the accounts if the caller account is of type admin. 
            //If account_id or account_name is explicitly mentioned, list records for the specified account only even if the caller is of type admin
            if(account.getType() == Account.ACCOUNT_TYPE_ADMIN){
                isAdmin = true;
            }
            s_logger.debug("Account details not available. Using userContext accountId: "+accountId);
        }

        Date startDate = cmd.getStartDate();
        Date endDate = cmd.getEndDate();
        TimeZone usageTZ = getUsageTimezone();
        Date adjustedStartDate = computeAdjustedTime(startDate, usageTZ, true);
        Date adjustedEndDate = computeAdjustedTime(endDate, usageTZ, false);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("getting usage records for account: " + accountId + ", domainId: " + domainId + ", between " + startDate + " and " + endDate + ", using pageSize: " + cmd.getPageSizeVal() + " and startIndex: " + cmd.getStartIndex());
        }

        Filter usageFilter = new Filter(UsageVO.class, "startDate", false, cmd.getStartIndex(), cmd.getPageSizeVal());
        SearchCriteria<UsageVO> sc = _usageDao.createSearchCriteria();

        if (accountId != -1 && accountId != Account.ACCOUNT_ID_SYSTEM && !isAdmin) {
            sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        }

        if (domainId != null) {
            sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
        }

        if ((adjustedStartDate != null) && (adjustedEndDate != null) && adjustedStartDate.before(adjustedEndDate)) {
            sc.addAnd("startDate", SearchCriteria.Op.BETWEEN, adjustedStartDate, adjustedEndDate);
            sc.addAnd("endDate", SearchCriteria.Op.BETWEEN, adjustedStartDate, adjustedEndDate);
        } else {
            return new ArrayList<Object>(); // return an empty list if we fail to validate the dates
        }

        List<UsageVO> usageRecords = null;
        List<UsageIPAddressVO> ipAddressRecords = null;
        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        try {
            usageRecords = _usageDao.searchAllRecords(sc, usageFilter);

            Long accountIdObj = null;
            if (accountId != -1) {
                accountIdObj = Long.valueOf(accountId);
            }
            ipAddressRecords = _usageIpAddressDao.getUsageRecords(accountIdObj, domainId, adjustedStartDate, adjustedEndDate, true, cmd.getStartIndex(), cmd.getPageSizeVal());
        } finally {
            txn.close();

            // switch back to VMOPS_DB
            Transaction swap = Transaction.open(Transaction.CLOUD_DB);
            swap.close();
        }

        List<Object> finalUsageRecords = new ArrayList<Object>();
        finalUsageRecords.addAll(usageRecords);
        finalUsageRecords.addAll(ipAddressRecords);

        // now that we are done with the records, update the command with the correct timezone so it can write the proper response
        cmd.setUsageTimezone(getUsageTimezone());

        return finalUsageRecords;
    }

    @Override
    public TimeZone getUsageTimezone() {
        return _usageTimezone;
    }

    @Override
    public String[] getApiConfig() {
        return new String[] { "commands.properties", "commands-ext.properties" };
    }

    private Date computeAdjustedTime(Date initialDate, TimeZone targetTZ, boolean adjustToDayStart) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(initialDate);
        TimeZone localTZ = cal.getTimeZone();
        int timezoneOffset = cal.get(Calendar.ZONE_OFFSET);
        if (localTZ.inDaylightTime(initialDate)) {
            timezoneOffset += (60 * 60 * 1000);
        }
        cal.add(Calendar.MILLISECOND, timezoneOffset);

        Date newTime = cal.getTime();

        Calendar calTS = Calendar.getInstance(targetTZ);
        calTS.setTime(newTime);
        timezoneOffset = calTS.get(Calendar.ZONE_OFFSET);
        if (targetTZ.inDaylightTime(initialDate)) {
            timezoneOffset += (60 * 60 * 1000);
        }

        calTS.add(Calendar.MILLISECOND, -1*timezoneOffset);
        if (adjustToDayStart) {
            calTS.set(Calendar.HOUR_OF_DAY, 0);
            calTS.set(Calendar.MINUTE, 0);
            calTS.set(Calendar.SECOND, 0);
            calTS.set(Calendar.MILLISECOND, 0);
        } else {
            calTS.set(Calendar.HOUR_OF_DAY, 23);
            calTS.set(Calendar.MINUTE, 59);
            calTS.set(Calendar.SECOND, 59);
            calTS.set(Calendar.MILLISECOND, 999);
        }

        return calTS.getTime();
    }
}
