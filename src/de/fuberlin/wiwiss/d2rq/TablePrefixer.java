package de.fuberlin.wiwiss.d2rq;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.fuberlin.wiwiss.d2rq.Column;
import de.fuberlin.wiwiss.d2rq.Join;

class TablePrefixer {
	protected String tablePrefix; // if null, collect info only but leave everything identical
	protected int tablePrefixLength; // kept for performance reasons (in sync with tablePrefix)
	protected static final String prefixSeparator = "_";
	protected static final String triplePrefix = "T";
	
	// prefixing and getting the table references and aliases right 
	// see SQLStatementMaker.sqlFromExpression(referredTables,aliasMap)
	protected Map aliasMap; // from String (aliased Table) to Alias
	protected Set referedTables = new HashSet(5); // Strings in their alias forms	
	protected Map prefixedAliasMap; // is created during prefixing
	
	// use without tablePrefixing
	public TablePrefixer() {
	}
	public TablePrefixer(String prefix) {
		this.setTablePrefix(prefix);
	}
	public TablePrefixer(int tripleNumber) {
		this.setTablePrefixToTripleNumber(tripleNumber);
	}
	
	// istance variable access methods
	// sets up tablePrefix including prefixSeparator and initializes prefixedAliasMap
	public void setTablePrefix(String newPrefix) {
		tablePrefix=newPrefix + prefixSeparator;
		tablePrefixLength=tablePrefix.length();
		if (newPrefix==null)
			prefixedAliasMap=null;
		else
			prefixedAliasMap=new HashMap(5);
	}
	public void setTablePrefixToTripleNumber(int n) {
		setTablePrefix(triplePrefix+n);
	}
	public Map getAliasMap() {
		return aliasMap;
	}
	public void setAliasMap(Map aliasMap) {
		this.aliasMap = aliasMap;
	}
	public Set getReferedTables() {
		return referedTables;
	}
	public void setReferedTables(Set referedTables) {
		this.referedTables = referedTables;
	}
	
	public Map getPrefixedAliasMap() {
		return prefixedAliasMap;
	}
	//  prefixing methods

	protected String prefixString(String table) {
		if (tablePrefix==null)
			return table;
		else 
			return tablePrefix + prefixSeparator + table;
	}
	protected String unprefixString(String table) {
		if (tablePrefix==null)
			return table;
		if (table.startsWith(tablePrefix))
			return table.substring(tablePrefixLength+1);
		return null;
	}
	public Map unprefixedColumnNameNumberMap(Map columnNameNumber) {
		if (tablePrefix==null)
			return columnNameNumber;
		Map result=new HashMap(10);
		Iterator keys=columnNameNumber.keySet().iterator();
		while (keys.hasNext()) {
			String prefixedCol=(String)keys.next();
			String unprefixed=unprefixString(prefixedCol);
			if (unprefixed!=null) {
				Object val=columnNameNumber.get(prefixedCol);
				result.put(unprefixed,val);
			}
		}
		return result;
	}				
	
	protected static java.util.regex.Pattern pat = java.util.regex.Pattern.compile("\\p{Alnum}");

	// the following code may fail, if there are strings in the expressions, that match with table names
	protected String replaceTablesInExpression(String expression) {
		if (tablePrefix==null)
			return expression;
		java.util.regex.Matcher m = pat.matcher(expression);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			int start=m.start();
			String alphanum=m.group();
			boolean prevIsDot = (start>0) && ('.' == expression.charAt(start-1));
			if (!prevIsDot) {
				String replacement=substituteIfTable(alphanum);
				m.appendReplacement(sb, replacement);
			} else {
				m.appendReplacement(sb, alphanum);				
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	
	// methods for prefixing and getting the table references and aliases right 
	
	protected String substituteIfTable(String identifier) {
		// figure out tableName version including alias and prefix information
		// needed in guessing if s.th. is a table in an expression (e.g. condition)
		if (tablePrefix==null)
			return identifier;
		String prefixed=prefixString(identifier);
		if (referedTables.contains(prefixed))
			return prefixed;
		return identifier;
	}		
		
	// return tableName resp. its substitution and make sure a FROM-Term exists
	protected String prefixAndReferTable(String tableName) {
		// figure out tableName version including alias and prefix information
		if (tablePrefix==null) {
			referedTables.add(tableName);
			return tableName;
		}
		Alias mapVal=null;
		String dbTable=tableName; // name of table in DB
		String prefixedTable=tableName;
		if (aliasMap!=null)
			mapVal = (Alias)aliasMap.get(tableName);
		boolean isAlias=(mapVal!=null);
		if (isAlias)
			dbTable=mapVal.databaseTable();
		boolean newAlias=false;
		if (tablePrefix!=null) {
			prefixedTable=prefixString(tableName);
			isAlias=true;
			newAlias=true;
		}
		referedTables.add(prefixedTable);
		if (newAlias)
			prefixedAliasMap.put(prefixedTable,new Alias(dbTable,prefixedTable));
		
		return prefixedTable;
	}
	
	// this polymorphism is used in handling NodeMaker and ValueSource classes
	// some of which do not implemnet Prefixable
	public Object prefixIfPrefixable(Prefixable obj) {
		return prefixPrefixable(obj);
	}
	public Object prefixIfPrefixable(Object obj) {	
		return obj;
	}
	
//////////////////////////////////
	// correctly typed methods grouped with their prefix() variant
	// prefix() is a polymorph version of less polymorph methods
	// that is needed for uniform collection handling

	public Object prefix(Object obj) {
		if (obj instanceof Prefixable) 
			return prefixPrefixable((Prefixable) obj);
		if (obj instanceof NodeMaker)  
			return prefixIfPrefixable((NodeMaker)obj);
		if (obj instanceof ValueSource)
			return prefixIfPrefixable((ValueSource)obj);
		if (obj instanceof Collection)
			return prefixCollection((Collection)obj);
		throw new RuntimeException("unrecognized argument " + obj.toString() + "to TablePrefixer.prefix().");
	}
	
	public Object prefix(Prefixable obj) {
		return prefixPrefixable(obj);
	}
	// useful, if Prefixable is known.
	public Prefixable prefixPrefixable(Prefixable obj) {
		if (tablePrefix==null) {
			obj.prefixTables(this);
			return obj;
		}
		Prefixable clon=null;
		Exception x=null;
		try {
			clon=(Prefixable)obj.clone();
		} catch (Exception e) {
			x=e;
		}
		if (x!=null) {
			clon.prefixTables(this);
			return clon;		
		} else
			throw new RuntimeException(x);
	}
		
	public Object prefix(NodeMaker obj) {
		return prefixIfPrefixable(obj);
	}
	public NodeMaker prefixNodeMaker(NodeMaker obj) {
		return (NodeMaker)prefixIfPrefixable(obj);
	}
	
	public Object prefix(ValueSource obj) {
		return prefixIfPrefixable(obj);
	}
	public ValueSource prefixValueSource(ValueSource obj) {
		return (ValueSource)prefixIfPrefixable(obj);
	}
	
	// helper method
	private Collection prefixCollectionIntoCollection(Collection collection, Collection results) {
		Iterator it=collection.iterator();
		while (it.hasNext()){
			Object entry=it.next();
			results.add(prefix(entry));
		}
		return results;
	}
	private Collection prefixCollectionEntries(Collection collection) {
		Iterator it=collection.iterator();
		while (it.hasNext()){
			Object entry=it.next();
			prefix(entry); // dont care about result
		}
		return collection;
	}
	
	private static Class[] intParameterTypes=new Class[]{int.class};
	private static Class[] noneParameterTypes=new Class[]{};

	public Object prefix(Collection obj) {
		return prefixCollection(obj);
	}
	// construct a collection instance of same class as collection
	public Collection prefixCollection(Collection collection) {
		if (tablePrefix==null) {
			return prefixCollectionEntries(collection);
		}
		Class cls=collection.getClass();
		Object inst=null;
		// Constructor cons=cls.getDeclaredConstructor(intParameterTypes);
		try {
		   inst=cls.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("TablePrefixer: class "+cls+" has no () constructor!");
		}
		return prefixCollectionIntoCollection(collection,(Collection)inst);
	}

	// produces HashSets
	public Set prefixSet(Set collection) {
		if (tablePrefix==null) {
			return (Set) prefixCollectionEntries(collection);
		}
		return (Set)prefixCollectionIntoCollection(collection,new HashSet(collection.size()));
	}



	public Object prefix(String obj) {
		return prefixTable(obj);
	}
	public String prefixTable(String table) {
		return prefixAndReferTable(table);
	}

	public Object prefix(Column obj) {
		return prefixPrefixable(obj);
	}
	// return column resp. its substitution and make sure a FROM-Term exists
	public Column prefixColumn(Column column) {
		return (Column)prefixPrefixable(column);
	}
	public Object prefix(Join obj) {
		return prefixPrefixable(obj);
	}
	public Join prefixJoin(Join join) {
		return (Join)prefixPrefixable(join);
	}

	// convenience Methods (remove!)
	
	public Set prefixConditions(Set conditions) {
		if (tablePrefix==null) {
			return conditions;
		}
		Set results=new HashSet();
		Iterator it=conditions.iterator();
		while (it.hasNext()){
			String condition=(String)it.next();
			results.add(this.replaceTablesInExpression(condition));
		}
		return results;
	}

	
	// public ArrayList
	
	public Map prefixColumnColumnMap(Map map) {
		Map results= (tablePrefix==null) ? map : new HashMap();
		Iterator it=map.keySet().iterator();
		while (it.hasNext()){
			Column fromColumn=(Column)it.next();
			Column toColumn=(Column)map.get(fromColumn);
			Column fromP=prefixColumn(fromColumn);
			Column toP=prefixColumn(toColumn);
			if (tablePrefix!=null)
				results.put(fromP, toP);
		}
		return results;
	}	

	

}