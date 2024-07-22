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
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCEConfigurationPage {

	private final WebDriver browser;
	
	public AssessmentCEConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AssessmentCEConfigurationPage selectConfiguration() {
		By configBy = By.className("o_sel_course_ms_score");
		OOGraphene.selectTab("o_node_config", configBy, browser);
		return this;
	}
	
	public AssessmentCEConfigurationPage selectConfigurationWithRubric() {
		By configBy = By.className("o_sel_course_ms");
		OOGraphene.selectTab("o_node_config", configBy, browser);
		return this;
	}
	
	/**
	 * Set the scoring with passed/failed, automatically with a cut value.
	 * 
	 * @param minVal The min. score value
	 * @param maxVal The max. score value
	 * @param cutVal The cut vallue for passed
	 * @return
	 */
	public AssessmentCEConfigurationPage setScoreAuto(float minVal, float maxVal, float cutVal) {
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_course_ms_form"), browser);
		OOGraphene.toggle("fieldset.o_sel_course_ms_form div.o_sel_course_ms_score button.o_sel_course_ms_score", true, false, browser);
		By minValBy = By.cssSelector(".o_sel_course_ms_min_val input[type='text']");
		OOGraphene.waitElement(minValBy, browser);
		
		WebElement minValEl = browser.findElement(minValBy);
		minValEl.clear();
		minValEl.sendKeys(Float.toString(minVal));
		
		By maxValBy = By.cssSelector(".o_sel_course_ms_max_val input[type='text']");
		WebElement maxValEl = browser.findElement(maxValBy);
		maxValEl.clear();
		maxValEl.sendKeys(Float.toString(maxVal));
		
		// Toggle passed/failed
		OOGraphene.toggle("div.o_sel_course_ms_display_passed button.o_sel_course_ms_display_passed", true, false, browser);
		
		By displayAutoBy = By.cssSelector("div.o_sel_course_ms_display_type input[type='radio'][name='form.passed.type'][value='true']");
		OOGraphene.waitElement(displayAutoBy, browser);
		browser.findElement(displayAutoBy).click();

		By cutValBy = By.cssSelector(".o_sel_course_ms_cut_val input[type='text']");
		OOGraphene.waitElement(cutValBy, browser);
		WebElement cutValEl = browser.findElement(cutValBy);
		cutValEl.clear();
		cutValEl.sendKeys(Float.toString(cutVal));
		return this;
	}
	
	public AssessmentCEConfigurationPage saveAssessmentOptions() {
		By saveBy = By.cssSelector(".o_sel_course_ms_form button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Give a score, set the min. and max. value and passed/failed
	 * is automatically calculated with the cut value.
	 * 
	 * @param minVal
	 * @param maxVal
	 * @param cutVal
	 * @return
	 */
	public AssessmentCEConfigurationPage setScore(float minVal, float maxVal, float cutVal) {
		By scoreBy = By.cssSelector("fieldset.o_sel_course_ms");
		OOGraphene.waitElement(scoreBy, browser);
		OOGraphene.toggle("fieldset.o_sel_course_ms div.o_sel_course_ms_score button.o_sel_course_ms_score", true, false, browser);
		
		By minValBy = By.cssSelector(".o_sel_course_ms_min input[type='text']");
		OOGraphene.waitElement(minValBy, browser);
		WebElement minValEl = browser.findElement(minValBy);
		minValEl.clear();
		minValEl.sendKeys(Float.toString(minVal));
		
		By maxValBy = By.cssSelector(".o_sel_course_ms_max input[type='text']");
		WebElement maxValEl = browser.findElement(maxValBy);
		maxValEl.clear();
		maxValEl.sendKeys(Float.toString(maxVal));
		
		By displayAutoBy = By.cssSelector("#o_coform_passed_type input[type='radio'][value='true']");
		browser.findElement(displayAutoBy).click();
		OOGraphene.waitBusy(browser);

		By cutValBy = By.cssSelector(".o_sel_course_ms_cut input[type='text']");
		WebElement cutValEl = browser.findElement(cutValBy);
		cutValEl.clear();
		cutValEl.sendKeys(Float.toString(cutVal));
		return this;
	}
	
	public AssessmentCEConfigurationPage enableGrade(boolean auto) {
		OOGraphene.waitElement(By.cssSelector("div.o_sel_course_ms_grade"), browser);
		OOGraphene.toggle("div.o_sel_course_ms_grade button.o_sel_course_ms_grade", true, auto, browser);
		
		By editBy = By.cssSelector("a.o_sel_grade_edit_scale");
		OOGraphene.waitElement(editBy, browser);
		
		By modeBy = By.xpath("//div[contains(@class,'o_sel_course_ms_grade_mode')]//input[@name='node.grade.auto'][@value='" + auto + "']");
		browser.findElement(modeBy).click();
		return this;
	}
	
	public AssessmentCEConfigurationPage save() {
		By saveBy = By.cssSelector(".o_sel_course_ms button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	public GradeConfigurationPage editGradingScale() {
		By editScaleBy = By.cssSelector("fieldset.o_sel_course_ms a.o_sel_grade_edit_scale");
		OOGraphene.click(editScaleBy, browser);
		OOGraphene.waitModalDialog(browser);
		return new GradeConfigurationPage(browser)
				.assertOnConfiguration();
	}
}
