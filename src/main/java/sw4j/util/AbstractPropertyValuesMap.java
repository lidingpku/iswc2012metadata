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
import java.util.Set;
/**
 * Store a List of (property, value) pairs.
 * - The toString function offers a canonical representation, i.e. property/value pairs are sorted in alphabatical order. 
 * Example:
 *    (p1, Collection(v11,v12)) 
 *    (p2, Collection(v21)) 
 *    
 * @author Li Ding
 * 
 */

abstract public class AbstractPropertyValuesMap <P,V> implements Comparable<AbstractPropertyValuesMap <P, V>>{

	/**
	 * add a pair of (p,v)
	 * @param p
	 * @param v
	 */
	abstract public void add(P p, V v);
	
	/**
	 * add a collection of p, vs={v1,v2,..}, i.e. (p,v1), (p,v2),...
	 * 
	 * @param p
	 * @param vs
	 */
	public void add(P p, Collection<V> vs){
		Iterator<V> iter = vs.iterator();
		while (iter.hasNext()){
			V v = iter.next();
			this.add(p, v);
		}
	}
	
	/**
	 * add the content of another map to this map
	 * 
	 * @param map
	 */
	final public void add(AbstractPropertyValuesMap<P,V> map){
		Iterator<P> iter = map.keySet().iterator();
		while (iter.hasNext()){
			P p = iter.next();
			this.add(p, map.getValues(p));
		}
	}

	/**
	 * remove all pairs have the property p
	 * 
	 * @param p
	 */
	abstract public void remove(P p);
	
	/**
	 * remove all entries
	 */
	abstract public void clear();

	/**
	 * return a collection of values. each value must show up in a pair (p, ..).
	 * 
	 * @param p
	 * @return  should not be null
	 */
	abstract public Collection<V> getValues(P p);

	public Set<V> getValues(){
		HashSet<V> ret = new HashSet<V>();
		for (P p: keySet()){
			ret.addAll(getValues(p));
		}
		return ret;
	}


	/**
	 * test existence of a value
	 * @param p
	 * @param v
	 * @return
	 */
	
	public boolean hasValue(P p, V v){
		Collection<V> ret = getValues(p);
		if (null==ret)
			return false;
		else
			return ret.contains(v);
	}
	
	/**
	 * return the values as a newly created set. 
	 * 
	 * @param p
	 * @return
	 */
	final public Set<V> getValuesAsSet(P p){
		return new HashSet<V>(getValues(p));
	}

	/**
	 * return the number of values
	 * 
	 * @param p
	 * @return
	 */
	final public int getValuesCount(P p){
		return getValues(p).size();
	}
	
	/**
	 * return all asserted properties 
	 *  
	 * @return
	 */
	abstract public Set<P> keySet();
	
	/**
	 * return cannonical string representation of this object, can be used to support comparable
	 *  
	 * @return
	 */
	abstract public String toString();
	
	
	final public int compareTo(AbstractPropertyValuesMap <P, V> o) {
		return this.toString().compareTo(o.toString());
	}
	
	@Override
	final public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.toString().hashCode();
		return result;
	}

	@Override
	final public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		return this.toString().equals(obj.toString());
	}	
	
	final public String toPrettyString() {
		String ret = "";
		Iterator <P> iter = this.keySet().iterator();
		while (iter.hasNext()){
			P p = iter.next();
			ret+="\n";
			ret+="------------------";
			ret+="\n";
			ret+=p;
			ret+="\n";
			ret+=this.getValues(p);
		}
		return ret;
	}
	
}
