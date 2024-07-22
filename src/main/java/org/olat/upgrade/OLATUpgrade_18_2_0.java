/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.upgrade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.prefs.gui.GuiPreference;
import org.olat.core.util.prefs.gui.GuiPreferenceService;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.VideoManager;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;

/**
 * Initial date: Dez 11, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OLATUpgrade_18_2_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_2_0.class);

	private static final int BATCH_SIZE = 1000;

	private static final String VERSION = "OLAT_18.2.0";
	private static final String MIGRATE_GUI_PREFERENCES = "MIGRATE GUI PREFERENCES";
	private static final String MIGRATE_REPOSITORY_ENTRY_RUNTIME_TYPE = "MIGRATE REPOSITORY ENTRY RUNTIME TYPE";
	private static final String MIGRATE_VIDEO_COLLECTION =  "MIGRATE VIDEO COLLECTION";

	private static final XStream xstream = XStreamHelper.createXStreamInstance();

	static {
		XStreamHelper.allowDefaultPackage(xstream);
		xstream.alias("prefstore", Map.class);
		xstream.ignoreUnknownElements();
	}

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private GuiPreferenceService guiPreferenceService;

	public OLATUpgrade_18_2_0() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= migrateRepositoryEntriesRuntimeType(upgradeManager, uhd);
		allOk &= migrateVideoCollection(upgradeManager, uhd);
		allOk &= migrateGuiPreferences(upgradeManager, uhd);
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_2_0 not finished, try to restart OpenOlat!");
		}

		return allOk;
	}
	

	private boolean migrateVideoCollection(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(MIGRATE_VIDEO_COLLECTION)) {
			try {
				log.info("Start migrating video collection.");
				
				int counter = 0;
				List<RepositoryEntry> entries;
				do {
					entries = getVideos(counter, BATCH_SIZE);
					for(int i=0; i<entries.size(); i++) {
						RepositoryEntry entry = entries.get(i);
						if(!entry.isVideoCollection()) {
							migrateVideo(entry);
						}

						if(i % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					counter += entries.size();
					log.info(Tracing.M_AUDIT, "Migrated video collection: {} total processed ({})", entries.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (entries.size() == BATCH_SIZE);

				log.info("Migration of video collection finished.");
				
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATE_VIDEO_COLLECTION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
	private void migrateVideo(RepositoryEntry entry) {
		Set<Organisation> organisations = new HashSet<>();
		List<Offer> offers = acService.findOfferByResource(entry.getOlatResource(), true, null, null);	
		Map<Long, List<Organisation>> map = acService.getOfferKeyToOrganisations(offers);
		if(map != null && !map.isEmpty()) {
			for(List<Organisation> organisationList:map.values()) {
				organisations.addAll(organisationList);
			}
		}
		
		boolean visibleInCollection = entry.isPublicVisible() || !offers.isEmpty();
		if(visibleInCollection) {
			videoManager.setVideoCollection(entry, true, new ArrayList<>(organisations));
		}
	}
	
	private List<RepositoryEntry> getVideos(int firstResult, int maxResults) {
		String query = """
				select v from repositoryentry as v 
				inner join v.olatResource as res
				where res.resName=:resourceType and v.status=:status
				order by v.key""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntry.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.setParameter("resourceType", VideoFileResource.TYPE_NAME)
				.setParameter("status", RepositoryEntryStatusEnum.published.name())
				.getResultList();
	}

	private boolean migrateRepositoryEntriesRuntimeType(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_REPOSITORY_ENTRY_RUNTIME_TYPE)) {
			try {
				log.info("Start migrating repository entries runtime types.");
				
				int counter = 0;
				List<RepositoryEntry> entries;
				do {
					entries = getRepositoryEntries(counter, BATCH_SIZE);
					for(int i=0; i<entries.size(); i++) {
						RepositoryEntry entry = entries.get(i);
						if(entry.getRuntimeType() == null) {
							migrateRepositoryEntryRuntimeType(entry);
						}
						if(i % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					counter += entries.size();
					log.info(Tracing.M_AUDIT, "Migrated repository entries runtime type: {} total processed ({})", entries.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (entries.size() == BATCH_SIZE);

				log.info("Migration of repository entries runtime types finished.");
				
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATE_REPOSITORY_ENTRY_RUNTIME_TYPE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateRepositoryEntryRuntimeType(RepositoryEntry entry) {
		entry = repositoryService.loadBy(entry);
		if(entry == null || entry.getRuntimeType() != null) return;
		
		String typeName = entry.getOlatResource().getResourceableTypeName();
		RepositoryEntryRuntimeType type = RepositoryEntryRuntimeType.embedded;
		if("CourseModule".equals(typeName)) {
			type = RepositoryEntryRuntimeType.standalone;
		} else {
			boolean hasUserManagement = repositoryService.hasUserManaged(entry);
			if(hasUserManagement) {
				type = RepositoryEntryRuntimeType.standalone;
			} else {
				List<Offer> offers = acService.findOfferByResource(entry.getOlatResource(), true, null, null);
				if(!offers.isEmpty()) {
					type = RepositoryEntryRuntimeType.standalone;
				}
			}
		}

		entry.setRuntimeType(type);
		repositoryService.update(entry);
	}
	
	public List<RepositoryEntry> getRepositoryEntries(int firstResult, int maxResults) {
		String query = "select v from repositoryentry as v order by v.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntry.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}

	private boolean migrateGuiPreferences(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATE_GUI_PREFERENCES)) {
			try {
				log.info("Start migrating gui preferences.");

				int counter = 0;
				List<Property> oldGuiPreferences;
				do {
					oldGuiPreferences = getOldGuiPreferences(counter, BATCH_SIZE);

					for (Property guiPref : oldGuiPreferences) {
						Identity identity = guiPref.getIdentity();
						// removing unnecessary root tag DbPrefs
						String normalizedGuiPrefStorage = guiPref.getTextValue().replace("<org.olat.core.util.prefs.db.DbPrefs>", "").replace("</org.olat.core.util.prefs.db.DbPrefs>", "");
						createOrUpdateGuiPref(identity, normalizedGuiPrefStorage);
						// commit after each property, old users could have a lot, so to prevent too much traffic at once
						dbInstance.commitAndCloseSession();
					}
					counter += oldGuiPreferences.size();
					log.info(Tracing.M_AUDIT, "Migrated gui preferences: {} total processed ({})", oldGuiPreferences.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (oldGuiPreferences.size() == BATCH_SIZE);

				log.info("Migration of gui preferences finished.");
			} catch (Exception e) {
				log.debug("", e);
				allOk = false;
			}

			uhd.setBooleanDataValue(MIGRATE_GUI_PREFERENCES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}

	private void createOrUpdateGuiPref(Identity identity, String normalizedGuiPrefStorage) {
		try {
			// if xstream throws exception, only that faulty property won't be transferred
			@SuppressWarnings("unchecked")
			Map<String, Object> attrClassAndPrefKeyToPrefValueMap = (Map<String, Object>) xstream.fromXML(normalizedGuiPrefStorage);
			for (Map.Entry<String, Object> entry : attrClassAndPrefKeyToPrefValueMap.entrySet()) {
				String attributedClass = StringUtils.substringBefore(entry.getKey(), "::");
				String prefKey = StringUtils.substringAfter(entry.getKey(), "::");
				String guiPrefEntryValue = xstream.toXML(entry.getValue());

				List<GuiPreference> guiPreferences = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass, prefKey);
				// if upgrade happens more than once then update entry instead of creating
				if (guiPreferences.isEmpty()) {
					GuiPreference guiPreference = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey, guiPrefEntryValue);
					guiPreferenceService.persistOrLoad(guiPreference);
				} else if (guiPreferences.size() == 1) {
					// updating happens only if upgrade is happening more than once, e.g. because first migration had faulty entries
					// if all three parameters (identity, attributedClass and prefKey) are set, there can only be one entry, thus get(0)
					GuiPreference guiPrefToUpdate = guiPreferences.get(0);
					guiPrefToUpdate.setPrefValue(guiPrefEntryValue);
					guiPreferenceService.updateGuiPreferences(guiPrefToUpdate);
				}
			}
		} catch (Exception e) {
			log.debug("Creating or updating for following entry failed: {}", normalizedGuiPrefStorage);
		}
	}

	private List<Property> getOldGuiPreferences(int firstResult, int maxResults) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select gp from property as gp")
				.append(" inner join fetch gp.identity as ident")
				.and().append("gp.name=:name");

		return dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), Property.class)
				.setParameter("name", "v2guipreferences")
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
}