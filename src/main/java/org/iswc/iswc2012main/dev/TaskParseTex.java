package org.iswc.iswc2012main.dev;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

import org.iswc.util.DataKeyKeyValue;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.ToolString;

public class TaskParseTex {

	public static void main(String[] args) throws Sw4jException{
		DataKeyKeyValue<String, TexProp,String> mapPv = new DataKeyKeyValue<String, TexProp,String>();

		File fRoot = new File ("local/tex");
		for (File fPaper:fRoot.listFiles()){			
			for (File f: fPaper.listFiles()){
				processTex(f, mapPv);
			}
		}
		
		print(mapPv.report(false));		
	}
	
	enum TexProp{
		id,
		file,
		title,
		author,
		keywords,
		_abstract,
	}
	
	public static void processTex(File f, DataKeyKeyValue<String, TexProp,String> mapPv){
		String key = f.getParentFile().getName();
		update(mapPv, key, TexProp.id, key);

		if (!f.getAbsolutePath().endsWith(".tex")){
			return;
		}
//	System.out.println("processing tex file"+f.getAbsolutePath());
		String szText;
		try {
			szText = ToolIO.pipeFileToString(f);
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		
		{
			String temp = extract(szText, "title{", "}");
			update(mapPv, key, TexProp.title, temp);
		}
		{
			String temp = extract(szText, "keywords{", "}");
			update(mapPv, key, TexProp.keywords, temp);
			
		}
		{
			String temp = extract(szText, "begin{abstract}", "\\end{abstract}");
			update(mapPv, key, TexProp._abstract, temp);			
		}
		
		
	}
	
	private static void update(DataKeyKeyValue<String, TexProp, String> mapPv,
			String key, TexProp subkey, String value) {
		mapPv.init(key, subkey, "");
		if (null!=value){
			mapPv.set(key,subkey, value);
		}
		
	}

	public static String extract(String szText, String szBegin, String szEnd){
		String temp = ToolString.section_extract(szText, szBegin, szEnd);
		if (null==temp)
			return null;
		
		temp = temp.substring(szBegin.length(), temp.length()-szEnd.length());
		temp = temp.replaceAll("%.*", "");
		temp = temp.replaceAll("\\\\\\s*", " ");
		temp = temp.replaceAll("\\s+", " ");
		temp = temp.replaceAll("\\\\\\w+\\{[^\\}]+[\\}]?", "");
		temp = temp.replaceAll("\\\\\\thanks\\{.+", "");
		temp = temp.trim();
		return temp;		
	}
	
	private static void print(List<String> data){
		for (String line: data){
			System.out.println(line);
		}
	}
	 
	
}
