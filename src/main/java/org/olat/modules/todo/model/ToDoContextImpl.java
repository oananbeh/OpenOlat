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
package org.olat.modules.todo.model;

import org.olat.modules.todo.ToDoContext;

/**
 * 
 * Initial date: 21 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoContextImpl implements ToDoContext {
	
	private final String type;
	private final Long originId;
	private final String originSubPath;
	private final String originTitle;
	private final String originSubTitle;
	
	public ToDoContextImpl(String type, Long originId, String originSubPath, String originTitle, String originSubTitle) {
		this.type = type;
		this.originId = originId;
		this.originSubPath = originSubPath;
		this.originTitle = originTitle;
		this.originSubTitle = originSubTitle;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Long getOriginId() {
		return originId;
	}

	@Override
	public String getOriginSubPath() {
		return originSubPath;
	}

	@Override
	public String getOriginTitle() {
		return originTitle;
	}

	@Override
	public String getOriginSubTitle() {
		return originSubTitle;
	}

}
