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
package org.olat.course.assessment.ui.tool;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.03.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeToApplyGradeSmallController extends CourseNodeToReviewAbstractSmallController {
	
	private static final Supplier<AssessedIdentityListState> IDENTITY_FILTER = 
			() -> new AssessedIdentityListState(null, null, null, null, null, null, IdentityListCourseNodeController.ALL_TAB_ID, true);
	
	private final List<String> manualGradeSubIdents;
	
	@Autowired
	private AssessmentToolManager assessmentToolManager;

	public CourseNodeToApplyGradeSmallController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback, List<String> manualGradeSubIdents) {
		super(ureq, wControl, courseEntry, assessmentCallback);
		this.manualGradeSubIdents = manualGradeSubIdents;
	}
	
	@Override
	protected String getIconCssClass() {
		return "o_icon_grade";
	}

	@Override
	protected String getTitleI18nKey() {
		return "grades.to.apply";
	}
	
	@Override
	protected String getLinkNumberI18nKey() {
		return "grades.to.apply.number";
	}

	@Override
	protected String getTableEmptyI18nKey() {
		return "grades.to.apply.empty";
	}

	@Override
	protected Map<String, List<AssessmentEntry>> loadNodeIdentToEntries(Collection<ParticipantType> participants) {
		Map<String, List<AssessmentEntry>> nodeIdentToEntries = new HashMap<>(manualGradeSubIdents.size());
		for (String subIdent : manualGradeSubIdents) {
			nodeIdentToEntries.put(subIdent, loadAssessmentEntries(subIdent, participants));
		}
		return nodeIdentToEntries;
	}
	
	private List<AssessmentEntry> loadAssessmentEntries(String subIdent, Collection<ParticipantType> participants) {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, subIdent, null, assessmentCallback);
		params.setParticipantTypes(participants);
		params.setScoreNull(Boolean.FALSE);
		params.setGradeNull(Boolean.TRUE);
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);
		return assessmentToolManager.getAssessmentEntries(getIdentity(), params, null);
	}

	@Override
	protected Supplier<AssessedIdentityListState> getIdentityFilter() {
		return IDENTITY_FILTER;
	}

}