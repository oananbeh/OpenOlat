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
package org.olat.course.nodes.videotask;

import org.olat.core.CoreSpringFactory;
import org.olat.course.assessment.handler.ModuleAssessmentConfig;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.videotask.ui.VideoTaskEditController;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskAssessmentConfig extends ModuleAssessmentConfig {

	private final String nodeIdent;
	private final RepositoryEntryRef courseEntry;
	
	public VideoTaskAssessmentConfig(RepositoryEntryRef courseEntry, VideoTaskCourseNode courseNode) {
		super(courseNode, courseNode.getModuleConfiguration());
		nodeIdent = courseNode.getIdent();
		this.courseEntry = courseEntry;
	}
	
	@Override
	public boolean isAssessable() {
		String mode = config.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE);
		return VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode);
	}
	
	@Override
	public Mode getScoreMode() {
		String mode = config.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE);
		if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)
				&& config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD)) {
			return Mode.setByNode;
		}
		return Mode.none;
	}
	
	@Override
	public Mode getPassedMode() {
		if (hasGrade() && Mode.none != getScoreMode()) {
			if (CoreSpringFactory.getImpl(GradeService.class).hasPassed(courseEntry, nodeIdent)) {
				return Mode.setByNode;
			}
			return Mode.none;
		}
		return super.getPassedMode();
	}
	
	@Override
	public boolean ignoreInCourseAssessment() {
		return config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
	}
	
	@Override
	public boolean hasMaxAttempts() {
		int attempts = config.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, 0);
		return attempts > 0;
	}

	@Override
	public Integer getMaxAttempts() {
		int attempts = config.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, 0);
		if(attempts > 0) {
			return Integer.valueOf(attempts);
		}
		return null;
	}

	@Override
	public boolean hasAttempts() {
		return true;
	}

	@Override
	public boolean hasStatus() {
		return true;
	}

	@Override
	public Boolean getInitialUserVisibility(boolean done, boolean coachCanNotEdit) {
		String mode = config.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE);
		if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)) {
			return config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE) != null;
		}
		return Boolean.TRUE;
	}

	@Override
	public boolean isEditable() {
		// manual scoring fields can be edited manually
		return true;
	}

	@Override
	public boolean isBulkEditable() {
		return true;
	}

	@Override
	public boolean hasEditableDetails() {
		String mode = config.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE);
		return VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode);
	}
}
