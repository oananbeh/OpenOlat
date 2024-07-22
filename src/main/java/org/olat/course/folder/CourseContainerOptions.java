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
package org.olat.course.folder;

/**
 * 
 * Initial date: 17 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseContainerOptions {
	
	private final boolean withSharedResource;
	private final boolean withCourseElements;
	private final boolean withCourseFolder;
	private final boolean withCourseDocuments;
	private final boolean withCoachFolder;
	private final boolean withArchives;
	
	private CourseContainerOptions(boolean withSharedResource, boolean withCourseElements, boolean withCourseFolder,
			boolean withCourseDocuments, boolean withCoachFolder, boolean withArchives) {
		this.withSharedResource = withSharedResource;
		this.withCourseElements = withCourseElements;
		this.withCourseFolder = withCourseFolder;
		this.withCourseDocuments = withCourseDocuments;
		this.withCoachFolder = withCoachFolder;
		this.withArchives = withArchives;
	}
	
	public static final CourseContainerOptions all() {
		return new CourseContainerOptions(true, true, true, true, true, true);
	}
	
	public static final CourseContainerOptions withoutElements() {
		return new CourseContainerOptions(true, false, true, false, false, false);
	}
	
	public static final CourseContainerOptions withoutCourseFolder() {
		return new CourseContainerOptions(true, true, false, true, true, true);
	}
	
	public static final CourseContainerOptions courseFolder() {
		return new CourseContainerOptions(false, false, true, false, false, false);
	}

	public boolean withSharedResource() {
		return withSharedResource;
	}

	public boolean withCourseElements() {
		return withCourseElements;
	}

	public boolean withCourseFolder() {
		return withCourseFolder;
	}

	public boolean withCourseDocuments() {
		return withCourseDocuments;
	}

	public boolean withCoachFolder() {
		return withCoachFolder;
	}
	
	public boolean withArchives() {
		return withArchives;
	}
}
