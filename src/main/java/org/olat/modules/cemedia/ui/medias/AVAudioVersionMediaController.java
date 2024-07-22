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

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.avrecorder.AVConfiguration;
import org.olat.core.gui.avrecorder.AVCreationController;
import org.olat.core.gui.avrecorder.AVCreationEvent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaService;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-10-31<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVAudioVersionMediaController extends BasicController {

	private Media mediaReference;

	private final AVCreationController creationController;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public AVAudioVersionMediaController(UserRequest ureq, WindowControl wControl, Media mediaReference,
										 long recordingLengthLimit) {
		super(ureq, wControl);
		this.mediaReference = mediaReference;

		AVConfiguration config = new AVConfiguration();
		config.setMode(AVConfiguration.Mode.audio);
		config.setAudioRendererActive(true);
		config.setRecordingLengthLimit(recordingLengthLimit);

		creationController = new AVCreationController(ureq, wControl, config);
		listenTo(creationController);

		VelocityContainer mainVC = createVelocityContainer("av_wrapper");
		mainVC.put("component", creationController.getInitialComponent());

		putInitialPanel(mainVC);
	}

	public Media getMediaReference() {
		return mediaReference;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (creationController == source) {
			if (event instanceof AVCreationEvent) {
				doSetVersion(ureq);
			}
		}
	}

	private void doSetVersion(UserRequest ureq) {
		String fileName = creationController.getFileName();
		File tempFile = creationController.getRecordedFile();
		mediaReference = mediaService.getMediaByKey(mediaReference.getKey());
		mediaService.addVersion(mediaReference, tempFile, fileName, getIdentity(), MediaLog.Action.RECORDED);
		if (mediaReference != null && !mediaReference.getVersions().isEmpty()) {
			VFSMetadata metadata = mediaReference.getVersions().get(0).getMetadata();
			if (vfsRepositoryService.getItemFor(metadata) instanceof VFSLeaf leaf) {
				creationController.triggerConversionIfNeeded(leaf);
			}
		}
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
