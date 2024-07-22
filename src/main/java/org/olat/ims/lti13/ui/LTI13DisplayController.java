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
package org.olat.ims.lti13.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.ui.LTIDisplayContentController;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13ContentItemPresentationEnum;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.springframework.beans.factory.annotation.Autowired;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

/**
 * 
 * Initial date: 17 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13DisplayController extends BasicController implements LTIDisplayContentController {

	private Link back;
	private final VelocityContainer mainVC;

	private final String frmConnectId;
	private final LTI13Context ltiContext;
	private final LTI13ContentItem contentItem;
	private final LTI13ToolDeployment toolDeployment;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13DisplayController(UserRequest ureq, WindowControl wControl,
			LTI13Context context, LTI13ContentItem contentItem, boolean inList, UserCourseEnvironment userCourseEnv) {
		this(ureq, wControl, context, contentItem, inList, userCourseEnv.isAdmin(), userCourseEnv.isCoach(), userCourseEnv.isParticipant());
	}

	public LTI13DisplayController(UserRequest ureq, WindowControl wControl, LTI13Context context, LTI13ContentItem contentItem,
			boolean inList, boolean admin, boolean coach, boolean participant) {
		super(ureq, wControl);
		this.ltiContext = context;
		this.contentItem = contentItem;
		toolDeployment = context.getDeployment();
		frmConnectId = "frmConnect" + CodeHelper.getRAMUniqueID();
		
		mainVC = createVelocityContainer("launch");
		mainVC.contextPut("inList", Boolean.valueOf(inList));
		mainVC.contextPut("frmConnect", frmConnectId);

		String loginHint = loginHint(admin, coach, participant);
		initViewSettings();
		initLaunch(loginHint);

		putInitialPanel(mainVC);
	}

	private void initLaunch(String loginHint) {
		// launch data
		LTI13Tool tool = toolDeployment.getTool();
		String targetLinkUri = tool.getToolUrl();
		if(contentItem != null) {
			if(StringHelper.containsNonWhitespace(contentItem.getIframeSrc())) {
				targetLinkUri = contentItem.getIframeSrc();
			} else {
				targetLinkUri = contentItem.getUrl();
			}
		} else if(StringHelper.containsNonWhitespace(ltiContext.getTargetUrl())) {
			targetLinkUri = ltiContext.getTargetUrl();
		} else if(StringHelper.containsNonWhitespace(toolDeployment.getTargetUrl())) {
			targetLinkUri = toolDeployment.getTargetUrl();
		}
		mainVC.contextPut("initiateLoginUrl", tool.getInitiateLoginUrl());
		mainVC.contextPut("iss", lti13Module.getPlatformIss());
		mainVC.contextPut("target_link_uri", targetLinkUri);
		mainVC.contextPut("login_hint", loginHint);
		mainVC.contextPut("lti_message_hint", getIdentity().getKey().toString());
		mainVC.contextPut("client_id", tool.getClientId());
		mainVC.contextPut("lti_deployment_id", toolDeployment.getDeploymentId());
	}
	
	private void initViewSettings() {
		final LTIDisplayOptions displayOption = ltiContext.getDisplayOptions();
		if(displayOption == LTIDisplayOptions.fullscreen) {
			back = LinkFactory.createLinkBack(mainVC, this);
		}
		
		Boolean newWindow = newWindow(contentItem, ltiContext);
		mainVC.contextPut("newWindow", newWindow);
		
		String iframeHeight = null;
		String iframeWidth = null;
		if(contentItem != null) {
			iframeWidth = contentItem.getIframeWidth() == null ? null : contentItem.getIframeWidth().toString();
			iframeHeight = contentItem.getIconHeight() == null ? null : contentItem.getIconHeight().toString();
		}
		if(iframeHeight == null && ltiContext.getDisplayHeight() != null && "auto".equals(ltiContext.getDisplayHeight())) {
			iframeHeight = ltiContext.getDisplayHeight();
		}
		if(iframeWidth == null && ltiContext.getDisplayWidth() != null && "auto".equals(ltiContext.getDisplayWidth())) {
			iframeWidth = ltiContext.getDisplayWidth();
		}
		
		if(iframeHeight != null) {
			mainVC.contextPut("height", iframeHeight);
		}
		if(iframeWidth != null) {
			mainVC.contextPut("width", iframeWidth);
		}
	}
	
	protected static Boolean newWindow(LTI13ContentItem item, LTI13Context context) {
		final LTIDisplayOptions displayOption = context.getDisplayOptions();
		
		Boolean newWindow;
		if(item != null) {
			if(item.getPresentation() == null) {
				newWindow = Boolean.valueOf(LTIDisplayOptions.window == displayOption);
			} else {
				newWindow = item.getPresentation() == LTI13ContentItemPresentationEnum.window
						&& !StringHelper.containsNonWhitespace(item.getIframeSrc());
			}
		} else {
			newWindow = Boolean.valueOf(LTIDisplayOptions.window == displayOption);
		}
		
		return newWindow;
	}
	
	private String loginHint(boolean admin, boolean coach, boolean participant) {
		LTI13Key platformKey = lti13Service.getLastPlatformKey();
		
		JwtBuilder builder = Jwts.builder()
			.header()
				.type(LTI13Constants.Keys.JWT)
				.add(LTI13Constants.Keys.ALGORITHM, platformKey.getAlgorithm())
				.keyId(platformKey.getKeyId())
			.and()
			.claim("deploymentKey", toolDeployment.getKey())
			.claim("deploymentId", toolDeployment.getDeploymentId())
			.claim("contextKey", ltiContext.getKey())
			.claim("contextId", ltiContext.getContextId())
			.claim("courseadmin", Boolean.valueOf(admin))
			.claim("coach", Boolean.valueOf(coach))
			.claim("participant", Boolean.valueOf(participant));
		if(contentItem != null) {
			builder = builder
					.claim("contentItemKey", contentItem.getKey());
		}
		return builder
				.signWith(platformKey.getPrivateKey())
				.compact();
	}

	@Override
	public void openLtiContent(UserRequest ureq) {
		//
	}

	public void manuallyOpenLtiContentInSeparateWindow() {
		getWindowControl().getWindowBackOffice()
			.sendCommandTo(FunctionCommand.startLti13(frmConnectId));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == back) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}
}
