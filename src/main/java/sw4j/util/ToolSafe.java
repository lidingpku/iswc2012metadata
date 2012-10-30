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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


/**
 * some safe functions with boundary or null test
 * <ul>
 * <li>safe empty test on objects, string and collections</li>
 * <li>safe object equality comparison</li>
 * <li>safe collection size </li>
 * </ul>
 * 
 * @author Li Ding
 * 
 */
public class ToolSafe {

	public final static String ERROR_EMPTY_OBJECT = "Unexpected empty object.";

	public static boolean isEmpty(Collection<Object> data) {
		return null == data || data.size() == 0;
	}

	public static boolean isEmpty(String szText) {
		return null == szText || szText.trim().length() == 0;
	}

	public static boolean isEmpty(File f) {
		return null == f || f.exists();
	}

	public static boolean isEmpty(Object data) {
		if (data instanceof String)
			return isEmpty((String)data);
		else
			return null == data;
	}



	public static boolean isEqual(Object obj1, Object obj2) {
		if (null == obj1 && null == obj2) {
			return true;
		}
		if (null != obj1 && null != obj2 && obj1.equals(obj2)) {
			return true;
		}
		return false;
	}

	public static int getSize(Collection<Object> data) {
		if (null == data) {
			return 0;
		} else {
			return data.size();
		}
	}

	/**
	 * get the first item in the iterator
	 * 
	 * @param iter
	 * @return the first item as object; otherwise null
	 */
	public static Object getFirstItem(Iterator<Object> iter) {
		if (null == iter) {
			return null;
		}

		if (!iter.hasNext()) {
			return null;
		}

		return iter.next();
	}

	public static void checkNonEmpty(Object data, String szMsg)
	throws Sw4jException {
		checkNonEmpty(data, szMsg, Sw4jMessage.STATE_ERROR);
	}
	
	public static void checkNonEmpty(Object data, String szMsg, int state)
	throws Sw4jException {
		if (isEmpty(data)) {
			throw new Sw4jException(state, ERROR_EMPTY_OBJECT, szMsg);
		}
	}

	@SuppressWarnings("unchecked")
	public static Object get(Map map_id_label, Integer id,
			Object default_label) {
		if (isEmpty(map_id_label))
			return default_label;
		
		Object label = map_id_label.get(id);
		if (isEmpty(label))
			return default_label;
		else
			return label;
	}
	
	

}
