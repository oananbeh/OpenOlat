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
package org.olat.course.nodes.video;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.functions.VideoCommands;
import org.olat.core.logging.Tracing;
import org.olat.course.assessment.AssessmentEvents;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.modules.video.ui.VideoHelper;
import org.olat.modules.video.ui.editor.CommentLayerController;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.modules.video.ui.segment.SegmentsController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author dfakae, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */

public class VideoRunController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(VideoRunController.class);

	private Panel main;
	
	private VideoDisplayController videoDispCtr;

	private VideoCourseNode videoNode;
	private ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;
	private double currentProgress = 0d;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	private CommentLayerController commentLayerController;

	public VideoRunController(ModuleConfiguration config, WindowControl wControl, UserRequest ureq,
			UserCourseEnvironment userCourseEnv, VideoCourseNode videoNode) {
		super(ureq, wControl);
		
		this.config = config;
		this.videoNode = videoNode;
		this.userCourseEnv = userCourseEnv;
		addLoggingResourceable(LoggingResourceable.wrap(videoNode));
		
		main = new Panel("videorunmain");
		doLaunch(ureq);
		putInitialPanel(main);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// Update last position if user leaves controller somehow uncontrolled
		if (this.currentProgress > 0d && this.currentProgress < 1d) {
			doUpdateAssessmentStatus(ureq, this.currentProgress, true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (videoDispCtr == source) {
			if (event instanceof VideoEvent videoEvent) {
				if (videoEvent.getCommand().equals(VideoEvent.ENDED)) {
					doUpdateAssessmentStatus(ureq, 1d, true);
				} else if (videoEvent.getCommand().equals(VideoEvent.PAUSE)) {
					doUpdateAssessmentStatus(ureq, videoEvent.getProgress(), true);
				} else if (videoEvent.getCommand().equals(VideoEvent.PROGRESS)) {
					doUpdateAssessmentStatus(ureq, videoEvent.getProgress(), false);
				} else if (videoEvent.getCommand().equals(VideoEvent.PLAY)) {
					if (commentLayerController != null) {
						commentLayerController.hideComment();
						videoDispCtr.showOtherLayers(commentLayerController);
						videoDispCtr.showHideProgressTooltip(true);
					}
				}
			} else if (event instanceof VideoDisplayController.MarkerReachedEvent markerReachedEvent) {
				if (commentLayerController != null) {
					commentLayerController.setComment(ureq, markerReachedEvent.getMarkerId());
					if (commentLayerController.isCommentVisible()) {
						videoDispCtr.hideOtherLayers(commentLayerController);
						videoDispCtr.showHideProgressTooltip(false);
						doPause(markerReachedEvent.getTimeInSeconds());
					}
				}
			}
		} else if (commentLayerController == source) {
			if (event == Event.DONE_EVENT) {
				commentLayerController.hideComment();
				videoDispCtr.showOtherLayers(commentLayerController);
				videoDispCtr.showHideProgressTooltip(true);
				doContinue();
			}
		}
	}

	private void doContinue() {
		getWindowControl().getWindowBackOffice().sendCommandTo(VideoCommands
				.videoContinue(videoDispCtr.getVideoElementId()));
	}

	private void doPause(long timeInSeconds) {
		getWindowControl().getWindowBackOffice().sendCommandTo(VideoCommands
				.pause(videoDispCtr.getVideoElementId(), timeInSeconds));
	}

	private void doUpdateAssessmentStatus(UserRequest ureq, double progress, boolean forceSave) {
		if (!userCourseEnv.isCourseReadOnly() && userCourseEnv.isParticipant()) {
			log.debug("Update assessment entry: ident={}, progress={}, forceSave={}",
					userCourseEnv.getIdentityEnvironment().getIdentity().getKey(), progress, forceSave);
			boolean update = forceSave;
			// Update video progress as assessment completion if not already in status DONE
			AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(videoNode,
					userCourseEnv);
			AssessmentEntryStatus assessmentEntryStatus = assessmentEvaluation.getAssessmentStatus();
			if (!AssessmentEntryStatus.done.equals(assessmentEntryStatus)) {
				// Update watch progress
				Double newProgress = assessmentEvaluation.getCompletion();
				if (newProgress == null || newProgress.floatValue() < progress) {
					newProgress = Double.valueOf(progress);
					// Save only in 10% steps to reduce save and assessment recalculation cycles
					if (!forceSave && (Math.round(currentProgress * 10) + 1 <= Math.round(progress * 10))) {
						update = true;
					}
					currentProgress = newProgress;
				}
				
				// 95% is considered as "fully watched", set as done
				if (newProgress.floatValue() >= 0.95d) {
					assessmentEntryStatus = AssessmentEntryStatus.done;
					newProgress = 1d;
					update = true;
				} else {
					assessmentEntryStatus = AssessmentEntryStatus.inProgress;
				}
				
				if (update) {
					courseAssessmentService.updateCompletion(videoNode, userCourseEnv, newProgress,
							assessmentEntryStatus, Role.user);
					log.debug("Updateed assessment entry (old): ident={}, progress={}, status={}",
							userCourseEnv.getIdentityEnvironment().getIdentity().getKey(), assessmentEvaluation.getCompletion(), assessmentEvaluation.getAssessmentStatus());
					log.debug("Updateed assessment entry (new): ident={}, progress={}, status={}",
							userCourseEnv.getIdentityEnvironment().getIdentity().getKey(), newProgress, assessmentEntryStatus);
					fireEvent(ureq, AssessmentEvents.CHANGED_EVENT);
				}
			}
		}
	}
	
	private void doLaunch(UserRequest ureq) {
		VelocityContainer myContent = createVelocityContainer("run");
		RepositoryEntry videoEntry = VideoEditController.getVideoReference(config, false);
		
		// show empty screen when video is not available or in deleted state
		if (videoEntry == null) {
			EmptyStateConfig emptyState = EmptyStateConfig.builder()
					.withIconCss("o_icon_video")
					.withMessageI18nKey(VideoEditController.NLS_ERROR_VIDEOREPOENTRYMISSING)
					.build();			
			myContent = createVelocityContainer("novideo");
			EmptyStateFactory.create("emptyStateCmp", myContent, this, emptyState);
			main.setContent(myContent);			
			return;
		} else if (RepositoryEntryStatusEnum.deleted == videoEntry.getEntryStatus()
				|| RepositoryEntryStatusEnum.trash == videoEntry.getEntryStatus()) {			
			EmptyStateConfig emptyState = EmptyStateConfig.builder()
					.withIconCss("o_icon_video")
					.withIndicatorIconCss("o_icon_deleted")
					.withMessageI18nKey(VideoEditController.NLS_ERROR_VIDEOREPOENTRYDELETED)
					.build();			
			myContent = createVelocityContainer("novideo");
			EmptyStateFactory.create("emptyStateCmp", myContent, this, emptyState);
			main.setContent(myContent);			
			return;
		}
		
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		VideoDisplayOptions displayOptions = videoNode.getVideoDisplay(videoEntry, userCourseEnv.isCourseReadOnly());
		
		// Read current status
		AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(videoNode, userCourseEnv);
		double completion = (assessmentEvaluation.getCompletion() == null ? 0d : assessmentEvaluation.getCompletion());
		// Override forwardSeeking configuration
		if (this.userCourseEnv.isParticipant()) {
			if (this.userCourseEnv.isCourseReadOnly() || completion == 1d) {
				displayOptions.setForwardSeekingRestricted(false);
			} // else use as configured in course element
		} else {
			// don't restrict it for owner, coaches etc.
			displayOptions.setForwardSeekingRestricted(false);
		}
		videoDispCtr = new VideoDisplayController(ureq, getWindowControl(), videoEntry, courseEntry, videoNode, displayOptions);
		// Enable progress tracking for participants in learning path courses
		if (!this.userCourseEnv.isCourseReadOnly() && this.userCourseEnv.isParticipant()) {
			videoDispCtr.setProgressListener(true);
			// Init with last position
			if (completion > 0.05d && completion < 0.95d) {
				videoDispCtr.setPlayProgress(completion);
			}
		}
		
		if(displayOptions.isShowSegments()) {
			long totalDurationInMillis = VideoHelper.durationInSeconds(videoEntry, videoDispCtr) * 1000l;
			SegmentsController segmentsCtrl = new SegmentsController(ureq, getWindowControl(),
					videoEntry, videoDispCtr.getVideoElementId(), totalDurationInMillis);
			listenTo(segmentsCtrl);
			videoDispCtr.addLayer(segmentsCtrl);
		}
		listenTo(videoDispCtr);

		if (displayOptions.isShowOverlayComments()) {
			commentLayerController = new CommentLayerController(ureq, getWindowControl(), videoEntry,
					videoDispCtr.getVideoElementId());
			commentLayerController.loadComments();
			listenTo(commentLayerController);
			videoDispCtr.addLayer(commentLayerController);
			videoDispCtr.addMarkers(commentLayerController.getCommentsAsMarkers());
		}

		myContent.put("videoDisplay", videoDispCtr.getInitialComponent());
		main.setContent(myContent);
		
		// Update launch counter
		repositoryService.incrementLaunchCounter(videoEntry);
	}

	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq) {
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), this, userCourseEnv, videoNode, "o_icon_video");
		return new NodeRunConstructionResult(ctrl);
	}
	
	public record RuntimeSegment(VideoSegmentCategory category, String width, String left,
			double start, double duration, String durationString) {
		
		public static RuntimeSegment valueOf(VideoSegmentCategory category, VideoSegment segment,
				long totalDurationInMillis, String durationString) {
			double dleft = (double) segment.getBegin().getTime() / totalDurationInMillis;
			double dwidth = (segment.getDuration() * 1000.0d) / totalDurationInMillis;
			double startInSeconds = segment.getBegin().getTime() / 1000.0d;
			return new RuntimeSegment(category,
					VideoSegment.formatAsPercentage(dwidth), VideoSegment.formatAsPercentage(dleft),
					startInSeconds, segment.getDuration(), durationString);
		}

		public String getCategoryLabel() {
			return category.getLabel();
		}
		
		public String getCategoryLabelAndTitle() {
			return category.getLabelAndTitle();
		}
		
		public String getCategoryColor() {
			return category.getStyle();
		}
	}
}
