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
 * Drive the settings in administration.
 * 
 * Initial date: 23 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ZoomSettingsPage {
	
	private final WebDriver browser;
	
	public ZoomSettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ZoomSettingsPage enableZoom() {
		By enableBy = By.cssSelector("fieldset.o_sel_zoom_admin_configuration .o_sel_zoom_admin_enable input[name='zoom.module.enabled']");
		OOGraphene.waitElement(enableBy, browser);
		OOGraphene.check(browser.findElement(enableBy), Boolean.TRUE);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_zoom_admin_configuration .o_sel_zoom_admin_enabled_for"), browser);
		return this;
	}
	
	public ZoomSettingsPage addProfile(String profile, String ltiKey) {
		By addProfileBy = By.cssSelector("fieldset.o_sel_zoom_admin_configuration a.o_sel_zoom_admin_add_profile");
		OOGraphene.waitElement(addProfileBy, browser);
		OOGraphene.click(addProfileBy, browser);
		
		// Modal
		OOGraphene.waitModalDialog(browser);
		
		By nameBy = By.cssSelector("fieldset.o_sel_zoom_edit_profile .o_sel_zoom_profile_name input[type='text']");
		OOGraphene.waitElement(nameBy, browser);
		browser.findElement(nameBy).sendKeys(profile);
		By ltiKeyBy = By.cssSelector("fieldset.o_sel_zoom_edit_profile .o_sel_zoom_profile_ltikey input[type='text']");
		OOGraphene.waitElement(ltiKeyBy, browser);
		browser.findElement(ltiKeyBy).sendKeys(ltiKey);
		
		By saveBy = By.cssSelector("fieldset.o_sel_zoom_edit_profile button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		
		OOGraphene.waitModalDialogDisappears(browser);
		
		By profileBy = By.xpath("//table//td[text()[contains(.,'" + profile + "')]]");
		OOGraphene.waitElement(profileBy, browser);
		
		return this;
	}
	
	public ZoomSettingsPage saveConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_sel_zoom_admin_configuration button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}

}
