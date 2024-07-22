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
package org.olat.core.commons.services.notifications.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;

/**
 * Initial date: Mär 23, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum NotificationSubscriptionCols implements FlexiSortableColumnDef {
	key("table.column.key"),
	section("table.column.section"),
	learningResource("table.column.learning.resource"),
	subRes("table.column.sub.res"),
	addDesc("table.column.add.desc"),
	statusToggle("table.column.status"),
	creationDate("table.column.creationDate"),
	lastEmail("table.column.lastEmail"),
	deleteLink("table.column.delete.action");

	private final String i18nKey;

	private NotificationSubscriptionCols(String i18nKey) {
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
