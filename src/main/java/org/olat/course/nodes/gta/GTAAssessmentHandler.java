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
package org.olat.course.nodes.gta;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.ui.GTAIdentityGroupListCourseNodeController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTAAssessmentHandler extends AbstractGTAAssessmentHandler {

	@Override
	public String acceptCourseNodeType() {
		return GTACourseNode.TYPE_GROUP;
	}
	
	@Override
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseNode courseNode, RepositoryEntry courseEntry,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		return new GTAIdentityGroupListCourseNodeController(ureq, wControl, stackPanel, courseEntry, courseNode,
				coachCourseEnv, toolContainer, assessmentCallback);
	}

}
