package org.iswc.iswc2012main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.iswc.iswc2012main.Config.FILE;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;

public class TaskDownloadHtml {

	
	public static void main(String[] args) {
		run();
	}

		
	public static void download(FILE file) throws Sw4jException{
		download(file, null);
	}
	public static void download(FILE file, String url) throws Sw4jException{
		if (null==url)
			url = String.format("http://iswc2012.semanticweb.org/%s", file.filename);
//		String f = file.getFile();
//		String content = ToolIO.pipeUrlToString(url);
//		System.out.println(content);
		ToolIO.pipeUrlToFile(url, file.getFile().getAbsolutePath(), false, true);
	}
	
	public static void run(){
		try {
	
			download(Config.FILE.html_research);
			download(Config.FILE.html_inuse);
			download(Config.FILE.html_evaluation);
			download(Config.FILE.html_doctoral_consortium);
			download(Config.FILE.html_poster_demo);
			download(Config.FILE.html_industry);
			download(Config.FILE.html_semantic_web_challenge, "http://challenge.semanticweb.org/2012/submissions/");

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
	
}
