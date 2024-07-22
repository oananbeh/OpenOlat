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
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.forms.model.xml.TextInput;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputInspectorController extends FormBasicController implements PageElementInspectorController {

	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";
	private static final String INPUT_TYPE_TEXT_KEY = "textinput.numeric.text";
	private static final String INPUT_TYPE_NUMERIC_KEY = "textinput.numeric.numeric";
	private static final String INPUT_TYPE_DATE_KEY = "textinput.numeric.date";
	private static final String[] NUMERIC_KEYS = new String[] {
			INPUT_TYPE_TEXT_KEY,
			INPUT_TYPE_NUMERIC_KEY,
			INPUT_TYPE_DATE_KEY
	};
	private static final String SINGLE_ROW_KEY = "textinput.single.row";
	private static final String MULTIPLE_ROWS_KEY = "textinput.multiple.rows";
	private static final String[] ROW_OPTIONS = new String[] {
			SINGLE_ROW_KEY,
			MULTIPLE_ROWS_KEY
	};

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;

	private SingleSelection inputTypeEl;
	private SingleSelection singleRowEl;
	private TextElement rowsEl;
	private TextElement numericMinEl;
	private TextElement numericMaxEl;
	private SingleSelection obligationEl;
	private FormLink saveButton;
	
	private final TextInput textInput;
	private final boolean restrictedEdit;

	@Autowired
	private ColorService colorService;

	public TextInputInspectorController(UserRequest ureq, WindowControl wControl, TextInput textInput, boolean restrictedEdit) {
		super(ureq, wControl, "textinput_editor");
		this.textInput = textInput;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		return translate("inspector.formtextinput");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addGeneralTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);

		updateUI();
	}

	private void addGeneralTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.general"), layoutCont);

		inputTypeEl = uifactory.addRadiosVertical("textinput_num_" + CodeHelper.getRAMUniqueID(),
				"textinput.numeric", layoutCont, NUMERIC_KEYS, translateAll(getTranslator(), NUMERIC_KEYS));
		if (textInput.isNumeric()) {
			inputTypeEl.select(INPUT_TYPE_NUMERIC_KEY, true);
		} else if (textInput.isDate()) {
			inputTypeEl.select(INPUT_TYPE_DATE_KEY, true);
		} else {
			inputTypeEl.select(INPUT_TYPE_TEXT_KEY, true);
		}
		inputTypeEl.addActionListener(FormEvent.ONCHANGE);
		inputTypeEl.setEnabled(!restrictedEdit);

		singleRowEl = uifactory.addRadiosVertical("textinput_row_" + CodeHelper.getRAMUniqueID(),
				"textinput.rows.mode", layoutCont, ROW_OPTIONS, translateAll(getTranslator(), ROW_OPTIONS));
		String selectedRowsKey = textInput.isSingleRow()? SINGLE_ROW_KEY: MULTIPLE_ROWS_KEY;
		singleRowEl.select(selectedRowsKey, true);
		singleRowEl.addActionListener(FormEvent.ONCHANGE);
		singleRowEl.setEnabled(!restrictedEdit);

		String rows = "";
		if(textInput.getRows() > 0) {
			rows = Integer.toString(textInput.getRows());
		}
		rowsEl = uifactory.addTextElement("textinput_rows_" + CodeHelper.getRAMUniqueID(), "textinput.rows", 8, rows, layoutCont);

		String numericMin = textInput.getNumericMin() != null? textInput.getNumericMin().toString(): "";
		numericMinEl = uifactory.addTextElement("textinput_nmin_" + CodeHelper.getRAMUniqueID(),
				"textinput.numeric.min", 8, numericMin, layoutCont);
		numericMinEl.setEnabled(!restrictedEdit);

		String numericMax = textInput.getNumericMax() != null? textInput.getNumericMax().toString(): "";
		numericMaxEl = uifactory.addTextElement( "textinput_nmax_" + CodeHelper.getRAMUniqueID(),
				"textinput.numeric.max", 8, numericMax, layoutCont);
		numericMaxEl.setEnabled(!restrictedEdit);

		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosVertical("obli_" + CodeHelper.getRAMUniqueID(), "obligation", layoutCont,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, textInput.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !textInput.isMandatory());
		obligationEl.setEnabled(!restrictedEdit);

		saveButton = uifactory.addFormLink("save_" + CodeHelper.getRAMUniqueID(), "save", null, layoutCont, Link.BUTTON);
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
		if (textInput.getLayoutSettings() != null) {
			return textInput.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings() {
		if (textInput.getAlertBoxSettings() != null) {
			return textInput.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private void updateUI() {
		boolean isText = INPUT_TYPE_TEXT_KEY.equals(inputTypeEl.getSelectedKey());
		boolean isMultipleRows = MULTIPLE_ROWS_KEY.equals(singleRowEl.getSelectedKey());
		singleRowEl.setVisible(isText);
		rowsEl.setVisible(isText && isMultipleRows);
		
		boolean numeric = INPUT_TYPE_NUMERIC_KEY.equals(inputTypeEl.getSelectedKey());
		numericMinEl.setVisible(numeric);
		numericMaxEl.setVisible(numeric);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		rowsEl.clearError();
		if(rowsEl.isVisible() && StringHelper.containsNonWhitespace(rowsEl.getValue())) {
			try {
				Integer.parseInt(rowsEl.getValue());
			} catch (NumberFormatException e) {
				rowsEl.setErrorKey("form.error.nointeger");
				allOk &= false;
			}
		}
		
		allOk &= validateDouble(numericMinEl);
		allOk &= validateDouble(numericMaxEl);
		if (allOk && numericMinEl.isVisible() && numericMaxEl.isVisible()
				&& StringHelper.containsNonWhitespace(numericMinEl.getValue())
				&& StringHelper.containsNonWhitespace(numericMaxEl.getValue()) 
				&& Double.parseDouble(numericMinEl.getValue()) >= Double.parseDouble(numericMaxEl.getValue())) {
			numericMaxEl.setErrorKey("error.numeric.max.lower.min");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateDouble(TextElement element) {
		element.clearError();
		if (element.isVisible()) {
			String value = element.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Double.parseDouble(value);
				} catch (NumberFormatException e) {
					element.setErrorKey("error.no.number");
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (inputTypeEl == source || singleRowEl == source) {
			updateUI();
			doSave(ureq);
		} else if (saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}	
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
		fireEvent(ureq, new ClosePartEvent(textInput));
	}
	
	private void doSave(UserRequest ureq) {
		boolean numeric = INPUT_TYPE_NUMERIC_KEY.equals(inputTypeEl.getSelectedKey());
		textInput.setNumeric(numeric);
		
		boolean date = INPUT_TYPE_DATE_KEY.equals(inputTypeEl.getSelectedKey());
		textInput.setDate(date);
		
		boolean singleRow = SINGLE_ROW_KEY.equals(singleRowEl.getSelectedKey());
		textInput.setSingleRow(singleRow);
		
		if(StringHelper.containsNonWhitespace(rowsEl.getValue())) {
			try {
				int rows = Integer.parseInt(rowsEl.getValue());
				textInput.setRows(rows);
			} catch (NumberFormatException e) {
				logError("Cannot parse integer: " + rowsEl.getValue(), null);
			}
		}
		
		Double numericMin = null;
		if(StringHelper.containsNonWhitespace(numericMinEl.getValue())) {
			try {
				numericMin = Double.parseDouble(numericMinEl.getValue());
			} catch (NumberFormatException e) {
				logError("Cannot parse double: " + numericMinEl.getValue(), null);
			}
		}
		textInput.setNumericMin(numericMin);
		
		Double numericMax = null;
		if(StringHelper.containsNonWhitespace(numericMaxEl.getValue())) {
			try {
				numericMax = Double.parseDouble(numericMaxEl.getValue());
			} catch (NumberFormatException e) {
				logError("Cannot parse double: " + numericMaxEl.getValue(), null);
			}
		}
		textInput.setNumericMax(numericMax);
		
		boolean mandatory = OBLIGATION_MANDATORY_KEY.equals(obligationEl.getSelectedKey());
		textInput.setMandatory(mandatory);
		fireEvent(ureq, new ChangePartEvent(textInput));
	}

	private void doChangeLayout(UserRequest ureq) {
		BlockLayoutSettings layoutSettings = getLayoutSettings();
		layoutTabComponents.sync(layoutSettings);
		textInput.setLayoutSettings(layoutSettings);
		fireEvent(ureq, new ChangePartEvent(textInput));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings();
		alertBoxComponents.sync(alertBoxSettings);
		textInput.setAlertBoxSettings(alertBoxSettings);
		fireEvent(ureq, new ChangePartEvent(textInput));

		getInitialComponent().setDirty(true);
	}
}
