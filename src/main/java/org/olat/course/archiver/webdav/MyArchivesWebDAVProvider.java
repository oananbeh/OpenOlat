/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.archiver.webdav;

import java.util.Locale;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.archiver.ArchivesController;

/**
 * 
 * Initial date: 26 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyArchivesWebDAVProvider implements WebDAVProvider {

	private static final String MOUNTPOINT = "mycoursearchives";

	@Override
	public String getMountPoint() {
		return MOUNTPOINT;
	}
	
	@Override
	public int getSortOrder() {
		return 100;
	}
	
	@Override
	public String getIconCss() {
		return "o_icon_coursearchive";
	}

	@Override
	public String getName(Locale locale) {
		return Util.createPackageTranslator(ArchivesController.class, locale).translate("webdav.name");
	}
	
	@Override
	public boolean hasAccess(UserSession usess) {
		if(usess == null) {
			return false;
		}
		IdentityEnvironment identityEnv = usess.getIdentityEnvironment();
		return identityEnv != null && identityEnv.getRoles() != null && identityEnv.getRoles()
				.hasSomeRoles(OrganisationRoles.author, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator);
	}

	@Override
	public VFSContainer getContainer(UserSession usess) {
		IdentityEnvironment identityEnv = usess.getIdentityEnvironment();
		return new MyArchivesWebDAVSource(identityEnv);
	}
}
