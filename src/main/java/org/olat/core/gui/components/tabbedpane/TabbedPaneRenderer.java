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

package org.olat.core.gui.components.tabbedpane;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentHelper;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem.TabIndentation;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * @author Felix Jost
 */
public class TabbedPaneRenderer extends DefaultComponentRenderer {

	private static final Logger log = Tracing.createLoggerFor(TabbedPaneRenderer.class);

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		TabbedPane tb = (TabbedPane)source;
		TabbedPaneItem tbi = tb.getTabbedPaneItem();
		
		final int cnt = tb.getTabCount();
		if (cnt == 0) return; // nothing to render
		
		int selPane = tb.getSelectedPane();
		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		 		
		sb.append("<div id=\"o_c").append(tb.getDispatchID()).append("\" class='o_tabbed_pane'>");
		if (tbi != null &&  TabIndentation.defaultFormLayout == tbi.getTabIndentation()) {
			sb.append("<div class='col-sm-offset-3'>");
		}
		sb.append("<ul role='tablist' class='nav nav-tabs");
		String css = tb.getElementCssClass();
		if (StringHelper.containsNonWhitespace(css)) {
			sb.append(" ").append(css);
		}
		sb.append("'>");
		for (int i = 0; i < cnt; i++) {
			if((tb.isHideDisabledTab() && !tb.isEnabled(i)) || !tb.isVisible(i)) {
				continue;
			}
			
			String tabName = tb.getDisplayNameAt(i);
			String elementCssClass = tb.getElementCssAt(i);
			// Render active tab as non clickable, passive tabs with link
			sb.append("<li role='presentation' class='");
			if(elementCssClass != null) {
				sb.append(elementCssClass);
			}
			if (i != selPane && cnt > 1) {
				if (tb.isEnabled(i)) {
					sb.append("'><a role='tab' aria-selected='false' aria-controls='o_c").append(tb.getDispatchID()).append("_c' ");
					if(tbi == null) {
						ubu.buildHrefAndOnclick(sb, null, iframePostEnabled, tb.isDirtyCheck(), true, new NameValuePair(TabbedPane.PARAM_PANE_ID, String.valueOf(i)));
					} else {
						String dispatchId = tbi.getFormDispatchId();
						sb.append("href=\"javascript:;\" onclick=\"")
						  .append(FormJSHelper.getXHRFnCallFor(tbi.getRootForm(), dispatchId, 1, false, true, true,
								  new NameValuePair(TabbedPane.PARAM_PANE_ID, String.valueOf(i))))
						  .append(";\" ");
					}
					sb.append(">");				
				} else {
					// disabled panels can not be clicked, but for layout reason needs still a a href
					sb.append(" disabled'><a href='javascript:;' title='")
					  .append(StringHelper.escapeHtml(tb.getCompTrans().translate("disabled"))).append("'>");
				}
			} else {
				sb.append(" active' ><a role='tab' aria-selected='true' aria-controls='o_c").append(tb.getDispatchID()).append("_c' id='o_c").append(tb.getDispatchID()).append("_a' href='javascript:;'>");
			}
			sb.append(tabName).append("</a></li>");
		}
		sb.append("</ul>");
		if (tbi != null &&  TabIndentation.defaultFormLayout == tbi.getTabIndentation()) {
			sb.append("</div>");
		}

		// now let the selected component render itself
		Component paneToRender = tb.getTabAt(selPane);
		sb.append("<div role='tabpanel' tabindex='-1' class='o_tabbed_pane_content' id='o_c").append(tb.getDispatchID()).append("_c' aria-labelledby='o_c").append(tb.getDispatchID()).append("_a'>");
		if (paneToRender != null) {
			renderer.render(sb, paneToRender, null);
		}
		sb.append("</div></div>");
		
		if (tb.isPanelFocus()) {
			sb.append("<script>try {document.getElementById('o_c").append(tb.getDispatchID()).append("_c').focus({preventScroll:true});} catch(e){if(console){console.log(e);}};</script>");
		}
		
		try {
			for(int i=0; i<cnt; i++) {
				if(i != selPane) {
					ComponentHelper.setDirtyFalseRecursive(tb.getTabAt(i));
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
