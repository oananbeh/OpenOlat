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

package org.olat.core.gui.control.winmgr;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;

/**
 * Description:<br>
 * Initial Date:  28.03.2006 <br>
 *
 * @author Felix Jost
 */
public class CommandFactory {
	
	public enum InvokeIdentifier {
		DIRTY(2),
		VIDEO(3),
		FUNCTION(4),
		REDIRECT_URL(5),
		PREPARE_CLIENT(6),
		JS_CSS(7),
		NEW_WINDOW(8),
		SCROLL(9),
		DIRTY_FORM(10),
		FOCUS(11),
		DOWNLOAD_URL(12);
		
		private int number;
		
		private InvokeIdentifier(int number) {
			this.number = number;
		}
		
		public int number() {
			return number;
		}
	}
	
	/**
	 * tells the ajax-command interpreter to reload the main (=ajax's parent) window
	 * @param redirectURL e.g. /olat/m/10001/
	 * @return the generated command
	 */
	public static Command createParentRedirectTo(String redirectURL) {
		JSONObject root = new JSONObject();
		try {
			root.put("rurl", redirectURL);
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.REDIRECT_URL);
		c.setSubJSON(root);
		return c;
	}
	
	public static Command createNewWindowRedirectTo(String redirectURL) {
		JSONObject root = new JSONObject();
		try {
			root.put("nwrurl", redirectURL);
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.NEW_WINDOW);
		c.setSubJSON(root);
		return c;
	}
	
	public static Command createCloseWindow() {
		return createNewWindowCancelRedirectTo();
	}
	
	public static Command createNewWindowCancelRedirectTo() {
		JSONObject root = new JSONObject();
		try {
			root.put("nwrurl", "close-window");
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.NEW_WINDOW);
		c.setSubJSON(root);
		return c;
	}
	
	/**
	 * command to replace sub tree of the dom with html-fragments and execute the script-tags of the fragments
	 * @return
	 */
	public static Command createDirtyComponentsCommand() {
		return new Command(InvokeIdentifier.DIRTY);
	}
	
	/**
	 * command to calculate the needed js lib to add, the needed css to include, and the needed css to hide/remove
	 * @return
	 */
	public static Command createJSCSSCommand() {
		return new Command(InvokeIdentifier.JS_CSS);
	}
	

	/**
	 * - resets the js flag which is set when the user changes form data and is checked when an other link is clicked.(to prevent form data loss).<br>
	 * @return the command 
	 */
	public static Command createPrepareClientCommand(String businessControlPath) {
		JSONObject root = new JSONObject();
		try {
			root.put("bc", businessControlPath==null? "":businessControlPath);
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.PREPARE_CLIENT);
		c.setSubJSON(root);
		return c;		
	}

	/**
	 * @param res
	 * @return
	 */
	public static Command createParentRedirectForExternalResource(String redirectMapperURL) {
		JSONObject root = new JSONObject();
		try {
			root.put("rurl", redirectMapperURL);
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.REDIRECT_URL);
		c.setSubJSON(root);
		return c;
	}
	
	public static Command createDownloadMediaResource(UserRequest ureq, MediaResource resource) {
		JSONObject root = new JSONObject();
		try {
			MediaResourceMapper extMRM = new MediaResourceMapper(resource);
			String mapperId = "cmd-download-" + ureq.getUuid() + "-" + CodeHelper.getForeverUniqueID();
			MapperKey mapperKey = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), mapperId, extMRM, 3000);
			String resUrl = mapperKey.getUrl() + "/";
			root.put("rurl", resUrl);
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.REDIRECT_URL);
		c.setSubJSON(root);
		return c;
	}
	
	public static Command createDownloadMediaResourceAsync(UserRequest ureq, String filename, MediaResource resource) {
		try {
			MediaResourceMapper extMRM = new MediaResourceMapper(resource);
			String mapperId = "cmd-download-" + ureq.getUuid() + "-" + CodeHelper.getForeverUniqueID();
			MapperKey mapperKey = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), mapperId, extMRM, 3000);
			String resUrl = Settings.createServerURI() + mapperKey.getUrl() + "/" + filename;
			return new DownloadURLCommand(filename, resUrl);
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
	}
	
	/**
	 * @param res
	 * @return
	 */
	public static Command createScrollTop() {
		JSONObject root = new JSONObject();
		try {
			root.put("rscroll", "top");
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.SCROLL);
		c.setSubJSON(root);
		return c;
	}
	
	public static Command createDirtyForm(Form form) {
		JSONObject root = new JSONObject();
		try {
			root.put("dispatchFieldId", form.getDispatchFieldId());
			root.put("hideDirtyMarking", form.isHideDirtyMarkingMessage());
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.DIRTY_FORM);
		c.setSubJSON(root);
		return c;
	}
	
	public static Command createLightBoxFocus() {
		JSONObject root = new JSONObject();
		try {
			root.put("type", "lightbox");
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.FOCUS);
		c.setSubJSON(root);
		return c;
	}
	
	public static Command createFlexiFocus(String formName, String formItemId) {
		JSONObject root = new JSONObject();
		try {
			root.put("type", "flexi");
			root.put("formName", formName);
			root.put("formItemId", formItemId);
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
		Command c = new Command(InvokeIdentifier.FOCUS);
		c.setSubJSON(root);
		return c;
	}
}


