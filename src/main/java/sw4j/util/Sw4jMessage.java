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
/**
 * Common sw4j message 
 *   -  a string summary
 *   -  a string details  
 *   -  a string state  (ok 0, warning 1, error 2, fatal 3)
 *   -  a string creator 
 *  
 * @author Li Ding
 * 
 */
public class Sw4jMessage extends DataSmartMap{

	public final static String FIELD_CREATOR = "creator";
	public final static String FIELD_DT_CREATED = "created";
	public final static String FIELD_DETAILS = "details";
	public final static String FIELD_STATE = "state";
	public final static String FIELD_SUMMARY = "summary";
	private final static String[] FIELDS = new String[] { 
		FIELD_STATE,
		FIELD_SUMMARY, 
		FIELD_DETAILS, 
		FIELD_DT_CREATED,
		FIELD_CREATOR };
	
	public final static String SUMMARY_IOException = "FATAL IO exception.";

	
	public final static int CONST_MIN_STATE = 0;
	public final static int CONST_MAX_STATE = 3;
	public final static int STATE_INFO = 0;
	public final static int STATE_WARNING = 1;
	public final static int STATE_ERROR = 2;
	public final static int STATE_FATAL = 3;
	public final static String[] STATE_STRING = new String[] { 
		"INFO", 
		"WARNING",
		"ERROR", 
		"FATAL" };

	
	private int m_state;
	
	private long m_ts;
	
	public Sw4jMessage(String name, int state, String summary, String details, String creator) {
		super(name);
		m_state= Math.max(CONST_MIN_STATE, Math.min(CONST_MAX_STATE, state));
		this.put(FIELD_STATE, STATE_STRING[m_state]);
		this.put(FIELD_SUMMARY, summary);
		this.put(FIELD_DETAILS, details);
		this.put(FIELD_CREATOR, creator);
		m_ts = System.currentTimeMillis();
		this.put(FIELD_DT_CREATED, ToolString.formatXMLDateTime(m_ts));
		this.addStringProperties(FIELDS);
	}
	
	
	////////////////////////////////////////////////
	// functions
	////////////////////////////////////////////////
	
	public String getCreator(){
		return this.getAsString(FIELD_CREATOR);
	}

	public String getDetails(){
		return this.getAsString(FIELD_DETAILS);
	}

	public String getState() {
		return this.getAsString(FIELD_STATE);
	}

	public int getStateID() {
		return this.m_state;
	}

	public String getSummary(){
		return this.getAsString(FIELD_SUMMARY);
	}

}
