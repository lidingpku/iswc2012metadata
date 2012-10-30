package org.iswc.iswc2012main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;

import org.iswc.iswc2012main.TaskConvertCsv2Rdf.CsvHeader;
import org.iswc.vocabulary.BIBO;
import org.iswc.vocabulary.SWC;
import org.iswc.vocabulary.SWRC;

import sw4j.rdf.util.ToolOwl2Java;
import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.web.ToolWeb;

import com.csvreader.CsvReader;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class TaskConvertCsv2Html {
	
	public static void main(String[] args){
		try {
			processPaperUsb();
			processPaperIndex();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	enum TemplatePaperProperty{
		uri,
		id ,
		author,
		title,
		booktitle,
		year,
		
		address,
		month,
		_abstract,
		
		pdf,
		pdfLocal,
		note,
		title_escaped,
		;
		public String getName(){
			return name().replaceAll("^_","");
		}
	}
	
	enum Param{
		file_data_paper,
		file_data_event,
		
		paper_page,
		index_page,
		index_section,
		
		output_file_index,
		output_dir_paper,
	}
	
	private static void processPaperUsb() throws IOException, Sw4jException {
		TreeMap<Param,String> params = new TreeMap<Param,String>();
		params.put(Param.file_data_paper, Config.FILE.data_paper.getFile().getAbsolutePath());
		params.put(Param.file_data_event, Config.FILE.data_event.getFile().getAbsolutePath());

		params.put(Param.paper_page, "template.paper.page.htm");
		params.put(Param.index_page, "template.index.page.htm");
		params.put(Param.index_section, "template.index.section.txt");
		
		params.put(Param.output_file_index, new File(Config.PATH.local_stick_pdf.getFile(), "_index."+Config.EXT.html).getAbsolutePath());
		params.put(Param.output_dir_paper, Config.PATH.local_stick_pdf.getFile().getAbsolutePath());
		
		processPaper(params);
		
	}
	private static void processPaperIndex() throws IOException, Sw4jException {
		TreeMap<Param,String> params = new TreeMap<Param,String>();
		params.put(Param.file_data_paper, Config.FILE.data_paper.getFile().getAbsolutePath());
		params.put(Param.file_data_event, Config.FILE.data_event.getFile().getAbsolutePath());

		params.put(Param.paper_page, "template.open.paper.page.htm");
		params.put(Param.index_page, "template.open.index.page.htm");
		params.put(Param.index_section, "template.open.index.section.txt");
		
		params.put(Param.output_file_index, new File(Config.PATH.data_proceedings.getFile(), "index."+Config.EXT.html).getAbsolutePath());
		params.put(Param.output_dir_paper, Config.PATH.data_proceedings.getFile().getAbsolutePath());
		
		processPaper(params);
	}
	
	private static void processPaper(TreeMap<Param,String> params) throws IOException, Sw4jException {

    	String templatePageAbstract = ToolIO.pipeInputStreamToString(
    			TaskConvertCsv2Html.class.getResourceAsStream(params.get(Param.paper_page)));

    	String templatePageIndex = ToolIO.pipeInputStreamToString(
    			TaskConvertCsv2Html.class.getResourceAsStream(params.get(Param.index_page)));
    	
    	String templateIndexSection= ToolIO.pipeInputStreamToString(
    			TaskConvertCsv2Html.class.getResourceAsStream(params.get(Param.index_section)));
    	String sectionContent = "";
    	String sectionIndex = "";
		String trackPrev = null;
		String groupPrev = null;

//		//keynotes from data-event
//		{
//			String track =  "Keynotes";
//			String trackBookmark = track.replaceAll("\\s+", "");
//			
//			sectionContent += String.format("<h2 id=\"%s\">%s</h2>\n<ol>", trackBookmark, track);
//			sectionIndex += String.format("<li><a href=\"#%s\">%s</a></li>\n", trackBookmark, track);
//
//			CsvReader reader = new CsvReader(params.get(Param.file_data_event));
//			reader.setSkipEmptyRecords(true);
//			reader.readHeaders();
//			while(reader.readRecord()){
//
//				TreeMap<TemplatePaperProperty,String> data = new TreeMap<TemplatePaperProperty,String>(); 
//				
//				String keyEvent= reader.get(CsvHeader.keyEvent.name());
//				if (!keyEvent.startsWith("talk/keynote"))
//					continue;
//				
//				String id = keyEvent.replaceAll("talk/", "iswc2012paper-"); 
//				
//				setValue(data, TemplatePaperProperty.uri, String.format("http://data.semanticweb.org/conference/iswc/2012/%s", keyEvent));
//				setValue(data, TemplatePaperProperty.id, id );
//				setValue(data, TemplatePaperProperty.author, reader.get(CsvHeader.keynoteSpeaker.name()) );
//				setValue(data, TemplatePaperProperty.title, reader.get(CsvHeader.label.name()) );
//				setValue(data, TemplatePaperProperty.title_escaped, reader.get(CsvHeader.label.name()).replaceAll("\"","&quot;") );
//				setValue(data, TemplatePaperProperty._abstract, reader.get(CsvHeader.hasAbstract.name()) );
//				setValue(data, TemplatePaperProperty.booktitle, Config.META_BOOKTITLE );
//				setValue(data, TemplatePaperProperty.year, Config.META_YEAR);
//				setValue(data, TemplatePaperProperty.month, Config.META_MONTH);
//				setValue(data, TemplatePaperProperty.address, Config.META_ADDRESS);
//
//				String urlPdf = reader.get(CsvHeader.paperPdfLink.name());
//				setValue(data, TemplatePaperProperty.pdf, urlPdf);
//				if (null!=urlPdf){
//					String urlPdfLocal = String.format("%s", urlPdf.substring(urlPdf.lastIndexOf("/")+1));
//					setValue(data, TemplatePaperProperty.pdfLocal, urlPdfLocal );
//					
//				}
//
//				{
//					String content = createHtml(templatePageAbstract, data);
//					File f = new File( new File (params.get(Param.output_dir_paper)), id+"."+Config.EXT.html);
//					ToolIO.pipeStringToFile(content, f);				
//				}
//				{
//					String fragment = String.format("<li>%s</li>",createHtml(templateIndexSection, data));
//					if (fragment.indexOf("___pdf___")>=0){
//						fragment =fragment.replace("<a href=\"___pdf___\">","<a>");
//						
//					}
//					sectionContent += fragment;
//				}
//			}
//			
//			// close track
//			sectionContent +="</ol><p><a href=\"#Top\">top</a></p>";	
//			File fIndex = new File( params.get(Param.output_file_index) );
//		}
		
		//papers from data-paper
		CsvReader reader = new CsvReader(params.get(Param.file_data_paper));
		reader.setSkipEmptyRecords(true);
		reader.readHeaders();
		while(reader.readRecord()){

			TreeMap<TemplatePaperProperty,String> data = new TreeMap<TemplatePaperProperty,String>(); 
			
			String keyPaper = reader.get(CsvHeader.keyPaper.name());
			String id = keyPaper.replaceAll("paper/", "iswc2012paper-"); 
			
			setValue(data, TemplatePaperProperty.uri, String.format("http://data.semanticweb.org/conference/iswc/2012/%s", keyPaper));
			setValue(data, TemplatePaperProperty.id, id );
			setValue(data, TemplatePaperProperty.author, reader.get(CsvHeader.authors.name()) );
			setValue(data, TemplatePaperProperty.title, reader.get(CsvHeader.title.name()) );
			setValue(data, TemplatePaperProperty.title_escaped, reader.get(CsvHeader.label.name()).replaceAll("\"","&quot;") );
			setValue(data, TemplatePaperProperty.booktitle, reader.get(CsvHeader.booktitle.name()) );
			setValue(data, TemplatePaperProperty.year, Config.META_YEAR);
			setValue(data, TemplatePaperProperty.month, Config.META_MONTH);
			setValue(data, TemplatePaperProperty.address, Config.META_ADDRESS);
			setValue(data, TemplatePaperProperty._abstract, reader.get(CsvHeader.hasAbstract.name()) );
			setValue(data, TemplatePaperProperty.note, reader.get(CsvHeader.paperSpotlight.name()) );
			
			String urlPdf = reader.get(CsvHeader.paperPdfLink.name());
			setValue(data, TemplatePaperProperty.pdf, urlPdf);
			if (null!=urlPdf){
				String urlPdfLocal = String.format("%s", urlPdf.substring(urlPdf.lastIndexOf("/")+1));
				setValue(data, TemplatePaperProperty.pdfLocal, urlPdfLocal );
				
			}
			

			{
				String content = createHtml(templatePageAbstract, data);
				File f = new File( new File (params.get(Param.output_dir_paper)), id+"."+Config.EXT.html);
				ToolIO.pipeStringToFile(content, f);				
			}
			{
				String group =  reader.get(CsvHeader.nameGroup.name());
			

				String track =  reader.get(CsvHeader.nameTrack.name());
				String trackBookmark = track.replaceAll("\\s+", "");
				if (!track.equals(trackPrev)){
					if (null!=trackPrev){
						sectionContent +="</ol><p><a href=\"#Top\">top</a></p>";										
					}

					//new track
					sectionContent += String.format("<h2 id=\"%s\">%s</h2>\n", trackBookmark, track);
					sectionIndex += String.format("<li><a href=\"#%s\">%s</a></li>\n", trackBookmark, track);
					trackPrev = track;	

					//new group as well
					sectionContent += String.format("<h3>%s</h3><ol>", group);
					groupPrev = group;	
					
				}else{
					if (null!=group){
						if (!group.equals(groupPrev)){
							if (null!=groupPrev){
								sectionContent +="</ol>";										
							}
							sectionContent += String.format("<h3>%s</h3><ol>", group);
							groupPrev = group;	
						}				
					}
				}
				
				{
					String fragment = String.format("<li>%s</li>",createHtml(templateIndexSection, data));
					if (fragment.indexOf("___pdfLocal___")>=0){
						fragment =fragment.replace("<a href=\"___pdfLocal___\">","<a>");
					}
					if (fragment.indexOf("___pdf___")>=0){
						fragment =fragment.replace("<a href=\"___pdf___\">","<a>");
					}
					sectionContent += fragment;
				}
			}
		}
		
		// close track
		sectionContent +="</ol><p><a href=\"#Top\">top</a></p>";	
		File fIndex = new File( params.get(Param.output_file_index) );
		
		templatePageIndex = templatePageIndex.replaceAll("___SECTION_INDEX___", sectionIndex);
		templatePageIndex = templatePageIndex.replaceAll("___SECTION_CONTENT___", sectionContent);
		ToolIO.pipeStringToFile(templatePageIndex, fIndex);	
	}


	private static String createHtml(String template,	TreeMap<TemplatePaperProperty, String> data) {
		String content = template;
		for (TemplatePaperProperty p: TemplatePaperProperty.values()){
			String value = data.get(p);
			if (null==value || value.length()==0){
				//remove span 
				content = spanRemove(content, p.getName());
			}else{
				content = spanUpdate(content, p.getName(), value);
			}
		}
		

		//System.out.println(content);
		return content;
	}

	
	private static String spanRemove(String template, String p){
		String temp = ToolWeb.removeMarkup(template, String.format("<span id=\"%s\"", p), "</span>");
		temp  = ToolWeb.removeMarkup(temp, String.format("<span class=\"%s\"", p), "</span>");
		return temp;
	}

	private static String spanUpdate(String template, String p, String v){
		String locator = String.format("___%s___", p);
		return template.replaceAll(locator, v);
	}
	private static void setValue(TreeMap<TemplatePaperProperty, String> data,
			TemplatePaperProperty p, String v) {
		if (null==v){
			return;
		}
		data.put(p,v);		
	}

}
