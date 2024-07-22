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
package org.olat.modules.video.ui.editor;

import java.io.Serial;

import org.olat.core.gui.control.Event;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2023-01-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditQuestionEvent extends Event {
	@Serial
	private static final long serialVersionUID = 7215582487384570416L;
	private static final String COMMAND = "edit.question";
	private final String questionId;
	private final RepositoryEntry repositoryEntry;

	public EditQuestionEvent(String questionId, RepositoryEntry repositoryEntry) {
		super(COMMAND);
		this.questionId = questionId;
		this.repositoryEntry = repositoryEntry;
	}

	public String getQuestionId() {
		return questionId;
	}

	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}
}
