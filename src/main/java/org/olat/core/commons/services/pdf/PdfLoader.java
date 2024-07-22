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
package org.olat.core.commons.services.pdf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 18 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfLoader {
	
	private PdfLoader() {
		//
	}
	
	public static PDDocument load(File file) throws IOException {
		return Loader.loadPDF(file, "", org.apache.pdfbox.io.IOUtils.createMemoryOnlyStreamCache());
	}
	
	public static PDDocument load(byte[] data) throws IOException {
		return Loader.loadPDF(data);// Memory only
	}
	
	public static PDDocument load(VFSLeaf leaf) throws IOException {
		if(leaf instanceof LocalFileImpl file) {
			return load(file.getBasefile());
		}
		byte[] data = IOUtils.toByteArray(leaf.getInputStream());
		return load(data);
	}

}
