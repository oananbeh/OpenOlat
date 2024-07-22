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
package org.olat.course.assessment.ui.inspection;

import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.core.id.Identity;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.ui.inspection.CreateInspectionContext.InspectionCompensation;

/**
 * 
 * Initial date: 19 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InspectionMailTemplate extends MailTemplate {

	private final CreateInspectionContext context;
	
	public InspectionMailTemplate(String subject, String body, CreateInspectionContext context) {
		super(subject, body, null);
		this.context = context;
	}

	@Override
	public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
		if(recipient != null) {
			Locale locale = I18nManager.getInstance().getLocaleOrDefault(recipient.getUser().getPreferences().getLanguage());
			fillContextWithStandardIdentityValues(vContext, recipient, locale);
		}
		
		long duration;
		if(context.getNewConfiguration() != null) {
			duration = context.getNewConfiguration().duration() / 60;
		} else {
			AssessmentInspectionConfiguration configuration = context.getInspectionConfiguration();
			duration = configuration.getDuration() / 60;
		}
		
		List<InspectionCompensation> compensations = context.getInspectionCompensations();
		if(compensations != null && recipient != null) {
			for(InspectionCompensation compensation:compensations) {
				if(recipient.getKey().equals(compensation.identity().getKey())) {
					duration += compensation.extraTimeInSeconds() / 60;
					break;
				}
			}
		}
		vContext.put("duration", Long.toString(duration));
	}
}
