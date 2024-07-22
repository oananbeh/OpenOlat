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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NamedFileMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupService;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21DeleteDataController extends FormBasicController {

	private final String[] onKeys = new String[]{ "on" };

	private MultipleSelectionElement acknowledgeEl;
	
	private final ArchiveOptions options;
	private final List<Identity> identities;
	
	private QTICourseNode courseNode;
	private CourseEnvironment courseEnv;
	private RepositoryEntry assessedEntry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public QTI21DeleteDataController(UserRequest ureq, WindowControl wControl, 
			CourseEnvironment courseEnv, AssessmentToolOptions asOptions, QTICourseNode courseNode) {
		super(ureq, wControl, "confirm_reset_data");
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		
		options = new ArchiveOptions();
		if(asOptions.getGroup() == null && asOptions.getIdentities() == null) {
			identities = ScoreAccountingHelper.loadUsers(courseEnv);
			options.setIdentities(identities);
		} else if (asOptions.getIdentities() != null) {
			identities = asOptions.getIdentities();
			options.setIdentities(identities);
		} else {
			identities = businessGroupService.getMembers(asOptions.getGroup());
			options.setGroup(asOptions.getGroup());
		}
		
		initForm(ureq);
	}
	
	public QTI21DeleteDataController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			IQTESTCourseNode courseNode, Identity assessedIdentity) {
		super(ureq, wControl, "confirm_reset_data");
		this.courseNode = courseNode;
		courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
		
		options = new ArchiveOptions();
		identities = Collections.singletonList(assessedIdentity);
		options.setIdentities(identities);
		initForm(ureq);
	}
	
	public QTI21DeleteDataController(UserRequest ureq, WindowControl wControl, 
			RepositoryEntry assessedEntry, AssessmentToolOptions asOptions) {
		super(ureq, wControl, "confirm_reset_data");
		this.assessedEntry = assessedEntry;
		
		options = new ArchiveOptions();
		if(asOptions.getGroup() == null && asOptions.getIdentities() == null) {
			identities = repositoryService.getMembers(assessedEntry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.participant.name());
			options.setIdentities(identities);
		} else if (asOptions.getIdentities() != null) {
			identities = asOptions.getIdentities();
			options.setIdentities(identities);
		} else {
			identities = businessGroupService.getMembers(asOptions.getGroup());
			options.setGroup(asOptions.getGroup());
		}

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean canDelete = !identities.isEmpty();
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String msg;
			if(canDelete) {
				msg = translate("delete.all.data.text", Integer.toString(identities.size()));
			} else {
				msg = translate("delete.nothing.data.text", Integer.toString(identities.size()));
			}
			layoutCont.contextPut("msg", msg);
		}
		
		FormLayoutContainer confirmCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
		formLayout.add("confirm", confirmCont);
		confirmCont.setRootForm(mainForm);
		
		String[] onValues = new String[]{ translate("delete.all.data.confirmation") };
		acknowledgeEl = uifactory.addCheckboxesHorizontal("acknowledge", "confirmation", confirmCont, onKeys, onValues);
		acknowledgeEl.setVisible(canDelete);
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, confirmCont);
		FormSubmit deleteButton = uifactory.addFormSubmitButton("delete.all.data", buttonsCont);
		deleteButton.setElementCssClass("btn-danger");
		deleteButton.setVisible(canDelete);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acknowledgeEl.clearError();
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		File archiveFile = null;
		if(courseNode instanceof IQTESTCourseNode) {
			RepositoryEntry testEntry = courseNode.getReferencedRepositoryEntry();
			RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
			
			ICourse course = CourseFactory.loadCourse(courseEntry);
			archiveFile = archiveData(course, options);
			
			qtiService.deleteAssessmentTestSession(identities, testEntry, courseEntry, courseNode.getIdent());
			for(Identity identity:identities) {
				ScoreEvaluation scoreEval = new ScoreEvaluation(null, null, null, null, null, null, null,
						AssessmentEntryStatus.notStarted, null, null, 0.0d, AssessmentRunStatus.notStarted, null);
				IdentityEnvironment ienv = new IdentityEnvironment(identity, Roles.userRoles());
				UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
				courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, uce, getIdentity(), false,
						Role.coach);
				courseAssessmentService.updateCurrentCompletion(courseNode, uce, null, null, AssessmentRunStatus.notStarted,
						Role.coach);
				dbInstance.commitAndCloseSession();
			}
		} else if(assessedEntry != null) {
			archiveFile = archiveData(assessedEntry);
			qtiService.deleteAssessmentTestSession(identities, assessedEntry, null, null);
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		if(archiveFile != null) {
			MediaResource archiveResource = new NamedFileMediaResource(archiveFile, archiveFile.getName(), archiveFile.getName(), false);
			Command downloadCmd = CommandFactory.createDownloadMediaResource(ureq, archiveResource);
			getWindowControl().getWindowBackOffice().sendCommandTo(downloadCmd);
		}
	}
	
	private File archiveData(ICourse course, ArchiveOptions archiveOptions) {
		File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), course.getCourseTitle());
		String archiveName = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".zip";

		File exportFile = new File(exportDirectory, archiveName);
		try(FileOutputStream fileStream = new FileOutputStream(exportFile);
			ZipOutputStream exportStream = new ZipOutputStream(fileStream)) {
			courseNode.archiveNodeData(getLocale(), course, archiveOptions, exportStream, "", "UTF-8");
		} catch (IOException e) {
			logError("", e);
		}
		return exportFile;
	}
	
	private File archiveData(RepositoryEntry testEntry) {
		//backup
		String archiveName = "qti21test_"
				+ StringHelper.transformDisplayNameToFileSystemName(testEntry.getDisplayname())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".zip";
		Path exportPath = Paths.get(FolderConfig.getCanonicalRoot(), FolderConfig.getUserHomes(), getIdentity().getName(),
				"private", "archive", StringHelper.transformDisplayNameToFileSystemName(testEntry.getDisplayname()), archiveName);
		File exportFile = exportPath.toFile();
		exportFile.getParentFile().mkdirs();
		
		try(FileOutputStream fileStream = new FileOutputStream(exportFile);
			ZipOutputStream exportStream = new ZipOutputStream(fileStream)) {
			//author can do this, also they can archive all users and anonyme users
			QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(testEntry, null, null, true, true, true, false);
			new QTI21ArchiveFormat(getLocale(), searchParams).exportResource(exportStream);
		} catch (IOException e) {
			logError("", e);
		}
		return exportFile;
	}
}
