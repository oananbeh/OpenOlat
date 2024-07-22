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
package org.olat.core.commons.services.vfs.manager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.modules.audiovideorecording.AVModule;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2022-09-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class VFSTranscodingServiceImpl implements VFSTranscodingService {

	private static final Logger log = Tracing.createLoggerFor(VFSTranscodingServiceImpl.class);

	private final JobKey vfsJobKey = new JobKey("vfsTranscodingJobDetail", Scheduler.DEFAULT_GROUP);

	@Autowired
	private VFSMetadataDAO vfsMetadataDAO;

	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	@Autowired
	private FolderModule folderModule;

	@Autowired
	private Scheduler scheduler;
	@Autowired
	private AVModule avModule;

	@Override
	public boolean isLocalVideoConversionEnabled() {
		return avModule.isLocalVideoConversionEnabled();
	}

	@Override
	public boolean isLocalAudioConversionEnabled() {
		return avModule.isLocalAudioConversionEnabled();
	}

	@Override
	public boolean isLocalConversionEnabled() {
		return avModule.isLocalVideoConversionEnabled() || avModule.isLocalAudioConversionEnabled();
	}

	@Override
	public List<VFSMetadata> getMetadatasInNeedForTranscoding() {
		return vfsMetadataDAO.getMetadatasInNeedForTranscoding();
	}

	@Override
	public List<VFSMetadata> getMetadatasWithUnresolvedTranscodingStatus() {
		return vfsMetadataDAO.getMetadatasWithUnresolvedTranscodingStatus();
	}

	@Override
	public VFSItem getDestinationItem(VFSMetadata vfsMetadata) {
		return vfsRepositoryService.getItemFor(vfsMetadata);
	}

	@Override
	public String getDirectoryString(VFSItem vfsItem) {
		String relativePath = vfsItem.getMetaInfo().getRelativePath();
		Path directoryPath = Paths.get(folderModule.getCanonicalRoot(), relativePath);
		return directoryPath.toString();
	}

	@Override
	public void setStatus(VFSMetadata vfsMetadata, int status) {
		vfsMetadataDAO.setTranscodingStatus(vfsMetadata.getKey(), status);
		if (status == VFSMetadata.TRANSCODING_STATUS_WAITING) {
			if (vfsRepositoryService.getItemFor(vfsMetadata) instanceof LocalFileImpl mediaLeaf && mediaLeaf.getSize() == 0) {
				File masterFile = getMasterFile(mediaLeaf.getBasefile());
				if (masterFile != null) {
					vfsMetadataDAO.setFileSize(vfsMetadata.getKey(), masterFile.length());
				}
			}
		} else if (status == VFSMetadata.TRANSCODING_STATUS_DONE) {
			if (vfsRepositoryService.getItemFor(vfsMetadata) instanceof VFSLeaf leaf && leaf.getSize() > 0) {
				long fileSize = leaf.getSize();
				vfsMetadataDAO.setFileSize(vfsMetadata.getKey(), fileSize);
			}
		}
	}

	@Override
	public void itemSavedWithTranscoding(VFSLeaf leaf, Identity savedBy) {
		vfsRepositoryService.itemSaved(leaf, savedBy);
		setStatus(leaf.getMetaInfo(), VFSMetadata.TRANSCODING_STATUS_WAITING);
	}

	@Override
	public void startTranscodingProcess() {
		if (!isLocalConversionEnabled()) {
			return;
		}

		try {
			scheduler.triggerJob(vfsJobKey);
		} catch (SchedulerException e) {
			log.error("Cannot start VFS transcoding job", e);
		}
	}

	@Override
	public void fileDoneEvent(VFSMetadata vfsMetadata) {
		VFSTranscodingDoneEvent doneEvent = new VFSTranscodingDoneEvent(vfsMetadata.getFilename());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(doneEvent, ores);
	}

	@Override
	public File getMasterFile(File mediaFile) {
		if (!mediaFile.exists()) {
			return null;
		}
		String parent = mediaFile.getParent();
		if (parent == null) {
			return null;
		}
		String masterFileName = masterFilePrefix + mediaFile.getName();
		File masterFile = new File(parent, masterFileName);
		if (!masterFile.exists()) {
			return null;
		}
		return masterFile;
	}

	@Override
	public void deleteMasterFile(VFSItem item) {
		if (item != null && item.canMeta() == VFSStatus.YES) {
			VFSMetadata metaInfo = item.getMetaInfo();
			if (metaInfo != null && metaInfo.isTranscoded()) {
				VFSContainer parentContainer = item.getParentContainer();
				String name = item.getName();
				String metaName = masterFilePrefix + name;
				VFSItem masterItem = parentContainer.resolve(metaName);
				if (masterItem != null) {
					masterItem.deleteSilently();
				}
			}
		}
	}

	@Override
	public String getHandBrakeCliExecutable() {
		return avModule.getHandBrakeCliCommandPath();
	}

	@Override
	public String getFfmpegExecutable() {
		return avModule.getFfmpegPath();
	}

	@Override
	public void registerForJobDoneEvent(GenericEventListener listener) {
		if (isLocalConversionEnabled()) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(listener, null,
					VFSTranscodingService.ores);
		}
	}

	@Override
	public void deregisterForJobDoneEvent(GenericEventListener listener) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(listener,
				VFSTranscodingService.ores);
	}
}
