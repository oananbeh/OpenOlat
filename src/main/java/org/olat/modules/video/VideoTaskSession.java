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
package org.olat.modules.video;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface VideoTaskSession extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public Date getFinishTime();
	
	public void setFinishTime(Date timestamp);
	
	/**
	 * @return The time between finish and creation date in milliseconds
	 */
	public long getDuration();
	
	/**
	 * The passed value calculated from a cut value and the
	 * raw score.
	 * 
	 * @return
	 */
	public Boolean getPassed();

	public void setPassed(Boolean passed);

	/**
	 * The score calculated only by algorithm.
	 * 
	 * @return The raw score calculate by the algorithm
	 */
	public BigDecimal getScore();

	public void setScore(BigDecimal score);
	
	public Float getScoreAsFloat();
	
	public BigDecimal getMaxScore();

	public void setMaxScore(BigDecimal maxScore);

	/**
	 * @return The raw results, can be negative
	 */
	public BigDecimal getResult();

	public void setResult(BigDecimal result);
	
	/**
	 * @return The results in percent, always between 0% - 100%
	 */
	public BigDecimal getResultInPercent();

	public int getSegments();

	public void setSegments(int segments);
	
	
	public long getAttempt();
	
	public boolean isCancelled();

	public void setCancelled(boolean cancelled);
	
	
	public Identity getIdentity();
	
	public String getAnonymousIdentifier();
	
	public boolean isAuthorMode();
	
	public AssessmentEntry getAssessmentEntry();
	
	public RepositoryEntry getVideoEntry();
	
	public RepositoryEntry getRepositoryEntry();
	
	public String getSubIdent();

}
