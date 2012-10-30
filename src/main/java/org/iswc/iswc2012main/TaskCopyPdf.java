package org.iswc.iswc2012main;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

import org.iswc.util.DataKeyKeyValue;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.ToolString;

public class TaskCopyPdf {

	public static void main(String[] args) throws Sw4jException{
		DataKeyKeyValue<String, String,String> mapPv = new DataKeyKeyValue<String, String,String>();

		File fRootTex = new File ("local/iswc2012full");
		File fRootPdf = new File ("local/iswc2012pdf");
		for (File fPaper:fRootTex.listFiles()){			
			for (File f: fPaper.listFiles()){

				if (!f.getAbsolutePath().endsWith(".pdf")){
					continue;
				}
				
				
				String idFolder = f.getParentFile().getName();
				String fileName = f.getName();
				if (!fileName.startsWith("76")){
					continue;
				}
				File fNew = new File(fRootPdf, idFolder+".pdf");
				ToolIO.pipeFileToFile(f, fNew);
				mapPv.set(idFolder, fileName, fileName);
				System.out.println(String.format("copied from %s to %s.pdf", fileName, idFolder));
			}
		}
		
		//print(mapPv.report(false));		
	}

	private static void print(List<String> data){
		for (String line: data){
			System.out.println(line);
		}
		System.out.println(data.size());
	}
	 
	
}
