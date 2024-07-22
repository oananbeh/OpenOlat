/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment;

import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentInspectionConfiguration extends SafeExamBrowserEnabled, ModifiedInfo, CreateInfo {
	
	Long getKey();
	
	/**
	 * @return Duration in seconds
	 */
	int getDuration();

	void setDuration(int duration);
	
	String getName();
	
	void setName(String name);

	String getOverviewOptions();
	
	List<String> getOverviewOptionsAsList();

	void setOverviewOptions(String overviewOptions);
	
	boolean isRestrictAccessIps();

	void setRestrictAccessIps(boolean restrictAccessIps);

	String getIpList();

	void setIpList(String ipList);
	
	RepositoryEntry getRepositoryEntry();

}
