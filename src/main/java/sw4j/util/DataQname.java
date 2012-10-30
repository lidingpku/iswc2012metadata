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
package sw4j.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *  XML Qname, split namespace and localname
 *  
 *  @author Li Ding
 *  
 */
public class DataQname {
	protected static boolean debug=false;
	
	////////////////////////////////////////////////
	// internal data
	////////////////////////////////////////////////

	private boolean m_bIsAnon= true;
	private URI m_uri = null;
	private String m_szUriCanonical = null;
	private String m_szNamespace = null;
	private String m_szLocalname =null;
	private String m_szPrefix =null;

	////////////////////////////////////////////////
	// constructor
	////////////////////////////////////////////////
	
	private DataQname(){
	};
	
	public static DataQname create(){
		return new DataQname(); 
	}

	public static DataQname create(String szUri) throws Sw4jException{
		return create(szUri, null);
	}

	public static DataQname create(String szUri,String szPrefix) throws Sw4jException{
		int index = ToolURI.splitUri(szUri);
		return create(
				szUri.substring(0,index),
				szUri.substring(index),
				szPrefix);
	}
	

	
	public static DataQname create(String szNamespace, String szLocalname, String szPrefix) throws Sw4jException{
		DataQname dq =new DataQname();
		
		String szUri= "";
		if (null!=szNamespace)
			szUri +=szNamespace;
		if (null!=szLocalname)
			szUri +=szLocalname;
		dq.setUri(szUri);
		
		dq.setNamespaceLocalname(szNamespace, szLocalname);
		
		dq.m_szPrefix = szPrefix;
		
		return dq;
	}
	
	public static String extractNamespace(String szUri){
		try {
			return create(szUri).getNamespace();
		} catch (Sw4jException e) {
			if (debug)
				e.printStackTrace();
			return null;
		}
	}

	public static String extractNamespaceUrl(String szUri){
		if (ToolSafe.isEmpty(szUri))
			return null;
		
		try {
			return create(szUri).getNamespaceUrl();
		} catch (Sw4jException e) {
			if (debug)
				e.printStackTrace();
			return null;
		}
	}
	
	
	////////////////////////////////////////////////
	// function
	////////////////////////////////////////////////
	public boolean isAnon(){
		return m_bIsAnon;
	}
	
	public URI getUri(){
		return m_uri;
	}
	
	public String getUriCanonical(){
		return m_szUriCanonical;
	}

	
	public String getNamespace(){
		return m_szNamespace;
	}
	

	public String getNamespaceCanonical(){
		if (null==getNamespace())
			return null;

		try {
			return ToolURI.decodeURIString(getNamespace());
		} catch (Sw4jException e) {
			if (debug)
				e.printStackTrace();
			return getNamespace();
		}
	}
	/**
	 * extract the canonical URL of a namespace
	 * @param szFileOrURI
	 * @return
	 */
	public String getNamespaceUrl()  {
		if (null==getNamespaceCanonical())
			return null;
		
		String temp = getNamespaceCanonical();
		if (temp.endsWith("#")){
			temp = temp.substring(0, temp.length()-1);
		}
		
		return temp;
	}
	
	public boolean hasNamespace(){
		return !ToolSafe.isEmpty(m_szNamespace);
	}

	public String getLocalname(){
		return m_szLocalname;
	}
	public boolean hasLocalname(){
		return !ToolSafe.isEmpty(m_szLocalname);
	}

	public String getPrefix(){
		return m_szPrefix;
	}
	
	public boolean hasPrefix(){
		return !ToolSafe.isEmpty(m_szPrefix);
	}
	
	
	private void setUri(String szUri) throws Sw4jException{
		this.m_uri = ToolURI.string2uri(szUri);

		//validate 
		ToolURI.validateUri(this.m_uri.toString());

		//set value
		this.m_szUriCanonical = ToolURI.decodeURIString(szUri);
		this.m_bIsAnon = false;

	}	
	
	private void setNamespaceLocalname(String szNamespace, String szLocalname) throws Sw4jException{
		// case1
		ToolSafe.checkNonEmpty(szNamespace, "Expect non-empty namespace");

		// case2
		if (!ToolSafe.isEmpty(szNamespace)&& !ToolSafe.isEmpty(szLocalname)){
			validateNamespace(szNamespace);
		}

		if (!ToolSafe.isEmpty(szLocalname)){
			validateLocalname(szLocalname);
		}

		this.m_szNamespace =szNamespace;
		this.m_szLocalname = szLocalname;
	}
	
	
	/**
	 * set namespace. Also validate if thestring is a good namespace. Throw an exception if failed
	 * 
	 * @param szNamespace
	 * @throws Sw4jException
	 */
	private void validateNamespace(String szNamespace) throws Sw4jException{
		// namespace should end with any of the following: '/' '#' ':'
		if (!szNamespace.endsWith("/") && !szNamespace.endsWith("#")
				&& !szNamespace.endsWith(":") && !szNamespace.endsWith("-3A")) {
			throw new Sw4jException( Sw4jMessage.STATE_ERROR, "bad Namespace.", "Expect end with either /, #, or : , but see "+szNamespace);
		}
	}


	/**
	 *  set localname. Also validate if the non-empty string is a valid localname 
	 *  
	 * see: http://www.ietf.org/rfc/rfc2396.txt
	 * 
	 * fragment = *uric uric = reserved | unreserved | escaped reserved = ";" |
	 * "/" | "?" | ":" | "@" | "&" | "=" | "+" | "$" | "," unreserved = alphanum |
	 * mark mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
	 * 
	 * escaped = "%" hex hex hex = digit | "A" | "B" | "C" | "D" | "E" | "F" |
	 * "a" | "b" | "c" | "d" | "e" | "f"
	 * 
	 * alphanum = alpha | digit alpha = lowalpha | upalpha
	 * 
	 * lowalpha = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" |
	 * "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" |
	 * "w" | "x" | "y" | "z" upalpha = "A" | "B" | "C" | "D" | "E" | "F" | "G" |
	 * "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" |
	 * "T" | "U" | "V" | "W" | "X" | "Y" | "Z" digit = "0" | "1" | "2" | "3" |
	 * "4" | "5" | "6" | "7" | "8" | "9"
	 * 
	 */
	private void validateLocalname(String szLocalname)
			throws Sw4jException {
		// check if the local name is empty
		// case 2
		if (szLocalname.equals("/")) {
			throw new Sw4jException( Sw4jMessage.STATE_ERROR, "Bad localname.", "see "+szLocalname);
		}

		// case 3
		final String[] INDEX_STRING = new String[] { ".pl?", ".php?", ".asp?", };

		for (int i = 0; i < INDEX_STRING.length; i++) {
			if (szLocalname.indexOf(INDEX_STRING[i]) >= 0) {
				throw new Sw4jException( Sw4jMessage.STATE_ERROR, "Bad localname.", "see "+szLocalname);
			}
		}
		// TODO other cases

		// avoid any local name starts without a letter or _
		// char ch = localname.charAt(0);
		// if (Character.isUnicodeIdentifierStart(ch))
		// return true;
		// if (Character.isLetterOrDigit(ch))
		// return true;

		// if (localname.indexOf(".")>=0)
		// return false; // although '.' is permitted by XML Name convention,
		// but it shows a bad local name in practice

	}
	
	
	
	/** 
		 * Token a local name to segments
		 * 
		 * exceptions:   PhDStudent
		 */
	    public ArrayList<String> getLocalnameTokens(){
	//    	token the localname
			//String token="", updatesql;            		
			//char[] strc=localname.toCharArray();
			ArrayList<String> tokens = new ArrayList<String>();
	    	if (ToolSafe.isEmpty(getLocalname()))
	    		return tokens;
			
		    Pattern pattern;
		    Matcher matcher;
	
		   //System.out.println(localname);
		    pattern = Pattern.compile ("[A-Z]*[a-z]*");
		    matcher = pattern.matcher(getLocalname());
		    while (matcher.find()){
		    	String temptext = matcher.group();
		    	if (temptext.length()==0)
		    		continue;
		    	
		    	tokens.add(temptext);
		       // System.out.println( temptext );
		    }
		    
		    return tokens;
	    }
	
	
}
