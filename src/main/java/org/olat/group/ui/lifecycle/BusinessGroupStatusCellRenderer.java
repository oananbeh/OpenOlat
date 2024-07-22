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
package org.olat.group.ui.lifecycle;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupStatusEnum;

/**
 * 
 * Initial date: 21 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public BusinessGroupStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof BusinessGroupStatusEnum) {
			render(target, (BusinessGroupStatusEnum)cellValue);
		} else if(cellValue instanceof BusinessGroup) {
			BusinessGroup businessGroup = (BusinessGroup)cellValue;
			render(target, businessGroup.getGroupStatus());
		}
	}
	
	public void render(StringOutput sb, BusinessGroupStatusEnum status) {
		sb.append("<span class='o_labeled_light o_businessgroup_status_").append(status.name())
		  .append("' title=\"").append(StringHelper.escapeHtml(translator.translate("status." + status.name() + ".desc"))).append("\">")
		  .append("<i class='o_icon o_icon-fw o_icon_businessgroup_status_").append(status.name()).append("'> </i> <span>")
		  .append(translator.translate("table.status.".concat(status.name())))
		  .append("</span></span>");
	}

}
