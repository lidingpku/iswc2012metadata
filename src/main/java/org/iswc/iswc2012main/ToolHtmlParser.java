package org.iswc.iswc2012main;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class ToolHtmlParser {

	private String content =null;
	private int index = -1;
	private List<String> result = new ArrayList<String>();
	private List<String> listTag = new ArrayList<String>();

	
	


	public List<String> getResult() {
		return result;
	}

	public void initTag(String tag){
		this.listTag.add(tag);	
	}

	public void run(String content, String tagFirst){
		this.content = content;
		this.index = 0;
		
		String tagNext = tagFirst;
		while (null!=tagNext){
			String fragment = extract(tagNext);
			if (null==fragment)
				break;
			tagNext = lookupNextTag();
		}		
	}

	private String lookupNextTag(){
		String markupBegin = "<";
		String markupEnd= ">";
		while (index>=0){
			int indexSearchBegin = content.indexOf(markupBegin, index);
			int indexSearchEnd = content.indexOf(markupEnd, indexSearchBegin);
			if (indexSearchBegin>=0 && indexSearchEnd>=0){
				String szTemp = content.substring(indexSearchBegin, indexSearchEnd+markupEnd.length());
				for (String tag : this.listTag){
					String pattern = "<"+tag+"[\\s>]";
					if (szTemp.matches(pattern)){
						return tag;
					}
				}
				//advance index
				index = indexSearchEnd +1;
			}else{
				index = -1; //no more tag
			}
		}

		return null;		
	}
	
	private String extract(String nextTag){
		String markupBegin = String.format("<%s", nextTag);
		String markupEnd= String.format("</%s>", nextTag);
		int indexSearchBegin = content.indexOf(markupBegin, index);
//		String temp = content.substring(index);
		
		int indexSearchEnd = -1;
		if (indexSearchBegin>=0){
			indexSearchEnd = content.indexOf(markupEnd, index);
			if (indexSearchEnd>=0){
				String fragment = content.substring(indexSearchBegin, indexSearchEnd);

				//update
				this.index = indexSearchEnd+ markupEnd.length();
				this.result.add(String.format("%s%s%03d%s%s",nextTag, SEPARATOR, this.result.size(), SEPARATOR,  fragment));
				return fragment;
			}
		}
	
		return null;
	}
	
	public static final String SEPARATOR = "----";
	public static final int IDX_TAG =0;
	public static final int IDX_LINE =1;
	public static final int IDX_FRAGMENT =2;
	
	public static String[] parseLine(String line){
		return line.split(SEPARATOR);
	}
}
