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
package org.olat.modules.forms.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Arrays;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.SingleChoice.Presentation;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceInspectorController extends FormBasicController implements PageElementInspectorController {

	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;

	private TextElement nameEl;
	private SingleSelection presentationEl;
	private SingleSelection obligationEl;
	
	private final SingleChoice singleChoice;
	private final boolean restrictedEdit;

	@Autowired
	private ColorService colorService;

	public SingleChoiceInspectorController(UserRequest ureq, WindowControl wControl, SingleChoice singleChoice, boolean restrictedEdit) {
		super(ureq, wControl, "single_choice_inspector");
		this.singleChoice = singleChoice;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		return translate("inspector.formsinglechoice");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addGeneralTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);
	}

	private void addGeneralTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.general"), layoutCont);

		// name
		nameEl = uifactory.addTextElement("rubric.name", 128, singleChoice.getName(), layoutCont);
		nameEl.addActionListener(FormEvent.ONCHANGE);

		// presentation
		presentationEl = uifactory.addRadiosVertical("sc_pres", "single.choice.presentation", layoutCont,
				getPresentationKeys(), getPresentationValues());
		if (Arrays.asList(Presentation.values()).contains(singleChoice.getPresentation())) {
			presentationEl.select(singleChoice.getPresentation().name(), true);
		}
		presentationEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosVertical("obli_o", "obligation", layoutCont,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, singleChoice.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !singleChoice.isMandatory());
		obligationEl.setEnabled(!restrictedEdit);
		obligationEl.addActionListener(FormEvent.ONCLICK);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		Translator translator = Util.createPackageTranslator(PageElementTarget.class, getLocale());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, translator, uifactory, getLayoutSettings(), velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings() {
		if (singleChoice.getLayoutSettings() != null) {
			return singleChoice.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings() {
		if (singleChoice.getAlertBoxSettings() != null) {
			return singleChoice.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(!(fiSrc instanceof TextElement)) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (nameEl == source || presentationEl == source || obligationEl == source || source instanceof TextElement) {
			doSave(ureq);
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
	}

	private void doSave(UserRequest ureq) {
		doSaveSingleChoice();
		fireEvent(ureq, new ChangePartEvent(singleChoice));
	}

	private void doSaveSingleChoice() {
		singleChoice.setName(nameEl.getValue());
		
		Presentation presentation = null;
		if (presentationEl.isOneSelected()) {
			String selectedKey = presentationEl.getSelectedKey();
			presentation = Presentation.valueOf(selectedKey);
		}
		singleChoice.setPresentation(presentation);
		
		boolean mandatory = OBLIGATION_MANDATORY_KEY.equals(obligationEl.getSelectedKey());
		singleChoice.setMandatory(mandatory);
	}

	private void doChangeLayout(UserRequest ureq) {
		BlockLayoutSettings layoutSettings = getLayoutSettings();
		layoutTabComponents.sync(layoutSettings);
		singleChoice.setLayoutSettings(layoutSettings);
		fireEvent(ureq, new ChangePartEvent(singleChoice));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings();
		alertBoxComponents.sync(alertBoxSettings);
		singleChoice.setAlertBoxSettings(alertBoxSettings);
		fireEvent(ureq, new ChangePartEvent(singleChoice));

		getInitialComponent().setDirty(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public String[] getPresentationKeys() {
		return Arrays.stream(Presentation.values())
				.map(Presentation::name)
				.toArray(String[]::new);
	}
	
	public String[] getPresentationValues() {
		return Arrays.stream(Presentation.values())
				.map(type -> "single.choice.presentation." + type.name().toLowerCase())
				.map(i18n -> getTranslator().translate(i18n))
				.toArray(String[]::new);
	}

}
