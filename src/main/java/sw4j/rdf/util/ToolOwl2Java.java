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



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;


import sw4j.rdf.util.ToolModelAnalysis;
import sw4j.util.Sw4jException;
import sw4j.util.ToolSafe;
import sw4j.util.ToolIO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
/**
 * generate Java vocabulary from an OWL Ontology
 * @author Li Ding
 *
 */
public class ToolOwl2Java {

	/**
	 * generate a java vocabulary file for a certain ontology.
	 * For each described resource, we generate one entry in the target java file
	 * 
	 * @param szOntologyNamespace	the namespace of the ontology, that is where we will download it too.
	 * @param szPackageBaseDir	the base dir for the source code where the java package resides
	 * @param szPackageName		the package to deploy this ontology
	 * @param szJavaClassName	the name of the corresponding java class for that ontology 
	 * @param bUseJena			whether use jena vocabulary
	 * @throws Sw4jException
	 */
	@SuppressWarnings("unchecked")
	public static void genSimpleJavaCode(String szOntologyNamespace, String szPackageBaseDir, String szPackageName, String szJavaClassName, String szPrefix,  boolean bUseJena) throws Sw4jException{
			// validate parameters
			ToolSafe.checkNonEmpty(szOntologyNamespace, "Ontology namesapce cannot be empty");
			ToolSafe.checkNonEmpty(szJavaClassName, "Ontology name cannot be empty");
			ToolSafe.checkNonEmpty(szPackageBaseDir, "package base directory cannot be empty");
			ToolSafe.checkNonEmpty(szPackageName, "package name cannot be empty");
			
        	// load template
			String template_vocabulary =loadTemplateVocabulary(bUseJena);

        	// update template with input parameters
        	template_vocabulary = template_vocabulary.replaceAll("__ONTOLOGY__NAME__", szJavaClassName);
        	template_vocabulary = template_vocabulary.replaceAll("__PACKAGE__NAME__", szPackageName);
        	template_vocabulary = template_vocabulary.replaceAll("__ONTOLOGY__NAMESPACE__", szOntologyNamespace);
        	template_vocabulary = template_vocabulary.replaceAll("___CACHED_DATE_TIME___", ""+Calendar.getInstance().getTime());

        	// load ontology
    		Model m =  ModelFactory.createDefaultModel();
    		m.read(szOntologyNamespace);
            
    		// add ontology description
            String tempNs = szOntologyNamespace.replaceAll("#","");
            Resource oNs = m.getResource(tempNs);
            String szAnnotation;
            szAnnotation = "n/a";
            if (null != oNs){
	    		Iterator iter = m.listObjectsOfProperty(oNs, RDFS.label);
	    		if (iter.hasNext()){
	    			szAnnotation = ""+iter.next();
	    		}
            }
	    	template_vocabulary = template_vocabulary.replaceAll("__ONTOLOGY__LABEL__", szAnnotation );
	    		
            szAnnotation = "n/a";
            if (null != oNs){
	    		Iterator iter = m.listObjectsOfProperty(oNs, RDFS.comment);
	    		if (iter.hasNext()){
	    			szAnnotation = ""+iter.next();
	    		}
            }
	    	template_vocabulary = template_vocabulary.replaceAll("__ONTOLOGY__COMMENT__", szAnnotation );
            
        	// prepare class, property and instances
            final int CAT_CLASS = 0;
            final int CAT_PROPERTY = 1;
            final int CAT_INSTANCE = 2;
            
        	HashSet [] arySetRes = new HashSet[]{ new HashSet<Resource>(), new HashSet<Resource>(), new HashSet<Resource>()};
        	String [][] aryCategoryType = new String [][]{
        			{"Class","Resource"},
        			{"Property","Property"},
        			{"Instance","Resource"},
        	};
        	
        	Iterator iter = m.listStatements(null, RDF.type, (RDFNode)null);
        	while (iter.hasNext()){
        		Statement stmt = (Statement) iter.next();
        		String subject = stmt.getSubject().toString();
        		if (!subject.startsWith(szOntologyNamespace) || subject.length()<=szOntologyNamespace.length())
        			continue;
        		
        		RDFNode object = stmt.getObject();
        		if (ToolModelAnalysis.testMetaClass(object)){
        			arySetRes[CAT_CLASS].add(subject);
        		}else if (ToolModelAnalysis.testMetaProperty(object)){
        			arySetRes[CAT_PROPERTY].add(subject);
        		}else {
        			arySetRes[CAT_INSTANCE].add(subject);
        		}
        	}
        	
        	//keep them disjoint 
			arySetRes[CAT_INSTANCE].removeAll(arySetRes[CAT_CLASS]);
			arySetRes[CAT_INSTANCE].removeAll(arySetRes[CAT_PROPERTY]);
			arySetRes[CAT_PROPERTY].removeAll(arySetRes[CAT_CLASS]);
        	
        	// generate ontology content
        	
        	String content = "";
        	for (int i=0; i<aryCategoryType.length; i++){
        		content += "\t// "+aryCategoryType[i][0]+" ("+arySetRes[i].size()+")\n";
        		iter =  arySetRes[i].iterator();
        		while (iter.hasNext()){
            		content += genSimpleJavaCodeOneEntry((String)iter.next(), szOntologyNamespace, aryCategoryType[i][1], szPrefix, bUseJena);
        		}
        	}

        	template_vocabulary = template_vocabulary.replaceAll("__ONTOLOGY__CONTENT__", content);
        	
        	String filename = szPackageBaseDir+"/"+szPackageName.replace('.', '/')+"/"+szJavaClassName+".java";
        	ToolIO.pipeStringToFile(template_vocabulary, filename, false);
	}
	
	
	private static String loadTemplateVocabulary(boolean bUseJena){
		String filename = "ToolOwl2Java.template.txt";
    	BufferedReader in = new BufferedReader(new InputStreamReader( new ToolOwl2Java().getClass().getResourceAsStream(filename)));
    	String line =null;
    	String content ="";
    	try {
			while (null!=(line=in.readLine())){
				content +=line+"\n";
			}

			
			String JENA__IMPORT ="import com.hp.hpl.jena.rdf.model.Resource;\n" +
								"import com.hp.hpl.jena.rdf.model.Property;\n"+
								"import com.hp.hpl.jena.rdf.model.ResourceFactory;";
			
	    	return content.replaceAll("___JENA__IMPORT___", JENA__IMPORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private static String genSimpleJavaCodeOneEntry(String szURI, String szOntologyNamespace, String szType, String szPrefix, boolean bUseJena){
		String localname = szURI.substring(szOntologyNamespace.length());
		localname = normalize_localname(localname);
		
		String var_name = normalize_java_variable_name(localname);
		if (bUseJena){
			
			
			String template =//"\t public final static String __LOCALNAME___uri = \"__URI__\";\n" +
//				 "\t public final static String __VAR_NAME___lname = \"__LOCALNAME__\";\n" +
//				 "\t public final static String __VAR_NAME___qname = \"__PREFIX__:__LOCALNAME__\";\n" +
//				 "\t public final static String __VAR_NAME___uri = \"__URI__\";\n" +
//				 "\t public final static __TYPE__  __VAR_NAME__ = ResourceFactory.create__TYPE__(__VAR_NAME___uri);\n\n";
				 "\t public final static __TYPE__  __VAR_NAME__ = ResourceFactory.create__TYPE__(\"__URI__\");\n\n";
			return template.replaceAll("__VAR_NAME__", var_name)
							.replaceAll("__LOCALNAME__", localname)
							.replaceAll("__TYPE__", szType)
							.replaceAll("__PREFIX__", szPrefix)
							.replaceAll("__URI__", szURI);
		}else{
			String template =//"\t public final static String __LOCALNAME___uri = \"__URI__\";\n" +
				 "\t public final static String __VAR_NAME___lname = \"__LOCALNAME__\";\n" +
				 "\t public final static String __VAR_NAME___qname = \"__PREFIX__:__LOCALNAME__\";\n" +
				 "\t public final static String __VAR_NAME___uri = \"__URI__\";\n";

			return template.replaceAll("__VAR_NAME__", var_name)
							.replaceAll("__LOCALNAME__", localname)
							.replaceAll("__TYPE__", szType)
							.replaceAll("__PREFIX__", szPrefix)
							.replaceAll("__URI__", szURI);
		}
	}
	
	
	public static String normalize_localname(String variable_name){
		return variable_name.replaceAll("[-/]+","_");
	}

	public static String normalize_java_variable_name(String variable_name){

		//handle reserved word
		final String  [] reserved_names = new String[]{
				"abstract",
				"assert",
				"boolean",
				"break",
				"byte",
				"case",
				"catch",
				"char",
				"class",
				"const",
				"continue",
					
				"default",
				"do",
				"double",
				"else",
				"enum",
				"extends",
				"false",
				"final",
				"finally",
				"float",
				"for",
				
				"goto",
				"if",
				"implements",
				"import",
				"instanceof",
				"int",
				"interface",
				"long",
				"native",
				"new",
				"null",
				
				"package",
				"private",
				"protected",
				"public",
				"return",
				"short",
				"static",
				"strictfp",
				"super",
				"switch",
				
				"synchronized",
				"this",
				"throw",
				"throws",
				"transient",
				"true",
				"try",
				"void",
				"volatile",
				"while",
		};
		
		for(String reserved_name : reserved_names){
			if (reserved_name.equals(variable_name))
				return variable_name+"_java";
		}
		
		return variable_name;
	
	}

}
