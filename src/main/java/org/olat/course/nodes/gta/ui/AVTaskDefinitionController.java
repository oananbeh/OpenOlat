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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.nodes.gta.model.TaskDefinition;

import java.util.List;

/**
 * Initial date: 2022-09-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVTaskDefinitionController extends FormBasicController {

	private TextElement titleEl;
	private TextElement descriptionEl;
	private TextElement fileNameEl;

	private final TaskDefinition task;
	private final VFSContainer tasksContainer;
	private final List<TaskDefinition> existingDefinitions;

	public AVTaskDefinitionController(UserRequest ureq, WindowControl wControl, TaskDefinition task,
									  VFSContainer tasksContainer, List<TaskDefinition> existingDefinitions) {
		super(ureq, wControl);
		this.task = task;
		this.tasksContainer = tasksContainer;
		this.existingDefinitions = existingDefinitions;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_upload_task_form");

		String title = task.getTitle() == null ? "" : task.getTitle();
		titleEl = uifactory.addTextElement("title", "task.title", 128, title, formLayout);
		titleEl.setMandatory(true);

		String description = task.getDescription() == null ? "" : task.getDescription();
		descriptionEl = uifactory.addTextAreaElement("descr", "task.description", 2048, 6, -1, true, false, description, formLayout);

		String fileName = task.getFilename() == null ? "" : task.getFilename();
		fileNameEl = uifactory.addTextElement("fileName", "task.file", 128, fileName, formLayout);
		fileNameEl.setMandatory(true);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		titleEl.clearError();
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}

		fileNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(fileNameEl.getValue())) {
			fileNameEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if (!FileUtils.validateFilename(fileNameEl.getValue())) {
			fileNameEl.setErrorKey("error.file.invalid");
			allOk &= false;
		} else {
			VFSItem item = tasksContainer.resolve(fileNameEl.getValue());
			if (item != null && item.exists()) {
				fileNameEl.setErrorKey("error.file.exists", fileNameEl.getValue());
				allOk &= false;
			} else if (existingDefinitions != null) {
				for (TaskDefinition currentDefinition : existingDefinitions) {
					if (fileNameEl.getValue().equals(currentDefinition.getFilename())) {
						fileNameEl.setErrorKey("error.file.exists", fileNameEl.getValue());
						allOk &= false;
					}
				}
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		task.setTitle(titleEl.getValue());
		task.setDescription(descriptionEl.getValue());
		task.setFilename(fileNameEl.getValue());

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
