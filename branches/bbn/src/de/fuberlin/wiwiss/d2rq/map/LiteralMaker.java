/*
 (c) Copyright 2004 by Chris Bizer (chris@bizer.de)
 */
package de.fuberlin.wiwiss.d2rq.map;

import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

import de.fuberlin.wiwiss.d2rq.rdql.NodeConstraint;
import de.fuberlin.wiwiss.d2rq.rdql.TablePrefixer;

/**
 * LiteralMakers transform attribute values from a result set into literals.
 *
 * <p>History:<br>
 * 06-21-2004: Initial version of this class.<br>
 * 08-03-2004: Extended with couldFit, getColumns, getColumnValues
 * 
 * @author Chris Bizer chris@bizer.de
 * @author Richard Cyganiak <richard@cyganiak.de>
 * @version V0.2
 */
public class LiteralMaker implements NodeMaker, Prefixable {
	private ValueSource valueSource;
	private RDFDatatype datatype;
	private String lang;
	private String id;
	
	public Object clone() throws CloneNotSupportedException {return super.clone();}
	public void prefixTables(TablePrefixer prefixer) {
		valueSource=(ValueSource)prefixer.prefixIfPrefixable(valueSource);
	}
    
	/**
	 * Prefix the table with the bind variable information considered
	 * @param prefixer the table prefixer
	 * @param boundVar the varaible that the table is bound to
	 */
    public void prefixTables(TablePrefixer prefixer, String boundVar ) {
        if( valueSource instanceof Prefixable ){
            valueSource=(ValueSource)prefixer.prefixPrefixable((Prefixable)valueSource, boundVar);
        }
    }

    public void matchConstraint(NodeConstraint c) {
        c.matchNodeType(NodeConstraint.LiteralNodeType);
        c.matchLiteralMaker(this);  
        c.matchValueSource(valueSource);
    }

    // jg
    public boolean matchesOtherLiteralMaker(LiteralMaker other) {
        boolean b1,b2;
        b1=(datatype==null && other.datatype==null) || datatype.equals(other.datatype);
        b2=(lang==null && other.lang==null) || lang.equals(other.lang);        	
        return b1 && b2;
    }        	
    
	public LiteralMaker(String id, ValueSource valueSource, RDFDatatype datatype, String lang) {
		this.valueSource = valueSource;
		this.datatype = datatype;
		this.lang = lang;
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see de.fuberlin.wiwiss.d2rq.NodeMaker#couldFit(com.hp.hpl.jena.graph.Node)
	 */
	public boolean couldFit(Node node) {
		if (node.isVariable()) {
			return true;
		}
		if (!node.isLiteral()) {
			return false;
		}
		LiteralLabel label = node.getLiteral();
        //Get around the problems with @en if language is specified then assume its a string
        if( !(label.getDatatype() == null && label.language() != null) ){
    		if (!areCompatibleDatatypes(this.datatype, label.getDatatype())) {
    			return false;
    		}
        }
		String nodeLang = label.language();
		if ("".equals(nodeLang)) {
			nodeLang = null;
		}
        
    	if (!areCompatibleLanguages(this.lang, nodeLang)) {
    		return false;
    	}
		return this.valueSource.couldFit(label.getLexicalForm());
	}

	/* (non-Javadoc)
	 * @see de.fuberlin.wiwiss.d2rq.NodeMaker#getColumns()
	 */
	public List getColumns() {
		return this.valueSource.getColumns();
	}

	/* (non-Javadoc)
	 * @see de.fuberlin.wiwiss.d2rq.NodeMaker#getColumnValues(com.hp.hpl.jena.graph.Node)
	 */
	public Map getColumnValues(Node node) {
		return this.valueSource.getColumnValues(node.getLiteral().getLexicalForm());
	}

	/* (non-Javadoc)
	 * @see de.fuberlin.wiwiss.d2rq.NodeMaker#getNode(java.lang.String[], java.util.Map)
	 */
	public Node getNode(String[] row, Map columnNameNumberMap) {
		String value = this.valueSource.getValue(row, columnNameNumberMap);        
		if (value == null) {
			return null;
		}
		return Node.createLiteral(value, this.lang, this.datatype);
	}

	private boolean areCompatibleDatatypes(RDFDatatype dt1, RDFDatatype dt2) {
		if (dt1 == null) {
			return dt2 == null;
		}
		return dt1.equals(dt2);
	}

	private boolean areCompatibleLanguages(String lang1, String lang2) {
		if (lang1 != null && lang2 != null ) {
			return lang1.equals(lang2);
        }
        //Can't tell so assume they are compatible
        return true;
	}
	
	public String toString() {
		return "LiteralMaker@" + this.id;
	}
}