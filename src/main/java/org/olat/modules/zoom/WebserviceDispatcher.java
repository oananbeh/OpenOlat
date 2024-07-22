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
package org.olat.modules.zoom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is a handler of a callback issued by Zoom when you use the Zoom LTI Pro App.
 * Zoom uses this callback to notify the LTI consumer (in this case OpenOLAT) of
 * scheduled meetings.
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 *
 */
@Service(value="webservicedispatcherbean")
public class WebserviceDispatcher implements Dispatcher {

    private static final Logger log = Tracing.createLoggerFor(WebserviceDispatcher.class);

    @Autowired
    private CalendarModule calendarModule;
    @Autowired
    private CalendarManager calendarManager;
    @Autowired
    private ZoomManager zoomManager;
    @Autowired
    private ZoomModule zoomModule;

    static class ZoomCalendarPayload {
        String token;
        List<ZoomCalendarEvent> events;

        ZoomCalendarPayload(HttpServletRequest request) {
            token = request.getParameter("wstoken");

            int i = 0;
            events = new ArrayList<>();
            while (true) {
                String prefix = "events[" + i + "]";
                String courseIdKey = prefix + "[courseid]";
                if (request.getParameter(courseIdKey) != null) {
                    ZoomCalendarEvent event = new ZoomCalendarEvent(request, i);
                    events.add(event);
                    i++;
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
        String requestUri = request.getRequestURI();
        String commandUri = requestUri.substring(uriPrefix.length());
        log.debug("Method: " + request.getMethod() + ", URI prefix: " + uriPrefix + ", request URI: " + requestUri);
        if ("rest/server.php".equals(commandUri)) {
            ServletUtil.printOutRequestHeaders(request);
            ServletUtil.printOutRequestParameters(request);
            ZoomCalendarPayload payload = new ZoomCalendarPayload(request);
            for (ZoomCalendarEvent event : payload.events) {
                processEvent(event, payload.token);
            }
        }
    }

    private void processEvent(ZoomCalendarEvent event, String token) {
        if (!zoomModule.isCalendarEntriesEnabled()) {
            log.debug("Not processing Zoom calendar event for meeting " + event.getMeetingId() + ". Setting calendar entries by Zoom is disabled.");
            return;
        }

        Optional<ZoomConfig> optionalZoomConfig = zoomManager.getConfig(event.contextId);
        if (optionalZoomConfig.isPresent()) {
            ZoomConfig zoomConfig = optionalZoomConfig.get();
            if (zoomConfig.getProfile().getToken().equals(token)) {
                LTI13Context ltiContext = zoomConfig.getLtiContext();
                if (ltiContext.getEntry() != null) {
                    ICourse course = CourseFactory.loadCourse(ltiContext.getEntry());
                    Kalendar calendar = calendarManager.getCalendar(CalendarManager.TYPE_COURSE, course.getResourceableId().toString());
                    addCalendarEvent(calendar, event, zoomConfig, course, null);
                }
                if (ltiContext.getBusinessGroup() != null) {
                    Kalendar calendar = calendarManager.getCalendar(CalendarManager.TYPE_GROUP, ltiContext.getBusinessGroup().getResourceableId().toString());
                    addCalendarEvent(calendar, event, zoomConfig, null, ltiContext.getBusinessGroup());
                }
            }
        }
    }

    private void addCalendarEvent(Kalendar calendar, ZoomCalendarEvent event, ZoomConfig zoomConfig, ICourse course, BusinessGroup businessGroup) {
        CalendarManagedFlag[] managedFlags = { CalendarManagedFlag.all };
        if (!calendarModule.isManagedCalendars()) {
            calendarModule.setManagedCalendars(true);
        }

        KalendarEvent existingEvent = calendar.getEvent(event.getMeetingId(), null);
        if (existingEvent != null) {
            existingEvent.setDescription("");
            existingEvent.setSubject(event.name);
            existingEvent.setBegin(event.getStartDate());
            existingEvent.setEnd(event.getEndDate());
            if (existingEvent.getKalendarEventLinks() == null || existingEvent.getKalendarEventLinks().isEmpty()) {
                KalendarEventLink calendarEventLink = generateEventLink(event, zoomConfig, course, businessGroup);
                if (calendarEventLink != null) {
                    existingEvent.setKalendarEventLinks(List.of(calendarEventLink));
                }
            }
            calendarManager.updateEventFrom(calendar, existingEvent);
            log.debug("Updated calendar event for meeting " + event.getMeetingId());
        } else {
            KalendarEvent calendarEvent = new KalendarEvent(event.getMeetingId(), null, event.name, event.getStartDate(), event.getEndDate());
            calendarEvent.setDescription("");
            calendarEvent.setManagedFlags(managedFlags);
            KalendarEventLink calendarEventLink = generateEventLink(event, zoomConfig, course, businessGroup);
            if (calendarEventLink != null) {
                calendarEvent.setKalendarEventLinks(List.of(calendarEventLink));
            }
            calendarManager.addEventTo(calendar, calendarEvent);
            log.debug("Added calendar event for meeting " + event.getMeetingId());
        }
    }

    private KalendarEventLink generateEventLink(ZoomCalendarEvent event, ZoomConfig zoomConfig, ICourse course, BusinessGroup businessGroup) {
        LTI13Context ltiContext = zoomConfig.getLtiContext();
        if (ltiContext.getEntry() != null) {
            StringBuilder businessPath = new StringBuilder(128);
            businessPath.append("[RepositoryEntry:").append(ltiContext.getEntry().getKey()).append("]");
            String displayName;
            if (zoomConfig.getDescription().contains(ZoomManager.ApplicationType.courseElement.name())) {
                businessPath.append("[CourseNode:").append(ltiContext.getSubIdent()).append("]");
                displayName = course.getRunStructure().getNode(ltiContext.getSubIdent()).getShortName();
                log.debug("Creating calendar link for course element '" + displayName + "' (" + ltiContext.getSubIdent() + ")");
            } else {
                businessPath.append("[zoom:0]");
                displayName = course.getCourseTitle();
                log.debug("Creating calendar link for course tool for course '" + displayName + "' (" + ltiContext.getSubIdent() + ")");
            }
            String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathStrings(businessPath.toString());
            return new KalendarEventLink("zoom", event.getMeetingId(), displayName, url, "o_CourseModule_icon");
        }
        if (ltiContext.getBusinessGroup() != null) {
            String businessPath = "[BusinessGroup:" + businessGroup.getKey() + "][toolzoom:0]";
            String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathStrings(businessPath);
            log.debug("Creating calendar link for group '" + businessGroup.getName() + "' (" + businessGroup.getKey().toString() + ")");
            return new KalendarEventLink("zoom", event.getMeetingId(), businessGroup.getName(), url, "o_icon_group");
        }
        return null;
    }
}
