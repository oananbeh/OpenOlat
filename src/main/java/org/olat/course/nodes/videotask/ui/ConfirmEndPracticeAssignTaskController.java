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
package org.olat.course.nodes.videotask.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.videotask.ui.VideoTaskDisplayController.SegmentMarker;
import org.olat.course.nodes.videotask.ui.components.RestartEvent;
import org.olat.course.nodes.videotask.ui.components.ShowSolutionEvent;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoTaskSegmentSelection;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmEndPracticeAssignTaskController extends FormBasicController {
	
	private FormLink restartButton;
	private FormLink endTaskButton;
	private FormLink showSolutionButton;
	
	private final int maxAttempts;
	private final int currentAttempt;
	private final List<VideoSegment> segmentsList;
	private final List<SegmentMarker> results;
	
	public ConfirmEndPracticeAssignTaskController(UserRequest ureq, WindowControl wControl,
			List<SegmentMarker> results, List<VideoSegment> segmentsList,
			int currentAttempt, int maxAttempts) {
		super(ureq, wControl, "confirm_end_practice_assign");
		
		this.results = results;
		this.segmentsList = segmentsList;
		this.maxAttempts = maxAttempts;
		this.currentAttempt = currentAttempt;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(currentAttempt + 1 < maxAttempts || maxAttempts <= 0) {
			String restartText = translate("restart", Integer.toString(currentAttempt + 2));
			restartButton = uifactory.addFormLink("restart", restartText, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			restartButton.setIconLeftCSS("o_icon o_icon_reload");
		}
		if(currentAttempt + 1 >= maxAttempts || maxAttempts <= 0) {
			showSolutionButton = uifactory.addFormLink("show.solution", null, "show.solution", formLayout, Link.BUTTON);
		}
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			List<AttemptStats> stats = correctlyAssignedSegments(results);
			layoutCont.contextPut("resultMsgList", stats);
			
			int correct = numOfCorrectlyAssignedSegments(results);
			String resultMsg = translate("confirm.practice.assign.results", Integer.toString(segmentsList.size()), Integer.toString(correct));
			layoutCont.contextPut("resultMsg", resultMsg);

			List<VideoTaskSegmentSelection> resultSelections = results.stream()
					.map(SegmentMarker::segmentSelection)
					.toList();
			int unsuccessful = VideoTaskHelper.unsuccessfulSegments(segmentsList, resultSelections);
			layoutCont.contextPut("unsuccessful", Integer.toString(unsuccessful));
		}
		
		endTaskButton = uifactory.addFormLink("end.task", formLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(restartButton == source) {
			fireEvent(ureq, new RestartEvent());
		} else if(endTaskButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(showSolutionButton == source) {
			fireEvent(ureq, new ShowSolutionEvent());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Do nothing
	}
	
	private int numOfCorrectlyAssignedSegments(List<SegmentMarker> resultMarkers) {
		Set<String> segmentsIds = segmentsList.stream()
				.map(VideoSegment::getId)
				.collect(Collectors.toSet());
		
		for(SegmentMarker result:resultMarkers) {
			if(result.segmentSelection().isCorrect()) {
				segmentsIds.remove(result.segmentSelection().getSegmentId());
			}
		}
		return segmentsList.size() - segmentsIds.size();
	}
	
	private List<AttemptStats> correctlyAssignedSegments(List<SegmentMarker> resultMarkers) {
		Map<String,SegmentStats> segmentsIdsToAttempts = new HashMap<>();
		
		for(SegmentMarker result:resultMarkers) {
			VideoTaskSegmentSelection segmentSelection = result.segmentSelection();
			
			SegmentStats stats = segmentsIdsToAttempts
				.computeIfAbsent(segmentSelection.getSegmentId(), selection -> new SegmentStats());
			if(!stats.isCorrect()) {
				stats.incrementAttempts();
				
				boolean correct = segmentSelection.isCorrect();
				stats.setCorrect(correct);
			}
		}
		
		Map<Integer,AttemptStats> attemptsStatistics = new HashMap<>();
		for(SegmentStats segmentStats:segmentsIdsToAttempts.values()) {
			int attempts = segmentStats.getAttempts();
			if(attempts > 0 && segmentStats.isCorrect()) {
				AttemptStats stats = attemptsStatistics.computeIfAbsent(Integer.valueOf(attempts), AttemptStats::new);
				stats.incrementCorrectAnswers();
			}
		}
		
		List<AttemptStats> attemptsStatisticsList = new ArrayList<>(attemptsStatistics.values());
		Collections.sort(attemptsStatisticsList);
		for(AttemptStats stats:attemptsStatisticsList) {
			String resultMsg = translate("confirm.practice.assign.attempt.label", Integer.toString(stats.attempt));
			stats.setMessage(resultMsg);
		}
		
		return attemptsStatisticsList;
	}
	
	public static class SegmentStats {
		
		private int attempts = 0;
		private boolean correct = false;
		
		public int getAttempts() {
			return attempts;
		}
		
		public void incrementAttempts() {
			attempts++;
		}
		
		public boolean isCorrect() {
			return correct;
		}
		
		public void setCorrect(boolean correct) {
			this.correct = correct;
		}
	}
	
	public static class AttemptStats implements Comparable<AttemptStats> {
		
		private final int attempt;
		private int correctAnswers;
		private String message;
		
		public AttemptStats(Integer attempt) {
			this.attempt = attempt.intValue();
		}
		
		public int getCorrectAnswers() {
			return correctAnswers;
		}
		
		public void incrementCorrectAnswers() {
			correctAnswers++;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public int compareTo(AttemptStats o) {
			return Integer.compare(attempt, o.attempt);
		}
	}
}
