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
package org.olat.modules.ceditor.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.modules.ceditor.manager.ContentEditorQti;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.jpa.QuizPart;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-03-25<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class NewQuestionItemCalloutController extends BasicController {

	private final VelocityContainer mainVC;
	private final QuizPart quizPart;

	private QuizQuestion quizQuestion;

	@Autowired
	private ContentEditorQti contentEditorQti;

	public NewQuestionItemCalloutController(UserRequest ureq, WindowControl wControl, QuizPart quizPart) {
		super(ureq, wControl);
		this.quizPart = quizPart;

		mainVC = createVelocityContainer("quiz_new_question");

		List<String> links = new ArrayList<>();
		addLink("quiz.question.sc", QTI21QuestionType.sc, links);
		addLink("quiz.question.mc", QTI21QuestionType.mc, links);
		addLink("quiz.question.fib", QTI21QuestionType.fib, links);
		addLink("quiz.question.numerical", QTI21QuestionType.numerical, links);
		addLink("quiz.question.inlinechoice", QTI21QuestionType.inlinechoice, links);

		mainVC.contextPut("links", links);
		putInitialPanel(mainVC);
	}

	private void addLink(String name, QTI21QuestionType type, List<String> links) {
		Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
		link.setIconLeftCSS("o_icon o_icon-fw ".concat(type.getCssClass()));
		link.setUserObject(type);
		mainVC.put(name, link);
		links.add(name);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			if (link.getUserObject() instanceof QTI21QuestionType questionType) {
				doCreateNewQuestion(ureq, questionType);
			}
		}
	}

	private void doCreateNewQuestion(UserRequest ureq, QTI21QuestionType type) {
		QuizQuestion newQuizQuestion = contentEditorQti.createQuestion(quizPart, type, getLocale());
		if (newQuizQuestion == null) {
			fireEvent(ureq, FormEvent.CANCELLED_EVENT);
			return;
		}

		quizQuestion = newQuizQuestion;

		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	public QuizQuestion getQuizQuestion() {
		return quizQuestion;
	}
}
