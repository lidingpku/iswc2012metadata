package org.iswc.iswc2012main.dev;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Logger;

import sw4j.util.DataSmartMap;

import com.csvreader.CsvReader;

public class ToolCsvLoader {
	public static void main(String[] args){
		test();
	}
	
	protected static void test(){
		String csvFile = "local/logd/data/us-federal-agency-dbpedia.csv";
		ToolCsvLoader loader = new ToolCsvLoader();
		try {
			int cnt = loader.loadCsvFile(csvFile);
			System.out.println(cnt);
			System.out.println(loader.m_data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	HashMap<String,String> m_config = new HashMap<String,String>();
	public static String CONFIG_COLUMN_KEY = "column_key";
	
	HashMap<String,DataSmartMap> m_data= new HashMap<String,DataSmartMap>();

	HashMap<String,String> m_metadata= new HashMap<String,String>();
	
	public int loadCsvUrl(String szCsvUrl) throws IOException {
		URL url = new URL(szCsvUrl);
		getLogger().info("loading csv url ... "+ url.toString());
		m_metadata.put("source", url.toString());
		return loadCsv(new CsvReader(new InputStreamReader(url.openStream())));
	}
	
	
	public int loadCsvFile(String szCsvFilename) throws IOException {
		File file_input = new File(szCsvFilename);
		return loadCsvFile(file_input);
	}
	
	public int loadCsvFile(File file_input) throws IOException {
		getLogger().info("loading csv file ... "+ file_input.getAbsolutePath());
		return loadCsv(new CsvReader(new FileReader(file_input)));
	}
	
	public int loadCsvString(String szCsvData) throws IOException {	
		return loadCsv(new CsvReader(new StringReader(szCsvData)));
	}
	
	
	private Logger getLogger(){
		return Logger.getLogger(this.getClass());
	}
	
	
	public int loadCsv(CsvReader csv) throws IOException {	
		int row_number=1;

		////////////////////////////
		//read header - we require all csv files have header
		csv.readHeaders();
		row_number++;

		//an array of of hasmaps that have a number as a Key(coloum position) and Property as a Value
		//each Hashmap for each input csv file in the dataset
		String[]  ary_prop = new String[csv.getHeaderCount()];
		for(int i = 0;i<csv.getHeaderCount();i++)
		{
			String name = csv.getHeader(i).trim();
					
			//save the property
			ary_prop[i] =name;
		}	

		////////////////////////////
		//read data
		int entry_number=0;
		while ((csv.readRecord()))
		{
			row_number++;


			//exit if the header count is mismatched
			if (csv.getHeaderCount()<csv.getColumnCount()){
				getLogger().fatal("too many columns: found (" +csv.getColumnCount()+") but the header row has ("+csv.getHeaderCount()+")");
				System.out.println("at row_number "+ row_number + " with current record "+ csv.getCurrentRecord());
				System.out.println("with value "+ csv.getRawRecord());
				System.exit(-1);
			}
			if (csv.getHeaderCount()>csv.getColumnCount()){
				getLogger().info("too few columns: found (" +csv.getColumnCount()+") but the header row has ("+csv.getHeaderCount()+")");
				System.out.println("at row_number "+ row_number + " with current record "+ csv.getCurrentRecord());
				System.out.println("with value "+ csv.getRawRecord());
			}

			//create entry id
			String entryid= String.format("thing_%05d",entry_number);
			entry_number++;

			String szColumnKey = this.m_config.get(CONFIG_COLUMN_KEY);
			if (null!=szColumnKey){
				for(int i = 0;i<csv.getColumnCount();i++){
					if (ary_prop[i].equals(szColumnKey))
						entryid = csv.get(i).trim();
				}
			}
			
			
			DataSmartMap row = new DataSmartMap();
			
			//convert all data
			for(int i = 0;i<csv.getColumnCount();i++)
			{
				row.put( ary_prop[i], csv.get(i));
			}  
			
			m_data.put(entryid, row);
		}
		

		return m_data.size();
	}	

}
