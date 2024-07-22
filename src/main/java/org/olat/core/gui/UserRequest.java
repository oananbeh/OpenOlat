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
* <p>
*/ 

package org.olat.core.gui;

import java.util.Date;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.gui.control.DispatchResult;
import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;

/**
 * is the "thing" generated by one user-click. It contains mainly the servlet
 * request and response and the usersession, and it should not be assigned to an instance variable.
 *
 * @author Felix Jost
 */
public interface UserRequest {

	public static final String PARAM_DELIM = ":";

	public String getUuid();
	
	public Date getRequestTimestamp();
	
	public String getUriPrefix();

	/**
	 * @param key
	 * @return the value of the parameter with key 'key'
	 */
	public String getParameter(String key);
	/**
	 * @return the Set of parameters
	 */
	public Set<String> getParameterSet();

	/**
	 * @return the http request
	 */
	public HttpServletRequest getHttpReq();

	/**
	 * @return the usersession
	 */
	public UserSession getUserSession();

	/**
	 * @return HttpServletResponse
	 */
	public HttpServletResponse getHttpResp();

	/**
	 * convenience method
	 * 
	 * @return Locale
	 */
	public Locale getLocale();

	/**
	 * convenience method
	 * 
	 * @return Subject
	 */
	public Identity getIdentity();

	/**
	 * @return String
	 */
	public String getModuleURI();

	/**
	 * Only getter provided. User URLBuilder to set the resulting respond's
	 * windowID.
	 * 
	 * @return The window ID
	 */
	public String getWindowID();
	

	/**
	 * @return The window component ID
	 */
	public String getWindowComponentID();
	
	/**
	 * 
	 * @param dispatchId
	 */
	public void overrideWindowComponentID(String dispatchId);

	/**
	 * Only getter provided. User URLBuilder to set the resulting respond's
	 * timestampID.
	 * 
	 * @return the timestamp
	 */
	public String getTimestampID();

	/**
	 * Only getter provided. User URLBuilder to set the resulting respond's
	 * componentID.
	 * 
	 * @return the component id
	 */
	public String getComponentID();
	
	/**
	 * @return
	 */
	public String getComponentTimestamp();
	
	public String getRequestCsrfToken();
	
	public void setRequestCsrfToken(String token);

	/**
	 * @return true if the url containing the encoded params for timestamp,
	 *         windowid, and component id; and false if the url was e.g. an url
	 *         like /olat/auth/go/course
	 */
	public boolean isValidDispatchURI();

	/**
	 * @return the uri; never null, but may be an empty string
	 */
	public String getNonParsedUri();

	/**
	 * @return Returns the dispatchResult.
	 */
	public DispatchResult getDispatchResult();

	/**
	 * @return Returns the mode.
	 */
	public int getMode();
}