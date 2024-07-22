/**
* OLAT - Online Learning and Training<br>
* https://www.olat.org
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
* <a href="https://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.TeaserImageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 02.09.2005 <br>
 * 
 * @author Mike Stock
 * @author guido
 * @author Florian Gnägi
 * @author fkiefer
 */
@Service
public class CourseModule extends AbstractSpringModule {
	
	public static final String COURSE_TYPE_PATH = LearningPathNodeAccessProvider.TYPE;
	public static final String COURSE_TYPE_PROGRESS = "progress";
	public static final String COURSE_TYPE_CLASSIC = ConditionNodeAccessProvider.TYPE;

	private static final String COURSE_DISPLAY_CHANGELOG = "course.display.changelog";
	private static final String COURSE_DISPLAY_INFOBOX = "course.display.infobox";
	private static final String COURSE_DISCLAIMER_ENABLED = "course.disclaimer.enabled";
	private static final String COURSE_TYPE_DEFAULT = "course.type.default";
	private static final String COURSE_STYLE_TEASER_IMAGE_SOURCE_TYPE = "course.style.teaser.image.source.type";
	private static final String COURSE_STYLE_TEASER_IMAGE_FILENAME = "course.style.teaser.image.filename";
	private static final String COURSE_STYLE_TEASER_IMAGE_STYLE = "course.style.teaser.image.style";
	private static final String COURSE_STYLE_SYSTEM_IMAGES_PROVIDED = "course.style.system.images.provided";
	private static final String COURSE_EFFICIENCY_STATEMENT_ENABLED = "course.efficiency.statement.enabled";
	
	private static final String COURSE_ARCHIVE_RETENTION = "course.archive.retention";
	
	@Value("${course.display.participants.count}")
	private boolean displayParticipantsCount;
	@Value("${help.course.softkey}")
	private String helpCourseSoftkey;
	@Autowired @Qualifier("logVisibilityForCourseAuthor")
	private HashMap<String, String> logVisibilities;
	@Value("${course.display.infobox}")
	private boolean displayInfoBox;
	@Value("${course.display.changelog}")
	private boolean displayChangeLog;
	@Value("${course.disclaimer.enabled:true}")
	private boolean disclaimerEnabled;
	@Value("${course.type.default}")
	private String courseTypeDefault;
	@Value("${course.archive.log.table.on.delete:true}")
	private String archiveLogTableOnDelete;
	@Value("${course.info.details.enabled:false}")
	private boolean infoDetailsEnabled;
	@Value("${course.efficiency.statement.enabled:true}")
	private boolean efficiencyStatementEnabled;
	@Value("${course.style.teaser.image.type}")
	private String teaserImageSourceTypeName;
	private ImageSourceType teaserImageSourceType;
	@Value("${course.style.teaser.image.filename}")
	private String teaserImageFilename;
	@Value("${course.style.teaser.image.style}")
	private String teaserImageStyleName;
	private TeaserImageStyle teaserImageStyle;
	private String systemImagesProvided;
	
	private Integer courseArchiveRetention;
	@Value("${course.archive.retention:10}")
	
	// Repository types
	public static final String ORES_TYPE_COURSE = OresHelper.calculateTypeName(CourseModule.class);
	public static final OLATResourceable ORESOURCEABLE_TYPE_COURSE = OresHelper.lookupType(CourseModule.class);
	public static final String ORES_COURSE_ASSESSMENT = OresHelper.calculateTypeName(AssessmentManager.class);
	
	private final CoordinatorManager coordinatorManager;

	@Autowired
	public CourseModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
		this.coordinatorManager = coordinatorManager;
	}
	
	@Override
	protected void initFromChangedProperties() {
		//set properties
		String userAllowed = getStringPropertyValue(COURSE_DISPLAY_INFOBOX, true);
		if(StringHelper.containsNonWhitespace(userAllowed)) {
			displayInfoBox = "true".equals(userAllowed);
		}
		String authorAllowed = getStringPropertyValue(COURSE_DISPLAY_CHANGELOG, true);
		if(StringHelper.containsNonWhitespace(authorAllowed)) {
			displayChangeLog = "true".equals(authorAllowed);
		}
		
		String disclaimerEnabledObj = getStringPropertyValue(COURSE_DISCLAIMER_ENABLED, false);
		if (StringHelper.containsNonWhitespace(disclaimerEnabledObj)) {
			disclaimerEnabled = "true".equals(disclaimerEnabledObj);
		}
		
		String courseTypeDefaultObj = getStringPropertyValue(COURSE_TYPE_DEFAULT, true);
		if (StringHelper.containsNonWhitespace(courseTypeDefaultObj)) {
			courseTypeDefault = courseTypeDefaultObj;
		}
		
		String teaserImageSourceTypeObj = getStringPropertyValue(COURSE_STYLE_TEASER_IMAGE_SOURCE_TYPE, true);
		if (StringHelper.containsNonWhitespace(teaserImageSourceTypeObj)) {
			teaserImageSourceTypeName = teaserImageSourceTypeObj;
			teaserImageSourceType = ImageSourceType.valueOf(teaserImageSourceTypeName);
		}
		
		String teaserImageFilenameObj = getStringPropertyValue(COURSE_STYLE_TEASER_IMAGE_FILENAME, true);
		if (StringHelper.containsNonWhitespace(teaserImageFilenameObj)) {
			teaserImageFilename = teaserImageFilenameObj;
		}
		
		String teaserImageStyleObj = getStringPropertyValue(COURSE_STYLE_TEASER_IMAGE_STYLE, true);
		if (StringHelper.containsNonWhitespace(teaserImageStyleObj)) {
			teaserImageStyleName = teaserImageStyleObj;
			teaserImageStyle = TeaserImageStyle.valueOf(teaserImageStyleName);
		}

		String efficiencyStatementObj = getStringPropertyValue(COURSE_EFFICIENCY_STATEMENT_ENABLED, true);
		if (StringHelper.containsNonWhitespace(efficiencyStatementObj)) {
			efficiencyStatementEnabled = "true".equals(efficiencyStatementObj);
		}
		
		systemImagesProvided = getStringPropertyValue(COURSE_STYLE_SYSTEM_IMAGES_PROVIDED, systemImagesProvided);
		
		String retentionObj = getStringPropertyValue(COURSE_ARCHIVE_RETENTION, true);
		if (StringHelper.containsNonWhitespace(retentionObj) && StringHelper.isLong(retentionObj)) {
			courseArchiveRetention = Integer.valueOf(retentionObj);
		}
	}

	@Override
	public void init() {
		initFromChangedProperties();
	}
	
	/**
	 * @return true if the course author can see/download/modify the admin log
	 */
	public boolean isAdminLogVisibleForMigrationOnly() {
		return logVisibilities.get("AdminLog").equals("VISIBLE");
	}

	/**
	 * @return true if the course author can see/download/modify the user log
	 */
	public boolean isUserLogVisibleForMigrationOnly() {
		return logVisibilities.get("UserLog").equals("VISIBLE");
	}

	/**
	 * @return true if the course author can see/download/modify the statistic log
	 */
	public boolean isStatisticLogVisibleForMigrationOnly() {
		return logVisibilities.get("StatisticLog").equals("VISIBLE");
	}


	/**
	 * 
	 * @return The filename of the zipped help course
	 */
	public String getHelpCourseSoftKey() {
		return helpCourseSoftkey;
	}
	
	/**
	 * @return type name
	 */
	public static String getCourseTypeName() {
		return ORES_TYPE_COURSE;
	}

	/**
	 * @param ce
	 * @param cn
	 * @return the generated SubscriptionContext
	 */
	public static SubscriptionContext createSubscriptionContext(CourseEnvironment ce, CourseNode cn) {
		return new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent());
	}

	/**
	 * @param ce
	 * @param cn
	 * @return a subscriptioncontext with no translations for the user, but only
	 *         to be able to cleanup/obtain
	 */
	public static SubscriptionContext createTechnicalSubscriptionContext(CourseEnvironment ce, CourseNode cn) {
		return new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent());
	}

	/**
	 * Creates subscription context which points to an element e.g. that is a sub
	 * element of a node (subsubId). E.g. inside the course node dialog elements
	 * where a course node can have several forums.
	 * 
	 * @param ce
	 * @param cn
	 * @param subsubId
	 * @return
	 */
	public static SubscriptionContext createSubscriptionContext(CourseEnvironment ce, CourseNode cn, String subsubId) {
		return new SubscriptionContext(getCourseTypeName(), ce.getCourseResourceableId(), cn.getIdent() + ":" + subsubId);
	}
	
	public void registerForCourseType(GenericEventListener gel, Identity identity) {
		coordinatorManager.getCoordinator().getEventBus().registerFor(gel, identity, ORESOURCEABLE_TYPE_COURSE);
	}

	public void deregisterForCourseType(GenericEventListener gel) {
		coordinatorManager.getCoordinator().getEventBus().deregisterFor(gel, ORESOURCEABLE_TYPE_COURSE);
	}

	/**
	 * max number of course nodes
	 * @return
	 */
	public static int getCourseNodeLimit() {
		return 499;
	}

	public boolean displayParticipantsCount() {
		return displayParticipantsCount;
	}

	public boolean isDisplayInfoBox() {
		return displayInfoBox;
	}

	public void setDisplayInfoBox(boolean enabled) {
		this.displayInfoBox = enabled;
		setStringProperty(COURSE_DISPLAY_INFOBOX, Boolean.toString(enabled), true);
	}

	public boolean isDisplayChangeLog() {
		return displayChangeLog;
	}

	public void setDisplayChangeLog(boolean enabled) {
		this.displayChangeLog = enabled;
		setStringProperty(COURSE_DISPLAY_CHANGELOG, Boolean.toString(enabled), true);
	}
	
	public void setDisclaimerEnabled(boolean enabled) {
		disclaimerEnabled = enabled;
		setStringProperty(COURSE_DISCLAIMER_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isDisclaimerEnabled() {
		return disclaimerEnabled;
	}
	
	public String getCourseTypeDefault() {
		return courseTypeDefault;
	}
	
	public void setCourseTypeDefault(String courseTypeDefault) {
		this.courseTypeDefault = courseTypeDefault;
		setStringProperty(COURSE_TYPE_DEFAULT, courseTypeDefault, true);
	}

	public boolean isArchiveLogTableOnDelete() {
		return "true".equals(archiveLogTableOnDelete);
	}

	public void setArchiveLogTableOnDelete(String archiveLogTableOnDelete) {
		this.archiveLogTableOnDelete = archiveLogTableOnDelete;
	}

	public boolean isInfoDetailsEnabled() {
		return infoDetailsEnabled;
	}

	public boolean isEfficiencyStatementEnabled() {
		return efficiencyStatementEnabled;
	}

	public void setEfficiencyStatementEnabled(boolean efficiencyStatementEnabled) {
		this.efficiencyStatementEnabled = efficiencyStatementEnabled;
		setStringProperty(COURSE_EFFICIENCY_STATEMENT_ENABLED, Boolean.toString(efficiencyStatementEnabled), true);
	}

	public ImageSourceType getTeaserImageSourceType() {
		return teaserImageSourceType;
	}

	public void setTeaserImageSourceType(ImageSourceType imageSourceType) {
		this.teaserImageSourceType = imageSourceType;
		this.teaserImageSourceTypeName = imageSourceType.name();
		setStringProperty(COURSE_STYLE_TEASER_IMAGE_SOURCE_TYPE, teaserImageSourceTypeName, true);
	}

	public String getTeaserImageFilename() {
		return teaserImageFilename;
	}
	
	public void setTeaserImageFilename(String filename) {
		this.teaserImageFilename = filename;
		setStringProperty(COURSE_STYLE_TEASER_IMAGE_FILENAME, filename, true);
	}

	public TeaserImageStyle getTeaserImageStyle() {
		return teaserImageStyle;
	}

	public void setTeaserImageStyle(TeaserImageStyle teaserImageStyle) {
		this.teaserImageStyle = teaserImageStyle;
		this.teaserImageStyleName = teaserImageStyle.name();
		setStringProperty(COURSE_STYLE_TEASER_IMAGE_STYLE, teaserImageStyleName, true);
	}
	
	public Set<String> getSystemImagesProvided() {
		return StringHelper.containsNonWhitespace(systemImagesProvided)
				? Arrays.stream(systemImagesProvided.split(",")).collect(Collectors.toSet())
				: new HashSet<>();
	}

	public void setSystemImages(Set<String> provided) {
		systemImagesProvided = provided.stream().collect(Collectors.joining(","));
		setStringProperty(COURSE_STYLE_SYSTEM_IMAGES_PROVIDED, systemImagesProvided, true);
	}

	/**
	 * How long the archives can stay.
	 * 
	 * @return A number of day
	 */
	public Integer getCourseArchiveRetention() {
		return courseArchiveRetention == null ? Integer.valueOf(10) : courseArchiveRetention;
	}

	public void setCourseArchiveRetention(Integer retention) {
		this.courseArchiveRetention = retention;
		setStringProperty(COURSE_ARCHIVE_RETENTION, retention == null ? "" : retention.toString(), true);
	}
}
