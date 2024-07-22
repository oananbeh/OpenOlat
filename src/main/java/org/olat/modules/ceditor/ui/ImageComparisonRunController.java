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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.ImageComparisonOrientation;
import org.olat.modules.ceditor.model.ImageComparisonSettings;
import org.olat.modules.ceditor.model.ImageComparisonType;
import org.olat.modules.ceditor.model.jpa.ImageComparisonPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.cemedia.manager.MediaToPagePartDAO;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ImageComparisonRunController extends BasicController implements PageRunElement {

	private static final String BEFORE_IMAGE_ID = "beforeImage";
	private static final String BEFORE_IMAGE_REL_PATH = "/static/images/media/image_regular.svg";
	private static final String AFTER_IMAGE_ID = "afterImage";
	private static final String AFTER_IMAGE_REL_PATH = "/static/images/media/image_solid.svg";

	private final VelocityContainer mainVC;
	private ImageComparisonPart imageComparisonPart;
	private ImageComparisonImages images;
	private final boolean editable;

	@Autowired
	private MediaService mediaService;
	@Autowired
	private MediaToPagePartDAO mediaToPagePartDAO;

	public ImageComparisonRunController(UserRequest ureq, WindowControl wControl, ImageComparisonPart imageComparisonPart, boolean editable) {
		super(ureq, wControl);
		this.imageComparisonPart = imageComparisonPart;
		this.editable = editable;
		mainVC = createVelocityContainer("image_comparison_run");
		mainVC.setElementCssClass("o_image_comparison_run_element_css_class");
		setBlockLayoutClass(imageComparisonPart.getSettings());
		putInitialPanel(mainVC);
		initUI(ureq);
		updateUI();
	}

	private void setBlockLayoutClass(ImageComparisonSettings settings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(settings, false));
	}

	private void initUI(UserRequest ureq) {
		images = new ImageComparisonImages(new ArrayList<>());

		addImagesCompareJs();

		String mapperUrl = registerCacheableMapper(ureq, "image-comparison-" + imageComparisonPart.getId(),
				new ImageComparisonMapper(imageComparisonPart, images, mediaService));
		mainVC.contextPut("mapperUrl", mapperUrl);
	}

	private void addImagesCompareJs() {
		List<String> jsPath = new ArrayList<>();
		List<String> cssPath = new ArrayList<>();
		jsPath.add("js/twentyTwenty/jquery.event.move.js");
		jsPath.add("js/twentyTwenty/oo_jquery.twentytwenty.js");
		cssPath.add(StaticMediaDispatcher.getStaticURI("js/twentyTwenty/twentytwenty.css"));

		JSAndCSSComponent js = new JSAndCSSComponent("js",
				jsPath.toArray(String[]::new),
				cssPath.toArray(String[]::new));
		mainVC.put("js", js);

	}

	private void updateUI() {
		ImageComparisonSettings imageComparisonSettings = imageComparisonPart.getSettings();

		List<ImageComparisonImageItem> imageComparisonImageItems = mediaToPagePartDAO.loadRelations(imageComparisonPart)
				.stream().map(this::getMediaVersion).filter(Objects::nonNull)
				.map(mv -> new ImageComparisonImageItem(Long.toString(mv.getKey()), mv.getMedia().getType(), mv.getMedia().getAltText(), mv))
				.toList();
		if (imageComparisonSettings.getInitialSliderPosition() == null) {
			mainVC.contextPut("initialSliderPosition", null);
		} else {
			double initialSliderPosition = imageComparisonSettings.getInitialSliderPosition() / 100.0;
			initialSliderPosition = Math.max(0.001, initialSliderPosition);
			mainVC.contextPut("initialSliderPosition", initialSliderPosition);
		}
		mainVC.contextPut("beforeImageId", imageComparisonImageItems.isEmpty() ? BEFORE_IMAGE_ID : imageComparisonImageItems.get(0).id);
		mainVC.contextPut("afterImageId", imageComparisonImageItems.size() < 2 ? AFTER_IMAGE_ID : imageComparisonImageItems.get(1).id);
		mainVC.contextPut("beforeText", imageComparisonSettings.getText1());
		mainVC.contextPut("afterText", imageComparisonSettings.getText2());

		mainVC.contextPut("beforeAltText", translate("imagecomparison.image1"));
		mainVC.contextPut("afterAltText", translate("imagecomparison.image2"));
		if (StringHelper.containsNonWhitespace(imageComparisonSettings.getText1())) {
			mainVC.contextPut("beforeAltText", imageComparisonSettings.getText1());
		}
		if (StringHelper.containsNonWhitespace(imageComparisonSettings.getText2())) {
			mainVC.contextPut("afterAltText", imageComparisonSettings.getText2());
		}
		if (imageComparisonImageItems.size() >= 2) {
			if (StringHelper.containsNonWhitespace(imageComparisonImageItems.get(0).altText)) {
				mainVC.contextPut("beforeAltText", imageComparisonImageItems.get(0).altText);
			}
			if (StringHelper.containsNonWhitespace(imageComparisonImageItems.get(1).altText)) {
				mainVC.contextPut("afterAltText", imageComparisonImageItems.get(1).altText);
			}
		}

		mainVC.contextPut("horizontal", ImageComparisonOrientation.horizontal.equals(imageComparisonSettings.getOrientation()));
		mainVC.contextPut("wrongRight", ImageComparisonType.wrongRight.equals(imageComparisonSettings.getType()));
		mainVC.contextPut("description", imageComparisonSettings.getDescription());
		images.items.clear();
		images.items.addAll(imageComparisonImageItems);
	}

	private MediaVersion getMediaVersion(MediaToPagePart relation) {
		if (relation.getMediaVersion() != null) {
			return relation.getMediaVersion();
		}
		return relation.getMedia().getVersions().get(0);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement() instanceof ImageComparisonPart updatedImageComparisonPart) {
				imageComparisonPart = updatedImageComparisonPart;
				setBlockLayoutClass(imageComparisonPart.getSettings());
				updateUI();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return false;
	}

	public record ImageComparisonImageItem(String id, String type, String altText, MediaVersion mediaVersion) {}

	public record ImageComparisonImages(List<ImageComparisonImageItem> items) {

		public ImageComparisonImageItem getImageById(String id) {
			if (!StringHelper.containsNonWhitespace(id)) {
				return null;
			}
			for (ImageComparisonImageItem item : items) {
				if (id.equals(item.id)) {
					return item;
				}
			}
			return null;
		}
	}

	public record ImageComparisonMapper(ImageComparisonPart imageComparisonPart, ImageComparisonImages imageComparisonImages,
										MediaService mediaService) implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource mediaResource = null;

			String id = relPath.startsWith("/") ? relPath.substring(1) : relPath;
			ImageComparisonImageItem image = imageComparisonImages.getImageById(id);
			if (image != null) {
				MediaHandler mediaHandler = mediaService.getMediaHandler(image.type);
				if (mediaHandler instanceof ImageHandler imageHandler) {
					VFSItem imageVfsItem = imageHandler.getImage(image.mediaVersion);
					if (imageVfsItem instanceof VFSLeaf imageVfsLeaf) {
						mediaResource = new VFSMediaResource(imageVfsLeaf);
					}
				}
			}
			if (BEFORE_IMAGE_ID.equals(id)) {
				String path = WebappHelper.getContextRoot() + BEFORE_IMAGE_REL_PATH;
				LocalFileImpl beforeImageLeaf = new LocalFileImpl(new File(path));
				mediaResource = new VFSMediaResource(beforeImageLeaf);
			}
			if (AFTER_IMAGE_ID.equals(id)) {
				String path = WebappHelper.getContextRoot() + AFTER_IMAGE_REL_PATH;
				LocalFileImpl beforeImageLeaf = new LocalFileImpl(new File(path));
				mediaResource = new VFSMediaResource(beforeImageLeaf);
			}
			return mediaResource == null ? new NotFoundMediaResource() : mediaResource;
		}
	}
}
