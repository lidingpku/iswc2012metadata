package org.iswc.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.iswc.iswc2012main.Config;
import org.iswc.iswc2012main.Config.RDFSYNTAX;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class AgentSparql {

	
	protected int inf_option=0;
	protected Query query;
	protected Dataset dataset;
	protected QueryExecution qexec;
	protected Syntax defaultQuerySyntax= Syntax.syntaxARQ;
	
	public Object exec(String queryString, Config.RDFSYNTAX rdfSyntax){
		this.query = QueryFactory.create(queryString, defaultQuerySyntax) ;
		
		Dataset dataset = DatasetFactory.create(query.getGraphURIs(), query.getNamedGraphURIs());
		return exec(queryString, dataset, rdfSyntax);
	}
	
	public Object exec(String queryString, Dataset dataset, Config.RDFSYNTAX rdfSyntax){
		this.query = QueryFactory.create(queryString, defaultQuerySyntax) ;
		
		this.dataset = dataset;
		run_sparql();
		
		Object ret = gerRet(rdfSyntax);
		
		this.qexec.close() ;
		return ret;
	}
	
	protected void run_sparql(){
		qexec = QueryExecutionFactory.create(query, dataset);
	}
	
	private Object gerRet(Config.RDFSYNTAX rdfSyntax){
		Object ret = null;
		if (query.isDescribeType()){
			ret = qexec.execDescribe();
		}else if (query.isConstructType()){
			ret = qexec.execConstruct() ;
		}else if (query.isSelectType()){
			ResultSet results = qexec.execSelect() ;
		
			//System.out.println(ResultSetFormatter.asText(results));				

			ByteArrayOutputStream sw = new ByteArrayOutputStream();
			if (Config.RDFSYNTAX.SPARQL_XML.equals(rdfSyntax)){
				ResultSetFormatter.outputAsXML(sw, results);
			}else if (Config.RDFSYNTAX.SPARQL_JSON.equals(rdfSyntax)){
				ResultSetFormatter.outputAsJSON(sw, results);
			}else if (Config.RDFSYNTAX.CSV.equals(rdfSyntax)){
				ResultSetFormatter.outputAsCSV(sw, results);
			}else if (Config.RDFSYNTAX.TSV.equals(rdfSyntax)){
				ResultSetFormatter.outputAsTSV(sw, results);
			}else if (Config.RDFSYNTAX.SPARQL_TXT.equals(rdfSyntax)){
				return ResultSetFormatter.asText(results);
			}else{
				ResultSetFormatter.out(sw,results, query);				
			}

			try {

				ret = sw.toString("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
		}else if (query.isAskType()){
			ret = qexec.execAsk() ;
		}
		
		return ret;
	}
}
