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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A LRU map. Users can specify the max-entry and the expiration time for entry.
 * example:
 *    (p1,v1)
 *    (p2,v2)
 *    (p3,v3)
 * 
 * @author Li Ding
 * 
 */
public class DataCachedObjectMap<K, V> extends LinkedHashMap<K, V> {
	
	////////////////////////////////////////////////
	// hidden data
	////////////////////////////////////////////////
	
	private static final long serialVersionUID = 1L;

	protected static boolean debug = false;

	////////////////////////////////////////////////
	// constant
	////////////////////////////////////////////////
	/**
	 * default max entries. the max size of the cache
	 */
	public static final int DEFAULT_MAX_ENTRIES = 100;

	/** 
	 * default expire minute. expire if not be accessed for 10 minutes
	 */
	public static final int DEFAULT_EXPIRATION_MINUTES = 10; 


	////////////////////////////////////////////////
	// internal data
	////////////////////////////////////////////////
	
	private int m_nLimit = DEFAULT_MAX_ENTRIES;

	private int m_nExpirationMinutes = DEFAULT_EXPIRATION_MINUTES;

	private long m_nLastAccessTime = 0;

	////////////////////////////////////////////////
	// constructor
	////////////////////////////////////////////////

	public DataCachedObjectMap() {
	}

	public DataCachedObjectMap(int limit) {
		this.m_nLimit = limit;
	}

	public DataCachedObjectMap(int limit, int expirationMinutes) {
		this.m_nLimit = limit;
		this.m_nExpirationMinutes = expirationMinutes;
	}
	
	
	////////////////////////////////////////////////
	// functions
	////////////////////////////////////////////////

	@Override
	public V put(K key, V value) {
		expireCache();
		this.remove(key);
		return super.put(key, value);
	}

	@Override
	public V get(Object key) {
		expireCache();
		return super.get(key);

	}

	private void expireCache() {
		long currentTime = System.currentTimeMillis();
		if (1000 * 60 * this.m_nExpirationMinutes < currentTime
				- this.m_nLastAccessTime) {
			this.clear();
		}
		this.m_nLastAccessTime = currentTime;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		if (debug)
			System.out.println("removeEldestEntry");
		return size() > this.m_nLimit;
	}

}
