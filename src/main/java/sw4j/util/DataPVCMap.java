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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *  extends AbstractPropertyValuesMap by adding the following features
 * - It offers a boolean "unique" option, i.e. allow duplicate (property, value) pairs.
 * - it counts the frequency of entry i.e. <p,v>
 * - slow
 *
 * Example:
 *    (p1, List(v11,v12))  
 *    (p2, List(v21)) 
 *    and 
 *    (p1, v11) = count 1
 *    (p1, v12) = count 3
 *    
 * @author Li Ding
 * 
 */
public class DataPVCMap <P, V> extends AbstractPropertyValuesMap<P,V>{
	////////////////////////////////////////////////
	// internal data
	////////////////////////////////////////////////

	/**
	 * raw data (property,value) => count
	 */ 
	DataObjectCounter<Entry> m_counter = new DataObjectCounter<Entry>();
	
	/** 
	 * index property => values
	 */
	HashMap<P, List<V>> m_index = new HashMap<P, List<V>>();

	/**
	 * unique option, allow a property's value list containing duplicated values
	 */
	private boolean m_bUnique = true;

	////////////////////////////////////////////////
	// constructor
	////////////////////////////////////////////////
	public DataPVCMap(){
		this(true);
	}

	public DataPVCMap(boolean unique){
		m_bUnique= unique;
	}
	


	////////////////////////////////////////////////
	// embedded data structure
	////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	public class Entry implements Comparable{
		public P property=null;
		public V value=null;
		
		public Entry(P p, V v){
			property =p;
			value =v;
		}
		
		@Override
		public String toString(){
			return property.toString()+"::"+value.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((property == null) ? 0 : property.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Entry other = (Entry) obj;
			if (property == null) {
				if (other.property != null)
					return false;
			} else if (!property.equals(other.property))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		public int compareTo(Object o) {
			if (null==o)
				return -1;
			return this.toString().compareTo(o.toString());
		}

	}
		
	////////////////////////////////////////////////
	// functions
	////////////////////////////////////////////////
	/**
	 * if this data structure is set unique.
	 */
	public boolean isUnique(){
		return m_bUnique;
	}
	
	/**
	 * add a (property, value) pair
	 * 
	 * @param property
	 * @param value
	 */
	public void add(P property, V value) {
		//find existing entry
		Entry entry = new Entry(property, value);
		
		// count p,v pair
		int count =	m_counter.getCount(entry);
		
		//if the value already exists, then do not add it to the property's value list
		if (m_bUnique && count>=1)
			return;
		m_counter.count(entry);
		
		//update index
		List<V> values = m_index.get(property);
		if (null == values) {
			values = new ArrayList<V>();
			m_index.put(property, values);
		}
		values.add(value);
		
	}

	/**
	 * get values of a property as a list, the list may contain duplicates
	 * 
	 * @param property
	 * @return
	 */
	public Collection<V> getValues(P property) {
		Collection<V> values = m_index.get(property);
		if (null==values)
			return new ArrayList<V>();
		else
			return values;
	}


	/**
	 * list all properties
	 * @return
	 */
	public Set<P> keySet() {
		return m_index.keySet();
	}


	/**
	 * get the number of duplicated (property,value) pair
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public int getEntryCount(P property, V value) {
		Entry entry = new Entry(property, value);
		return m_counter.getCount(entry);
	}
	
	public DataObjectCounter<Entry> getEntryCounter() {
		return m_counter;
	}
	
	/**
	 * remove a property
	 * 
	 * @param property
	 */
	public void remove(P property) {
		// remove index entries
		m_index.remove(property);
		
		// remove data entries
		Iterator<Map.Entry<Entry, Integer>> iter= this.m_counter.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<Entry,Integer> entry = iter.next();
			if (entry.getKey().property.equals(property))
				iter.remove();
		}
	}
	

	/**
	 * print an alphabetical ordered list of (property, value) pairs with their count
	 */
	@Override
	public String toString() {
		if (null==this.m_counter)
			return "";
		else
			return m_counter.getSortedDataByKey().toString();//ToolSafe.printMapToString(this.m_counter);
	}


	public void clear() {
		this.m_counter.clear();
		this.m_index.clear();
	}


	
}
