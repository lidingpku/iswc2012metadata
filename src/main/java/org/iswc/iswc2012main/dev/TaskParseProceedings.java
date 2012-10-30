package org.iswc.iswc2012main.dev;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.iswc.iswc2012main.Config;
import org.iswc.iswc2012main.Config.FILE;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.ToolString;

public class TaskParseProceedings {
	public static void main(String[] args) throws Sw4jException{
		File fData = Config.FILE.iswc2012_front.getFile();
		String content = ToolIO.pipeFileToString(fData);
		
		
		extractSpecialChars(content);
	}
	
 
	
	public static void extractSpecialChars(String content){
		TreeMap<String,String> mapCharEncoding= new TreeMap<String,String>();
//		mapCharEncoding.put("&#168;o", "�");
//		mapCharEncoding.put("&#180;","�");
//		mapCharEncoding.put("&#184;","�");
//		mapCharEncoding.put("&#305;","");
//		mapCharEncoding.put("&#710;","�");
//		mapCharEncoding.put("&#711;","");
		
//		mapCharEncoding.put("&amp;", "&");
		mapCharEncoding.put("&#8211;", "-");
		mapCharEncoding.put("&#8217;", "'");
		mapCharEncoding.put("&#8220;", "");
		mapCharEncoding.put("&#8221;", "");
		mapCharEncoding.put("&#65279;","");
		
		mapCharEncoding.put("&#64258;","fl");
		mapCharEncoding.put("&#64259;","ffl");
		mapCharEncoding.put("&#64257;","fi");
		mapCharEncoding.put("&#64256;","ff");
		
		for (String key: mapCharEncoding.keySet()){
			String replacee = mapCharEncoding.get(key);
			content = content.replaceAll(key, replacee);
		}
		
		int index =-1;
		TreeSet<String> setCharSpecial = new TreeSet<String>();
		TreeSet<String> setCharSpecialPlus = new TreeSet<String>();
		while (0<(index=content.indexOf("&",index+1))){
			int index_temp1 = content.indexOf(";",index)+1;
			int index_temp2 = content.indexOf(" ",index);
			int index_next =Math.min(index_temp1, index_temp2);
			if (index_next<=index)
				continue; // not found 
			
			if (index_next==index+1)
				continue; //length is 1
			
			String temp = content.substring(index, index_next);
			setCharSpecial.add(temp);
			
			if (index_next-index==6){
				String temp1 = content.substring(index, index_next+1);
				setCharSpecialPlus.add(temp1);
			}
		}
		
		for(String t: setCharSpecial){
			String unescaped = StringEscapeUtils.unescapeHtml4(t);

			System.out.println("["+t+"] => ["+unescaped+"]");
		}
		System.out.println(setCharSpecial.size());

		for(String t: setCharSpecialPlus){
			String unescaped = StringEscapeUtils.unescapeHtml4(t);

			System.out.println("["+t+"] => ["+unescaped+"]");
		}
		System.out.println(setCharSpecialPlus.size());
	}

	
}
