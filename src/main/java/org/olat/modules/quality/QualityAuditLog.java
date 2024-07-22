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
package org.olat.modules.quality;

import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.modules.todo.ToDoTask;

/**
 * Initial date: 17 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public interface QualityAuditLog extends CreateInfo {
	
	Long getKey();

	Action getAction();

	String getBefore();

	String getAfter();
	
	Identity getDoer();

	QualityDataCollection getDataCollection();

	ToDoTask getToDoTask();
	
	Identity getIdentity();

	enum Action {
		toDoCreate,
		toDoContentUpdate,
		toDoStatusUpdate,
		toDoTagsUpdate,
		toDoMemberAdd,
		toDoMemberRemove,
		toDoRolesUpdate;
	}
	
	public static final List<Action> TODO_ACTIONS = List.of(
			Action.toDoCreate,
			Action.toDoContentUpdate,
			Action.toDoStatusUpdate,
			Action.toDoTagsUpdate,
			Action.toDoMemberAdd,
			Action.toDoMemberRemove,
			Action.toDoRolesUpdate);
	
}
