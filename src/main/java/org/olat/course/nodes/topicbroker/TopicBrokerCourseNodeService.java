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
package org.olat.course.nodes.topicbroker;

import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 14 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface TopicBrokerCourseNodeService {

	void synchBroker(Identity doer, RepositoryEntry courseEntry, CourseNode courseNode);
	
	void createCustomFieldDefinitions(Identity doer, RepositoryEntry courseEntry, String courseNodeIdent,
			String customFieldDefinitionsXml);

	String getCustomFieldDefinitionExportXml(RepositoryEntry courseEntry, String courseNodeIdent);

	void deleteBroker(RepositoryEntry courseEntry, CourseNode courseNode);

}
