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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import sw4j.util.ToolSafe;
import sw4j.util.ToolURI;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
/**
 * store descriptions about an RDF resource
 * 
 * @author Li Ding
 *
 */
public class DataInstance {
	Resource m_subject = null;
	Model m_model = ModelFactory.createDefaultModel();
	Model m_global_model = null;
	String m_szRuntimeXmlbase = null;
	
	public DataInstance(String subject_uri){
		this(subject_uri, null);
	}
	public DataInstance(String subject_uri, String szRuntimeXmlbase){
		if (ToolSafe.isEmpty(szRuntimeXmlbase)){
			m_szRuntimeXmlbase = "http://example.org/rdf";
		}else{
			m_szRuntimeXmlbase = szRuntimeXmlbase;
		}
		m_subject = this.createResource(subject_uri);
		m_global_model= m_model;
	}
	
	public DataInstance(Resource subject){
		this(subject, null);
	}
	public DataInstance(Resource subject, Model m){
		m_subject= subject;
		if (!ToolSafe.isEmpty(m)){
			m_global_model = m;
			m_model.add( m.listStatements(subject, null, (String)null) );
			ToolJena.update_copyNsPrefix(m_model, m);
		}
	}
	
	public Resource getSubject() {
		return m_subject;
	}
	
	public Model getModel(){
		return m_model;
	}
	
	public Model getGlobalModel(){
		return m_global_model;
	}
	
	/**
	 * check if the property has value
	 * 
	 * @param prop
	 * @return 
	 */
	public boolean hasPropertyValue(Property prop){
		return m_model.listObjectsOfProperty(m_subject, prop).hasNext();
	}
	
	/**
	 * list all values of the property
	 * 
	 * @param prop
	 * @return
	 */
	public NodeIterator listPropertyValue(Property prop){
		return m_model.listObjectsOfProperty(m_subject, prop);
	}

	/**
	 * just return the first value of the property.
	 * return null if the property is not instantiated.
	 * 
	 * 
	 */ 
	public RDFNode getPropertyValue(Property prop){
		NodeIterator iter = m_model.listObjectsOfProperty(m_subject, prop);
		if (!iter.hasNext())
			return null;
		else
			return iter.nextNode();
	}
	
	public String getPropertyValueAsString(Property prop){
		RDFNode node = getPropertyValue(prop);
		if (null!=node)
			return ToolJena.getNodeString(node);
		else
			return null;
	}
	public Resource getPropertyValueAsURIResource(Property prop){
		RDFNode node = getPropertyValue(prop);
		if (null!=node && node.isURIResource())
			return (Resource) node;
		else
			return null;
	}
		
	
	public TreeSet<String> getPropertyValueAsStringSet(Property prop){
		TreeSet<String> ret = new TreeSet<String>();
		NodeIterator iter = m_model.listObjectsOfProperty(m_subject, prop);
		while (iter.hasNext()){
			RDFNode node = iter.nextNode();
			ret.add(ToolJena.getNodeString(node));
		}
		return ret;
	}	
	
	public void addPropertyValue(Property prop, String value) {
		if (ToolSafe.isEmpty(value))
			return;
		Statement stmt = m_model.createStatement(m_subject, prop, value);
		addDescription(stmt);
	}

	public String getPropertyPropertyValueAsString(Property prop1, Property prop2){
		NodeIterator iter = m_model.listObjectsOfProperty(m_subject, prop1);
		if (!iter.hasNext()){
		}else{
			Resource node= (Resource)iter.nextNode();
			if (null!=m_global_model){
				NodeIterator iter_sub = m_global_model.listObjectsOfProperty(node, prop2);
				if (iter_sub.hasNext()){
					return ToolJena.getNodeString(iter_sub.nextNode());
				}
			}
		}
		return null;
	}
	
	
	public void addPropertyValue(Property prop, String value, XSDDatatype datatype) {
		if (ToolSafe.isEmpty(value))
			return;
		addPropertyValue(prop, m_model.createTypedLiteral(value,datatype ));
	}

	public void addPropertyValue(Property prop, long value) {
		addPropertyValue(prop, m_model.createTypedLiteral(value));
	}

	public void addPropertyValue(Property prop, double value) {
		addPropertyValue(prop, m_model.createTypedLiteral(value));
	}

	public void addPropertyValue(Property prop, RDFNode value) {
		if (ToolSafe.isEmpty(value))
			return;
		Statement stmt = m_model.createStatement(m_subject, prop, value);
		addDescription(stmt);
	}
		
	public void addPropertyValueDatetime(Property prop, long datetime) {
		addPropertyValue(prop, ToolJena.createDatetimeLiteral(m_model, datetime));
	}

	public void addDescription(Statement stmt){
		m_model.add(stmt);
	}
	
	public void addType(Resource type){
		addPropertyValue(RDF.type, type);
	}

	@SuppressWarnings("unchecked")
	public Set<Resource> getNamedTypes(){
		Set ret= this.m_model.listObjectsOfProperty(null, RDF.type).toSet();
		Iterator<RDFNode> iter = ret.iterator();
		while (iter.hasNext()){
			RDFNode node = iter.next();
			if (!node.isURIResource())
				iter.remove();
		}
		return ret;
	}
	
	public Set<Resource> getProperties(){
		Set<Resource> ret = new HashSet<Resource>();
		StmtIterator iter = this.m_model.listStatements();
		while (iter.hasNext()){
			ret.add(iter.nextStatement().getPredicate());
		}
		return ret;
	}
	
	public Resource createResource(String szUri){
		if (!ToolURI.isUriValid(szUri)){
			if (szUri.startsWith("#"))
				szUri= szUri.substring(1);
			szUri = m_szRuntimeXmlbase + szUri;
		}
		return  m_model.createResource(szUri);
	}
	
	public String printModelToString(Map<String,String> mapNsPrefix, String szJenaRdfSyntax){
		// set ns prefix for better print
		if (!ToolSafe.isEmpty(mapNsPrefix))
			getModel().setNsPrefixes(mapNsPrefix);
		
		// get the output
		String output = ToolJena.printModelToString(getModel(), szJenaRdfSyntax);
		
		// make runtime namespace relative url
		if (null!=this.m_szRuntimeXmlbase)
			output = output.replaceAll(this.m_szRuntimeXmlbase,"#");
	
		return output;
	}

		
	public String printModelFragmentToString(Map<String,String> mapNsPrefix, String szJenaRdfSyntax){
		String output = printModelToString(mapNsPrefix, szJenaRdfSyntax);
		
		// extract the fragment
		if ("N3".equals(szJenaRdfSyntax)){

			// remove namespace declaration
			output = output.replaceAll("@prefix[^\n]+\n","");
		}else if (szJenaRdfSyntax.startsWith("RDF/XML")){

			// remove namespace declaration
			output = output.replaceAll("<\\?xml[^>]+>","");
			output = output.replaceAll("<[/]?rdf:RDF[^>]*>","");
			
		}
		return output;
	}

}
