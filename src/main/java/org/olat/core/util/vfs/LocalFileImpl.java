/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util.vfs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;

/**
 * Description:<br>
 * VFSLeaf implementation that is based on a java.io.File from a local filesystem 
 * 
 * <P>
 * Initial Date:  23.06.2005 <br>
 *
 * @author Felix Jost
 */
public class LocalFileImpl extends LocalImpl implements VFSLeaf {
	private static final Logger log = Tracing.createLoggerFor(LocalFileImpl.class);

	private LocalFileImpl() {
		super(null, null);
		throw new AssertException("Cannot instantiate LocalFileImpl().");
	}

	/**
	 * Constructor
	 * @param file The real file wrapped by this VFSLeaf
	 */
	public LocalFileImpl(File file) {
		this(file, null);
	}
	
	/**
	 * @param file
	 */
	protected LocalFileImpl(File file, VFSContainer parentContainer) {
		super(file, parentContainer);
	}

	@Override
	public InputStream getInputStream() {
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream( new FileInputStream(getBasefile()) );
		} catch (FileNotFoundException e) {
			log.warn("Could not create input stream for file::{}", getBasefile().getAbsolutePath(), e);
		}
		return bis;
	}

	@Override
	public long getSize() {
		return getBasefile().length();
	}

	@Override
	public String getRelPath() {
		Path bFile = getBasefile().toPath();
		Path bcRoot = FolderConfig.getCanonicalRootPath();
		if(bFile.startsWith(bcRoot)) {
			String relPath = bcRoot.relativize(bFile).toString();
			return "/" + relPath;
		}
		return null;
	}

	@Override
	public VFSStatus canVersion() {
		return VFSRepositoryModule.canVersion(getBasefile());
	}

	@Override
	public OutputStream getOutputStream(boolean append) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(getBasefile(), append);
		} catch (FileNotFoundException e) {
			log.warn("Could not create output stream for file::{}", getBasefile().getAbsolutePath(), e);
		}
		return os;
	}

	@Override
	public VFSSuccess rename(String newname) {
		File f = getBasefile();
		if(!f.exists()) {
			return VFSSuccess.ERROR_FAILED;
		}
		
		if(canMeta() == VFSStatus.YES) {
			CoreSpringFactory.getImpl(VFSRepositoryService.class).rename(this, newname);
		}

		File par = f.getParentFile();
		File nf = new File(par, newname);
		boolean ren = f.renameTo(nf);
		if (ren) {
			// f.renameTo() does NOT modify the path contained in the object f!!
			// The guys at sun consider this a feature and not a bug...
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4094022
			// We need to manually reload the new basefile
			super.setBasefile(new File(nf.getAbsolutePath()));
			return VFSSuccess.SUCCESS;
		}
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess delete() {
		File file = getBasefile();
		if (!file.exists()) {
			return VFSSuccess.SUCCESS;
		}
		
		if (canMeta() == VFSStatus.YES) {
			// Move to trash
			
			// File is already in trash
			File parentFile = file.getParentFile();
			if (VFSRepositoryService.TRASH_NAME.equals(parentFile.getName())) {
				return VFSSuccess.SUCCESS;
			}
			
			VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
			Identity doer = ThreadLocalUserActivityLogger.getLoggedIdentity();
			
			// Get the metadata before moving the file to the trash folder
			VFSMetadata vfsMetadata = vfsRepositoryService.getMetadataFor(this);
			
			File fileInTrash = null;
			if (!getRelPath().contains(VFSRepositoryService.TRASH_NAME)) {
				File trashFile = new File(parentFile, VFSRepositoryService.TRASH_NAME);
				if (!trashFile.exists()) {
					trashFile.mkdirs();
				}
				
				LocalFolderImpl trashContainer = new LocalFolderImpl(trashFile);
				String filenameInTrash = VFSManager.similarButNonExistingName(trashContainer, file.getName(), "_");
				fileInTrash = new File(trashFile, filenameInTrash);
				boolean renamed = file.renameTo(fileInTrash);
				
				if (renamed) {
					super.setBasefile(new File(fileInTrash.getAbsolutePath()));
				}
			} else {
				fileInTrash = file;
			}
			
			if (vfsMetadata != null) {
				vfsRepositoryService.markAsDeleted(doer, vfsMetadata, fileInTrash);
				return VFSSuccess.SUCCESS;
			}
			return VFSSuccess.ERROR_FAILED;
		}
		
		return deleteBasefile();
	}

	@Override
	public VFSSuccess restore(VFSContainer targetContainer) {
		if (targetContainer.canWrite() != VFSStatus.YES) {
			return VFSSuccess.ERROR_FAILED;
		}
		if (canMeta() != VFSStatus.YES) {
			return VFSSuccess.ERROR_FAILED;
		}
		File file = getBasefile();
		if (!file.exists()) {
			return VFSSuccess.ERROR_FAILED;
		}
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		Identity doer = ThreadLocalUserActivityLogger.getLoggedIdentity();
		
		VFSMetadata vfsMetadata = vfsRepositoryService.getMetadataFor(this);
		if (vfsMetadata == null || !vfsMetadata.isDeleted()) {
			return VFSSuccess.ERROR_FAILED;
		}
		
		if (targetContainer instanceof LocalFolderImpl localFolder) {
			long quotaLeft = VFSManager.getQuotaLeftKB(targetContainer);
			if (quotaLeft != Quota.UNLIMITED && quotaLeft < (file.length() / 1024)) {
				return VFSSuccess.ERROR_QUOTA_EXCEEDED;
			}
			
			File restoredFile;
			if (getRelPath().contains(VFSRepositoryService.TRASH_NAME)) {
				File folder = localFolder.getBasefile();
				String restoredFilename = VFSManager.similarButNonExistingName(targetContainer, file.getName(), "_");
				restoredFile = new File(folder, restoredFilename);
				boolean renamed = file.renameTo(restoredFile);
				if (renamed) {
					super.setBasefile(new File(restoredFile.getAbsolutePath()));
				}
			} else {
				restoredFile = file;
			}
			
			vfsRepositoryService.unmarkFromDeleted(doer, vfsMetadata, restoredFile);
			return VFSSuccess.SUCCESS;
		}
		
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess deleteSilently() {
		if(canMeta() == VFSStatus.YES) {
			CoreSpringFactory.getImpl(VFSRepositoryService.class).deleteMetadata(getMetaInfo());
		} else {
			// some lock can create a metadata object with canMeta() == NO
			CoreSpringFactory.getImpl(VFSRepositoryService.class).deleteMetadata(getBasefile());
		}
		return deleteBasefile();
	}
	
	private VFSSuccess deleteBasefile() {
		VFSSuccess status = VFSSuccess.ERROR_FAILED;
		try {
			if(!Files.deleteIfExists(getBasefile().toPath())) {
				log.debug("Cannot delete base file because it doesn't exist: {}", this);
			}
			status = VFSSuccess.SUCCESS;
		} catch(IOException e) {
			log.error("Cannot delete base file: {}", this, e);
		}
		return status;
	}

	@Override
	public VFSItem resolve(String path) {
		path = VFSManager.sanitizePath(path);
		if (path.equals("/")) return this;
		String name = VFSManager.extractChild(path);
		if (path.equals(name)) {
			return this;
		} else  {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "LFile [file="+getBasefile()+"] ";
	}
}

