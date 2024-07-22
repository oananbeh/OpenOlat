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

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.model.jpa.CodePart;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.model.jpa.ImageComparisonPart;
import org.olat.modules.ceditor.model.jpa.MathPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.model.jpa.TablePart;
import org.olat.modules.cemedia.MediaVersion;

/**
 * 
 * Initial date: 21 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorUIFactory {
	
	public static String formatUntitled(Translator translator, String original) {
		String shortened = original;
		if (!StringHelper.containsNonWhitespace(original)) {
			shortened = "";
		} else if (original.length() > 9) {
			shortened = original.substring(0, 8);
		}
		return translator.translate("untitled", new String[] { shortened } );
	}

	public static void refreshElementLayoutOptions(PageElement sourceElement, PageElement targetElement) {
		if (sourceElement instanceof MediaPart sourcePart && targetElement instanceof MediaPart targetPart) {
			targetPart.setLayoutOptions(sourcePart.getLayoutOptions());
		}
		if (sourceElement instanceof ParagraphPart sourcePart && targetElement instanceof ParagraphPart targetPart) {
			targetPart.setLayoutOptions(sourcePart.getLayoutOptions());
		}
		if (sourceElement instanceof TablePart sourcePart && targetElement instanceof TablePart targetPart) {
			targetPart.setLayoutOptions(sourcePart.getLayoutOptions());
		}
		if (sourceElement instanceof MathPart sourcePart && targetElement instanceof MathPart targetPart) {
			targetPart.setLayoutOptions(sourcePart.getLayoutOptions());
		}
		if (sourceElement instanceof CodePart sourcePart && targetElement instanceof CodePart targetPart) {
			targetPart.setLayoutOptions(sourcePart.getLayoutOptions());
		}
		if (sourceElement instanceof QuizPart sourcePart && targetElement instanceof QuizPart targetPart) {
			targetPart.setLayoutOptions(sourcePart.getLayoutOptions());
		}
		if (sourceElement instanceof GalleryPart sourcePart && targetElement instanceof GalleryPart targetPart) {
			targetPart.setLayoutOptions(sourcePart.getLayoutOptions());
		}
		if (sourceElement instanceof ImageComparisonPart sourcePart && targetElement instanceof ImageComparisonPart targetPart) {
			targetPart.setLayoutOptions(sourcePart.getLayoutOptions());
		}
	}

	public static String getVersionName(Translator translator, MediaVersion mediaVersion) {
		if (mediaVersion == null || mediaVersion.getVersionName() == null ||
				"0".equals(mediaVersion.getVersionName())) {
			return translator.translate("gallery.version.last");
		}
		return translator.translate("gallery.version.nodate", mediaVersion.getVersionName());
	}
}
