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
package org.olat.modules.docpool.webdav;

import java.util.Locale;

import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.docpool.ui.DocumentPoolMainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("taxonomyDocumentsLibraryWebDAVProvider")
public class DocumentPoolWebDAVProvider implements WebDAVProvider {

	@Autowired
	private DocumentPoolModule docPoolModule;

	@Override
	public String getMountPoint() {
		String mountPoint = docPoolModule.getWebDAVMountPoint();
		if(!StringHelper.containsNonWhitespace(mountPoint)) {
			mountPoint = "docpool";
		}
		return mountPoint;
	}
	
	@Override
	public int getSortOrder() {
		return 100;
	}
	
	@Override
	public String getIconCss() {
		return "o_icon_taxonomy";
	}

	@Override
	public String getName(Locale locale) {
		return Util.createPackageTranslator(DocumentPoolMainController.class, locale).translate("webdav.name");
	}
	
	@Override
	public boolean hasAccess(UserSession usess) {
		return docPoolModule.isEnabled() && usess != null && usess.getIdentityEnvironment() != null;
	}

	@Override
	public VFSContainer getContainer(UserSession usess) {
		IdentityEnvironment identityEnv = usess.getIdentityEnvironment();
		return new DocumentPoolWebDAVMergeSource("docpool", identityEnv);
	}
}