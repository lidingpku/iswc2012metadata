package org.iswc.iswc2012main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.iswc.iswc2012main.Config.EXT;
import org.iswc.iswc2012main.Config.FILE;
import org.iswc.util.AgentSparql;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;



import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TaskRunSparqlQuery {

	public static void main(String[] args){
		try {
			runDataset( FILE.iswc2012_complete  );
//			runDataset( FILE.iswc2010_complete  );
//			runDataset( FILE.iswc2011_complete  );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void runDataset(FILE fileDataset) throws FileNotFoundException, Sw4jException{
		
		File fData = fileDataset.getFile();
		
		Model m = ModelFactory.createDefaultModel();
		{
			
			if (!fData.exists()){
				System.out.println("not found "+fData.getAbsolutePath());			
			}
			
			m.read(ToolIO.prepareFileInputStream(fData), Config.RDFSYNTAX.RDFXML.getValue());	
			System.out.println(m.size());	

			System.out.println(	m.getNsPrefixMap() );

		}

		
		for (FILE fileQuery: FILE.values()){
			if (!EXT.sparql.equals(fileQuery.ext))
				continue;
			
			File fQuery = fileQuery.getFile();
			System.out.println("processing query "+fQuery.getAbsolutePath());
			
			String output = runSelect(m, fQuery,Config.RDFSYNTAX.CSV);
			
			File fOutput = new File( Config.PATH.data_queryoutput.getFile(), fileDataset.name()+"-"+ fileQuery.name()+"."+EXT.csv);
			ToolIO.pipeStringToFile(output, fOutput, false, false);
		}
	}
	
	
	public static String runSelect(Model m, File fQuery, Config.RDFSYNTAX syntax) throws FileNotFoundException, Sw4jException{

		
		String sz_sparql_query = "";		
		{
				sz_sparql_query = ToolIO.pipeFileToString(fQuery);
				System.out.println(sz_sparql_query);
		}


		
		
		AgentSparql sparql = new  AgentSparql();
		Dataset dataset = DatasetFactory.create(m);
		Object ret = sparql.exec(sz_sparql_query, dataset, syntax);
		String result = ret.toString();
		
		Map<String,String > mapPrefixNs = m.getNsPrefixMap();
		for (String prefix: mapPrefixNs.keySet()){
			String uri = mapPrefixNs.get(prefix);
			if (null == prefix || prefix.length()==0)
				continue;
				
			result = result.replaceAll(uri, prefix+":");
		}
//		result = result.replaceAll("http://data.semanticweb.org/person/", "dogfood_person:");
//		result = result.replaceAll("http://data.semanticweb.org/organization/", "dogfood_org:");

		
		System.out.println(result);
		
		return result;
	}
}
