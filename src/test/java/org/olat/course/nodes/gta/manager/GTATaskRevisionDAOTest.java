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
package org.olat.course.nodes.gta.manager;

import static org.olat.test.JunitTestHelper.random;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.model.TaskRevisionImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTATaskRevisionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private GTATaskRevisionDAO taskRevisionDao;
	
	@Test
	public void createTaskRevision() {//prepare
		// course with task course element
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-1");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-2");
		
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		// participant select a task
		File taskFile = new File("task.txt");
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, taskFile);
		Task task = response.getTask();
		Assert.assertNotNull(task);
		
		// coach create the revision
		TaskRevisionImpl rev = (TaskRevisionImpl)taskRevisionDao.createTaskRevision(task, TaskProcess.revision.name(), 1, "Hello world", coach, new Date());
		dbInstance.commit();
		
		// check
		Assert.assertNotNull(rev);
		Assert.assertNotNull(rev.getKey());
		Assert.assertNotNull(rev.getCreationDate());
		Assert.assertNotNull(rev.getLastModified());
		Assert.assertEquals(task, rev.getTask());
		Assert.assertEquals(TaskProcess.revision, rev.getTaskStatus());
		Assert.assertEquals(1, rev.getRevisionLoop());
		Assert.assertEquals("Hello world", rev.getComment());
		Assert.assertEquals(coach, rev.getCommentAuthor());
		Assert.assertNotNull(rev.getCommentLastModified());
	}
	
	@Test
	public void createTaskRevisionNullDate() {
		// course with task course element
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-1nd");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-2nd");
		
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		// participant select a task
		File taskFile = new File("task.txt");
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, taskFile);
		Task task = response.getTask();
		Assert.assertNotNull(task);
		
		// coach create the revision
		TaskRevisionImpl rev = (TaskRevisionImpl)taskRevisionDao.createTaskRevision(task, TaskProcess.revision.name(), 1, "Hello null data", coach, null);
		dbInstance.commit();
		
		// check
		Assert.assertNotNull(rev);
		Assert.assertNotNull(rev.getKey());
		Assert.assertNull(rev.getDate());
		Assert.assertEquals(task, rev.getTask());
	}
	
	@Test
	public void getTaskRevision() {
		// course with task course element
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-3");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-4");
		
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		// participant select a task
		File taskFile = new File("tasked.txt");
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, taskFile);
		Task task = response.getTask();
		Assert.assertNotNull(task);
		
		// coach create the revision
		TaskRevision rev = taskRevisionDao.createTaskRevision(task, TaskProcess.correction.name(), 0, "More work needed", coach, new Date());
		dbInstance.commit();
		
		// load the revision
		TaskRevisionImpl loadedRev = (TaskRevisionImpl)taskRevisionDao.getTaskRevision(task, TaskProcess.correction.name(), 0);
		// check
		Assert.assertNotNull(loadedRev);
		Assert.assertEquals(rev, loadedRev);
		Assert.assertNotNull(loadedRev.getKey());
		Assert.assertNotNull(loadedRev.getCreationDate());
		Assert.assertNotNull(loadedRev.getLastModified());
		Assert.assertEquals(task, loadedRev.getTask());
		Assert.assertEquals(TaskProcess.correction, loadedRev.getTaskStatus());
		Assert.assertEquals(0, loadedRev.getRevisionLoop());
		Assert.assertEquals("More work needed", loadedRev.getComment());
		Assert.assertEquals(coach, loadedRev.getCommentAuthor());
		Assert.assertNotNull(loadedRev.getCommentLastModified());
		
		// negative checks
		TaskRevisionImpl otherIterationRev = (TaskRevisionImpl)taskRevisionDao.getTaskRevision(task, TaskProcess.correction.name(), 1);
		Assert.assertNull(otherIterationRev);
		TaskRevisionImpl otherStatusRev = (TaskRevisionImpl)taskRevisionDao.getTaskRevision(task, TaskProcess.assignment.name(), 0);
		Assert.assertNull(otherStatusRev);
	}
	
	@Test
	public void getLatestTaskRevisions() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		Task task1 = gtaManager.selectTask(participant1, tasks, null, node, new File("task1.txt")).getTask();
		Task task2 = gtaManager.selectTask(participant2, tasks, null, node, new File("task2.txt")).getTask();
		gtaManager.selectTask(participant3, tasks, null, node, new File("task3.txt")).getTask();
		Task task4 = gtaManager.selectTask(participant4, tasks, null, node, new File("task4.txt")).getTask();
		
		// coach create the revision
		taskRevisionDao.createTaskRevision(task1, TaskProcess.correction.name(), 0, "More work needed", coach, new Date());
		taskRevisionDao.createTaskRevision(task1, TaskProcess.correction.name(), 1, "More work needed", coach, new Date());
		TaskRevision rev12 = taskRevisionDao.createTaskRevision(task1, TaskProcess.correction.name(), 2, "More work needed", coach, new Date());
		TaskRevision rev20 = taskRevisionDao.createTaskRevision(task2, TaskProcess.correction.name(), 0, "More work needed", coach, new Date());
		taskRevisionDao.createTaskRevision(task4, TaskProcess.correction.name(), 0, "More work needed", coach, new Date());
		dbInstance.commit();
		
		List<TaskRevision> revisions = taskRevisionDao.getLatestTaskRevisions(re, node, List.of(participant1, participant2, participant3));
		Assert.assertEquals(2, revisions.size());
		TaskRevision revision1 = revisions.stream().filter(revision -> revision.getTask().getIdentity().getKey().equals(participant1.getKey())).findFirst().get();
		Assert.assertEquals(revision1, rev12);
		TaskRevision revision2 = revisions.stream().filter(revision -> revision.getTask().getIdentity().getKey().equals(participant2.getKey())).findFirst().get();
		Assert.assertEquals(revision2, rev20);
	}
	
	@Test
	public void getTaskRevisions_task() {
		// course with task course element
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-5");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-6");
		
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		// participant select a task
		File taskFile = new File("tasked.txt");
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, taskFile);
		Task task = response.getTask();
		Assert.assertNotNull(task);
		
		// coach create the revision
		TaskRevision rev = taskRevisionDao.createTaskRevision(task, TaskProcess.revision.name(), 3, "Still to do", coach, new Date());
		dbInstance.commit();
		
		// load the revisions
		List<TaskRevision> revisions = taskRevisionDao.getTaskRevisions(task);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(1, revisions.size());
		Assert.assertEquals(rev, revisions.get(0));
	}
	
	@Test
	public void deleteTaskRevisionsByTaskList() {
		// course with task course element
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-7");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-8");
		
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		TaskList taskList = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		// participant select a task
		File taskFile = new File("tasked.txt");
		AssignmentResponse response = gtaManager.selectTask(participant, taskList, null, node, taskFile);
		Task task = response.getTask();
		Assert.assertNotNull(task);
		
		// coach create the revision
		TaskRevision rev = taskRevisionDao.createTaskRevision(task, TaskProcess.revision.name(), 3, "Still to do", coach, new Date());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rev);
		
		int rowDeleted = taskRevisionDao.deleteTaskRevision(taskList);
		Assert.assertEquals(1, rowDeleted);
		List<TaskRevision> revisions = taskRevisionDao.getTaskRevisions(task);
		Assert.assertTrue(revisions.isEmpty());
	}
	
	@Test
	public void deleteTaskRevisionsByTask() {
		// course with task course element
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-9");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-rev-user-10");
		
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		TaskList taskList = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		// participant select a task
		File taskFile = new File("tasked.txt");
		AssignmentResponse response = gtaManager.selectTask(participant, taskList, null, node, taskFile);
		Task task = response.getTask();
		Assert.assertNotNull(task);
		
		// coach create the revision
		TaskRevision rev = taskRevisionDao.createTaskRevision(task, TaskProcess.revision.name(), 1, "Still to do", coach, new Date());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rev);
		
		int rowDeleted = taskRevisionDao.deleteTaskRevision(task);
		dbInstance.commit();
		Assert.assertEquals(1, rowDeleted);
		List<TaskRevision> revisions = taskRevisionDao.getTaskRevisions(task);
		Assert.assertTrue(revisions.isEmpty());
	}
	
}
