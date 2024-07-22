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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 23 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13SettingsPage {
	
	private final WebDriver browser;
	
	public LTI13SettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LTI13SettingsPage enableLTI() {
		By enableBy = By.cssSelector("fieldset.o_sel_lti13_admin_settings .o_sel_lti13_admin_enable input[name='lti13.module.enabled']");
		OOGraphene.waitElement(enableBy, browser);
		OOGraphene.check(browser.findElement(enableBy), Boolean.TRUE);
		By platformIssBy = By.cssSelector("fieldset.o_sel_lti13_admin_settings .o_sel_lti13_admin_platform_iss");
		OOGraphene.waitElement(platformIssBy, browser);
		return this;
	}
	
	public LTI13SettingsPage saveConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_sel_lti13_admin_buttons button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}

}
