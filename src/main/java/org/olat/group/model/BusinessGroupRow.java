/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.group.model;

import java.util.Date;
import java.util.List;

import org.olat.core.id.context.BusinessControlFactory;
import org.olat.group.BusinessGroupLifecycle;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.repository.RepositoryEntryShort;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;

/**
 * 
 * Initial date: 05.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupRow implements BusinessGroupShort, BusinessGroupLifecycle {
	
	private final Long key;
	private final Date creationDate;
	private final String name;
	private final String description;
	private final String externalId;
	private final Date lastUsage;
	private final Long resourceKey;
	private final String technicalType;

	private final Integer maxParticipants;
	private final Boolean waitingListEnabled;
	private final Boolean autoCloseRanksEnabled;
	private final BusinessGroupManagedFlag[] managedFlags;
	
	private final BusinessGroupStatusEnum status;
	private boolean excludeFromAutoLifecycle;
	private final Date inactivationDate;
	private final Date inactivationEmailDate;
	private final Date reactivationDate;
	private final Date softDeleteEmailDate;
	private final Date softDeleteDate;
	
	private boolean marked;
	
	private final String url;
	
	private BusinessGroupMembershipImpl member;
	
	private List<PriceMethodBundle> bundles;
	protected List<RepositoryEntryShort> resources;

	public BusinessGroupRow(BusinessGroupToSearch businessGroup) {
		key = businessGroup.getKey();
		creationDate = businessGroup.getCreationDate();
		name = businessGroup.getName();
		lastUsage = businessGroup.getLastUsage();
		description = businessGroup.getDescription();
		externalId = businessGroup.getExternalId();
		managedFlags = businessGroup.getManagedFlags();
		resourceKey = businessGroup.getResource().getKey();
		waitingListEnabled = businessGroup.getWaitingListEnabled();
		autoCloseRanksEnabled = businessGroup.getAutoCloseRanksEnabled();
		maxParticipants = businessGroup.getMaxParticipants();
		technicalType = businessGroup.getTechnicalType();
		
		// life cycle
		status = businessGroup.getGroupStatus();
		excludeFromAutoLifecycle = businessGroup.isExcludeFromAutoLifecycle();
		inactivationDate = businessGroup.getInactivationDate();
		inactivationEmailDate = businessGroup.getInactivationEmailDate();
		reactivationDate = businessGroup.getReactivationDate();
		softDeleteDate = businessGroup.getSoftDeleteDate();
		softDeleteEmailDate = businessGroup.getSoftDeleteEmailDate();
		
		String path = "[BusinessGroup:" + businessGroup.getKey() + "]";
		url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
	}
	
	public BusinessGroupRow(BusinessGroupRow businessGroup) {
		key = businessGroup.getKey();
		creationDate = businessGroup.getCreationDate();
		name = businessGroup.getName();
		lastUsage = businessGroup.getLastUsage();
		description = businessGroup.getDescription();
		externalId = businessGroup.getExternalId();
		managedFlags = businessGroup.getManagedFlags();
		resourceKey = businessGroup.getResourceKey();
		waitingListEnabled = businessGroup.isWaitingListEnabled();
		autoCloseRanksEnabled = businessGroup.isAutoCloseRanksEnabled();
		maxParticipants = businessGroup.getMaxParticipants();
		technicalType = businessGroup.getTechnicalType();
		
		// life cycle
		status = businessGroup.getGroupStatus();
		excludeFromAutoLifecycle = businessGroup.isExcludeFromAutoLifecycle();
		inactivationDate = businessGroup.getInactivationDate();
		inactivationEmailDate = businessGroup.getInactivationEmailDate();
		reactivationDate = businessGroup.getReactivationDate();
		softDeleteDate = businessGroup.getSoftDeleteDate();
		softDeleteEmailDate = businessGroup.getSoftDeleteEmailDate();
		
		String path = "[BusinessGroup:" + businessGroup.getKey() + "]";
		url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
	}
	
	public boolean isWaitingListEnabled() {
		return  waitingListEnabled == null ? false : waitingListEnabled.booleanValue();
	}
	
	public boolean isAutoCloseRanksEnabled() {
		return autoCloseRanksEnabled == null ? false : autoCloseRanksEnabled.booleanValue();
	}
	
	public Integer getMaxParticipants() {
		return maxParticipants;
	}
	
	public String getUrl() {
		return url;
	}

	@Override
	public Long getKey() {
		return key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Long getResourceKey() {
		return resourceKey;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getResourceableTypeName() {
		return "BusinessGroup";
	}

	@Override
	public Long getResourceableId() {
		return key;
	}

	public String getDescription() {
		return description;
	}
	
	public String getTechnicalType() {
		return technicalType;
	}

	@Override
	public Date getLastUsage() {
		return lastUsage;
	}

	public String getExternalId() {
		return externalId;
	}

	@Override
	public BusinessGroupManagedFlag[] getManagedFlags() {
		return managedFlags;
	}
	
	public boolean isManaged() {
		return managedFlags != null && managedFlags.length > 0;
	}

	public List<PriceMethodBundle> getBundles() {
		return bundles;
	}

	public void setBundles(List<PriceMethodBundle> bundles) {
		this.bundles = bundles;
	}

	public List<RepositoryEntryShort> getResources() {
		return resources;
	}

	public void setResources(List<RepositoryEntryShort> resources) {
		this.resources = resources;
	}
	
	public boolean hasResources() {
		return resources != null && !resources.isEmpty();
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public BusinessGroupMembershipImpl getMember() {
		return member;
	}

	public void setMember(BusinessGroupMembershipImpl member) {
		this.member = member;
	}
	
	@Override
	public BusinessGroupStatusEnum getGroupStatus() {
		return status;
	}
	
	@Override
	public boolean isExcludeFromAutoLifecycle() {
		return excludeFromAutoLifecycle;
	}
	
	public void setExcludedFromAutoLifecycle(boolean excluded) {
		this.excludeFromAutoLifecycle = excluded;
	}

	@Override
	public Date getInactivationDate() {
		return inactivationDate;
	}

	@Override
	public Date getInactivationEmailDate() {
		return inactivationEmailDate;
	}

	@Override
	public Date getReactivationDate() {
		return reactivationDate;
	}
	
	@Override
	public Date getSoftDeleteEmailDate() {
		return softDeleteEmailDate;
	}

	@Override
	public Date getSoftDeleteDate() {
		return softDeleteDate;
	}

	@Override
	public int hashCode() {
		return key == null ? -54851 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		
		if(obj instanceof BusinessGroupRow) {
			BusinessGroupRow row = (BusinessGroupRow)obj;
			return key != null && key.equals(row.getKey());
		}
		return false;
	}
}
