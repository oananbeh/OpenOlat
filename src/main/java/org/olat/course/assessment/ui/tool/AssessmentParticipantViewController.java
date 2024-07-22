/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.tool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.components.widget.ComponentWidget;
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.TextWidget;
import org.olat.core.gui.components.widget.Widget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.vfs.DownloadeableVFSMediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.FormEvaluationScoreMode;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.AssessmentForm.DocumentWrapper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ms.DocumentsMapper;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.SlidersStatistic;
import org.olat.modules.forms.StepCounts;
import org.olat.modules.forms.model.SliderStatisticImpl;
import org.olat.modules.forms.model.SlidersStatisticImpl;
import org.olat.modules.forms.model.StepCountsBuilder;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.BadgeWidgetController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentParticipantViewController extends BasicController implements Activateable2 {

	private int counter = 0;
	private final VelocityContainer mainVC;
	private WidgetGroup widgetGroup;
	private TextWidget passedWidget;
	private DisplayOrDownloadComponent download;
	
	private final AssessmentEvaluation assessmentEval;
	private final AssessmentConfig assessmentConfig;
	private final AssessmentDocumentsSupplier assessmentDocumentsSupplier;
	private final GradeSystemSupplier gradeSystemSupplier;
	private final FormEvaluationSupplier formEvaluationSupplier;
	private final PanelInfo panelInfo;
	private String mapperUri;
	private final Roles roles;
	
	private Controller docEditorCtrl;
	
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private OpenBadgesManager openBadgesManager;

	public AssessmentParticipantViewController(UserRequest ureq, WindowControl wControl,
			AssessmentEvaluation assessmentEval, AssessmentConfig assessmentConfig,
			AssessmentDocumentsSupplier assessmentDocumentsSupplier, GradeSystemSupplier gradeSystemSupplier,
			FormEvaluationSupplier formEvaluationSupplier, PanelInfo panelInfo) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MSCourseNodeRunController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.assessmentEval = assessmentEval;
		this.assessmentConfig = assessmentConfig;
		this.assessmentDocumentsSupplier = assessmentDocumentsSupplier;
		this.gradeSystemSupplier = gradeSystemSupplier;
		this.formEvaluationSupplier = formEvaluationSupplier;
		this.panelInfo = panelInfo;
		roles = ureq.getUserSession().getRoles();
		
		mainVC = createVelocityContainer("participant_view");
		
		setTitle(translate("personal.title"));
		exposeToVC(ureq);
		
		putInitialPanel(mainVC);
	}

	private void exposeToVC(UserRequest ureq) {
		widgetGroup = WidgetFactory.createWidgetGroup("results", mainVC);
		
		passedWidget = null;
		FigureWidget gradeWidget = null;
		FigureWidget scoreWidget = null;
		Widget scoreWWeightedWidget = null;
		FigureWidget attemptsWidget = null;
		List<Widget> rubricsWidgets = null;
		
		
		boolean resultsVisible = assessmentEval.getUserVisible() != null && assessmentEval.getUserVisible().booleanValue();
		mainVC.contextPut("resultsVisible", resultsVisible);
		
		// Attempts
		boolean hasAttempts = assessmentConfig.hasAttempts();
		if (hasAttempts) {
			attemptsWidget = WidgetFactory.createFigureWidget("attempts", null, translate("attempts.yourattempts"), "o_icon_attempts");
			attemptsWidget.setValueCssClass("o_sel_attempts");
			
			Integer attempts = assessmentEval.getAttempts();
			if (attempts == null) {
				attempts = Integer.valueOf(0);
			}
			attemptsWidget.setValue(String.valueOf(attempts));
			if (assessmentConfig.hasMaxAttempts() && attempts > 0) {
				Integer maxAttempts = assessmentConfig.getMaxAttempts();
				attemptsWidget.setDesc(translate("attempts.of", String.valueOf(maxAttempts)));
			}
		}
		
		// Score
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		ProgressBar scoreProgress = null;
		if (hasScore) {
			scoreWidget = WidgetFactory.createFigureWidget("score", null, translate("score"), "o_icon_score");
			scoreWidget.setValueCssClass("o_sel_score");
			
			String scoreFormatted;
			int progress;
			if (resultsVisible && assessmentEval.getScore() != null) {
				scoreFormatted = AssessmentHelper.getRoundedScore(assessmentEval.getScore());
				progress = assessmentEval.getScore() != null? assessmentEval.getScore().intValue(): 0;
			} else {
				scoreFormatted = translate("assessment.value.not.visible");
				progress = 0;
			}
			scoreWidget.setValue(scoreFormatted);
			
			Float maxScore = assessmentConfig.getMaxScore();
			if (maxScore != null && maxScore > 0) {
				scoreWidget.setDesc(translate("score.of", AssessmentHelper.getRoundedScore(maxScore)));
				
				scoreProgress = new ProgressBar("scoreProgress", 100, progress, maxScore, null);
				scoreProgress.setWidthInPercent(true);
				scoreProgress.setLabelAlignment(LabelAlignment.none);
				scoreProgress.setRenderSize(RenderSize.small);
				scoreProgress.setLabelMaxEnabled(false);
				scoreWidget.setAdditionalComp(scoreProgress);
				scoreWidget.setAdditionalCssClass("o_widget_progress");
			}
			
			BigDecimal scoreScale = assessmentEval.getScoreScale();
			if(scoreScale != null && assessmentConfig.isScoreScalingEnabled()
					&& !ScoreScalingHelper.equals(BigDecimal.ONE, scoreScale)) {
				
				if (resultsVisible) {
					
					String scale = assessmentConfig.getScoreScale();
					String i18nLabel =  ScoreScalingHelper.isFractionScale(scale)
							? "score.weighted.fraction" : "score.weighted.decorated";
					scoreWWeightedWidget = WidgetFactory.createFigureWidget("scoreWeighted", null,
							translate("score"), translate("score.weighted.subtitle"), "o_icon_score_unbalanced",
							AssessmentHelper.getRoundedScore(assessmentEval.getWeightedScore()), null,
							translate(i18nLabel, scale), null, null, null);
				} else {
					scoreWWeightedWidget = WidgetFactory.createTextWidget("scoreWeighted", null, translate("score"),
							translate("score.weighted.subtitle"), "o_icon_score_unbalanced",
							translate("assessment.value.not.visible"), null, null, null, null);
				}
			}
		}
		
		// Rubrics
		boolean hasFormEvaluation = assessmentConfig.hasFormEvaluation();
		if(hasFormEvaluation) {
			List<RubricValue> statistics = formEvaluationSupplier.getRubricStatistics(assessmentConfig.getFormEvaluationScoreMode());
			rubricsWidgets = new ArrayList<>(statistics.size());
			
			for(int i=0; i<statistics.size(); i++) {
				RubricValue rubricValue = statistics.get(i);
				Double maxScoreRubric = rubricValue.maxScore();
				Double score = rubricValue.score();
				
				String name;
				boolean showName = rubricValue.rubric().getNameDisplays().contains(NameDisplay.report)
						&& StringHelper.containsNonWhitespace(rubricValue.rubric().getName());
				if(showName) {
					name = translate("form.evaluation.rubric.score.named", rubricValue.rubric().getName());
				} else {
					name = translate("form.evaluation.rubric.score", Integer.toString(i + 1));
				}
				FigureWidget rubricWidget = WidgetFactory.createFigureWidget("rubric_" + i, null, name, "o_icon_score");
				rubricWidget.setDesc(translate("score.of", AssessmentHelper.getRoundedScore(maxScoreRubric)));
				
				float progress = 0.0f;
				if(resultsVisible) {
					rubricWidget.setValue(AssessmentHelper.getRoundedScore(score));
					progress = score.floatValue();
				} else {
					rubricWidget.setValue(translate("assessment.value.not.visible"));
				}
				
				ProgressBar rubricProgress = new ProgressBar("scoreProgress_" + i, 100, progress, maxScoreRubric.floatValue(), null);
				rubricProgress.setWidthInPercent(true);
				rubricProgress.setLabelAlignment(LabelAlignment.none);
				rubricProgress.setRenderSize(RenderSize.small);
				rubricProgress.setLabelMaxEnabled(false);
				rubricProgress.setBarColor(BarColor.passed);
				rubricWidget.setAdditionalComp(rubricProgress);
				rubricWidget.setAdditionalCssClass("o_widget_progress");
				rubricsWidgets.add(rubricWidget);
			}
		}
		
		// Grade
		boolean hasGrade = hasScore && assessmentConfig.hasGrade() && gradeModule.isEnabled();
		if (hasGrade) {
			String gradeSystemident = StringHelper.containsNonWhitespace(assessmentEval.getGradeSystemIdent())
					? assessmentEval.getGradeSystemIdent()
					: gradeSystemSupplier.getGradeSystem().getIdentifier();
			String translatePerformanceClass = GradeUIFactory.translatePerformanceClass(getTranslator(), 
					assessmentEval.getPerformanceClassIdent(), assessmentEval.getGrade(), assessmentEval.getGradeSystemIdent());
			
			gradeWidget = WidgetFactory.createFigureWidget("frade", null,
					GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeSystemident), "o_icon_grade");
			if (resultsVisible && StringHelper.containsNonWhitespace(translatePerformanceClass)) {
				gradeWidget.setValue(translatePerformanceClass);
			} else {
				gradeWidget.setValue(translate("assessment.value.not.visible"));
			}
		}
		
		// Passed
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		if (hasPassed) {
			passedWidget = WidgetFactory.createTextWidget("passed", null, translate("passed.success.status"), "o_icon_success_status");
			if (resultsVisible) {
				if (assessmentEval.getPassed() == null) {
					passedWidget.setValue(translate("passed.nopassed"));
					passedWidget.setValueCssClass("o_noinfo");
				} else if (assessmentEval.getPassed()) {
					passedWidget.setValue(translate("passed.yes"));
					passedWidget.setValueCssClass("o_state o_passed");
				} else {
					passedWidget.setValue(translate("passed.no"));
					passedWidget.setValueCssClass("o_state o_failed");
				}
			} else {
				passedWidget.setValue(translate("assessment.value.not.visible"));
			}
			
			if (!hasGrade && assessmentConfig.getCutValue() != null) {
				passedWidget.setAdditionalText(translate("passed.cut.from", AssessmentHelper.getRoundedScore(assessmentConfig.getCutValue())));
			}
			if (scoreProgress != null && assessmentEval.getPassed() != null) {
				if (assessmentEval.getPassed().booleanValue()) {
					scoreProgress.setBarColor(BarColor.passed);
				} else {
					scoreProgress.setBarColor(BarColor.failed);
				}
			}
		}
		
		// Status
		boolean hasStatus = AssessmentEntryStatus.inReview == assessmentEval.getAssessmentStatus()
				|| AssessmentEntryStatus.done == assessmentEval.getAssessmentStatus();
		mainVC.contextPut("hasStatusField", Boolean.valueOf(hasStatus));
		if (hasStatus) {
			String statusText = null;
			String statusIconCss = null;
			String statusLabelCss = null;
			if (AssessmentEntryStatus.done == assessmentEval.getAssessmentStatus()) {
				if (resultsVisible) {
					statusText = translate("assessment.status.done");
					statusIconCss = "o_icon_status_done";
					statusLabelCss = "o_results_visible";
				} else {
					statusText = translate("in.release");
					statusIconCss = "o_icon_status_in_review";
					statusLabelCss = "o_results_hidden";
				}
			} else {
				statusText = translate("in.review");
				statusIconCss = "o_icon_status_in_review";
				statusLabelCss = "o_results_hidden";
			}
			mainVC.contextPut("statusText", statusText);
			mainVC.contextPut("statusIconCss", statusIconCss);
			mainVC.contextPut("statusLabelCss", statusLabelCss);
		}
		
		// Comments for participant
		String rawComment = assessmentEval.getComment();
		boolean hasComment = assessmentConfig.hasComment() && StringHelper.containsNonWhitespace(rawComment);
		if (hasComment) {
			StringBuilder comment = Formatter.stripTabsAndReturns(rawComment);
			if (comment != null && !comment.isEmpty()) {
				mainVC.contextPut("comment", StringHelper.xssScan(comment));
				mainVC.contextPut("incomment", isPanelOpen(ureq, "comment", true));
			}
		}
		
		// Assessment documents
		if (assessmentConfig.hasIndividualAsssessmentDocuments()) {
			List<VFSLeaf> documents = assessmentDocumentsSupplier.getIndividualAssessmentDocuments();
			VelocityContainer docsVC = createVelocityContainer("individual_assessment_docs");
			List<DocumentWrapper> wrappers = new ArrayList<>(documents.size());
			for (VFSLeaf document : documents) {
				wrappers.add(createDocumentWrapper(document, docsVC));
			}
			
			mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(documents));
			mainVC.contextPut("docs", docsVC);
			mainVC.contextPut("inassessmentDocuments", isPanelOpen(ureq, "assessmentDocuments", true));
			docsVC.contextPut("mapperUri", mapperUri);
			docsVC.contextPut("documents", wrappers);
			docsVC.setVisible(!documents.isEmpty());
			mainVC.put("docs", docsVC);
			
			if (assessmentDocumentsSupplier.isDownloadEnabled() && download == null) {
				download = new DisplayOrDownloadComponent("", null);
				mainVC.put("download", download);
			}
		}
		
		widgetGroup.add(passedWidget);
		widgetGroup.add(gradeWidget);
		widgetGroup.add(scoreWidget);
		widgetGroup.add(scoreWWeightedWidget);
		widgetGroup.add(attemptsWidget);
		widgetGroup.addAll(rubricsWidgets);
		widgetGroup.add(createBadgesWidget(ureq));
	}

	private ComponentWidget createBadgesWidget(UserRequest ureq) {
		Identity assessedIdentity = getIdentity();
		RepositoryEntryRef courseEntry = null;
		String courseNodeIdent = null;
		boolean myBadges = true;
		if (formEvaluationSupplier != null) {
			assessedIdentity = formEvaluationSupplier.getAssessedIdentity();
			courseEntry = formEvaluationSupplier.getCourseEntry();
			courseNodeIdent = formEvaluationSupplier.getCourseNode().getIdent();
			myBadges = false;
		}
		if (gradeSystemSupplier != null) {
			courseEntry = gradeSystemSupplier.getCourseEntry();
			courseNodeIdent = gradeSystemSupplier.getSubIdent();
		}
		List<BadgeAssertion> ruleEarnedBadgeAssertions = openBadgesManager.getRuleEarnedBadgeAssertions(
				assessedIdentity, courseEntry, courseNodeIdent);
		if (!ruleEarnedBadgeAssertions.isEmpty()) {
			ComponentWidget badgeWidget = WidgetFactory.createComponentWidget("badges", null, "Badges", "o_icon_badge");
			BadgeWidgetController badgeWidgetController = new BadgeWidgetController(ureq, getWindowControl(),
					courseEntry, ruleEarnedBadgeAssertions, myBadges);
			addControllerListener(badgeWidgetController);
			badgeWidget.setContent(badgeWidgetController.getInitialComponent());
			badgeWidget.setMainCss("o_badge_widget_main");
			return badgeWidget;
		}

		return null;
	}

	private DocumentWrapper createDocumentWrapper(VFSLeaf document, VelocityContainer docsVC) {
		String initializedBy = null;
		Date creationDate = null;
		VFSMetadata metadata = document.getMetaInfo();
		if(metadata != null) {
			creationDate = metadata.getCreationDate();
			Identity identity = metadata.getFileInitializedBy();
			if(identity != null) {
				initializedBy = userManager.getUserDisplayName(identity);
			}
		}
		DocumentWrapper wrapper = new DocumentWrapper(document, initializedBy, creationDate);
		
		DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, document,
				metadata, true, DocEditorService.modesEditView(false));
		Link openLink;
		if(editorInfo != null && editorInfo.isEditorAvailable()) {
			openLink = LinkFactory.createLink("openfile" + (++counter), "open", getTranslator(), docsVC, this, Link.LINK | Link.NONTRANSLATED);
			
			if (editorInfo.isNewWindow()) {
				openLink.setNewWindow(true, true);
			}
		} else {
			openLink = LinkFactory.createLink("openfile" + (++counter), "download", getTranslator(), docsVC, this, Link.LINK | Link.NONTRANSLATED);
		}
		openLink.setCustomDisplayText(wrapper.getFilename());
		wrapper.setOpenLink(openLink);
		
		Link downloadLink = LinkFactory.createCustomLink("download_" + (++counter), "download", "", Link.BUTTON | Link.NONTRANSLATED, docsVC, this);
		downloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		downloadLink.setGhost(true);
		downloadLink.setTarget("_blank");
		wrapper.setDownloadLink(downloadLink);
		
		return wrapper;
	}
	
	public void setTitle(String title) {
		mainVC.contextPut("title", title);
	}
	
	public void setPassedProgress(Component passedProgress) {
		if (passedWidget != null) {
			passedWidget.setLeftComp(passedProgress);
		}
	}
	
	public void addCustomWidget(Widget widget) {
		if (widgetGroup != null) {
			widgetGroup.add(widget);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(type.startsWith("path")) {
			if(download != null) {
				String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
				String url = mapperUri + "/" + path;
				download.triggerFileDownload(url);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == docEditorCtrl) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(docEditorCtrl);
		docEditorCtrl = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("show".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), true);
		} else if ("hide".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), false);
		} else if(source instanceof Link link && link.getUserObject() instanceof DocumentWrapper wrapper) {
			if("download".equals(link.getCommand())) {
				ureq.getDispatchResult()
					.setResultingMediaResource(new DownloadeableVFSMediaResource(wrapper.getDocument()));
			} else if("open".equals(link.getCommand())) {
				doOpenDocument(ureq, wrapper);
			}
		}
	}
	
	private boolean isPanelOpen(UserRequest ureq, String panelId, boolean showDefault) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(panelInfo.attributedClass(), getOpenPanelId(panelId));
		return showConfig == null ? showDefault : showConfig.booleanValue();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(panelInfo.attributedClass(), getOpenPanelId(panelId), Boolean.valueOf(newValue));
		}
	}
	
	private String getOpenPanelId(String panelId) {
		return panelId + panelInfo.idSuffix();
	}
	
	private void doOpenDocument(UserRequest ureq, DocumentWrapper wrapper) {
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withVersionControlled(false)
				.withMode(DocEditor.Mode.VIEW)
				.build(wrapper.getDocument());
		docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.modesEditView(false)).getController();
		listenTo(docEditorCtrl);
	}

	public record PanelInfo(Class<?> attributedClass, String idSuffix) { }
	
	public interface GradeSystemSupplier {
		
		public GradeSystem getGradeSystem();
		public RepositoryEntryRef getCourseEntry();
		public String getSubIdent();
	}
	
	public static GradeSystemSupplier gradeSystem(UserCourseEnvironment userCourseEnv, CourseNode courseNode) {
		return new DefaultGradeSystemSupplier(userCourseEnv, courseNode);
	}
	
	private static final class DefaultGradeSystemSupplier implements GradeSystemSupplier {
		
		private final RepositoryEntryRef courseEntry;
		private final String subIdent;
		
		private DefaultGradeSystemSupplier(UserCourseEnvironment userCourseEnv, CourseNode courseNode) {
			this.courseEntry = new CourseEntryRef(userCourseEnv);
			this.subIdent = courseNode.getIdent();
		}

		@Override
		public GradeSystem getGradeSystem() {
			return CoreSpringFactory.getImpl(GradeService.class).getGradeSystem(courseEntry, subIdent);
		}

		@Override
		public RepositoryEntryRef getCourseEntry() {
			return courseEntry;
		}

		@Override
		public String getSubIdent() {
			return subIdent;
		}
	}
	
	public interface AssessmentDocumentsSupplier {
		
		public List<VFSLeaf> getIndividualAssessmentDocuments();
		
		public boolean isDownloadEnabled();
		
	}
	
	public interface FormEvaluationSupplier {
		
		public List<RubricValue> getRubricStatistics(FormEvaluationScoreMode scoreMode);
		public Identity getAssessedIdentity();
		public RepositoryEntry getCourseEntry();
		public CourseNode getCourseNode();
	}
	
	public static FormEvaluationSupplier formEvaluation(UserCourseEnvironment userCourseEnv, CourseNode courseNode, AssessmentConfig assessmentConfig) {
		if(assessmentConfig.hasAssessmentForm()) {
			return new DefaultFormEvaluationSupplier(userCourseEnv, courseNode, assessmentConfig);
		}
		return new NoFormEvaluationSupplier();
	}

	public static class NoFormEvaluationSupplier implements FormEvaluationSupplier {

		@Override
		public List<RubricValue> getRubricStatistics(FormEvaluationScoreMode scoreMode) {
			return List.of();
		}

		@Override
		public Identity getAssessedIdentity() {
			return null;
		}

		@Override
		public RepositoryEntry getCourseEntry() {
			return null;
		}

		@Override
		public CourseNode getCourseNode() {
			return null;
		}
	}
	
	public static class DefaultFormEvaluationSupplier implements FormEvaluationSupplier {
		
		private final Identity assessedIdentity;
		private final RepositoryEntry courseEntry;
		private final CourseNode courseNode;
		private final float scale;
		
		private DefaultFormEvaluationSupplier(UserCourseEnvironment userCourseEnv, CourseNode courseNode, AssessmentConfig assessmentConfig) {
			this.courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			this.courseNode = courseNode;
			
			String scaleConfig = assessmentConfig.getFormEvaluationScoreScale();
			if(StringHelper.containsNonWhitespace(scaleConfig)) {
				scale = Float.parseFloat(scaleConfig);
			} else {
				scale = 1.0f;
			}

			assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		}

		@Override
		public Identity getAssessedIdentity() {
			return assessedIdentity;
		}

		@Override
		public RepositoryEntry getCourseEntry() {
			return courseEntry;
		}

		@Override
		public CourseNode getCourseNode() {
			return courseNode;
		}

		@Override
		public List<RubricValue> getRubricStatistics(FormEvaluationScoreMode scoreMode) {
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
			EvaluationFormSession session = courseAssessmentService.getSession(courseEntry, courseNode, assessedIdentity);
			if(session != null) {
				List<RubricStatistic> statsList = evaluationFormManager.getRubricStatistics(session);
				List<RubricValue> values = new ArrayList<>(statsList.size());
				for(RubricStatistic stats:statsList) {
					Rubric rubric = stats.getRubric();
					SlidersStatistic slidersStatistics = getSlidersStatistic(rubric);
					RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, slidersStatistics);
	
					Double score = null;
					Double maxScore = null;
					if(scoreMode == FormEvaluationScoreMode.sum) {
						score = stats.getTotalStatistic().getSum();
						maxScore = rubricStatistic.getTotalStatistic().getSum();
					} else if(scoreMode == FormEvaluationScoreMode.avg) {
						score = stats.getTotalStatistic().getAvg();
						maxScore = rubricStatistic.getTotalStatistic().getAvg();
					}
					
					if(maxScore != null) {
						double dScore = score == null ? 0.0d : score.doubleValue();
						double scaledScore = dScore * scale;
						double scaledMaxScore = maxScore.doubleValue() * scale;
						values.add(new RubricValue(scaledScore, scaledMaxScore, rubric));
					}
				}
				return values;
			}
			return List.of();
		}
		
		private SlidersStatistic getSlidersStatistic(Rubric rubric) {
			SlidersStatisticImpl slidersStatisticImpl = new SlidersStatisticImpl();
			int step = rubric.getSteps();
			for (Slider slider : rubric.getSliders()) {
				StepCounts stepCounts = StepCountsBuilder.builder(rubric.getSteps())
						.withCount(step, Long.valueOf(1))
						.build();
				SliderStatistic sliderStatistic = new SliderStatisticImpl(null, null, null, null, null, null, stepCounts, null);
				slidersStatisticImpl.put(slider, sliderStatistic);
			}
			return slidersStatisticImpl;
		}
	}
	
	public record RubricValue(Double score, Double maxScore, Rubric rubric) {
		//
	}
}
