package org.iswc.iswc2012main.dev;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;
import org.iswc.iswc2012main.DataPaperInPdf;
import org.iswc.iswc2012main.DataPaperInPdf.STATE;
import org.iswc.util.DataKeyKeyValue;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.ToolString;

public class TaskParsePdf {

	public static void main(String[] args) throws Sw4jException{
		DataKeyKeyValue<String, String,String> mapPv = new DataKeyKeyValue<String, String,String>();

		File fRootPdf = new File ("local/iswc2012pdf");
		for (File f:fRootPdf.listFiles()){			
			
			if (!f.getAbsolutePath().endsWith(".pdf")){
				continue;
			}
				
			System.out.println();
			System.out.println();
			System.out.println(f.getAbsolutePath());
			try {
				extractText(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//print(mapPv.report(false));		
	}
	enum PROP{
		lineSeparator,
		paragraphStart,
		title,
		author,
		_abstract,
		keywords,
	}
	
	
	
	
	private static void extractText(File f) throws IOException{
        PDDocument pddDocument=PDDocument.load(f);
        PDFTextStripper textStripper = new PDFTextStripper();
        String content = textStripper.getText(pddDocument);
        
        TreeMap<PROP, String> temp = new  TreeMap<PROP, String> ();
        temp.put(PROP.lineSeparator,  textStripper.getLineSeparator());
        temp.put(PROP.paragraphStart,  textStripper.getParagraphStart());
        
        System.out.println(temp);
        

        DataPaperInPdf parser = new DataPaperInPdf(f.getName());
        for (String line: content.split(temp.get(PROP.lineSeparator))){
        	parser.processLine(line);
        	
        	if (DataPaperInPdf.STATE.content.equals(parser.state)){
        		break;
        	}
        }
        
        parser.printReport();
        
        System.out.println("-----");
        //System.out.println(content.substring(0, 500));
        /*
        PDDocumentInformation info = pddDocument.getDocumentInformation();
        System.out.println( "Page Count=" + pddDocument.getNumberOfPages() );
        System.out.println( "Title=" + info.getTitle() );
        System.out.println( "Author=" + info.getAuthor() );
        System.out.println( "Subject=" + info.getSubject() );
        System.out.println( "Keywords=" + info.getKeywords() );
        System.out.println( "Creator=" + info.getCreator() );
        System.out.println( "Producer=" + info.getProducer() );
        System.out.println( "Creation Date=" + info.getCreationDate() );
        System.out.println( "Modification Date=" + info.getModificationDate());
        System.out.println( "Trapped=" + info.getTrapped() );   
        */   
        
	}

	private static void print(List<String> data){
		for (String line: data){
			System.out.println(line);
		}
		System.out.println(data.size());
	}
	 
	
}
