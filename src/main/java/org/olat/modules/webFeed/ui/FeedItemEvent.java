/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.webFeed.ui;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.modules.webFeed.Item;

/**
 * Initial date: Mai 24, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedItemEvent extends Event {

	private static final long serialVersionUID = 2405172041950251807L;

	public static final String BULK_ADD_TAGS = "bulk-add-tags";
	public static final String BULK_REMOVE_TAGS = "bulk-remove-tags";
	public static final String ARTEFACT_FEED_ITEM = "artefact-feed-item";
	public static final String EDIT_FEED_ITEM = "edit-feed-item";
	public static final String DELETE_FEED_ITEM = "delete-feed-item";

	private List<String> tagDisplayNames;
	private Item item;

	public FeedItemEvent(String eventCmd, Item item) {
		super(eventCmd);
		this.item = item;
	}

	public FeedItemEvent(String eventCmd, List<String> tagDisplayNames) {
		super(eventCmd);
		this.tagDisplayNames = tagDisplayNames;
	}

	public Item getItem() {
		return item;
	}

	public List<String> getTagDisplayNames() {
		return tagDisplayNames;
	}
}
