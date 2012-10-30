package org.iswc.iswc2012main.dev;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class MyConfig {

	public static File getDirBase(){
		return new File(System.getProperty("user.dir"));
	}
	
	public static File getFile(String[] div_path){
		return getFile(div_path[0],div_path[1]);
	}
	
	public static File getFile(String div, String filepath){
		File dir_div= new File(getDirBase(), div);
		File dir_ret = new File(dir_div, filepath);
		return dir_ret;
	}

	
	
	public static final String PATH_DIV_DATA = "local/logd/data";
	public static final String PATH_DIV_LOCAL = "local/logd";
	public static final String [] DIV_FILE_CONFIG = new String []{PATH_DIV_LOCAL, "config.txt"};
	public static final String[] DIV_FILE_LOG = new String[]{PATH_DIV_LOCAL, "log.txt"};
	

	
	public static String CONFIG_YAHOO_APPID="yahoo_appid";
	
	public static String getProperty(String property){
		Properties config = getConfigFile();
		if (null!=config){
			return config.getProperty(property);
		}else{
			return null;
		}
	}
	
	private static Properties gConfig = null;
	public static Properties getConfigFile(){
		if (null== gConfig){
			gConfig = new Properties();
			try {
				gConfig.load(new FileReader(getFile(DIV_FILE_CONFIG)));
			} catch (FileNotFoundException e) {
				gConfig =null;
				e.printStackTrace();
				System.exit(0);
			} catch (IOException e) {
				gConfig =null;
				e.printStackTrace();
				System.exit(0);
			}
		}
		return gConfig;
	}
	

}
