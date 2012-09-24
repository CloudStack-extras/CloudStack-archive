/*
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.bridge.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

import com.amazon.ec2.*;
import com.cloud.bridge.service.core.ec2.EC2Address;
import com.cloud.bridge.service.core.ec2.EC2AddressFilterSet;
import com.cloud.bridge.service.core.ec2.EC2AssociateAddress;
import com.cloud.bridge.service.core.ec2.EC2AuthorizeRevokeSecurityGroup;
import com.cloud.bridge.service.core.ec2.EC2AvailabilityZonesFilterSet;
import com.cloud.bridge.service.core.ec2.EC2CreateImage;
import com.cloud.bridge.service.core.ec2.EC2CreateImageResponse;
import com.cloud.bridge.service.core.ec2.EC2CreateKeyPair;
import com.cloud.bridge.service.core.ec2.EC2CreateVolume;
import com.cloud.bridge.service.core.ec2.EC2Tags;
import com.cloud.bridge.service.core.ec2.EC2DeleteKeyPair;
import com.cloud.bridge.service.core.ec2.EC2DescribeAddresses;
import com.cloud.bridge.service.core.ec2.EC2DescribeAddressesResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeAvailabilityZones;
import com.cloud.bridge.service.core.ec2.EC2DescribeAvailabilityZonesResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeImageAttribute;

import com.cloud.bridge.service.core.ec2.EC2DescribeImages;
import com.cloud.bridge.service.core.ec2.EC2DescribeImagesResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeInstances;
import com.cloud.bridge.service.core.ec2.EC2DescribeInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeKeyPairs;
import com.cloud.bridge.service.core.ec2.EC2DescribeKeyPairsResponse;
import com.cloud.bridge.service.core.ec2.EC2ImageLaunchPermission;
import com.cloud.bridge.service.core.ec2.EC2ResourceTag;
import com.cloud.bridge.service.core.ec2.EC2DescribeSecurityGroups;
import com.cloud.bridge.service.core.ec2.EC2DescribeSecurityGroupsResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeSnapshots;
import com.cloud.bridge.service.core.ec2.EC2DescribeSnapshotsResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeTags;
import com.cloud.bridge.service.core.ec2.EC2DescribeTagsResponse;
import com.cloud.bridge.service.core.ec2.EC2DescribeVolumes;
import com.cloud.bridge.service.core.ec2.EC2DescribeVolumesResponse;
import com.cloud.bridge.service.core.ec2.EC2DisassociateAddress;
import com.cloud.bridge.service.core.ec2.EC2Engine;
import com.cloud.bridge.service.core.ec2.EC2Filter;
import com.cloud.bridge.service.core.ec2.EC2GroupFilterSet;
import com.cloud.bridge.service.core.ec2.EC2Image;
import com.cloud.bridge.service.core.ec2.EC2ImageAttributes;
import com.cloud.bridge.service.core.ec2.EC2ImageAttributes.ImageAttribute;
import com.cloud.bridge.service.core.ec2.EC2ImportKeyPair;
import com.cloud.bridge.service.core.ec2.EC2Instance;
import com.cloud.bridge.service.core.ec2.EC2InstanceFilterSet;
import com.cloud.bridge.service.core.ec2.EC2IpPermission;
import com.cloud.bridge.service.core.ec2.EC2KeyPairFilterSet;
import com.cloud.bridge.service.core.ec2.EC2ModifyImageAttribute;
import com.cloud.bridge.service.core.ec2.EC2PasswordData;
import com.cloud.bridge.service.core.ec2.EC2RebootInstances;
import com.cloud.bridge.service.core.ec2.EC2RegisterImage;
import com.cloud.bridge.service.core.ec2.EC2ReleaseAddress;
import com.cloud.bridge.service.core.ec2.EC2TagKeyValue;
import com.cloud.bridge.service.core.ec2.EC2TagTypeId;
import com.cloud.bridge.service.core.ec2.EC2RunInstances;
import com.cloud.bridge.service.core.ec2.EC2RunInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2SSHKeyPair;
import com.cloud.bridge.service.core.ec2.EC2SecurityGroup;
import com.cloud.bridge.service.core.ec2.EC2Snapshot;
import com.cloud.bridge.service.core.ec2.EC2SnapshotFilterSet;
import com.cloud.bridge.service.core.ec2.EC2StartInstances;
import com.cloud.bridge.service.core.ec2.EC2StartInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2StopInstances;
import com.cloud.bridge.service.core.ec2.EC2StopInstancesResponse;
import com.cloud.bridge.service.core.ec2.EC2TagsFilterSet;
import com.cloud.bridge.service.core.ec2.EC2Volume;
import com.cloud.bridge.service.core.ec2.EC2VolumeFilterSet;
import com.cloud.bridge.service.exception.EC2ServiceException;
import com.cloud.bridge.service.exception.EC2ServiceException.ClientError;
import com.cloud.bridge.service.exception.EC2ServiceException.ServerError;
import com.cloud.bridge.util.EC2RestAuth;


public class EC2SoapServiceImpl implements AmazonEC2SkeletonInterface  {

    private static EC2Engine engine;
    
    @SuppressWarnings("static-access")
	public EC2SoapServiceImpl(EC2Engine engine) {
    	this.engine = engine;
    }

	public AttachVolumeResponse attachVolume(AttachVolume attachVolume) {
		EC2Volume request = new EC2Volume();
		AttachVolumeType avt = attachVolume.getAttachVolume();
		
		request.setId(avt.getVolumeId());
		request.setInstanceId(avt.getInstanceId());
		request.setDevice( avt.getDevice());
		return toAttachVolumeResponse( engine.attachVolume( request ));
	}
	
	public AuthorizeSecurityGroupIngressResponse authorizeSecurityGroupIngress(AuthorizeSecurityGroupIngress authorizeSecurityGroupIngress) {
        AuthorizeSecurityGroupIngressType sgit = authorizeSecurityGroupIngress.getAuthorizeSecurityGroupIngress();        
        IpPermissionSetType ipPerms = sgit.getIpPermissions();
        
        EC2AuthorizeRevokeSecurityGroup request = toSecurityGroup( sgit.getGroupName(), ipPerms.getItem());
		return toAuthorizeSecurityGroupIngressResponse( engine.authorizeSecurityGroup( request ));
	}

	
	public RevokeSecurityGroupIngressResponse revokeSecurityGroupIngress( RevokeSecurityGroupIngress revokeSecurityGroupIngress ) 
	{
        RevokeSecurityGroupIngressType sgit = revokeSecurityGroupIngress.getRevokeSecurityGroupIngress();        
        IpPermissionSetType ipPerms = sgit.getIpPermissions();
        
        EC2AuthorizeRevokeSecurityGroup request = toSecurityGroup( sgit.getGroupName(), ipPerms.getItem());
		return toRevokeSecurityGroupIngressResponse( engine.revokeSecurityGroup( request ));
	}

	
	/**
	 * Authorize and Revoke Security Group Ingress have the same parameters.
	 */
	private EC2AuthorizeRevokeSecurityGroup toSecurityGroup( String groupName, IpPermissionType[] items ) {
        EC2AuthorizeRevokeSecurityGroup request = new  EC2AuthorizeRevokeSecurityGroup();

        request.setName( groupName );
        
        for (IpPermissionType ipPerm : items) {
    	   EC2IpPermission perm = new EC2IpPermission();       	
    	   perm.setProtocol( ipPerm.getIpProtocol());
           if (ipPerm.getIpProtocol().equalsIgnoreCase("icmp")) {
               perm.setIcmpType( Integer.toString(ipPerm.getFromPort()));
               perm.setIcmpCode( Integer.toString(ipPerm.getToPort()));
           } else {
               perm.setFromPort( ipPerm.getFromPort());
               perm.setToPort( ipPerm.getToPort());
           }
    	   UserIdGroupPairSetType groups = ipPerm.getGroups();
    	   if (null != groups && groups.getItem() != null) {
    		   UserIdGroupPairType[] groupItems = groups.getItem();
    		   for (UserIdGroupPairType groupPair : groupItems) {
    			  EC2SecurityGroup user = new EC2SecurityGroup();
    			  user.setName( groupPair.getGroupName());
    			  user.setAccount( groupPair.getUserId());
    			  perm.addUser( user );
    		   }    		
    	   }     	
   
    	   IpRangeSetType ranges = ipPerm.getIpRanges();
    	   if (ranges != null && ranges.getItem() != null) {
    		   IpRangeItemType[] rangeItems = ranges.getItem();
                for (IpRangeItemType ipRange: rangeItems) {
                    perm.addIpRange( ipRange.getCidrIp() );
                    perm.setCIDR(ipRange.getCidrIp());
                }
    	   }  
   
    	   request.addIpPermission( perm );
        }
        return request;
    }

	public CreateImageResponse createImage(CreateImage createImage) {
		EC2CreateImage request = new EC2CreateImage();
		CreateImageType cit = createImage.getCreateImage();
		
		request.setInstanceId( cit.getInstanceId());
		request.setName( cit.getName());
		request.setDescription( cit.getDescription());
		return toCreateImageResponse( engine.createImage(request));
	}

	public CreateSecurityGroupResponse createSecurityGroup(CreateSecurityGroup createSecurityGroup) {
        CreateSecurityGroupType sgt = createSecurityGroup.getCreateSecurityGroup();
        
		return toCreateSecurityGroupResponse( engine.createSecurityGroup(sgt.getGroupName(), sgt.getGroupDescription()));
	}

	public CreateSnapshotResponse createSnapshot(CreateSnapshot createSnapshot) {
		CreateSnapshotType cst = createSnapshot.getCreateSnapshot();
		return toCreateSnapshotResponse( engine.createSnapshot( cst.getVolumeId()), engine);
	}

	public CreateVolumeResponse createVolume(CreateVolume createVolume) {
		EC2CreateVolume request = new EC2CreateVolume();
		CreateVolumeType cvt = createVolume.getCreateVolume();
		
		request.setSize( cvt.getSize());
		request.setSnapshotId(cvt.getSnapshotId() != null ? cvt.getSnapshotId() : null);
		request.setZoneName( cvt.getAvailabilityZone());
		return toCreateVolumeResponse( engine.createVolume( request ));
	}

    public CreateTagsResponse createTags(CreateTags createTags) {
        EC2Tags request = new EC2Tags();
        ArrayList<String> resourceIdList = new ArrayList<String>();
        Map<String, String> resourceTagList = new HashMap<String, String>();

        CreateTagsType ctt = createTags.getCreateTags();
        ResourceIdSetType resourceIds = ctt.getResourcesSet();
        ResourceTagSetType resourceTags = ctt.getTagSet();

        ResourceIdSetItemType[] resourceIdItems = resourceIds.getItem();
        if (resourceIdItems != null) {
            for( int i=0; i < resourceIdItems.length; i++ )
            	resourceIdList.add(resourceIdItems[i].getResourceId());
        }
        request = toResourceTypeAndIds(request, resourceIdList);

        //add resource tag's to the request
        ResourceTagSetItemType[] resourceTagItems = resourceTags.getItem();
        if (resourceTagItems != null) {
            for( int i=0; i < resourceTagItems.length; i++ )
              	resourceTagList.put(resourceTagItems[i].getKey(), resourceTagItems[i].getValue());
        }
        request = toResourceTag(request, resourceTagList);

        return toCreateTagsResponse( engine.modifyTags( request, "create"));
    }

    public DeleteTagsResponse deleteTags(DeleteTags deleteTags) {
        EC2Tags request = new EC2Tags();
        ArrayList<String> resourceIdList = new ArrayList<String>();
        Map<String, String> resourceTagList = new HashMap<String, String>();

        DeleteTagsType dtt = deleteTags.getDeleteTags();
        ResourceIdSetType resourceIds = dtt.getResourcesSet();
        DeleteTagsSetType resourceTags = dtt.getTagSet();

        ResourceIdSetItemType[] resourceIdItems = resourceIds.getItem();

        if (resourceIdItems != null) {
            for( int i=0; i < resourceIdItems.length; i++ )
            	resourceIdList.add(resourceIdItems[i].getResourceId());
        }
        request = toResourceTypeAndIds(request, resourceIdList);

        //add resource tag's to the request
        DeleteTagsSetItemType[] resourceTagItems = resourceTags.getItem();
        if (resourceTagItems != null) {
            for( int i=0; i < resourceTagItems.length; i++ )
            	resourceTagList.put(resourceTagItems[i].getKey(), resourceTagItems[i].getValue());
        }
        request = toResourceTag(request, resourceTagList);

        return toDeleteTagsResponse( engine.modifyTags( request, "delete"));
    }

    public static EC2Tags toResourceTypeAndIds( EC2Tags request, ArrayList<String> resourceIdList ) {
        List<String> resourceTypeList = new ArrayList<String>();
        for (String resourceId : resourceIdList) {
            if (!resourceId.contains(":") || resourceId.split(":").length != 2) {
                throw new EC2ServiceException( ClientError.InvalidResourceId_Format,
                        "Invalid Format. ResourceId format is resource-type:resource-uuid");
            }
            String resourceType = resourceId.split(":")[0];
            if (resourceTypeList.isEmpty())
                resourceTypeList.add(resourceType);
            else {
                Boolean existsInList = false;
                for (String addedResourceType : resourceTypeList) {
                    if (addedResourceType.equalsIgnoreCase(resourceType)) {
                        existsInList = true;
                        break;
                    }
                }
                if (!existsInList)
                	resourceTypeList.add(resourceType);
            }
        }
        for (String resourceType : resourceTypeList){
            EC2TagTypeId param1 = new EC2TagTypeId();
            param1.setResourceType(resourceType);
            for (String resourceId : resourceIdList) {
            	String[] resourceTag = resourceId.split(":");
            	if (resourceType.equals(resourceTag[0]))
            		param1.addResourceId(resourceTag[1]);
            }
            request.addResourceType(param1);
        }
        return request;
    }

    public static EC2Tags toResourceTag( EC2Tags request, Map<String, String> resourceTagList ) {
        Set<String> resourceTagKeySet = resourceTagList.keySet();
        for (String resourceTagKey : resourceTagKeySet) {
            EC2TagKeyValue param1 = new EC2TagKeyValue();
            param1.setKey(resourceTagKey);
            param1.setValue(resourceTagList.get(resourceTagKey));
            request.addResourceTag(param1);
        }
        return request;
    }

	public DeleteSecurityGroupResponse deleteSecurityGroup(DeleteSecurityGroup deleteSecurityGroup) {
        DeleteSecurityGroupType sgt = deleteSecurityGroup.getDeleteSecurityGroup();
		return toDeleteSecurityGroupResponse( engine.deleteSecurityGroup( sgt.getGroupName()));
	}

	public DeleteSnapshotResponse deleteSnapshot(DeleteSnapshot deleteSnapshot) {
		DeleteSnapshotType dst = deleteSnapshot.getDeleteSnapshot();		
		return toDeleteSnapshotResponse( engine.deleteSnapshot( dst.getSnapshotId()));
	}

	public DeleteVolumeResponse deleteVolume(DeleteVolume deleteVolume) {
		EC2Volume request = new EC2Volume();
		DeleteVolumeType avt = deleteVolume.getDeleteVolume();
		
		request.setId(avt.getVolumeId());
		return toDeleteVolumeResponse( engine.deleteVolume( request ));
	}

	public DeregisterImageResponse deregisterImage(DeregisterImage deregisterImage) {
		DeregisterImageType dit = deregisterImage.getDeregisterImage();
		EC2Image image = new EC2Image();
		
		image.setId( dit.getImageId());
		return toDeregisterImageResponse( engine.deregisterImage( image ));
	}

	public DescribeAvailabilityZonesResponse describeAvailabilityZones(DescribeAvailabilityZones describeAvailabilityZones) {
		EC2DescribeAvailabilityZones request = new EC2DescribeAvailabilityZones();
		
		DescribeAvailabilityZonesType dazt = describeAvailabilityZones.getDescribeAvailabilityZones();
		DescribeAvailabilityZonesSetType dazs = dazt.getAvailabilityZoneSet();
		DescribeAvailabilityZonesSetItemType[] items = dazs.getItem();
		if (null != items) {  // -> can be empty
			for( int i=0; i < items.length; i++ ) request.addZone( items[i].getZoneName());
		}

        FilterSetType fst = dazt.getFilterSet();
        if (fst != null) {
            request.setFilterSet( toAvailabiltyZonesFilterSet(fst));
        }

		return toDescribeAvailabilityZonesResponse( engine.handleRequest( request ));
	}

	/**
	 * This only supports a query about description.
	 */
    public DescribeImageAttributeResponse describeImageAttribute(DescribeImageAttribute describeImageAttribute) {
        EC2DescribeImageAttribute request = new EC2DescribeImageAttribute();
        DescribeImageAttributeType diat = describeImageAttribute.getDescribeImageAttribute();
        DescribeImageAttributesGroup diag = diat.getDescribeImageAttributesGroup();
        EmptyElementType description = diag.getDescription();
        EmptyElementType launchPermission = diag.getLaunchPermission();

        if ( null != description ) {
             request.setImageId(diat.getImageId());
             request.setAttribute(ImageAttribute.description);
             return toDescribeImageAttributeResponse( engine.describeImageAttribute( request ));
        }else if(launchPermission != null){
           request.setImageId(diat.getImageId());
           request.setAttribute(ImageAttribute.launchPermission);
           return toDescribeImageAttributeResponse( engine.describeImageAttribute( request ));
        }
        else throw new EC2ServiceException( ClientError.Unsupported, "Unsupported - only description or launchPermission supported" );
    }


	public DescribeImagesResponse describeImages(DescribeImages describeImages) {
		EC2DescribeImages  request = new EC2DescribeImages();
		DescribeImagesType dit     = describeImages.getDescribeImages();
		
		// -> toEC2DescribeImages
	    DescribeImagesExecutableBySetType param1 = dit.getExecutableBySet();
	    if (null != param1) {
	        DescribeImagesExecutableByType[] items1  = param1.getItem();
	        if (null != items1) { 
		        for( int i=0; i < items1.length; i++ ) request.addExecutableBySet( items1[i].getUser());
	        }
	    }
		DescribeImagesInfoType param2 = dit.getImagesSet();
		if (null != param2) {
		    DescribeImagesItemType[] items2 = param2.getItem();
		    if (null != items2) {  
		        for( int i=0; i < items2.length; i++ ) request.addImageSet( items2[i].getImageId());
		    }
		}
		DescribeImagesOwnersType param3 = dit.getOwnersSet();
		if (null != param3) {
		    DescribeImagesOwnerType[] items3 = param3.getItem();
		    if (null != items3) {  
			    for( int i=0; i < items3.length; i++ ) request.addOwnersSet( items3[i].getOwner());
		    }
		}    

		return toDescribeImagesResponse( engine.describeImages( request ));
	}

	public DescribeInstanceAttributeResponse describeInstanceAttribute(DescribeInstanceAttribute describeInstanceAttribute) {
	    EC2DescribeInstances  request = new EC2DescribeInstances();
	    DescribeInstanceAttributeType diat = describeInstanceAttribute.getDescribeInstanceAttribute();
	    DescribeInstanceAttributesGroup diag = diat.getDescribeInstanceAttributesGroup();
	    EmptyElementType instanceType = diag.getInstanceType();
		
	    // -> toEC2DescribeInstances
	    if (null != instanceType) {
		    request.addInstanceId( diat.getInstanceId());
		    return toDescribeInstanceAttributeResponse( engine.describeInstances( request ));
	    }
	    throw new EC2ServiceException( ClientError.Unsupported, "Unsupported - only instanceType supported");
	}

	
	public DescribeInstancesResponse describeInstances( DescribeInstances describeInstances ) 
	{
		EC2DescribeInstances  request = new EC2DescribeInstances();
		DescribeInstancesType dit     = describeInstances.getDescribeInstances();
		FilterSetType fst = dit.getFilterSet();

		// -> toEC2DescribeInstances
		DescribeInstancesInfoType   diit  = dit.getInstancesSet();
		DescribeInstancesItemType[] items = diit.getItem();
		if (null != items) {  // -> can be empty
			for( int i=0; i < items.length; i++ ) request.addInstanceId( items[i].getInstanceId());
		}
		
		if (null != fst) {
			request.setFilterSet( toInstanceFilterSet( fst ));
		}
		
		return toDescribeInstancesResponse( engine.describeInstances( request ), engine );
	}

	
    @Override
    public DescribeAddressesResponse describeAddresses(DescribeAddresses describeAddresses) {
        EC2DescribeAddresses ec2Request = new EC2DescribeAddresses();
        DescribeAddressesType dat = describeAddresses.getDescribeAddresses();
        
        DescribeAddressesInfoType dait = dat.getPublicIpsSet();
        DescribeAddressesItemType[] items = dait.getItem();
        if (items != null) {  // -> can be empty
        	for (DescribeAddressesItemType itemType : items) 
        		ec2Request.addPublicIp( itemType.getPublicIp());
        }

        FilterSetType fset = dat.getFilterSet();
        if (fset != null) {
        	ec2Request.setFilterSet(toAddressFilterSet(fset));
        }
        
        return toDescribeAddressesResponse( engine.describeAddresses( ec2Request ));
    }

    @Override
    public AllocateAddressResponse allocateAddress(AllocateAddress allocateAddress) {
    	return toAllocateAddressResponse( engine.allocateAddress());
    }

    @Override
    public ReleaseAddressResponse releaseAddress(ReleaseAddress releaseAddress) {
    	EC2ReleaseAddress request = new EC2ReleaseAddress();
    	
    	request.setPublicIp(releaseAddress.getReleaseAddress().getPublicIp());
    	
        return toReleaseAddressResponse( engine.releaseAddress( request ) );
    }

    @Override
    public AssociateAddressResponse associateAddress(AssociateAddress associateAddress) {
    	EC2AssociateAddress request = new EC2AssociateAddress();
    	
    	request.setPublicIp(associateAddress.getAssociateAddress().getPublicIp());
    	request.setInstanceId(associateAddress.getAssociateAddress().getInstanceId());
    	
        return toAssociateAddressResponse( engine.associateAddress( request ) );
    }

    @Override
    public DisassociateAddressResponse disassociateAddress(DisassociateAddress disassociateAddress) {
    	EC2DisassociateAddress request = new EC2DisassociateAddress();
    	
    	request.setPublicIp(disassociateAddress.getDisassociateAddress().getPublicIp());
    	
        return toDisassociateAddressResponse( engine.disassociateAddress( request ) );
    }
    
	public DescribeSecurityGroupsResponse describeSecurityGroups(DescribeSecurityGroups describeSecurityGroups) 
	{
	    EC2DescribeSecurityGroups request = new EC2DescribeSecurityGroups();
	    
	    DescribeSecurityGroupsType sgt = describeSecurityGroups.getDescribeSecurityGroups();
        
		FilterSetType fst = sgt.getFilterSet();

		// -> toEC2DescribeSecurityGroups
        DescribeSecurityGroupsSetType sgst = sgt.getSecurityGroupSet();
        DescribeSecurityGroupsSetItemType[] items = sgst.getItem();
		if (null != items) {  // -> can be empty
			for (DescribeSecurityGroupsSetItemType item :items) request.addGroupName(item.getGroupName());
		}
		
		if (null != fst) {
			request.setFilterSet( toGroupFilterSet( fst ));
		}
		

		return toDescribeSecurityGroupsResponse( engine.describeSecurityGroups( request ));
	}

	public DescribeSnapshotsResponse describeSnapshots(DescribeSnapshots describeSnapshots) 
	{
		EC2DescribeSnapshots request = new EC2DescribeSnapshots();
		DescribeSnapshotsType dst = describeSnapshots.getDescribeSnapshots();
	
		DescribeSnapshotsSetType dsst = dst.getSnapshotSet();
		FilterSetType fst = dst.getFilterSet();

		if (null != dsst) 
		{
			DescribeSnapshotsSetItemType[] items = dsst.getItem();
            if (null != items) {
			    for( int i=0; i < items.length; i++ ) request.addSnapshotId( items[i].getSnapshotId());
            }
		}
		
		if (null != fst) 
		{
			String[] timeFilters = new String[1];
			timeFilters[0] = new String( "start-time" );
			request.setFilterSet( toSnapshotFilterSet( fst, timeFilters ));
		}

		return toDescribeSnapshotsResponse(engine.handleRequest(request));
	}

    public DescribeTagsResponse describeTags(DescribeTags decsribeTags) {
        EC2DescribeTags request = new EC2DescribeTags();
        DescribeTagsType dtt = decsribeTags.getDescribeTags();

        FilterSetType fst = dtt.getFilterSet();

        if (fst != null)
            request.setFilterSet( toTagsFilterSet( fst ));

        return toDescribeTagsResponse(engine.describeTags(request));
    }

	public DescribeVolumesResponse describeVolumes(DescribeVolumes describeVolumes) 
	{
		EC2DescribeVolumes request = new EC2DescribeVolumes();
		DescribeVolumesType dvt = describeVolumes.getDescribeVolumes();
		
		DescribeVolumesSetType dvst = dvt.getVolumeSet();
		FilterSetType fst = dvt.getFilterSet();
		
		if (null != dvst) 
		{
		    DescribeVolumesSetItemType[] items = dvst.getItem();
		    if (null != items) {
		    	for( int i=0; i < items.length; i++ ) request.addVolumeId( items[i].getVolumeId());
		    }
		}	
		
		if (null != fst) 
		{
			String[] timeFilters = new String[2];
			timeFilters[0] = new String( "attachment.attach-time" );
			timeFilters[1] = new String( "create-time"            );
			request.setFilterSet( toVolumeFilterSet( fst, timeFilters ));
		}
		
		return toDescribeVolumesResponse( engine.handleRequest( request ));
	}
	
	public DetachVolumeResponse detachVolume(DetachVolume detachVolume) {
		EC2Volume request = new EC2Volume();
		DetachVolumeType avt = detachVolume.getDetachVolume();
		
		request.setId(avt.getVolumeId());
		request.setInstanceId(avt.getInstanceId());
		request.setDevice( avt.getDevice());
		return toDetachVolumeResponse( engine.detachVolume( request ));
	}

	public ModifyImageAttributeResponse modifyImageAttribute(ModifyImageAttribute modifyImageAttribute) {
		EC2ModifyImageAttribute request = new EC2ModifyImageAttribute();
		
		ModifyImageAttributeType miat = modifyImageAttribute.getModifyImageAttribute();
		ModifyImageAttributeTypeChoice_type0 item = miat.getModifyImageAttributeTypeChoice_type0();

		AttributeValueType description = item.getDescription();
		
		LaunchPermissionOperationType launchPermOp = item.getLaunchPermission();
		
		if (null != description) {
            request.setImageId(miat.getImageId());
            request.setAttribute(ImageAttribute.description);
		    request.setDescription(description.getValue());
		    return toModifyImageAttributeResponse( engine.modifyImageAttribute( request ));
		}else if(launchPermOp != null){
            request.setImageId(miat.getImageId());
            request.setAttribute(ImageAttribute.launchPermission);
            if(launchPermOp.getAdd() != null){
                setAccountOrGroupList(launchPermOp.getAdd().getItem(), request, "add");
            }else if(launchPermOp.getRemove() != null){
                setAccountOrGroupList(launchPermOp.getRemove().getItem(), request, "remove");
            }
            return toModifyImageAttributeResponse( engine.modifyImageAttribute( request ));
		}
		throw new EC2ServiceException( ClientError.Unsupported, "Unsupported - can only modify image description or launchPermission");
	}	

    private void setAccountOrGroupList(LaunchPermissionItemType[] items, EC2ModifyImageAttribute request, String operation){
        EC2ImageLaunchPermission launchPermission = new EC2ImageLaunchPermission();

        if (operation.equalsIgnoreCase("add"))
            launchPermission.setLaunchPermOp(EC2ImageLaunchPermission.Operation.add);
        else
            launchPermission.setLaunchPermOp(EC2ImageLaunchPermission.Operation.remove);

        for (LaunchPermissionItemType lpItem : items) {
            if(lpItem.getGroup() != null){
                launchPermission.addLaunchPermission(lpItem.getGroup());
            }else if(lpItem.getUserId() != null){
                launchPermission.addLaunchPermission(lpItem.getUserId());
            }
        }

        request.addLaunchPermission(launchPermission);
	}
	/**
	 * Did not find a matching service offering so for now we just return disabled
	 * for each instance request.  We could verify that all of the specified instances
	 * exist to detect an error which would require a listVirtualMachines.
	 */
	public MonitorInstancesResponse monitorInstances(MonitorInstances monitorInstances) {
		MonitorInstancesResponse response = new MonitorInstancesResponse();
		MonitorInstancesResponseType param1 = new MonitorInstancesResponseType();
		MonitorInstancesResponseSetType param2 = new MonitorInstancesResponseSetType();
 		
		MonitorInstancesType mit = monitorInstances.getMonitorInstances();
		MonitorInstancesSetType mist = mit.getInstancesSet();
		MonitorInstancesSetItemType[] misit = mist.getItem();
		
		if (null != misit) {  
			for( int i=0; i < misit.length; i++ ) {
				String instanceId = misit[i].getInstanceId();
				MonitorInstancesResponseSetItemType param3 = new MonitorInstancesResponseSetItemType();	
				param3.setInstanceId( instanceId );
				InstanceMonitoringStateType param4 = new InstanceMonitoringStateType();
				param4.setState( "disabled" );
				param3.setMonitoring( param4 );
				param2.addItem( param3 );
			}
		}

		param1.setRequestId( UUID.randomUUID().toString());
        param1.setInstancesSet( param2 );
		response.setMonitorInstancesResponse( param1 );
		return response;
	}

	public RebootInstancesResponse rebootInstances(RebootInstances rebootInstances) {
		EC2RebootInstances request = new EC2RebootInstances();
		RebootInstancesType rit = rebootInstances.getRebootInstances();
		
		// -> toEC2StartInstances
		RebootInstancesInfoType   rist  = rit.getInstancesSet();
		RebootInstancesItemType[] items = rist.getItem();
		if (null != items) {  // -> should not be empty
			for( int i=0; i < items.length; i++ ) request.addInstanceId( items[i].getInstanceId());
		}
		return toRebootInstancesResponse( engine.rebootInstances( request ));
	}

	
	/**
	 * Processes ec2-register
	 * 
	 * @param
	 * 
	 * @see <a href="http://docs.amazonwebservices.com/AWSEC2/2010-11-15/APIReference/index.html?ApiReference-query-RegisterImage.html">RegisterImage</a>
	 */
	public RegisterImageResponse registerImage(RegisterImage registerImage) {
		EC2RegisterImage request = new EC2RegisterImage();
		RegisterImageType rit = registerImage.getRegisterImage();
		
		// -> we redefine the architecture field to hold: "format:zonename:osTypeName",
		//    these are the bare minimum that we need to call the cloud registerTemplate call.
		request.setLocation( rit.getImageLocation());   // -> should be a URL for us
		request.setName( rit.getName());
		request.setDescription( rit.getDescription());
		request.setArchitecture( rit.getArchitecture());  
		return toRegisterImageResponse( engine.registerImage( request ));
	}
	
	/**
	 * Processes ec2-reset-image-attribute
	 * 
	 * @param resetImageAttribute
	 * 
	 * @see <a href="http://docs.amazonwebservices.com/AWSEC2/2010-11-15/APIReference/index.html?ApiReference-query-ResetInstanceAttribute.html">ResetInstanceAttribute</a>
	 */

	public ResetImageAttributeResponse resetImageAttribute(ResetImageAttribute resetImageAttribute) {
	    EC2ModifyImageAttribute request = new EC2ModifyImageAttribute();
		ResetImageAttributeType riat = resetImageAttribute.getResetImageAttribute();
		EmptyElementType elementType = riat.getResetImageAttributesGroup().getLaunchPermission();
		if(elementType != null){
		    request.setImageId( riat.getImageId());
		    request.setAttribute(ImageAttribute.launchPermission);
            EC2ImageLaunchPermission launchPermission = new EC2ImageLaunchPermission();
            launchPermission.setLaunchPermOp(EC2ImageLaunchPermission.Operation.reset);
            request.addLaunchPermission(launchPermission);
    		return toResetImageAttributeResponse( engine.modifyImageAttribute( request ));
		}
		throw new EC2ServiceException( ClientError.Unsupported, "Unsupported - can only reset image launchPermission" );
	}
	
	/**
	 *  ec2-run-instances
	 *	
	 * @param runInstances
	 * 
	 * @see <a href="http://docs.amazonwebservices.com/AWSEC2/2010-11-15/APIReference/index.html?ApiReference-query-RunInstances.html">RunInstances</a>
	 */
	public RunInstancesResponse runInstances(RunInstances runInstances) {
		RunInstancesType rit = runInstances.getRunInstances();
		GroupSetType gst = rit.getGroupSet();
		PlacementRequestType prt = rit.getPlacement();
		UserDataType userData = rit.getUserData();
		String type = rit.getInstanceType();	
		String keyName = rit.getKeyName();
		
		EC2RunInstances request = new EC2RunInstances();
		
		request.setTemplateId(rit.getImageId());
		request.setMinCount(rit.getMinCount());
		request.setMaxCount(rit.getMaxCount());
		if (null != type) request.setInstanceType(type);
		if (null != prt) request.setZoneName(prt.getAvailabilityZone());
		if (null != userData) request.setUserData(userData.getData());
		if (null != keyName) request.setKeyName(rit.getKeyName() );
		
		// -> we can only support one group per instance
		if (null != gst) {
			GroupItemType[] items = gst.getItem();
			if (null != items) {
				for( int i=0; i < items.length; i++ ) request.addGroupName(items[i].getGroupId());
		    }
		}
		return toRunInstancesResponse( engine.runInstances( request ), engine);
	}
	
	public StartInstancesResponse startInstances(StartInstances startInstances) {
		EC2StartInstances request = new EC2StartInstances();
		StartInstancesType sit = startInstances.getStartInstances();
		
		// -> toEC2StartInstances
		InstanceIdSetType iist  = sit.getInstancesSet();
		InstanceIdType[]  items = iist.getItem();
		if (null != items) {  // -> should not be empty
			for( int i=0; i < items.length; i++ ) request.addInstanceId( items[i].getInstanceId());
		}
		return toStartInstancesResponse( engine.startInstances( request ));
	}
	
	public StopInstancesResponse stopInstances(StopInstances stopInstances) {
		EC2StopInstances request = new EC2StopInstances();
		StopInstancesType sit = stopInstances.getStopInstances();
		
		// -> toEC2StopInstances
		InstanceIdSetType iist  = sit.getInstancesSet();
		InstanceIdType[]  items = iist.getItem();
		if (null != items) {  // -> should not be empty
			for( int i=0; i < items.length; i++ ) request.addInstanceId( items[i].getInstanceId());
		}
		return toStopInstancesResponse( engine.stopInstances( request ));
	}
	
	/**
	 * Mapping this to the destroyVirtualMachine cloud API concept.
	 * This makes sense since when considering the rebootInstances function.   In reboot
	 * any terminated instances are left alone.   We will do the same with destroyed instances.
	 */
	public TerminateInstancesResponse terminateInstances(TerminateInstances terminateInstances) {
		EC2StopInstances request = new EC2StopInstances();
		TerminateInstancesType sit = terminateInstances.getTerminateInstances();
		
		// -> toEC2StopInstances
		InstanceIdSetType iist  = sit.getInstancesSet();
		InstanceIdType[]  items = iist.getItem();
		if (null != items) {  // -> should not be empty
			for( int i=0; i < items.length; i++ ) request.addInstanceId( items[i].getInstanceId());
		}

		request.setDestroyInstances( true );
		return toTermInstancesResponse( engine.stopInstances( request ));
	}
	
	/**
	 * See comment for monitorInstances.
	 */
	public UnmonitorInstancesResponse unmonitorInstances(UnmonitorInstances unmonitorInstances) {
		UnmonitorInstancesResponse response = new UnmonitorInstancesResponse();
		MonitorInstancesResponseType param1 = new MonitorInstancesResponseType();
		MonitorInstancesResponseSetType param2 = new MonitorInstancesResponseSetType();
 		
		MonitorInstancesType mit = unmonitorInstances.getUnmonitorInstances();
		MonitorInstancesSetType mist = mit.getInstancesSet();
		MonitorInstancesSetItemType[] items = mist.getItem();
		
		if (null != items) {  
			for( int i=0; i < items.length; i++ ) {
				String instanceId = items[i].getInstanceId();
				MonitorInstancesResponseSetItemType param3 = new MonitorInstancesResponseSetItemType();	
				param3.setInstanceId( instanceId );
				InstanceMonitoringStateType param4 = new InstanceMonitoringStateType();
				param4.setState( "disabled" );
				param3.setMonitoring( param4 );
				param2.addItem( param3 );
			}
		}

        param1.setInstancesSet( param2 );
		param1.setRequestId( UUID.randomUUID().toString());
		response.setUnmonitorInstancesResponse( param1 );
		return response;
	}
	
	
	public static DescribeImageAttributeResponse toDescribeImageAttributeResponse(EC2DescribeImagesResponse engineResponse) {
		DescribeImageAttributeResponse response = new DescribeImageAttributeResponse();
		DescribeImageAttributeResponseType param1 = new DescribeImageAttributeResponseType();
		
		EC2Image[] imageSet = engineResponse.getImageSet();
		if ( 0 < imageSet.length ) {
		     DescribeImageAttributeResponseTypeChoice_type0 param2 = new DescribeImageAttributeResponseTypeChoice_type0();
		     NullableAttributeValueType param3 = new NullableAttributeValueType();
		     param3.setValue( imageSet[0].getDescription());
		     param2.setDescription( param3 );
		     param1.setDescribeImageAttributeResponseTypeChoice_type0( param2 );
		     param1.setImageId( imageSet[0].getId());	
		}
		
		param1.setRequestId( UUID.randomUUID().toString());
        response.setDescribeImageAttributeResponse( param1 );
		return response;
	}
	
	public static DescribeImageAttributeResponse toDescribeImageAttributeResponse(EC2ImageAttributes engineResponse) {
       DescribeImageAttributeResponse response = new DescribeImageAttributeResponse();
       DescribeImageAttributeResponseType param1 = new DescribeImageAttributeResponseType();
       
       if (engineResponse != null ) {
            DescribeImageAttributeResponseTypeChoice_type0 param2 = new DescribeImageAttributeResponseTypeChoice_type0();

            if(engineResponse.getIsPublic()){
                LaunchPermissionListType param3 = new LaunchPermissionListType();
                LaunchPermissionItemType param4 = new LaunchPermissionItemType();
                param4.setGroup("all");
                param3.addItem(param4);
                param2.setLaunchPermission(param3);
            }else if(engineResponse.getAccountNamesWithLaunchPermission() != null){
                LaunchPermissionListType param3 = new LaunchPermissionListType();
                for(String accountName : engineResponse.getAccountNamesWithLaunchPermission()){
                    LaunchPermissionItemType param4 = new LaunchPermissionItemType();
                    param4.setUserId(accountName);
                    param3.addItem(param4);
                }
                param2.setLaunchPermission(param3);
                
            }else if(engineResponse.getDescription() != null){
                NullableAttributeValueType param3 = new NullableAttributeValueType();
                param3.setValue( engineResponse.getDescription());
                param2.setDescription( param3 );
            }
            
            
            param1.setDescribeImageAttributeResponseTypeChoice_type0( param2 );
            param1.setImageId(engineResponse.getImageId());   
       }
       
       param1.setRequestId( UUID.randomUUID().toString());
       response.setDescribeImageAttributeResponse( param1 );
       return response;
	}
	   
	
	public static ModifyImageAttributeResponse toModifyImageAttributeResponse( boolean engineResponse ) {
		ModifyImageAttributeResponse response = new ModifyImageAttributeResponse();
		ModifyImageAttributeResponseType param1 = new ModifyImageAttributeResponseType();
		
		param1.set_return( engineResponse );
		param1.setRequestId( UUID.randomUUID().toString());
        response.setModifyImageAttributeResponse( param1 );
		return response;
	}
	
	public static ResetImageAttributeResponse toResetImageAttributeResponse( boolean engineResponse ) {
		ResetImageAttributeResponse response = new ResetImageAttributeResponse();
		ResetImageAttributeResponseType param1 = new ResetImageAttributeResponseType();
		
		param1.set_return( engineResponse );
		param1.setRequestId( UUID.randomUUID().toString());
        response.setResetImageAttributeResponse( param1 );
		return response;		
	}
	
	public static DescribeImagesResponse toDescribeImagesResponse(EC2DescribeImagesResponse engineResponse ) {
		DescribeImagesResponse response = new DescribeImagesResponse();
		DescribeImagesResponseType param1 = new DescribeImagesResponseType();
		DescribeImagesResponseInfoType param2 = new DescribeImagesResponseInfoType();
		
		EC2Image[] images = engineResponse.getImageSet();
 	    for( int i=0; i < images.length; i++ ) {
 	    	String accountName = images[i].getAccountName();
			String domainId = images[i].getDomainId();
			String ownerId = domainId + ":" + accountName;
			
		    DescribeImagesResponseItemType param3 = new DescribeImagesResponseItemType();
		    param3.setImageId( images[i].getId());
		    param3.setImageLocation( "" );
		    param3.setImageState( (images[i].getIsReady() ? "available" : "unavailable" ));
		    param3.setImageOwnerId(ownerId);    
		    param3.setIsPublic( images[i].getIsPublic());

		    ProductCodesSetType param4 = new ProductCodesSetType();
	        ProductCodesSetItemType param5 = new ProductCodesSetItemType();
	        param5.setProductCode( "" );
            param4.addItem( param5 );		    
		    param3.setProductCodes( param4 );
		    
		    String description = images[i].getDescription();
		    param3.setDescription( (null == description ? "" : description));
		    
		         if (null == description) param3.setArchitecture( "" );
			else if (-1 != description.indexOf( "x86_64" )) param3.setArchitecture( "x86_64" );
			else if (-1 != description.indexOf( "i386"   )) param3.setArchitecture( "i386" );
			else param3.setArchitecture( "" );
		         
			param3.setImageType( "machine" );
		    param3.setKernelId( "" );
		    param3.setRamdiskId( "" );
		    param3.setPlatform( "" );
		    
		    StateReasonType param6 = new StateReasonType();
	        param6.setCode( "" );
	        param6.setMessage( "" );
            param3.setStateReason( param6 );
            
		    param3.setImageOwnerAlias( "" );
		    param3.setName( images[i].getName());
		    param3.setRootDeviceType( "" );
		    param3.setRootDeviceName( "" );
		    
		    BlockDeviceMappingType param7 = new BlockDeviceMappingType();
		    BlockDeviceMappingItemType param8 = new BlockDeviceMappingItemType();
		    BlockDeviceMappingItemTypeChoice_type0 param9 = new BlockDeviceMappingItemTypeChoice_type0();
		    param8.setDeviceName( "" );
		    param9.setVirtualName( "" );
		    EbsBlockDeviceType param10 = new EbsBlockDeviceType();
		    param10.setSnapshotId( "" );
		    param10.setVolumeSize( 0 );
		    param10.setDeleteOnTermination( false );
		    param9.setEbs( param10 );
		    param8.setBlockDeviceMappingItemTypeChoice_type0( param9 );
            param7.addItem( param8 );

            param3.setBlockDeviceMapping( param7 );

            EC2TagKeyValue[] tags = images[i].getResourceTags();
            param3.setTagSet(setResourceTags(tags));

            param2.addItem( param3 );
		}

		param1.setImagesSet( param2 );
		param1.setRequestId( UUID.randomUUID().toString());
		response.setDescribeImagesResponse( param1 );
		return response;
	}
	
	public static CreateImageResponse toCreateImageResponse(EC2CreateImageResponse engineResponse) {
		CreateImageResponse response = new CreateImageResponse();
		CreateImageResponseType param1 = new CreateImageResponseType();
       
		param1.setImageId( engineResponse.getId());
		param1.setRequestId( UUID.randomUUID().toString());
		response.setCreateImageResponse( param1 );
		return response;
	}
	
	public static RegisterImageResponse toRegisterImageResponse(EC2CreateImageResponse engineResponse) {
		RegisterImageResponse response = new RegisterImageResponse();
		RegisterImageResponseType param1 = new RegisterImageResponseType();

		param1.setImageId( engineResponse.getId());
		param1.setRequestId( UUID.randomUUID().toString());
		response.setRegisterImageResponse( param1 );
		return response;
	}
	
	public static DeregisterImageResponse toDeregisterImageResponse( boolean engineResponse) {
		DeregisterImageResponse response = new DeregisterImageResponse();
		DeregisterImageResponseType param1 = new DeregisterImageResponseType();
		
		param1.set_return( engineResponse );
		param1.setRequestId( UUID.randomUUID().toString());
        response.setDeregisterImageResponse( param1 );
		return response;
	}

	// filtersets
	private EC2AddressFilterSet toAddressFilterSet( FilterSetType fst )	{
		EC2AddressFilterSet vfs = new EC2AddressFilterSet();
		
		FilterType[] items = fst.getItem();
		if (items != null) {
			// -> each filter can have one or more values associated with it
			for (FilterType item : items) {
				EC2Filter oneFilter = new EC2Filter();
				String filterName = item.getName();
				oneFilter.setName( filterName );
				
				ValueSetType vst = item.getValueSet();
				ValueType[] valueItems = vst.getItem();
				for (ValueType valueItem : valueItems) {
					oneFilter.addValueEncoded( valueItem.getValue());
				}
				vfs.addFilter( oneFilter );
			}
		}		
		return vfs;
	}
	
	private EC2KeyPairFilterSet toKeyPairFilterSet( FilterSetType fst )
	{
		EC2KeyPairFilterSet vfs = new EC2KeyPairFilterSet();
		
		FilterType[] items = fst.getItem();
		if (items != null) {
			// -> each filter can have one or more values associated with it
			for (FilterType item : items) {
				EC2Filter oneFilter = new EC2Filter();
				String filterName = item.getName();
				oneFilter.setName( filterName );
				
				ValueSetType vst = item.getValueSet();
				ValueType[] valueItems = vst.getItem();
				for (ValueType valueItem : valueItems) {
					oneFilter.addValueEncoded( valueItem.getValue());
				}
				vfs.addFilter( oneFilter );
			}
		}		
		return vfs;
	}

	
	private EC2VolumeFilterSet toVolumeFilterSet( FilterSetType fst, String[] timeStrs )
	{
		EC2VolumeFilterSet vfs = new EC2VolumeFilterSet();
		boolean timeFilter = false;
		
		FilterType[] items = fst.getItem();
		if (null != items) 
		{
			// -> each filter can have one or more values associated with it
			for( int j=0; j < items.length; j++ )
			{
				EC2Filter oneFilter = new EC2Filter();
				String filterName = items[j].getName();
				oneFilter.setName( filterName );
				
				// -> is the filter one of the xsd:dateTime filters?
				timeFilter = false;
				for( int m=0; m < timeStrs.length; m++ )
				{
					 timeFilter = filterName.equalsIgnoreCase( timeStrs[m] );
					 if (timeFilter) break;
				}
				
				ValueSetType vst = items[j].getValueSet();
				ValueType[] valueItems = vst.getItem();
				for( int k=0; k < valueItems.length; k++ ) 
				{
					// -> time values are not encoded as regexes
					if ( timeFilter )
					     oneFilter.addValue( valueItems[k].getValue());
					else oneFilter.addValueEncoded( valueItems[k].getValue());
				}
				vfs.addFilter( oneFilter );
			}
		}		
		return vfs;
	}

	
	private EC2SnapshotFilterSet toSnapshotFilterSet( FilterSetType fst, String[] timeStrs )
	{
		EC2SnapshotFilterSet vfs = new EC2SnapshotFilterSet();
		boolean timeFilter = false;
		
		FilterType[] items = fst.getItem();
		if (null != items) 
		{
			// -> each filter can have one or more values associated with it
			for( int j=0; j < items.length; j++ )
			{
				EC2Filter oneFilter = new EC2Filter();
				String filterName = items[j].getName();
				oneFilter.setName( filterName );
				
				// -> is the filter one of the xsd:dateTime filters?
				timeFilter = false;
				for( int m=0; m < timeStrs.length; m++ )
				{
					 timeFilter = filterName.equalsIgnoreCase( timeStrs[m] );
					 if (timeFilter) break;
				}
				
				ValueSetType vst = items[j].getValueSet();
				ValueType[] valueItems = vst.getItem();
				for( int k=0; k < valueItems.length; k++ ) 
				{
					// -> time values are not encoded as regexes
					if ( timeFilter )
					     oneFilter.addValue( valueItems[k].getValue());
					else oneFilter.addValueEncoded( valueItems[k].getValue());
				}
				vfs.addFilter( oneFilter );
			}
		}		
		return vfs;
	}

	
	// TODO make these filter set functions use generics 
	private EC2GroupFilterSet toGroupFilterSet( FilterSetType fst )
	{
		EC2GroupFilterSet gfs = new EC2GroupFilterSet();
		
		FilterType[] items = fst.getItem();
		if (null != items) 
		{
			// -> each filter can have one or more values associated with it
			for( int j=0; j < items.length; j++ )
			{
				EC2Filter oneFilter = new EC2Filter();
				String filterName = items[j].getName();
				oneFilter.setName( filterName );
				
				ValueSetType vst = items[j].getValueSet();
				ValueType[] valueItems = vst.getItem();
				for( int k=0; k < valueItems.length; k++ ) 
				{
					oneFilter.addValueEncoded( valueItems[k].getValue());
				}
				gfs.addFilter( oneFilter );
			}
		}		
		return gfs;
	}

	
	private EC2InstanceFilterSet toInstanceFilterSet( FilterSetType fst )
	{
		EC2InstanceFilterSet ifs = new EC2InstanceFilterSet();
		
		FilterType[] items = fst.getItem();
		if (null != items) 
		{
			// -> each filter can have one or more values associated with it
			for( int j=0; j < items.length; j++ )
			{
				EC2Filter oneFilter = new EC2Filter();
				String filterName = items[j].getName();
				oneFilter.setName( filterName );
				
				ValueSetType vst = items[j].getValueSet();
				ValueType[] valueItems = vst.getItem();
				for( int k=0; k < valueItems.length; k++ ) 
				{
					oneFilter.addValueEncoded( valueItems[k].getValue());
				}
				ifs.addFilter( oneFilter );
			}
		}		
		return ifs;
	}


    private EC2AvailabilityZonesFilterSet toAvailabiltyZonesFilterSet( FilterSetType fst )	{
        EC2AvailabilityZonesFilterSet azfs = new EC2AvailabilityZonesFilterSet();

        FilterType[] items = fst.getItem();
        if (items != null) {
            for (FilterType item : items) {
                EC2Filter oneFilter = new EC2Filter();
                String filterName = item.getName();
                oneFilter.setName( filterName );

                ValueSetType vft = item.getValueSet();
                ValueType[] valueItems = vft.getItem();
                for (ValueType valueItem : valueItems) {
                    oneFilter.addValueEncoded( valueItem.getValue());
                }
                azfs.addFilter( oneFilter );
            }
        }
        return azfs;
    }

    private EC2TagsFilterSet toTagsFilterSet( FilterSetType fst ) {
        EC2TagsFilterSet tfs = new EC2TagsFilterSet();

        FilterType[] items = fst.getItem();
        if (items != null) {
            for (FilterType item : items) {
                EC2Filter oneFilter = new EC2Filter();
                String filterName = item.getName();
                oneFilter.setName( filterName );

                ValueSetType vft = item.getValueSet();
                ValueType[] valueItems = vft.getItem();
                for (ValueType valueItem : valueItems) {
                    oneFilter.addValueEncoded( valueItem.getValue());
                }
                tfs.addFilter( oneFilter );
            }
        }
        return tfs;
    }

	// toMethods
	public static DescribeVolumesResponse toDescribeVolumesResponse( EC2DescribeVolumesResponse engineResponse ) 
	{
	    DescribeVolumesResponse      response = new DescribeVolumesResponse();
	    DescribeVolumesResponseType    param1 = new DescribeVolumesResponseType();
	    DescribeVolumesSetResponseType param2 = new DescribeVolumesSetResponseType();
        
		EC2Volume[] volumes = engineResponse.getVolumeSet();
		for (EC2Volume vol : volumes) {
			DescribeVolumesSetItemResponseType param3 = new DescribeVolumesSetItemResponseType();
	        param3.setVolumeId( vol.getId().toString());
	        
	        Long volSize = new Long(vol.getSize());
	        param3.setSize(volSize.toString());  
	        String snapId = vol.getSnapshotId() != null ? vol.getSnapshotId().toString() : "";
	        param3.setSnapshotId(snapId);
	        param3.setAvailabilityZone( vol.getZoneName());
	        param3.setStatus( vol.getState());
	        
        	// -> CloudStack seems to have issues with timestamp formats so just in case
	        Calendar cal = EC2RestAuth.parseDateString(vol.getCreated());
	        if (cal == null) {
	        	 cal = Calendar.getInstance();
	        	 cal.set( 1970, 1, 1 );
	        }
	        param3.setCreateTime( cal );
	        
	        AttachmentSetResponseType param4 = new AttachmentSetResponseType();
	        if (null != vol.getInstanceId()) {
	        	AttachmentSetItemResponseType param5 = new AttachmentSetItemResponseType();
	        	param5.setVolumeId(vol.getId().toString());
	        	param5.setInstanceId(vol.getInstanceId().toString());
	        	String devicePath = engine.cloudDeviceIdToDevicePath( vol.getHypervisor(), vol.getDeviceId());
	        	param5.setDevice( devicePath );
	        	param5.setStatus( toVolumeAttachmentState( vol.getInstanceId(), vol.getVMState()));
                if (vol.getAttached() == null) {
                    param5.setAttachTime( cal );
                } else {
                    Calendar attachTime = EC2RestAuth.parseDateString(vol.getAttached());
                    param5.setAttachTime( attachTime );
                }
	        	param5.setDeleteOnTermination( false );
                param4.addItem( param5 );
            }
	        
            param3.setAttachmentSet( param4 );

            EC2TagKeyValue[] tags = vol.getResourceTags();
            param3.setTagSet( setResourceTags(tags) );
            param2.addItem( param3 );
        }
	    param1.setVolumeSet( param2 );
	    param1.setRequestId( UUID.randomUUID().toString());
	    response.setDescribeVolumesResponse( param1 );
	    return response;
	}
	
	
	public static DescribeInstanceAttributeResponse toDescribeInstanceAttributeResponse(EC2DescribeInstancesResponse engineResponse) {
      	DescribeInstanceAttributeResponse response = new DescribeInstanceAttributeResponse();
      	DescribeInstanceAttributeResponseType param1 = new DescribeInstanceAttributeResponseType();

      	EC2Instance[] instanceSet = engineResponse.getInstanceSet();
      	if (0 < instanceSet.length) {
      		DescribeInstanceAttributeResponseTypeChoice_type0 param2 = new DescribeInstanceAttributeResponseTypeChoice_type0();
      		NullableAttributeValueType param3 = new NullableAttributeValueType();
      		param3.setValue( instanceSet[0].getServiceOffering());
      		param2.setInstanceType( param3 );
            param1.setDescribeInstanceAttributeResponseTypeChoice_type0( param2 );
      		param1.setInstanceId( instanceSet[0].getId());
      	}
	    param1.setRequestId( UUID.randomUUID().toString());
        response.setDescribeInstanceAttributeResponse( param1 );
		return response;
	}

	
	public static DescribeInstancesResponse toDescribeInstancesResponse(EC2DescribeInstancesResponse engineResponse, EC2Engine engine) 
	{
	    DescribeInstancesResponse response = new DescribeInstancesResponse();
	    DescribeInstancesResponseType param1 = new DescribeInstancesResponseType();
	    ReservationSetType param2 = new ReservationSetType();
	    
		EC2Instance[] instances = engineResponse.getInstanceSet();
		
		for (EC2Instance inst:instances) {
			String accountName = inst.getAccountName();
			String domainId = inst.getDomainId();
			String ownerId = domainId + ":" + accountName;
			
			ReservationInfoType param3 = new ReservationInfoType();
			
			param3.setReservationId( inst.getId());   // -> an id we could track down if needed
	        param3.setOwnerId(ownerId);
	        param3.setRequesterId( "" );
	        
			GroupSetType  param4 = new GroupSetType();
			
	        
            String[] groups = inst.getGroupSet();
            if (null == groups || 0 == groups.length) {
                GroupItemType param5 = new GroupItemType();
                param5.setGroupId("");
                param4.addItem( param5 );
            } else {
                for (String group : groups) {
                    GroupItemType param5 = new GroupItemType();
                    param5.setGroupId(group);
                    param4.addItem( param5 );
                }
            }
            param3.setGroupSet( param4 );
	        
	        RunningInstancesSetType  param6 = new RunningInstancesSetType();
	        RunningInstancesItemType param7 = new RunningInstancesItemType();

	        param7.setInstanceId( inst.getId());
	        param7.setImageId( inst.getTemplateId());
	        
	        InstanceStateType param8 = new InstanceStateType();
	        param8.setCode( toAmazonCode( inst.getState()));
	        param8.setName( toAmazonStateName( inst.getState()));
	        param7.setInstanceState( param8 );
	        
	        param7.setPrivateDnsName( "" );
	        param7.setDnsName( "" );
	        param7.setReason( "" );
            param7.setKeyName( inst.getKeyPairName());
	        param7.setAmiLaunchIndex( "" );
	        param7.setInstanceType( inst.getServiceOffering());
	        
	        ProductCodesSetType param9 = new ProductCodesSetType();
	        ProductCodesSetItemType param10 = new ProductCodesSetItemType();
	        param10.setProductCode( "" );
            param9.addItem( param10 );
	        param7.setProductCodes( param9 );
	        
	        Calendar cal = inst.getCreated();
	        if ( null == cal ) {
	        	 cal = Calendar.getInstance();
//	        	 cal.set( 1970, 1, 1 );
	        }
	        param7.setLaunchTime( cal );
	        
	        PlacementResponseType param11 = new PlacementResponseType();
	        param11.setAvailabilityZone( inst.getZoneName());
	        param11.setGroupName( "" );
	        param7.setPlacement( param11 );
	        param7.setKernelId( "" );
	        param7.setRamdiskId( "" );
	        param7.setPlatform( "" );
	        
	        InstanceMonitoringStateType param12 = new InstanceMonitoringStateType();
	        param12.setState( "" );
            param7.setMonitoring( param12 );
            param7.setSubnetId( "" );
            param7.setVpcId( "" );
//            String ipAddr = inst.getPrivateIpAddress();
//            param7.setPrivateIpAddress((null != ipAddr ? ipAddr : ""));
            param7.setPrivateIpAddress(inst.getPrivateIpAddress());
	        param7.setIpAddress( inst.getIpAddress());
	        
	        StateReasonType param13 = new StateReasonType();
	        param13.setCode( "" );
	        param13.setMessage( "" );
            param7.setStateReason( param13 );
            param7.setArchitecture( "" );
            param7.setRootDeviceType( "" );
        	String devicePath = engine.cloudDeviceIdToDevicePath( inst.getHypervisor(), inst.getRootDeviceId());
            param7.setRootDeviceName( devicePath );
            

            param7.setInstanceLifecycle( "" );
            param7.setSpotInstanceRequestId( "" );
            param7.setHypervisor(inst.getHypervisor());

            EC2TagKeyValue[] tags = inst.getResourceTags();
            param7.setTagSet(setResourceTags(tags));

	        param6.addItem( param7 );
	        param3.setInstancesSet( param6 );
	        param2.addItem( param3 );
		}
	    param1.setReservationSet( param2 );
	    param1.setRequestId( UUID.randomUUID().toString());
	    response.setDescribeInstancesResponse( param1 );
		return response;
	}

	
    public static DescribeAddressesResponse toDescribeAddressesResponse(EC2DescribeAddressesResponse engineResponse) {
    	List<DescribeAddressesResponseItemType> items = new ArrayList<DescribeAddressesResponseItemType>();
    	EC2Address[] addressSet = engineResponse.getAddressSet();
    	
    	for (EC2Address addr: addressSet) {
    		DescribeAddressesResponseItemType item = new DescribeAddressesResponseItemType();
    		item.setPublicIp(addr.getIpAddress());
    		item.setInstanceId(addr.getAssociatedInstanceId());
    		items.add(item);
    	}
    	DescribeAddressesResponseInfoType descAddrRespInfoType = new DescribeAddressesResponseInfoType();
    	descAddrRespInfoType.setItem(items.toArray(new DescribeAddressesResponseItemType[0]));
    	
    	DescribeAddressesResponseType descAddrRespType = new DescribeAddressesResponseType();   	
    	descAddrRespType.setRequestId(UUID.randomUUID().toString());
    	descAddrRespType.setAddressesSet(descAddrRespInfoType);
    	
    	DescribeAddressesResponse descAddrResp = new DescribeAddressesResponse();
    	descAddrResp.setDescribeAddressesResponse(descAddrRespType);
    	
    	return descAddrResp;
    }

    public static AllocateAddressResponse toAllocateAddressResponse(final EC2Address ec2Address) {
    	AllocateAddressResponse response = new AllocateAddressResponse();
    	AllocateAddressResponseType param1 = new AllocateAddressResponseType();
    	
    	param1.setPublicIp(ec2Address.getIpAddress());
    	param1.setRequestId(UUID.randomUUID().toString());
    	response.setAllocateAddressResponse(param1);
    	return response;
    }

    public static ReleaseAddressResponse toReleaseAddressResponse(final boolean result) {
    	ReleaseAddressResponse response = new ReleaseAddressResponse();
    	ReleaseAddressResponseType param1 = new ReleaseAddressResponseType();
    	
    	param1.set_return(result);
    	param1.setRequestId(UUID.randomUUID().toString());
    	
    	response.setReleaseAddressResponse(param1);
    	return response;
    }

    public static AssociateAddressResponse toAssociateAddressResponse(final boolean result) {
    	AssociateAddressResponse response = new AssociateAddressResponse();
    	AssociateAddressResponseType param1 = new AssociateAddressResponseType();
    	
    	param1.setRequestId(UUID.randomUUID().toString());
    	param1.set_return(result);
    	
    	response.setAssociateAddressResponse(param1);
    	return response;
    }

    public static DisassociateAddressResponse toDisassociateAddressResponse(final boolean result) {
    	DisassociateAddressResponse response = new DisassociateAddressResponse();
    	DisassociateAddressResponseType param1 = new DisassociateAddressResponseType();
    	
    	param1.setRequestId(UUID.randomUUID().toString());
    	param1.set_return(result);
    	
    	response.setDisassociateAddressResponse(param1);
    	return response;
    }

	/**
	 * Map our cloud state values into what Amazon defines.
	 * Where are the values that can be returned by our cloud api defined?
	 * 
	 * @param cloudState
	 * @return 
	 */
	public static int toAmazonCode( String cloudState )
	{
		if (null == cloudState) return 48;
		
		     if (cloudState.equalsIgnoreCase( "Destroyed" )) return 48;
		else if (cloudState.equalsIgnoreCase( "Stopped"   )) return 80;
		else if (cloudState.equalsIgnoreCase( "Running"   )) return 16;
		else if (cloudState.equalsIgnoreCase( "Starting"  )) return 0;
		else if (cloudState.equalsIgnoreCase( "Stopping"  )) return 64;
		else if (cloudState.equalsIgnoreCase( "Error"     )) return 1;
		else if (cloudState.equalsIgnoreCase( "Expunging" )) return 48;
		else return 16;
	}
	
	public static String toAmazonStateName( String cloudState )
	{
		if (null == cloudState) return new String( "terminated" );
		
		     if (cloudState.equalsIgnoreCase( "Destroyed" )) return new String( "terminated" );
		else if (cloudState.equalsIgnoreCase( "Stopped"   )) return new String( "stopped" );
		else if (cloudState.equalsIgnoreCase( "Running"   )) return new String( "running" );
		else if (cloudState.equalsIgnoreCase( "Starting"  )) return new String( "pending" );
		else if (cloudState.equalsIgnoreCase( "Stopping"  )) return new String( "stopping" );
		else if (cloudState.equalsIgnoreCase( "Error"     )) return new String( "error" );
		else if (cloudState.equalsIgnoreCase( "Expunging" )) return new String( "terminated");
		else return new String( "running" );
	}
	
	/**
	 * We assume a state for the volume based on what its associated VM is doing.
	 * 
	 * @param vmId
	 * @param vmState
	 * @return
	 */
	public static String toVolumeAttachmentState(String instanceId, String vmState ) {
		if (null == instanceId || null == vmState) return "detached";
		
		     if (vmState.equalsIgnoreCase( "Destroyed" )) return "detached";
		else if (vmState.equalsIgnoreCase( "Stopped"   )) return "attached";
		else if (vmState.equalsIgnoreCase( "Running"   )) return "attached";
		else if (vmState.equalsIgnoreCase( "Starting"  )) return "attaching";
		else if (vmState.equalsIgnoreCase( "Stopping"  )) return "attached";
		else if (vmState.equalsIgnoreCase( "Error"     )) return "detached";
		else return "detached";
	}
	
	public static StopInstancesResponse toStopInstancesResponse(EC2StopInstancesResponse engineResponse) {
	    StopInstancesResponse response = new StopInstancesResponse();
	    StopInstancesResponseType param1 = new StopInstancesResponseType();
	    InstanceStateChangeSetType param2 = new InstanceStateChangeSetType();

		EC2Instance[] instances = engineResponse.getInstanceSet();
		for( int i=0; i < instances.length; i++ ) {
			InstanceStateChangeType param3 = new InstanceStateChangeType();
			param3.setInstanceId( instances[i].getId());
			
			InstanceStateType param4 = new InstanceStateType();
	        param4.setCode( toAmazonCode( instances[i].getState()));
	        param4.setName( toAmazonStateName( instances[i].getState()));
			param3.setCurrentState( param4 );

			InstanceStateType param5 = new InstanceStateType();
	        param5.setCode( toAmazonCode( instances[i].getPreviousState() ));
	        param5.setName( toAmazonStateName( instances[i].getPreviousState() ));
			param3.setPreviousState( param5 );
			
			param2.addItem( param3 );
		}
		
	    param1.setRequestId( UUID.randomUUID().toString());
        param1.setInstancesSet( param2 );
	    response.setStopInstancesResponse( param1 );
		return response;
	}
	
	public static StartInstancesResponse toStartInstancesResponse(EC2StartInstancesResponse engineResponse) {
	    StartInstancesResponse response = new StartInstancesResponse();
	    StartInstancesResponseType param1 = new StartInstancesResponseType();
	    InstanceStateChangeSetType param2 = new InstanceStateChangeSetType();

		EC2Instance[] instances = engineResponse.getInstanceSet();
		for( int i=0; i < instances.length; i++ ) {
			InstanceStateChangeType param3 = new InstanceStateChangeType();
			param3.setInstanceId( instances[i].getId());
			
			InstanceStateType param4 = new InstanceStateType();
	        param4.setCode( toAmazonCode( instances[i].getState()));
	        param4.setName( toAmazonStateName( instances[i].getState()));
			param3.setCurrentState( param4 );

			InstanceStateType param5 = new InstanceStateType();
	        param5.setCode( toAmazonCode( instances[i].getPreviousState() ));
	        param5.setName( toAmazonStateName( instances[i].getPreviousState() ));
			param3.setPreviousState( param5 );
			
			param2.addItem( param3 );
		}
		
	    param1.setRequestId( UUID.randomUUID().toString());
        param1.setInstancesSet( param2 );
	    response.setStartInstancesResponse( param1 );
		return response;
	}
	
	public static TerminateInstancesResponse toTermInstancesResponse(EC2StopInstancesResponse engineResponse) {
		TerminateInstancesResponse response = new TerminateInstancesResponse();
		TerminateInstancesResponseType param1 = new TerminateInstancesResponseType();
	    InstanceStateChangeSetType param2 = new InstanceStateChangeSetType();

		EC2Instance[] instances = engineResponse.getInstanceSet();
		for( int i=0; i < instances.length; i++ ) {
			InstanceStateChangeType param3 = new InstanceStateChangeType();
			param3.setInstanceId( instances[i].getId());
			
			InstanceStateType param4 = new InstanceStateType();
	        param4.setCode( toAmazonCode( instances[i].getState()));
	        param4.setName( toAmazonStateName( instances[i].getState()));
			param3.setCurrentState( param4 );

			InstanceStateType param5 = new InstanceStateType();
	        param5.setCode( toAmazonCode( instances[i].getPreviousState() ));
	        param5.setName( toAmazonStateName( instances[i].getPreviousState() ));
			param3.setPreviousState( param5 );
			
			param2.addItem( param3 );
		}
		
	    param1.setRequestId( UUID.randomUUID().toString());
        param1.setInstancesSet( param2 );
	    response.setTerminateInstancesResponse( param1 );
		return response;
	}
	
	public static RebootInstancesResponse toRebootInstancesResponse(boolean engineResponse) {
	    RebootInstancesResponse response = new RebootInstancesResponse();
	    RebootInstancesResponseType param1 = new RebootInstancesResponseType();

	    param1.setRequestId( UUID.randomUUID().toString());
	    param1.set_return( engineResponse );
	    response.setRebootInstancesResponse( param1 );
		return response;
	}

	public static RunInstancesResponse toRunInstancesResponse(EC2RunInstancesResponse engineResponse, EC2Engine engine ) {
	    RunInstancesResponse response = new RunInstancesResponse();
	    RunInstancesResponseType param1 = new RunInstancesResponseType();
	    
	    param1.setReservationId( "" );
	    
	    RunningInstancesSetType param6 = new RunningInstancesSetType();
		EC2Instance[] instances = engineResponse.getInstanceSet();
		for (EC2Instance inst : instances) {
	        RunningInstancesItemType param7 = new RunningInstancesItemType();
	        param7.setInstanceId( inst.getId());
	        param7.setImageId( inst.getTemplateId());
	        
	        String accountName = inst.getAccountName();
			String domainId = inst.getDomainId();
			String ownerId = domainId + ":" + accountName;
		
	        param1.setOwnerId(ownerId);
			
            String[] groups = inst.getGroupSet();
            GroupSetType  param2 = new GroupSetType();
            if (null == groups || 0 == groups.length) {
                GroupItemType param3 = new GroupItemType();
                param3.setGroupId("");
                param2.addItem( param3 );
            } else {
                for (String group : groups) {
                    GroupItemType param3 = new GroupItemType();
                    param3.setGroupId(group);
                    param2.addItem( param3 );   
                }
            }
            param1.setGroupSet(param2);
			
	        InstanceStateType param8 = new InstanceStateType();
	        param8.setCode( toAmazonCode( inst.getState()));
	        param8.setName( toAmazonStateName( inst.getState()));
	        param7.setInstanceState( param8 );
	        
	        param7.setPrivateDnsName( "" );
	        param7.setDnsName( "" );
	        param7.setReason( "" );
            param7.setKeyName( inst.getKeyPairName());
	        param7.setAmiLaunchIndex( "" );
	        
	        ProductCodesSetType param9 = new ProductCodesSetType();
	        ProductCodesSetItemType param10 = new ProductCodesSetItemType();
	        param10.setProductCode( "" );
            param9.addItem( param10 );
	        param7.setProductCodes( param9 );
	        
	        param7.setInstanceType( inst.getServiceOffering());
        	// -> CloudStack seems to have issues with timestamp formats so just in case
	        Calendar cal = inst.getCreated();
	        if ( null == cal ) {
	        	 cal = Calendar.getInstance();
	        	 cal.set( 1970, 1, 1 );
	        }
	        param7.setLaunchTime( cal );

	        PlacementResponseType param11 = new PlacementResponseType();
	        param11.setAvailabilityZone( inst.getZoneName());
	        param7.setPlacement( param11 );
	        
	        param7.setKernelId( "" );
	        param7.setRamdiskId( "" );
	        param7.setPlatform( "" );
	        
	        InstanceMonitoringStateType param12 = new InstanceMonitoringStateType();
	        param12.setState( "" );
            param7.setMonitoring( param12 );
            param7.setSubnetId( "" );
            param7.setVpcId( "" );
            String ipAddr = inst.getPrivateIpAddress();
            param7.setPrivateIpAddress((null != ipAddr ? ipAddr : ""));
	        param7.setIpAddress(inst.getIpAddress());

	        StateReasonType param13 = new StateReasonType();
	        param13.setCode( "" );
	        param13.setMessage( "" );
            param7.setStateReason( param13 );
            param7.setArchitecture( "" );
            param7.setRootDeviceType( "" );
            param7.setRootDeviceName( "" );
            
            param7.setInstanceLifecycle( "" );
            param7.setSpotInstanceRequestId( "" );
            param7.setVirtualizationType( "" );
            param7.setClientToken( "" );
            
            ResourceTagSetType param18 = new ResourceTagSetType();
            ResourceTagSetItemType param19 = new ResourceTagSetItemType();
            param19.setKey("");
            param19.setValue("");
            param18.addItem( param19 );
            param7.setTagSet( param18 );
            
            String hypervisor = inst.getHypervisor();
            param7.setHypervisor((null != hypervisor ? hypervisor : ""));
	        param6.addItem( param7 );
		}
		param1.setInstancesSet( param6 );
		param1.setRequesterId( "" );
		
	    param1.setRequestId( UUID.randomUUID().toString());
	    response.setRunInstancesResponse( param1 );
		return response;
	}

	public static DescribeAvailabilityZonesResponse toDescribeAvailabilityZonesResponse(EC2DescribeAvailabilityZonesResponse engineResponse) {
		DescribeAvailabilityZonesResponse response = new DescribeAvailabilityZonesResponse();
		DescribeAvailabilityZonesResponseType param1 = new DescribeAvailabilityZonesResponseType();
        AvailabilityZoneSetType param2 = new AvailabilityZoneSetType();
        
		String[] zones = engineResponse.getZoneSet();
		for (String zone : zones) {
            AvailabilityZoneItemType param3 = new AvailabilityZoneItemType(); 
            AvailabilityZoneMessageSetType param4 = new AvailabilityZoneMessageSetType();
            param3.setZoneName( zone );
            param3.setZoneState( "available" );
            param3.setRegionName( "" );
            param3.setMessageSet( param4 );
            param2.addItem( param3 );
		}

	    param1.setRequestId( UUID.randomUUID().toString());
        param1.setAvailabilityZoneInfo( param2 );
	    response.setDescribeAvailabilityZonesResponse( param1 );
		return response;
	}
	
	public static AttachVolumeResponse toAttachVolumeResponse(EC2Volume engineResponse) {
		AttachVolumeResponse response = new AttachVolumeResponse();
		AttachVolumeResponseType param1 = new AttachVolumeResponseType();
		
	    Calendar cal = Calendar.getInstance();
		
	    // -> if the instanceId was not given in the request then we have no way to get it
		param1.setVolumeId( engineResponse.getId().toString());
		param1.setInstanceId( engineResponse.getInstanceId().toString());
		param1.setDevice( engineResponse.getDevice());
		if ( null != engineResponse.getState())
		     param1.setStatus( engineResponse.getState());
		else param1.setStatus( "" );  // ToDo - throw an Soap Fault 
		
		param1.setAttachTime( cal );
			
		param1.setRequestId( UUID.randomUUID().toString());
        response.setAttachVolumeResponse( param1 );
		return response;
	}

	public static DetachVolumeResponse toDetachVolumeResponse(EC2Volume engineResponse) {
		DetachVolumeResponse response = new DetachVolumeResponse();
		DetachVolumeResponseType param1 = new DetachVolumeResponseType();
	    Calendar cal = Calendar.getInstance();
	    cal.set( 1970, 1, 1 );   // return one value, Unix Epoch, what else can we return? 
		
		param1.setVolumeId( engineResponse.getId().toString());
		param1.setInstanceId( (null == engineResponse.getInstanceId() ? "" : engineResponse.getInstanceId().toString()));
		param1.setDevice( (null == engineResponse.getDevice() ? "" : engineResponse.getDevice()));
		if ( null != engineResponse.getState())
		     param1.setStatus( engineResponse.getState());
		else param1.setStatus( "" );  // ToDo - throw an Soap Fault 
		
		param1.setAttachTime( cal );
			
		param1.setRequestId( UUID.randomUUID().toString());
        response.setDetachVolumeResponse( param1 );
		return response;
	}
	
	public static CreateVolumeResponse toCreateVolumeResponse(EC2Volume engineResponse) {
		CreateVolumeResponse response = new CreateVolumeResponse();
		CreateVolumeResponseType param1 = new CreateVolumeResponseType();
		
		param1.setVolumeId( engineResponse.getId().toString());
        Long volSize = new Long( engineResponse.getSize());
        param1.setSize( volSize.toString());  
        param1.setSnapshotId( "" );
        param1.setAvailabilityZone( engineResponse.getZoneName());
		if ( null != engineResponse.getState())
		     param1.setStatus( engineResponse.getState());
		else param1.setStatus( "" );  // ToDo - throw an Soap Fault 
		
       	// -> CloudStack seems to have issues with timestamp formats so just in case
        Calendar cal = EC2RestAuth.parseDateString(engineResponse.getCreated());
        if ( null == cal ) {
        	 cal = Calendar.getInstance();
//        	 cal.set( 1970, 1, 1 );
        }
		param1.setCreateTime( cal );

		param1.setRequestId( UUID.randomUUID().toString());
        response.setCreateVolumeResponse( param1 );
		return response;
	}

	public static DeleteVolumeResponse toDeleteVolumeResponse(EC2Volume engineResponse) {
		DeleteVolumeResponse response = new DeleteVolumeResponse();
		DeleteVolumeResponseType param1 = new DeleteVolumeResponseType();
		
		if ( null != engineResponse.getState())
			 param1.set_return( true  );
		else param1.set_return( false );  // ToDo - supposed to return an error
	
		param1.setRequestId( UUID.randomUUID().toString());
        response.setDeleteVolumeResponse( param1 );
		return response;
	}
	
	public static DescribeSnapshotsResponse toDescribeSnapshotsResponse(EC2DescribeSnapshotsResponse engineResponse) {
	    DescribeSnapshotsResponse response = new DescribeSnapshotsResponse();
	    DescribeSnapshotsResponseType param1 = new DescribeSnapshotsResponseType();
	    DescribeSnapshotsSetResponseType param2 = new DescribeSnapshotsSetResponseType();
        
	    EC2Snapshot[] snaps = engineResponse.getSnapshotSet();
	    for (EC2Snapshot snap : snaps) {
	         DescribeSnapshotsSetItemResponseType param3 = new DescribeSnapshotsSetItemResponseType();
	         param3.setSnapshotId( snap.getId());
	         param3.setVolumeId( snap.getVolumeId());

	         // our semantics are different than those ec2 uses
	         if (snap.getState().equalsIgnoreCase("backedup")) {
                 param3.setStatus("completed");
                 param3.setProgress("100%");
             } else if (snap.getState().equalsIgnoreCase("creating")) {
                 param3.setStatus("pending");
                 param3.setProgress("33%");
             } else if (snap.getState().equalsIgnoreCase("backingup")) {
                 param3.setStatus("pending");
                 param3.setProgress("66%");
             } else {
                 // if we see anything besides: backedup/creating/backingup, we assume error
                 param3.setStatus("error");
                 param3.setProgress("0%");
             }
//	         param3.setStatus( snap.getState());
	         
	         String ownerId = snap.getDomainId() + ":" + snap.getAccountName();
	         
	         // -> CloudStack seems to have issues with timestamp formats so just in case
		     Calendar cal = snap.getCreated();
		     if ( null == cal ) {
		       	  cal = Calendar.getInstance();
		       	  cal.set( 1970, 1, 1 );
		     }
	         param3.setStartTime( cal );
	         
	         param3.setOwnerId(ownerId);
	         param3.setVolumeSize( snap.getVolumeSize().toString());
	         param3.setDescription( snap.getName());
	         param3.setOwnerAlias( snap.getAccountName() );
	         

	         EC2TagKeyValue[] tags = snap.getResourceTags();
	         param3.setTagSet(setResourceTags(tags));
             param2.addItem( param3 );
	    }
	    
	    param1.setSnapshotSet( param2 );
	    param1.setRequestId( UUID.randomUUID().toString());
	    response.setDescribeSnapshotsResponse( param1 );
	    return response;
	}
	
	public static DeleteSnapshotResponse toDeleteSnapshotResponse( boolean engineResponse ) {
		DeleteSnapshotResponse response = new DeleteSnapshotResponse();
		DeleteSnapshotResponseType param1 = new DeleteSnapshotResponseType();
	
		param1.set_return( engineResponse );
	    param1.setRequestId( UUID.randomUUID().toString());
        response.setDeleteSnapshotResponse( param1 );
		return response;
	}
	
	public static CreateSnapshotResponse toCreateSnapshotResponse(EC2Snapshot engineResponse, EC2Engine engine ) {
		CreateSnapshotResponse response = new CreateSnapshotResponse();
		CreateSnapshotResponseType param1 = new CreateSnapshotResponseType();
		
		String accountName = engineResponse.getAccountName();
		String domainId = engineResponse.getDomainId().toString();
		String ownerId = domainId + ":" + accountName;

		param1.setSnapshotId( engineResponse.getId().toString());
		param1.setVolumeId( engineResponse.getVolumeId().toString());
		param1.setStatus( "completed" );
		
       	// -> CloudStack seems to have issues with timestamp formats so just in case
        Calendar cal = engineResponse.getCreated();
        if ( null == cal ) {
        	 cal = Calendar.getInstance();
        	 cal.set( 1970, 1, 1 );
        }
		param1.setStartTime( cal );
		
		param1.setProgress( "100" );
		param1.setOwnerId(ownerId);
        Long volSize = new Long( engineResponse.getVolumeSize());
        param1.setVolumeSize( volSize.toString());
        param1.setDescription( engineResponse.getName());
	    param1.setRequestId( UUID.randomUUID().toString());
        response.setCreateSnapshotResponse( param1 );
		return response;
	}
	
	public static DescribeSecurityGroupsResponse toDescribeSecurityGroupsResponse(
			EC2DescribeSecurityGroupsResponse engineResponse) {
		DescribeSecurityGroupsResponse response = new DescribeSecurityGroupsResponse();
		DescribeSecurityGroupsResponseType param1 = new DescribeSecurityGroupsResponseType();
		SecurityGroupSetType param2 = new SecurityGroupSetType();

		EC2SecurityGroup[] groups = engineResponse.getGroupSet();
		for (EC2SecurityGroup group : groups) {
			SecurityGroupItemType param3 = new SecurityGroupItemType();
			String accountName = group.getAccountName();
			String domainId = group.getDomainId();
			String ownerId = domainId + ":" + accountName;

			param3.setOwnerId(ownerId);
			param3.setGroupName(group.getName());
			String desc = group.getDescription();
			param3.setGroupDescription((null != desc ? desc : ""));

			IpPermissionSetType param4 = new IpPermissionSetType();
			EC2IpPermission[] perms = group.getIpPermissionSet();
			for (EC2IpPermission perm : perms) {
				// TODO: Fix kludges like this...
				if (perm == null)
					continue;
				IpPermissionType param5 = new IpPermissionType();
				param5.setIpProtocol(perm.getProtocol());
                if (perm.getProtocol().equalsIgnoreCase("icmp")) {
                    param5.setFromPort(Integer.parseInt(perm.getIcmpType()));
                    param5.setToPort(Integer.parseInt(perm.getIcmpCode()));
                } else {			
                    param5.setFromPort(perm.getFromPort());
                    param5.setToPort(perm.getToPort());
                }

				// -> user groups
				EC2SecurityGroup[] userSet = perm.getUserSet();
				if (null == userSet || 0 == userSet.length) {
					UserIdGroupPairSetType param8 = new UserIdGroupPairSetType();
					param5.setGroups(param8);
				} else {
					for (EC2SecurityGroup secGroup : userSet) {
						UserIdGroupPairSetType param8 = new UserIdGroupPairSetType();
						UserIdGroupPairType param9 = new UserIdGroupPairType();
						param9.setUserId(secGroup.getAccount());
						param9.setGroupName(secGroup.getName());
						param8.addItem(param9);
						param5.setGroups(param8);
					}
				}

				// -> or CIDR list
				String[] rangeSet = perm.getIpRangeSet();
				if (null == rangeSet || 0 == rangeSet.length) {
					IpRangeSetType param6 = new IpRangeSetType();
					param5.setIpRanges(param6);
				} else {
					for (String range : rangeSet) {
						// TODO: This needs further attention...
                        IpRangeSetType param6 = new IpRangeSetType();
                        if (range != null) {
                            IpRangeItemType param7 = new IpRangeItemType();
                            param7.setCidrIp(range);
                            param6.addItem(param7);
                        }
                        param5.setIpRanges(param6);
					}
				}
				param4.addItem(param5);
			}
			param3.setIpPermissions(param4);
			param2.addItem(param3);
		}
		param1.setSecurityGroupInfo(param2);
		param1.setRequestId(UUID.randomUUID().toString());
		response.setDescribeSecurityGroupsResponse(param1);
		return response;
	}
	
	public static CreateSecurityGroupResponse toCreateSecurityGroupResponse( boolean success ) {
		CreateSecurityGroupResponse response = new CreateSecurityGroupResponse();
		CreateSecurityGroupResponseType param1 = new CreateSecurityGroupResponseType();

		param1.set_return(success);
		param1.setRequestId( UUID.randomUUID().toString());
		response.setCreateSecurityGroupResponse( param1 );
		return response;
	}
	
	public static DeleteSecurityGroupResponse toDeleteSecurityGroupResponse( boolean success ) {
		DeleteSecurityGroupResponse response = new DeleteSecurityGroupResponse();
		DeleteSecurityGroupResponseType param1 = new DeleteSecurityGroupResponseType();
		
		param1.set_return( success );
		param1.setRequestId( UUID.randomUUID().toString());
		response.setDeleteSecurityGroupResponse( param1 );
		return response;
	}
	
	public static AuthorizeSecurityGroupIngressResponse toAuthorizeSecurityGroupIngressResponse( boolean success ) {
		AuthorizeSecurityGroupIngressResponse response = new AuthorizeSecurityGroupIngressResponse();
		AuthorizeSecurityGroupIngressResponseType param1 = new AuthorizeSecurityGroupIngressResponseType();
	
		param1.set_return( success );
		param1.setRequestId( UUID.randomUUID().toString());
		response.setAuthorizeSecurityGroupIngressResponse( param1 );
        return response;
	}
	
	public static RevokeSecurityGroupIngressResponse toRevokeSecurityGroupIngressResponse( boolean success ) {
		RevokeSecurityGroupIngressResponse response = new RevokeSecurityGroupIngressResponse();
		RevokeSecurityGroupIngressResponseType param1 = new RevokeSecurityGroupIngressResponseType();
	
		param1.set_return( success );
		param1.setRequestId( UUID.randomUUID().toString());
		response.setRevokeSecurityGroupIngressResponse( param1 );
        return response;
	}

    public static CreateTagsResponse toCreateTagsResponse( boolean success ) {
        CreateTagsResponse response = new CreateTagsResponse();
        CreateTagsResponseType param1 = new CreateTagsResponseType();

        param1.set_return(success);
        param1.setRequestId( UUID.randomUUID().toString());
        response.setCreateTagsResponse(param1);
        return response;
    }

    public static DeleteTagsResponse toDeleteTagsResponse( boolean success ) {
        DeleteTagsResponse response = new DeleteTagsResponse();
        DeleteTagsResponseType param1 = new DeleteTagsResponseType();

        param1.set_return(success);
        param1.setRequestId( UUID.randomUUID().toString());
        response.setDeleteTagsResponse(param1);
        return response;
    }

    public static DescribeTagsResponse toDescribeTagsResponse( EC2DescribeTagsResponse engineResponse) {
        DescribeTagsResponse response = new DescribeTagsResponse();
        DescribeTagsResponseType param1 = new DescribeTagsResponseType();

        EC2ResourceTag[] tags = engineResponse.getTagsSet();
        TagSetType param2 = new TagSetType();
        for (EC2ResourceTag tag : tags) {
            TagSetItemType param3 = new TagSetItemType();
            param3.setResourceId(tag.getResourceId());
            param3.setResourceType(tag.getResourceType());
            param3.setKey(tag.getKey());
            if (tag.getValue() != null)
                param3.setValue(tag.getValue());
            param2.addItem(param3);
        }
        param1.setTagSet(param2);
        param1.setRequestId( UUID.randomUUID().toString());
        response.setDescribeTagsResponse(param1);
        return response;
    }

	public DescribeKeyPairsResponse describeKeyPairs(DescribeKeyPairs describeKeyPairs) {
		
		EC2DescribeKeyPairs ec2Request = new EC2DescribeKeyPairs();

		// multiple keynames may be provided
		DescribeKeyPairsInfoType kset = describeKeyPairs.getDescribeKeyPairs().getKeySet();
		if (kset != null) {
			DescribeKeyPairsItemType[] keyPairKeys = kset.getItem();
			if (keyPairKeys != null) {
				for (DescribeKeyPairsItemType key : keyPairKeys) {
					ec2Request.addKeyName(key.getKeyName());
				}
			}
		}
		
		// multiple filters may be provided
		FilterSetType fset = describeKeyPairs.getDescribeKeyPairs().getFilterSet();
		if (fset != null) {
			ec2Request.setKeyFilterSet(toKeyPairFilterSet(fset));
		}
		
		return toDescribeKeyPairs(engine.describeKeyPairs(ec2Request));
	}
	
	public static DescribeKeyPairsResponse toDescribeKeyPairs(final EC2DescribeKeyPairsResponse response) {
		EC2SSHKeyPair[] keyPairs = response.getKeyPairSet();
		
		DescribeKeyPairsResponseInfoType respInfoType = new DescribeKeyPairsResponseInfoType();
		if (keyPairs != null && keyPairs.length > 0) {
			for (final EC2SSHKeyPair key : keyPairs) {
				DescribeKeyPairsResponseItemType respItemType = new DescribeKeyPairsResponseItemType();
				respItemType.setKeyFingerprint(key.getFingerprint());
				respItemType.setKeyName(key.getKeyName());
				respInfoType.addItem(respItemType);
			}
		}
		
		DescribeKeyPairsResponseType respType = new DescribeKeyPairsResponseType();
		respType.setRequestId(UUID.randomUUID().toString());
		respType.setKeySet(respInfoType);

		DescribeKeyPairsResponse resp = new DescribeKeyPairsResponse();
		resp.setDescribeKeyPairsResponse(respType);
		return resp;
	}
	
	public ImportKeyPairResponse importKeyPair(ImportKeyPair importKeyPair) {
		String publicKey = importKeyPair.getImportKeyPair().getPublicKeyMaterial();
        if (!publicKey.contains(" "))
             publicKey = new String(Base64.decodeBase64(publicKey.getBytes()));
        
        EC2ImportKeyPair ec2Request = new EC2ImportKeyPair();
    	if (ec2Request != null) {
    		ec2Request.setKeyName(importKeyPair.getImportKeyPair().getKeyName());
    		ec2Request.setPublicKeyMaterial(publicKey);
    	}

		return toImportKeyPair(engine.importKeyPair(ec2Request));
	}
	
	public static ImportKeyPairResponse toImportKeyPair(final EC2SSHKeyPair key) {
		ImportKeyPairResponseType respType = new ImportKeyPairResponseType();
		respType.setRequestId(UUID.randomUUID().toString());
		respType.setKeyName(key.getKeyName());
		respType.setKeyFingerprint(key.getFingerprint());
		
		ImportKeyPairResponse response = new ImportKeyPairResponse();
		response.setImportKeyPairResponse(respType);
		
		return response;
	}
	
	public CreateKeyPairResponse createKeyPair(CreateKeyPair createKeyPair) {
		EC2CreateKeyPair ec2Request = new EC2CreateKeyPair();
    	if (ec2Request != null) {
    		ec2Request.setKeyName(createKeyPair.getCreateKeyPair().getKeyName());
    	}

		return toCreateKeyPair(engine.createKeyPair( ec2Request ));
	}
	
	public static CreateKeyPairResponse toCreateKeyPair(final EC2SSHKeyPair key) {
		CreateKeyPairResponseType respType = new CreateKeyPairResponseType();
		respType.setRequestId(UUID.randomUUID().toString());
		respType.setKeyName(key.getKeyName());
		respType.setKeyFingerprint(key.getFingerprint());
		respType.setKeyMaterial(key.getPrivateKey());
		
		CreateKeyPairResponse response = new CreateKeyPairResponse();
		response.setCreateKeyPairResponse(respType);
		
		return response;
	}
	
	public DeleteKeyPairResponse deleteKeyPair(DeleteKeyPair deleteKeyPair) {
		EC2DeleteKeyPair ec2Request = new EC2DeleteKeyPair();
		ec2Request.setKeyName(deleteKeyPair.getDeleteKeyPair().getKeyName());
		
		return toDeleteKeyPair(engine.deleteKeyPair(ec2Request));
	}
	
	public static DeleteKeyPairResponse toDeleteKeyPair(final boolean success) {
		DeleteKeyPairResponseType respType = new DeleteKeyPairResponseType();
		respType.setRequestId(UUID.randomUUID().toString());
		respType.set_return(success);
		
		DeleteKeyPairResponse response = new DeleteKeyPairResponse();
		response.setDeleteKeyPairResponse(respType);
		
		return response;
	}
	
	public GetPasswordDataResponse getPasswordData(GetPasswordData getPasswordData) {
		return toGetPasswordData(engine.getPasswordData(getPasswordData.getGetPasswordData().getInstanceId()));
	}

    public static ResourceTagSetType setResourceTags(EC2TagKeyValue[] tags){
        ResourceTagSetType param1 = new ResourceTagSetType();
        if (null == tags || 0 == tags.length) {
            ResourceTagSetItemType param2 = new ResourceTagSetItemType();
            param2.setKey("");
            param2.setValue("");
            param1.addItem( param2 );
        }
        else {
            for(EC2TagKeyValue tag : tags) {
                ResourceTagSetItemType param2 = new ResourceTagSetItemType();
                param2.setKey(tag.getKey());
                if (tag.getValue() != null)
                    param2.setValue(tag.getValue());
                else
                    param2.setValue("");
                param1.addItem(param2);
            }
        }
        return param1;
    }

	@SuppressWarnings("serial")
	public static GetPasswordDataResponse toGetPasswordData(final EC2PasswordData passwdData) {
		return new GetPasswordDataResponse() {{
			setGetPasswordDataResponse(new GetPasswordDataResponseType() {{
				setRequestId(UUID.randomUUID().toString());
				setTimestamp(Calendar.getInstance());
				setPasswordData(passwdData.getEncryptedPassword());
				setInstanceId(passwdData.getInstanceId());
			}});
		}};
	}

	
	
	
	// Actions not yet implemented:
	
	public ActivateLicenseResponse activateLicense(ActivateLicense activateLicense) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public AssociateDhcpOptionsResponse associateDhcpOptions(AssociateDhcpOptions associateDhcpOptions) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	};
	
	public AttachVpnGatewayResponse attachVpnGateway(AttachVpnGateway attachVpnGateway) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public BundleInstanceResponse bundleInstance(BundleInstance bundleInstance) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public CancelBundleTaskResponse cancelBundleTask(CancelBundleTask cancelBundleTask) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public CancelConversionTaskResponse cancelConversionTask(CancelConversionTask cancelConversionTask) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public CancelSpotInstanceRequestsResponse cancelSpotInstanceRequests(CancelSpotInstanceRequests cancelSpotInstanceRequests) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public ConfirmProductInstanceResponse confirmProductInstance(ConfirmProductInstance confirmProductInstance) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public CreateCustomerGatewayResponse createCustomerGateway(CreateCustomerGateway createCustomerGateway) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public CreateDhcpOptionsResponse createDhcpOptions(CreateDhcpOptions createDhcpOptions) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public CreatePlacementGroupResponse createPlacementGroup(CreatePlacementGroup createPlacementGroup) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public CreateSpotDatafeedSubscriptionResponse createSpotDatafeedSubscription(CreateSpotDatafeedSubscription createSpotDatafeedSubscription) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public CreateSubnetResponse createSubnet(CreateSubnet createSubnet) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public CreateVpcResponse createVpc(CreateVpc createVpc) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public CreateVpnConnectionResponse createVpnConnection(CreateVpnConnection createVpnConnection) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public CreateVpnGatewayResponse createVpnGateway(CreateVpnGateway createVpnGateway) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DeactivateLicenseResponse deactivateLicense(DeactivateLicense deactivateLicense) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DeleteCustomerGatewayResponse deleteCustomerGateway(DeleteCustomerGateway deleteCustomerGateway) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DeleteDhcpOptionsResponse deleteDhcpOptions(DeleteDhcpOptions deleteDhcpOptions) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DeletePlacementGroupResponse deletePlacementGroup(DeletePlacementGroup deletePlacementGroup) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DeleteSpotDatafeedSubscriptionResponse deleteSpotDatafeedSubscription(DeleteSpotDatafeedSubscription deleteSpotDatafeedSubscription) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DeleteSubnetResponse deleteSubnet(DeleteSubnet deleteSubnet) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DeleteVpcResponse deleteVpc(DeleteVpc deleteVpc) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DeleteVpnConnectionResponse deleteVpnConnection(DeleteVpnConnection deleteVpnConnection) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DeleteVpnGatewayResponse deleteVpnGateway(DeleteVpnGateway deleteVpnGateway) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DescribeBundleTasksResponse describeBundleTasks(DescribeBundleTasks describeBundleTasks) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribeConversionTasksResponse describeConversionTasks(DescribeConversionTasks describeConversionTasks) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DescribeCustomerGatewaysResponse describeCustomerGateways(DescribeCustomerGateways describeCustomerGateways) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribeDhcpOptionsResponse describeDhcpOptions(DescribeDhcpOptions describeDhcpOptions) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DescribeLicensesResponse describeLicenses(DescribeLicenses describeLicenses) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribePlacementGroupsResponse describePlacementGroups(DescribePlacementGroups describePlacementGroups) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DescribeRegionsResponse describeRegions(DescribeRegions describeRegions) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DescribeReservedInstancesResponse describeReservedInstances(DescribeReservedInstances describeReservedInstances) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribeReservedInstancesOfferingsResponse describeReservedInstancesOfferings(DescribeReservedInstancesOfferings describeReservedInstancesOfferings) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DescribeSnapshotAttributeResponse describeSnapshotAttribute(DescribeSnapshotAttribute describeSnapshotAttribute) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DescribeSpotDatafeedSubscriptionResponse describeSpotDatafeedSubscription(DescribeSpotDatafeedSubscription describeSpotDatafeedSubscription) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribeSpotInstanceRequestsResponse describeSpotInstanceRequests(DescribeSpotInstanceRequests describeSpotInstanceRequests) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribeSpotPriceHistoryResponse describeSpotPriceHistory(DescribeSpotPriceHistory describeSpotPriceHistory) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribeSubnetsResponse describeSubnets(DescribeSubnets describeSubnets) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public DescribeVpcsResponse describeVpcs(DescribeVpcs describeVpcs) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribeVpnConnectionsResponse describeVpnConnections(DescribeVpnConnections describeVpnConnections) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DescribeVpnGatewaysResponse describeVpnGateways(DescribeVpnGateways describeVpnGateways) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public DetachVpnGatewayResponse detachVpnGateway(DetachVpnGateway detachVpnGateway) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public GetConsoleOutputResponse getConsoleOutput(GetConsoleOutput getConsoleOutput) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public ImportInstanceResponse importInstance(ImportInstance importInstance) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public ImportVolumeResponse importVolume(ImportVolume importVolume) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public ModifyInstanceAttributeResponse modifyInstanceAttribute(ModifyInstanceAttribute modifyInstanceAttribute) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public ModifySnapshotAttributeResponse modifySnapshotAttribute(ModifySnapshotAttribute modifySnapshotAttribute) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
	
	public PurchaseReservedInstancesOfferingResponse purchaseReservedInstancesOffering(PurchaseReservedInstancesOffering purchaseReservedInstancesOffering) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public RequestSpotInstancesResponse requestSpotInstances(RequestSpotInstances requestSpotInstances) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public ResetInstanceAttributeResponse resetInstanceAttribute(ResetInstanceAttribute resetInstanceAttribute) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}

	public ResetSnapshotAttributeResponse resetSnapshotAttribute(ResetSnapshotAttribute resetSnapshotAttribute) {
		throw new EC2ServiceException(ClientError.Unsupported, "This operation is not available");
	}
}
