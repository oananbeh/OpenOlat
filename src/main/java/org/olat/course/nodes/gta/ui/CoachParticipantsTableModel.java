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
package org.olat.course.nodes.gta.ui;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.ui.component.SubmissionDateCellRenderer;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CoachParticipantsTableModel extends DefaultFlexiTableDataModel<CoachedIdentityRow> implements SortableFlexiTableDataModel<CoachedIdentityRow> {
	
	private static final CGCols[] COLS = CGCols.values();
	
	private final Locale locale;
	
	public CoachParticipantsTableModel(Locale locale, FlexiTableColumnModel columnModel) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CoachedIdentityRow> views = new CoachParticipantsModelSort(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	public int indexOf(IdentityRef identity) {
		if(identity == null) return -1;
		
		for(int i=getRowCount(); i-->0; ) {
			CoachedIdentityRow row = getObject(i);
			if(row.getIdentityKey().equals(identity.getKey())) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CoachedIdentityRow participant = getObject(row);
		return getValueAt(participant, col);
	}
	
	@Override
	public Object getValueAt(CoachedIdentityRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case mark: return row.getMarkLink();
				case taskStatus: return row.getTaskStatus();
				case taskName:
					if (row.getOpenTaskFileLink() == null && row.getDownloadTaskFileLink() == null) {
						return row.getTaskName();
					} else if (row.getOpenTaskFileLink() == null) {
						return row.getDownloadTaskFileLink();
					} else {
						return row.getOpenTaskFileLink();
					}
				case taskTitle:
					String title = row.getTaskTitle();
					if(!StringHelper.containsNonWhitespace(title)) {
						title = row.getTaskName();
					}
					return title;
				case submissionDate:
					return SubmissionDateCellRenderer.cascading(row);
				case userVisibility: return row.getUserVisibility();
				case score: return row.getScore();
				case passed: return row.getPassed();
				case numOfSubmissionDocs:
					if(row.getCollectionDate() != null) {
						return row.getNumOfCollectedDocs();
					}
					return row.getNumOfSubmissionDocs();
				case assessmentStatus: return row.getAssessmentStatus();
				case evaluationForm: return row.getEvaluationFormSessionStatus();
				case coachAssignment: return row.getCoachFullName();
				case tools: return row.getToolsLink();
				default: return "ERROR";	
			}
		} else if(col >= GTACoachedGroupGradingController.USER_PROPS_OFFSET) {
			int propIndex = col - GTACoachedGroupGradingController.USER_PROPS_OFFSET;
			return row.getIdentityProp(propIndex);
		}
		return "ERROR";
	}
	
	public enum CGCols implements FlexiSortableColumnDef {
		mark("table.header.mark"),
		taskTitle("table.header.group.taskTitle"),
		taskName("table.header.group.taskName"),
		taskStatus("table.header.group.step"),
		submissionDate("table.header.submissionDate"),
		userVisibility("table.header.userVisibility"),
		score("table.header.score"),
		passed("table.header.passed"),
		numOfSubmissionDocs("table.header.num.submissionDocs"),
		assessmentStatus("table.header.assessmentStatus"),
		coachAssignment("table.header.coach.assignment"),
		tools("table.header.tools"),
		evaluationForm("table.header.evaluation.form");
		
		private final String i18nKey;
		
		private CGCols(String i18nKey) {
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
