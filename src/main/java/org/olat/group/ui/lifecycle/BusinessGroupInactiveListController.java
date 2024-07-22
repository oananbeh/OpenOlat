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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.group.model.BusinessGroupQueryParams.LifecycleSyntheticStatus;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;

/**
 * 
 * Initial date: 14 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupInactiveListController extends AbstractBusinessGroupLifecycleListController {

	private FlexiFiltersTab inactiveTab;
	private FlexiFiltersTab inactiveLongTab;
	private FlexiFiltersTab responseDelayTab;
	private FlexiFiltersTab toSoftDeleteTab;
	
	private DefaultFlexiColumnModel actionColumn;
	
	public BusinessGroupInactiveListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
		super(ureq, wControl, prefsKey);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String tabName = entry.getOLATResourceable().getResourceableTypeName();
			if(tableEl.getSelectedFilterTab() == null || !tableEl.getSelectedFilterTab().getId().equals(tabName)) {
				FlexiFiltersTab tab = tableEl.getFilterTabById(tabName);
				if(tab != null) {
					selectFilterTab(ureq, tab);
				} else {
					selectFilterTab(ureq, inactiveTab);
				}
			} else {
				tableEl.addToHistory(ureq);
			}
		} else if(tableEl.getSelectedFilterTab() == null) {
			selectFilterTab(ureq, inactiveTab);
		}
	}
	
	@Override
	protected void initStatusColumnModel(FlexiTableColumnModel columnsModel) {
		super.initStatusColumnModel(columnsModel);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.inactivationDate, new DateFlexiCellRenderer(getLocale())));
		
		DefaultFlexiColumnModel plannedCol = new DefaultFlexiColumnModel(Cols.plannedSoftDeleteDate, new DateFlexiCellRenderer(getLocale()));
		plannedCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(plannedCol);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.method,
				new BusinessGroupLifecycleMethodRenderer(getTranslator(),
						groupModule.isAutomaticGroupSoftDeleteEnabled(),
						groupModule.getNumberOfDayBeforeSoftDeleteMail() > 0)));
	}

	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = super.initColumnModel();
		boolean withMail = groupModule.getNumberOfDayBeforeDeactivationMail() > 0;
		String action = withMail ? TABLE_ACTION_START_SOFT_DELETE : TABLE_ACTION_SOFT_DELETE;
		String i18nKey = withMail ? "table.header.start.soft.delete" : "table.header.soft.delete";
		actionColumn = new DefaultFlexiColumnModel(i18nKey, translate(i18nKey), action);
		columnsModel.addFlexiColumnModel(actionColumn);
		return columnsModel;
	}
	
	@Override
	protected void initFilterTabs(List<FlexiFiltersTab> tabs) {
		boolean automatic = groupModule.isAutomaticGroupSoftDeleteEnabled();
		boolean withMail = groupModule.getNumberOfDayBeforeSoftDeleteMail() > 0;
		
		// auto + mail:      active, long, responseDelay
		// auto + no mail:   active, long
		// manual + mail:    active, long, to soft delete, response delay
		// manual + no mail: active, long, to soft delete
		
		inactiveTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Inactive", translate("admin.groups.inactive.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.INACTIVE.name())));
		tabs.add(inactiveTab);
		
		inactiveLongTab = FlexiFiltersTabFactory.tabWithImplicitFilters("LongInactive", translate("admin.groups.inactive.long.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.INACTIVE_LONG.name())));
		tabs.add(inactiveLongTab);
		
		if(!automatic) {
			String action = withMail ? LifecycleSyntheticStatus.TO_START_SOFT_DELETE.name() : LifecycleSyntheticStatus.TO_SOFT_DELETE.name();
			toSoftDeleteTab = FlexiFiltersTabFactory.tabWithImplicitFilters("ToSoftDelete", translate("admin.groups.to.soft.delete.preset"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, action)));
			tabs.add(toSoftDeleteTab);
		}
		
		if(withMail) {
			responseDelayTab= FlexiFiltersTabFactory.tabWithImplicitFilters("SoftDeleteDelay", translate("admin.groups.inactive.response.delay.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.INACTIVE_RESPONSE_DELAY.name())));
			tabs.add(responseDelayTab);
		}
	}
	
	@Override
	protected void changeFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		boolean actionVisible = (tab == toSoftDeleteTab);
		actionColumn.setAlwaysVisible(actionVisible);
		tableEl.setColumnModelVisible(actionColumn, actionVisible);
		if(startSoftDeleteButton != null) {
			startSoftDeleteButton.setVisible(tab != responseDelayTab);
		}
	}
	
	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		super.initButtons(formLayout, ureq);

		boolean withMail = groupModule.getNumberOfDayBeforeSoftDeleteMail() > 0;
		if(withMail && !groupModule.isAutomaticGroupSoftDeleteEnabled()) {
			startSoftDeleteButton = uifactory.addFormLink("table.start.soft.delete", TABLE_ACTION_START_SOFT_DELETE, "table.start.soft.delete", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(startSoftDeleteButton);
		}
		
		softDeleteButton = uifactory.addFormLink("table.soft.delete", TABLE_ACTION_SOFT_DELETE, "table.soft.delete", null, formLayout, Link.BUTTON);
		tableEl.addBatchButton(softDeleteButton);
		
		if(withMail && !groupModule.isAutomaticGroupSoftDeleteEnabled()) {
			reactivateButton = uifactory.addFormLink("table.reactivate", TABLE_ACTION_REACTIVATE, "table.reactivate", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(reactivateButton);
		}
	}
	
	@Override
	protected boolean isAutomaticMethod() {
		return businessGroupModule.isAutomaticGroupSoftDeleteEnabled();
	}
	
}
