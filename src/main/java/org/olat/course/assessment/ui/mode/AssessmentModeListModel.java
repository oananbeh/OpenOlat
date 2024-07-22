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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.model.EnhancedStatus;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.ui.author.copy.wizard.additional.AssessmentModeCopyInfos;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentModeListModel extends DefaultFlexiTableDataModel<AssessmentMode> implements SortableFlexiTableDataModel<AssessmentMode> {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentModeListModel.class);
	private static final Cols[] COLS = Cols.values();
	
	private final Translator translator;
	private final AssessmentModeCoordinationService coordinationService;
	
	private Map<AssessmentMode, AssessmentModeCopyInfos> copyInfos = new HashMap<>();
	
	public AssessmentModeListModel(FlexiTableColumnModel columnsModel, Translator translator,
			AssessmentModeCoordinationService coordinationService) {
		super(columnsModel);
		this.translator = translator;
		this.coordinationService = coordinationService;
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentMode mode = getObject(row);
		return getValueAt(mode, col);
	}
		
	@Override
	public Object getValueAt(AssessmentMode mode, int col) {
		return switch (COLS[col]) {
			case status -> getStatus(mode);
			case course -> mode.getRepositoryEntry().getDisplayname();
			case externalId -> mode.getRepositoryEntry().getExternalId();
			case externalRef -> mode.getRepositoryEntry().getExternalRef();
			case name -> mode.getName();
			case begin -> mode.getBegin();
			case end -> mode.getEnd();
			case mode ->
					mode.isManualBeginEnd() ? translator.translate("mode.beginend.manual") : translator.translate("mode.beginend.automatic");
			case leadTime -> mode.getLeadTime();
			case followupTime -> mode.getFollowupTime();
			case target -> mode.getTargetAudience();
			case start -> canStart(mode);
			case stop -> canStop(mode);
			case beginChooser, endChooser -> getDateChooser(mode, col);
			case configSeb -> Boolean.valueOf(hasSafeExamBrowserConfiguration(mode));
			default -> "ERROR";
		};
	}
	
	private boolean hasSafeExamBrowserConfiguration(AssessmentMode mode) {
		return mode.isSafeExamBrowser() && StringHelper.containsNonWhitespace(mode.getSafeExamBrowserConfigPList());
	}
	
	private DateChooser getDateChooser(AssessmentMode mode, int col) {
		if (copyInfos.containsKey(mode)) {
			if (col == Cols.beginChooser.ordinal()) {
				return copyInfos.get(mode).getBeginDateChooser();
			} else if (col == Cols.endChooser.ordinal()) {
				return copyInfos.get(mode).getEndDateChooser();
			}
		}
		
		return null;
	}
	
	public void setCopyInfos(Map<AssessmentMode, AssessmentModeCopyInfos> copyInfos) {
		this.copyInfos = copyInfos;
	}
	
	public Map<AssessmentMode, AssessmentModeCopyInfos> getCopyInfos() {
		return copyInfos;
	}
	
	private boolean canStart(AssessmentMode mode) {
		boolean canStart = mode.isManualBeginEnd();
		if(canStart) {
			canStart = coordinationService.canStart(mode);
		}
		// second condition: Show button only if Begin <= today
		return canStart && mode.getBeginWithLeadTime().before(DateUtils.getEndOfDay(new Date()));
	}
	
	private boolean canStop(AssessmentMode mode) {
		boolean canStop = mode.isManualBeginEnd();
		if(canStop) {
			canStop = coordinationService.canStop(mode);
		}
		return canStop;
	}
	
	private EnhancedStatus getStatus(AssessmentMode mode) {
		List<String> warnings = null;
		try {
			if(StringHelper.containsNonWhitespace(mode.getStartElement())) {
				ICourse course = CourseFactory.loadCourse(mode.getRepositoryEntry());
				CourseNode node = course.getRunStructure().getNode(mode.getStartElement());
				if(node == null) {
					warnings = new ArrayList<>(2);
					warnings.add(translator.translate("warning.missing.start.element"));
				}
			}
			if(StringHelper.containsNonWhitespace(mode.getElementList())) {
				ICourse course = CourseFactory.loadCourse(mode.getRepositoryEntry());
				String elements = mode.getElementList();
				for(String element:elements.split(",")) {
					CourseNode node = course.getRunStructure().getNode(element);
					if(node == null) {
						if(warnings == null) {
							warnings = new ArrayList<>(2);
						}
						warnings.add(translator.translate("warning.missing.element"));
						break;
					}
				}
			}
		} catch (CorruptedCourseException e) {
			log.error("", e);
			if(warnings == null) {
				warnings = new ArrayList<>(2);
			}
			warnings.add(translator.translate("cif.error.corrupted"));
		}
		return new EnhancedStatus(mode.getStatus(), mode.getEndStatus(), warnings);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AssessmentMode> views = new AssessmentModeListModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	public boolean updateModeStatus(TransientAssessmentMode modeToUpdate) {
		boolean updated = false;
		
		List<AssessmentMode> modes = getObjects();
		for(AssessmentMode mode:modes) {
			if(mode.getKey().equals(modeToUpdate.getModeKey())) {
				if(mode.getStatus() != modeToUpdate.getStatus()) {
					mode.setStatus(modeToUpdate.getStatus());
					updated = true;
				}
			}
		}
		
		return updated;
	}
	
	public enum Cols implements FlexiSortableColumnDef {
		status("table.header.status"),
		course("table.header.course"),
		externalId("table.header.externalId"),
		externalRef("table.header.externalRef"),
		name("table.header.name"),
		begin("table.header.begin"),
		end("table.header.end"),
		mode("mode.beginend"),
		leadTime("table.header.leadTime"),
		followupTime("table.header.followupTime"),
		target("table.header.target"),
		start(""),
		stop(""),
		beginChooser("table.header.begin"),
		endChooser("table.header.end"),
		configSeb("table.header.config.seb"),
		toolsLink("table.header.actions");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
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
