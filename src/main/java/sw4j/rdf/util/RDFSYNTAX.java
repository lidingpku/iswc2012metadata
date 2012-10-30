package sw4j.rdf.util;

import sw4j.util.ToolSafe;

public class RDFSYNTAX {

	public static final String N3 = "N3";
	public static final String NT = "N-TRIPLE";
	public static final String RDFXML = "RDF/XML";
	public static final String TURTLE = "TURTLE";
	public static final String RDFXML_ABBREV = "RDF/XML-ABBREV";
	
	public static final String RDFA = "RDFA";
	public static final String GRDDL = "GRDDL";
	public static final String SPARQL_XML = "sparql/xml";
	public static final String SPARQL_JSON = "sparql/json";
	
	
	public static final String MIME_N3= "text/rdf+n3";
	public static final String MIME_RDFXML= "application/rdf+xml";
	public static final String MIME_TEXT_PLAIN= "text/plain";
	public static final String MIME_TEXT_HTML= "text/html";
	public static final String MIME_SPARQL_XML ="application/sparql-results+xml";
	public static final String MIME_SPARQL_JSON ="application/sparql-results+json";
	/**
	 * get mimetype for the rdf syntax
	 * @param rdfsyntax
	 * @param default_mime_type
	 * @return
	 */
	public static String getMimeType(String rdfsyntax, String default_mime_type){
		if (N3.equals(rdfsyntax)){
			return MIME_N3;
		}else if (TURTLE.equals(rdfsyntax)){
			return MIME_N3;
		}else if (RDFXML.equals(rdfsyntax)){
			return MIME_RDFXML;
		}else if (RDFXML_ABBREV.equals(rdfsyntax)){
			return MIME_RDFXML;
		}else if (NT.equals(rdfsyntax)){
			return MIME_TEXT_PLAIN;
		}else if (SPARQL_XML.equals(rdfsyntax)){
			return MIME_SPARQL_XML;
		}else if (SPARQL_JSON.equals(rdfsyntax)){
			return MIME_SPARQL_JSON;
		}else {
			if (ToolSafe.isEmpty(default_mime_type))
				return MIME_TEXT_HTML;
			else
				return default_mime_type;
		}
	}
	/**
	 * parse/validate RDF syntax from input string, return null if no RDF syntax is detected
	 * 
	 * @param rdfsyntax
	 * @return
	 */
	public static String parseSyntaxRdf(String rdfsyntax, String sz_default){
		String ret = parseSyntaxRdf(rdfsyntax);
		if (null==ret)
			return sz_default;
		else
			return ret;		
	}
	/**
	 * parse/validate RDF syntax from input string, return null if no RDF syntax is detected
	 * 
	 * @param rdfsyntax
	 * @return
	 */
	public static String parseSyntaxRdf(String rdfsyntax){
		if (ToolSafe.isEmpty(rdfsyntax)){
			return null;
		}else{
			if (N3.equalsIgnoreCase(rdfsyntax)){
				return N3;
			}else if (TURTLE.equalsIgnoreCase(rdfsyntax)){
				return TURTLE;
			}else if (RDFXML.equalsIgnoreCase(rdfsyntax)){
				return RDFXML;
			}else if (RDFXML_ABBREV.equalsIgnoreCase(rdfsyntax)){
				return RDFXML_ABBREV;
			}else if (NT.equalsIgnoreCase(rdfsyntax)){
				return NT;
			}else{
				return null;
			}
		}
		
	}
	
	public static String parseSyntax(String rdfsyntax){
		if (ToolSafe.isEmpty(rdfsyntax)){
			return null;
		}else{
			if (N3.equalsIgnoreCase(rdfsyntax)){
				return N3;
			}else if (TURTLE.equalsIgnoreCase(rdfsyntax)){
				return TURTLE;
			}else if (RDFXML.equalsIgnoreCase(rdfsyntax)){
				return RDFXML;
			}else if (RDFXML_ABBREV.equalsIgnoreCase(rdfsyntax)){
				return RDFXML_ABBREV;
			}else if (NT.equalsIgnoreCase(rdfsyntax)){
				return NT;
			}else if (SPARQL_XML.equalsIgnoreCase(rdfsyntax)){
				return SPARQL_XML;
			}else if (SPARQL_JSON.equalsIgnoreCase(rdfsyntax)){
				return SPARQL_JSON;
			}else{
				return null;
			}
		}
		
	}
}
