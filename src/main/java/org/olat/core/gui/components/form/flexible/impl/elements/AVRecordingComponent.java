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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * Initial date: 2022-09-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVRecordingComponent extends FormBaseComponentImpl {

	private static final ComponentRenderer RENDERER = new AVRecordingRenderer();

	private final String posterName;
	private final AVRecordingImpl element;

	public AVRecordingComponent(String name, String posterName, AVRecordingImpl element) {
		super(name);
		this.posterName = posterName;
		this.element = element;
	}

	@Override
	public AVRecordingImpl getFormItem() {
		return element;
	}

	public String getRecordedFileId() {
		return "o_vid_" + getDispatchID();
	}

	public String getPosterName() {
		return posterName;
	}

	public String getPosterFileId() {
		return getRecordedFileId() + "_poster";
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
