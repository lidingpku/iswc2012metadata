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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeSet;


/**
 *  implement AbstractPropertyValuesMap by adding the following features
 *  - require the value comparable
 *  - fast, unsorted, no cannonical form
 *  
 * @author Li Ding
 * 
 */
public class DataPVHMap <P, V> extends AbstractPropertyValuesMap<P,V>{
	HashMap<P, Set<V>> data = new HashMap<P, Set<V>>();

	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void add(P property, V value) {
		if (null==value)
			return;
		
		Set<V> values = this.data.get(property);
		if (null == values) {
			values = new HashSet<V>();
			this.data.put(property, values);
		}

		values.add(value);
	}

	@Override
	public void add(P property, Collection<V> vs) {
		if (null==vs)
			return;
		Set<V> values = this.data.get(property);
		if (null == values) {
			values = new HashSet<V>();
			this.data.put(property, values);
		}

		values.addAll(vs);
	}

	/**
	public void set(P property, V value) {
		Set<V> values = this.data.get(property);
		if (null == values) {
			values = new HashSet<V>();
		} else {
			values.clear();
		}

		values.add(value);
	}*/

	public void remove(P property) {
		this.data.remove(property);
	}
	
	@Override
	public Collection<V> getValues(P p) {
		Set<V> ret = this.data.get(p);
		if (null==ret)
			ret = new HashSet<V>();
		return ret;
	}


	
	public Set<Map.Entry<P, Set<V>>> entrySet() {
		return this.data.entrySet();
	}

	public Set<P> keySet() {
		return this.data.keySet();
	}

	public Collection<Set<V>> values() {
		return this.data.values();
	}

	@Override
	public String toString() {
		TreeSet<String> strdata = new TreeSet<String>();
		Iterator<Map.Entry<P, Set<V>>> iter = this.data.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<P, Set<V>> entry = iter.next();
			strdata.add(entry.toString());
		}
		return strdata.toString();
	}

	public void clear() {
		this.data.clear();
	}
	
}
