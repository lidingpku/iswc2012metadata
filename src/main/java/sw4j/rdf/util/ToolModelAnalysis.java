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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import sw4j.util.AbstractPropertyValuesMap;
import sw4j.util.DataPVHMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
/**
 * heuristics functions for analyzing a model
 * 
 * @author Li Ding
 *
 */
public class ToolModelAnalysis {
	private static Logger getLogger() {
		return Logger.getLogger(ToolModelAnalysis.class);
	}
	
	/**
	 * the meta classes
	 * 
	 */
	static HashSet<Resource> META_CLASS = new HashSet<Resource>();
	static {
		getLogger().info("initialize META_CLASS");
		
		META_CLASS.add(RDFS.Class);
		META_CLASS.add(RDFS.Datatype);
		META_CLASS.add(OWL.Class);
		META_CLASS.add(OWL.Restriction);
		META_CLASS.add(OWL.DataRange);
		META_CLASS.add(OWL.DeprecatedClass);
		META_CLASS.add(DAML_OIL.Class);
		META_CLASS.add(DAML_OIL.Datatype);
		META_CLASS.add(DAML_OIL.Restriction);
	}

	/**
	 * if a resource is meta class
	 * 
	 * @param node
	 * @return
	 */
	public static boolean testMetaClass(RDFNode node) {
		if (null == node || node.isAnon()) {
			return false;
		}

		return META_CLASS.contains(node);
	}

	/**
	 * meta properties
	 */
	static HashSet<Resource> META_PROPERTY = new HashSet<Resource>();
	static {
		getLogger().info("initialize META_PROPERTY");
		
		META_PROPERTY.add(RDF.Property);
		META_PROPERTY.add(OWL.ObjectProperty);
		META_PROPERTY.add(OWL.DatatypeProperty);
		META_PROPERTY.add(OWL.AnnotationProperty);
		META_PROPERTY.add(OWL.DeprecatedProperty);
		META_PROPERTY.add(OWL.FunctionalProperty);
		META_PROPERTY.add(OWL.InverseFunctionalProperty);
		META_PROPERTY.add(OWL.SymmetricProperty);
		META_PROPERTY.add(OWL.OntologyProperty);
		META_PROPERTY.add(OWL.TransitiveProperty);
		META_PROPERTY.add(DAML_OIL.Property);
		META_PROPERTY.add(DAML_OIL.ObjectProperty);
		META_PROPERTY.add(DAML_OIL.DatatypeProperty);
		META_PROPERTY.add(DAML_OIL.TransitiveProperty);
		// META_PROPERTY.add(DAML_OIL.UnambigousProperty);
		META_PROPERTY.add(DAML_OIL.UniqueProperty);
	}

	/**
	 * if a resource is meta property
	 * 
	 * @param node
	 * @return
	 */
	public static boolean testMetaProperty(RDFNode node) {
		if (null == node || node.isAnon()) {
			return false;
		}
		// do not accept blank node
		return META_PROPERTY.contains(node);
	}

	/**
	 * meta namespace
	 */
	private static final String[] META_NS = new String[] { 
			RDF.getURI(),
			RDFS.getURI(), 
			OWL.getURI(), 
			DAMLVocabulary.NAMESPACE_DAML_2000_12_URI,
			DAMLVocabulary.NAMESPACE_DAML_2001_03_URI,
			XSD.getURI()};

	/**
	 * if the given uri is using meta namespace
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean useMetaNamespace(String uri) {
		if (null == uri || uri.length() == 0) {
			return false;
		}

		for (int i = 0; i < META_NS.length; i++) {
			if (uri.startsWith(META_NS[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static Set<String> getMetaMamespaces(){
		final HashSet<String> ret = new HashSet<String>();
		for (int i = 0; i < META_NS.length; i++) {
			ret.add(META_NS[i]);
			ret.add(META_NS[i].substring(0,META_NS[i].length()-1));
		}
		return ret;
	}
	
	public static final String RELATION_ONTO_IM = "o-(im)->o";
	public static final String RELATION_ONTO_PV = "o-(pv)->o";
	public static final String RELATION_ONTO_CPV = "o-(cpv)->o";
	public static final String RELATION_ONTO_IPV = "o-(ipv)->o";

	static HashMap<String,String> RELATION_ONTO = new HashMap<String,String>();
	static {
		getLogger().info("initialize RELATION_ONTO");
		RELATION_ONTO.put(OWL.imports.toString(),RELATION_ONTO_IM);
		RELATION_ONTO.put(DAML_OIL.imports.toString(),RELATION_ONTO_IM);

		RELATION_ONTO.put(OWL.priorVersion.toString(),RELATION_ONTO_PV);
		RELATION_ONTO.put(OWL.backwardCompatibleWith.toString(),RELATION_ONTO_CPV);
		RELATION_ONTO.put(OWL.incompatibleWith.toString(),RELATION_ONTO_IPV);
	}

	public static String testRelationOnto(String uri){
		// do not accept blank node
		return RELATION_ONTO.get(uri);
	}
	public static String testRelationOnto(Property predicate){
		String temp = ToolJena.getNodeString( predicate );
		return testRelationOnto(temp);
	}
	
	
	public static HashMap<String,String> RELATION_RES = new HashMap<String,String>();
	public static final String RELATION_RES_CC = "c->c";
	public static final String RELATION_RES_CP = "c->p";
	public static final String RELATION_RES_PC = "p->c";
	public static final String RELATION_RES_PP = "p->p";
	public static final String RELATION_RES_CLC = "c->l->c";
	public static final String RELATION_RES_C = "c->";
	
	static {
		RELATION_RES.put(RDFS.subClassOf.toString(),RELATION_RES_CC);
		RELATION_RES.put(DAML_OIL.subClassOf.toString(),RELATION_RES_CC);
		RELATION_RES.put(OWL.disjointWith.toString(),RELATION_RES_CC);
		RELATION_RES.put(DAML_OIL.disjointWith.toString(),RELATION_RES_CC);
		RELATION_RES.put(OWL.equivalentClass.toString(),RELATION_RES_CC);
		RELATION_RES.put(DAML_OIL.sameClassAs.toString(),RELATION_RES_CC);
		RELATION_RES.put(OWL.complementOf.toString(),RELATION_RES_CC);
		RELATION_RES.put(DAML_OIL.complementOf.toString(),RELATION_RES_CC);
		RELATION_RES.put(OWL.allValuesFrom.toString(),RELATION_RES_CC);
		RELATION_RES.put(OWL.someValuesFrom.toString(),RELATION_RES_CC);


		RELATION_RES.put(OWL.onProperty.toString(),RELATION_RES_CP);
		RELATION_RES.put(DAML_OIL.onProperty.toString(),RELATION_RES_CP);

		RELATION_RES.put(RDFS.domain.toString(),RELATION_RES_PC);
		RELATION_RES.put(DAML_OIL.domain.toString(),RELATION_RES_PC);
		RELATION_RES.put(RDFS.range.toString(),RELATION_RES_PC);
		RELATION_RES.put(DAML_OIL.range.toString(),RELATION_RES_PC);

		RELATION_RES.put(RDFS.subPropertyOf.toString(),RELATION_RES_PP);
		RELATION_RES.put(DAML_OIL.subPropertyOf.toString(),RELATION_RES_PP);
		RELATION_RES.put(OWL.equivalentProperty.toString(),RELATION_RES_PP);
		RELATION_RES.put(DAML_OIL.samePropertyAs.toString(),RELATION_RES_PP);
		RELATION_RES.put(OWL.inverseOf.toString(),RELATION_RES_PP);
		RELATION_RES.put(DAML_OIL.inverseOf.toString(),RELATION_RES_PP);
		
		RELATION_RES.put(OWL.intersectionOf.toString(),RELATION_RES_CLC );
		RELATION_RES.put(DAML_OIL.intersectionOf.toString(),RELATION_RES_CLC);
		RELATION_RES.put(OWL.unionOf.toString(),RELATION_RES_CLC);
		RELATION_RES.put(DAML_OIL.unionOf.toString(),RELATION_RES_CLC);

		RELATION_RES.put(OWL.oneOf.toString(),RELATION_RES_C);
		RELATION_RES.put(DAML_OIL.disjointUnionOf.toString(),RELATION_RES_C);
		RELATION_RES.put(OWL.hasValue.toString(),RELATION_RES_C);
		RELATION_RES.put(DAML_OIL.hasValue.toString(),RELATION_RES_C);
	};
	public static String checkRelationNonInstance(String uri){
		// do not accept blank node
		return RELATION_RES.get(uri);
	}
	public static String checkRelationNonInstance(Property predicate){
		String temp = ToolJena.getNodeString( predicate );
		return checkRelationNonInstance(temp);
	}


	
	
	/**
	 * build a map indexing declared types of each named instance in model_data.
	 * excluding class/property definition.
	 * 
	 * @param model_data
	 * @return
	 */
	public static AbstractPropertyValuesMap<Resource,Resource> getMapInstanceTypes(Model model_data) {
		DataPVHMap<Resource,Resource> data = new DataPVHMap<Resource,Resource>();
	
		// TODO we assume simple instance definition here
		StmtIterator iter = model_data
				.listStatements(null, RDF.type, (String) null);
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
	
			// only add named classes
			if (!stmt.getObject().isURIResource()) {
				continue;
			}
	
			// skip meta-class
			if (ToolModelAnalysis.testMetaClass(stmt.getObject())) {
				continue;
			}
	
			// skip meta-property
			if (ToolModelAnalysis.testMetaProperty(stmt.getObject())) {
				continue;
			}
	
			data.add(stmt.getSubject(), (Resource)stmt.getObject());
		}
	
		return data;
	}
	
	public static String getKnownNamespacePrefix(String szNamespace){
		String [][] nsPrefix= new String[][]{
				{"http://inference-web.org/2.0/pml-provenance.owl#","pmlp"},
				{"http://inference-web.org/2.0/pml-justification.owl#","pmlj"},
				{DCTerms.getURI(),DCTerms.class.getSimpleName().toLowerCase()},
				{DC.getURI(),DC.class.getSimpleName().toLowerCase()},
				{FOAF.getURI(),FOAF.class.getSimpleName().toLowerCase()},
				{"http://www.w3.org/2003/01/geo/wgs84_pos#","geo"},
		};
		
		for (int i=0; i<nsPrefix.length; i++){
			if (nsPrefix[i][0].equals(szNamespace))
				return nsPrefix[i][1];
		}
		
		return null;
	}
	
	////////////////////////////////////////////////
	// model analysis
	////////////////////////////////////////////////
	
	/**
	 * split subjects into instance, ontology, and unknown
	 * excluding class/property definition.
	 * 
	 * @param model_data
	 * @return
	 */
	
	public static final int SUBJECT_INSTANCE = 0;
	public static final int SUBJECT_ONTOLOGY = 1;
	public static final int SUBJECT_UNKNOWN = 2;
	public static List<Set<Resource>> splitSubjects(Model model_data) {
		ArrayList<Set<Resource>> ret = new ArrayList<Set<Resource>>();
		
		ret.add(new HashSet<Resource>());	// instance
		ret.add(new HashSet<Resource>());	// ontology
		ret.add(new HashSet<Resource>());	// unknown

		ret.get(SUBJECT_UNKNOWN).addAll(model_data.listSubjects().toSet());
		StmtIterator iter = model_data.listStatements(null, RDF.type, (String) null);
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			ret.get(SUBJECT_UNKNOWN).remove(stmt.getSubject());
			
			// skip meta-class
			if (ToolModelAnalysis.testMetaClass(stmt.getObject())) {
				ret.get(SUBJECT_ONTOLOGY).add(stmt.getSubject());
				continue;
			}

			// skip meta-property
			if (ToolModelAnalysis.testMetaProperty(stmt.getObject())) {
				ret.get(SUBJECT_ONTOLOGY).add(stmt.getSubject());
				continue;
			}

			ret.get(SUBJECT_INSTANCE).add(stmt.getSubject());
		}
		return ret;
	}
	
	
	public static Map<Resource, DataInstance> listInstanceDescription(Model m){
		HashMap<Resource, DataInstance> data = new HashMap<Resource, DataInstance>();
		StmtIterator iter = m.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			Resource subject = stmt.getSubject();
			DataInstance di = data.get(subject);
			if (null== di){
				di = new DataInstance(subject);
				data.put(subject, di);
			}
			di.addDescription(stmt);
		}
		
		return data;

	}	
}
