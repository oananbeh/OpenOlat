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
package org.olat.course.assessment;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 30 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionChangeEvent extends MultiUserEvent  {

	private static final long serialVersionUID = 8420592773385290029L;

	public static final String UPDATE = "assessment-inspection-update-notification";


	private AssessmentInspection inspection;
	private Long assessedIdentityKey;
	
	public AssessmentInspectionChangeEvent(String cmd, AssessmentInspection inspection, Long assessedIdentityKey) {
		super(cmd, 9);
		this.inspection = inspection;
		this.assessedIdentityKey = assessedIdentityKey;
	}

	public AssessmentInspection getAssessementInspection() {
		return inspection;
	}

	public Long getAssessedIdentityKey() {
		return assessedIdentityKey;
	}
	
	public boolean sameInspection(AssessmentInspection otherInspection) {
		return inspection != null && otherInspection != null
				&& inspection.getKey().equals(otherInspection.getKey());
	}
}
