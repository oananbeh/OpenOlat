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

import java.io.File;
import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Drive the course element view for coaches.
 * 
 * Initial date: 26.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupTaskToCoachPage {
	
	private WebDriver browser;
	
	public GroupTaskToCoachPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public GroupTaskToCoachPage assertOnVisitGreen() {
		By visitedBy = By.cssSelector(".panel.o_assessment_stats .o_launch_list .o_icon.o_green_led");
		OOGraphene.waitElement(visitedBy, browser);
		return this;
	}
	
	public GroupTaskToCoachPage selectBusinessGroupToCoach(String name) {
		By selectBy = By.xpath("//table[contains(@class,'table')]//tr/td[contains(@class,'o_dnd_label')]/a[text()[contains(.,'" + name + "')]][contains(@onclick,'select')]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		By processBy = By.cssSelector("div.o_process");
		OOGraphene.waitElement(processBy, browser);
		return this;
	}
	
	/**
	 * Select the segment with the list of assessed groups.
	 * 
	 * @return Itself
	 */
	public GroupTaskToCoachPage selectGroupsToCoach() {
		By identitiesListSegmentBy = By.cssSelector("div.o_segments a.btn.o_sel_course_gta_coaching");
		OOGraphene.waitElement(identitiesListSegmentBy, browser);
		browser.findElement(identitiesListSegmentBy).click();
		By groupsListBy = By.cssSelector("div.o_table_flexi.o_sel_course_gta_coached_groups");
		OOGraphene.waitElement(groupsListBy, browser);
		return this;
	}
	
	/**
	 * Select the segment with the list of assessed identities.
	 * 
	 * @return Itself
	 */
	public GroupTaskToCoachPage selectIdentitiesToCoach() {
		By identitiesListSegmentBy = By.cssSelector("div.o_segments a.btn.o_sel_course_gta_coaching");
		OOGraphene.waitElement(identitiesListSegmentBy, browser);
		browser.findElement(identitiesListSegmentBy).click();
		By identitiesListBy = By.cssSelector("div.o_table_flexi.o_sel_course_gta_coached_participants");
		OOGraphene.waitElement(identitiesListBy, browser);
		return this;
	}
	
	public GroupTaskToCoachPage selectIdentityToCoach(UserVO user) {
		By selectLinkBy = By.xpath("//table[contains(@class,'table')]//td//a[contains(@onclick,'firstName')][contains(text(),'" + user.getFirstName() + "')]");
		browser.findElement(selectLinkBy).click();
		By processBy = By.cssSelector("div.o_process");
		OOGraphene.waitElement(processBy, browser);
		return this;
	}
	
	public GroupTaskToCoachPage assertSubmittedDocument(String title) {
		By selectLinkBy = By.xpath("//div[@id='o_step_submit_content']//ul//a//span[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(selectLinkBy, browser);
		return this;
	}
	
	public GroupTaskToCoachPage assertRevision(String title) {
		By selectLinkBy = By.xpath("//div[@id='o_step_revision_content']//ul//a//span[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(selectLinkBy, browser);
		return this;
	}
	
	public GroupTaskToCoachPage reviewed() {
		By reviewBy = By.cssSelector("#o_step_review_content .o_sel_course_gta_reviewed");
		OOGraphene.waitElement(reviewBy, browser);
		OOGraphene.scrollBottom(By.id("o_step_review_content"), browser);
		OOGraphene.click(reviewBy, browser);
		confirm();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public GroupTaskToCoachPage needRevision() {
		By reviewBy = By.cssSelector("#o_step_review_content .o_sel_course_gta_need_revision");
		OOGraphene.click(reviewBy, browser);
		
		OOGraphene.waitModalDialog(browser);
		By okBy = By.cssSelector("div.modal-dialog div.buttons button.btn.btn-primary");
		browser.findElement(okBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskToCoachPage closeRevisions() {
		By closeRevisionBy = By.cssSelector("#o_step_revision_content .o_sel_course_gta_close_revision");
		OOGraphene.click(closeRevisionBy, browser);
		return confirm();
	}
	
	public GroupTaskToCoachPage confirm() {
		OOGraphene.waitModalDialog(browser);
		By yes = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		browser.findElement(yes).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public GroupTaskToCoachPage openRevisionsStep() {
		By uploadButtonBs = By.cssSelector("#o_step_review_content .o_sel_course_gta_submit_file");
		List<WebElement> buttons = browser.findElements(uploadButtonBs);
		if(buttons.isEmpty() || !buttons.get(0).isDisplayed()) {
			//open grading tab
			By collpaseBy = By.xpath("//div[contains(@class,'o_step_review')]//button[contains(@class,'o_button_details')]");
			OOGraphene.click(collpaseBy, browser);
			OOGraphene.waitElement(uploadButtonBs, browser);
		}
		return this;
	}
	
	public GroupTaskToCoachPage uploadCorrection(File correctionFile) {
		By uploadButtonBy = By.cssSelector("#o_step_review_content .o_sel_course_gta_submit_file");
		OOGraphene.click(uploadButtonBy, browser);
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, correctionFile, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector(".o_sel_course_gta_upload_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		By saveButtonBy = By.cssSelector(".o_sel_course_gta_upload_form button.btn-primary");
		OOGraphene.click(saveButtonBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		By correctionUploaded = By.xpath("//table[contains(@class,'table')]//tr/td//a[span/text()[contains(.,'" + correctionFile.getName() + "')]]");
		OOGraphene.waitElement(correctionUploaded, browser);
		return this;
	}
	
	public GroupTaskToCoachPage openIndividualAssessment() {
		By assessmentPanelBy = By.cssSelector("div.o_step_grading .o_assessment_panel");
		List<WebElement> assessmentPanels = browser.findElements(assessmentPanelBy);
		if(assessmentPanels.isEmpty() || !assessmentPanels.get(0).isDisplayed()) {
			//open grading tab
			By collpaseBy = By.xpath("//div[contains(@class,'o_step_grading')]//button[contains(@class,'o_button_details')]");
			OOGraphene.click(collpaseBy, browser);
			OOGraphene.waitElement(assessmentPanelBy, browser);
		}
		return this;
	}
	
	public GroupTaskToCoachPage individualAssessment(Boolean passed, Float score) {
		if(passed != null) {
			By passedBy = By.cssSelector(".o_sel_assessment_form_passed input[type='radio'][value='true']");
			browser.findElement(passedBy).click();
		}
		
		if(score != null) {
			By scoreBy = By.xpath("//input[contains(@class,'o_sel_assessment_form_score')][@type='text']");
			browser.findElement(scoreBy).sendKeys(Float.toString(score));
		}
		
		By saveAndCloseBy = By.cssSelector(".o_sel_assessment_form button.btn.o_sel_assessment_form_save_and_done");
		OOGraphene.click(saveAndCloseBy, browser);
		
		By viewPanelBy = By.cssSelector(".o_step_grading .o_assessment_panel>table");
		OOGraphene.waitElement(viewPanelBy, browser);
		return this;
	}
	
	public GroupTaskToCoachPage openGroupAssessment() {
		By assessmentButtonBy = By.cssSelector("div.o_step_grading .o_sel_course_gta_assessment_button");
		List<WebElement> buttons = browser.findElements(assessmentButtonBy);
		if(buttons.isEmpty() || !buttons.get(0).isDisplayed()) {
			//open grading tab
			By collpaseBy = By.xpath("//div[contains(@class,'o_step_grading')]//button[contains(@class,'o_button_details')]");
			browser.findElement(collpaseBy).click();
			OOGraphene.waitElement(assessmentButtonBy, browser);
			browser.findElement(assessmentButtonBy).click();
		} else {
			buttons.get(0).click();
		}
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * Apply passed/score to all members of the group
	 * @param passed
	 * @param score
	 * @return
	 */
	public GroupTaskToCoachPage groupAssessment(Boolean passed, Float score) {
		OOGraphene.waitModalDialog(browser);
		By groupAssessmentPopupBy = By.cssSelector(".modal-body .o_sel_course_gta_group_assessment_form");
		OOGraphene.waitElement(groupAssessmentPopupBy, browser);
		
		By applyToAllCheckBy = By.xpath("//div[contains(@class,'o_sel_course_gta_group_assessment_form')]//div[contains(@class,'o_sel_course_gta_apply_to_all')]//input[@type='checkbox']");
		WebElement applyToAllCheckEl = browser.findElement(applyToAllCheckBy);
		OOGraphene.check(applyToAllCheckEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		
		if(passed != null) {
			By passedCheckBy = By.xpath("//div[contains(@class,'o_sel_course_gta_group_assessment_form')]//div[contains(@class,'o_sel_course_gta_group_passed')]//input[@type='checkbox']");
			WebElement passedCheckEl = browser.findElement(passedCheckBy);
			OOGraphene.check(passedCheckEl, Boolean.TRUE);
			OOGraphene.waitBusy(browser);
		}
		
		if(score != null) {
			By scoreBy = By.cssSelector(".o_sel_course_gta_group_assessment_form .o_sel_course_gta_group_score input[type='text']");
			WebElement scoreEl = browser.findElement(scoreBy);
			scoreEl.clear();
			scoreEl.sendKeys(score.toString());
			OOGraphene.waitBusy(browser);
		}
		
		By saveBy = By.cssSelector(".o_sel_course_gta_group_assessment_form a.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public GroupTaskToCoachPage assertPassed() {
		By passedBy = By.cssSelector("#o_step_grading_content div.o_assessment_preformance_summary div.o_widget_content div.o_state.o_passed");
		OOGraphene.waitElement(passedBy, browser);
		return this;
	}

}
