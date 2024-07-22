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
package org.olat.course.archiver.wizard;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 19 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveElementTableModel extends DefaultFlexiTreeTableDataModel<CourseArchiveElementRow> {

	private static final ElementsCols[] COLS = ElementsCols.values();
	
	private final Translator translator;
	
	public CourseArchiveElementTableModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseArchiveElementRow elementRow = getObject(row);
		switch(COLS[col]) {
			case node: return elementRow;
			case type: return getTranslatedType(elementRow.getType());
			case longTitle: return elementRow.getLongTitle();
			default: return "ERROR";
		}
	}
	
	private String getTranslatedType(String type) {
		return translator.translate("title_".concat(type));
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		CourseArchiveElementRow viewRow = getObject(row);
		return viewRow.hasChildren();
	}
	
	@Override
	public boolean isSelectable(int row) {
		CourseArchiveElementRow elementRow = getObject(row);
		return elementRow != null && CourseArchiveContext.acceptCourseElement(elementRow.getCourseNode());
	}

	public enum ElementsCols implements FlexiSortableColumnDef {
		node("table.header.node"),
		type("table.header.type"),
		longTitle("table.header.long.title");
		
		private final String i18nKey;
		
		private ElementsCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
