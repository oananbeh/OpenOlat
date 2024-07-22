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
package org.olat.course.editor.overview;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;

/**
 * 
 * Initial date: 16 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewRow implements FlexiTreeTableNode, IndentedCourseNode {
	
	private final CourseEditorTreeNode editorNode;
	private final CourseNode courseNode;
	private final int recursionLevel;
	private OverviewRow parent;
	private boolean hasChildren;
	private String translatedDisplayOption;
	private Integer duration;
	private String translatedObligation;
	private DueDateConfig start;
	private DueDateConfig end;
	private String translatedTrigger;
	private AssessmentConfig assessmentConfig;
	private FormToggle incorporateInCourseAssessmentEl;
	private FormLink scoreScalingEl;
	
	public OverviewRow(CourseEditorTreeNode editorNode, int recursionLevel) {
		this(editorNode, editorNode.getCourseNode(), recursionLevel);
	}
	
	public OverviewRow(CourseEditorTreeNode editorNode, CourseNode courseNode, int recursionLevel) {
		this.editorNode = editorNode;
		this.courseNode = courseNode;
		this.recursionLevel = recursionLevel;
	}

	@Override
	public int getRecursionLevel() {
		return recursionLevel;
	}

	@Override
	public String getType() {
		return getCourseNode().getType();
	}

	@Override
	public String getShortTitle() {
		return getCourseNode().getShortName();
	}

	@Override
	public String getLongTitle() {
		return getCourseNode().getLongTitle();
	}

	@Override
	public String getCrump() {
		return null;
	}

	public void setParent(OverviewRow parent) {
		this.parent = parent;
		if (parent != null) {
			parent.hasChildren = true;
		}
	}

	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return hasChildren;
	}

	public CourseEditorTreeNode getEditorNode() {
		return editorNode;
	}

	public CourseNode getCourseNode() {
		return courseNode;
	}

	public String getTranslatedDisplayOption() {
		return translatedDisplayOption;
	}

	public void setTranslatedDisplayOption(String translatedDisplayOption) {
		this.translatedDisplayOption = translatedDisplayOption;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getTranslatedObligation() {
		return translatedObligation;
	}

	public void setTranslatedObligation(String translatedObligation) {
		this.translatedObligation = translatedObligation;
	}

	public DueDateConfig getStart() {
		return start;
	}

	public void setStart(DueDateConfig start) {
		this.start = start;
	}

	public DueDateConfig getEnd() {
		return end;
	}

	public void setEnd(DueDateConfig end) {
		this.end = end;
	}

	public String getTranslatedTrigger() {
		return translatedTrigger;
	}

	public void setTranslatedTrigger(String translatedTrigger) {
		this.translatedTrigger = translatedTrigger;
	}

	public FormToggle getIncorporateInCourseAssessmentEl() {
		return incorporateInCourseAssessmentEl;
	}

	public void setIncorporateInCourseAssessmentEl(FormToggle incorporateInCourseAssessmentEl) {
		this.incorporateInCourseAssessmentEl = incorporateInCourseAssessmentEl;
	}

	public FormLink getScoreScalingEl() {
		return scoreScalingEl;
	}

	public void setScoreScalingEl(FormLink scoreScalingEl) {
		this.scoreScalingEl = scoreScalingEl;
	}

	public AssessmentConfig getAssessmentConfig() {
		return assessmentConfig;
	}

	public void setAssessmentConfig(AssessmentConfig assessmentConfig) {
		this.assessmentConfig = assessmentConfig;
	}
	
}
