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
package org.olat.core.commons.services.tag.model;

import java.util.Date;

import org.olat.core.commons.services.tag.TagInfo;

/**
 * 
 * Initial date: 6 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TagInfoImpl implements TagInfo {

	private final Long key;
	private final Date creationDate;
	private final String displayName;
	private final Long count;
	private boolean selected;

	public TagInfoImpl(Long key, Date creationDate, String displayName, Long count, Long numSelected) {
		this(key, creationDate, displayName, count, numSelected > 0);
	}
	
	public TagInfoImpl(Long key, Date creationDate, String displayName, Long count, boolean selected) {
		this.key = key;
		this.creationDate = creationDate;
		this.displayName = displayName;
		this.count = count;
		this.selected = selected;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public Long getCount() {
		return count;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
