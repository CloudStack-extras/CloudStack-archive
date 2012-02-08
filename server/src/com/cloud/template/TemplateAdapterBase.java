package com.cloud.template;

import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.api.commands.DeleteIsoCmd;
import com.cloud.api.commands.DeleteTemplateCmd;
import com.cloud.api.commands.RegisterIsoCmd;
import com.cloud.api.commands.RegisterTemplateCmd;
import com.cloud.configuration.ResourceCount.ResourceType;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.dao.UsageEventDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Grouping;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserContext;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.EnumUtils;
import com.cloud.utils.component.Inject;
import com.cloud.vm.UserVmVO;

public abstract class TemplateAdapterBase implements TemplateAdapter {
	private final static Logger s_logger = Logger.getLogger(TemplateAdapterBase.class);
	protected String _name;
	protected @Inject DomainDao _domainDao;
	protected @Inject AccountDao _accountDao;
	protected @Inject ConfigurationDao _configDao;
	protected @Inject UserDao _userDao;
	protected @Inject AccountManager _accountMgr;
	protected @Inject DataCenterDao _dcDao;
	protected @Inject VMTemplateDao _tmpltDao;
	protected @Inject VMTemplateHostDao _tmpltHostDao;
	protected @Inject VMTemplateZoneDao _tmpltZoneDao;
	protected @Inject UsageEventDao _usageEventDao;
	protected @Inject HostDao _hostDao;
	
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		_name = name;
		return true;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}
	
	private static boolean isAdmin(short accountType) {
	    return ((accountType == Account.ACCOUNT_TYPE_ADMIN) ||
	    		(accountType == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) ||
	            (accountType == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) ||
	            (accountType == Account.ACCOUNT_TYPE_READ_ONLY_ADMIN));
	}

	public TemplateProfile prepare(boolean isIso, Long userId, String name, String displayText, Integer bits,
            Boolean passwordEnabled, Boolean requiresHVM, String url, Boolean isPublic, Boolean featured,
            Boolean isExtractable, String format, Long guestOSId, Long zoneId, HypervisorType hypervisorType,
            String accountName, Long domainId, String chksum, Boolean bootable, Map details) throws ResourceAllocationException {
	    return prepare(isIso, userId, name, displayText, bits, passwordEnabled, requiresHVM, url, isPublic, featured, isExtractable, format, guestOSId, zoneId, hypervisorType,
	            accountName, domainId, chksum, bootable, null, details);
	}
	
	public TemplateProfile prepare(boolean isIso, Long userId, String name, String displayText, Integer bits,
			Boolean passwordEnabled, Boolean requiresHVM, String url, Boolean isPublic, Boolean featured,
			Boolean isExtractable, String format, Long guestOSId, Long zoneId, HypervisorType hypervisorType,
			String accountName, Long domainId, String chksum, Boolean bootable, String templateTag, Map details) 
	        throws ResourceAllocationException {
	    
		Account ctxAccount = UserContext.current().getCaller();
		Account resourceAccount = null;
		Long accountId = null;
		// parameters verification
		
		if (isPublic == null) {
			isPublic = Boolean.FALSE;
		}
		
		if (zoneId.longValue() == -1) {
			zoneId = null;
		}
		
		if (isIso) {
	        if (bootable == null) {
	        	bootable = Boolean.TRUE;
	        }
	        
	        if ((guestOSId == null || guestOSId == 138L) && bootable == true){
	        	throw new InvalidParameterValueException("Please pass a valid GuestOS Id");
	        }
	        if (bootable == false){
	        	guestOSId = 138L; //Guest os id of None.
	        }
		} else {
			if (bits == null) {
				bits = Integer.valueOf(64);
			}
			if (passwordEnabled == null) {
				passwordEnabled = false;
			}
			if (requiresHVM == null) {
				requiresHVM = true;
			}	
		}
		
        if (isExtractable == null) {
            isExtractable = Boolean.FALSE;
        }
		if ((accountName == null) ^ (domainId == null)) {// XOR - Both have to be passed or don't pass any of them
			throw new InvalidParameterValueException("Please specify both account and domainId or dont specify any of them");
		}

		// This complex logic is just for figuring out the template owning
		// account because a user can register templates on other account's
		// behalf.
		if ((ctxAccount == null) || isAdmin(ctxAccount.getType())) {
			if (domainId != null) {
				if ((ctxAccount != null) && !_domainDao.isChildDomain(ctxAccount.getDomainId(), domainId)) {
					throw new PermissionDeniedException("Failed to register template, invalid domain id (" + domainId + ") given.");
				}
				if (accountName != null) {
					resourceAccount = _accountDao.findActiveAccount(accountName, domainId);
					if (resourceAccount == null) {
						throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
					}
					accountId = resourceAccount.getId();
				}
			} else {
				accountId = ((ctxAccount != null) ? ctxAccount.getId() : null);
			}
		} else {
			accountId = ctxAccount.getId();
		}

		if (null == accountId && null == accountName && null == domainId && null == ctxAccount) {
			accountId = 1L;
		}
		if (null == accountId) {
			throw new InvalidParameterValueException("No valid account specified for registering template.");
		}

		boolean isAdmin = _accountDao.findById(accountId).getType() == Account.ACCOUNT_TYPE_ADMIN;

		if (!isAdmin && zoneId == null) {
			throw new InvalidParameterValueException("Please specify a valid zone Id.");
		}

		if (url.toLowerCase().contains("file://")) {
			throw new InvalidParameterValueException("File:// type urls are currently unsupported");
		}
		
		boolean allowPublicUserTemplates = Boolean.parseBoolean(_configDao.getValue("allow.public.user.templates"));
		if (!isAdmin && !allowPublicUserTemplates && isPublic) {
			throw new InvalidParameterValueException("Only private templates/ISO can be created.");
		}

		if (!isAdmin || featured == null) {
			featured = Boolean.FALSE;
		}

		// If command is executed via 8096 port, set userId to the id of System
		// account (1)
		if (userId == null) {
			userId = Long.valueOf(1);
		}
		
		ImageFormat imgfmt = ImageFormat.valueOf(format.toUpperCase());
		if (imgfmt == null) {
			throw new IllegalArgumentException("Image format is incorrect " + format + ". Supported formats are " + EnumUtils.listValues(ImageFormat.values()));
		}
         
        // Check that the resource limit for templates/ISOs won't be exceeded
        UserVO user = _userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Unable to find user with id " + userId);
        }
        
    	AccountVO account = _accountDao.findById(accountId);
        if (_accountMgr.resourceLimitExceeded(account, ResourceType.template)) {
        	ResourceAllocationException rae = new ResourceAllocationException("Maximum number of templates and ISOs for account: " + account.getAccountName() + " has been exceeded.");
        	rae.setResourceType("template");
        	throw rae;
        }
        
        if (account.getType() != Account.ACCOUNT_TYPE_ADMIN && zoneId == null) {
        	throw new IllegalArgumentException("Only admins can create templates in all zones");
        }
        
        // If a zoneId is specified, make sure it is valid
        if (zoneId != null) {
        	DataCenterVO zone = _dcDao.findById(zoneId);
        	if (zone == null) {
        		throw new IllegalArgumentException("Please specify a valid zone.");
        	}
    		Account caller = UserContext.current().getCaller();
    		if(Grouping.AllocationState.Disabled == zone.getAllocationState() && !_accountMgr.isRootAdmin(caller.getType())){
    			throw new PermissionDeniedException("Cannot perform this operation, Zone is currently disabled: "+ zoneId );
    		}
        }
       
        List<VMTemplateVO> systemvmTmplts = _tmpltDao.listAllSystemVMTemplates();
        for (VMTemplateVO template : systemvmTmplts) {
            if (template.getName().equalsIgnoreCase(name) || template.getDisplayText().equalsIgnoreCase(displayText)) {
                throw new IllegalArgumentException("Cannot use reserved names for templates");
            }
        }
        
        Long id = _tmpltDao.getNextInSequence(Long.class, "id");
        UserContext.current().setEventDetails("Id: " +id+ " name: " + name);
		return new TemplateProfile(id, userId, name, displayText, bits, passwordEnabled, requiresHVM, url, isPublic,
				featured, isExtractable, imgfmt, guestOSId, zoneId, hypervisorType, accountName, domainId, accountId, chksum, bootable, templateTag, details);
	}
	
	@Override
	public TemplateProfile prepare(RegisterTemplateCmd cmd) throws ResourceAllocationException {
		return prepare(false, UserContext.current().getCallerUserId(), cmd.getTemplateName(), cmd.getDisplayText(),
				cmd.getBits(), cmd.isPasswordEnabled(), cmd.getRequiresHvm(), cmd.getUrl(), cmd.isPublic(), cmd.isFeatured(),
				cmd.isExtractable(), cmd.getFormat(), cmd.getOsTypeId(), cmd.getZoneId(), HypervisorType.getType(cmd.getHypervisor()),
				cmd.getAccountName(), cmd.getDomainId(), cmd.getChecksum(), true, cmd.getTemplateTag(), cmd.getDetails());
	}

	public TemplateProfile prepare(RegisterIsoCmd cmd) throws ResourceAllocationException {
		return prepare(true, UserContext.current().getCallerUserId(), cmd.getIsoName(), cmd.getDisplayText(), 64, false,
					true, cmd.getUrl(), cmd.isPublic(), cmd.isFeatured(), cmd.isExtractable(), ImageFormat.ISO.toString(), cmd.getOsTypeId(),
					cmd.getZoneId(), HypervisorType.None, cmd.getAccountName(), cmd.getDomainId(), null, cmd.isBootable(), null, null);
	}
	
	protected VMTemplateVO persistTemplate(TemplateProfile profile) {
		Long zoneId = profile.getZoneId();
		VMTemplateVO template = new VMTemplateVO(profile.getTemplateId(), profile.getName(), profile.getFormat(), profile.getIsPublic(),
				profile.getFeatured(), profile.getIsExtractable(), TemplateType.USER, profile.getUrl(), profile.getRequiresHVM(),
				profile.getBits(), profile.getAccountId(), profile.getCheckSum(), profile.getDisplayText(),
				profile.getPasswordEnabled(), profile.getGuestOsId(), profile.getBootable(), profile.getHypervisorType(), profile.getTemplateTag(), 
				profile.getDetails());
        
		if (zoneId == null || zoneId == -1) {
            List<DataCenterVO> dcs = _dcDao.listAllIncludingRemoved();

        	for (DataCenterVO dc: dcs) {
    			_tmpltDao.addTemplateToZone(template, dc.getId());
    		}
        	template.setCrossZones(true);
        } else {
			_tmpltDao.addTemplateToZone(template, zoneId);
        }
		return template;
	}
	

	private Long accountAndUserValidation(Account account, Long userId, UserVmVO vmInstanceCheck, VMTemplateVO template, String msg)
			throws PermissionDeniedException {

		if (account != null) {
			if (!isAdmin(account.getType())) {
				if ((vmInstanceCheck != null) && (account.getId() != vmInstanceCheck.getAccountId())) {
					throw new PermissionDeniedException(msg + ". Permission denied.");
				}

				if ((template != null)
						&& (!template.isPublicTemplate() && (account.getId() != template.getAccountId()) && (template.getTemplateType() != TemplateType.PERHOST))) {
					throw new PermissionDeniedException(msg + ". Permission denied.");
				}

			} else {
				if ((vmInstanceCheck != null) && !_domainDao.isChildDomain(account.getDomainId(), vmInstanceCheck.getDomainId())) {
					throw new PermissionDeniedException(msg + ". Permission denied.");
				}
				// FIXME: if template/ISO owner is null we probably need to
				// throw some kind of exception

				if (template != null) {
					Account templateOwner = _accountDao.findById(template.getAccountId());
					if ((templateOwner != null) && !_domainDao.isChildDomain(account.getDomainId(), templateOwner.getDomainId())) {
						throw new PermissionDeniedException(msg + ". Permission denied.");
					}
				}
			}
		}
		// If command is executed via 8096 port, set userId to the id of System
		// account (1)
		if (userId == null) {
			userId = new Long(1);
		}

		return userId;
	}
	
	public TemplateProfile prepareDelete(DeleteTemplateCmd cmd) {
		Long templateId = cmd.getId();
		Long userId = UserContext.current().getCallerUserId();
		Account account = UserContext.current().getCaller();
		Long zoneId = cmd.getZoneId();

		VMTemplateVO template = _tmpltDao.findById(templateId.longValue());
		if (template == null) {
			throw new InvalidParameterValueException("unable to find template with id " + templateId);
		}

		userId = accountAndUserValidation(account, userId, null, template, "Unable to delete template ");

		UserVO user = _userDao.findById(userId);
		if (user == null) {
			throw new InvalidParameterValueException("Please specify a valid user.");
		}

		if (template.getFormat() == ImageFormat.ISO) {
			throw new InvalidParameterValueException("Please specify a valid template.");
		}

		return new TemplateProfile(userId, template, zoneId);
	}

	public TemplateProfile prepareDelete(DeleteIsoCmd cmd) {
		Long templateId = cmd.getId();
        Long userId = UserContext.current().getCallerUserId();
        Account account = UserContext.current().getCaller();
        Long zoneId = cmd.getZoneId();
        
        VMTemplateVO template = _tmpltDao.findById(templateId.longValue());
        if (template == null) {
            throw new InvalidParameterValueException("unable to find iso with id " + templateId);
        }
        
        userId = accountAndUserValidation(account, userId, null, template, "Unable to delete iso " );
        
    	UserVO user = _userDao.findById(userId);
    	if (user == null) {
    		throw new InvalidParameterValueException("Please specify a valid user.");
    	}
    	
    	if (template.getFormat() != ImageFormat.ISO) {
    		throw new InvalidParameterValueException("Please specify a valid iso.");
    	}
 	
    	return new TemplateProfile(userId, template, zoneId);
	}

	abstract public VMTemplateVO create(TemplateProfile profile);
	abstract public boolean delete(TemplateProfile profile);
}
