package de.fuberlin.wiwiss.d2rq.pp;

import junit.framework.TestCase;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class PrettyPrinterTest extends TestCase {
	
	public void testNodePrettyPrinting() {
		assertEquals("\"foo\"", 
				PrettyPrinter.toString(Node.createLiteral("foo")));
		assertEquals("\"foo\"@en", 
				PrettyPrinter.toString(Node.createLiteral("foo", "en", null)));
		assertEquals("\"1\"^^<" + XSDDatatype.XSDint.getURI() + ">",
				PrettyPrinter.toString(Node.createLiteral("1", null, XSDDatatype.XSDint)));
		assertEquals("\"1\"^^xsd:int",
				PrettyPrinter.toString(Node.createLiteral("1", null, XSDDatatype.XSDint), PrefixMapping.Standard));
		assertEquals("_:foo", 
				PrettyPrinter.toString(Node.createAnon(new AnonId("foo"))));
		assertEquals("<http://example.org/>", 
				PrettyPrinter.toString(Node.createURI("http://example.org/")));
		assertEquals("<" + RDF.type.getURI() + ">", 
				PrettyPrinter.toString(RDF.type.asNode(), new PrefixMappingImpl()));
		assertEquals("rdf:type", 
				PrettyPrinter.toString(RDF.type.asNode(), PrefixMapping.Standard));
	}
	
	public void testTriplePrettyPrinting() {
		assertEquals("<http://example.org/a> <" + RDFS.label.getURI() + "> \"Example\" .",
				PrettyPrinter.toString(new Triple(
						Node.createURI("http://example.org/a"),
						RDFS.label.asNode(),
						Node.createLiteral("Example", null, null))));
	}
	
	public void testTriplePrettyPrintingWithPrefixMapping() {
		PrefixMappingImpl prefixes = new PrefixMappingImpl();
		prefixes.setNsPrefixes(PrefixMapping.Standard);
		prefixes.setNsPrefix("ex", "http://example.org/");
		assertEquals("ex:a rdfs:label \"Example\" .",
				PrettyPrinter.toString(new Triple(
						Node.createURI("http://example.org/a"),
						RDFS.label.asNode(),
						Node.createLiteral("Example", null, null)), prefixes));
	}
}