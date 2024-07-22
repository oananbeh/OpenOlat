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
package org.olat.modules.immunityproof;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofType;

public class ImmunityProofContext {

	private String qrCode;
	private Identity identity;
	private boolean certificateFound;
	private boolean certificateValid;
	private boolean certificateBelongsToUser;

	private String firstName;
	private String lastName;
	private Date birthDate;
	private Date proofFrom;
	private Date proofUntil;
	private ImmunityProofType proofType;

	private StringBuilder errors;
	private StringBuilder output;

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public boolean isCertificateFound() {
		return certificateFound;
	}

	public void setCertificateFound(boolean certificateFound) {
		this.certificateFound = certificateFound;
	}

	public boolean isCertificateValid() {
		return certificateValid;
	}

	public void setCertificateValid(boolean certificateValid) {
		this.certificateValid = certificateValid;
	}

	public boolean isCertificateBelongsToUser() {
		return certificateBelongsToUser;
	}

	public void setCertificateBelongsToUser(boolean certificateBelongsToUser) {
		this.certificateBelongsToUser = certificateBelongsToUser;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Date getProofFrom() {
		return proofFrom;
	}

	public void setProofFrom(Date proofFrom) {
		this.proofFrom = proofFrom;
	}
	
	public Date getProofUntil() {
		return proofUntil;
	}
	
	public void setProofUntil(Date proofUntil) {
		this.proofUntil = proofUntil;
	}
	
	public ImmunityProofType getProofType() {
		return proofType;
	}
	
	public void setProofType(ImmunityProofType proofType) {
		this.proofType = proofType;
	}

	public StringBuilder getErrors() {
		return errors;
	}

	public void setErrors(StringBuilder errors) {
		this.errors = errors;
	}

	public StringBuilder getOutput() {
		return output;
	}

	public void setOutput(StringBuilder output) {
		this.output = output;
	}

}
