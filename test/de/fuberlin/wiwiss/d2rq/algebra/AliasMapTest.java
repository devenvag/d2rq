package de.fuberlin.wiwiss.d2rq.algebra;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fuberlin.wiwiss.d2rq.algebra.AliasMap;
import de.fuberlin.wiwiss.d2rq.algebra.Attribute;
import de.fuberlin.wiwiss.d2rq.algebra.Expression;
import de.fuberlin.wiwiss.d2rq.algebra.Join;

import junit.framework.TestCase;

/**
 * @author Richard Cyganiak (richard@cyganiak.de)
 * @version $Id: AliasMapTest.java,v 1.1 2006/09/11 23:22:26 cyganiak Exp $
 */
public class AliasMapTest extends TestCase {
	private final static Attribute foo_col1 = new Attribute("foo.col1");
	private final static Attribute bar_col1 = new Attribute("bar.col1");
	private final static Attribute baz_col1 = new Attribute("baz.col1");
	private final static Attribute abc_col1 = new Attribute("abc.col1");
	private final static Attribute xyz_col1 = new Attribute("xyz.col1");

	private AliasMap fooAsBar;
	
	public void setUp() {
		Map m = new HashMap();
		m.put("bar", "foo");
		this.fooAsBar = new AliasMap(m);
	}
	
	public void testEmptyMapDoesIdentityTranslation() {
		AliasMap aliases = new AliasMap(Collections.EMPTY_MAP);
		assertFalse(aliases.isAlias("foo"));
		assertFalse(aliases.hasAlias("foo"));
		assertEquals("foo", aliases.applyToTableName("foo"));
		assertEquals("foo", aliases.originalOf("foo"));
	}

	public void testAliasIsTranslated() {
		assertFalse(this.fooAsBar.isAlias("foo"));
		assertTrue(this.fooAsBar.isAlias("bar"));
		assertFalse(this.fooAsBar.isAlias("baz"));
		assertTrue(this.fooAsBar.hasAlias("foo"));
		assertFalse(this.fooAsBar.hasAlias("bar"));
		assertFalse(this.fooAsBar.hasAlias("baz"));
		assertEquals("bar", this.fooAsBar.applyToTableName("foo"));
		assertEquals("baz", this.fooAsBar.applyToTableName("baz"));
		assertEquals("foo", this.fooAsBar.originalOf("bar"));
		assertEquals("baz", this.fooAsBar.originalOf("baz"));
	}
	
	public void testApplyToColumn() {
		assertEquals(baz_col1, this.fooAsBar.applyTo(baz_col1));
		assertEquals(bar_col1, this.fooAsBar.applyTo(foo_col1));
		assertEquals(bar_col1, this.fooAsBar.applyTo(bar_col1));
	}
	
	public void testOriginalOfColumn() {
		assertEquals(baz_col1, this.fooAsBar.originalOf(baz_col1));
		assertEquals(foo_col1, this.fooAsBar.originalOf(foo_col1));
		assertEquals(foo_col1, this.fooAsBar.originalOf(bar_col1));
	}
	
	public void testApplyToMapKeys() {
		Map data = new HashMap();
		data.put(foo_col1, "val1");
		data.put(baz_col1, "val2");
		Map expected = new HashMap();
		expected.put(bar_col1, "val1");
		expected.put(baz_col1, "val2");
		assertEquals(expected, this.fooAsBar.applyToMapKeys(data));
	}
	
	public void testApplyToColumnSet() {
		Set data = new HashSet(Arrays.asList(new Attribute[]{foo_col1, baz_col1}));
		Set expected = new HashSet(Arrays.asList(new Attribute[]{bar_col1, baz_col1}));
		assertEquals(expected, this.fooAsBar.applyToColumnSet(data));
	}
	
	public void testApplyToJoinSetDoesNotModifyUnaliasedJoin() {
		Join join = new Join();
		join.addCondition(abc_col1, xyz_col1);
		Set joins = Collections.singleton(join);
		assertEquals(joins, this.fooAsBar.applyToJoinSet(joins));
	}
	
	public void testApplyToJoinSetDoesModifyAliasedJoin() {
		Join join = new Join();
		join.addCondition(foo_col1, foo_col1);
		Set aliasedSet = this.fooAsBar.applyToJoinSet(Collections.singleton(join));
		assertEquals(1, aliasedSet.size());
		Join aliased = (Join) aliasedSet.iterator().next();
		assertEquals(Collections.singleton(bar_col1), aliased.getFirstColumns());
		assertEquals(Collections.singleton(bar_col1), aliased.getSecondColumns());
	}
	
	public void testApplyToExpression() {
		assertEquals(new Expression("bar.col1"), 
				fooAsBar.applyTo(new Expression("foo.col1")));
	}

	public void testNoAliasesConstantHasNoAliasesAndNoOriginals() {
		assertTrue(AliasMap.NO_ALIASES.allAliases().isEmpty());
		assertTrue(AliasMap.NO_ALIASES.allOriginalsWithAliases().isEmpty());
	}
	
	public void testNoAliasesConstantEqualsNewEmptyAliasMap() {
		AliasMap noAliases = new AliasMap(Collections.EMPTY_MAP);
		assertTrue(AliasMap.NO_ALIASES.equals(noAliases));
		assertTrue(noAliases.equals(AliasMap.NO_ALIASES));
	}
	
	public void testEmptyMapEqualsItself() {
		assertTrue(AliasMap.NO_ALIASES.equals(AliasMap.NO_ALIASES));
	}
	
	public void testEmptyMapDoesntEqualPopulatedMap() {
		assertFalse(AliasMap.NO_ALIASES.equals(fooAsBar));
	}
	
	public void testPopulatedMapDoesntEqualEmptyMap() {
		assertFalse(fooAsBar.equals(AliasMap.NO_ALIASES));
	}
	
	public void testPopulatedMapEqualsItself() {
		Map m = new HashMap();
		m.put("bar", "foo");
		AliasMap fooAsBar2 = new AliasMap(m);
		assertTrue(fooAsBar.equals(fooAsBar2));
		assertTrue(fooAsBar2.equals(fooAsBar));
	}
	
	public void testPopulatedMapDoesNotEqualDifferentMap() {
		Map m = new HashMap();
		m.put("baz", "foo");
		AliasMap fooAsBaz = new AliasMap(m);
		assertFalse(fooAsBar.equals(fooAsBaz));
		assertFalse(fooAsBaz.equals(fooAsBar));
	}
	
	public void testEqualMapsHaveSameHashCode() {
		AliasMap m1 = new AliasMap(new HashMap());
		AliasMap m2 = new AliasMap(new HashMap());
		assertEquals(m1.hashCode(), m2.hashCode());
	}
	
	public void testBuildFromSQL() {
		assertEquals(AliasMap.NO_ALIASES, AliasMap.buildFromSQL(Collections.EMPTY_SET));
		assertEquals(fooAsBar, AliasMap.buildFromSQL(Collections.singleton("foo AS bar")));
		assertEquals(fooAsBar, AliasMap.buildFromSQL(Collections.singleton("foo as bar")));
	}

	public void testToStringEmpty() {
		assertEquals("AliasMap()", AliasMap.NO_ALIASES.toString());
	}
	
	public void testToStringOneAlias() {
		assertEquals("AliasMap(foo AS bar)", fooAsBar.toString());
	}
	
	public void testToStringTwoAliases() {
		Map m = new HashMap();
		m.put("bar", "foo");
		m.put("xyz", "abc");
		// Order is alphabetical by table name
		assertEquals("AliasMap(abc AS xyz, foo AS bar)", new AliasMap(m).toString());
	}
	
	public void testAllAliasesIsEmptyForNoAliases() {
		assertTrue(AliasMap.NO_ALIASES.allAliases().isEmpty());
	}
	
	public void testAllAliasesContainsAliasForSingleAlias() {
		assertEquals(Collections.singleton("bar"), fooAsBar.allAliases());
	}
	
	public void testAllAliasesContainsBothForTwoAliases() {
		Map m = new HashMap();
		m.put("bar", "foo");
		m.put("xyz", "abc");
		Set expected = new HashSet(Arrays.asList(new String[]{"bar", "xyz"}));
		assertEquals(expected, new AliasMap(m).allAliases());
	}
}