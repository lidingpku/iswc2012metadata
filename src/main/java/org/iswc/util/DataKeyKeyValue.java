package org.iswc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class DataKeyKeyValue<KEY,SUBKEY,VALUE> {

	TreeMap<KEY, TreeMap<SUBKEY, VALUE>> data = new TreeMap<KEY, TreeMap<SUBKEY, VALUE>>();
	
	public void set(KEY key, SUBKEY subkey, VALUE value){
		TreeMap<SUBKEY, VALUE> map = data.get(key);
		if (null==map){
			map = new TreeMap<SUBKEY, VALUE>();
			data.put(key, map);
		}
		map.put(subkey, value);		
	}

	public void init(KEY key, SUBKEY subkey, VALUE value) {
		TreeMap<SUBKEY, VALUE> map = data.get(key);
		if (null==map){
			map = new TreeMap<SUBKEY, VALUE>();
			data.put(key, map);
		}
		if (null== map.get(subkey)){
			map.put(subkey, value);	
		}

	}
	public List<String> report(boolean bUseSubKey){
		List<String> ret = new ArrayList<String>();
		for (KEY key: data.keySet()){
			String line = key.toString()+":\t";
			TreeMap<SUBKEY, VALUE> map = data.get(key);
			for (SUBKEY subkey: map.keySet()){
				if (bUseSubKey)
					line += String.format("%s=%s\t", subkey, map.get(subkey));
				else
					line += String.format("%s\t",  map.get(subkey));
			}
			ret.add(line);
		}
		return ret;
	}

	
}
