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
package org.olat.modules.openbadges.manager;

import java.io.InputStream;

import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeClasses;
import org.olat.modules.openbadges.model.BadgeClassImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * Initial date: 2024-06-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeClassesXStream {
	private static final XStream xstream = XStreamHelper.createXStreamInstance();

	static {
		Class<?>[] types = new Class[]{
			BadgeClasses.class,
				BadgeClass.class, BadgeClassImpl.class,
				BadgeClass.BadgeClassStatus.class, BadgeClass.BadgeClassTimeUnit.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("badgeClasses", BadgeClasses.class);
		xstream.alias("badgeClass", BadgeClassImpl.class);
		xstream.omitField(BadgeClassImpl.class, "entry");
	}

	public static String toXML(BadgeClasses badgeClasses) {
		return xstream.toXML(badgeClasses);
	}

	public static BadgeClasses fromXML(InputStream is) {
		return (BadgeClasses) xstream.fromXML(is);
	}
}
