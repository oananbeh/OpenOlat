/*
 * ========================================================
 *  <a href="http://www.openolat.org">
 *  OpenOLAT - Online Learning and Training</a><br>
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License"); <br>
 *  you may not use this file except in compliance with the License.<br>
 *  You may obtain a copy of the License at the
 *  <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 *  <p>
 *  Unless required by applicable law or agreed to in writing,<br>
 *  software distributed under the License is distributed on an "AS IS" BASIS, <br>
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 *  See the License for the specific language governing permissions and <br>
 *  limitations under the License.
 *  <p>
 *  Initial code contributed and copyrighted by<br>
 *  28.04.2014 by frentix GmbH, http://www.frentix.com
 *  <p>
 *  @author srosse, www.frentix.com
 *  @date Sept. 2022
 * ========================================================
 */
(function($) {
	"use strict";
    $.fn.ceditor = function(options) {
    	var editor = this.data("data-oo-ceditor");
    	if(typeof editor === "undefined" || editor == null) {
    		editor = new ContentEditor(this.get(0), options);
    		this.data("data-oo-ceditor", editor);
    	} else if(options && options.resetDrake) {
			editor.drake.destroy();
    		editor.drake = initDrake(editor.settings);
		} else {// if the same DOM element exists
			if (editor.initWindowListener) {
				editor.initWindowListener(editor.settings);
			}

			if (editor.initDrake && typeof editor.initDrake === "function") {
				editor.drake = editor.initDrake(editor.settings);
			}
    	}
    	return editor;
	};
	
	var ContentEditor = function(container, params) {
		this.settings = $.extend({
			componentUrl: '',
			csrfToken: ''
		}, params);

		if (!this.settings.csrfToken) {
			if (window.console) {
				console.log('csrf token missing');
			}
		}

		initWindowListener(this.settings);
		this.container = container;
		this.drake = initDrake(this.settings);
	};
	
	function initDrake(settings) {
		var $settings = settings;

		var containers = [];
		jQuery('.o_page_container_slot-inner').each(function(index, slot) {
			containers.push(slot);
		});
		jQuery('.o_legacy_container').each(function(index, slot) {
			containers.push(slot);
		});
		return dragula({
			containers: containers,
			revertOnSpill: true,
			copy: function (el, source) {
				return false;
			},
			accepts: function (el, target) {
				return true;
			},
			moves: function (el, container, handle) {
    			return handle.classList.contains('o_icon_move') || handle.classList.contains('o_page_drag_handle');
  			}
		}).on('over', function (el, container) {
			jQuery(container).addClass('oo-drop-accepted');
		}).on('out', function (el, container) {
			jQuery(container).removeClass('oo-drop-accepted');
		}).on('cloned', function (clone, original, container) {
			var tinyClonedFrame = jQuery(clone).find('.o_richtext_mce iframe');
			var tinyOriginalFrame = jQuery(original).find('.o_richtext_mce iframe');
			if (tinyClonedFrame.length > 0 && tinyOriginalFrame.length > 0) {
				var frameDocument = tinyOriginalFrame.get(0).contentWindow.document;
				tinyClonedFrame.get(0).srcdoc = '<!DOCTYPE html><html>' + frameDocument.head.innerHTML + frameDocument.body.innerHTML + '</html>';
			}
		}).on('drop', function(el, target, source, sibling) {
			drop($settings, el, target, source, sibling);
		}).on('cancel', function(el, container, source) {
			cancel(settings, el, container);
		});
	}
	
	function cancel(settings, el, container) {
		var draggedId = jQuery(el).data('oo-page-fragment');
		var jContainer = jQuery(container);
		if(jContainer.hasClass("o_page_container_slot-inner")) {
			var jContainerSlot = jContainer.closest(".o_page_container_slot");
			var slotId = jContainerSlot.data('oo-slot');
			var componentUrl = jContainerSlot.data("oo-content-editor-url");
			var containerId = jContainerSlot.data('oo-page-fragment');

			o_afterserver();

			o_XHREvent(componentUrl, false, false, "_csrf", settings.csrfToken, "cid", "drop_canceled", "fragment", containerId, "dragged", draggedId, "source", draggedId, "container", containerId, "slot", slotId);
			return true;
		}
	}
	
	function drop(settings, el, targetContainer, sourceContainer, sibling) {
		var position;
		var targetId = null;
		if(sibling) {
			position = "top";
			targetId = jQuery(sibling).data('oo-page-fragment');
		} else {
			position = "bottom";
		}

		var draggedId = jQuery(el).data('oo-page-fragment');
		var jTargetContainer = jQuery(targetContainer);
		if(jTargetContainer.hasClass("o_page_container_slot-inner")) {
			var jContainerSlot = jTargetContainer.closest(".o_page_container_slot");
			var slotId = jContainerSlot.data('oo-slot');
			var componentUrl = jContainerSlot.data("oo-content-editor-url");
			var containerId = jContainerSlot.data('oo-page-fragment');

			o_afterserver();

			var fragmentContent = '';
			var richTextJ = jQuery(el).find('.o_richtext_mce');
			if (richTextJ.length > 0) {
				var tinyId = richTextJ.get(0).id.replace('_diw', '');
				var tiny = tinymce.get(tinyId);
				fragmentContent = tiny.getContent();
			}

			o_XHREvent(componentUrl, false, false, "_csrf", settings.csrfToken, "cid", "drop_fragment", "fragment", containerId, "dragged", draggedId, "source", draggedId, "target", targetId, "container", containerId, "slot", slotId, "position", position, "content", fragmentContent);
			return true;
		} else if(jTargetContainer.hasClass("o_legacy_container")) {
			var containerId = null;
			var componentUrl;
			
			if(sibling) {
				componentUrl = jQuery(sibling).data('oo-content-editor-url');
			} else {
				var partEls = jTargetContainer.children(".o_page_part.o_page_part_view");
				if(partEls.length > 0) {
					var partEl = jQuery(partEls.get(partEls.length - 1));
					targetId = partEl.data('oo-page-fragment');
					componentUrl = partEl.data("oo-content-editor-url");
				} else {
					return false;
				}
			}
			o_afterserver();
			o_XHREvent(componentUrl, false, false, "_csrf", settings.csrfToken, "cid", "drop_fragment", "fragment", targetId, "dragged", draggedId, "source", draggedId, "target", targetId, "container", containerId, "slot", slotId, "position", position);
			return true;
		}
		return false;
	}
	
	function closeMathLive() {
		var mf = document.getElementById('mathlive');
		if(mf) {
			mf.executeCommand("hideVirtualKeyboard");
		}
		var mlk = document.querySelector('body>div.ML__keyboard.is-visible');
		if(mlk) {
			jQuery(mlk).removeClass("is-visible");
		}
	}
	
	function unfocusEditor() {
		try {
			if(o_info.lastFormFocusEl) {
				var focusedEl = jQuery(".o_page_content_editor #" + o_info.lastFormFocusEl);
				if(focusedEl.length > 0) {
					o_info.lastFormFocusEl = 0;
				}
			}
		} catch(e) {
			if(window.console) console.log(e);
		}
	}

	function initWindowListener(settings) {
		if(o_info.contentEditorWindowListener === undefined || o_info.contentEditorWindowListener == null) {
			o_info.contentEditorWindowListener = function(e) {
				var componentUrl = jQuery(".o_page_content_editor").data("oo-content-editor-url");
				if(componentUrl === undefined || componentUrl == null) {
					jQuery(window).off('click', o_info.contentEditorWindowListener);
					o_info.contentEditorWindowListener = null;
				} else {
					var jTarget = jQuery(e.target);
					var excludedEls = jTarget.closest(".o_page_add_in_container").length > 0 
						// Modal dialogs
						|| jTarget.closest(".o_popover").length > 0
						|| jTarget.closest(".o_layered_panel .popover").length > 0
						|| jTarget.closest(".o_layered_panel .modal-dialog").length > 0
						// TinyMCE
						|| jTarget.closest(".tox-tinymce").length > 0
						|| jTarget.closest(".tox-dialog").length > 0
						|| jTarget.closest(".tox-dialog-wrap__backdrop").length > 0
						|| jTarget.closest(".mce-content-body").length > 0
						// MathLive
						|| jTarget.closest(".ML__keyboard").length > 0
						|| jTarget.closest(".o_mathlive_editor").length > 0
						// Mediaelement.js
						|| jTarget.closest(".mejs__overlay-button").length > 0
						|| jTarget.closest(".mejs__controls").length > 0
						// Content editor
						|| (jTarget.closest(".o_ceditor_inspector").length > 0 && jTarget.closest(".o_ceditor_inspector_header").length == 0)
						|| jTarget.closest(".o_evaluation_editor_form").length > 0
						|| jTarget.closest(".o_page_with_side_options_wrapper").length > 0
						|| jTarget.closest(".o_page_table_edition").length > 0
						|| jTarget.closest(".o_page_edit_code").length > 0
						|| jTarget.closest(".o_page_edit_quiz").length > 0
						|| jTarget.closest(".o_gallery_images.o_editable_table").length > 0
						|| jTarget.closest(".o_page_run_image_comparison .twentytwenty-handle").length > 0
						|| (jTarget.closest("a").length > 0 && jTarget.closest(".o_tools_container").length == 0 && jTarget.closest(".o_ceditor_inspector_header").length == 0)
						|| jTarget.closest("button").length > 0
						|| e.target.nodeName == 'BODY'
						// callout of an editor is open
						|| jQuery(".o_ceditor_callout").length > 0;
					
					if(!excludedEls) {
						var edited = jTarget.closest(".o_fragment_edited").length > 0;
						var parts = jTarget.closest(".o_page_part");
						if(jTarget.hasClass('o_page_container_tools')) {
							var containerUrl = jTarget.data("oo-content-editor-url");
							closeMathLive();
							o_XHREvent(containerUrl, false, false, '_csrf', settings.csrfToken, 'cid', 'edit_fragment', 'fragment', jTarget.data('oo-page-fragment'));
						} else if(parts.length == 1) {
							var element = jQuery(parts.get(0));
							var elementUrl = element.data("oo-content-editor-url");
							closeMathLive();
							o_XHREvent(elementUrl, false, false, '_csrf', settings.csrfToken, 'cid', 'edit_fragment', 'fragment', element.data('oo-page-fragment'));
						} else if(!edited) {
							closeMathLive();
							o_afterserver();
							unfocusEditor();
							o_XHREvent(componentUrl, false, false, '_csrf', settings.csrfToken, 'cid', 'close_edit_fragment', 'ignore-validating-error', 'oo-ignore-validating-error');
						} else if(jTarget.closest(".o_ceditor_inspector_header").length > 0 && jTarget.closest("a").length > 0) {
							closeMathLive();
							o_afterserver();
							unfocusEditor();
							o_XHREvent(componentUrl, false, false, '_csrf', settings.csrfToken, 'cid', 'close_inspector', 'ignore-validating-error', 'oo-ignore-validating-error');
						}
					}
				}
			};
			jQuery(window).on('click', o_info.contentEditorWindowListener);
		}
	}
}(jQuery));