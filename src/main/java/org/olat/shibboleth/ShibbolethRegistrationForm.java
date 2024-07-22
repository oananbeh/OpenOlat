/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.shibboleth;

import java.util.List;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.UsernameValidationRulesFactory;
import org.olat.login.validation.ValidationResult;
import org.olat.user.ChangePasswordForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
* Initial Date:  09.08.2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */

public class ShibbolethRegistrationForm extends FormBasicController {

	private TextElement usernameEl;
	private final String proposedUsername;

	private final SyntaxValidator usernameSyntaxValidator;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UsernameValidationRulesFactory usernameRulesFactory;

	public ShibbolethRegistrationForm(UserRequest ureq, WindowControl wControl, String proposedUsername) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale(), getTranslator()));
		this.proposedUsername = proposedUsername;
		usernameSyntaxValidator = new SyntaxValidator(usernameRulesFactory.createRules(false), false);
		initForm(ureq);
	}
	
	/**
	 * @return Login field.
	 */
	protected String getUsernameEl() {
		return usernameEl.getValue();
	}
	
	protected String getProposedUsername() {
		return proposedUsername;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		// validate if username does match the syntactical login requirements
		boolean allOk = super.validateFormLogic(ureq);
		
		usernameEl.clearError();
		String username = usernameEl.getValue();
		if (StringHelper.containsNonWhitespace(username)) {
			TransientIdentity newIdentity = new TransientIdentity();
			newIdentity.setName(username);
			ValidationResult validationResult = usernameSyntaxValidator.validate(username, newIdentity);
			if (!validationResult.isValid()) {
				String descriptions = validationResult.getInvalidDescriptions().get(0).getText(getLocale());
				usernameEl.setErrorKey("error.username.invalid", descriptions);
				allOk &= false;
			} else if(!isNickNameValid(username)) {

				usernameEl.setErrorKey("sm.error.username_in_use");
				allOk &= false;
			}
		} else {
			usernameEl.setErrorKey("form.legende.mandatory");
			return false;
		} 
		
		return allOk;
	}
	
	private boolean isNickNameValid(String val) {
		Identity identity = securityManager.findIdentityByNickName(val);
		if(identity == null) {
			return true;
		}
		
		List<Identity> identities = securityManager.findIdentitiesByUsername(val);
		if(identities == null || identities.isEmpty()) {
			return true;
		}
		if(identities.size() > 1) {
			return false;
		}
		
		// If only one user with the default authentication is present, it can be migrated
		List<Authentication> authentications = securityManager.getAuthentications(identities.get(0));
		return authentications.stream()
				.anyMatch(auth -> BaseSecurityModule.getDefaultAuthProviderIdentifier().equals(auth.getProvider()));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String initialValue = proposedUsername == null ? "" : proposedUsername;
		usernameEl = uifactory.addTextElement("srf_login", "srf.login", 128, initialValue, formLayout);
		usernameEl.setExampleKey("srf.login.example", null);
		uifactory.addFormSubmitButton("save", formLayout);
	}
}
