/**

 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.archiver.ArchiveResource;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessedIdentityLargeInfosController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.ui.GTACoachedParticipantListController.MakedEvent;
import org.olat.course.nodes.gta.ui.component.DownloadDocumentMapper;
import org.olat.course.nodes.gta.ui.events.SelectBusinessGroupEvent;
import org.olat.course.nodes.gta.ui.events.SelectIdentityEvent;
import org.olat.course.nodes.ms.MSStatisticController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GTACoachSelectionController extends BasicController implements Activateable2 {

	private Controller coachingCtrl;
	private GTACoachedGroupListController groupListCtrl;
	private CoachAssignmentListController coachAssignmentListCtrl;
	private GTACoachedParticipantListController participantListCtrl;
	private ContextualSubscriptionController contextualSubscriptionCtr;
	
	private final Link backLink;
	private final Link statsLink;
	private final Link downloadButton;
	private final Link nextIdentityLink;
	private final Link previousIdentityLink;
	private final Link coachAssignmentButton;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel assessedIdentityStackPanel;

	private final String solutionMapperUri;
	private final DisplayOrDownloadComponent solutionDownloadCmp;
	
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	private final UserCourseEnvironment coachCourseEnv;
	private final AssessmentConfig assessmentConfig;
	private boolean markedOnly = false;
	
	private MSStatisticController statsCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public GTACoachSelectionController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.coachCourseEnv = coachCourseEnv;
		this.courseEnv = coachCourseEnv.getCourseEnvironment();
		assessmentConfig = courseAssessmentService
				.getAssessmentConfig(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode);
		
		mainVC = createVelocityContainer("coach_selection");
		backLink = LinkFactory.createLinkBack(mainVC, this);

		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		File solutionsDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
		solutionMapperUri = registerMapper(ureq, new DownloadDocumentMapper(solutionsDir));
		solutionDownloadCmp = new DisplayOrDownloadComponent("download", null);
		mainVC.put("solutionDownload", solutionDownloadCmp);
		
		coachAssignmentButton = LinkFactory.createButton("bulk.coach.assignment", mainVC, this);
		coachAssignmentButton.setElementCssClass("o_sel_assessment_bulk_coach_assignment");
		coachAssignmentButton.setVisible(isCoachAssignmentAvailable()
				&& GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE)));
		
		downloadButton = LinkFactory.createButton("bulk.download.title", mainVC, this);
		downloadButton.setTranslator(getTranslator());
		downloadButton.setVisible(isDownloadAvailable());
		
		statsLink = LinkFactory.createButton("tool.stats", mainVC, this);
		statsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_statistics_tool");
		statsLink.setVisible(assessmentConfig.hasAssessmentForm()
				&& GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE)));

		assessedIdentityStackPanel = new TooledStackedPanel("gta-assessed-identity-stack", getTranslator(), this);
		assessedIdentityStackPanel.setNeverDisposeRootController(true);
		assessedIdentityStackPanel.setInvisibleCrumb(0);
		assessedIdentityStackPanel.setShowCloseLink(true, false);
		assessedIdentityStackPanel.pushController(translate("root.participant"), null, new Object());
		
		previousIdentityLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousIdentityLink.setTitle(translate("command.previous"));
		assessedIdentityStackPanel.addTool(previousIdentityLink, Align.rightEdge, true, "o_tool_previous");
		nextIdentityLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextIdentityLink.setTitle(translate("command.next"));
		assessedIdentityStackPanel.addTool(nextIdentityLink, Align.rightEdge, true, "o_tool_next");
		
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<BusinessGroup> groups;
			CourseGroupManager gm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
			if(coachCourseEnv.isAdmin()) {
				groups = gm.getAllBusinessGroups();
			} else {
				groups = coachCourseEnv.getCoachedGroups();
			}
			
			groups = gtaManager.filterBusinessGroups(groups, gtaNode);
			
			groupListCtrl = new GTACoachedGroupListController(ureq, getWindowControl(), null, coachCourseEnv, gtaNode, groups);
			listenTo(groupListCtrl);
			mainVC.put("list", groupListCtrl.getInitialComponent());
			
			if(groups.size() == 1) {
				doSelectBusinessGroup(ureq, groups.get(0));
			}	
		} else {
			markedOnly = true; // Init marked tab
			participantListCtrl = new GTACoachedParticipantListController(ureq, getWindowControl(), coachCourseEnv, gtaNode, markedOnly);
			markedOnly = participantListCtrl.isMarkedFilterSelected(); // Marked is preferred but if empty, show all users
			listenTo(participantListCtrl);
			mainVC.put("list", participantListCtrl.getInitialComponent());
		}
		
		initSubscription(ureq);
		
		putInitialPanel(mainVC);
	}

	private void initSubscription(UserRequest ureq) {
		removeAsListenerAndDispose(contextualSubscriptionCtr);
		
		PublisherData publisherData = gtaManager.getPublisherData(courseEnv, gtaNode, markedOnly);
		SubscriptionContext subsContext = gtaManager.getSubscriptionContext(courseEnv, gtaNode, markedOnly);
		contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, publisherData);
		listenTo(contextualSubscriptionCtr);
		mainVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
	}
	
	private boolean isDownloadAvailable() {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		return config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)
				|| config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
	}
	
	private boolean isCoachAssignmentAvailable() {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		return config.getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT)
				&& !coachCourseEnv.isCourseReadOnly()
				&& coachCourseEnv.isAdmin();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(coachAssignmentListCtrl != null) {
				back(ureq);
			}
			return;
		}
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		Long key = entries.get(0).getOLATResourceable().getResourceableId();
		if("Identity".equalsIgnoreCase(type)) {
			if(participantListCtrl != null && participantListCtrl.hasIdentityKey(key)) {
				Identity selectedIdentity = securityManager.loadIdentityByKey(key);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doSelectParticipant(ureq, selectedIdentity).activate(ureq, subEntries, entries.get(0).getTransientState());
			}
		} else if("BusinessGroup".equalsIgnoreCase(type)) {
			if(groupListCtrl != null) {
				BusinessGroup group = groupListCtrl.getBusinessGroup(key);
				if(group != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					doSelectBusinessGroup(ureq, group).activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		} else if("Solution".equalsIgnoreCase(type) && entries.size() > 1) {
			String path = BusinessControlFactory.getInstance().getPath(entries.get(1));
			String url = solutionMapperUri + "/" + path;
			solutionDownloadCmp.triggerFileDownload(url);
		} else if("Assignments".equalsIgnoreCase(type)
				&& coachAssignmentButton != null && coachAssignmentButton.isVisible()) {
			doCoachAssignment(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(groupListCtrl == source) {
			if(event instanceof SelectBusinessGroupEvent selectEvent) {
				doSelectBusinessGroup(ureq, selectEvent.getBusinessGroup());
				backLink.setVisible(true);
			}
		} else if(participantListCtrl == source) {
			if(event instanceof SelectIdentityEvent selectEvent) {
				Identity selectedIdentity = securityManager.loadIdentityByKey(selectEvent.getIdentityKey());
				doSelectParticipant(ureq, selectedIdentity);
				backLink.setVisible(true);
			} else if (event instanceof MakedEvent makedEvent) {
				markedOnly = makedEvent.isMarked();
				initSubscription(ureq);
			}
		} else if(coachAssignmentListCtrl == source) {
			if(event == Event.DONE_EVENT) {
				reload(ureq);
				back(ureq);
				dbInstance.commit();
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event == Event.BACK_EVENT || event == Event.CANCELLED_EVENT) {
				back(ureq);
			}
		} else if(source instanceof GTAAssessedBusinessGroupController) {
			if(event == Event.BACK_EVENT) {
				back(ureq);
			}
		}
		
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			back(ureq);
		} else if(downloadButton == source) {
			doBulkDownload(ureq);
		} else if(coachAssignmentButton == source) {
			doCoachAssignment(ureq);
		} else if(nextIdentityLink == source) {
			doNextIdentity(ureq);
		} else if(previousIdentityLink == source) {
			doPreviousIdentity(ureq);
		} else if(statsLink == source) {
			doLaunchStatistics(ureq);
		} else if(assessedIdentityStackPanel == source) {
			if(event instanceof PopEvent) {
				back(ureq);
			}
		}
	}
	
	public void reload(UserRequest ureq) {
		if (participantListCtrl != null) {
			participantListCtrl.updateModel(ureq);
		}
		if (groupListCtrl != null) {
			groupListCtrl.updateModel(ureq);
		}
	}
	
	private void back(UserRequest ureq) {
		if(coachingCtrl != null) {
			mainVC.remove("selectionStack");
			mainVC.remove(coachingCtrl.getInitialComponent());
			removeAsListenerAndDispose(coachingCtrl);
			coachingCtrl = null;
		}
		if(coachAssignmentListCtrl != null) {
			assessedIdentityStackPanel.popController(coachAssignmentListCtrl);
			mainVC.remove("selectionStack");
			mainVC.remove(coachAssignmentListCtrl.getInitialComponent());
			removeAsListenerAndDispose(coachAssignmentListCtrl);
			coachAssignmentListCtrl = null;
		}
		if(statsCtrl != null) {
			assessedIdentityStackPanel.popController(statsCtrl);
			mainVC.remove("selectionStack");
			mainVC.remove(statsCtrl.getInitialComponent());
			removeAsListenerAndDispose(statsCtrl);
			statsCtrl = null;
		}
		backLink.setVisible(false);
		if (participantListCtrl != null) {
			participantListCtrl.updateModel(ureq);			
		}
		if (groupListCtrl != null) {
			groupListCtrl.updateModel(ureq);
		}
	}
	
	private void doBulkDownload(UserRequest ureq) {
		if (participantListCtrl != null) {
			ArchiveOptions asOptions = new ArchiveOptions();
			asOptions.setIdentities(getIdentitiesForBulkDownload(ureq));
			asOptions.setOnlySubmitted(true);
			OLATResource ores = courseEnv.getCourseGroupManager().getCourseResource();
			ArchiveResource resource = new ArchiveResource(gtaNode, ores, asOptions, getLocale());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		} else if (groupListCtrl != null) {
			OLATResource ores = courseEnv.getCourseGroupManager().getCourseResource();
			GroupBulkDownloadResource resource = new GroupBulkDownloadResource(gtaNode, ores, groupListCtrl.getCoachedGroups(), getLocale());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void doCoachAssignment(UserRequest ureq) {
		if(coachAssignmentListCtrl != null) {
			assessedIdentityStackPanel.popController(coachAssignmentListCtrl);
			removeAsListenerAndDispose(coachAssignmentListCtrl);
		}

		List<Identity> assessedIdentities = participantListCtrl.getAssessableIdentities();

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Assignments", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance()
				.createBusinessWindowControl(ores, null, getWindowControl());
		coachAssignmentListCtrl = new CoachAssignmentListController(ureq, bwControl,
				assessedIdentities, coachCourseEnv, courseEnv, gtaNode);
		listenTo(coachAssignmentListCtrl);
		
		assessedIdentityStackPanel.pushController(translate("coach.assignment"), coachAssignmentListCtrl);
		mainVC.put("selectionStack", assessedIdentityStackPanel);
		assessedIdentityStackPanel.getToolBar().setVisible(false);
	}
	
	private void doNextIdentity(UserRequest ureq) {
		assessedIdentityStackPanel.popController(coachingCtrl);
		removeAsListenerAndDispose(coachingCtrl);
		
		if(coachingCtrl instanceof GTAAssessedIdentityController assessedIdentityCtlr) {
			Identity currentIdentity = assessedIdentityCtlr.getAssessedIdentity();
			int nextIndex = participantListCtrl.indexOfIdentity(currentIdentity) + 1;
			int rowCount = participantListCtrl.numOfIdentities();
			if(nextIndex >= 0 && nextIndex < rowCount) {
				IdentityRef nextIdentity = participantListCtrl.getIdentity(nextIndex);
				doSelectParticipant(ureq, nextIdentity);
			} else if(rowCount > 0) {
				IdentityRef nextIdentity = participantListCtrl.getIdentity(0);
				doSelectParticipant(ureq, nextIdentity);
			}
		}
	}
	
	private void doPreviousIdentity(UserRequest ureq) {
		assessedIdentityStackPanel.popController(coachingCtrl);
		removeAsListenerAndDispose(coachingCtrl);
		
		if(coachingCtrl instanceof GTAAssessedIdentityController assessedIdentityCtlr) {
			Identity currentIdentity = assessedIdentityCtlr.getAssessedIdentity();
			int previousIndex = participantListCtrl.indexOfIdentity(currentIdentity) - 1;
			int rowCount = participantListCtrl.numOfIdentities();
			if(previousIndex >= 0 && previousIndex < rowCount) {
				IdentityRef nextIdentity = participantListCtrl.getIdentity(previousIndex);
				doSelectParticipant(ureq, nextIdentity);
			} else if(rowCount > 0) {
				IdentityRef nextIdentity = participantListCtrl.getIdentity(rowCount - 1);
				doSelectParticipant(ureq, nextIdentity);
			}
		}
	}

	private List<Identity> getIdentitiesForBulkDownload(UserRequest ureq) {
		List<Identity> identities = participantListCtrl.getAssessableIdentities();
		if (markedOnly) {
			RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
			List<Identity> markedIdentities =
					gtaManager.getMarks(entry, gtaNode, ureq.getIdentity()).stream()
							.map(IdentityMark::getParticipant)
							.collect(Collectors.toList());
			identities.retainAll(markedIdentities);
		}
		return identities;
	}
	
	private Activateable2 doSelectBusinessGroup(UserRequest ureq, BusinessGroup group) {
		removeAsListenerAndDispose(coachingCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.clone(group), null);
		coachingCtrl = new GTAAssessedBusinessGroupController(ureq, swControl, group);
		listenTo(coachingCtrl);
		mainVC.put("selection", coachingCtrl.getInitialComponent());
		return (Activateable2)coachingCtrl;
	}
	
	private Activateable2 doSelectParticipant(UserRequest ureq, IdentityRef identity) {
		Identity id = securityManager.loadIdentityByKey(identity.getKey());
		return doSelectParticipant(ureq, id);
	}
	
	private Activateable2 doSelectParticipant(UserRequest ureq, Identity identity) {
		removeAsListenerAndDispose(coachingCtrl);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Identity", identity.getKey()), null);
		coachingCtrl = new GTAAssessedIdentityController(ureq, swControl, identity);
		listenTo(coachingCtrl);
		
		String fullName = userManager.getUserDisplayName(identity);
		assessedIdentityStackPanel.pushController(fullName, coachingCtrl);
		mainVC.put("selectionStack", assessedIdentityStackPanel);
		assessedIdentityStackPanel.getToolBar().setVisible(true);
		
		int index = participantListCtrl.indexOfIdentity(identity);
		int numOfRows = participantListCtrl.numOfIdentities();
		previousIdentityLink.setEnabled(index > 0);
		nextIdentityLink.setEnabled(index + 1 < numOfRows);

		return (Activateable2)coachingCtrl;
	}
	
	private void doLaunchStatistics(UserRequest ureq) {
		AssessmentToolOptions options = new AssessmentToolOptions();
		List<Identity> assessedIdentities = participantListCtrl.getAssessableIdentities();
		options.setIdentities(assessedIdentities);

		statsCtrl = new MSStatisticController(ureq, getWindowControl(), courseEnv, options,
				gtaNode, GTACourseNode.getEvaluationFormProvider());
		listenTo(statsCtrl);
		
		assessedIdentityStackPanel.pushController(translate("tool.stats"), statsCtrl);
		mainVC.put("selectionStack", assessedIdentityStackPanel);
		assessedIdentityStackPanel.getToolBar().setVisible(true);
	}
	
	private class GTAAssessedBusinessGroupController extends BasicController implements Activateable2 {
		
		private final Link bLink;
		private final GTACoachController groupTaskCtrl;
		
		public GTAAssessedBusinessGroupController(UserRequest ureq, WindowControl wControl, BusinessGroup assessedBusinessGroup) {
			super(ureq, wControl);
			
			VelocityContainer wrapperVC = createVelocityContainer("coach_wrapper_businessgroup");

			groupTaskCtrl = new GTACoachController(ureq, wControl, assessedIdentityStackPanel,
					courseEnv, gtaNode, coachCourseEnv, assessedBusinessGroup, true, true, false, false);
			listenTo(groupTaskCtrl);
			wrapperVC.put("selection", groupTaskCtrl.getInitialComponent());
			
			bLink = LinkFactory.createLinkBack(wrapperVC, this);
			wrapperVC.put("backLink", bLink);
			
			putInitialPanel(wrapperVC);
		}

		@Override
		public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
			groupTaskCtrl.activate(ureq, entries, state);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(bLink == source) {
				fireEvent(ureq, Event.BACK_EVENT);
			}
		}
	}
	
	/**
	 * Little wrapper to pack user informations, subscription and coach view
	 * of the task process.
	 * 
	 * Initial date: 16 sept. 2022<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private class GTAAssessedIdentityController extends BasicController implements Activateable2 {
		
		private final GTACoachController userTaskCtrl;
		private final Identity assessedIdentity;
		
		public GTAAssessedIdentityController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity) {
			super(ureq, wControl);
			this.assessedIdentity = assessedIdentity;
			
			VelocityContainer wrapperVC = createVelocityContainer("coach_wrapper");
			
			AssessedIdentityLargeInfosController userInfosCtrl = new AssessedIdentityLargeInfosController(ureq, getWindowControl(),
					assessedIdentity, coachCourseEnv.getCourseEnvironment(), gtaNode);
			listenTo(userInfosCtrl);
			wrapperVC.put("userInfos", userInfosCtrl.getInitialComponent());
			

			wrapperVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
			
			userTaskCtrl = new GTACoachController(ureq, wControl, assessedIdentityStackPanel,
					courseEnv, gtaNode, coachCourseEnv, assessedIdentity, false, true, false, false);
			listenTo(userTaskCtrl);
			wrapperVC.put("selection", userTaskCtrl.getInitialComponent());
			
			putInitialPanel(wrapperVC);
		}
		
		public Identity getAssessedIdentity() {
			return assessedIdentity;
		}

		@Override
		public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
			userTaskCtrl.activate(ureq, entries, state);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}
