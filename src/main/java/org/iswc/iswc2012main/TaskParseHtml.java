package org.iswc.iswc2012main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.iswc.iswc2012main.Config.FILE;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;

public class TaskParseHtml {

	
	public static void main(String[] args) {
		run();
	}

	
	public static void test(){
		debug =true;
		try {
			ToolIO.pipeStringToFile(getCsvHeader(), Config.FILE.csv_paper_cleanup.getFile(), false, false);

			runOne(Config.FILE.html_industry, 12, Config.FILE.csv_paper_cleanup);

			
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void run(){
		try {
			ToolIO.pipeStringToFile(getCsvHeader(), Config.FILE.csv_paper_cleanup.getFile(), false, false);

			if (!runOne(Config.FILE.html_research, 41, Config.FILE.csv_paper_cleanup)){			
				return;
			}
			if (!runOne(Config.FILE.html_inuse, 17, Config.FILE.csv_paper_cleanup)){			
				return;
			}
			if (!runOne(Config.FILE.html_evaluation, 8, Config.FILE.csv_paper_cleanup)){			
				return;
			}
			if (!runOne(Config.FILE.html_doctoral_consortium, 15, Config.FILE.csv_paper_cleanup)){			
				return;
			}
			if (!runOne(Config.FILE.html_poster_demo, 31, Config.FILE.csv_paper_cleanup)){			
				return;
			}
			if (!runOne(Config.FILE.html_industry, 12, Config.FILE.csv_paper_cleanup)){			
				return;
			}
			if (!runOne(Config.FILE.html_semantic_web_challenge, 24, Config.FILE.csv_paper_cleanup)){			
				return;
			}


			logInfo("All Done!");

		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	static boolean debug = false;
	
	private static void logInfo(Object o){
		System.out.println(o);		
	}
	private static void logV(Object o){
		if (debug){
			System.out.println(o);
		}
	}
	public static String getCsvHeader(){
		String line = "";
		for (EnumPaper p: EnumPaper.values()){
			line += String.format("%s,", p);
		}
		line+="EOL\n";
		return line;
	}
	
	private static boolean runOne(FILE input, int expectedSize, FILE output) throws Sw4jException {
		List<TreeMap<EnumPaper,String>> ret = null;
		if (Config.FILE.html_doctoral_consortium.equals(input)){
			ret = parseHtmlDc(input.getFile());
		}else if (Config.FILE.html_poster_demo.equals(input)){
			ret = parseHtmlPd(input.getFile());
		}else if (Config.FILE.html_inuse.equals(input)){
			ret = parseHtmlRegular(input.getFile());
		}else if (Config.FILE.html_evaluation.equals(input)){
			ret = parseHtmlRegular(input.getFile());
		}else if (Config.FILE.html_research.equals(input)){
			ret = parseHtmlRegular(input.getFile());
		}else if (Config.FILE.html_industry.equals(input)){
			ret = parseHtmlIndustry(input.getFile());
		}else if (Config.FILE.html_semantic_web_challenge.equals(input)){
			ret = parseHtmlSwc(input.getFile());
		}
		
		
		logInfo("=============final list of paper ("+ret.size()+ ") for "+ input.name()+" =================");
		for (TreeMap<EnumPaper,String> p: ret){
			logInfo(p);
		}
		

		//validate
		if (ret.size()!=expectedSize){
			logInfo("FAILED");
			System.exit(0);
			return false;
		}
		
		//save
		for (TreeMap<EnumPaper, String> paper: ret){
			//append track key
			paper.put(EnumPaper.track, input.getKeyTrack());

			//append local pdf
			String urlPdf = paper.get(EnumPaper.paperPdfLink);
			if (null!=urlPdf && urlPdf.length()>0){
				String filenamePdf = urlPdf.substring(urlPdf.lastIndexOf("/")+1);
				paper.put(EnumPaper.paperPdfLinkFile, "pdf/"+filenamePdf);
				File f = new File (Config.PATH.local_stick_pdf.getFile(), filenamePdf);
				if (!f.exists()){
					File fSubmission = new File (Config.PATH.local_iswc2012submission.getFile(), filenamePdf);
					if (fSubmission.exists()){
						fSubmission.renameTo(f);
					}else{
						logInfo("missing file "+ f.getName());
						System.exit(0);
					}
				}
			}
			
			String line = "";
			for (EnumPaper p: EnumPaper.values()){
				String value = paper.get(p);
				if (null==value)
					value="";
				else
					value= value.replaceAll("\"", "\\\"");
				line += String.format("\"%s\",", value);
			}
			line+="\n";
			ToolIO.pipeStringToFile(line, output.getFile(), false, true);
		}
		return true;
	}

	
	enum StateRegular{
		ready(""),
		sessionTimeTitle ("h3"),
		sessionChair("p"),
		paper("li"),
		finish(""),
		groupH3("h3"),
		paperDc("li"),
		sessionTimeTitleIndustry("h2"),
		paperIndustry("li"),
		groupH2("h2"),
		paperSwc("li"),
		;
		String tag;
		StateRegular(String mark){
			this.tag = mark;
		}
	}
	

	private static List<TreeMap<EnumPaper,String>> parseHtmlRegular(File f) throws Sw4jException{
		String content = ToolIO.pipeFileToString(f);
			
		//parse html
		ToolHtmlParser parser = new ToolHtmlParser();		
		parser.initTag(StateRegular.sessionTimeTitle.tag);				
		parser.initTag(StateRegular.sessionChair.tag);				
		parser.initTag(StateRegular.paper.tag);		
		parser.run(content, StateRegular.sessionTimeTitle.tag);

		//extact results
		StateRegular state = StateRegular.ready;
		
		List<TreeMap<EnumPaper,String>> listPaper = new ArrayList<TreeMap<EnumPaper,String>>();
		TreeMap<EnumPaper,String> paper = null; 
		int indexPaperInSession=0;
		int indexPaperInTrack =0;
		int indexSessionInTrack=0;
		
		for (String line: parser.getResult()){
			String [] lineData = ToolHtmlParser.parseLine(line);
			
			//skip
			if (line.indexOf("http://www.w3.org/")>=0){
				continue;
			}
			
			if (StateRegular.ready.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.sessionTimeTitle, paperNew )){
					state = StateRegular.sessionTimeTitle;
					paper = paperNew;
					
					indexSessionInTrack++;
					indexPaperInSession =0;
					
				}
			}else if (StateRegular.sessionTimeTitle.equals(state)){
				if (tryNextState (lineData, StateRegular.sessionChair, paper)){
					state = StateRegular.sessionChair;
					//do nothing				
				}
			}else if (StateRegular.sessionChair.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paper, paper)){
					state = StateRegular.paper;
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);

					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));
					
				}
			}else if (StateRegular.paper.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paper, paper)){
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);

					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));
					
				}else if (tryNextState (lineData, StateRegular.sessionTimeTitle, paperNew)){
					state = StateRegular.sessionTimeTitle;
					paper = paperNew;					
					indexSessionInTrack++;
					indexPaperInSession =0;
				}
			}
		}
		
		return listPaper;
	}
	
	private static String formatIndex(int value) {
		return String.format("%02d", value);
	}
	private static List<TreeMap<EnumPaper,String>> parseHtmlDc(File f) throws Sw4jException{
		String content = ToolIO.pipeFileToString(f);
			
		//parse html
		ToolHtmlParser parser = new ToolHtmlParser();		
		parser.initTag(StateRegular.groupH3.tag);		
		parser.initTag(StateRegular.paperDc.tag);		
		parser.run(content, StateRegular.groupH3.tag);

		//extact results
		StateRegular state = StateRegular.ready;
		int indexSessionInTrack=0;
		int indexPaperInSession=0;
		int indexPaperInTrack =0;

		List<TreeMap<EnumPaper,String>> listPaper = new ArrayList<TreeMap<EnumPaper,String>>();
		TreeMap<EnumPaper,String> paper = null; 
		for (String line: parser.getResult()){
			String [] lineData = ToolHtmlParser.parseLine(line);
			
			if (line.indexOf("Whittier")>0)
				continue; //skip line
			
			if (StateRegular.ready.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.groupH3, paperNew )){
					state = StateRegular.groupH3;
					paper = paperNew;
					
					indexSessionInTrack++;
					indexPaperInSession =0;

				}
			}else if (StateRegular.groupH3.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paperDc, paper)){
					state = StateRegular.paperDc;
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);
					

					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));
				}
			}else if (StateRegular.paperDc.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paperDc, paper)){
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);


					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));
					
				}else if (tryNextState (lineData, StateRegular.groupH3, paperNew)){
					state = StateRegular.groupH3;
					paper = paperNew;	
					
					indexSessionInTrack++;
					indexPaperInSession =0;

				}
			}
			
			
		}
		
		return listPaper;

	}
	
	private static List<TreeMap<EnumPaper,String>> parseHtmlIndustry(File f) throws Sw4jException{
		String content = ToolIO.pipeFileToString(f);
			
		//parse html
		ToolHtmlParser parser = new ToolHtmlParser();		
		parser.initTag(StateRegular.sessionTimeTitleIndustry.tag);				
		parser.initTag(StateRegular.sessionChair.tag);				
		parser.initTag(StateRegular.paperIndustry.tag);		
		parser.run(content, StateRegular.sessionTimeTitleIndustry.tag);

		//extact results
		StateRegular state = StateRegular.ready;
		int indexSessionInTrack=0;
		int indexPaperInSession=0;
		int indexPaperInTrack =0;

		List<TreeMap<EnumPaper,String>> listPaper = new ArrayList<TreeMap<EnumPaper,String>>();
		TreeMap<EnumPaper,String> paper = null; 
		for (String line: parser.getResult()){
			String [] lineData = ToolHtmlParser.parseLine(line);
			
			//skip special line
			if (line.indexOf("ISWC2012")>0)
				continue; 
			
			if (StateRegular.ready.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.sessionTimeTitleIndustry, paperNew )){
					state = StateRegular.sessionTimeTitleIndustry;
					paper = paperNew;

					indexSessionInTrack++;
					indexPaperInSession =0;
				}
			}else if (StateRegular.sessionTimeTitleIndustry.equals(state)){
				if (tryNextState (lineData, StateRegular.sessionChair, paper)){
					state = StateRegular.sessionChair;
					//do nothing				
				}
			}else if (StateRegular.sessionChair.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paperIndustry, paper)){
					state = StateRegular.paperIndustry;
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);
					
					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));

				}
			}else if (StateRegular.paperIndustry.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paperIndustry, paper)){
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);
					
					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));

				}else if (tryNextState (lineData, StateRegular.sessionTimeTitleIndustry, paperNew)){
					state = StateRegular.sessionTimeTitleIndustry;
					paper = paperNew;					

					indexSessionInTrack++;
					indexPaperInSession =0;
				}
			}
		}
		
		return listPaper;
	}
		

	private static List<TreeMap<EnumPaper,String>> parseHtmlPd(File f) throws Sw4jException{
		String content = ToolIO.pipeFileToString(f);
			
		//parse html
		ToolHtmlParser parser = new ToolHtmlParser();		
		parser.initTag(StateRegular.groupH3.tag);				
		parser.initTag(StateRegular.paper.tag);		
		parser.run(content, StateRegular.groupH3.tag);

		//extact results
		StateRegular state = StateRegular.ready;
		int indexSessionInTrack=0;
		int indexPaperInSession=0;
		int indexPaperInTrack =0;

		List<TreeMap<EnumPaper,String>> listPaper = new ArrayList<TreeMap<EnumPaper,String>>();
		TreeMap<EnumPaper,String> paper = null; 
		for (String line: parser.getResult()){
			String [] lineData = ToolHtmlParser.parseLine(line);
			
			//skip special line
			
			if (StateRegular.ready.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.groupH3, paperNew )){
					state = StateRegular.groupH3;
					paper = paperNew;
					
					indexSessionInTrack++;
					indexPaperInSession =0;

				}
			}else if (StateRegular.groupH3.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paper, paper)){
					state = StateRegular.paper;
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);
					

					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));

				}
			}else if (StateRegular.paper.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paper, paper)){
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);
					

					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));

				}else if (tryNextState (lineData, StateRegular.groupH3, paperNew)){
					state = StateRegular.groupH3;
					paper = paperNew;	
					
					indexSessionInTrack++;
					indexPaperInSession =0;

				}
			}
		}
		
		
		return listPaper;
	}
		

	private static List<TreeMap<EnumPaper,String>> parseHtmlSwc(File f) throws Sw4jException{
		String content = ToolIO.pipeFileToString(f);
			
		//parse html
		ToolHtmlParser parser = new ToolHtmlParser();		
		parser.initTag(StateRegular.groupH2.tag);				
		parser.initTag(StateRegular.paperSwc.tag);		
		parser.run(content, StateRegular.groupH2.tag);

		//extact results
		StateRegular state = StateRegular.ready;
		int indexSessionInTrack=0;
		int indexPaperInSession=0;
		int indexPaperInTrack =0;

		List<TreeMap<EnumPaper,String>> listPaper = new ArrayList<TreeMap<EnumPaper,String>>();
		TreeMap<EnumPaper,String> paper = null; 
		for (String line: parser.getResult()){
			String [] lineData = ToolHtmlParser.parseLine(line);
			
			//skip special line
			if (line.indexOf("Submissions")>0)
				continue; //skip line
			
			if (StateRegular.ready.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.groupH2, paperNew )){
					state = StateRegular.groupH2;
					paper = paperNew;
					
					indexSessionInTrack++;
					indexPaperInSession =0;

				}
			}else if (StateRegular.groupH2.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paperSwc, paper)){
					state = StateRegular.paperSwc;
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);
					

					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));

				}
			}else if (StateRegular.paperSwc.equals(state)){
				TreeMap<EnumPaper,String> paperNew = new TreeMap<EnumPaper,String>();
				if (tryNextState (lineData, StateRegular.paperSwc, paper)){
					paperNew = new TreeMap<EnumPaper,String>();
					paperNew.putAll(paper);
					listPaper.add(paperNew);
					

					indexPaperInSession++;
					indexPaperInTrack++;
					paperNew.put(EnumPaper.paperIndexInTrack, formatIndex(indexPaperInTrack));
					paperNew.put(EnumPaper.paperIndexInSession, formatIndex(indexPaperInSession));
					paperNew.put(EnumPaper.sessionIndexInTrack, formatIndex(indexSessionInTrack));

				}else if (tryNextState (lineData, StateRegular.groupH2, paperNew)){
					state = StateRegular.groupH2;
					paper = paperNew;	
					
					indexSessionInTrack++;
					indexPaperInSession =0;

				}
			}
		}
		
		return listPaper;
	}	
	private static boolean tryNextState(String [] lineData, StateRegular stateNext, TreeMap<EnumPaper,String> paper){
		if (stateNext.tag.equals(lineData[ToolHtmlParser.IDX_TAG])){
			String fragment = lineData[ToolHtmlParser.IDX_FRAGMENT];
			logV("[FRAGMENT]"+fragment);			
			String temp = fragment;
			
			if (StateRegular.sessionTimeTitle.equals(stateNext)){
				temp = filterSessionTitle(temp);
				logV(temp);

				//update paper
				String [] aryTemp = temp.split("\\|");
				if (aryTemp.length==4){
					int indexTemp =0;
					paper.put(EnumPaper.sessionTimeStart, aryTemp[indexTemp]);
					indexTemp++;
					paper.put(EnumPaper.sessionTimeEnd, aryTemp[indexTemp]);
					indexTemp++;
					paper.put(EnumPaper.sessionTitle, aryTemp[indexTemp]);
					indexTemp++;
					paper.put(EnumPaper.sessionRoom, aryTemp[indexTemp]);
					indexTemp++;					
					return true;
				}else if (aryTemp.length==3){
					int indexTemp =0;
					paper.put(EnumPaper.sessionTimeStart, aryTemp[indexTemp]);	
					indexTemp++;
					
					int indexSeparator = aryTemp[indexTemp].indexOf(" ");
					paper.put(EnumPaper.sessionTimeEnd, aryTemp[indexTemp].substring(0, indexSeparator));
					paper.put(EnumPaper.sessionTitle, aryTemp[indexTemp].substring(indexSeparator).trim());
					indexTemp++;
					
					paper.put(EnumPaper.sessionRoom, aryTemp[indexTemp]);
					return true;
					
				}else{
					for (char c :temp.toCharArray()){
						logV( (int)c+" - "+ c);
					}

					logV("Teminated due to mismatch -- " + stateNext);
					return false;
				}
			}else if (StateRegular.sessionTimeTitleIndustry.equals(stateNext)){
				temp = temp.replaceAll(": ","|");
				temp = filterSessionTitle(temp);
				logV(temp);

				//update paper
				String [] aryTemp = temp.split("\\|");
				if (aryTemp.length==4){
					int indexTemp =0;
					paper.put(EnumPaper.sessionTimeStart, aryTemp[indexTemp]);
					indexTemp++;
					paper.put(EnumPaper.sessionTimeEnd, aryTemp[indexTemp]);
					indexTemp++;
					paper.put(EnumPaper.sessionTitle, aryTemp[indexTemp]);
					indexTemp++;
					paper.put(EnumPaper.sessionRoom, aryTemp[indexTemp]);
					indexTemp++;					
					return true;
				}else{
					for (char c :temp.toCharArray()){
						logV( (int)c+" - "+ c);
					}

					logV("Teminated due to mismatch -- " + stateNext);
					return false;
				}
			}else if (StateRegular.sessionChair.equals(stateNext)){
				temp = temp.substring(temp.indexOf(":")+1);
				temp =temp.trim();
				logV(temp);

				//update paper
				String [] aryTemp = temp.split("\\|");
				if (aryTemp.length==1){
					int indexTemp =0;
					paper.put(EnumPaper.sessionChair, aryTemp[indexTemp]);
					indexTemp++;

					return true;
				}else{
					logV("Teminated due to mismatch -- " + stateNext);
					return false;
				}				
			}else if (StateRegular.paper.equals(stateNext)){
				temp = temp.replaceAll("[\\(\\)]+", "|");  //spotlight
				temp = filterPaper(temp);
				logV(temp);

				//skip special presentation
				if (temp.indexOf("Special presentation")>=0)
					return false;
				
				//update paper
				String [] aryTemp = temp.split("\\|");
				if (aryTemp.length==3 ){
					int indexTemp =0;
					paper.put(EnumPaper.paperPdfLink, cleanUrl(aryTemp[indexTemp]));
					indexTemp++;
					paper.put(EnumPaper.paperTitle, cleanTitle(aryTemp[indexTemp]));
					indexTemp++;
					paper.put(EnumPaper.paperAuthorList, cleanAuthor(aryTemp[indexTemp]));
					indexTemp++;
					
					paper.put(EnumPaper.paperSpotlight, "");
					return true;
				}else if (aryTemp.length==4 ){
					int indexTemp =0;
					paper.put(EnumPaper.paperPdfLink, cleanUrl(aryTemp[indexTemp]));
					indexTemp++;
					paper.put(EnumPaper.paperTitle, cleanTitle(aryTemp[indexTemp]));
					indexTemp++;
					paper.put(EnumPaper.paperSpotlight, aryTemp[indexTemp]);
					indexTemp++;
					paper.put(EnumPaper.paperAuthorList, cleanAuthor(aryTemp[indexTemp]));
					indexTemp++;
					
					return true;
				}else{
					for (char c :temp.toCharArray()){
						logV( (int)c+" - "+ c);
					}

					logV("Teminated due to mismatch -- " + stateNext);
					return false;
				}				
			}else if (StateRegular.paperIndustry.equals(stateNext)){
				temp = temp.replaceAll("[\\(\\)]+", "|");
				temp = filterPaper(temp);
				logV(temp);
				
				//update paper
				String [] aryTemp = temp.split("\\|");
				if (aryTemp.length==3 ){
					int indexTemp =0;
					paper.put(EnumPaper.paperTitle, cleanTitle(aryTemp[indexTemp]));
					indexTemp++;
					paper.put(EnumPaper.paperAuthorList, cleanAuthor(aryTemp[indexTemp]));
					indexTemp++;
					paper.put(EnumPaper.paperAuthorAffiliation, aryTemp[indexTemp]);
					indexTemp++;
					
					return true;
				}else{
					for (char c :temp.toCharArray()){
						logV( (int)c+" - "+ c);
					}

					logV("Teminated due to mismatch -- " + stateNext);
					return false;
				}				
			}else if (StateRegular.groupH3.equals(stateNext) || StateRegular.groupH2.equals(stateNext)){
				temp =filterSessionTitle(temp);
				logV(temp);

				//update paper
				String [] aryTemp = temp.split("\\|");
				if (aryTemp.length==1){
					int indexTemp =0;
					paper.put(EnumPaper.group, aryTemp[indexTemp]);
					indexTemp++;
					

					return true;
				}else{
					logV("Teminated due to mismatch -- " + stateNext);
					return false;
				}				

			}else if (StateRegular.paperDc.equals(stateNext)){
				temp = filterPaperDc(temp);
				logV(temp);

				//update paper
				String [] aryTemp = temp.split("\\|");
				if (aryTemp.length==2){
					int indexTemp =0;
					paper.put(EnumPaper.paperAuthorList, cleanAuthor(aryTemp[indexTemp]));
					indexTemp++;
					paper.put(EnumPaper.paperTitle, cleanTitle(aryTemp[indexTemp]));
					indexTemp++;

					return true;
				}else{
					logV("Teminated due to mismatch -- " + stateNext);
					return false;
				}				

			}else if (StateRegular.paperSwc.equals(stateNext)){
				temp = filterPaper(temp);
				logV(temp);

				//update paper
				String [] aryTemp = temp.split("\\|");
				if (aryTemp.length >= 5){
					paper.put(EnumPaper.paperDemoLink, cleanUrl(aryTemp[0]));
					paper.put(EnumPaper.paperAuthorList, cleanAuthor(aryTemp[aryTemp.length-3]));
					paper.put(EnumPaper.paperPdfLink, cleanUrl(aryTemp[aryTemp.length-2]));
					paper.put(EnumPaper.paperTitle, cleanTitle(aryTemp[aryTemp.length-1]));

					return true;
				}else{
					logV("Teminated due to mismatch -- " + stateNext);
					return false;
				}				
				
			}
			
			
		}
	//	logV("Teminated due to failed matching -- " + stateNext);
		return false;
		
	}
	
	private static String filterSessionTitle(String line){
		String temp = line;
		temp = temp.replaceAll("-", "|");
		temp = temp.replaceAll("[\\(\\)]+", "|");
		temp = filterPaper(temp);
		return temp;		
	}
	
	private static String filterPaperDc(String line){
		String temp = line;
		temp = temp.replaceAll("\\.", "|");
		temp = filterPaper(temp);
		return temp;		
	}

	private static String filterPaper(String line){
		String temp = line;
		temp = temp.replaceAll("[\\s\\xA0]+", " ");
		temp = temp.replaceAll("<a href=\"", "|");
		temp = temp.replaceAll("<a href='", "|");
		temp = temp.replaceAll("\">", "|");
		temp = temp.replaceAll("'>", "|");
		temp = temp.replaceAll("<[^>]+>", "|");
		temp = temp.replaceAll("[\\|\\s]*\\|[\\|\\s]*", "|");
		temp = temp.replaceAll("\\|.\\|", "|");
		temp = temp.trim();
		temp = temp.replaceAll("\\|$", "");
		temp = temp.replaceAll("^\\|", "");
		temp = temp.trim();
		return temp;
	}
	
	private static String cleanUrl(String url){
		if (null==url || url.length()==0)
			return null;
		
		String ret =url;
		int index = url.indexOf("#");
		if (index>=0){
			ret = url.substring(0, index);
		}
		
		if (ret.startsWith("/sites")){
			ret = "http://iswc2012.semanticweb.org"+ret;
		}else if (ret.startsWith("swc2012_")){
			ret = "http://challenge.semanticweb.org/2012/submissions/"+ret;
		}

		if (!ret.startsWith("http")){
			logInfo("wrong url");
			System.exit(0);
		}
		return ret;
	}

	
	public static String cleanTitle(String title){
		String temp = title;
		temp = temp.replace("’", "'");
		temp = temp.replace("–", "-");
		return temp;
	}
	
	public static String cleanAuthor(String authorList){
		String temp = authorList;
		temp = temp.replaceAll("\\s*,\\s*and", " and");
		temp = temp.replaceAll(",$", "");
		String [] authors = authorList.split("\\s*,\\s*");
		if (authors.length>1){
			if (authors[authors.length-1].indexOf(" and ")<0){
				temp = temp.replaceAll("\\s*,\\s*"+authors[authors.length-1], " and "+authors[authors.length-1]);
			}
		}

		return temp;
	}
	
}
