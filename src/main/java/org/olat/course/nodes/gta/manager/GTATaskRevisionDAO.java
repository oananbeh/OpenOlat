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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.model.TaskRevisionImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTATaskRevisionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public TaskRevision createTaskRevision(Task task, String status, int revisionLoop, String comment, Identity commentator, Date revisionDate) {
		TaskRevisionImpl rev = new TaskRevisionImpl();
		rev.setCreationDate(new Date());
		rev.setLastModified(rev.getCreationDate());
		rev.setDate(revisionDate);
		if(StringHelper.containsNonWhitespace(comment)) {
			rev.setComment(comment);
			rev.setCommentLastModified(new Date());
			rev.setCommentAuthor(commentator);
		}
		rev.setTask(task);
		rev.setStatus(status);
		rev.setRevisionLoop(revisionLoop);
		dbInstance.getCurrentEntityManager().persist(rev);
		return rev;
	}
	
	public TaskRevision updateTaskRevision(TaskRevision taskRevision) {
		((TaskRevisionImpl)taskRevision).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(taskRevision);
	}
	
	public List<TaskRevision> getTaskRevisions(Task task) {
		String s = "select rev from gtataskrevision as rev where rev.task.key=:taskKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(s, TaskRevision.class)
				.setParameter("taskKey", task.getKey())
				.getResultList();
	}
	
	public List<TaskRevision> getLatestTaskRevisions(RepositoryEntryRef entry, GTACourseNode gtaNode, Collection<? extends IdentityRef> identites) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rev");
		sb.append("  from gtataskrevision as rev");
		sb.append("       join fetch rev.task as task");
		sb.append(" where (task.id, rev.revisionLoop) in (");
		sb.append("       select task2.key");
		sb.append("            , max(rev2.revisionLoop)");
		sb.append("         from gtataskrevision as rev2");
		sb.append("              join rev2.task as task2");
		sb.append("              join task2.taskList as taskList2");
		sb.append("        where taskList2.entry.key = :entryKey");
		sb.append("          and taskList2.courseNodeIdent = :courseNodeIdent");
		sb.append("          and task2.identity.key in (:identityKeys)");
		sb.append("     group by task2.key");
		sb.append(")");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TaskRevision.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("courseNodeIdent", gtaNode.getIdent())
				.setParameter("identityKeys", identites.stream().map(IdentityRef::getKey).toList())
				.getResultList();
	}
	
	public TaskRevision getTaskRevision(Task task, String status, int revisionLoop) {
		String s = "select rev from gtataskrevision as rev where rev.task.key=:taskKey and rev.revisionLoop=:revisionLoop and rev.status=:status";
		List<TaskRevision> revisions = dbInstance.getCurrentEntityManager()
				.createQuery(s, TaskRevision.class)
				.setParameter("taskKey", task.getKey())
				.setParameter("revisionLoop", Integer.valueOf(revisionLoop))
				.setParameter("status", status)
				.getResultList();
		return revisions == null || revisions.isEmpty() ? null : revisions.get(0);
	}
	
	public int deleteTaskRevision(TaskList taskList) {
		StringBuilder taskSb = new StringBuilder(128);
		taskSb.append("delete from gtataskrevision as taskrev where taskrev.task.key in (")
		      .append("  select task.key from gtatask as task where task.taskList.key=:taskListKey)");
		return dbInstance.getCurrentEntityManager()
			.createQuery(taskSb.toString())
			.setParameter("taskListKey", taskList.getKey())
			.executeUpdate();
	}
	
	public int deleteTaskRevision(Task task) {
		StringBuilder taskSb = new StringBuilder(128);
		taskSb.append("delete from gtataskrevision as taskrev where taskrev.task.key=:taskKey");
		return dbInstance.getCurrentEntityManager()
			.createQuery(taskSb.toString())
			.setParameter("taskKey", task.getKey())
			.executeUpdate();
	}
}
