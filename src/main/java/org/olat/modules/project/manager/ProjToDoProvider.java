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
package org.olat.modules.project.manager;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjToDoDetailController;
import org.olat.modules.project.ui.ProjToDoEditController;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.olat.modules.todo.ToDoContextFilter;
import org.olat.modules.todo.ToDoMailRule;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjToDoProvider implements ToDoProvider, ToDoContextFilter {

	public static final String TYPE = "project";
	
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;

	@Override
	public String getType() {
		return TYPE;
	}
	@Override
	public boolean isEnabled() {
		return projectModule.isEnabled();
	}

	@Override
	public String getBusinessPath(ToDoTask toDoTask) {
		ProjToDo toDo = projectService.getToDo(toDoTask.getOriginSubPath());
		if (toDo == null) {
			return null;
		}
		ProjProject project = toDo.getArtefact().getProject();
		return ProjectBCFactory.createFactory(project).getBusinessPath(project, ProjToDo.TYPE, toDo.getKey());
	}

	@Override
	public String getDisplayName(Locale locale) {
		return Util.createPackageTranslator(ProjectUIFactory.class, locale).translate("todo.type");
	}
	
	@Override
	public String getContextFilterType() {
		return TYPE;
	}

	@Override
	public int getFilterSortOrder() {
		return 300;
	}
	
	@Override
	public String getModifiedBy(Locale locale, ToDoTask toDoTask) {
		return null;
	}

	@Override
	public ToDoMailRule getToDoMailRule(ToDoTask toDoTask) {
		ProjProject project = projectService.getProject(() -> toDoTask.getOriginId());
		if (project == null || project.getStatus() == ProjectStatus.deleted || project.isTemplatePrivate() || project.isTemplatePublic()) {
			return ToDoMailRule.NO_EMAILS;
		}
		return ToDoProvider.super.getToDoMailRule(toDoTask);
	}
	
	@Override
	public void upateStatus(Identity doer, ToDoTaskRef toDoTask, Long originId, String originSubPath, ToDoStatus status) {
		projectService.updateToDoStatus(doer, originSubPath, status);
	}

	@Override
	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId, String originSubPath) {
		ProjProject project = projectService.getProject(() -> originId);
		if (project == null || project.getStatus() == ProjectStatus.deleted) {
			return null;
		}
		
		return new ProjToDoEditController(ureq, wControl, ProjectBCFactory.createFactory(project), project, false);
	}
	
	@Override
	public boolean isCopyable() {
		return true;
	}
	
	@Override
	public boolean isRestorable() {
		return false;
	}

	@Override
	public Controller createCopyController(UserRequest ureq, WindowControl wControl, Identity doer,
			ToDoTask sourceToDoTask, boolean showContext) {
		return createEditCopyController(ureq, wControl, sourceToDoTask, true, showContext);
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask, boolean showContext, boolean showSingleAssignee) {
		return createEditCopyController(ureq, wControl, toDoTask, false, showContext);
	}
	
	private Controller createEditCopyController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			boolean toDoTaskIsCopySource, boolean showContext) {
		ProjToDo toDo = projectService.getToDo(toDoTask.getOriginSubPath());
		ProjectBCFactory bcFactory = ProjectBCFactory.createFactory(toDo.getArtefact().getProject());
		return new ProjToDoEditController(ureq, wControl, bcFactory, toDo, toDoTaskIsCopySource, false, showContext);
	}

	@Override
	public FormBasicController createDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity creator,
			Identity modifier, Set<Identity> assignees, Set<Identity> delegatees) {
		ProjToDo toDo = projectService.getToDo(toDoTask.getOriginSubPath());
		ProjectBCFactory bcFactory = ProjectBCFactory.createFactory(toDo.getArtefact().getProject());
		return new ProjToDoDetailController(ureq, wControl, mainForm, bcFactory, toDo, secCallback, toDoTask, tags, creator, modifier, assignees, delegatees);
	}

	@Override
	public void deleteToDoTaskSoftly(Identity doer, ToDoTask toDoTask) {
		ProjToDo toDo = projectService.getToDo(toDoTask.getOriginSubPath());
		projectService.deleteToDoSoftly(doer, toDo);
	}

	@Override
	public Controller createDeleteConfirmationController(UserRequest ureq, WindowControl wControl, Locale locale, ToDoTask toDoTask) {
		Translator translator = Util.createPackageTranslator(ProjectUIFactory.class, locale);
		return new ConfirmationController(ureq, wControl,
				translator.translate("todo.delete.confirmation.message", StringHelper.escapeHtml(ToDoUIFactory.getDisplayName(translator, toDoTask))),
				translator.translate("todo.delete.confirmation.confirm"),
				translator.translate("todo.delete.confirmation.button"), true);
	}
	
}
