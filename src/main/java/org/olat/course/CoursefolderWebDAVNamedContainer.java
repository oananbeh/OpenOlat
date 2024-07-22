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
package org.olat.course;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoursefolderWebDAVNamedContainer extends NamedContainerImpl {
	
	private static final Logger log = Tracing.createLoggerFor(CoursefolderWebDAVNamedContainer.class);
	
	private RepositoryEntry entry;
	private VFSContainer parentContainer;
	
	private final Boolean entryAdmin;
	private final IdentityEnvironment identityEnv;
	
	public CoursefolderWebDAVNamedContainer(String courseTitle, RepositoryEntry entry, IdentityEnvironment identityEnv, Boolean entryAdmin) {
		super(courseTitle, null);
		this.entry = entry;
		this.entryAdmin = entryAdmin;
		this.identityEnv = identityEnv;
		setIconCSS("o_CourseModule_icon");
	}
	

	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return null;
	}

	@Override
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		//
	}
	
	@Override
	public boolean exists() {
		return true;
	}
	
	@Override
	public VFSStatus canMeta() {
		return VFSStatus.NO;
	}

	@Override
	public VFSStatus canWrite() {
		return getDelegate().canWrite();
	}

	@Override
	public VFSStatus canCopy() {
		return getDelegate().canCopy();
	}

	@Override
	public VFSStatus canVersion() {
		return VFSStatus.NO;
	}

	@Override
	public VFSMetadata getMetaInfo() {
		// Don't return the metainfo from the /coursefolder/ because this container
		// is a virtual one.
		return null;
	}

	@Override
	public VFSContainer getDelegate() {
		if(super.getDelegate() == null) {
			try {
				VFSContainer courseFolder = CourseFactory.loadCourseContainer(entry, identityEnv, CourseContainerOptions.all(), false, entryAdmin);
				setDelegate(courseFolder);
				if(parentContainer != null) {
					super.setParentContainer(parentContainer);
					parentContainer = null;
				}
			} catch (CorruptedCourseException e) {
				// Avoid RS 
				setDelegate(new VirtualContainer("ERROR"));
				log.warn("Error loading course: {}", e.getMessage());
			} catch (Exception e) {
				setDelegate(new VirtualContainer("ERROR"));
				log.error("Error loading course: {}", entry, e);
			}
		}
		return super.getDelegate();
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		if(super.getDelegate() == null) {
			this.parentContainer = parentContainer;
		} else {
			super.setParentContainer(parentContainer);
		}
	}
}
