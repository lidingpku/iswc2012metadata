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
 * Common exception message 
 *  
 * @author Li Ding
 * 
 */
public class Sw4jException extends Exception {

	private static final long serialVersionUID = 1L;

	public Sw4jException(int state, Exception e) {
		this (state, e.getClass().getSimpleName(), e.getMessage());
	}

	public Sw4jException(int state, Exception e, String details) {
		this (state, e.getClass().getSimpleName(), details +"\n"+ e.getMessage());
	}

	public Sw4jException(int state, String summary) {
		this(state, summary, "");
	}
	public Sw4jException(int state, String summary, String details) {
		this(state, summary, details,"");
	}	
	
	public Sw4jException(int state, String summary, String details, String creator) {
		super(new Sw4jMessage("", state,summary,details,creator).toString());
	}
	
	public Sw4jException(DataSmartMap msg) {
		super(msg.toString());
	}

}
