/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.generator.ui;

import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 21 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryPreviewListController extends AbstractPreviewListController {
	
	private final List<RepositoryEntryRef> restrictRepositoryEntries;
	private final boolean canEdit;

	public RepositoryEntryPreviewListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			 RepositoryEntry repositoryEntry, boolean canEdit) {
		super(ureq, wControl, stackPanel);
		this.restrictRepositoryEntries = List.of(repositoryEntry);
		this.canEdit = canEdit;
		
		initForm(ureq);
		initFilters();
		initFilterTabs(ureq);
		selectFilterTab(ureq, tabAll);
	}

	@Override
	protected List<OrganisationRef> getDataCollectionOrganisationRefs() {
		return null;
	}

	@Override
	protected List<OrganisationRef> getLearnResourceManagerOrganisationRefs() {
		return null;
	}

	@Override
	protected List<OrganisationRef> getGeneratorOrganisationRefs() {
		return null;
	}

	@Override
	protected Collection<? extends RepositoryEntryRef> getRestrictRepositoryEntries() {
		return restrictRepositoryEntries;
	}

	@Override
	protected Collection<? extends CurriculumElementRef> getRestrictCurriculumElements() {
		return List.of();
	}

	@Override
	protected boolean isFilterTabCourse() {
		return false;
	}

	@Override
	protected boolean isFilterTabCurriculumElement() {
		return false;
	}

	@Override
	protected boolean isFilterGenerator() {
		return false;
	}

	@Override
	protected String getEmptyTableHintKey() {
		return "preview.empty.table.hint.course";
	}

	@Override
	protected boolean canEdit() {
		return canEdit;
	}

}
