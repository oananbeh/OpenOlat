/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.home;

import java.util.Comparator;
import java.util.List;

import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderControllerConfig;
import org.olat.core.commons.services.folder.ui.FolderEmailFilter;
import org.olat.core.commons.services.folder.ui.FolderUIFactory;
import org.olat.core.commons.services.folder.ui.TranslatedWebDAVProvider;
import org.olat.core.commons.services.folder.ui.event.FolderRootEvent;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VirtualContainer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PersonalFileHubMountPointsController extends BasicController implements Activateable2 {

	private static final FolderControllerConfig FOLDER_CONFIG = FolderControllerConfig.builder()
			.withDisplaySubscription(false)
			.withDisplayQuotaLink(false)
			.withFileHub(true)
			.withMail(FolderEmailFilter.publicOnly)
			.build();
	private static final String CMD_OPEN = "open";
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackedPanel;
	private final List<Link> links;

	private FolderController folderCtrl;

	private final String fileHubName;
	private int counter = 0;
	
	@Autowired
	private WebDAVModule webdavModule;

	public PersonalFileHubMountPointsController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackedPanel, String fileHubName) {
		super(ureq, wControl);
		this.stackedPanel = stackedPanel;
		this.fileHubName = fileHubName;

		velocity_root = Util.getPackageVelocityRoot(FolderUIFactory.class);
		mainVC = createVelocityContainer("browser_mega_buttons");
		putInitialPanel(mainVC);
		
		Comparator.comparing(TranslatedWebDAVProvider::getSortOrder).thenComparing(TranslatedWebDAVProvider::getName);

		UserSession usess = ureq.getUserSession();
		links = webdavModule.getWebDAVProviders().values().stream()
				.filter(WebDAVProvider::isDisplayInFileHub)
				.filter(provider -> provider.hasAccess(usess))
				.map(provider -> new TranslatedWebDAVProvider(provider, getLocale()))
				.sorted(Comparator.comparing(TranslatedWebDAVProvider::getSortOrder).thenComparing(TranslatedWebDAVProvider::getName))
				.map(this::createLink)
				.toList();
		mainVC.contextPut("links", links);
	}

	private Link createLink(TranslatedWebDAVProvider provider) {
		Link link = LinkFactory.createCustomLink("cont_" + counter++, CMD_OPEN, null, Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, mainVC, this);
		link.setElementCssClass("btn btn-default o_button_mega o_sel_" + provider.getProvider().getMountPoint().replace(" ", "_"));
		link.setIconLeftCSS("o_icon o_icon-xl " + provider.getProvider().getIconCss());
		String text = "<div class=\"o_mega_headline\">" + provider.getName() + "</div>";
		text += "<div class=\"o_mega_subline\">" + "</div>";
		link.setCustomDisplayText(text);
		link.setUserObject(provider.getProvider());
		return link;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
		if (StringHelper.containsNonWhitespace(path)) {
			UserSession usess = ureq.getUserSession();
			
			String[] pathParts = path.split("/");
			if (pathParts.length >= 2) {
				String providerName = pathParts[1];
				links.stream()
					.map(link -> (WebDAVProvider)link.getUserObject())
					.filter(provider -> providerName.equalsIgnoreCase(provider.getContainer(usess).getName()))
					.findFirst().ifPresent(provider -> {
						doOpen(ureq, provider);
						if (folderCtrl != null) {
							folderCtrl.activate(ureq, entries, state);
						}
					});
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			String command = link.getCommand();
			if (CMD_OPEN.equals(command)) {
				if (link.getUserObject() instanceof WebDAVProvider provider) {
					doOpen(ureq, provider);
				}
			}
		}
	}
	
	private void doOpen(UserRequest ureq, WebDAVProvider provider) {
		UserSession usess = ureq.getUserSession();
		VFSContainer vfsContainer = provider.getContainer(usess);
		vfsContainer = new NamedContainerImpl(provider.getName(getLocale()), vfsContainer);
		
		VirtualContainer fileHubContainer = new VirtualContainer(fileHubName);
		fileHubContainer.addItem(vfsContainer);

		folderCtrl = new FolderController(ureq, getWindowControl(), fileHubContainer, FOLDER_CONFIG);
		listenTo(folderCtrl);
		folderCtrl.updateCurrentContainer(ureq, vfsContainer, true);
		
		String providerName = provider.getName(getLocale());
		stackedPanel.pushController(providerName, folderCtrl);
		stackedPanel.setInvisibleCrumb(2);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == folderCtrl) {
			if (event == FolderRootEvent.EVENT) {
				stackedPanel.setInvisibleCrumb(1);
				stackedPanel.popUpToRootController(ureq);
			}
		}
		super.event(ureq, source, event);
	}

}
