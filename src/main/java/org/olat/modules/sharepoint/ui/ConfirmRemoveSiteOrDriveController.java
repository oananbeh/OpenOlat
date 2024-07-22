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
package org.olat.modules.sharepoint.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.sharepoint.model.SiteAndDriveConfiguration;

/**
 * 
 * Initial date: 8 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRemoveSiteOrDriveController extends FormBasicController {
	
	private final SiteAndDriveConfiguration configuration;
	
	public ConfirmRemoveSiteOrDriveController(UserRequest ureq, WindowControl wControl, SiteAndDriveConfiguration configuration) {
		super(ureq, wControl, "confirm_remove_site");
		this.configuration = configuration;
		initForm(ureq);
	}
	
	public SiteAndDriveConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String msg;
			String siteDisplayName = configuration.getSiteDisplayName();
			if(!StringHelper.containsNonWhitespace(siteDisplayName) ) {
				siteDisplayName = configuration.getSiteName();
			}
			if(StringHelper.containsNonWhitespace(configuration.getDriveId())) {
				msg = translate("warning.remove.drive", configuration.getDriveName(), siteDisplayName);
			} else {
				msg = translate("warning.remove.site", siteDisplayName);
			}
			layoutCont.contextPut("msg", msg);
		}
		
		uifactory.addFormSubmitButton("remove", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
