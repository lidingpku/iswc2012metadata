package org.iswc.util;

import java.text.Normalizer;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

public class ToolText2Rdf {
	public static String extractLocalName(String label){
		String localName = label.trim();
		localName = localName.replaceAll("[\\.\\s &:,\\(\\)-]+", "-");
		localName = localName.replace("'", "");
//		String localName = label.replaceAll("[^a-zA-Z0-9]+", "-");
		localName = localName.toLowerCase();
		localName = removeDiacritics(localName);
		
		if (localName.indexOf(" ")>=0)
			System.out.println("label");
		return localName;
	}
	
	
	  public static String removeDiacritics(String input)
	    {
	        String nrml = Normalizer.normalize(input, Normalizer.Form.NFD);
	        StringBuilder stripped = new StringBuilder();
	        for (int i=0;i<nrml.length();++i)
	        {
	            if (Character.getType(nrml.charAt(i)) != Character.NON_SPACING_MARK)
	            {
	                stripped.append(nrml.charAt(i));
	            }
	        }
	        return stripped.toString();
	    }

}
