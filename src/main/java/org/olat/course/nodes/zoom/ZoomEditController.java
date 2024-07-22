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
package org.olat.course.nodes.zoom;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.ZoomCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ui.ZoomUpdateConfigController;
import org.olat.repository.RepositoryEntry;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomEditController extends ActivateableTabbableDefaultController {

    private TabbedPane tabbedPane;

    private final ZoomUpdateConfigController zoomConfig;

    private final ModuleConfiguration config;

    public ZoomEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, String courseNodeIdent, ModuleConfiguration config) {
        super(ureq, wControl);
        this.config = config;
        zoomConfig = new ZoomUpdateConfigController(ureq, wControl, courseEntry, courseNodeIdent, null, ZoomManager.ApplicationType.courseElement);
        listenTo(zoomConfig);
    }

    @Override
    public String[] getPaneKeys() {
        return new String[0];
    }

    @Override
    public TabbedPane getTabbedPane() {
        return tabbedPane;
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == zoomConfig) {
            if (event == Event.CANCELLED_EVENT) {
                //
            } else if (event == Event.DONE_EVENT) {
                config.setStringValue(ZoomCourseNode.CLIENT_ID, zoomConfig.getClientId());
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        }
    }

    @Override
    public void addTabs(TabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        tabbedPane.addTab(translate("pane.tab.zoomConfig"), "o_sel_zoom_options", zoomConfig.getInitialComponent());
    }
}
