/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.webdav.ui;

import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class WebDAVController extends BasicController {

	private Link closeButton;

	public WebDAVController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("webdav");
		putInitialPanel(mainVC);
		
		String webdavUrl = FolderManager.getWebDAVHttps();
		if (!StringHelper.containsNonWhitespace(webdavUrl)) {
			webdavUrl = FolderManager.getWebDAVHttp();
		}
		mainVC.contextPut("webdavUrl", webdavUrl);
		
		closeButton = LinkFactory.createButton("close", mainVC, this);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == closeButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

}
