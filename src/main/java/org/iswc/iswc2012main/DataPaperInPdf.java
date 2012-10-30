package org.iswc.iswc2012main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.iswc.util.ToolText2Rdf;


import sw4j.util.DataSmartMap;
import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;

public class DataPaperInPdf {

	public enum STATE{
		ready,
		title,
		author ,
		affiliation,
		_abstract,
		keyword,
		content,
	}
	
	TreeMap<STATE,String> data = new TreeMap<STATE,String>();
	public STATE state = STATE.ready;
	String id = null;
	
	public DataPaperInPdf(String id) {
		super();
		this.id = id;
	}

	private void appendContent(STATE prop, String line){
		String content = data.get(prop);
		if (null==content){
			content = line;
		}else{
			content += " " +line.trim();
		}
		data.put(prop, content);
	}
	
	static TreeSet<String> setName = new TreeSet<String>();
	private boolean isAuthorLine(String line){
		if (setName.size()==0){
			File fName = new File("local/misc/name.txt");
			if (fName.exists()){
				for (String name: pipeFileToStringArray(fName)){
					for (String temp: name.split(" ")){
						temp = ToolText2Rdf.extractLocalName(temp);
						
						setName.add(temp);						
					}
				}
			}
		}
		

		String[] words = line.split("[\\s,]");
		int index_base =0;
		if (words.length==0)
			return false;
		if (words[0].equals("and"))
			index_base++;
		
		for (int i=index_base; i<words.length; i++){
			String name =words[i];
			name = ToolText2Rdf.extractLocalName(name);
			boolean ret =  setName.contains(name);
			if (ret){
				return true;
			}
		}
		return false;
	}
	
	public static List<String> pipeFileToStringArray(File f){
		ArrayList<String> ret = new ArrayList<String>();
		try {
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(ToolIO.prepareFileInputStream(f)));
			String line;
			while (null!=(line=reader.readLine())){
				ret.add(line);
			}
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
    int lineNumber = 0;
    int cntChar = 0;
	public STATE processLine(String line){
    	lineNumber++;
    	cntChar += line.length();
    	
    	String tempStr = line.replaceAll("\\s+", " ").toLowerCase();
    	tempStr= tempStr.trim();
    	if (tempStr.length()==0)
    		return state;
    	
    	System.out.println(String.format("%03d:%s\t%s", lineNumber, state, line));

    	
    	if (STATE.ready.equals(state)){
	        System.out.println("---title--");
	        data.put(STATE.ready, id);
	        state = STATE.title;    		
		}else if (!STATE.content.equals(state)&& tempStr.startsWith("abstract.")){
	        System.out.println("---abstract--");
	        state = STATE._abstract;
	        line = line.substring("abstract.".length()).trim();	        
		}else if (!STATE.content.equals(state)&& tempStr.startsWith("abstract")){
	        System.out.println("---abstract--");
	        state = STATE._abstract;
	        line = line.substring("abstract".length()).trim();
		}else if (STATE.title.equals(state)&& tempStr.indexOf("springer")>0){
	        System.out.println("---title--");
	        data.put(STATE.title, null);//reset
	        line = "";
	        
		}else if (STATE.title.equals(state)&& isAuthorLine(tempStr)){
	        System.out.println("---author--");
	        state = STATE.author;
		}else if (STATE.author.equals(state)&& !isAuthorLine(tempStr)){
	        System.out.println("---affiliation--");
	        state = STATE.affiliation;
	
		}else if (STATE._abstract.equals(state)&& tempStr.startsWith("keywords:")){
	        System.out.println("---keywords--");
	        state = STATE.keyword;
	        line = line.substring("keywords:".length()).trim();

		}else if (tempStr.startsWith("1 introduction")){
            System.out.println("---INTRODUCTION--");
            state = STATE.content;
    	}else if (tempStr.startsWith("1 motivation")){
            System.out.println("---MOTIVATION--");
            state = STATE.content;
    	}else if (tempStr.startsWith("1 sparql")){
            System.out.println("---SPARQL--");
            state = STATE.content;
    	}else if (tempStr.startsWith("1 background")){
            System.out.println("---Background--");
            state = STATE.content;
    	}else if (tempStr.startsWith("1 research")){
            System.out.println("---Research--");
            state = STATE.content;
    	}else if (tempStr.startsWith("1 problem")){
            System.out.println("---Problem--");
            state = STATE.content;
    	}else if (tempStr.startsWith("1 smart")){
            System.out.println("---Smart--");
            state = STATE.content;
    	}else if (cntChar >5000){
    		if (!STATE.content.equals(state)){
                System.out.println("---5000--");
                state = STATE.content;    			
    		}
    	}

    	if (line.length()==0)
    		return state;
    	
    	appendContent(state, line);
    	
        
    	return state;
	}
	
	public void cleanup(){
		
		for (STATE state: STATE.values()){
			String value = data.get(state);
			if (null==value)
				continue;
			
			if (STATE._abstract.equals(state)){
				value = value.replaceAll("- ", "");
			}
//			value.replaceAll("’", "'");
			value.replaceAll("�", "\"");
			value.replaceAll("�", "\"");
//			value.replaceAll("–","-");
			
			data.put(state, value);
		}
	}

	public void printReport(){
		cleanup();
		
		System.out.println("----report of paper content-----");

		DataSmartMap dsm = new DataSmartMap();
		for (STATE state: STATE.values()){
			dsm.addStringProperty(state.toString());
			if (null==data.get(state)){
				data.put(state, "");
			}
		}
		for(STATE state: data.keySet()){
			dsm.put(state.name(), data.get(state));
			System.out.println(String.format("[%s]\t%s", state, data.get(state)));
		}
		
		
		File fOut = new File("local/output/pdf.tsv");
		try {
			ToolIO.pipeStringToFile( String.format("%s\t%s\n", dsm.toTSVrow(), dsm.toTSVheader()),fOut.getAbsolutePath(), false, true);
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
