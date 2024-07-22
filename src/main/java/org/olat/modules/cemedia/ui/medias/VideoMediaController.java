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
package org.olat.modules.cemedia.ui.medias;

import java.io.File;

import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSTranscodingDoneEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.ModalInspectorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ChangeVersionPartEvent;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaMetadataController;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoMediaController extends BasicController implements GenericEventListener {
	
	private final ImageComponent videoCmp;
	
	private final DataStorage dataStorage;
	private final VelocityContainer mainVC;
	private MediaVersion version;

	@Autowired
	VFSTranscodingService vfsTranscodingService;
	@Autowired
	private MediaService mediaService;


	public VideoMediaController(UserRequest ureq, WindowControl wControl, PageElement pageElement, DataStorage dataStorage, MediaVersion version, RenderingHints hints) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		this.dataStorage = dataStorage;
		this.version = version;
		
		mainVC = createVelocityContainer("media_video");
		setBlockLayoutClass(pageElement);

		videoCmp = new ImageComponent(ureq.getUserSession(), "image");
		setMedia();
		mainVC.put("video", videoCmp);
		mainVC.contextPut("pdf", hints.isToPdf());
		if(hints.isToPdf()) {
			videoCmp.setMaxWithAndHeightToFitWithin(800, 600);
			Size size = videoCmp.getScaledSize();
			if(size != null) {
				mainVC.contextPut("scaledSize", size);
			}
		}
		
		if(hints.isExtendedMetadata()) {
			MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, version.getMedia());
			listenTo(metaCtrl);
			mainVC.put("meta", metaCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);

		vfsTranscodingService.registerForJobDoneEvent(this);
	}

	private void setBlockLayoutClass(PageElement pageElement) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(pageElement, false));
	}

	private void setMedia() {
		File mediaFile = dataStorage.getFile(version);
		videoCmp.setMedia(mediaFile);
		if (version.getMetadata().isInTranscoding()) {
			File masterFile = vfsTranscodingService.getMasterFile(mediaFile);
			if (masterFile != null) {
				videoCmp.setMedia(masterFile);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof ModalInspectorController && event instanceof ChangeVersionPartEvent cvpe) {
			PageElement element = cvpe.getElement();
			if(element instanceof MediaPart videoPart) {
				File mediaFile = dataStorage.getFile(videoPart.getStoredData());
				if(mediaFile != null) {
					videoCmp.setMedia(mediaFile);
				}
			}
		} else if (source instanceof ModalInspectorController && event instanceof ChangePartEvent changePartEvent) {
			setBlockLayoutClass(changePartEvent.getElement());
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		vfsTranscodingService.deregisterForJobDoneEvent(this);
		videoCmp.dispose();
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if (event instanceof VFSTranscodingDoneEvent doneEvent) {
			VFSLeaf media = videoCmp.getMedia();
			if (media == null) {
				return;
			}
			if (media.getName().contains(doneEvent.getFileName())) {
				version = mediaService.getMediaVersionByKey(version.getKey());
				setMedia();
			}
		}
	}
}
