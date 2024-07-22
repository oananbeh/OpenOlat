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
package org.olat.course.style.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ConfigurationChangedListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.spacesaver.ExpandController;
import org.olat.core.gui.control.generic.spacesaver.ExpandableController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.Header;
import org.olat.course.style.Header.Builder;
import org.olat.course.style.TeaserImageStyle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HeaderContentController extends BasicController
		implements Activateable2, TooledController, ConfigurationChangedListener {
	
	private final VelocityContainer mainVC;
	private final ExpandController collapseCtrl;
	private ExpandableController headerCtrl;
	private final Controller contentCtrl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final ICourse course;
	private final CourseNode courseNode;
	private final String iconCssClass;
	
	@Autowired
	private CourseStyleService courseStyleService;
	@Autowired
	private LearningPathService learningPathService;

	public HeaderContentController(UserRequest ureq, WindowControl wControl, Controller contentCtrl,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode, String iconCssClass) {
		super(ureq, wControl);
		this.contentCtrl = contentCtrl;
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		this.iconCssClass = iconCssClass;
		course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		
		mainVC = createVelocityContainer("header_content");
		
		collapseCtrl = new ExpandController(ureq, wControl, courseNode.getIdent());
		listenTo(collapseCtrl);
		reloadHeader(ureq);
		
		listenTo(contentCtrl);
		mainVC.put("content", contentCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	public Controller getContentController() {
		return contentCtrl;
	}

	@Override
	public void configurationChanged() {
		if(contentCtrl instanceof ConfigurationChangedListener) {
			((ConfigurationChangedListener)contentCtrl).configurationChanged();
		}
	}

	@Override
	public void initTools() {
		if(contentCtrl instanceof TooledController) {
			((TooledController)contentCtrl).initTools();
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(contentCtrl instanceof Activateable2) {
			((Activateable2)contentCtrl).activate(ureq, entries, state);
		}
	}

	public void reloadHeader(UserRequest ureq) {
		removeAsListenerAndDispose(headerCtrl);
		headerCtrl = null;
		
		Header header = createHeader(userCourseEnv, courseNode, iconCssClass);
		if (header != null && CourseStyleUIFactory.hasValues(header)) {
			headerCtrl = new HeaderController(ureq, getWindowControl(), header);
			listenTo(headerCtrl);
			collapseCtrl.setExpandableController(headerCtrl);
			mainVC.put("header", collapseCtrl.getInitialComponent());
		} else {
			mainVC.remove("header");
		}
	}
	
	public Header createHeader(UserCourseEnvironment userCourseEnv, CourseNode courseNode, String iconCssClass) {
		String displayOption = courseNode.getDisplayOption();
		if (CourseNode.DISPLAY_OPTS_CONTENT.equals(displayOption)) {
			return null;
		}
		
		Builder builder = Header.builder();
		boolean coach = userCourseEnv.isAdmin() || userCourseEnv.isCoach();
		CourseStyleUIFactory.addMetadata(builder, courseNode, displayOption, coach);
		
		CourseConfig courseConfig = userCourseEnv.getCourseEnvironment().getCourseConfig();
		VFSMediaMapper teaserImageMapper = courseStyleService.getTeaserImageMapper(course, courseNode);
		if (teaserImageMapper != null) {
			boolean teaserImageTransparent = courseStyleService.isImageTransparent(teaserImageMapper);
			TeaserImageStyle teaserImageStyle = courseStyleService.getTeaserImageStyle(course, courseNode);
			builder.withTeaserImage(teaserImageMapper, teaserImageTransparent, teaserImageStyle);
		}
		
		ColorCategoryResolver colorCategoryResolver = courseStyleService.getColorCategoryResolver(null, courseConfig.getColorCategoryIdentifier());
		builder.withColorCategoryCss(colorCategoryResolver.getColorCategoryCss(courseNode));
		builder.withIconCss(iconCssClass);
		
		if (LearningPathNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType())) {
			if (userCourseEnv.isParticipant()) {
				AssessmentEvaluation evaluation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
				if (evaluation != null) {
					CourseStyleUIFactory.addHandlingRangeData(builder, evaluation);
				}
			} else {
				LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode);
				builder.withDuration(learningPathConfigs.getDuration());
				builder.withStartDateConfig(learningPathConfigs.getStartDateConfig());
				builder.withEndDateConfig(learningPathConfigs.getEndDateConfig());
			}
		}
		
		return builder.build();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
	}

}
