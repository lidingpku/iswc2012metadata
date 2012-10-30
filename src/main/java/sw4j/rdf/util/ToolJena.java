/**
MIT License

Copyright (c) 2009 

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
 */

package sw4j.rdf.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import sw4j.util.DataObjectGroupMap;
import sw4j.util.DataPVCMap;
import sw4j.util.DataPVHMap;
import sw4j.util.DataQname;
import sw4j.util.DataSmartMap;
import sw4j.util.Sw4jException;
import sw4j.util.ToolHash;
import sw4j.util.ToolSafe;
import sw4j.util.ToolIO;
import sw4j.util.ToolString;
import sw4j.util.ToolURI;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * provide many useful RDF manipulation functions
 * 
 * @author Li Ding
 * 
 */
public class ToolJena {

	private static Logger getLogger(){
		return Logger.getLogger(ToolJena.class);
	}

	////////////////////////////////////////////////
	// RDF List <=> rdfs:member
	////////////////////////////////////////////////
	/**
	 * parse an RDF:List into a java List object
	 * 
	 * @param m
	 * @param root
	 * @return
	 */
	public static List<RDFNode> listListMembers(Model m, Resource root) {
		return listListMembers(m, root, RDF.first, RDF.rest);
	}

	/**
	 * parse an a list  into a java List object
	 * 
	 * @param m
	 * @param root
	 * @return
	 */
	public static List<RDFNode> listListMembers(Model m, Resource root, Property first, Property rest) {
		Model model_list = create_copyList(m,root,first, rest);
		return model_list.listObjectsOfProperty(first).toList();
		/*
		ArrayList<RDFNode> data = new ArrayList<RDFNode>();
		Resource node = root;
		while (null != node) {
			NodeIterator iter_obj = m.listObjectsOfProperty(node, first);
			if (iter_obj.hasNext()) {
				data.add(iter_obj.nextNode());
			}

			iter_obj = m.listObjectsOfProperty(node, rest);
			if (iter_obj.hasNext()) {
				node = (Resource) iter_obj.next();
			} else {
				node = null;
			}
		}
		return data;
		*/
	}	

	public static Model create_copyList(Model m, Resource root, Property first, Property rest) {
		Model ret = ModelFactory.createDefaultModel();
		Resource node = root;
		while (null != node) {
			ret.add(m.listStatements(node,null,(String)null));

			NodeIterator iter_obj = m.listObjectsOfProperty(node, rest);
			if (iter_obj.hasNext()) {
				node=(Resource) iter_obj.next();
			} else {
				node = null;
			}
		}
		return ret;
	}	
	////////////////////////////////////////////////
	// generate a model from the current model
	////////////////////////////////////////////////
	
	/**
	 * generate a new model given a model, generate rdfs:member base on list (first, rest) assertions 
	 * - optionally remove all ds:List assertions 
	 * 
	 * @param m
	 * @param first
	 * @param rest
	 * @param bRemoveListTriple
	 * @return
	 */
	public static void update_decoupleList(Model m, Property first, Property rest, Property part, boolean bRemoveListTriple) {
		if (null==part)
			part=RDFS.member;
		
		Set<Resource> subjects = m.listSubjects().toSet();

		// subjects of list
		Set<Resource> subjects_first = m.listSubjectsWithProperty(first).toSet();
		Set<RDFNode> objects_rest = m.listObjectsOfProperty(rest).toSet();

		// exclude subjects that are of type List
		subjects.removeAll(subjects_first);

		// exclude none root node 
		subjects_first.removeAll(objects_rest);
		
		// add converted list members
		Model mi = ModelFactory.createDefaultModel();
		Iterator<Resource> iter_sub = subjects_first.iterator();
		while (iter_sub.hasNext()) {
			Resource root = iter_sub.next();

			
			List<RDFNode> members = ToolJena.listListMembers(m, root, first, rest);
			Iterator<RDFNode> iter_member = members.iterator();
			
			while (iter_member.hasNext()){
				RDFNode member = iter_member.next();
				mi.add(mi.createStatement(root, part, member));
			}
			
			//keep the type of the root
			//mi.add(m.listStatements(root, RDF.type, (RDFNode)null));
		}

		//add the rest triples
		StmtIterator iter = m.listStatements();
		while (iter.hasNext()){
			Statement stmt = iter.nextStatement();
			
			if (bRemoveListTriple){
				if (!subjects.contains(stmt.getSubject()))
					iter.remove();
			}
		}

		// member statements
		m.add(mi);
	}	
	
	/**
	 * recursively traverse an RDF:List and put triples into a model
	 * 
	 * @param m
	 * @param root
	 * @return
	 */
	/*
	public static Model getModel_byList(Model m, Resource root) {
		Model list_m = ModelFactory.createDefaultModel();
		Resource node = root;
		while (null != node) {
			Iterator iter_obj = m.listStatements(node, RDF.type, RDF.List);
			if (iter_obj.hasNext()) {
				list_m.add((Statement) iter_obj.next());
			}

			iter_obj = m.listStatements(node, RDF.first, (RDFNode) null);
			if (iter_obj.hasNext()) {
				list_m.add((Statement) iter_obj.next());
			}

			iter_obj = m.listStatements(node, RDF.rest, (RDFNode) null);
			if (iter_obj.hasNext()) {
				Statement stmt = (Statement) iter_obj.next();
				list_m.add(stmt);
				if (stmt.getObject().isResource()) {
					node = (Resource) stmt.getObject();
				} else {
					node = null;
				}
			} else {
				node = null;
			}
		}
		return list_m;
	}
    */
	
	/**
	 * create a new model from the given model that includes triples only
	 * describing res.
	 * 
	 * Limitation: we only collect triples having the resource as the subject by
	 * assuming all resources in m are not anonymous. future work may consider
	 * better heuristics.
	 * 
	 * 
	 * @param m
	 *            the given graph
	 * @param res
	 *            the resource to be described
	 * @param bRecursive
	 *            recursively get the entire graph rooted from res 
	 * @return
	 */
/*	@SuppressWarnings("unchecked")
	public static Model getModel_byDescription(Model m, Resource res, boolean bRecursive) {
		Model m_desc = ModelFactory.createDefaultModel();
		m_desc.add(m.listStatements(res, null, (RDFNode) null));
		
		if (bRecursive){
			Set<RDFNode> visited = new HashSet<RDFNode>();
			visited.add(res);
			
			boolean bContinue=true;
			while (bContinue){
				//reset
				bContinue =false;
				
				Set targets = m_desc.listObjects().toSet();
				//remove visited
				targets.removeAll(visited);
				//remove literals
				{
					Iterator<RDFNode> iter = targets.iterator();
					while (iter.hasNext()){
						RDFNode target = iter.next();
						if (target.isLiteral())
							iter.remove();
					}
				}
				
				//update visited
				visited.addAll(targets);
				
				//add new triples if found
				Iterator<RDFNode> iter = targets.iterator();
				while (iter.hasNext()){
					RDFNode target = iter.next();
				
					StmtIterator iter_stmt = m.listStatements((Resource)target, null, (RDFNode) null);
					if (iter_stmt.hasNext()){
						// add new triple
						m_desc.add(iter_stmt);
						bContinue=true;
					}
				}
			}
				
		}
		// TODO maybe we want provide CBD support
		return m_desc;
	}
*/
	////////////////////////////////////////////////
	// namespace
	////////////////////////////////////////////////
	

	
	/** list all namespaces used in this model
	 *  TODO
	 *  
	 * @param m
	 * @return
	 */
	public static TreeSet<String> listNamespaceByJena(Model m){
		TreeSet<String> ret = new TreeSet<String>();
		ret.addAll(m.listNameSpaces().toSet());
		return ret;
	}
	
	public static Set<String> listNamespaceByParse(Set<Resource> set_res){
		Set<String> namespaces = new HashSet<String>();
		Iterator<Resource> iter = set_res.iterator();
		while (iter.hasNext()){
			Resource res = iter.next();
			if (res.isURIResource()){
				namespaces.add(DataQname.extractNamespace(res.getURI()));
			}
		}
		return namespaces;
	}	
	
	
	////////////////////////////////////////////////
	// model print
	////////////////////////////////////////////////
	public static boolean printModelToWriter(Model m, String sz_rdfsyntax, String sz_namespace, Writer out) throws IOException {
		if (null==m || null==out)
			return false;
		
		if (ToolSafe.isEmpty(sz_rdfsyntax))
			sz_rdfsyntax = RDFSYNTAX.RDFXML;

		if (ToolSafe.isEmpty(sz_namespace))
			sz_namespace = "";

		RDFWriter writer = m.getWriter(sz_rdfsyntax);
		if (RDFSYNTAX.RDFXML.equals(sz_rdfsyntax)||RDFSYNTAX.RDFXML_ABBREV.equals(sz_rdfsyntax)){
			writer.setProperty("showXmlDeclaration", "true");
			writer.setProperty("allowBadURIs", "true");
			writer.setProperty("relativeURIs","same-document");
		}
		if (RDFSYNTAX.TURTLE.equals(sz_rdfsyntax)){
			writer.setProperty("usePropertySymbols", "false");
			writer.setProperty("useTripleQuotedStrings", "false");
			writer.setProperty("useDoubles", "false");
		}
		writer.write(m, out, sz_namespace);
		out.flush();
		return true;
	}

	/**
	 * print model to an RDF/XML string
	 * 
	 * @param m
	 * @return
	 */
	public static String printModelToString(Model m) {
		return  printModelToString(m, null);
	}

	/**
	 * print model to a string
	 * 
	 * @param m
	 * @param rdfsyntax
	 * @return
	 */
	public static String printModelToString(Model m, String sz_rdfsyntax){
		return printModelToString(m, sz_rdfsyntax, null);
	}
	
	public static String printModelToString(Model m, String sz_rdfsyntax, String sz_namespace) {
		try {
			StringWriter sw = new StringWriter();
			printModelToWriter(m, sz_rdfsyntax, sz_namespace, sw);
			return sw.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}


	/**
	 * print model to file
	 * 
	 * @param model
	 * @param szFilename
	 * @return
	 */
	public static boolean printModelToFile(Model m, String szFilename) {
		return printModelToFile(m, new File(szFilename));
	}

	public static boolean printModelToFile(Model m, File f) {
		return printModelToFile(m, RDFSYNTAX.RDFXML ,f,  false);
	}


	public static boolean printModelToFile(Model model,String szRdfSyntax, File f, boolean bGzip) {
		return printModelToFile(model,szRdfSyntax,null, f, bGzip);
	}

	public static boolean printModelToFile(Model model, String sz_rdfsyntax, String sz_namespace, File f, boolean bGzip) {
		boolean bRet =true;
		OutputStream _fos =null;
		try {
			 _fos = ToolIO.prepareFileOutputStream(f,false, bGzip);
			PrintWriter out = ToolIO.prepareUtf8Writer(_fos);
			try {
				
				bRet = printModelToWriter(model, sz_rdfsyntax, sz_namespace, out);
			} catch (IOException e) {
				e.printStackTrace();
				bRet =false;
			}
			out.close();
		} catch (Sw4jException e) {
			e.printStackTrace();
		}
		if (null!=_fos)
			try {
				_fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}		
		return bRet;
	}	
	
	public static void printModel(Model m, String sz_rdfsyntax, String sz_namespace) {
		try {
			PrintWriter out = new PrintWriter(System.out);
			printModelToWriter(m, sz_rdfsyntax,sz_namespace, out);
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}

	/**
	 * just print the triples line by line
	 * 
	 * @param m
	 */
	public static void printModel(Model m) {
		printModel(m, RDFSYNTAX.NT,"");
	}

	


	// ////////////////////////////////////////////////////////
	//
	// ////////////////////////////////////////////////////////

	public static final String NODE_URI = "u"; //

	public static final String NODE_BNODE = "b"; //

	public static final String NODE_LITERAL = "l"; //

	public static String getNodeType(RDFNode node) {
		try {
			Resource temp = (Resource) node;
			if (temp.isAnon()) {
				return NODE_BNODE;
			} else {
				return NODE_URI;
			}
		} catch (Exception e) {
			return NODE_LITERAL;
		}
	}



	public static Individual getIndividual(String uriStr, OntModel model) {
		Individual result = null;
		if (model != null) {
			RDFNode _rdf = null;
			_rdf = model.getIndividual(uriStr);
			if (_rdf != null && _rdf.canAs(Individual.class)) {
				result = (Individual) _rdf.as(Individual.class);
			} else {
				System.out
						.println("DataObjectManager.getIndividual: Could not read individual with URI "
								+ uriStr);
			}
		}
		return result;
	}
	
	/*
	@SuppressWarnings("unchecked")
	public static Resource getMostspecificRDFType(Individual ind){
		List types = ind.listRDFTypes(true).toList();
		if (ToolCommon.safe_is_empty(types)){
			getLogger().info("empty types");
			return null;
		}
		if (types.size()==1){
			getLogger().debug("only one element, no need to work");
			return (Resource)types.iterator().next();
		}

		// collect required namespaces
		Iterator iter = types.iterator();
		LinkedHashSet<String> namespaces = new LinkedHashSet<String>();
		while (iter.hasNext()){
			String ns = ToolURI.extractNamespace(iter.next().toString());
			if (null!=ns)
				namespaces.add(ns);
		}	

		if (namespaces.size()<1){
			getLogger().info("no namespaces has been parsed from types "+ types);
			return null;
		}

		// build the model
		Model m = ModelFactory.createDefaultModel();
		iter = namespaces.iterator();
		while (iter.hasNext()){
			String ns = iter.next().toString();
			try {
				ModelManager.get().readModel(m, ns);
			} catch (SwutilException e) {
				getLogger().error(e.getMessage());
			}
		}
		
		return getMostspecificRDFType(types,m);
	}
	*/
	public static Resource getMostspecificRDFType(List<Resource> types, Model m){
		if (null==m){
			getLogger().info("empty model");
			return null;
		}
		
		if (ToolSafe.isEmpty(types)){
			getLogger().info("empty types");
			return null;
		}

		if (types.size()==1){
			getLogger().debug("only one element, no need to work");
			return types.iterator().next();
		}
		
		// build a map for rdfs:subClassOf relations
		DataPVCMap<Resource,Resource> mapSubclass = new DataPVCMap<Resource,Resource>(true);
		{
			StmtIterator iter = m.listStatements(null, RDFS.subClassOf, (RDFNode)null);
			while (iter.hasNext()){
				Statement stmt =  iter.nextStatement();
				if (!stmt.getObject().isResource()){
					getLogger().info("encountered a bad RDF triple "+ stmt);
				}
				mapSubclass.add( stmt.getSubject(), (Resource)stmt.getObject());
			}
		}
		
		// find the most specific type
		HashSet<Resource> frontClasses = new HashSet<Resource>();
		frontClasses.addAll(types);
		HashSet<Resource> superClasses = new HashSet<Resource>();
		{
			Iterator<Resource> iter = frontClasses.iterator();
			while (iter.hasNext()){
				Resource curRes = iter.next();
				if (superClasses.contains(curRes)){
					continue;
				}
				Collection<Resource> classes = mapSubclass.getValues(curRes);
				if (null!=classes){
					classes.remove(curRes); // should not add self
					superClasses.addAll(classes);
				}
			}
		}
		frontClasses.removeAll(superClasses);
		
		if (frontClasses.size()==1)
			return frontClasses.iterator().next();
		else{
			getLogger().info("expected exactly one most specific class, but found "+ frontClasses);
			return null;
		}
	}
	

	


/*
	@SuppressWarnings("unchecked")
	public static Dataset prepareDataset(Query query){
		Dataset dataset = DatasetFactory.create(query.getGraphURIs(), query.getNamedGraphURIs());
		
		
		if (usePellet){
			Iterator iter  = dataset.listNames();
			while (iter.hasNext()){
				String name = (String)iter.next();
				Model m = dataset.getNamedModel(name);
				try {
					model_createDeductiveClosure(m,m);
				} catch (Sw4jException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Model m = dataset.getDefaultModel();
			try {
				model_createDeductiveClosure(m,m);
			} catch (Sw4jException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dataset;
	}
		
	
	
	public static Object sparql_exec(String queryString, AgentSparql queryProcessor, String szRdfSyntax){
		if (null== queryProcessor)
			queryProcessor = new AgentSparql();
		
		Object ret = queryProcessor.exec(queryString, szRdfSyntax);
		//Query query = QueryFactory.create(queryString) ;
		
	//	Dataset dataset = DatasetFactory.create(query.getGraphURIs(), query.getNamedGraphURIs());
		//QueryExecution qexec = queryProcessor.sparql_exec(query, dataset);
		
		//if (usePellet){

			//Dataset dataset = prepareDataset(query);
			//qexec = ToolPellet.sparql_exec(dataset.getDefaultModel(), query);
		//}else{
			//Dataset dataset = prepareDataset(query);
			//qexec = QueryExecutionFactory.create(query, dataset) ;
		//}

		/*if (query.isDescribeType()){
			ret = qexec.execDescribe();
		}else if (query.isConstructType()){
			ret = qexec.execConstruct() ;
		}else if (query.isSelectType()){
			ResultSet results = qexec.execSelect() ;
			ByteArrayOutputStream sw = new ByteArrayOutputStream();
			if (RDFSYNTAX.SPARQL_XML.equals(szRdfSyntax)){
				ResultSetFormatter.outputAsXML(sw, results);
			}else if (RDFSYNTAX.SPARQL_JSON.equals(szRdfSyntax)){
				ResultSetFormatter.outputAsJSON(sw, results);
			}else{
				ResultSetFormatter.out(sw,results, query);				
			}
					
			ret = sw.toString();
		}else if (query.isAskType()){
			ret = qexec.execAsk() ;
		}
		
		qexec.close() ;
		return ret;
	}
	
	
	public static Model  sparql_create_describe(String queryString, AgentSparql queryProcessor, String szRdfSyntax){	
		Object ret = sparql_exec(queryString, queryProcessor,szRdfSyntax);
		if (ret instanceof Model)
			return (Model)ret;
		else
			return null;
		/*
		Query query = QueryFactory.create(queryString) ;
		
		Dataset dataset = prepareDataset(query, queryProcessor);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
		Model resultModel =null;
		if (query.isDescribeType())
			resultModel = qexec.execDescribe() ;
		else if (query.isConstructType())
			resultModel = qexec.execConstruct() ;
		else
			return null;
		
		qexec.close() ;
		return resultModel;
		
	}
	
	public static String  sparql_select(String queryString,  AgentSparql queryProcessor, String szRdfSyntax){
		Object ret = sparql_exec(queryString, queryProcessor, szRdfSyntax);
		if (ret instanceof String)
			return (String)ret;
		else
			return null;
		/*
		Query query = QueryFactory.create(queryString) ;
	
		//System.out.println(queryString);
		Dataset dataset = prepareDataset(query, queryProcessor);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
		ResultSet results =null;
		if (query.isSelectType())
			results = qexec.execSelect() ;
		else{
			return "";
		}

		//System.out.println(results.getRowNumber());
		// Output query results	
		ByteArrayOutputStream sw = new ByteArrayOutputStream();
		ResultSetFormatter.out(sw,results, query);
		String ret = sw.toString();
		//ret  = ret.replaceAll("\r", "<br/>\r");
		//String ret = ResultSetFormatter.asXMLString(results);
		
		// Important - free up resources used running the query
		qexec.close() ;
		
		return ret;
		
	}	

	public static boolean sparql_ask (String queryString,  AgentSparql queryProcessor, String szRdfSyntax){
		Object ret = sparql_exec(queryString, queryProcessor, szRdfSyntax);
		if (ret instanceof Boolean)
			return (Boolean)ret;
		else
			return false;
		/*
		Query query = QueryFactory.create(queryString) ;
	
		//System.out.println(queryString);
		Dataset dataset = prepareDataset(query, queryProcessor);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
		boolean ret = false;
		if (query.isAskType())
			ret = qexec.execAsk() ;
		else{
			return ret;
		}			
		
		// Important - free up resources used running the query
		qexec.close() ;
		
		return ret;
		
	}		
	*/
	/*
	public static  Model model_createDeductiveClosure(Model deduction, Model m) throws Sw4jException{
		OntModel ont;
		if ( m instanceof OntModel){
			ont = (OntModel) m;
		}else{
			ont = ToolPellet.createOntModel();
			ToolJena.model_merge(ont, m);
		}

		//ont.setStrictMode( false );

		
		// invalid model should not have any deduction model
		if (!ont.validate().isValid()){
			throw new Sw4jException(Sw4jMessage.STATE_WARNING, ont.validate().getReports().next().toString());
		}
		
		if (null==deduction)
			deduction = ModelFactory.createDefaultModel();

		
		ToolJena.model_merge(deduction,ont);
		
		return deduction;
	}
	*/
	//TODO
	/*
	public static void model_add_transtive(Model m, Property p){
		Model m1 = ModelFactory.createDefaultModel();
		m1.add(m.listStatements(null, p, (String)null) );
		Model m2 = ModelFactory.createRDFSModel(m1);
		StmtIterator iter= m2.listStatements(null, p, (String)null);
		while (iter.hasNext()){
			Statement stmt = iter.next();
			if (ToolJena.test_meta_namespace(stmt.getSubject())){
				continue;
			}
			
			if (ToolJena.test_meta_namespace(stmt.getObject())){
				continue;
			}
			m.add(stmt);
		}
	}
	*/
//	public static void updateModelTranstive(Model m, Property p){
//		HashMap<Integer, Resource> map_id_resource = new  HashMap<Integer, Resource>();
//		HashMap<Resource, Integer> map_resource_id = new  HashMap<Resource,Integer>();
//		
//		DataPVHMap<Integer,Integer> map_dag= new DataPVHMap<Integer,Integer>();
//		int id =1;
//		{
//			Iterator<Statement> iter = m.listStatements(null,p, (String)null);
//			while (iter.hasNext()){
//				Statement stmt = iter.next();
//				
//			//	if (stmt.getObject().isAnon())
//			//		continue;
//				
//				Resource subject = stmt.getSubject();
//				Integer nid_subject = map_resource_id.get(subject);
//				if (null==nid_subject){
//					nid_subject = id;
//					map_resource_id.put(subject,nid_subject);
//					map_id_resource.put(nid_subject, subject);
//					id++;
//				}
//				
//				Resource object = (Resource) stmt.getObject();
//				Integer nid_object = map_resource_id.get(object);
//				if (null==nid_object){
//					nid_object = id;
//					map_resource_id.put(object,nid_object);
//					map_id_resource.put(nid_object, object);
//					id++;
//				}
//				
//				map_dag.add(nid_subject,nid_object);
//			}
//		}
//		
//		DataDigraph dag = DataDigraph.create(map_dag);
//		DataDigraph tc = dag.create_tc();
//		tc.reflex();
//
//		long size_before = m.size();
//		for (int nid_subject: tc.getFrom()){
//			Resource subject = map_id_resource.get(nid_subject);
//			
//			for (int nid_object: tc.getTo(nid_subject)){
//				Resource object = map_id_resource.get(nid_object);
//				
//				m.add(m.createStatement(subject, p, object));
//			}
//		}
//		
//		String message= "";
//		message +=String.format("added %d edges to (%d) after transitive inference \n", tc.size()-dag.size(),dag.size());
//		message += String.format("added %d triples to (%d) after transitive inference \n", m.size()-size_before,size_before);
//		getLogger().info(message);
//	}
	
//	public static Model create_deduction(Model m){
//		Model deduction = ModelFactory.createDefaultModel();
//		deduction.add(m);
//		ToolJena.updateModelTranstive(deduction, RDFS.subClassOf);
//		ToolJena.updateModelTranstive(deduction, RDFS.subPropertyOf);
//		return deduction;
//	}

	public static boolean test_meta_namespace(RDFNode node){
		if (!node.isURIResource())
			return false;
		Resource res = (Resource) node;
		if (ToolSafe.isEmpty(res.getNameSpace()))
			return false;
		
		if (RDF.getURI().equals(res.getNameSpace())){
			return true;
		}
		if (RDFS.getURI().equals(res.getNameSpace())){
			return true;
		}
		if (OWL.getURI().equals(res.getNameSpace())){
			return true;
		}
		
		return false;
	}
	

	
	/**
	 * create model A-B
	 * @param m
	 * @param ref
	 */
	public static Model create_diff(Model m_a, Model m_b){
		if (ToolSafe.isEmpty(m_a))
			return null;
		
		if (ToolSafe.isEmpty(m_b))
			return null;

		Model ret = ModelFactory.createDefaultModel();
		Model signed_a = create_signBlankNode(m_a,null);
		Model signed_b = create_signBlankNode(m_b,null);
		update_copy(ret,signed_a);
		ret.remove(signed_b);
	
		return create_unsignBlankNode(ret,m_a);
	}
	
	public static Model create_unsignBlankNode(Model m, Model ref){
		Set<Resource> subjects = m.listSubjects().toSet();
		subjects.removeAll(ref.listSubjects().toSet());
		return create_unsignBlankNode(m, subjects); 
	}
	
	public static Model create_rename(Model m, Map<RDFNode,Resource> map_from_to){
		Model ret = ModelFactory.createDefaultModel();
		StmtIterator iter = m.listStatements();
		while (iter.hasNext()){
			Statement stmt = iter.nextStatement();
			
			Resource subject = stmt.getSubject();
			RDFNode object = stmt.getObject();
			
			//skip
			if (map_from_to.keySet().contains(subject))
				subject = map_from_to.get(subject);

			if (map_from_to.keySet().contains(object))
				object = map_from_to.get(object);
			
			ret.add(ret.createStatement(subject, stmt.getPredicate(),object));

		}

		update_copyNsPrefix(ret,m);

		return ret;
	}

	/** 
	 * list all same as relation in m and create an all same model
	 * @param m
	 * @return
	 */

//	
//	public static Model create_allsame(Collection<Set<Resource>> groups, String szNamespace){
//		Model m = ModelFactory.createDefaultModel();
//		int gid = 1;
//		for (Set<Resource> set_info: groups){
//			Resource subject = m.createResource();
//			if (ToolURI.isUriHttp(szNamespace)){
//				subject= m.createResource(szNamespace+"group"+gid);
//				gid++;
//			}
//			subject.addProperty(RDF.type, PMLR.AllSame);
//			for (Resource res : set_info){
//				subject.addProperty(PMLR.hasMember, res);
//			}
//		}
//		m.setNsPrefix( PMLR.class.getSimpleName().toLowerCase(),PMLR.getURI());
//		return m;
//		
//	}

	public static Model create_sameas(Collection<Set<Resource>> groups){
		Model model_sameas = ModelFactory.createDefaultModel();
		for (Set<Resource> set_info: groups){
			//no need to map single element set
			if (set_info.size()<2)
				continue;
			
			Resource res_info_root = null;
			for (Resource res_info: set_info){
				
				if (null==res_info_root)
					res_info_root = res_info;
				else{
					model_sameas.add(model_sameas.createStatement(res_info_root, OWL.sameAs, res_info));
				}
			}
		}
		return model_sameas;
	}

	/**
	 * merge a collection of models into a new model
	 * @param ref
	 * @return
	 */
	public static Model create_copy(Collection<Model> ref){
		Model m = ModelFactory.createDefaultModel();
		update_copy(m, ref);
		return m;
	}
	/**
	 * add a list of models to model m
	 * @param m
	 * @param ref
	 */
	public static void update_copy(Model m, Collection<Model> set_ref){
		for (Model ref : set_ref)
			update_copy(m,ref);
	}
	
	/**
	 * add a model to a model m
	 * @param m
	 * @param ref
	 */
	public static void update_copy(Model m, Model ref){
		if (ToolSafe.isEmpty(ref))
			return;
		
		if (!isConsistent(ref)){
			//System.out.println("inconsistent reference");
			return;
		}
		
		if (!isConsistent(m)){
			//System.out.println("inconsistent reference");
			return;
		}
				
		m.add(ref);
		update_copyNsPrefix( m,ref);						

	}
	
	/**
	 * unsign some instance to blank node
	 * @param m
	 * @param type
	 * @return
	 */
	public static Model create_unsignBlankNode(Model m, String type){
		return create_unsignBlankNode(m, m.createResource(type));
	}
	
	public static Model create_unsignBlankNode(Model m, Resource type){
		return create_unsignBlankNode(m, m.listSubjectsWithProperty(RDF.type, type).toSet()); 
	}
	
	public static Model create_unsignBlankNode(Model m, Collection<Resource> set_res){
		HashMap<RDFNode,Resource> map_res_bnode = new HashMap<RDFNode,Resource>();
		for(Resource subject: set_res){
			map_res_bnode.put(subject, m.createResource());
		}
		
		return create_rename(m, map_res_bnode); 
	}
	
	private static Resource resource_sign_bnode(Model m, Resource node, String sz_namespace){
		return m.createResource(sz_namespace+"_"+node.getId().getLabelString());
	}
	
	public static Model create_signBlankNode(Model m, String sz_namespace) {
		if (ToolSafe.isEmpty(sz_namespace)){
			sz_namespace="http://inference-web.org/vocab/";
			m.setNsPrefix("iwv",sz_namespace);
		}

		HashMap<RDFNode, Resource> map_bnode_res = new HashMap<RDFNode, Resource>();
		Iterator<Statement> iter = m.listStatements();
		while (iter.hasNext()){
			Statement stmt = iter.next();
			Resource node = stmt.getSubject();
			if (node.isAnon()){
				map_bnode_res.put(node, resource_sign_bnode(m,node, sz_namespace));
			}
			if (stmt.getObject().isAnon()){
				node =(Resource) stmt.getObject();
				map_bnode_res.put(node, resource_sign_bnode(m,node, sz_namespace));
			}
				
		}
		return create_rename(m, map_bnode_res); 

	}
	
	public static Model create_signBlankNode_hash(Model m, String namespace ){
		if (ToolSafe.isEmpty(namespace)){
			namespace="http://inference-web.org/vocab/";
			m.setNsPrefix("iwv",namespace);
		}
		
		//partition
		DataPVHMap<RDFNode, String> map_s_bnode = new DataPVHMap<RDFNode, String>();
		HashMap<RDFNode,RDFNode> map_os_bnode = new HashMap<RDFNode,RDFNode>();
		DataPVHMap<RDFNode, Statement> map_o_bnode = new DataPVHMap<RDFNode, Statement>();
		{
			StmtIterator iter = m.listStatements();
			while (iter.hasNext()){
				Statement stmt = iter.nextStatement();
				
				//skip
				if (!stmt.getSubject().isAnon())
					continue;
				
				if (stmt.getObject().isAnon()){
					map_o_bnode.add(stmt.getObject(),stmt);
					map_os_bnode.put(stmt.getObject(), stmt.getSubject());
				}else{
					String hash_pv= ToolHash.hash_sum_md5(String.format("%s-%s", stmt.getPredicate().getURI(), ToolJena.getNodeString(stmt.getObject())).getBytes());
					map_s_bnode.add(stmt.getSubject(), hash_pv);
				}
			}
		}
		
		//update uri 
		HashMap<RDFNode, Resource> map_bnode_res = new HashMap<RDFNode, Resource>();
		while (true){
			Set<RDFNode> terminal = new HashSet<RDFNode>(map_s_bnode.keySet());
			terminal.removeAll(map_os_bnode.values());
			
			if (terminal.size()==0)
				break;
			
			//create uri
			Iterator<RDFNode> iter = terminal.iterator();
			while (iter.hasNext()){
				RDFNode bnode= iter.next();
				TreeSet<String> temp = new TreeSet<String>(map_s_bnode.getValues(bnode));
				String hash = ToolHash.hash_sum_md5(temp.toString().getBytes());
				String bnode_uri = namespace+"_"+hash;
				Resource bnode_res = m.createResource(bnode_uri);
				map_bnode_res.put(bnode, bnode_res);
				map_s_bnode.remove(bnode);

				//update
				Iterator<Statement> iter_stmt = map_o_bnode.getValues(bnode).iterator();
				while (iter_stmt.hasNext()){
					Statement stmt = iter_stmt.next();

					String hash_pv= ToolHash.hash_sum_md5(String.format("%s-%s", stmt.getPredicate().getURI(), bnode_uri).getBytes());
					map_s_bnode.add(stmt.getSubject(), hash_pv);
				}
				map_os_bnode.remove(bnode);
				
			}
		}
		
		return create_rename(m, map_bnode_res); 

	}

	/**
	 * copy ns prefix declaration from the other model (ref)
	 * @param m
	 * @param ref
	 */
	public static void update_copyNsPrefix(Model m, Model ref){
		if (!isConsistent(m))
			return;

		if (!isConsistent(ref))
			return;

		//System.out.println(ref.getNsPrefixMap());
		m.setNsPrefixes(ref.getNsPrefixMap());
		/*NsIterator iter = ref.listNameSpaces();
		while (iter.hasNext()){
			String ns = iter.nextNs();
			String prefix = ref.getNsURIPrefix(ns);
			if (null!= prefix)
				m.setNsPrefix(prefix,ns);
		}*/
		//System.out.println(m.getNsPrefixMap());
		
 	}	
	
	public static boolean isConsistent(Model m){
		if (m instanceof OntModel){
			OntModel ont =(OntModel) m;
			return ont.validate().isValid();
		}
		
		return true;
	}
	/**
	 * add m to target
	 * @param target
	 * @param m
	 */
	public static Model create_copy( Model m){
		Model target= ModelFactory.createDefaultModel();
		update_copy(target, m);
		return target;
	}

/*
	public static TreeSet<String> extractLinks(Model m, boolean bUseImport, boolean bUseNamespace){
		TreeSet<String> new_urls = new TreeSet<String>(); 
		
		// find new urls using owl:imports
		if (null!=m){
			if (bUseImport){
				//all_namespaces.add(szOntologyURI);	// add imported namespace
				NodeIterator iter_import = m.listObjectsOfProperty(OWL.imports);
				while (iter_import.hasNext()){
					RDFNode node = iter_import.nextNode();
					if (node.isURIResource()){
						String szNewURL = ((Resource)node).getURI();
						szNewURL = ToolURI.extractNamespaceCanonical(szNewURL);
						if (null!= szNewURL)
							new_urls.add(szNewURL);
					}
				}
			}
			if (bUseNamespace){
				//all_namespaces.add(szOntologyURI);	// add imported namespace
				ToolModelStat stat = new ToolModelStat();
				stat.traverse(m);
	
				Iterator<String> iter_ns = stat.getReferencedOntologies().iterator();
				while (iter_ns.hasNext()){
					String szNewURL = iter_ns.next();
					szNewURL = ToolURI.extractNamespaceCanonical(szNewURL);
					if (null!= szNewURL)
						new_urls.add(szNewURL);
					
				}
			}
		}
		
		// skip any RDF, RDFS, OWL namespace
		new_urls.removeAll(ToolModelStat.getMetaMamespaces());
		
		return new_urls;
	}
*/
	
	
	public static  String fromatRDFnode(RDFNode node, boolean bDetail){
		if (ToolSafe.isEmpty(node))
			return "";

		if (node.isURIResource()){
			DataQname dq;
			try {
				dq = DataQname.create(getNodeString(node), getNodePrefix(node));
				String szLn = dq.getLocalname();
				String szNs = dq.getNamespace();
				
				if (!ToolSafe.isEmpty(szLn)&& !ToolSafe.isEmpty(szNs) && !bDetail){
					return String.format("[%s]",szLn); 
				}
			} catch (Sw4jException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			return "<"+node.toString()+">";
		}else if (node.isAnon()){
			return "<"+node.toString()+">";			
		}else{
			return node.toString();
		}
	}	
	
	public static String getNodeString(RDFNode node) {
		if (ToolSafe.isEmpty(node))
			return "";
		
		String type = getNodeType(node);
		if (type.equals(NODE_LITERAL)) {
			return ((Literal) node).getString().trim();
		} else {
			Resource res = (Resource) node;
			return getNodeStringRes(res);
		}
	}

	public static String getNodeStringRes(Resource res) {
		if (ToolSafe.isEmpty(res))
			return "";

		// I have seen some ugly swds which contain white-spaces at the end of
		// URI
		// e.g.
		// http://www.dbis.informatik.uni-frankfurt.de/~tolle/RDF/FondsSchema/Schema/Asset.rdf
		// so I have to trim the NodeString when accessing the URI.
		return res.toString().trim();
	}	
	public static Literal createDatetimeLiteral(Model m, long millisecond){
		return m.createTypedLiteral(ToolString.formatXMLDateTime(millisecond), XSDDatatype.XSDdateTime);
	}

	public static Set<String> listNameSpacesData(Model m){
		TreeSet<String> ret = new TreeSet<String>();
		Iterator<Statement> iter = m.listStatements();
		while (iter.hasNext()){
			Statement stmt = iter.next();
			String  ns;
			Resource res = stmt.getSubject();
			ns = DataQname.extractNamespace(res.getURI());
			if (!ToolSafe.isEmpty(ns))
				ret.add(ns);

			if (stmt.getPredicate().equals(RDF.type))
				continue;

			if (!stmt.getObject().isURIResource())
				continue;
			res = (Resource)stmt.getObject();
			ns = DataQname.extractNamespace(res.getURI());
			if (!ToolSafe.isEmpty(ns))
				ret.add(ns);
		
		}
		return ret;
	}
	public static Set<String> listNameSpaces(Model m){
		TreeSet<String> ret = new TreeSet<String>();
		Iterator<Statement> iter = m.listStatements();
		while (iter.hasNext()){
			Statement stmt = iter.next();
			String  ns;
			Resource res = stmt.getSubject();
			ns = DataQname.extractNamespace(res.getURI());
			if (!ToolSafe.isEmpty(ns))
				ret.add(ns);

			res = stmt.getPredicate();
			ns = DataQname.extractNamespace(res.getURI());
			if (!ToolSafe.isEmpty(ns))
				ret.add(ns);

			if (!stmt.getObject().isURIResource())
				continue;
			res = (Resource)stmt.getObject();
			ns = DataQname.extractNamespace(res.getURI());
			if (!ToolSafe.isEmpty(ns))
				ret.add(ns);
		
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static Set listLinkedResources(Model m){
		Set<RDFNode> ret = m.listObjects().toSet();
		ret.addAll(m.listSubjects().toSet());
		ret.removeAll(m.listObjectsOfProperty(RDF.type).toSet());

		Iterator<RDFNode> iter = ret.iterator();
		while (iter.hasNext()){
			RDFNode node = iter.next();
			if (!node.isURIResource()){
				iter.remove();
				continue;
			}
			//to simplify the case, only include Resource with URI
			try {
				DataQname dq = DataQname.create(getNodeString(node), getNodePrefix(node));
				if (dq.hasLocalname()){
					iter.remove();
					continue;
				}
			} catch (Sw4jException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
/*			//as of now, skip obvious non-rdf data by file extension
			String uri = node.toString().toLowerCase();
			final String [] FILTER = new String []{
				".jpeg",	
				".jpg",	
				".png",	
				".gif",	
			};
			for (int i=0; i<FILTER.length; i++){
				if (uri.endsWith(FILTER[i]))
					iter.remove();
			}
*/		}
		
		return ret;
	}

	
	public static String getNodePrefix(RDFNode node) {
		if (node.isURIResource()){
			Resource res = (Resource) node;
			
			String szNamespace = res.getNameSpace();
			if (null!=szNamespace){
				String szPrefix = ToolModelAnalysis.getKnownNamespacePrefix(szNamespace);
				//System.out.println(res.getModel().getNsPrefixMap());
				if (ToolSafe.isEmpty(szPrefix))
					szPrefix = res.getModel().getNsURIPrefix(szNamespace);
				return szPrefix;
			}
		}
		return null;
	}
	
	
	public static DataQname getDataQname(RDFNode node){
		if (node.isURIResource()){
			try {
				Resource res = (Resource)node;
				String prefix = ToolJena.getNodePrefix(node);
				if (!ToolSafe.isEmpty(res.getLocalName())&&!ToolSafe.isEmpty(res.getNameSpace())){
					//use jena results
					return DataQname.create(res.getNameSpace(), res.getLocalName(), prefix);
				}else{
					//we guess it
					return DataQname.create(((Resource)node).getURI(), prefix);
				}
			} catch (Sw4jException e) {
			}
		}
		return null;

	}

	public static String getValueOfProperty(Model m, Resource s, Property p, String default_value) {
		 NodeIterator iter_node = m.listObjectsOfProperty(s,p);
		 if (iter_node.hasNext()){
			 return getNodeString(iter_node.nextNode());
		 }else{
			 return default_value;
		 }
	}
	public static Resource getValueOfProperty(Model m, Resource s, Property p, Resource default_value) {
		 NodeIterator iter_node = m.listObjectsOfProperty(s,p);
		 if (iter_node.hasNext()){
			 RDFNode node= iter_node.nextNode();
//			 if (node.isURIResource())
				 return (Resource)node;
		 }
		 return default_value;
	}
	
	
	public static String prettyPrint(RDFNode node){
		if (node.isLiteral()){
			return ((Literal)node).getString();
		}else if (node.isURIResource()){
			DataQname ret = getDataQname(node);
			if (null!=ret && null!=ret.getPrefix() && !ToolSafe.isEmpty(ret.getLocalname())){
				return ret.getPrefix()+":"+ ret.getLocalname();
			}else{
				return node.toString();
			}
		}else {
			return ((Resource)node).getId().toString();
		}
	}
	
	public static Model create_rename(Model m, DataObjectGroupMap<Resource> map_res_id, String sz_namespace){
		HashMap<RDFNode,Resource> map_from_to = new HashMap<RDFNode,Resource>();
		for (Resource res: map_res_id.getObjects()){
			Integer gid= map_res_id.getGid(res);
			map_from_to.put(res, m.createResource(sz_namespace+"id"+gid));
		}
		return create_rename(m,map_from_to);
	}

	public static Model create_filter(Model m, Property[] properties) {
		HashSet<Property> set_property = new HashSet<Property>();
		for (Property property: properties){
			set_property.add(property);
		}
		
		Model ret= ModelFactory.createDefaultModel();
		for (Statement stmt: m.listStatements().toList()){
			if (set_property.contains(stmt.getPredicate()))
				continue;
			
			ret.add(stmt);
		}
		return ret;
		
	}

	/** copy description of instance 
	 * 
	 * @param model_data
	 * @param model_ref
	 * @param res_subject
	 * @param prop
	 * @param bRecursive
	 */

	public static void update_copyResourceDescription(Model model_data, Model model_ref, Resource res_subject, Property prop, boolean bRecursive){
		if (ToolSafe.isEmpty(res_subject))
			return;
		
		StmtIterator iter =  model_ref.listStatements(res_subject,prop,(String)null);
		if (!iter.hasNext())
			return;
		for (Statement stmt:iter.toSet()){
			model_data.add(res_subject, stmt.getPredicate(), stmt.getObject());
			if (bRecursive && stmt.getObject().isResource()){
				Collection<Resource> subjects = model_data.listSubjects().toSet();
				//skip copied resoruces
				if (!subjects.contains(stmt.getObject()))
					update_copyResourceDescription(model_data, model_ref, (Resource)stmt.getObject(),null, bRecursive);
			}
		}
	}
	
	
	public static void update_addInstance(Model model_data, DataSmartMap ind, String prop_namespace, String ind_uri){
		Resource subject = model_data.createResource();
		if (!ToolSafe.isEmpty(ind_uri))
			subject = model_data.createResource(ind_uri);
		
		for (Map.Entry<String,Object> entry : ind.getData().entrySet()){
			String prop = entry.getKey();
			Object value = entry.getValue();
			
			Property p =  model_data.createProperty(prop_namespace+prop);
			
			update_addTriple(subject, p, value.toString());
		}
	}
	
	public static boolean update_addTriple(Resource res, Property property, String value){
		if (ToolSafe.isEmpty(value))
			return false;
		value = value.trim();

		//process http url
		String szURL= ToolString.parse_hyperlink(value);
		if (!ToolSafe.isEmpty(szURL)){
			URI uri;
			try {
				uri = ToolURI.string2uri(szURL);
				if (!ToolSafe.isEmpty(uri)&&!ToolSafe.isEmpty(uri.toString()) ){
					Resource obj = res.getModel().createResource(uri.toString());
					res.addProperty(property, obj);
					
					//correctly processed url
					return true;
				}
			} catch (Sw4jException e) {
			}
		}

		//do normal literal triple generation
		String value_norm = ToolString.filter_control_character(value);
		//ToolString.protectSpecialCharactersForXml(value);

		if (ToolSafe.isEmpty(value_norm))
			return false;

		if ("NA".equalsIgnoreCase(value_norm))
			return false;

		if (!value.equals(value_norm)){
			System.out.println("filtered control charaters: "+ value_norm +" FROM "+ value);
		}

		//parse typed literal
		//try{
		//	res.addLiteral(property, Boolean.parseBoolean(value_norm));	
		//	return  true;
		//}catch(NumberFormatException e){
		//}

		try{
			Integer.parseInt(value_norm);
			
			res.addProperty(property, value_norm, XSDDatatype.XSDinteger);			
			return  true;
		}catch(NumberFormatException e){
		}

		try{
			res.addLiteral(property, Float.parseFloat(value_norm));			
			return  true;
		}catch(NumberFormatException e){
		}
		
		// if now type can be determined, use plain literal
		res.addProperty(property, value_norm);			

		return true;
	}
/**
 * create concise bounded description
 * i.e. list all triples decribing the subject, and recursively, include triples describing the blank-node objects of the descriptions.
 * @param m
 * @param subject
 * @return
 */
	public static Model createCBD(Model m, Resource subject){
		Model mret= ModelFactory.createDefaultModel();
		mret.add(m.listStatements(subject, null, (String)null));
		for (RDFNode node : mret.listObjects().toList()){
			if (node.isAnon()){
				mret.add(createCBD(m,(Resource)node));
			}
		}
		update_copyNsPrefix(mret,m);
		return mret;
	}

	
	
	/**
	 * decompose a model to RDF molecules
	 * 
	 * @param m
	 * @return
	 */
	
	public static Collection<Model> decompse_rdf_molecule(Model m){
		Collection<Model> models = new ArrayList<Model>();
		Set<Statement> stmts = m.listStatements().toSet();
		DataObjectGroupMap<Statement> grp = new DataObjectGroupMap<Statement>();
		
		for (Statement stmt1: stmts){
			grp.addObject(stmt1);
			for (Statement stmt2: stmts){
				if (stmt2.getSubject().isAnon() && stmt2.getSubject().equals(stmt1.getObject())){
					grp.addSameObjectAs(stmt1, stmt2);
				}
				if (stmt1.getSubject().isAnon() && stmt1.getSubject().equals(stmt2.getObject())){
					grp.addSameObjectAs(stmt1, stmt2);
				}
			}
		}
		
		for (Integer gid: grp.getGids()){
			Model mret= ModelFactory.createDefaultModel();
			for (Statement stmt: grp.getObjectsByGid(gid)){
				mret.add(stmt);
			}
			models.add(mret);
		}
		return models;
	}

	/**
	 * print cannonical string for an RDF graph
	 * 
	 * sort triple
	 *    1. subject ( string order(URI resource); all equal(blank node) )
	 *    2. predicate ( string order(URI resource) )
	 *    3. object  ( string order (URI resource); string order (Literal); all equal(blank node) )    
	 * 
	 * @param m
	 * @return
	 */
	public static String printModel_cannonical(Model m){
		String ret="";
		Iterator<Statement> iter = m.listStatements();
		while (iter.hasNext()){
			Statement stmt =iter.next();
			if (!stmt.getSubject().isAnon() && !stmt.getObject().isAnon() )
				iter.remove();
		}
		
		ToolJena.printModel(m);
		
		TreeMap<String,Statement> stmts = new TreeMap<String,Statement>();
		for(Statement stmt: m.listStatements().toList()){
			String key = stmt.toString();
			
			//pritn statement into key
			String s= stmt.getSubject().getURI();
			String p = stmt.getPredicate().getURI();
			String o = stmt.getObject().toString();
			
			if (stmt.getSubject().isAnon()){
				s="";
			}
			if (stmt.getObject().isAnon()){
				o="";
			}else if (stmt.getObject().isLiteral()){
				o= ((Literal)stmt.getObject()).getString();
			}
				
			if (stmt.getObject().isResource()){
				key = String.format("<%s> <%s> <%s> .", s, p, o);
			}else{
				key = String.format("<%s> <%s> \"%s\" .", s, p, o);				
			}
			
			//System.out.println (key);
			
			stmts.put(key, stmt);
		}
		
		
		
		for (Statement stmt: stmts.values()){
			System.out.println (stmt);
		}
		
		
		return ret;
	}
	
	
	public static String getNTripleString(RDFNode node){
		if (node.isAnon()){
			return node.toString();
		}else if (node.isLiteral()){
			Literal lit = (Literal)node;
			String ret = String.format("\"%s\"",lit.getValue());
			if (null!=lit.getDatatypeURI())
				ret +=String.format("^^<%s>",lit.getDatatypeURI());
			return ret;
		}else{
			return "<"+((Resource)node).getURI()+">";	
		}
	}

	public static void updateModelTranstive(Model m, Property p){
		HashMap<Integer, Resource> map_id_resource = new  HashMap<Integer, Resource>();
		HashMap<Resource, Integer> map_resource_id = new  HashMap<Resource,Integer>();
		
		DataPVHMap<Integer,Integer> map_dag= new DataPVHMap<Integer,Integer>();
		int id =1;
		{
			Iterator<Statement> iter = m.listStatements(null,p, (String)null);
			while (iter.hasNext()){
				Statement stmt = iter.next();
				
			//	if (stmt.getObject().isAnon())
			//		continue;
				
				Resource subject = stmt.getSubject();
				Integer nid_subject = map_resource_id.get(subject);
				if (null==nid_subject){
					nid_subject = id;
					map_resource_id.put(subject,nid_subject);
					map_id_resource.put(nid_subject, subject);
					id++;
				}
				
				Resource object = (Resource) stmt.getObject();
				Integer nid_object = map_resource_id.get(object);
				if (null==nid_object){
					nid_object = id;
					map_resource_id.put(object,nid_object);
					map_id_resource.put(nid_object, object);
					id++;
				}
				
				map_dag.add(nid_subject,nid_object);
			}
		}
		
		DataDigraph dag = DataDigraph.create(map_dag);
		DataDigraph tc = dag.create_tc();
		tc.reflex();

		long size_before = m.size();
		for (int nid_subject: tc.getFrom()){
			Resource subject = map_id_resource.get(nid_subject);
			
			for (int nid_object: tc.getTo(nid_subject)){
				Resource object = map_id_resource.get(nid_object);
				
				m.add(m.createStatement(subject, p, object));
			}
		}
		
		String message= "";
		message +=String.format("added %d edges to (%d) after transitive inference \n", tc.size()-dag.size(),dag.size());
		message += String.format("added %d triples to (%d) after transitive inference \n", m.size()-size_before,size_before);
		getLogger().info(message);
	}
}
