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

package sw4j.util.web;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sw4j.util.Sw4jException;
import sw4j.util.ToolSafe;
import sw4j.util.ToolURI;






/**
 * provide functions for processing HTML character escape encoding
 * 
 * @author Li Ding
 *
 * Feb 22, 2007, li udpated escape HTML code
 */
public class ToolWeb {
	
	public final static int MAX_LEN_URL = 250;
	public final static int MAX_LEN_HOST_URL = 200;
	public final static int MAX_LEN_SUFFIX = 10;

	public static void main(String[] args) {
		testSuffix();
	}
	
	public static void testAll(){
		testEscape();
		testSuffix();
		
	}

	public static void testEscape(){
		String temp = "Holly Br�gge Jimison";
		System.out.println(temp);
		temp = ToolWeb.escapeHTML(temp);
		System.out.println(temp);
		temp = ToolWeb.unescapeHTML(temp);
		System.out.println(temp);
	}
/*	
	public static void testValidateURI(){
		String [] szURIs = new String []{
				//bad
				"http",
				"http://",
				"xyz://da.bb/asdf",
				"http://da.bb/asdf\"asdfas",
				"http:///aa",
				"http://high_g.ciao.jp/blog/index.rdf",	// cannot have _ in host name according to RFC
				"http://:980/sadf",
				"irc://localhost/foo#SHA",
				
				//correct
				"http://daf:8080/aa",
				"http://aa.d/%7edaf%3baads",
				"http://sa.d?http://dfa.d#dafse",
				"http://sa.d?http://dfa.d",
				"http://dev.w3.org/cvsweb/2000/10/swap/Attic/logic.n3?rev=1.2",
				"http://sw.deri.org/~aharth/2004/11/rdfquery-perf/univ20/University18_2.nt",
				"mailto://da.bb/asdf",
				"HTTp://aa.bb",	//
				"http://aa.bb",
		};
		for (int i=0; i<szURIs.length; i++){
			System.out.println("testing: " + szURIs[i]);
			boolean ret =validateAbsoluteURI(szURIs[i]);
			System.out.println("====> "+ret);
			System.out.println();
		}		
	}
*/	
	public static void testSuffix(){
		String [] szURIs = new String []{
			"http://dev.w3.org/cvsweb/2000/10/swap/Attic/logic.n3?rev=1.2",
			"http://sw.deri.org/~aharth/2004/11/rdfquery-perf/univ20/University18_2.nt",
			"http://onohiroki.cycling.jp/tb/tb.cgi",
			"http://dev.w3.org/cvsweb/2000/10/swap/grammar/n3.n3?rev=1.21",
			"http://jip.kwark.org/Gfx/2000/09/edin.lela.jpg.html?tmpl=image-foaf",
			"http://www.Department14.University12.edu/FullProfessor4",
			"http://www.livejournal.com/users/idoj_nilbog/data/foaf",
			"http://orlando.openguides.org/index.cgi?id=Text_Formatting_Examples;format=rdf",
			"http://www.communityprogrammes.org.uk/events/cgfl-london/.meta.rdf",
		};
		for (int i=0; i<szURIs.length; i++){
			System.out.println("testing: " + szURIs[i]);
			System.out.println("====> "+getSuffix(szURIs[i]));
			System.out.println();
		}		
		
	}
	

	
	/////////////////////////////////////////////
	// validate URL
	/////////////////////////////////////////////	
	
	/////////////////////////////////////////////
	// process URL
	/////////////////////////////////////////////	
    public static boolean isLongURL(String szURL){
        return szURL.length()>=MAX_LEN_URL;
    }
    
    public static boolean isLongURI(String szURL){
        return szURL.length()>=MAX_LEN_URL;
    }

    public static boolean isLongHostURL(String szURL){
        return szURL.length()>=MAX_LEN_HOST_URL;
    }
	
	public static final String decodeURL(String szURL){
		try {
			return URLDecoder.decode(szURL,"UTF8");
		}catch (IllegalArgumentException e){
			e.printStackTrace();
		}catch (java.io.UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return szURL;	//cannot decode it
	}

	
	public static String getSuffix(String szURI){
		final String NO_SUFFIX ="---";
		
		String suffix = NO_SUFFIX;
		String szFileName="";

		URI uri;
		try {
			uri = ToolURI.string2uri(szURI);
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return NO_SUFFIX;
		}

		szFileName = uri.getPath();
		
		// non-empty query part
		if (null!=uri.getQuery())
			return NO_SUFFIX;	
		
		// empty path			
		if ((null==szFileName)||(szFileName.length()==0))
			return NO_SUFFIX;
		
		// find separator .
		int index = szFileName.lastIndexOf(".");
		if ((index<=0)||(szFileName.endsWith(".")))
			return NO_SUFFIX;

		// can not have / after .
		int indexBS = szFileName.lastIndexOf("/");
		if (index<indexBS)
			return NO_SUFFIX;
		
		suffix= szFileName.substring(index+1);

		// validate 
		if (!Character.isLetter(suffix.charAt(0)))
			return NO_SUFFIX;
		
		if (suffix.length()>MAX_LEN_SUFFIX)
			return NO_SUFFIX;
		
		return suffix.toLowerCase();
	}	


	
	/////////////////////////////////////////////
	// encode and decode HTML with escape chars 
	/////////////////////////////////////////////
	// source: http://www.w3.org/MarkUp/html-spec/html-spec_13.html
	// http://www.theukwebdesigncompany.com/articles/entity-escape-characters.php
	
	// source: http://www.thesauruslex.com/typo/eng/enghtml.htm
	private static HashMap<String,String> map = new HashMap<String,String>();

	static {
		map.put("�","&euro;");
//		! 	&33; 	 
		map.put("\"","&quot;");
//		# 	&35; 	 
//		$ 	&36; 	 
//		% 	&37; 	 
		map.put("&","&amp;");
		/*
		' 	&39; 	 
		( 	&40; 	 
		) 	&41; 	 
		* 	&42; 	 
		+ 	&43; 	 
		, 	&44; 	 
		- 	&45; 	 
		. 	&46; 	 
		/ 	&47; 	 
//		0-9
		: 	&58; 	 
		; 	&59; 	 
		*/
		map.put("<","&lt;");
//		= 	&61; 	 
		map.put(">","&gt;");
		/*
		? 	&63; 	 
		@ 	&64; 	 
//		 A-Z
		[ 	&91; 	 
		\ 	&92; 	 
		] 	&93; 	 
		^ 	&94; 	 
		_ 	&95; 	 
		` 	&96; 	 
//		 a-z 
		{ 	&123; 	 
		| 	&124; 	 
		} 	&125; 	 
		~ 	&126; 	 
//		Non-breaking space 	&160; 	,"&nbsp;
		*/
		map.put("�","&iexcl;");
		map.put("�","&cent;");
		map.put("�","&pound;");
		map.put("�","&curren;");
		map.put("�","&yen;");
		map.put("�","&brvbar;");
		map.put("�","&sect;");
		map.put("�","&uml;");
		map.put("�","&copy;");
		map.put("�","&ordf;");
//		� 	&171;
		map.put("�","&not;");
		map.put("�","&shy;");
		map.put("�","&reg;");
		map.put("�","&macr;");
		map.put("�","&deg;");
		map.put("�","&plusmn;");
		map.put("�","&sup2;");
		map.put("�","&sup3;");
		map.put("�","&acute;");
		map.put("�","&micro;");
		map.put("�","&para;");
		map.put("�","&middot;");
		map.put("�","&cedil;");
		map.put("�","&sup1;");
		map.put("�","&ordm;");
		map.put("�","&raquo;");
		map.put("�","&frac14;");
		map.put("�","&frac12;");
		map.put("�","&frac34;");
		map.put("�","&iquest;");
		map.put("�","&Agrave;");
		map.put("�","&Aacute;");
//		� &#194;	�
		map.put("�","&Atilde;");
		map.put("�","&Auml;");
		map.put("�","&Aring;");
		map.put("�","&AElig;");
		map.put("�","&Ccedil;");
		map.put("�","&Egrave;");
		map.put("�","&Eacute;");
		map.put("�","&Ecirc;");
//		� 		�
		map.put("�","&Igrave;");
		map.put("�","&Iacute;");
		map.put("�","&Icirc;");
		map.put("�","&Iuml;");
		map.put("�","&ETH;");
		map.put("�","&Ntilde;");
		map.put("�","&Ograve;");
		map.put("�","&Oacute;");
		map.put("�","&Ocirc;");
		map.put("�","&Otilde;");
		map.put("�","&Ouml;");
		map.put("�","&times;");
		map.put("�","&Oslash;");
		map.put("�","&Ugrave;");
		map.put("�","&Uacute;");
		map.put("�","&Ucirc;");
		map.put("�","&Uuml;");
		map.put("�","&Yacute;");
		map.put("�","&THORN;");
		map.put("�","&szlig;");
		map.put("�","&agrave;");
		map.put("�","&aacute;");
		map.put("�","&acirc;");
		map.put("�","&atilde;");
		map.put("�","&auml;");
		map.put("�","&aring;");
		map.put("�","&aelig;");
		map.put("�","&ccedil;");
		map.put("�","&egrave;");
		map.put("�","&eacute;");
		map.put("�","&ecirc;");
		map.put("�","&euml;");
		map.put("�","&igrave;");
		map.put("�","&iacute;");
		map.put("�","&icirc;");
		map.put("�","&iuml;");
		map.put("�","&eth;");
		map.put("�","&ntilde;");
		map.put("�","&ograve;");
		map.put("�","&oacute;");
		map.put("�","&ocirc;");
		map.put("�","&otilde;");
		map.put("�","&ouml;");
		map.put("�","&divide;");
		map.put("�","&oslash;");
		map.put("�","&ugrave;");
		map.put("�","&uacute;");
		map.put("�","&ucirc;");
		map.put("�","&uuml;");
		map.put("�","&yacute;");
		map.put("�","&thorn;");
		/*
		� 	&255; 	 
		A 	&256; 	 
		a 	&257; 	 
		A 	&258; 	 
		a 	&259; 	 
		A 	&260; 	 
		a 	&261; 	 
		C 	&262; 	 
		c 	&263; 	 
		C 	&264; 	 
		c 	&265; 	 
		C 	&266; 	 
		c 	&267; 	 
		C 	&268; 	 
		c 	&269; 	 
		D 	&270; 	 
		d 	&271; 	 
		� 	&272; 	 
		d 	&273; 	 
		E 	&274; 	 
		e 	&275; 	 
		E 	&276; 	 
		e 	&277 	 
		E 	&278; 	 
		e 	&279; 	 
		E 	&280; 	 
		e 	&281; 	 
		E 	&282; 	 
		e 	&283; 	 
		G 	&284; 	 
		g 	&285; 	 
		G 	&286; 	 
		g 	&287; 	 
		G 	&288; 	 
		g 	&289; 	 
		G 	&290; 	 
		g 	&291; 	 
		H 	&292; 	 
		h 	&293; 	 
		H 	&294; 	 
		h 	&295; 	 
		I 	&296; 	 
		i 	&297; 	 
		I 	&298; 	 
		i 	&299; 	 
		I 	&300; 	 
		i 	&301; 	 
		I 	&302; 	 
		i 	&303; 	 
		I 	&304; 	 
		i 	&305; 	 
		? 	&306; 	 
		? 	&307; 	 
		J 	&308; 	 
		j 	&309; 	 
		K 	&310; 	 
		k 	&311; 	 
		? 	&312; 	 
		L 	&313; 	 
		l 	&314; 	 
		L 	&315; 	 
		l 	&316; 	 
		L 	&317 	 
		l 	&318; 	 
		? 	&319; 	 
		? 	&320; 	 
		L 	&321; 	 
		l 	&322; 	 
		N 	&323; 	 
		n 	&324; 	 
		N 	&325; 	 
		n 	&326; 	 
		N 	&327; 	 
		n 	&328; 	 
		? 	&329; 	 
		? 	&330; 	 
		? 	&331; 	 
		O 	&332; 	 
		o 	&333; 	 
		O 	&334; 	 
		o 	&335; 	 
		O 	&336; 	 
		o 	&337; 	 
		� 	&338; 	 
		� 	&339; 	 
		R 	&340; 	 
		r 	&341; 	 
		R 	&342; 	 
		r 	&343; 	 
		R 	&344; 	 
		r 	&345; 	 
		S 	&346; 	 
		s 	&347; 	 
		S 	&348; 	 
		s 	&349; 	 
		S 	&350; 	 
		s 	&351; 	 
		� 	&352; 	 
		� 	&353; 	 
		T 	&354; 	 
		t 	&355; 	 
		T 	&356; 	 
		t 	&357 	 
		T 	&358; 	 
		t 	&359; 	 
		U 	&360; 	 
		u 	&361; 	 
		U 	&362; 	 
		u 	&363; 	 
		U 	&364; 	 
		u 	&365; 	 
		U 	&366; 	 
		u 	&367; 	 
		U 	&368; 	 
		u 	&369; 	 
		U 	&370; 	 
		u 	&371; 	 
		W 	&372; 	 
		w 	&373; 	 
		Y 	&374; 	 
		y 	&375; 	 
		� 	&376; 	 
		Z 	&377; 	 
		z 	&378; 	 
		Z 	&379; 	 
		z 	&380; 	 
		� 	&381; 	 
		� 	&382; 	 
		? 	&383; 	 
		R 	&340; 	 
		r 	&341; 	 
		R 	&342; 	 
		r 	&343; 	 
		R 	&344; 	 
		r 	&345; 	 
		S 	&346; 	 
		s 	&347; 	 
		S 	&348; 	 
		s 	&349; 	 
		S 	&350; 	 
		s 	&351; 	 
		� 	&352; 	 
		� 	&353; 	 
		T 	&354; 	 
		t 	&355; 	 
		T 	&356; 	 
		t 	&577; 	 
		T 	&358; 	 
		t 	&359; 	 
		U 	&360; 	 
		u 	&361; 	 
		U 	&362; 	 
		u 	&363; 	 
		U 	&364; 	 
		u 	&365; 	 
		U 	&366; 	 
		u 	&367; 	 
		U 	&368; 	 
		u 	&369; 	 
		U 	&370; 	 
		u 	&371; 	 
		W 	&372; 	 
		w 	&373; 	 
		Y 	&374; 	 
		y 	&375; 	 
		� 	&376; 	 
		Z 	&377 	 
		z 	&378; 	 
		Z 	&379; 	 
		z 	&380; 	 
		� 	&381; 	 
		� 	&382; 	 
		? 	&383; 	
		*/
		
/*
 		Albanian
		map.put("�","&Ccedil;");map.put("�","&ccedil;");
		map.put("�","&Euml;");map.put("�","&euml;");



//		Catalan

		map.put("�","&Agrave;");map.put("�","&agrave");
		map.put("�","&Ccedil;");map.put("�","&ccedil;");
		map.put("�","&Egrave;");map.put("�","&egrave;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("�","&Iacute;");map.put("�","&iacute;");
		map.put("�","&Iuml;");map.put("�","&iuml;");
		map.put("�","&Ograve;");map.put("�","&ograve;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("�","&Uacute;");map.put("�","&uacute;");
		map.put("�","&Uuml;");map.put("�","&uuml;");
		map.put("�","&ordf;");map.put("�","&ordm;");
		map.put("�","&middot;");map.put("","");



//		Croatian

		map.put("?","&#262;");map.put("?","&#263;");
		map.put("?","&#268;");map.put("?","&#269;");
		map.put("?","&#272;");map.put("?","&#273;");
		map.put("�","&#352;");map.put("�","&#353;");
		map.put("�","&#381;");map.put("�","&#382;");



//		Czech

		map.put("�","&Aacute;");map.put("�","&aacute;");
		map.put("?","&#268;");map.put("?","&#269;");
		map.put("?","&#270;");map.put("?","&#271;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("?","&#282;");map.put("?","&#283;");
		map.put("�","&Iacute;");map.put("�","&iacute;");
		map.put("?","&#327;");map.put("?","&#328;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("?","&#344;");map.put("?","&#345;");
		map.put("�","&#352;");map.put("�","&#353;");
		map.put("?","&#356;");map.put("?","&#357;");
		map.put("�","&Uacute;");map.put("�","&uacute;");
		map.put("?","&#366;");map.put("?","&#367;");
		map.put("�","&Yacute;");map.put("�","&yacute;");
		map.put("�","&#381;");map.put("�","&#382;");



//		Danish

		map.put("�","&AElig;");map.put("�","&aelig;");
		map.put("�","&Oslash;");map.put("�","&oslash;");
		map.put("�","&Aring;");map.put("�","&aring;");



//		Dutch

		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("�","&Euml;");map.put("�","&euml;");
		map.put("�","&Oacute;");map.put("�","&oacute;");



//		Esperanto

		map.put("?","&#264;");map.put("?","&#265;");
		map.put("?","&#284;");map.put("?","&#285;");
		map.put("?","&#292;");map.put("?","&#293;");
		map.put("?","&#308,");map.put("?","&#309;");
		map.put("?","&#348;");map.put("?","&#349;");
		map.put("?","&#364;");map.put("?","&#365;");



//		Estonian

		map.put("�","&Auml;");map.put("�","&auml;");
		map.put("�","&Ouml;");map.put("�","&ouml;");
		map.put("�","&Otilde;");map.put("�","&otilde;");
		map.put("�","&Uuml;");map.put("�","&uuml;");



//		Faroese

		map.put("�","&Aacute;");map.put("�","&aacute;");
		map.put("�","&ETH;");map.put("�","&eth;");
		map.put("�","&Iacute;");map.put("�","&iacute;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("�","&Uacute;");map.put("�","&uacute;");
		map.put("�","&Yacute;");map.put("�","&yacute;");
		map.put("�","&AElig;");map.put("�","&aelig;");
		map.put("�","&Oslash;");map.put("�","&oslash;");



//		Finnish

		map.put("�","&Auml;");map.put("�","&auml;");
		map.put("�","&Ouml;");map.put("�","&ouml;");



//		French

		map.put("�","&Agrave;");map.put("�","&agrave");
		map.put("�","&Acirc;");map.put("�","&acirc;");
		map.put("�","&Ccedil;");map.put("�","&ccedil;");
		map.put("�","&Egrave;");map.put("�","&egrave;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("�","&Ecirc;");map.put("�","&ecirc;");
		map.put("�","&Euml;");map.put("�","&euml;");
		map.put("�","&Icirc;");map.put("�","&icirc;");
		map.put("�","&Iuml;");map.put("�","&iuml;");
		map.put("�","&Ocirc;");map.put("�","&ocirc;");
		map.put("�","&OElig;");map.put("�","&oelig;");
		map.put("�","&Ugrave;");map.put("�","&ugrave;");
		map.put("�","&Ucirc;");map.put("�","&ucirc;");
		map.put("�","&Uuml;");map.put("�","&uuml;");
		map.put("�","&#376;");map.put("�","&yuml;");



//		German

		map.put("�","&Auml;");map.put("�","&auml;");
		map.put("�","&Ouml;");map.put("�","&ouml;");
		map.put("�","&Uuml;");map.put("�","&uuml;");
							 map.put("�","&szlig;");



//		Hungarian

		map.put("�","&Aacute;");map.put("�","&aacute;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("�","&Iacute;");map.put("�","&iacute;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("?","&#336;");map.put("?","&#337;");
		map.put("�","&Uacute;");map.put("�","&uacute;");
		map.put("�","&Uuml;");map.put("�","&uuml;");
		map.put("?","&#368;");map.put("?","&#369;");



//		Icelandic

		map.put("�","&Aacute;");map.put("�","&aacute;");
		map.put("�","&ETH;");map.put("�","&eth;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("�","&Iacute;");map.put("�","&iacute;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("�","&Uacute;");map.put("�","&uacute;");
		map.put("�","&Yacute;");map.put("�","&yacute;");
		map.put("�","&THORN;");map.put("�","&thorn;");
		map.put("�","&AElig;");map.put("�","&aelig;");
		map.put("�","&Ouml;");map.put("�","&uml;");



//		Italian

		map.put("�","&Agrave;");map.put("�","&agrave");
		map.put("�","&Acirc;");map.put("�","&acirc;");
		map.put("�","&Egrave;");map.put("�","&egrave;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("�","&Ecirc;");map.put("�","&ecirc;");
		map.put("�","&Igrave;");map.put("�","&igrave;");
		map.put("�","&Iacute;");map.put("�","&iacute;");
		map.put("�","&Icirc;");map.put("�","&icirc;");
		map.put("�","&Iuml;");map.put("�","&iuml;");
		map.put("�","&Ograve;");map.put("�","&ograve;");
		map.put("�","&Ocirc;");map.put("�","&ocirc;");
		map.put("�","&Ugrave;");map.put("�","&ugrave;");
		map.put("�","&Ucirc;");map.put("�","&ucirc;");



//		Latvian

		map.put("?","&#256;");map.put("?","&#257;");
		map.put("?","&#268;");map.put("?","&#269;");
		map.put("?","&#274");map.put("?","&#275;");
		map.put("?","&#290;");map.put("?","&#291;");
		map.put("?","&#298,");map.put("?","&#299;");
		map.put("?","&#310;");map.put("?","&#311;");
		map.put("?","&#315;");map.put("?","&#316;");
		map.put("?","&#325;");map.put("?","&#326;");
		map.put("?","&#342;");map.put("?","&#343;");
		map.put("�","&#352;");map.put("�","&#353;");
		map.put("?","&#362;");map.put("?","&#363;");
		map.put("�","&#381;");map.put("�","&#382;");

//		Norwegian
		map.put("�","&AElig;");map.put("�","&aelig;");
		map.put("�","&Oslash;");map.put("�","&oslash;");
		map.put("�","&Aring;");map.put("�","&aring;");



//		Polish

		map.put("?","&#260;");map.put("?","&#261;");
		map.put("?","&#262;");map.put("?","&#263;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("?","&#280;");map.put("?","&#281;");
		map.put("?","&#321;");map.put("?","&#322;");
		map.put("?","&#323;");map.put("?","&#324;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("?","&#346;");map.put("?","&#347;");
		map.put("?","&#377;");map.put("?","&#378;");
		map.put("?","&#379;");map.put("?","&#380;");



//		Portuguese

		map.put("�","&Agrave;");map.put("�","&agrave");
		map.put("�","&Aacute;");map.put("�","&aacute;");
		map.put("�","&Acirc;");map.put("�","&acirc;");
		map.put("�","&Atilde;");map.put("�","&atilde;");
		map.put("�","&Ccedil;");map.put("�","&ccedil;");
		map.put("�","&Egrave;");map.put("�","&egrave;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("�","&Ecirc;");map.put("�","&ecirc;");
		map.put("�","&Igrave;");map.put("�","&igrave;");
		map.put("�","&Iacute;");map.put("�","&iacute;");
		map.put("�","&Iuml;");map.put("�","&iuml;");
		map.put("�","&Ograve;");map.put("�","&ograve;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("�","&Otilde;");map.put("�","&otilde;");
		map.put("�","&Ugrave;");map.put("�","&ugrave;");
		map.put("�","&Uacute;");map.put("�","&uacute;");
		map.put("�","&Uuml;");map.put("�","&uuml;");
			map.put("�","&ordf;");
			map.put("�","&ordm;");



//		Romanian

		map.put("?","&#258;");map.put("?","&#259;");
		map.put("�","&Acirc;");map.put("�","&acirc;");
		map.put("�","&Icirc;");map.put("�","&icirc;");
		map.put("?","&#350;");map.put("?","&#351;");
		map.put("?","&#354;");map.put("?","&#355;");



//		Slovak

		map.put("�","&Aacute;");map.put("�","&aacute;");
		map.put("�","&Auml;");map.put("�","&auml;");
		map.put("?","&#268;");map.put("?","&#269;");
		map.put("?","&#270;");map.put("?","&#271;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("?","&#313;");map.put("?","&#314;");
		map.put("?","&#317;");map.put("?","&#318;");
		map.put("?","&#327;");map.put("?","&#328;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("�","&Ocirc;");map.put("�","&ocirc;");
		map.put("?","&#340;");map.put("?","&#341;");
		map.put("�","&#352;");map.put("�","&#353;");
		map.put("?","&#356;");map.put("?","&#357;");
		map.put("�","&Uacute;");map.put("�","&uacute;");
		map.put("�","&Yacute;");map.put("�","&yacute;");
		map.put("�","&#381;");map.put("�","&#382;");



//		Slovene

		map.put("?","&#268;");map.put("?","&#269;");
		map.put("�","&#352;");map.put("�","&#353;");
		map.put("�","&#381;");map.put("�","&#382;");



//		Spanish

		map.put("�","&Aacute;");map.put("�","&aacute;");
		map.put("�","&Eacute;");map.put("�","&eacute;");
		map.put("�","&Iacute;");map.put("�","&iacute;");
		map.put("�","&Oacute;");map.put("�","&oacute;");
		map.put("�","&Ntilde;");map.put("�","&ntilde;");
		map.put("�","&Uacute;");map.put("�","&uacute;");
		map.put("�","&Uuml;");map.put("�","&uuml;");
		map.put("�","&iexcl;");map.put("�","&ordf;");
		map.put("�","&iquest;");map.put("�","&ordm;");



//		Swedish

		map.put("�","&Aring;");map.put("�","&aring;");
		map.put("�","&Auml;");map.put("�","&auml;");
		map.put("�","&Ouml;");map.put("�","&ouml;");



//		Turkish

		map.put("�","&Ccedil;");map.put("�","&ccedil;");
		map.put("?","&#286;");map.put("?","&#287;");
		map.put("?","&#304;");map.put("?","&#305;");
		map.put("�","&Ouml;");map.put("�","&ouml;");
		map.put("?","&#350;");map.put("?","&#351;");
		map.put("�","&Uuml;");map.put("�","&uuml;");
		
*/		
	};

	
	
	public static final String escapeHTML(String s){
		   StringBuffer sb = new StringBuffer();
		   int n = s.length();
		   for (int i = 0; i < n; i++) {
		      String c = s.substring(i,i+1);
		      Object escaped = map.get(c);
		      if (null!=escaped)
		      	sb.append(escaped);
		      else
		      	sb.append(c);
		   }
		   return sb.toString();
	}

	public static final String unescapeHTML(String s){
		String temp =s;
		Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> entry = iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			
			temp = temp.replaceAll(value,key);
		}
		return temp;
	}

	public static String extractHtmlHeadMetaRedirectedURL(String text){
		Pattern pattern;
	    Matcher matcher;
	    String temp;
	    //int cURL =0;
	
	    pattern = Pattern.compile ("<[m|M][e|E][t|T][a|A][^>]+>");
	    matcher = pattern.matcher( text);
	    while (matcher.find()){
	        temp = matcher.group();
	        System.out.println(temp.trim());
	        
	        StringTokenizer st = new StringTokenizer(temp,"\t\n \"<>;");
	        
	        if (!st.hasMoreTokens())
	        	return null;
	        if (!st.nextToken().equalsIgnoreCase("meta"))
	        	return null;
	        
	        if (!st.hasMoreTokens())
	        	return null;
	        if (!st.nextToken().equalsIgnoreCase("http-equiv="))
	        	return null;
	        
	        if (!st.hasMoreTokens())
	        	return null;
	        if (!st.nextToken().equalsIgnoreCase("refresh"))
	        	return null;
	
	        if (!st.hasMoreTokens())
	        	return null;
	        if (!st.nextToken().equalsIgnoreCase("content="))
	        	return null;
	
	        while(st.hasMoreTokens()){
	        	String token = st.nextToken();
	        	//System.out.println(token);
	        	if (token.startsWith("url="))
	        		return token.substring(4);
	        }
	    }
	    
	    return null;    	
	}
		   	
/*	
	//source http://www.rgagnon.com/javadetails/java-0306.html
	public static final String escapeHTML(String s){
		   StringBuffer sb = new StringBuffer();
		   int n = s.length();
		   for (int i = 0; i < n; i++) {
		      char c = s.charAt(i);
		      switch (c) {
		         case '�': sb.append("&agrave;");break;
		         case '�': sb.append("&Agrave;");break;
		         case '�': sb.append("&acirc;");break;
		         case '�': sb.append("&Acirc;");break;
		         case '�': sb.append("&auml;");break;
		         case '�': sb.append("&Auml;");break;
		         case '�': sb.append("&aring;");break;
		         case '�': sb.append("&Aring;");break;
		         case '�': sb.append("&aelig;");break;
		         case '�': sb.append("&AElig;");break;
		         case '�': sb.append("&ccedil;");break;
		         case '�': sb.append("&Ccedil;");break;
		         case '�': sb.append("&eacute;");break;
		         case '�': sb.append("&Eacute;");break;
		         case '�': sb.append("&egrave;");break;
		         case '�': sb.append("&Egrave;");break;
		         case '�': sb.append("&ecirc;");break;
		         case '�': sb.append("&Ecirc;");break;
		         case '�': sb.append("&euml;");break;
		         case '�': sb.append("&Euml;");break;
		         case '�': sb.append("&iuml;");break;
		         case '�': sb.append("&Iuml;");break;
		         case '�': sb.append("&ocirc;");break;
		         case '�': sb.append("&Ocirc;");break;
		         case '�': sb.append("&ouml;");break;
		         case '�': sb.append("&Ouml;");break;
		         case '�': sb.append("&oslash;");break;
		         case '�': sb.append("&Oslash;");break;
		         case '�': sb.append("&szlig;");break;
		         case '�': sb.append("&ugrave;");break;
		         case '�': sb.append("&Ugrave;");break;         
		         case '�': sb.append("&ucirc;");break;         
		         case '�': sb.append("&Ucirc;");break;
		         case '�': sb.append("&uuml;");break;
		         case '�': sb.append("&Uuml;");break;
		         default:  sb.append(c); break;
		      }
		   }
		   return sb.toString();
		}
*/

	public static String removeHtmlComment(String szContent){
		return removeMarkup(szContent, "<!--","-->");
	}
	
	public static String removeMarkup(String szContent, String szBegin, String szEnd){
    	String first ="";
    	String rest = szContent;
    	
    	while (true) {
        	//find start of 
        	int index1=rest.indexOf(szBegin);
    		if (index1<0)
    			break;
    		int index2=rest.indexOf(szEnd,index1);
    		if (index2<0)
    			break;
    		first += rest.substring(0,index1);
    		
    		index2 += szEnd.length();
    		if (index2 >= rest.length())
    			break;
    		
    		rest = rest.substring(index2);
    		
    		if (ToolSafe.isEmpty(rest))
    			break;
    	}
    	first += rest;

    	//remove whitespace
    	return first.trim();
    	// the following regular expression code takes too much time
    	// so we get rid of them 
    	//
    	//String temp = text.replaceAll("<!--[^-]+[-[^-]+]*[--[^>]+]*-->","");
    	//temp = temp.trim();
    	//return temp;
	}
	
	public static ArrayList<String> extractMarkup(String szContent, String szBegin, String szEnd){
    	String rest = szContent;
    	ArrayList<String> data = new ArrayList<String>();
    	
    	while (true) {
        	//find start of 
        	int index1=rest.indexOf(szBegin);
    		if (index1<0)
    			break;
    		int index2=rest.indexOf(szEnd,index1);
    		if (index2<0)
    			break;
    		
    		String szTemp = rest.substring(index1,index2+ szEnd.length());
    		data.add(szTemp);
    		
    		rest = rest.substring(index2+ szEnd.length());
    	}

    	return data;
	}
	
	
	public static String string2htmlStringWhitespace(String szText){
		String temp =szText;
		temp = temp.replaceAll("\t","&nbsp;&nbsp;&nbsp;&nbsp;");
		temp = temp.replaceAll("\\s","&nbsp;");
		return temp;
	}
	
	public static String encloseXmlText(String szText){
		return String.format("<![CDATA[%s]]>", szText); 
		// this will avoid parse errors such as & in the text.
	}
	
	public static String writeXmlNode(String szMarkup, String szText){
		return String.format("<%s>%s</%s>", szMarkup, encloseXmlText(szText), szMarkup); 
		// this will avoid parse errors such as & in the text.
	}
	public static String writeXmlNode(String szMarkup, String szText, String szAttribute){
		return String.format("<%s %s>%s</%s>", szMarkup, szAttribute, encloseXmlText(szText), szMarkup); 
		// this will avoid parse errors such as & in the text.
	}

}



