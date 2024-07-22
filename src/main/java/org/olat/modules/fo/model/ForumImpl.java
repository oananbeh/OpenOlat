/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.modules.fo.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.fo.Forum;

/**
 * Implementation of the Forum Interface 
 *
 * @author Felix Jost
 */
@Entity(name="forum")
@Table(name="o_forum")
public class ForumImpl implements Forum, CreateInfo, Persistable{

	private static final long serialVersionUID = 3240047228287740990L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="forum_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="f_refresname", nullable=true, insertable=true, updatable=false)
	private String refResName;
	@Column(name="f_refresid", nullable=true, insertable=true, updatable=false)
	private Long refResId;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	@Transient
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(Forum.class);
	}

	@Override
	public Long getResourceableId() {
		Long id = getKey();
		if (id == null) {
			throw new AssertException("getId() must not be called before an id has been generated by the persister");				
		}
		return id;
	}
	
	@Override
	public void setReference(OLATResourceable ores) {
		if (ores != null) {
			this.refResName = ores.getResourceableTypeName();
			this.refResId = ores.getResourceableId();
		} else {
			this.refResName = null;
			this.refResId = null;
		}
	}
	
	@Override
	public OLATResourceable getReference() {
		return new OLATResourceable() {
			
			@Override
			public String getResourceableTypeName() {
				return refResName;
			}
			
			@Override
			public Long getResourceableId() {
				return refResId;
			}
		};
	}
	
	@Override
	public int hashCode() {
		return key == null ? 835245 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ForumImpl) {
			ForumImpl other = (ForumImpl) obj;
			return getKey().equals(other.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}