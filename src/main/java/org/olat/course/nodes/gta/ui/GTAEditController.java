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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAEditController extends ActivateableTabbableDefaultController {

	public static final String PANE_TAB_WORKLOW = "pane.tab.workflow";
	public static final String PANE_TAB_ASSIGNMENT = "pane.tab.assignment";
	public static final String PANE_TAB_SUBMISSION = "pane.tab.submission";
	public static final String PANE_TAB_PEER_REVIEW = "pane.tab.peer.review";
	public static final String PANE_TAB_REVIEW_AND_CORRECTIONS = "pane.tab.review";
	public static final String PANE_TAB_GRADING = "pane.tab.grading";
	public static final String PANE_TAB_SOLUTIONS = "pane.tab.solutions";
	public static final String PANE_TAB_HIGHSCORE = "pane.tab.highscore";
	private static final String[] paneKeys = {
			PANE_TAB_WORKLOW, PANE_TAB_ASSIGNMENT, PANE_TAB_SUBMISSION, PANE_TAB_PEER_REVIEW,
			PANE_TAB_REVIEW_AND_CORRECTIONS, PANE_TAB_GRADING, PANE_TAB_SOLUTIONS };
	private int workflowPos;
	private int assignmentPos;
	private int submissionPos;
	private int peerReviewPos;
	private int revisionPos;
	private int gradingPos;
	private int solutionsPos;
	private int highScoreTabPosition;
	
	private TabbedPane myTabbedPane;
	private final BreadcrumbPanel stackPanel;
	
	private GTAWorkflowEditController workflowCtrl;
	private GTAPeerReviewEditController peerReviewCtrl;
	private GTARevisionAndCorrectionEditController revisionCtrl;
	private GTAAssignmentEditController assignmentCtrl;
	private GTASubmissionEditController submissionCtrl;
	private GTAEditAssessmentConfigController assessmentCtrl;
	private GTASampleSolutionsEditController solutionsCtrl;
	private HighScoreEditController highScoreNodeConfigController;
	
	private final GTACourseNode gtaNode;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment euce;
	private final CourseEnvironment courseEnv;
	private final RepositoryEntry courseEntry;
	
	public GTAEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, GTACourseNode gtaNode,
			ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		this.euce = euce;
		this.gtaNode = gtaNode;
		this.stackPanel = stackPanel;
		courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		courseEnv = course.getCourseEnvironment();
		config = gtaNode.getModuleConfiguration();

		//workflow
		workflowCtrl = new GTAWorkflowEditController(ureq, getWindowControl(), gtaNode, euce.getCourseEditorEnv());
		listenTo(workflowCtrl);
		//assignment
		assignmentCtrl = new GTAAssignmentEditController(ureq, getWindowControl(), gtaNode, config, courseEnv, false);
		listenTo(assignmentCtrl);
		//submission
		submissionCtrl = new GTASubmissionEditController(ureq, getWindowControl(), config);
		listenTo(submissionCtrl);
		//feedback: peer review or revisions
		peerReviewCtrl = new GTAPeerReviewEditController(ureq, getWindowControl(), stackPanel, config);
		listenTo(peerReviewCtrl);
		revisionCtrl = new GTARevisionAndCorrectionEditController(ureq, getWindowControl(), config);
		listenTo(revisionCtrl);
		//grading
		assessmentCtrl = createManualAssessmentCtrl(ureq, course);
		listenTo(assessmentCtrl);
		//solutions
		solutionsCtrl = new GTASampleSolutionsEditController(ureq, getWindowControl(), gtaNode, courseEnv, false);
		listenTo(solutionsCtrl);
		//highscore
		highScoreNodeConfigController = new HighScoreEditController(ureq, wControl, config, course);
		listenTo(highScoreNodeConfigController);
		if ("group".equals(config.get(GTACourseNode.GTASK_TYPE))) {
			highScoreNodeConfigController.setFormInfoMessage("highscore.forminfo", getTranslator());			
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		workflowPos = tabbedPane.addTab(translate(PANE_TAB_WORKLOW), "o_sel_gta_workflow", workflowCtrl.getInitialComponent());
		assignmentPos = tabbedPane.addTab(translate(PANE_TAB_ASSIGNMENT), "o_sel_gta_assignment", assignmentCtrl.getInitialComponent());
		submissionPos = tabbedPane.addTab(translate(PANE_TAB_SUBMISSION), "o_sel_gta_submission", submissionCtrl.getInitialComponent());
		peerReviewPos = tabbedPane.addTab(translate(PANE_TAB_PEER_REVIEW), peerReviewCtrl.getInitialComponent());
		revisionPos = tabbedPane.addTab(translate(PANE_TAB_REVIEW_AND_CORRECTIONS), revisionCtrl.getInitialComponent());
		gradingPos = tabbedPane.addTab(translate(PANE_TAB_GRADING), "o_sel_gta_assessment", assessmentCtrl.getInitialComponent());
		solutionsPos = tabbedPane.addTab(translate(PANE_TAB_SOLUTIONS), "o_sel_gta_solution", solutionsCtrl.getInitialComponent());
		highScoreTabPosition = myTabbedPane.addTab(translate(PANE_TAB_HIGHSCORE), highScoreNodeConfigController.getInitialComponent());
		updateEnabledDisabledTabs();
	}
	
	private void updateEnabledDisabledTabs() {
		myTabbedPane.setEnabled(assignmentPos, config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT));
		myTabbedPane.setEnabled(submissionPos, config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT));
		boolean peerReviewEnabled = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW);
		boolean correctionEnabled = config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION);
		myTabbedPane.setEnabled(peerReviewPos, peerReviewEnabled);
		myTabbedPane.setEnabled(revisionPos, correctionEnabled);
		myTabbedPane.setVisible(revisionPos, !peerReviewEnabled);
		myTabbedPane.setVisible(peerReviewPos, peerReviewEnabled);
		myTabbedPane.setEnabled(gradingPos, config.getBooleanSafe(GTACourseNode.GTASK_GRADING));
		myTabbedPane.setEnabled(solutionsPos, config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));
		myTabbedPane.setEnabled(highScoreTabPosition, config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD));
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(workflowCtrl == source) {
			 if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
				updateEnabledDisabledTabs();
				updateAssessment(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(workflowCtrl);
				workflowCtrl = new GTAWorkflowEditController(ureq, getWindowControl(), gtaNode, euce.getCourseEditorEnv());
				listenTo(workflowCtrl);
				myTabbedPane.replaceTab(workflowPos, workflowCtrl.getInitialComponent());
			} else if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(assignmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(assignmentCtrl);
				assignmentCtrl = new GTAAssignmentEditController(ureq, getWindowControl(), gtaNode, config, courseEnv, false);
				listenTo(assignmentCtrl);
				myTabbedPane.replaceTab(assignmentPos, assignmentCtrl.getInitialComponent());
			}
		} else if(submissionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				if(revisionCtrl != null) {
					revisionCtrl.updateDefaultNumbersOfDocuments();
				}
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(submissionCtrl);
				submissionCtrl = new GTASubmissionEditController(ureq, getWindowControl(), config);
				listenTo(submissionCtrl);
				myTabbedPane.replaceTab(submissionPos, submissionCtrl.getInitialComponent());
			}
		} else if(revisionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(revisionCtrl);
				revisionCtrl = new GTARevisionAndCorrectionEditController(ureq, getWindowControl(), config);
				listenTo(revisionCtrl);
				myTabbedPane.replaceTab(revisionPos, revisionCtrl.getInitialComponent());
			}
		} else if(peerReviewCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(peerReviewCtrl);
				peerReviewCtrl = new GTAPeerReviewEditController(ureq, getWindowControl(), stackPanel, config);
				listenTo(peerReviewCtrl);
				myTabbedPane.replaceTab(peerReviewPos, peerReviewCtrl.getInitialComponent());
			}
		} else if(assessmentCtrl == source) {
			if (event == Event.DONE_EVENT){
				assessmentCtrl.updateModuleConfiguration(config);
				updateEnabledDisabledTabs();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
			} else if(event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(assessmentCtrl);
				assessmentCtrl = createManualAssessmentCtrl(ureq, CourseFactory.loadCourse(courseEntry));
				listenTo(assessmentCtrl);
				myTabbedPane.replaceTab(gradingPos, assessmentCtrl.getInitialComponent());
			} else if(event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if(solutionsCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == highScoreNodeConfigController) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
		
		super.event(ureq, source, event);
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		super.dispatchEvent(ureq, source, event);
		if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
			workflowCtrl.onNodeConfigChanged();
		}
	}
	
	private void updateAssessment(UserRequest ureq) {
		if(assessmentCtrl != null) {
			assessmentCtrl.update(ureq);
		}
	}

	private GTAEditAssessmentConfigController createManualAssessmentCtrl(UserRequest ureq, ICourse course) {
		return new GTAEditAssessmentConfigController(ureq, getWindowControl(), stackPanel, gtaNode, course);
	}
}