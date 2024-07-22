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
package org.olat.commons.calendar.ui;

import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.id.Identity;

/**
 * Needed for the personal configuratio.
 * 
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CalendarPersonalConfigurationRow {
	
	private final KalendarRenderWrapper wrapper;
	
	private FormToggle visibleToggle;
	private FormToggle aggregatedToggle;
	private FormLink feedLink;
	private FormLink toolsLink;
	private ColorPickerElement colorPickerElement;

	public CalendarPersonalConfigurationRow(KalendarRenderWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	public KalendarRenderWrapper getWrapper() {
		return wrapper;
	}

	public int getAccess() {
		return wrapper.getAccess();
	}
	
	public String getCalendarType() {
		return wrapper.getKalendar().getType();
	}
	
	public String getCalendarId() {
		return wrapper.getKalendar().getCalendarID();
	}

	public String getDisplayName() {
		return wrapper.getDisplayName();
	}
	
	public String getIdentifier() {
		return wrapper.getIdentifier();
	}

	public String getCssClass() {
		return wrapper.getCssClass();
	}

	public String getColor() {
		return CalendarColors.colorFromColorClass(wrapper.getCssClass());
	}
	
	public boolean isVisible() {
		return wrapper.isVisible();
	}
	
	public boolean isAggregated() {
		return wrapper.isInAggregatedFeed();
	}
	
	public boolean isImported() {
		return wrapper.isImported();
	}

	public String getToken() {
		return wrapper.getToken();
	}

	public String getFeedUrl(Identity identity) {
		return wrapper.getFeedUrl(identity);
	}

	public FormToggle getVisibleToggle() {
		return visibleToggle;
	}

	public void setVisibleToggle(FormToggle visibleToggle) {
		this.visibleToggle = visibleToggle;
	}

	public FormToggle getAggregatedToggle() {
		return aggregatedToggle;
	}

	public void setAggregatedToggle(FormToggle aggregatedToggle) {
		this.aggregatedToggle = aggregatedToggle;
	}

	public FormLink getFeedLink() {
		return feedLink;
	}

	public void setFeedLink(FormLink feedLink) {
		this.feedLink = feedLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public void setColorPickerElement(ColorPickerElement colorPickerElement) {
		this.colorPickerElement = colorPickerElement;
	}

	public ColorPickerElement getColorPickerElement() {
		return colorPickerElement;
	}
}
