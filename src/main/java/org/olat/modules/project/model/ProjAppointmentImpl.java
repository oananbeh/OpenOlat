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
package org.olat.modules.project.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;

/**
 * 
 * Initial date: 13 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="projappointment")
@Table(name="o_proj_appointment")
public class ProjAppointmentImpl implements ProjAppointment, Persistable  {
	
	private static final long serialVersionUID = 5579422252770658249L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="p_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;
	@Column(name="p_event_id", nullable=false, insertable=true, updatable=true)
	private String eventId;
	@Column(name="p_recurrence_id", nullable=true, insertable=true, updatable=true)
	private String recurrenceId;
	@Column(name="p_start_date", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Column(name="p_end_date", nullable=true, insertable=true, updatable=true)
	private Date endDate;
	@Column(name="p_subject", nullable=true, insertable=true, updatable=true)
	private String subject;
	@Column(name="p_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="p_location", nullable=true, insertable=true, updatable=true)
	private String location;
	@Column(name="p_color", nullable=true, insertable=true, updatable=true)
	private String color;
	@Column(name="p_all_day", nullable=false, insertable=true, updatable=true)
	private boolean allDay;
	@Column(name="p_recurrence_rule", nullable=true, insertable=true, updatable=true)
	private String recurrenceRule;
	@Column(name="p_recurrence_exclusion", nullable=true, insertable=true, updatable=true)
	private String recurrenceExclusion;
	
	@ManyToOne(targetEntity=ProjArtefactImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_artefact", nullable=true, insertable=true, updatable=true)
	private ProjArtefact artefact;
	
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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getEventId() {
		return eventId;
	}

	@Override
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	@Override
	public String getRecurrenceId() {
		return recurrenceId;
	}

	@Override
	public void setRecurrenceId(String recurrenceId) {
		this.recurrenceId = recurrenceId;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public boolean isAllDay() {
		return allDay;
	}

	@Override
	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	@Override
	public String getRecurrenceRule() {
		return recurrenceRule;
	}

	@Override
	public void setRecurrenceRule(String recurrenceRule) {
		this.recurrenceRule = recurrenceRule;
	}

	@Override
	public String getRecurrenceExclusion() {
		return recurrenceExclusion;
	}

	@Override
	public void setRecurrenceExclusion(String recurrenceExclusion) {
		this.recurrenceExclusion = recurrenceExclusion;
	}

	@Override
	public ProjArtefact getArtefact() {
		return artefact;
	}

	public void setArtefact(ProjArtefact artefact) {
		this.artefact = artefact;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjAppointmentImpl other = (ProjAppointmentImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
