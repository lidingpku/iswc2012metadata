/**
MIT License

Copyright (c) 2009 

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
 */

package sw4j.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * a counter for objects
 * 
 * @author Li Ding
 * 
 */
public class DataObjectCounter<V> {
	
	HashMap<V,Integer> m_counter = new HashMap<V,Integer>();
	DataPVHMap<V,String> m_value_source= new DataPVHMap<V,String>();
	
	public int count(V object){
		return count(object,null);
	}

	public int count(V object, String source){
		m_value_source.add(object, source);
		return setCount(object,getCount(object)+1);
	}

	public int setCount(V object, int count){
		return setCount(object,count, null);
	}

	public int setCount(V object, int count, Set<String> sources){
		m_counter.put(object, count);
		m_value_source.add(object, sources);
		return count;
	}

	public Set<V> keySet(){
		return m_counter.keySet();
	}
	
	public Set<Map.Entry<V,Integer>> entrySet(){
		return m_counter.entrySet();
	}
	
	public Map<V,Integer> getData(){
		return m_counter;
	}
	
	public TreeMap<V,Integer> getSortedDataByKey(){
		return new TreeMap<V,Integer>(m_counter);
	}
	public TreeSet<DataObjectCounter<V>.Entry> getSortedDataByCount(){
		TreeSet<DataObjectCounter<V>.Entry> set_ret = new TreeSet<DataObjectCounter<V>.Entry>();
		for (Map.Entry<V, Integer> entry: this.m_counter.entrySet()){
			Entry x = new Entry();
			x.key = entry.getKey();
			x.count = entry.getValue();
			x.sources = this.m_value_source.getValuesAsSet(x.key);
			set_ret.add(x);
		}
		return set_ret;		
	}
	
	public class Entry implements Comparable<Entry>{
		public V key;
		public Integer count =0;
		public Set<String> sources;
		
		@Override
		public int compareTo(Entry arg0) {
			int ret = this.count.compareTo(arg0.count);
			if (ret==0)
				ret = this.key.toString().compareTo(arg0.key.toString());
			
			return -1 * ret;
		}
		
		@Override
		public String toString(){
			DataSmartMap data = new DataSmartMap();
			data.put("key", key);
			data.put("count", count);
			String temp = sources.toString();
			temp= temp.substring(1,temp.length()-1);			
			data.put("sources", temp);
			return data.toCSVrow();
		}
		
	}

	
	public int size(){
		return m_counter.size();
	}
	
	public int getCount(V object){
		Integer count = m_counter.get(object);
		if (null==count){
			count = 0;
		}
		return count;
	}
	
	public void clear(){
		this.m_counter.clear();
	}
	

}
