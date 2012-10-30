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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;


/**
 * provide functions for process URI and URL
 * 
 * @author Li Ding
 * 
 */

public class ToolURI {
	public static boolean debug =false;
	
	private static Logger getLogger() {
		return Logger.getLogger(ToolURI.class);
	}

	////////////////////////////////////////////////
	// constants
	////////////////////////////////////////////////

	
	public final static String ERROR_NON_EMPTY_URL_URI = "Need non-empty url or uri here.";
	public final static String ERROR_BAD_URI_CRAWLER_TRAP = "Bad URI, potential crawler trap.";
	public final static String ERROR_BAD_URI= "Bad URI.";

	public final static String DEFAULT_XMLBASE = "http://ex.org/base#";

	////////////////////////////////////////////////
	// functions (convert)
	////////////////////////////////////////////////
	/** 
	 * convert a string to an URL object with minimal validation
	 * 
	 * @param szUrl
	 * @return
	 * @throws Sw4jException 
	 */
	public static URL string2url(String szUrl) throws Sw4jException{
		return string2url(szUrl, false);
	}	

	/** 
	 * convert a string to an URL object with minimal validation
	 *  - with canonicalize option
	 * 
	 * @param szUrl
	 * @param bCanonicalize
	 * @return
	 * @throws Sw4jException 
	 */
	private static URL string2url(String szUrl, boolean bCanonicalize) throws Sw4jException{
		// validate 1: empty url
		ToolSafe.checkNonEmpty(szUrl, ERROR_NON_EMPTY_URL_URI);

		try {
			if (bCanonicalize)
				szUrl = decodeURIString(szUrl);
			
			// validate 2: basic syntactic check 
			return new URL(szUrl);
		} catch (MalformedURLException e) {
			throw new Sw4jException( Sw4jMessage.STATE_ERROR, e, "see "+ szUrl);
		}		
	}	
	
	/** 
	 * convert a string to an URI object with minimal validation
	 * 
	 * @param szUri
	 * @return
	 * @throws Sw4jException 
	 */
	public static URI string2uri(String szUri) throws Sw4jException{
		szUri = szUri.replaceAll("\\\"", "%22");			
		szUri = szUri.replaceAll("`", "%60");			
		return string2uri(szUri, false);
	}

	/** 
	 * convert a string to an URI object with minimal validation
	 *  - with canonicalize option 
	 * 
	 * @param szUri
	 * @param bCanonicalize
	 * @return
	 * @throws Sw4jException 
	 */
	private static URI string2uri(String szUri, boolean bCanonicalize) throws Sw4jException{
		// validate 1: empty uri
		ToolSafe.checkNonEmpty(szUri, ERROR_NON_EMPTY_URL_URI);

		try {
			if (bCanonicalize)
				szUri = decodeURIString(szUri);

			// validate 2: basic syntactic check 
			URI uri = new URI(szUri);

			return uri;
		} catch (URISyntaxException e) {
			throw new Sw4jException( Sw4jMessage.STATE_ERROR, e, "see "+ szUri);
		}		
	}	
	
	/**
	 * encode a string into canonical form (using UTF8 encoding as recommended by W3C), throw exception if not succeed.
	 * 
	 * http://www.w3.org/International/O-URL-code.html
	 * 
	 * @param szText
	 * @return
	 * @throws Sw4jException 
	 */
	public static String encodeURIString(String szUri) throws Sw4jException {
		ToolSafe.checkNonEmpty(szUri, ERROR_NON_EMPTY_URL_URI);

		// decode first
		String temp =null;
		do{
			if (null!=temp)
				szUri = temp;
			temp = decodeURIString(szUri);
		}while (!szUri.equals(temp));

		//encode
		try {
			String szEncoded = URLEncoder.encode(szUri, "UTF-8");
			szEncoded = szEncoded.replaceAll("\\+", "%2B");
			return szEncoded;
		} catch (UnsupportedEncodingException e) {
			throw new Sw4jException( Sw4jMessage.STATE_ERROR, e);
		}
	}

	/**
	 * decode a string from canonical form (in UTF8), throw exception if not succeed.
	 * 
	 * @param szText
	 * @return
	 * @throws Sw4jException 
	 * @throws Sw4jException
	 */
	public static String decodeURIString(String szUri) throws Sw4jException  {
		ToolSafe.checkNonEmpty(szUri, ERROR_NON_EMPTY_URL_URI);

		try {
			String szDecoded = URLDecoder.decode(szUri, "UTF-8");
			szDecoded = szDecoded.replaceAll("%2B", "+");
			szDecoded = szDecoded.replaceAll(" ", "+");
			return szDecoded;
		} catch (UnsupportedEncodingException e) {
			throw new Sw4jException( Sw4jMessage.STATE_ERROR, e);
		}
	}
	

		

	
	////////////////////////////////////////////////
	// functions (validate)
	////////////////////////////////////////////////
	
	public static void validateUri(String szUri) throws Sw4jException {
		validateUri(string2uri(szUri));
	}

	public static void validateUri(URI uri) throws Sw4jException {
		// validate 1: empty uri
		ToolSafe.checkNonEmpty(uri, ERROR_NON_EMPTY_URL_URI);

		// validate 2: bad segment, cralwer trap
		validateUri_crawlerTrap(uri.toString());
		
		// validate 4: scheme should not be empty
		ToolSafe.checkNonEmpty(uri.getScheme(), "Need non-empty scheme in URI. But see "+ uri.toString());

		// validate 5: SchemeSpecificPart should not be empty
		ToolSafe.checkNonEmpty(uri.getSchemeSpecificPart(), "Need non-emptry SchemeSpecificPart in URI. But see "+ uri.toString());

		// validate 6: SchemeSpecificPart should not consists of [/|:]
		String temp = uri.getSchemeSpecificPart().replaceAll("[/|:]", "");
		ToolSafe.checkNonEmpty(temp, "SchemeSpecificPart should not consists of only [/|:]. But see "+ uri.toString());

		// validate 7: do http validation if it is a http URI
		validateUri_http(uri);
	}
	

	/**
	 * check if the URL is crawler-trap, throw IWSharedException when
	 * encountered one.
	 * 
	 * http://crawler.archive.org/faq.html#traps
	 * 
	 * What are crawler traps? Traps are infinite page sources put up to occupy
	 * ('trap') a crawler. Traps may be as innocent as a calendar that returns
	 * pages years into the future or not-so-innocent
	 * http://spiders.must.die.net/. Traps are created by CGIs/server-side code
	 * that dynamically conjures 'nonsense' pages or else exploits combination
	 * of soft and relative links to generate URI paths of infinite variety and
	 * depth. Once identified, use filters to guard against falling in. Another
	 * trap that works by feeding documents of infinite sizes to the crawler is
	 * http://yahoo.domain.com.au/tools/spiderbait.aspx* as in
	 * http://yahoo.domain.com.au/tools/spiderbait.aspx?state=vic or
	 * http://yahoo.domain.com.au/tools/spiderbait.aspx?state=nsw. To filter out
	 * infinite document size traps, add a maximum doc. size filter to your
	 * crawl order.
	 * 
	 * What do I do to avoid crawling "junk"? In the past crawls were stopped
	 * when we ran into "junk." An example of what we mean by "junk" is the
	 * crawler stuck in a web calendar crawling the year 2020. Nowadays, if
	 * "junk" is detected, we'll pause the crawl and set filters to eliminate
	 * "junk" and then resume (Eliminated URIs will show in the logs. Helps when
	 * doing post-crawl analysis). To help guard against the crawling of "junk"
	 * setup the pathological and path-depth filters. This will also help the
	 * crawler avoid traps. Recommended values for pathological filter is 3
	 * repetitions of same pattern -- e.g. /images/images/images/... -- and for
	 * path-depth, a value of 20.
	 * 
	 * @param szURL
	 */
	public static void validateUri_crawlerTrap(String szURL) throws Sw4jException {
		// case 1
		string2url(szURL);

		// type 2: special cases
		final String[] aryBadSegment = new String[] { "/..", "/text/text/", };
		for (int i = 0; i < aryBadSegment.length; i++) {
			if (szURL.indexOf(aryBadSegment[i]) > 0) {
				throw new Sw4jException( Sw4jMessage.STATE_ERROR, ERROR_BAD_URI_CRAWLER_TRAP, "found "+ aryBadSegment[i]+ " in URL "+ szURL);
			}
		}

		// type 2: repeated path fragments
		StringTokenizer st = new StringTokenizer(szURL, "/");
		String[] lastTokens = new String[10];
		int[] trap_depth = new int[lastTokens.length];
		for (int i = 0; i < lastTokens.length; i++) {
			lastTokens[i] = "";
			trap_depth[i] = 0;
		}
		int path_depth = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();

			// check pattern trap
			for (int i = 0; i < lastTokens.length; i++) {
				if (token.equals(lastTokens[i])) {
					trap_depth[i]++;
				} else {
					trap_depth[i] = 0; // reset
				}

				if (trap_depth[i] >= 2) {
					throw new Sw4jException( Sw4jMessage.STATE_ERROR, ERROR_BAD_URI_CRAWLER_TRAP,
							"repeated pattern - " + (i + 3) + " " + token
									+ " in " + szURL);
				}
			}

			// update last Tokens
			for (int i = 0; i < lastTokens.length - 1; i++) {
				lastTokens[i + 1] = lastTokens[i];
			}
			lastTokens[0] = token;

			// check absolute path depth
			path_depth++;
			if (path_depth > 20) {
					throw new Sw4jException( Sw4jMessage.STATE_ERROR, ERROR_BAD_URI_CRAWLER_TRAP,
						"path_depth too long - " + path_depth + " in " + szURL);
			}
		}
	}	
	
	
	/**
	 * test if a URI is a good HTTP or HTTPS URI
	 * 
	 * @param szUri
	 * @throws Sw4jException 
	 */
	public static void validateUri_http(String szUri) throws Sw4jException {
		validateUri_http(string2uri(szUri));
	}

	/**
	 * test if a URI is a good HTTP or HTTPS URI
	 * 
	 * @param uri
	 * @throws Sw4jException
	 */
	public static void validateUri_http(URI uri) throws Sw4jException {
		// validate 1: empty uri
		ToolSafe.checkNonEmpty(uri, ERROR_NON_EMPTY_URL_URI);
		
		//skip if this is not http uri
		if (!isUriHttp(uri))
			return;
		
		// we use authority instead of host because some time host cannot be
		// parsed
		// e.g. http://high_g.ciao.jp/blog/index.rdf
		ToolSafe.checkNonEmpty( uri.getAuthority(),	"Need non-empty Authority in URI. But see "+ uri.toString());

		if (uri.getAuthority().indexOf(".") < 0) {
			throw new Sw4jException(Sw4jMessage.STATE_ERROR, ERROR_BAD_URI,
					"no '.' in domain part of uri. see "+uri.toString() );
		}

		if ("127.0.0.1".equals(uri.getHost())) {
			getLogger().info("127.0.0.1 - " + uri.toString());
		}

		if ("localhost".equals(uri.getHost())) {
			getLogger().info("127.0.0.1 - " + uri.toString());
		}
	}
	

	
	/**
	 * obtain IP address of an URL, require web connection
	 * 
	 * @param szURL
	 * @return
	 * @throws Sw4jException 
	 */
	public static String url2ip(String szURL) throws Sw4jException {
		return url2ip(string2url(szURL));
	}

	/**
	 * obtain the IP of an URL, require web connection
	 * 
	 * @param url
	 * @return
	 * @throws Sw4jException 
	 */
	public static String url2ip(URL url) throws Sw4jException {
		ToolSafe.checkNonEmpty(url, "Expect non-empty URL");
		
		try {
			InetAddress host;
			host = InetAddress.getByName(url.getHost());
			String ip = host.getHostAddress();
			return ip;
		} catch (UnknownHostException e) {
			throw new Sw4jException(Sw4jMessage.STATE_ERROR, e, "See URL "+ url);
		}
	}	
	
	/**
	 * obtain the URL of an URI, require web connection
	 * 
	 * @param uri
	 * @return
	 * @throws Sw4jException 
	 */
	public static String uri2url(String uri) throws Sw4jException {
		ToolSafe.checkNonEmpty(uri, "Expect non-empty URI");

		int index = uri.indexOf("#");
		if (index>0){
			return uri.substring(0,index);
		}else{
			return uri;
		}
	}	
	
	
	
	
	
	////////////////////////////////////////////////
	// functions (URI)
	////////////////////////////////////////////////
	public static boolean isUriValid(String szUri) {
		try {
			validateUri(szUri);
			return true;
		} catch (Sw4jException e) {
			//getLogger().info(e.getMessage());
			return false;
		}
	}


	public static boolean isUriHttp(String szUri) {
		try {
			URI uri = string2uri(szUri);
			return isUriHttp(uri);
		} catch (Sw4jException e) {
			if (debug)
				getLogger().info(e.getMessage());
			return false;
		}
	}	

	public static boolean isUriHttp(URI uri) {
		return ("http".equalsIgnoreCase(uri.getScheme())
				|| "https".equalsIgnoreCase(uri.getScheme()));
	}	
	
	
	/**
	 * validate and compare if two URIs are the same, throw exception if any of
	 * the URIs or URLs are invalid.
	 * 
	 * @param szUri1
	 * @param szUri2
	 * @return
	 * @throws Sw4jException
	 */
	public static boolean isUriEqual(String szUri1, String szUri2) {
		try {
			URI uri1 = string2uri(szUri1);
			URI uri2 = string2uri(szUri2);
			validateUri(uri1);
			validateUri(uri2);
			
			String szDecodedUri1 = decodeURIString(uri1.toString());
			String szDecodedUri2 = decodeURIString(uri2.toString());
			return szDecodedUri1.equals(szDecodedUri2);
		} catch (Sw4jException e) {
			getLogger().info(e.getMessage());
			return false;
		}
	}
	


	////////////////////////////////////////////////
	// functions (split)
	////////////////////////////////////////////////
	

	public static String [] well_known_ns = new String []{
		"http://sws.geonames.org/",	// there URIs are ugly   http://sws.geonames.org/1283416/
		"http://rdf.freebase.com/ns/", //freebase
		"http://data.nytimes.com/",  // http://data.nytimes.com/34102657707806421181
		"http://sw.cyc.com/concept/", 
		"http://ontology.dumontierlab.com/",
		"http://purl.uniprot.org/core/",
		"http://rdf.insee.fr/geo/",
		"http://web.resource.org/cc/",
		"http://www.w3.org/2006/03/wn/wn20/schema/",

		"http://xmlns.com/foaf/0.1/",
		"http://xmlns.com/wot/0.1/",
		"http://xmlns.com/wordnet/1.6/",

		"http://purl.org/dc/elements/1.1/",
		"http://purl.org/dc/terms/",
		"http://purl.org/rss/1.0/",
		"http://purl.org/dc/dcmitype/",
		"http://purl.org/vocab/bio/0.1/",

		"http://dbpedia.org/class/yago/",
		"http://dbpedia.org/ontology/",
		"http://dbpedia.org/resource/", //dbpedia

		"http://sw.opencyc.org/concept/",
		"http://wiki.infowiss.net/Spezial:URIResolver/Kategorie-3A",
	};

	public static String [][] well_known_special_pattern = new String [][]{
		{"http://umbel.org/.*","/"},
		{"http://sw.nokia.com/.*","/"},
		{"http://wiki.infowiss.net/.*","/"},
		{"http://www.rdfabout.com/.*","/"},
		{"http://sw.opencyc.org/.*","/"},
		{"http://xmlns.com/.*","/"},
		{"http://dbpedia.org/.*","/"},
		{"http://purl.org/.*","/"},
//		{"http://data-gov.tw.rpi.edu/.*","/"},
		{".*Category-3A.*","Category-3A"},
		{".*Kategorie-3A.*","Kategorie-3A"},
		{".*:URIResolver/.*","/"},
		{"http://bio2rdf.org/.*",":"},
		{".*/resource/.*","/"},
		{".*/class/.*","/"},
		{".*/things/.*","/"},
	};
	
	public static int splitUri(String szFileOrURI){
		int index = szFileOrURI.indexOf("#");
		if (index == 0) {
			return 1;
		} else if (index < 0) {
				for (int i=0; i<well_known_ns.length; i++){
					if (szFileOrURI.startsWith(well_known_ns[i])){
						return well_known_ns[i].length();
					}
				}
				
				for (int i=0; i<well_known_special_pattern.length; i++){
					if (szFileOrURI.matches(well_known_special_pattern[i][0])){
						index = szFileOrURI.lastIndexOf(well_known_special_pattern[i][1]);
						if (index>0 && index <szFileOrURI.length())
							return index+ (well_known_special_pattern[i][1].length());
					}
				}
			return szFileOrURI.length();
		} else {
			return index+1;
		}
	}
	
	/**
	 * generate host URI. get the (scheme, host, port) part of the given URI
	 * 
	 * @param szFileOrURI
	 * @return
	 */
//	public static URI extractHostUrl(String szFileOrURI) throws Sw4jException{
//		return extractHostUrl(string2uri(szFileOrURI));
//	}

	/**
	 * generate host URI. get the (scheme, host, port) part of the given URI
	 * 
	 * @param uri
	 * @return
	 */

	public static URI extractHostUrl(URI uri)  throws Sw4jException{
		ToolSafe.checkNonEmpty(uri, ERROR_NON_EMPTY_URL_URI);
		try {
			return new URI(uri.getScheme(),null, uri.getHost(), uri.getPort(), "/", null, null);
		} catch (URISyntaxException e) {
			throw new Sw4jException(Sw4jMessage.STATE_ERROR, e);
		}
	}	
		



}
