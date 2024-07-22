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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 4 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormConfigurationPage {
	
	private WebDriver browser;
	
	public FormConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public FormConfigurationPage saveConfiguration() {
		By saveBy = By.cssSelector(".o_sel_form_config_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		By dirtySaveBy = By.xpath("//fieldset[contains(@class,'o_sel_form_config_form')]//button[contains(@class,'btn-primary') and not(contains(@class,'o_button_dirty'))]");
		OOGraphene.waitElement(dirtySaveBy, browser);
		return this;
	}
}
