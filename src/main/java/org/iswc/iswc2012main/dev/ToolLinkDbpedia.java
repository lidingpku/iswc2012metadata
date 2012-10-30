package org.iswc.iswc2012main.dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;


import sw4j.rdf.util.ToolJena;
import sw4j.util.DataPVHMap;
import sw4j.util.DataSmartMap;
import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.ToolSafe;
import sw4j.util.web.ToolWeb;

/*
 * input: 	a list of names, 
 * 			a csv file containing existing mappings
 * 			mapping result 
 */

public class ToolLinkDbpedia {
	public static String VERSION = "2011-07-01";


	
//	public static void main(String[] args){
//		test_us_federal_agency();
//	}
//	
//	public static void test_us_federal_agency(){
//		String label = "gov-us-agency";
//		//map_init(label);
//		map_generate(label);
//		//map_publish(label);
//	}
//	
//	public static void test_us_state(){
//		String label = "us-state";
//		//map_generate(label);
//		//publish_map(label);
//	}
//	
//	protected static void map_init(String label){
//		File fileInput= getFile(String.format("%s.txt", label));
//		if (!fileInput.exists()){
//			ToolIO.pipeStringToFile("", fileInput);
//		}else{
//			System.out.println("input file already exist: \n" + fileInput.getAbsolutePath());
//		}
//	}
//	
//	protected static void map_generate(String label){
//		File fileInput= getFile(String.format("%s.txt", label));
//		File fileOutput= getFile(String.format("%s-dbpedia-output.csv", label));
//		File fileFinal= getFile(String.format("%s-dbpedia.csv", label));
//		HashSet<String> stopWords = new HashSet<String>();	
//		name2dbpediaByFile(fileInput, fileOutput, loadKnownNames(fileFinal), stopWords);
//	}
//	
//	protected static void map_publish(String label){
//		File fileFinal= getFile(String.format("%s-dbpedia.csv", label));
//		File fileMapping= getFile(String.format("%s-dbpedia.ttl", label));
//		createMappingFile(fileFinal,fileMapping);
//	}
//	
	
	public static int MAX_WIKIPEDIA_URL =3;
	
	
	public static String NAME2DBPEDIA_NAME = "name";
	public static String NAME2DBPEDIA_URI_FIRST = "uri_first";
	public static String NAME2DBPEDIA_URI_VERIFIED = "uri_verified";
	public static String NAME2DBPEDIA_MESSAGE = "message";
	public static String NAME2DBPEDIA_ABBREVIATION = "dbpedia_abbreviation";
	public static String NAME2DBPEDIA_DBPEDIANAME = "dbpedia_name";
	public static String NAME2DBPEDIA_REDIRECT = "dbpedia_redirect";
	public static String NAME2DBPEDIA_HOMEPAGE= "dbpedia_homepage";


	public static Collection<String> loadKnownNames(File fileFinal){
		HashSet<String> knownNames = new HashSet<String>();
		
		if (!fileFinal.exists())
			return knownNames;
	
		ToolCsvLoader loader = new ToolCsvLoader();
		try {
			loader.loadCsvFile(fileFinal);

			String [] id_props = new String []{
					NAME2DBPEDIA_NAME,
					NAME2DBPEDIA_ABBREVIATION,
				};

			for (DataSmartMap row : loader.m_data.values()){
				String dbpedia_uri = row.getAsString(NAME2DBPEDIA_URI_VERIFIED);
				if (ToolSafe.isEmpty(dbpedia_uri))
					continue;

				for (String id_prop: id_props){
					String id =  row.getAsString(id_prop);
					if (!ToolSafe.isEmpty(id)){
						knownNames.add(id);
					}					
				}				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return knownNames;
	}

	public static void createMappingFile(File fileFinal, File fileMapping){
		ToolCsvLoader loader = new ToolCsvLoader();
		try {
			loader.loadCsvFile(fileFinal);

			String [] id_props = new String []{
				NAME2DBPEDIA_NAME,
				NAME2DBPEDIA_ABBREVIATION,
			};

			Model m = ModelFactory.createDefaultModel();
			DataPVHMap<String,String> pvh = new DataPVHMap<String,String>();
			for (DataSmartMap row : loader.m_data.values()){
				String dbpedia_uri = row.getAsString(NAME2DBPEDIA_URI_VERIFIED);
				if (ToolSafe.isEmpty(dbpedia_uri))
					continue;

				for (String id_prop: id_props){
					String id =  row.getAsString(id_prop);
					if (!ToolSafe.isEmpty(id)){
						pvh.add(dbpedia_uri, id);			
						m.add(m.createStatement(m.createResource(dbpedia_uri), DCTerms.identifier, id));
					}					
				}				
			}
			
			//attached metadata
			String baseURI= "http://foo/bar";
			m.createResource(baseURI)
				.addProperty(DCTerms.date, m.createTypedLiteral(Calendar.getInstance()))
				.addProperty(DCTerms.created, "TWC DBpedia Linking Service (ver " + VERSION+")");
			
			ToolJena.printModelToFile(m, "N3", baseURI, fileMapping,false);
			
			System.out.println("total dbpedia entries: " + pvh.entrySet().size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static int name2dbpediaByFile(File fileInput, File fileOutput,   Collection<String> knownNames,  Collection<String> stopWords){
		System.out.println(String.format("process name2dbpedia: input file %s --- output file %s", fileInput.getAbsolutePath(), fileOutput.getAbsolutePath()));
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(fileInput));
			PrintWriter o = new PrintWriter(new FileOutputStream(fileOutput));
			String line;
			int cnt_verified=0;
			int cnt_processed=0;
			int cnt_total =0;
			boolean bFirstRow = true;
			while ( null!=(line=reader.readLine())){
				line = line.trim();
				if (line.startsWith("#") || line.length()==0)
					continue;
				
				cnt_total++;

				if (knownNames.contains(line))
					continue; // we already know its mapping
				
				cnt_processed++;
				
				DataSmartMap mapping = name2dbpedia(line, stopWords);
				
				if (bFirstRow){
					o.println(mapping.toCSVheader());
					bFirstRow=false;
				}
				
				o.println(mapping.toCSVrow());
				o.flush();
				
				if (!ToolSafe.isEmpty(mapping.getAsString(NAME2DBPEDIA_URI_VERIFIED)))
					cnt_verified++;
			}
			o.close();
			
			System.out.println(String.format("input %d entries", cnt_total));
			System.out.println(String.format("processed %d entries", cnt_processed));
			System.out.println(String.format("verified %d best mappings", cnt_verified));
			return cnt_verified;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static  String name2dbpediaBest(String name, Collection<String> stopWords){
		return name2dbpedia(name, stopWords).getAsString(NAME2DBPEDIA_URI_VERIFIED);
	}
	
	
	

	public static  DataSmartMap name2dbpedia(String name, Collection<String> stopWords){
		System.out.println("\n ----------- mapping name to dbpedia------------------");
		System.out.println(name);

		DataSmartMap ret = new DataSmartMap();
		String message = "n/a";
		ret.put(NAME2DBPEDIA_NAME, name);
		ret.put(NAME2DBPEDIA_MESSAGE, "n/a");
		ret.put(NAME2DBPEDIA_URI_FIRST, "");
		ret.put(NAME2DBPEDIA_URI_VERIFIED, "");
		ret.put(NAME2DBPEDIA_ABBREVIATION, "");
		ret.put(NAME2DBPEDIA_DBPEDIANAME, "");
		ret.put(NAME2DBPEDIA_REDIRECT, "");
		ret.put(NAME2DBPEDIA_HOMEPAGE, "");

		try {
			//prepare yahoo boss query
			String appid = MyConfig.getProperty(MyConfig.CONFIG_YAHOO_APPID);
			String query = "\""+ToolWeb.escapeHTML(name)+"\" site:en.wikipedia.org";
			query = query.replaceAll("\\s", "+");
			//String szUrl = String.format("http://pipes.yahoo.com/pipes/pipe.run?_id=8229ea1d5fe540124faa315e2d11e25b&_render=rss&name=%s", name.replaceAll("\\s", "+"));
			String szUrl = String.format("http://boss.yahooapis.com/ysearch/web/v1/%s?appid=%s&format=xml",  query ,appid);

			//load query result
			String szContent = ToolIO.pipeUrlToString(szUrl);

			//process results
			
			// check if we can access result
			if (ToolSafe.isEmpty(szContent)){
				message = "cannot load query result";
				ret.put(NAME2DBPEDIA_MESSAGE, message);
				return ret;
			}
				
			// check if any results has been returned
			if (ToolWeb.extractMarkup(szContent, "<url>", "</url>").size()<=0){
				message = "empty result set";
				ret.put(NAME2DBPEDIA_MESSAGE, message);
				return ret;
			}

			// extract dbpedia URL
			String szTempLink=null;
			ArrayList<String> aryUrl = ToolWeb.extractMarkup(szContent, "<url>", "</url>");
			for (int i=0; i<Math.min(aryUrl.size(),MAX_WIKIPEDIA_URL); i++){
				szTempLink = aryUrl.get(i);
				
				//remove markup
				szTempLink = szTempLink.replaceAll("<[^>]+>", "");

				//yahoo boos specific problem, it returns URL in text form, so we need to un-escape it 
				// http://dbpedia.org/resource/AT&amp;T
				szTempLink = ToolWeb.unescapeHTML(szTempLink);

				//switch from wikipedia URL to dbpedia URI
				szTempLink = wikipediaUrl2dbpediaUri(szTempLink);

				//save the first link
				if (0==i){
					ret.put(NAME2DBPEDIA_URI_FIRST, szTempLink);					
				}
				
				//check with dbpedia to see if the uri is valid 
				
				// load dbpedia URI following linked data principle
				Model m = ModelFactory.createDefaultModel();
				try {
					m= m.read(szTempLink);
				}catch(JenaException e){
					e.printStackTrace();
					m = null;
				}
				
				//check if the model is valid
				if (null== m){
					szTempLink =null;
					System.out.println("cannot load dbpedia data");
					continue;
				}
				
				//check if this is a disambiguation file
				if (m.listObjectsOfProperty(m.createResource(szTempLink),m.createProperty("http://dbpedia.org/ontology/wikiPageDisambiguates")).hasNext()){
					szTempLink =null;
					System.out.println("encounter a disambiguation page");
					continue;					
				}
					
				
				//verify the mapping is correct
				if (!verifyMapping(name, m, stopWords)){
					System.out.println("cannot verify the mapping");
					System.out.println(szTempLink);
					szTempLink =null;
				}else{
					message = "matched at trial "+ i;
					ret.put(NAME2DBPEDIA_MESSAGE, message);
					ret.put(NAME2DBPEDIA_URI_VERIFIED, szTempLink);	
					String temp = "";

					temp = "";
					for (RDFNode node: m.listObjectsOfProperty(m.createProperty("http://dbpedia.org/property/abbreviation")).toSet()){
						temp = node.asLiteral().getString();
						break;
					}
					ret.put(NAME2DBPEDIA_ABBREVIATION, temp);

					temp = "";
					for (RDFNode node: m.listObjectsOfProperty(m.createProperty("http://dbpedia.org/property/name")).toSet()){
						if (!node.isLiteral())
							continue;
						temp = node.asLiteral().getString();
						break;
					}
					ret.put(NAME2DBPEDIA_DBPEDIANAME, temp);
					
					temp = "";
					for (RDFNode node: m.listObjectsOfProperty(m.createProperty("http://xmlns.com/foaf/0.1/homepage")).toSet()){
						temp = node.asResource().getURI();
						break;
					}
					ret.put(NAME2DBPEDIA_HOMEPAGE, temp);
					
					
					temp = "";
					for (Resource res: m.listSubjectsWithProperty(m.createProperty("http://dbpedia.org/ontology/wikiPageRedirects")).toSet()){
						String label = res.getLocalName();
						if (label.matches("[A-Z]+") && ToolSafe.isEmpty(ret.getAsString(NAME2DBPEDIA_ABBREVIATION))){
							ret.put(NAME2DBPEDIA_ABBREVIATION, label);							
						}
							
						label = label.replaceAll("_"," ");
						if (temp.length()>0)
							temp +="\t";
						temp +=label;
					}
					ret.put(NAME2DBPEDIA_REDIRECT, temp);
					
					return ret;					
				}
			}
			
			//return fail when no match can be found
			System.out.println(szContent);
			message = String.format("cannot find match after %d trials ", Math.min(aryUrl.size(),MAX_WIKIPEDIA_URL));
			ret.put(NAME2DBPEDIA_MESSAGE, message);
			return ret;

		} catch (Sw4jException e) {
			e.printStackTrace();
		}
		message = "cannot load query result";
		ret.put(NAME2DBPEDIA_MESSAGE, message);
		return ret;
	}
	
	public static String wikipediaUrl2dbpediaUri(String szUrlWikipedia){
		String szTemp = szUrlWikipedia;
		szTemp = szTemp.replaceAll("&", "%26");
		szTemp = szTemp.replaceAll("en.wikipedia.org/wiki", "dbpedia.org/resource");
		return szTemp;
	}
	
	private static String normalizeName(String name, Collection<String> stopWords){
		String temp = name;
		temp = temp.replaceAll("\\.", "");
		temp = temp.replaceAll("[\\W_]+"," ");
		temp = temp.toLowerCase();
		StringTokenizer st = new StringTokenizer(temp);
		
		String ret ="";
		while (st.hasMoreTokens()){
			String token = st.nextToken();
			
			//heuristic 1: skip common stop words
			for (String stopword: new String[]{"of","the","and"}){
				if (stopword.equals(token)){
					token = "";
					break;
				}
			}

			//heuristic 2: skip single character word
			if (token.length()<=1){
				token ="";
			}

			//heuristic 3: skip customized stop words
			if (!ToolSafe.isEmpty(stopWords)){
				for (String stopword: stopWords){
					if (stopword.toLowerCase().equals(token)){
						token = "";
						break;
					}
				}				
			}


			if (ret.length()>0 && !ret.endsWith(" "))
				ret +=" ";
			ret += token;
		}
		return ret;
	}	
	private static boolean verifyMapping(String name, Model m, Collection<String> stopWords){
		String myname = normalizeName(name, stopWords);
		String [][] landmark = new String[][]{
				{"LABEL", normalizeName(m.listObjectsOfProperty(RDFS.label).toSet().toString(),stopWords)},
				{"REDIRECT", normalizeName(m.listSubjectsWithProperty(ResourceFactory.createProperty("http://dbpedia.org/ontology/wikiPageRedirects")).toSet().toString(),stopWords)}
		};
//		for (Statement stmt: m.listStatements().toSet()){
	//		System.out.println(stmt);
	//	}
		
		
		for (String [] entry : landmark){
			if (entry[1].indexOf(myname)>=0){
				System.out.println(String.format("Matched by [%s] --- \t input:%s; \t output:%s",  entry[0], myname, entry[1] ));
				return true;				
			}
		}
		return false;
	}
	
	public static  String name2homepage(String name){
		String appid = "b3Pn_9XV34FamuvQvH.pRRLjb.m01on0MVvqbgjeLF0aCfOtoTie7GRAQRqHJjz";
		String query = name;
		query = query.replaceAll("\\s", "+");
		//String szUrl = String.format("http://pipes.yahoo.com/pipes/pipe.run?_id=8229ea1d5fe540124faa315e2d11e25b&_render=rss&name=%s", name.replaceAll("\\s", "+"));
		String szUrl = String.format("http://boss.yahooapis.com/ysearch/web/v1/%s?appid=%s&format=xml",  query ,appid);
		try {
			String szContent;
			szContent = ToolIO.pipeUrlToString(szUrl);
						
			if (ToolWeb.extractMarkup(szContent, "<url>", "</url>").size()<=0){
				System.out.println(szContent);
				return null;
			}
			String szTempLink,szTempTitle;
			szTempLink = ToolWeb.extractMarkup(szContent, "<url>", "</url>").get(0);
			szTempLink = szTempLink.replaceAll("<[^>]+>", "");
			szTempLink = szTempLink.replaceAll("en.wikipedia.org/wiki", "dbpedia.org/resource");

			szTempTitle= ToolWeb.extractMarkup(szContent, "<title>", "</title>").get(0);
			szTempTitle = szTempTitle.replaceAll("<[^>]+>", "");
						
			if (szTempTitle.toLowerCase().indexOf(name.toLowerCase())<0){
				System.out.println(String.format("result:%b \t input:%s; \t output:%s", szTempTitle.toLowerCase().indexOf(name.toLowerCase())>=0, name,szTempTitle ));
				System.out.println(szTempLink);
				return null;
			}
			
			return szTempLink;
		} catch (Sw4jException e) {
			e.printStackTrace();
		}
		return null;
	}
}
