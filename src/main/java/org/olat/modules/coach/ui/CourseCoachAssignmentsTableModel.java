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
package org.olat.modules.coach.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 20 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseCoachAssignmentsTableModel extends DefaultFlexiTableDataModel<CourseCoachAssignmentRow>
implements SortableFlexiTableDataModel<CourseCoachAssignmentRow> {
	
	private static final CAssignmentsCol[] COLS = CAssignmentsCol.values();
	
	private final Locale locale;
	
	public CourseCoachAssignmentsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CourseCoachAssignmentRow> rows = new CourseCoachAssignmentsTableSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseCoachAssignmentRow assignmentRow = getObject(row);
		return getValueAt(assignmentRow, col);
	}

	@Override
	public Object getValueAt(CourseCoachAssignmentRow row, int col) {
		switch(COLS[col]) {
			case entryId: return row.getCourseEntry().getKey();
			case entry: return row.getCourseEntry().getDisplayname();
			case entryExternalRef: return row.getCourseEntry().getExternalRef();
			case entryExternalId: return row.getCourseEntry().getExternalId();
			case courseElement: return row;
			case openAssignments: return row.getNumOfAssignments();
			default: return "ERROR";
		}
	}
	
	public enum CAssignmentsCol implements FlexiSortableColumnDef {
		entryId("table.header.course.key"),
		entry("table.header.course.name"),
		entryExternalRef("table.header.course.externalRef"),
		entryExternalId("table.header.course.externalId"),
		courseElement("table.header.node"),
		openAssignments("table.header.open.assignments");
		
		private final String i18nKey;
		
		private CAssignmentsCol(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
