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
package org.olat.course.nodes.form;

import java.util.Collection;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 1 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormParticipationSearchParams {
	
	public enum Status { notStarted, inProgress, done }
	
	private Identity identity;
	private boolean admin;
	private boolean coach;
	private RepositoryEntryRef courseEntry;
	private Collection<AssessmentObligation> obligations;
	private Collection<Status> status;
	private Collection<ParticipantType> participants;
	private Collection<Identity> fakeParticipants;

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isCoach() {
		return coach;
	}

	public void setCoach(boolean coach) {
		this.coach = coach;
	}

	public RepositoryEntryRef getCourseEntry() {
		return courseEntry;
	}

	public void setCourseEntry(RepositoryEntryRef courseEntry) {
		this.courseEntry = courseEntry;
	}

	public Collection<AssessmentObligation> getObligations() {
		return obligations;
	}

	public void setObligations(Collection<AssessmentObligation> obligations) {
		this.obligations = obligations;
	}

	public Collection<Status> getStatus() {
		return status;
	}

	public void setStatus(Collection<Status> status) {
		this.status = status;
	}

	public Collection<ParticipantType> getParticipants() {
		return participants;
	}

	public void setParticipants(Collection<ParticipantType> participants) {
		this.participants = participants;
	}

	public Collection<Identity> getFakeParticipants() {
		return fakeParticipants;
	}

	public void setFakeParticipants(Collection<Identity> fakeParticipants) {
		this.fakeParticipants = fakeParticipants;
	}

}
