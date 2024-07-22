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
package org.olat.commons.coordinate.cluster.jms;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 13 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JMSHelper {
	
	private static final Logger log = Tracing.createLoggerFor(JMSHelper.class);
	
	public static void close(Connection connection, MessageConsumer consumer, Session session) {
		if(consumer != null) {
			try {
				consumer.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
		
		if(session != null ) {
			try {
				session.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
		
		if(connection != null ) {
			try {
				connection.stop();
				connection.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
	}
}
