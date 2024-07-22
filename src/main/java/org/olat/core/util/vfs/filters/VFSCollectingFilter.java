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
package org.olat.core.util.vfs.filters;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 27 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class VFSCollectingFilter implements VFSItemFilter {
	
	private final VFSItemFilter delegate;
	private final List<VFSItem> acceptedItems = new ArrayList<>();

	public VFSCollectingFilter(VFSItemFilter delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean accept(VFSItem vfsItem) {
		boolean accepted = delegate.accept(vfsItem);
		if (accepted) {
			acceptedItems.add(vfsItem);
		}
		return accepted;
	}

	public List<VFSItem> getAcceptedItems() {
		return acceptedItems;
	}

}
