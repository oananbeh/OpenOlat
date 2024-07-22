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
package org.olat.resource.accesscontrol.provider.paypal.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ui.AbstractAccessController;

/**
 * 
 * Initial date: 7 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PaypalAccessController extends AbstractAccessController {

	public PaypalAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl, link);
		init(ureq);
	}

	@Override
	protected String getTitle() {
		return getTranslator().translate("access.paypal.title");
	}

	@Override
	protected String getMethodDescription() {
		return getTranslator().translate("access.paypal.desc");
	}

	@Override
	protected Controller createDetailsController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		return new PaypalSubmitController(ureq, wControl, link);
	}

}