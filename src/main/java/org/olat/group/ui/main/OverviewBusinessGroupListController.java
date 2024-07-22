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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.ui.lifecycle.OverviewBusinessGroupLifecycleController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OverviewBusinessGroupListController extends BasicController implements Activateable2 {
	
	private Link groupsLink;
	private Link lifecycleLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;

	private Controller currentCtrl;
	private BusinessGroupListWrapperController myGroupsCtrl;
	private OverviewBusinessGroupLifecycleController lifecycleCtrl;
	
	private final boolean isGuestOnly;
	
	public OverviewBusinessGroupListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		
		mainVC = createVelocityContainer("group_list_overview");
	
		//segmented view
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		if(!isGuestOnly) {
			groupsLink = LinkFactory.createLink("main.menu.title", mainVC, this);
			groupsLink.setElementCssClass("o_sel_group_all_groups_seg");
			segmentView.addSegment(groupsLink, false);
		}
		
		Roles roles = ureq.getUserSession().getRoles();
		if(roles.isGroupManager() || roles.isAdministrator()) {
			lifecycleLink = LinkFactory.createLink("opengroups.search.admin", mainVC, this);
			lifecycleLink.setElementCssClass("o_sel_group_lifecyle_groups_seg");
			segmentView.addSegment(lifecycleLink, false);
			mainVC.contextPut("withSegment", Boolean.TRUE);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {			
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				Controller selectedController = null;
				if (clickedLink == groupsLink) {
					BusinessGroupListWrapperController bController = updateGroups(ureq);
					bController.activate(ureq, null, null);
					selectedController = bController;
				} else if (clickedLink == lifecycleLink) {
					OverviewBusinessGroupLifecycleController lifecycleCtrl = updateSearch(ureq);
					lifecycleCtrl.activate(ureq, null, null);
					selectedController = lifecycleCtrl;
				}
				addToHistory(ureq, selectedController);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(myGroupsCtrl);
		removeAsListenerAndDispose(lifecycleCtrl);
		myGroupsCtrl = null;
		lifecycleCtrl = null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(currentCtrl == null) {
				updateGroups(ureq).activate(ureq, null, null);
				segmentView.select(groupsLink);
			}
			addToHistory(ureq, currentCtrl);
		} else {
			ContextEntry entry = entries.get(0);
			String segment = entry.getOLATResourceable().getResourceableTypeName();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if("Groups".equals(segment) || "AllGroups".equals(segment) || "OwnedGroups".equals(segment)) {
				updateGroups(ureq).activate(ureq, subEntries, state);
				segmentView.select(groupsLink);
			} else if("Search".equals(segment) && lifecycleLink != null) {
				updateSearch(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(lifecycleLink);
			} else {//default all groups
				updateGroups(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(groupsLink);
			}
		}
	}
	
	private BusinessGroupListWrapperController updateGroups(UserRequest ureq) {
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Groups", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		myGroupsCtrl = new BusinessGroupListWrapperController(ureq, bwControl);
		listenTo(myGroupsCtrl);
		currentCtrl = myGroupsCtrl;

		mainVC.put("groupList", myGroupsCtrl.getInitialComponent());
		addToHistory(ureq, myGroupsCtrl);
		return myGroupsCtrl;
	}
	
	private OverviewBusinessGroupLifecycleController updateSearch(UserRequest ureq) {
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		lifecycleCtrl = new OverviewBusinessGroupLifecycleController(ureq, bwControl);
		listenTo(lifecycleCtrl);
		currentCtrl = lifecycleCtrl;

		mainVC.put("groupList", lifecycleCtrl.getInitialComponent());
		addToHistory(ureq, lifecycleCtrl);
		return lifecycleCtrl;
	}
}
