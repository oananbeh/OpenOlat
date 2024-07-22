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
package org.olat.modules.ceditor.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.manager.PageDAO;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.ImageComparisonElement;
import org.olat.modules.ceditor.model.ImageComparisonOrientation;
import org.olat.modules.ceditor.model.ImageComparisonSettings;
import org.olat.modules.ceditor.model.ImageComparisonType;
import org.olat.modules.ceditor.model.jpa.ImageComparisonPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaToPagePartDAO;
import org.olat.modules.cemedia.ui.MediaUIHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ImageComparisonInspectorController extends FormBasicController implements PageElementInspectorController {
	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;
	private ImageComparisonElement imageComparisonElement;
	private final PageElementStore<ImageComparisonElement> store;
	private SingleSelection orientationEl;
	private SingleSelection typeEl;
	private SliderElement initialSliderPositionEl;
	private TextAreaElement descriptionEl;
	private List<StaticTextElement> imageNameEls = new ArrayList<>();
	private List<FormLink> chooseImageLinks = new ArrayList<>();
	private List<SingleSelection> imageVersionEls = new ArrayList<>();
	private List<TextElement> textEls = new ArrayList<>();
	private List<FormLink> mediaCenterLinks = new ArrayList<>();
	private List<FormLayoutContainer> imageLayouts = new ArrayList<>();
	private CloseableModalController cmc;
	private ChooseImageController chooseImageController;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ColorService colorService;
	@Autowired
	private MediaToPagePartDAO mediaToPagePartDAO;
	@Autowired
	private PageDAO pageDAO;

	public ImageComparisonInspectorController(UserRequest ureq, WindowControl wControl,
											  ImageComparisonElement imageComparisonElement,
											  PageElementStore<ImageComparisonElement> store) {
		super(ureq, wControl, "tabs_inspector");
		this.imageComparisonElement = imageComparisonElement;
		this.store = store;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("add.imagecomparison");
	}

	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addImageComparisonTab(formLayout);
		addDisplayTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);

		updateUI();
	}

	private void addImageComparisonTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("comparison", getTranslator());
		layoutCont.setElementCssClass("o_image_comparison_inspector_tab");
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.imagecomparison"), layoutCont);

		SelectionValues orientationKV = new SelectionValues();
		for (ImageComparisonOrientation imageComparisonOrientation : ImageComparisonOrientation.values()) {
			orientationKV.add(SelectionValues.entry(imageComparisonOrientation.name(), translate(imageComparisonOrientation.getI18nKey())));
		}
		orientationEl = uifactory.addDropdownSingleselect("imagecomparison.orientation", "imagecomparison.orientation",
				layoutCont, orientationKV.keys(), orientationKV.values(), null);
		orientationEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues typeKV = new SelectionValues();
		for (ImageComparisonType imageComparisonType : ImageComparisonType.values()) {
			typeKV.add(SelectionValues.entry(imageComparisonType.name(), translate(imageComparisonType.getI18nKey())));
		}
		typeEl = uifactory.addDropdownSingleselect("imagecomparison.type", "imagecomparison.type",
				layoutCont, typeKV.keys(), typeKV.values(), null);
		typeEl.addActionListener(FormEvent.ONCHANGE);

		initialSliderPositionEl = uifactory.addSliderElement("imagecomparison.position",
				"imagecomparison.initial.slider.position", layoutCont);
		initialSliderPositionEl.addActionListener(FormEvent.ONCHANGE);
		initialSliderPositionEl.setMinValue(0);
		initialSliderPositionEl.setMaxValue(100);
		initialSliderPositionEl.setStep(5);

		// first image
		addSetImageSection(layoutCont);

		// second image
		addSetImageSection(layoutCont);
	}

	private void addDisplayTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("display", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("image.display"), layoutCont);

		descriptionEl = uifactory.addTextAreaElement("imagecomparison.description", 4, 60, null, layoutCont);
		descriptionEl.addActionListener(FormEvent.ONCHANGE);
	}

	private void addSetImageSection(FormLayoutContainer layoutCont) {
		int index = imageLayouts.size();

		FormLayoutContainer imageLayout = FormLayoutContainer.createVerticalFormLayout("imageLayout" + index, getTranslator());
		imageLayouts.add(imageLayout);
		imageLayout.setFormTitle(getTranslator().translate("imagecomparison.image" + (index + 1)));
		layoutCont.add(imageLayout);

		String page = velocity_root + "/image_search.html";
		FormLayoutContainer imageSearchLayout = FormLayoutContainer.createCustomFormLayout("image.search" + index, getTranslator(), page);
		imageLayout.add(imageSearchLayout);
		imageSearchLayout.setLabel("imagecomparison.image", null);
		String staticTextElementName = "imageName" + index;
		imageSearchLayout.contextPut("staticTextElementName", staticTextElementName);
		StaticTextElement imageNameEl = uifactory.addStaticTextElement(staticTextElementName, "", "", imageSearchLayout);
		imageNameEls.add(imageNameEl);
		String formLinkName = "chooseImageLink" + index;
		imageSearchLayout.contextPut("formLinkName", formLinkName);
		FormLink chooseImageLink = uifactory.addFormLink(formLinkName, "", "", imageSearchLayout, Link.NONTRANSLATED);
		chooseImageLink.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		String chooseImageLabel = getTranslator().translate("choose.image");
		chooseImageLink.setLinkTitle(chooseImageLabel);
		chooseImageLinks.add(chooseImageLink);

		SingleSelection imageVersionEl = uifactory.addDropdownSingleselect("imageVersion" + index,
				"imagecomparison.version", imageLayout, new String[0], new String[0]);
		imageVersionEl.addActionListener(FormEvent.ONCHANGE);
		imageVersionEls.add(imageVersionEl);

		TextElement textEl = uifactory.addTextElement("text" + index, "imagecomparison.text", 40, null, imageLayout);
		textEl.addActionListener(FormEvent.ONCHANGE);
		textEls.add(textEl);

		FormLink mediaCenterLink = uifactory.addFormLink("goto.media.center" + index, "goto.media.center", null, imageLayout, Link.LINK);
		mediaCenterLink.setIconLeftCSS("o_icon o_icon_external_link");
		mediaCenterLinks.add(mediaCenterLink);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(getImageComparisonSettings()), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory,
				getLayoutSettings(getImageComparisonSettings()), velocity_root);
	}

	private void updateUI() {
		ImageComparisonSettings imageComparisonSettings = imageComparisonElement.getSettings();
		orientationEl.select(imageComparisonSettings.getOrientation().name(), true);
		typeEl.select(imageComparisonSettings.getType().name(), true);
		initialSliderPositionEl.setValue(imageComparisonSettings.getInitialSliderPosition() == null ? 50.0 :
				imageComparisonSettings.getInitialSliderPosition());
		descriptionEl.setValue(imageComparisonSettings.getDescription());
		if (imageComparisonElement instanceof ImageComparisonPart imageComparisonPart) {
			List<MediaToPagePart> relations = mediaToPagePartDAO.loadRelations(imageComparisonPart);
			if (!relations.isEmpty()) {
				updateImageEl(0, relations.get(0));
			} else {
				updateImageEl(0, null);
			}
			if (relations.size() >= 2) {
				updateImageEl(1, relations.get(1));
			} else {
				updateImageEl(1, null);
			}
		}
		textEls.get(0).setValue(imageComparisonSettings.getText1());
		textEls.get(1).setValue(imageComparisonSettings.getText2());
	}

	private void updateImageEl(int index, MediaToPagePart mediaToPagePart) {
		StaticTextElement imageNameEl = imageNameEls.get(index);
		if (mediaToPagePart != null) {
			imageNameEl.setValue(mediaToPagePart.getMedia().getTitle());
		} else {
			imageNameEl.setValue("");
		}

		FormLink mediaCenterLink = mediaCenterLinks.get(index);
		mediaCenterLink.setVisible(mediaToPagePart != null);

		SingleSelection imageVersionEl = imageVersionEls.get(index);
		if (mediaToPagePart != null) {
			imageVersionEl.setVisible(true);

			SelectionValues imageVersionKV = new SelectionValues();
			mediaToPagePart.getMedia().getVersions()
					.forEach(mv -> imageVersionKV.add(SelectionValues.entry(mv.getVersionUuid(),
							PageEditorUIFactory.getVersionName(getTranslator(), mv))));
			imageVersionEl.setKeysAndValues(imageVersionKV.keys(), imageVersionKV.values(), null);

			if (mediaToPagePart.getMediaVersion() != null) {
				imageVersionEl.select(mediaToPagePart.getMediaVersion().getVersionUuid(), true);
			} else if (!imageVersionKV.isEmpty()) {
				imageVersionEl.select(imageVersionKV.keys()[0], true);
			}
		} else {
			imageVersionEl.setVisible(false);
		}
	}


	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (cmc == source) {
			cleanUp();
		} else if (chooseImageController == source) {
			if (event == Event.DONE_EVENT && chooseImageController.getUserData() instanceof Integer index) {
				doSaveMedia(ureq, chooseImageController.getMediaReference(), index);
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(chooseImageController);
		cmc = null;
		chooseImageController = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		} else if (orientationEl == source) {
			doSaveSettings(ureq);
		} else if (typeEl == source) {
			doSaveSettings(ureq);
		} else if (initialSliderPositionEl == source) {
			doSaveSettings(ureq);
		} else if (descriptionEl == source) {
			doSaveSettings(ureq);
		} else if (chooseImageLinks.size() >= 2 && chooseImageLinks.get(0) == source) {
			doChooseImage(ureq, 0);
		} else if (chooseImageLinks.size() >= 2 && chooseImageLinks.get(1) == source) {
			doChooseImage(ureq, 1);
		} else if (textEls.size() >= 2 && textEls.get(0) == source) {
			doSaveImageText(ureq, 0);
		} else if (textEls.size() >= 2 && textEls.get(1) == source) {
			doSaveImageText(ureq, 1);
		} else if (imageVersionEls.size() >= 2 && imageVersionEls.get(0) == source) {
			doSaveImageVersion(ureq, 0);
		} else if (imageVersionEls.size() >= 2 && imageVersionEls.get(1) == source) {
			doSaveImageVersion(ureq, 1);
		} else if (mediaCenterLinks.size() >= 2 && mediaCenterLinks.get(0) == source) {
			doShowInMediaCenter(ureq, 0);
		} else if (mediaCenterLinks.size() >= 2 && mediaCenterLinks.get(1) == source) {
			doShowInMediaCenter(ureq, 1);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}

	private void doSaveSettings(UserRequest ureq) {
		ImageComparisonSettings imageComparisonSettings = imageComparisonElement.getSettings();
		imageComparisonSettings.setOrientation(ImageComparisonOrientation.valueOf(orientationEl.getSelectedKey()));
		imageComparisonSettings.setType(ImageComparisonType.valueOf(typeEl.getSelectedKey()));
		imageComparisonSettings.setInitialSliderPosition(initialSliderPositionEl.getValue());
		imageComparisonSettings.setDescription(descriptionEl.getValue());
		imageComparisonElement.setSettings(imageComparisonSettings);
		store.savePageElement(imageComparisonElement);
		dbInstance.commit();
		updateUI();
		fireEvent(ureq, new ChangePartEvent(imageComparisonElement));
	}

	private void doSave(UserRequest ureq) {
		imageComparisonElement = store.savePageElement(imageComparisonElement);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(imageComparisonElement));
	}

	private void doSaveMedia(UserRequest ureq, Media media, int index) {
		if (imageComparisonElement instanceof ImageComparisonPart imageComparisonPart) {
			ImageComparisonPart reloadedPart = (ImageComparisonPart) pageDAO.loadPart(imageComparisonPart);
			List<MediaToPagePart> relations = mediaToPagePartDAO.loadRelations(imageComparisonPart);
			int numberOfMissingRelations = Math.max(index - (relations.size() - 1), 0);
			if (numberOfMissingRelations > 0) {
				for (int i = 0; i < numberOfMissingRelations; i++) {
					mediaToPagePartDAO.persistRelation(reloadedPart, media, null, getIdentity());
				}
				relations = mediaToPagePartDAO.loadRelations(imageComparisonPart);
			}
			mediaToPagePartDAO.updateMedia(relations.get(index), media);
			dbInstance.commit();
			updateUI();

			fireEvent(ureq, new ChangePartEvent(reloadedPart));
		}
	}

	private void doSaveImageVersion(UserRequest ureq, int index) {
		if (imageComparisonElement instanceof ImageComparisonPart imageComparisonPart) {
			ImageComparisonPart reloadedPart = (ImageComparisonPart) pageDAO.loadPart(imageComparisonPart);
			List<MediaToPagePart> relations = mediaToPagePartDAO.loadRelations(reloadedPart);
			MediaToPagePart relation = relations.get(index);
			Media media = relation.getMedia();
			SingleSelection imageVersionEl = imageVersionEls.get(index);
			if (imageVersionEl.isOneSelected()) {
				String imageVersionUuid = imageVersionEl.getSelectedKey();
				for (MediaVersion mediaVersion : media.getVersions()) {
					if (mediaVersion.getVersionUuid().equals(imageVersionUuid)) {
						mediaToPagePartDAO.updateMediaVersion(relations.get(index), mediaVersion, getIdentity());
						dbInstance.commit();
						updateUI();

						fireEvent(ureq, new ChangePartEvent(reloadedPart));
						return;
					}
				}
			}
		}
	}

	private void doShowInMediaCenter(UserRequest ureq, int index) {
		if (imageComparisonElement instanceof ImageComparisonPart imageComparisonPart) {
			ImageComparisonPart reloadedPart = (ImageComparisonPart) pageDAO.loadPart(imageComparisonPart);
			List<MediaToPagePart> relations = mediaToPagePartDAO.loadRelations(reloadedPart);
			MediaToPagePart relation = relations.get(index);
			Media media = relation.getMedia();
			String businessPath = MediaUIHelper.toMediaCenterBusinessPath(media.getKey());
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}

	private void doSaveImageText(UserRequest ureq, int index) {
		ImageComparisonSettings imageComparisonSettings = imageComparisonElement.getSettings();
		switch (index) {
			case 0 -> imageComparisonSettings.setText1(textEls.get(index).getValue());
			case 1 -> imageComparisonSettings.setText2(textEls.get(index).getValue());
		}
		imageComparisonElement.setSettings(imageComparisonSettings);
		store.savePageElement(imageComparisonElement);
		dbInstance.commit();
		updateUI();
		fireEvent(ureq, new ChangePartEvent(imageComparisonElement));
	}

	private void doChangeLayout(UserRequest ureq) {
		ImageComparisonSettings imageComparisonSettings = getImageComparisonSettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(imageComparisonSettings);
		layoutTabComponents.sync(layoutSettings);
		imageComparisonSettings.setLayoutSettings(layoutSettings);

		imageComparisonElement.setSettings(imageComparisonSettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		ImageComparisonSettings imageComparisonSettings = getImageComparisonSettings();

		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(imageComparisonSettings);
		alertBoxComponents.sync(alertBoxSettings);
		imageComparisonSettings.setAlertBoxSettings(alertBoxSettings);

		imageComparisonElement.setSettings(imageComparisonSettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}

	private void doChooseImage(UserRequest ureq, int index) {
		chooseImageController = new ChooseImageController(ureq, getWindowControl(), false);
		chooseImageController.setUserData(index);
		listenTo(chooseImageController);
		String title = translate("choose.image");
		cmc = new CloseableModalController(getWindowControl(), null,
				chooseImageController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private BlockLayoutSettings getLayoutSettings(ImageComparisonSettings imageComparisonSettings) {
		if (imageComparisonSettings.getLayoutSettings() != null) {
			return imageComparisonSettings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings(ImageComparisonSettings imageComparisonSettings) {
		if (imageComparisonSettings.getAlertBoxSettings() != null) {
			return imageComparisonSettings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private ImageComparisonSettings getImageComparisonSettings() {
		if (imageComparisonElement.getSettings() != null) {
			return imageComparisonElement.getSettings();
		}
		return new ImageComparisonSettings();
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
	}
}
