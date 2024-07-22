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
package org.olat.course.todo.manager;

import java.util.HashMap;
import java.util.Map;

import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.todo.CourseNodeToDoSyncher;
import org.olat.course.todo.CourseNodesToDoSyncher;
import org.olat.course.todo.CourseToDoEnvironment;

/**
 * 
 * Initial date: 19 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodesToDoSyncherImpl implements CourseNodesToDoSyncher {
	
	private CourseToDoEnvironment courseToDoEnv;
	private Map<String, CourseNodeToDoSyncher> identToCourseNodeToDoSyncher;

	@Override
	public void synch(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		CourseNodeToDoSyncher courseNodeToDoSyncher = identToCourseNodeToDoSyncher.get(courseNode.getIdent());
		if(courseNodeToDoSyncher != null) {
			courseNodeToDoSyncher.synch(courseNode, userCourseEnv, courseToDoEnv);
		}
	}

	public void setCourseToDoEnv(CourseToDoEnvironment courseToDoEnv) {
		this.courseToDoEnv = courseToDoEnv;
	}

	public void setCourseNodeToDoSyncher(CourseNode courseNode, CourseNodeToDoSyncher courseNodeToDoSyncher) {
		if (courseNode == null || courseNodeToDoSyncher == null) {
			return;
		}
		
		if (identToCourseNodeToDoSyncher == null) {
			identToCourseNodeToDoSyncher = new HashMap<>();
		}
		identToCourseNodeToDoSyncher.put(courseNode.getIdent(), courseNodeToDoSyncher);
	}

	@Override
	public void reset() {
		if (courseToDoEnv != null) {
			courseToDoEnv.reset();
		}
		if (identToCourseNodeToDoSyncher != null && !identToCourseNodeToDoSyncher.isEmpty()) {
			identToCourseNodeToDoSyncher.values().forEach(CourseNodeToDoSyncher::reset);
		}
	}
	
}
