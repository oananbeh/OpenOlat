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
package org.olat.modules.video.manager;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.video.VideoToOrganisation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoToOrganisationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private VideoToOrganisationDAO videoToOrganisationDao;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void createRelation() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Video 1", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, RepositoryEntryRuntimeType.standalone, defOrganisation);
		VideoToOrganisation relation = videoToOrganisationDao.createVideoToOrganisation(re, defOrganisation);
		dbInstance.commit();
		
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertEquals(re, relation.getRepositoryEntry());
		Assert.assertEquals(defOrganisation, relation.getOrganisation());
	}
	
	@Test
	public void getVideoToOrganisation() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Video 2", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, RepositoryEntryRuntimeType.standalone, defOrganisation);
		VideoToOrganisation relation = videoToOrganisationDao.createVideoToOrganisation(re, defOrganisation);
		dbInstance.commit();
		Assert.assertNotNull(relation);
		
		List<VideoToOrganisation> organisations = videoToOrganisationDao.getVideoToOrganisation(re);
		assertThat(organisations)
			.hasSize(1)
			.containsExactly(relation);
	}
	
	@Test
	public void getOrganisations() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Video 3", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, RepositoryEntryRuntimeType.standalone, defOrganisation);
		VideoToOrganisation relation = videoToOrganisationDao.createVideoToOrganisation(re, defOrganisation);
		dbInstance.commit();
		Assert.assertNotNull(relation);
		
		List<Organisation> organisations = videoToOrganisationDao.getOrganisations(re);
		assertThat(organisations)
			.hasSize(1)
			.containsExactly(defOrganisation);
	}

}
