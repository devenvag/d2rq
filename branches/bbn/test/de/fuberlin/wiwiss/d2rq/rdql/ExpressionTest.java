/*
 * Created on 14.04.2005 by Joerg Garbers, FU-Berlin
 *
 */
package de.fuberlin.wiwiss.d2rq.rdql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import de.fuberlin.wiwiss.d2rq.GraphD2RQ;
import de.fuberlin.wiwiss.d2rq.RDQLTestFramework;
import de.fuberlin.wiwiss.d2rq.find.SQLResultSet;
import de.fuberlin.wiwiss.d2rq.helpers.Logger;
import junit.framework.TestCase;

/**
 * @author jgarbers
 *
 */
public class ExpressionTest extends RDQLTestFramework {
    String condition, sqlCondition=null;
    Logger translatedLogger=new Logger();

    public ExpressionTest(String arg0) {
        super(arg0);
     }

	protected void setUp() throws Exception {
		super.setUp();
		rsLogger.setDebug(false); // true
		rdqlLogger.setDebug(false); // true
		translatedLogger.setDebug(true);
		if (translatedLogger.debugEnabled())
		    ExpressionTranslator.logSqlExpressions=new HashSet();
		SQLResultSet.simulationMode=true;
		RDQLTestFramework.compareQueryHandlers=false;
		GraphD2RQ.setUsingD2RQQueryHandler(true);
		SQLResultSet.protocol=new ArrayList();
	}
	
	String getQuery(String condition) {
	    String triples=
	        "(?x, <http://annotation.semanticweb.org/iswc/iswc.daml#author>, ?z), " +
			"(?z, <http://annotation.semanticweb.org/iswc/iswc.daml#eMail> , ?y)";
	    String query="SELECT ?x, ?y WHERE " + triples + " AND " + condition;
	    return query;
	}
		
	void query() {
	    rdql(getQuery(condition));
	    if (translatedLogger.debugEnabled()) {
	        translatedLogger.debug(condition + " ->");
	        Iterator it=ExpressionTranslator.logSqlExpressions.iterator();
	        while (it.hasNext()) {
	            String translated=(String) it.next();
	            translatedLogger.debug(translated);
	        }
	        if (sqlCondition!=null)
	            assertTrue(ExpressionTranslator.logSqlExpressions.contains(sqlCondition));
	        // else
	        //    assertTrue(ExpressionTranslator.logSqlExpressions.size()==0);
	    }
	}

	public void testRDQLGetAuthorsAndEmailsWithCondition() {
	    // GraphD2RQ.setUsingD2RQQueryHandler(false);
	    condition="(?x eq ?z)";
	    //sqlCondition=""
		query();
	}
	public void testNobody() {
	    condition="(?z eq \"Nobody\")";
		query();
	}
	public void testIsSeaborne() {
	    condition="?z eq \"http://www-uk.hpl.hp.com/people#andy_seaborne\"";
	    query();
	}
	public void testEasy() {
	    condition="(1 == 1)";
		query();
	}
	public void testImpossible() {
	    condition="! (1 == 1)";
		query();
	}
	public void testAnd() {
	    condition="(1 == 1) && true";
		query();
	}

}