/*
 * $Id: DummyValueSource.java,v 1.1 2004/08/02 22:48:44 cyganiak Exp $
 */
package de.fuberlin.wiwiss.d2rq;

import java.util.Map;
import java.util.Set;

/**
 * Dummy implementation of {@link ValueSource}
 *
 * @author Richard Cyganiak <richard@cyganiak.de>
 */
public class DummyValueSource implements ValueSource {
	private boolean couldFit = true;
	private String returnValue = null;
	private Set columns;
	private Map columnValues;

	public DummyValueSource(String value, boolean couldFit) {
		this.returnValue = value;
		this.couldFit = couldFit;
	}

	public void setCouldFit(boolean couldFit) {
		this.couldFit = couldFit;
	}

	public void setValue(String value) {
		this.returnValue = value;
	}

	public void setColumns(Set columns) {
		this.columns = columns;
	}

	public void setColumnValues(Map columnValues) {
		this.columnValues = columnValues;
	}

	public boolean couldFit(String value) {
		return this.couldFit;
	}

	public Set getColumns() {
		return this.columns;
	}

	public Map getColumnValues(String value) {
		return this.columnValues;
	}

	public String getValue(String[] row, Map columnNameNumberMap) {
		return this.returnValue;
	}
}