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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.spacesaver.ExpandableController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.learningpath.ui.LearningPathListController;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.Header;
import org.olat.course.style.TeaserImageStyle;

/**
 * 
 * Initial date: 24 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HeaderController extends BasicController implements ExpandableController {

	private final VelocityContainer mainVC;
	private final Header header;

	public HeaderController(UserRequest ureq, WindowControl wControl, Header header) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LearningPathListController.class, getLocale(), getTranslator()));
		this.header = header;
		mainVC = isStyled()? createVelocityContainer("header"): createVelocityContainer("header_plain");
		
		mainVC.contextPut("item", header);
		mainVC.contextPut("handlingRange", getHandlingRange());
		
		if (header.getTeaserImageMapper() != null) {
			String teaserImageUrl = registerCacheableMapper(ureq, "teaserimage-" + CodeHelper.getRAMUniqueID(), header.getTeaserImageMapper());
			mainVC.contextPut("teaserImageUrl", teaserImageUrl);
			mainVC.contextPut("teaserImageBgCss", getTeaserImageBgCss());
			mainVC.contextPut("cover", TeaserImageStyle.cover == header.getTeaserImageStyle());
		}
		
		putInitialPanel(mainVC);
	}
	
	private boolean isStyled() {
		return isExpandable()
				|| DueDateConfig.isDueDate(header.getStartDateConfig())
				|| DueDateConfig.isDueDate(header.getEndDateConfig())
				|| header.getDuration() != null
				|| header.getTeaserImageMapper() != null
				|| !ColorCategory.CSS_NO_COLOR.equals(header.getColorCategoryCss());
		
	}

	private String getHandlingRange() {
		return CourseStyleUIFactory.formatHandlingRangeDate(getTranslator(), header.getStartDateConfig(),
				header.getEndDateConfig(), header.getDuration());
	}
	
	private String getTeaserImageBgCss() {
		if (header.isTeaserImageTransparent()) {
			if (StringHelper.containsNonWhitespace(header.getColorCategoryCss())) {
				return header.getColorCategoryCss();
			}
			return ColorCategory.CSS_NO_COLOR;
		}
		return null;
	}

	@Override
	public boolean isExpandable() {
		return StringHelper.containsNonWhitespace(header.getDescription())
				|| StringHelper.containsNonWhitespace(header.getObjectives())
				|| StringHelper.containsNonWhitespace(header.getInstruction())
				|| StringHelper.containsNonWhitespace(header.getInstructionalDesign());
	}
	
	@Override
	public void setExpanded(boolean expanded) {
		mainVC.contextPut("expanded", Boolean.valueOf(expanded));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
