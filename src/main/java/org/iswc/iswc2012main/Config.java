package org.iswc.iswc2012main;

import java.io.File;

public class Config {

	public enum RDFSYNTAX{
		N3 ("N3"),
		NT ("N-TRIPLE"),
		RDFXML ("RDF/XML"),
		TURTLE ("TURTLE"),
		RDFXML_ABBREV ("RDF/XML-ABBREV"),
		SPARQL_XML ("sparql/xml"),
		SPARQL_JSON ("sparql/json"), 
		CSV ("CSV"), 
		TSV ("TSV"),
		SPARQL_TXT("TXT"),
		;
		String v;
		RDFSYNTAX(String v){
			this.v =v;
		}
		public String getValue(){
			return v;
		}
	}
	
	public static final String VERSION= "iswc2012 metadata v20121029";
	public static final String META_YEAR= "2012";
	public static final String META_MONTH = "November";
	public static final String META_PUBLISHER = "Springer";
	public static final String META_ADDRESS = "Boston, US";
	public static final String META_BOOKTITLE = "Proceedings of the 11th International Semantic Web Conference (ISWC 2012)";
	
	

	public enum PATH{
		local_stick_pdf,
		local_stick,
		
		local_iswc2012full,
		local_metadata2012csv,
		local_raw,
		local_metadata4iswc,
		local_iswc2012submission,
		
		data_rdf,
		data_query,
		data_queryoutput,
		data_rawsite, 
		
		data_proceedings, //html index derived from our metadata
		
		;
		File getFile(){
			return new File(name().replace("_", "/"));
		}
	}
	
	public enum EXT {
		rdf,
		sparql,
		csv,
		xml,
		n3,
		ttl,
		pdf,
		tsv,
		htm,
		html,
		txt,
	}
	
	public enum FILE{
		iswc2010_complete (PATH.local_metadata4iswc, EXT.rdf),
		iswc2011_complete (PATH.local_metadata4iswc, EXT.rdf),

		iswc2012_complete (PATH.data_rdf, EXT.rdf), 

		iswc2012_front (PATH.local_raw, EXT.pdf),

		query_select_conf_metadata (PATH.data_query, EXT.sparql),
		query_select_conf_metadata2 (PATH.data_query, EXT.sparql),
		query_select_conf_role(PATH.data_query, EXT.sparql),
		query_select_organization (PATH.data_query, EXT.sparql), 
		query_select_person  (PATH.data_query, EXT.sparql), 
		query_select_person_role  (PATH.data_query, EXT.sparql), 
		query_select_event(PATH.data_query, EXT.sparql),
		query_select_paper(PATH.data_query, EXT.sparql),
		query_select_paper_count(PATH.data_query, EXT.sparql),
		query_select_role(PATH.data_query, EXT.sparql),
		query_select_role_count(PATH.data_query, EXT.sparql),
		query_select_vocab_domain_range (PATH.data_query, EXT.sparql),		

		
		data_person (PATH.local_metadata2012csv, EXT.csv),	
		data_conf (PATH.local_metadata2012csv, EXT.csv),	
		data_paper (PATH.local_metadata2012csv, EXT.csv),		
		data_event (PATH.local_metadata2012csv, EXT.csv),
		
		html_research ("research-papers", PATH.data_rawsite, EXT.htm),
		html_inuse ("use-papers", PATH.data_rawsite, EXT.htm),
		html_doctoral_consortium ("doctoral-consortium", PATH.data_rawsite, EXT.htm),
		html_evaluation ("evaluations-and-experiments-papers", PATH.data_rawsite, EXT.htm),
		html_industry ("industry-track-presentations", PATH.data_rawsite, EXT.htm),
		html_poster_demo ("posters-and-demos", PATH.data_rawsite, EXT.htm),
		html_semantic_web_challenge ("Semantic Web Challenge", PATH.data_rawsite, EXT.htm),
		csv_paper_cleanup(PATH.data_rawsite, EXT.csv),
		;
		
		String filename=null;
		PATH path;
		EXT ext;
		FILE(PATH path, EXT ext){
			this.path = path;
			this.ext =ext;
		}
		FILE(String name, PATH path, EXT ext){
			this.filename = name;
			this.path = path;
			this.ext =ext;
		}
		String getFileName(){
			return getFileName(this.ext);
		}
		String getFileName(EXT ext){
			String name = name();
			if (null!=this.filename)
				name = this.filename;
			return String.format("%s.%s", name, ext);
		}
		public File getFile(){
			return getFile(path, ext);
		}
		File getFile(EXT ext){
			return getFile(path, ext);
		}
		File getFile(PATH path, EXT ext){
			return new File(path.getFile(), getFileName(ext));
		}
		
		public String getKeyTrack(){
			String name = name();
			if (name.startsWith("html_")){
				name=name.substring(5);
				name =name.replace('_', '-');
				return String.format("track/%s",name);
			}
			return "";
		}
	}
	
}
