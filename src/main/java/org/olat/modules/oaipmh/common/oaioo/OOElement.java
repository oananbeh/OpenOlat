/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="client://java.sun.com/xml/jaxb">client://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.27 at 01:16:00 PM WEST 
//

package org.olat.modules.oaipmh.common.oaioo;


import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OOElement implements XmlWritable {

	protected String name;
	protected String value;
	protected List<OOElement> ooElements = new ArrayList<>();

	public OOElement(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public OOElement withName(String value) {
		this.name = value;
		return this;
	}

	public OOElement withValue(String value) {
		this.value = value;
		return this;
	}

	public List<OOElement> getElements() {
		return this.ooElements;
	}

	public OOElement withElement(OOElement OOElement) {
		this.ooElements.add(OOElement);
		return this;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		try {

			writer.writeCharacters(value);

		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}
}
