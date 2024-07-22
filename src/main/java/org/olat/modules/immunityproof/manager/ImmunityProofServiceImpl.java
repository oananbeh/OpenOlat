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
package org.olat.modules.immunityproof.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofType;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 08.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class ImmunityProofServiceImpl implements ImmunityProofService, UserDataDeletable {

	@Autowired
	private ImmunityProofDAO immunityProofDAO;
	@Autowired
	private ImmunityProofModule immunityProofModule;
	
	@Override
	public void createImmunityProof(Identity identity, ImmunityProofType type, Date validFrom, Date validUntil, boolean sendMail, boolean validated, boolean deleteOtherImmunityProof) {
		if (deleteOtherImmunityProof) {
			deleteImmunityProof(identity);
		}
		
		if (validFrom == null || immunityProofModule.getValidity(type) == null) {
			return;
		}
		
		long daysToMs = 24l * 60 * 60 * 1000;
		Date maxSafeUntilDate = new Date(validFrom.getTime() + immunityProofModule.getValidity(type).getRight() * daysToMs);
		Date safeUntil = new Date();
		safeUntil.setTime(safeUntil.getTime() + immunityProofModule.getValidity(type).getLeft() * daysToMs);
			
		if (safeUntil.after(maxSafeUntilDate)) {
			safeUntil = maxSafeUntilDate;
		}
		
		if (validUntil != null && safeUntil.after(validUntil)) {
			safeUntil = validUntil;
		}
		
		immunityProofDAO.createImmunityProof(identity, safeUntil, sendMail, validated);
	}

	@Override
	public ImmunityProof getImmunityProof(Identity identity) {
		if (identity == null) {
			return null;
		}
		
		return immunityProofDAO.getImmunitiyProof(identity);
	}

	@Override
	public List<ImmunityProof> getImmunityProofs(Collection<? extends Identity> identities) {
		return immunityProofDAO.getImmunityProofs(identities);
	}

	@Override
	public void deleteImmunityProof(Identity identity) {
		immunityProofDAO.deleteImmunityProof(identity);
	}

	@Override
	public void pruneImmunityProofs(Date pruneUntil) {
		immunityProofDAO.pruneImmunityProofs(pruneUntil);
	}

	@Override
	public void deleteAllImmunityProofs(boolean notifyUser) {
		immunityProofDAO.deleteAllImmunityProofs();
	}
	
	@Override
	public void deleteImmunityProof(ImmunityProof immunityProof) {
		immunityProofDAO.deleteImmunityProof(immunityProof);
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		deleteImmunityProof(identity);		
	}
	
	@Override
	public List<ImmunityProof> getAllCertificates() {
		return immunityProofDAO.getAllCertificates();
	}
	
	@Override
	public ImmunityProof updateImmunityProof(ImmunityProof certificate) {
		return immunityProofDAO.updateImmunityProof(certificate);
	}
	
	@Override
	public ImmunityProofLevel getImmunityProofLevel(ImmunityProof immunityProof) {
		ImmunityProofLevel level = ImmunityProofLevel.none;
		
		if (immunityProof != null && immunityProof.getSafeDate().after(new Date())) {
			if (immunityProof.isValidated()) {
				level = ImmunityProofLevel.validated;
			} else {
				level = ImmunityProofLevel.claimed;
			}
		}
		
		return level;
	}
	
	@Override
	public long getImmunityProofCount() {
		return immunityProofDAO.getCount();
	}

}
