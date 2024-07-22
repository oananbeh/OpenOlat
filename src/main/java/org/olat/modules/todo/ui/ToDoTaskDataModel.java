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
package org.olat.modules.todo.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskDataModel extends DefaultFlexiTreeTableDataModel<ToDoTaskRow> implements SortableFlexiTableDataModel<ToDoTaskRow> {
	
	public static final ToDoTaskCols[] COLS = ToDoTaskCols.values();

	private final Locale locale;
	private final ToDoTaskGroupFactory groupFactory;
	private final Integer maxRows;
	private List<ToDoTaskRow> backups;
	private List<ToDoTaskRow> groupCandiatades;
	
	public ToDoTaskDataModel(FlexiTableColumnModel columnsModel, ToDoTaskGroupFactory groupFactory, Integer maxRows, Locale locale) {
		super(columnsModel);
		this.groupFactory = groupFactory;
		this.maxRows = maxRows;
		this.locale = locale;
	}
	
	public ToDoTaskRow getObjectByKey(Long key) {
		List<ToDoTaskRow> rows = getObjects();
		for (ToDoTaskRow row: rows) {
			if (row != null && row.getKey() != null && row.getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}
	
	public void setRows(List<ToDoTaskRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	public List<ToDoTaskRow> getBackups() {
		return backups;
	}

	public void setGroupCandiatades(List<ToDoTaskRow> groupCandiatades) {
		this.groupCandiatades = groupCandiatades;
	}

	@Override
	public void sort(SortKey orderBy) {
		// This sorts the backup rows
		List<ToDoTaskRow> rows = new ToDoTaskRowSortDelegate(orderBy, this, locale).sort();
		if (maxRows != null && rows.size() > maxRows.intValue()) {
			rows = rows.subList(0, maxRows.intValue()-1);
		}
		rows = groupFactory.groupRows(rows, groupCandiatades);
		super.setObjects(rows);
	}
	
	public void groupRows() {
		List<ToDoTaskRow> rows = groupFactory.groupRows(backups, groupCandiatades);
		super.setObjects(rows);
	}
	
	@Override
	public void setHasOpenCloseAll(boolean hasOpenCloseAll) {
		super.setHasOpenCloseAll(hasOpenCloseAll);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ToDoTaskRow note = getObject(row);
		return getValueAt(note, col);
	}
	
	@Override
	public boolean hasChildren(int row) {
		return getObject(row).hasChildren();
	}

	@Override
	public Object getValueAt(ToDoTaskRow row, int col) {
		switch(COLS[col]) {
		case id: return row.getKey();
		case creationDate: return row.getCreationDate();
		case contentLastModifiedDate: return row.getContentModifiedDate();
		case title: return row.getTitleItems();
		case status: return row;
		case priority: return row;
		case expenditureOfWork: return row.getFormattedExpenditureOfWork();
		case startDate: return row.getFormattedStartDate();
		case dueDate: return row;
		case due: return row;
		case doneDate: return row.getFormattedDoneDate();
		case contextType: return row.getTranslatedType();
		case contextTitle: return row.getGoToOriginLink();
		case contextSubTitle: return row.getGoToSubOriginLink();
		case assigned: return row.getAssigneesPortraits();
		case delegated: return row.getDelegateesPortraits();
		case tags: return row.getFormattedTags();
		case deletedDate: return row.getDeletedDate();
		case deletedBy: return row.getDeletedByName();
		case tools: return row.getToolsLink();
		default: return null;
		}
	}
	
	public enum ToDoTaskCols implements FlexiSortableColumnDef {
		id("task.id"),
		creationDate("task.creation.date"),
		contentLastModifiedDate("task.content.modified.date"),
		title("task.title"),
		status("task.status"),
		priority("task.priority"),
		expenditureOfWork("task.expenditure.of.work"),
		startDate("task.start.date"),
		dueDate("task.due.date"),
		due("task.due"),
		doneDate("task.done.date"),
		contextType("task.context.type"),
		contextTitle("task.context.title"),
		contextSubTitle("task.context.sub.title"),
		assigned("task.assigned"),
		delegated("task.delegated"),
		tags("tags"),
		deletedBy("deleted.by"),
		deletedDate("deleted.date"),
		tools("tools");
		
		private final String i18nKey;
		
		private ToDoTaskCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
		@Override
		public boolean sortable() {
			return this != assigned
					&& this != delegated
					&& this != tags
					&& this != tools;
		}

		@Override
		public String sortKey() {
			 return name();
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	public interface ToDoTaskGroupFactory {
		public List<ToDoTaskRow> groupRows(List<ToDoTaskRow> sortedRows, List<ToDoTaskRow> groupCandiatades);
	}
}
