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
package org.olat.modules.ceditor.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.TableContent;
import org.olat.modules.ceditor.model.TableElement;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * 
 * Initial date: 19 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TableRunController extends BasicController implements PageRunElement {
	
	private TableElement table;
	private final boolean inForm;
	private final VelocityContainer mainVC;
	
	public TableRunController(UserRequest ureq, WindowControl wControl, TableElement table, boolean inForm) {
		super(ureq, wControl);
		this.table = table;
		this.inForm = inForm;
		mainVC = createVelocityContainer("table_run");
		setBlockLayoutClass(table.getTableSettings());
		putInitialPanel(mainVC);
		loadModel();
	}

	private void setBlockLayoutClass(TableSettings tableSettings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(tableSettings, inForm));
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return false;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			if(cpe.isElement(table)) {
				table = (TableElement)cpe.getElement();
				loadModel();
				setBlockLayoutClass(table.getTableSettings());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	protected void loadModel() {
		TableSettings settings = table.getTableSettings();
		mainVC.contextPut("settings", settings);
		TableContent content = table.getTableContent();
		
		int numOfRows = content.getNumOfRows();
		
		List<Row> tableRows = new ArrayList<>(numOfRows);
		List<Row> headerRows = new ArrayList<>(numOfRows);
		
		int i = 0;
		if(settings.isColumnHeaders()) {
			headerRows.add(loadColumns(i, content, settings));
			i++;
		}

		for( ; i<numOfRows; i++) {
			tableRows.add(loadColumns(i, content, settings));
		}

		mainVC.contextPut("headerRows", headerRows);
		mainVC.contextPut("tableRows", tableRows);
		if(StringHelper.containsNonWhitespace(content.getTitle())) {
			mainVC.contextPut("tableTitle", content.getTitle());
		} else {
			mainVC.contextRemove("tableTitle");
		}
		if(StringHelper.containsNonWhitespace(content.getCaption())) {
			mainVC.contextPut("tableCaption", content.getCaption());	
		} else {
			mainVC.contextRemove("tableCaption");
		}
	}
	
	private Row loadColumns(int i, TableContent content, TableSettings settings) {
		int numOfColumns = content.getNumOfColumns();
		
		List<Column> columns = new ArrayList<>(numOfColumns);
		Row row = new Row(columns);
		for(int j=0; j<numOfColumns; j++) {
			String text = content.getContent(i, j);
			Column column = new Column(text, j == 0 && settings.isRowHeaders());
			columns.add(column);
		}
		return row;
	}
	
	public static class Row {
		
		private final List<Column> columns;
		
		public Row(List<Column> columns) {
			this.columns = columns;
		}
		
		public List<Column> getColumns() {
			return columns;
		}
	}
	
	public static class Column {
		
		private final String text;
		private final boolean header;
		
		public Column(String text, boolean header) {
			this.text = text;
			this.header = header;
		}
		
		public boolean isHeader() {
			return header;
		}
		
		public String getText() {
			return text;
		}
	}
}
