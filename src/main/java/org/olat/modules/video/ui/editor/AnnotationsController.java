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
package org.olat.modules.video.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractFlexiTableRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.ui.marker.VideoMarkerRowComparator;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AnnotationsController extends BasicController {
	public static final Event RELOAD_MARKERS_EVENT = new Event("video.edit.reload.markers");
	private final VelocityContainer mainVC;
	private final RepositoryEntry repositoryEntry;
	private final AnnotationsHeaderController annotationsHeaderController;
	private final AnnotationController annotationController;
	private VideoMarkers annotations;
	private VideoMarker annotation;
	@Autowired
	private VideoManager videoManager;

	public AnnotationsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
								 long videoDurationInSeconds, String videoElementId) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;
		mainVC = createVelocityContainer("annotations");

		annotations = videoManager.loadMarkers(repositoryEntry.getOlatResource());
		annotation = annotations.getMarkers().stream().min(new VideoMarkerRowComparator()).orElse(null);

		annotationsHeaderController = new AnnotationsHeaderController(ureq, wControl, repositoryEntry);
		annotationsHeaderController.setAnnotations(annotations);
		listenTo(annotationsHeaderController);
		mainVC.put("header", annotationsHeaderController.getInitialComponent());

		annotationController = new AnnotationController(ureq, wControl, annotation, videoDurationInSeconds,
				videoElementId);
		listenTo(annotationController);
		if (annotation != null) {
			mainVC.put("annotation", annotationController.getInitialComponent());
		} else {
			mainVC.remove("annotation");
		}

		Translator tableTranslator = Util.createPackageTranslator(AbstractFlexiTableRenderer.class, ureq.getLocale());
		EmptyStateConfig emptyStateConfig = EmptyStateConfig
				.builder()
				.withIconCss("o_icon_empty_objects")
				.withIndicatorIconCss("o_icon_empty_indicator")
				.withMessageTranslated(tableTranslator.translate("default.tableEmptyMessage"))
				.build();
		EmptyStateFactory.create("emptyState", mainVC, this, emptyStateConfig);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (annotationController == source) {
			if (event == Event.DONE_EVENT) {
				annotation = annotationController.getAnnotation();
				videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
				annotationsHeaderController.setAnnotations(annotations);
				reloadMarkers(ureq);
				fireEvent(ureq, new AnnotationSelectedEvent(annotation.getId(), annotation.getBegin().getTime(),
						annotation.getDuration()));
			}
		} else if (annotationsHeaderController == source) {
			if (event instanceof AnnotationSelectedEvent annotationSelectedEvent) {
				annotations.getMarkers().stream()
						.filter(a -> a.getId().equals(annotationSelectedEvent.getId()))
						.findFirst().ifPresent(a -> {
							annotationController.setAnnotation(a);
							fireEvent(ureq, annotationSelectedEvent);
						});
			} else if (event == AnnotationsHeaderController.ANNOTATION_ADDED_EVENT ||
					event == AnnotationsHeaderController.ANNOTATION_DELETED_EVENT) {
				this.annotations = annotationsHeaderController.getAnnotations();
				String newAnnotationId = annotationsHeaderController.getAnnotationId();
				showAnnotation(newAnnotationId);
				annotationController.setAnnotation(annotation);
				videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
				reloadMarkers(ureq);
				if (annotation != null) {
					fireEvent(ureq, new AnnotationSelectedEvent(annotation.getId(), annotation.getBegin().getTime(),
							annotation.getDuration()));
				} else {
					fireEvent(ureq, new SetTypeEvent(TimelineEventType.ANNOTATION));
				}
			}
		}

		super.event(ureq, source, event);
	}

	private void reloadMarkers(UserRequest ureq) {
		fireEvent(ureq, RELOAD_MARKERS_EVENT);
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		annotationsHeaderController.setCurrentTimeCode(currentTimeCode);
	}

	public void showAnnotation(String annotationId) {
		this.annotation = annotations.getMarkerById(annotationId);
		if (annotation != null) {
			annotationsHeaderController.setAnnotationId(annotation.getId());
			annotationController.setAnnotation(annotation);
			mainVC.put("annotation", annotationController.getInitialComponent());
		} else {
			annotationsHeaderController.setAnnotationId(null);
			mainVC.remove("annotation");
		}
	}

	public void handleDeleted(String annotationId) {
		annotationsHeaderController.handleDeleted(annotationId);
		String currentAnnotationId = annotationsHeaderController.getAnnotationId();
		showAnnotation(currentAnnotationId);
	}

	public void setAnnotationSize(String annotationId, double width, double height) {
		showAnnotation(annotationId);

		if (annotation == null) {
			return;
		}
		annotation.setWidth(width);
		annotation.setHeight(height);
		videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
		annotationController.setAnnotation(annotation);
	}

	public void setAnnotationPosition(String annotationId, double top, double left) {
		showAnnotation(annotationId);

		if (annotation == null) {
			return;
		}
		annotation.setTop(top);
		annotation.setLeft(left);
		videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
		annotationController.setAnnotation(annotation);
	}

	public void sendSelectionEvent(UserRequest ureq) {
		if (annotation != null) {
			fireEvent(ureq, new AnnotationSelectedEvent(annotation.getId(), annotation.getBegin().getTime(),
					annotation.getDuration()));
		} else {
			fireEvent(ureq, new SetTypeEvent(TimelineEventType.ANNOTATION));
		}
	}
}
