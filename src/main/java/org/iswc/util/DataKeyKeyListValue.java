package org.iswc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class DataKeyKeyListValue<KEY,SUBKEY,VALUE> {

	TreeMap<KEY, TreeMap<SUBKEY, List<VALUE>>> data = new TreeMap<KEY, TreeMap<SUBKEY, List<VALUE>>>();
	
	public void add(KEY key, SUBKEY subkey, VALUE value){
		TreeMap<SUBKEY, List<VALUE>> map = data.get(key);
		if (null==map){
			map = new TreeMap<SUBKEY, List<VALUE>>();
			data.put(key, map);			
		}
		
		List<VALUE> list = map.get(subkey);		
		if (null==list){
			list = new ArrayList<VALUE>();
			map.put(subkey, list);			
		}
		
		list.add(value);		
	}

	public Set<KEY> keySet(){
		return this.data.keySet();
	}

	public List<String> report(boolean bUseSubKey){
		List<String> ret = new ArrayList<String>();
		for (KEY key: data.keySet()){
			String line = key.toString()+":\t";
			TreeMap<SUBKEY, List<VALUE>> map = data.get(key);
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

	public Map<SUBKEY, List<VALUE>> get(String track) {
		return this.data.get(track);
	}

	
}
