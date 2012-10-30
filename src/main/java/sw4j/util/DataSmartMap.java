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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.TreeSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;



/**
 * Store a sorted list of (property, value) pairs, and each property only has one value. 
 * - it has connection with SQL
 * - it has connection with XML rendering
 *
 * @author  Li Ding

 * Sample Code
		DataSmartMap dsm = new DataSmartMap();

		System.out.println("++++++++++ test: add property values ++++++++++++");
		dsm.put("id", 11);
		dsm.put("gid", new Integer(293));
		dsm.put("name", "Li Ding");
		dsm.put("description", "Li Ding's information");
		dsm.put("male", true);

		System.out.println("++++++++++ test: set string-valued properties ++++++++++++");
		dsm.addStringProperty("name");
		dsm.addStringProperty("title");
		dsm.addStringProperty("description");
		
		
		System.out.println("++++++++++ field name ++++++++++++");
		System.out.println (dsm.getAllFieldName());
		System.out.println (dsm.getStringProperties());

		System.out.println("++++++++++ toString ++++++++++++");
		System.out.println (dsm.toString());

		System.out.println("++++++++++ toXml ++++++++++++");
		System.out.println (dsm.toXml());

		System.out.println("++++++++++ toProperty ++++++++++++");
		System.out.println (dsm.toProperty());

		System.out.println("++++++++++ toSQL Insert ++++++++++++");
		System.out.println ("INSERT INTO mytable ");
		System.out.println (dsm.getSQLInsert());

		System.out.println("++++++++++ toSQL Update ++++++++++++");
		System.out.println ("UPDATE mytable SET ");
		System.out.println (dsm.getSQLUpdate());
		System.out.println ("WHERE ");
		System.out.println (dsm.getSQLWhere("id"));

		System.out.println("++++++++++ toSQL Where (>1) ++++++++++++");
		HashSet <String> condition = new HashSet<String>();
		condition.add("id");
		condition.add("name");
		System.out.println (dsm.getSQLWhere(condition));
 *
 */
public class DataSmartMap implements Comparable<String>{
	////////////////////////////////////////////////
	// constant
	////////////////////////////////////////////////
	public final static String DB_TRUE = "1";
	public final static String DB_FALSE = "0";
	
	////////////////////////////////////////////////
	// internal data
	////////////////////////////////////////////////
	
	private String m_szName = null;

	/**
	 * property whose value is a string
	 */
    private TreeSet<String> m_string_fields =new TreeSet<String>();
	
    /**
     * the map of (property, value)
     */
	protected TreeMap<String,Object> m_data = new TreeMap<String,Object>();
	
	////////////////////////////////////////////////
	// constructor
	////////////////////////////////////////////////
	public DataSmartMap(){
		this("");
	}

	public DataSmartMap(String szName){
		if (ToolSafe.isEmpty(szName))
			this.m_szName = this.getClass().getSimpleName()+ System.currentTimeMillis();
		else
			this.m_szName = szName;
	}
	
	
	////////////////////////////////////////////////
	// function (string property: the value of a property is a string)
	////////////////////////////////////////////////

    public void addStringProperty(String property){
    	m_string_fields.add(property);
    }
	public boolean isStringProperty(String szField){
		return m_string_fields.contains(szField);
	}
	
	public void addStringProperties(Set<String> fields){
		m_string_fields.addAll(fields);
	}

	public void addStringProperties(String[] fields){
		if (null!=fields)
			for (int i=0; i<fields.length; i++)
				addStringProperty(fields[i]);
	}

	public TreeSet<String> getStringProperties(){
		return m_string_fields;
	}
	
	////////////////////////////////////////////////
	// function  (data map)
	////////////////////////////////////////////////
	protected Logger getLogger(){
		return Logger.getLogger(this.getClass());
	}
	

	public Map<String,Object> getData(){
		return m_data;
	}
	public Set<String> getAllFieldName(){
		return m_data.keySet();
	}
    
   
	public void put(String property, String value){
		this.m_string_fields.add(property);
		this.do_put( property, value );
	}
	
    public void put(String property, String value, int len){
    	if (null!=value){
    		if (value.length()>len){
    			value = value.substring(0,len);
    			getLogger().info("value truncated:"+property+"=>"+value);
    		}
    	}
		this.put( property, value );
    }

    public void put(String property, Object value){
    	this.do_put(property, value);
	}

    public void put(String property, char value){
    	this.do_put(property, ""+value);
	}

	public void put(String property, boolean value){
		Object obj = value;
    	if (value)
    		this.do_put( property, obj );	//true
        else
        	this.do_put( property, obj );  //false
    }

	public void put(String property, int value){
		Object obj = value;
		this.do_put( property, obj );
	}

	public void put(String property, long value){
		Object obj = value;
		this.do_put( property, obj );
	}

	public void put(String property, double value){
		Object obj = value;
		this.do_put( property, obj );
	}
	
	private void do_put(String property, Object value){
   		m_data.put( property, value );
    }
 	
	public void putAll(Map<String,Object> data, Set<String> stringFields){
		m_data.putAll(data);
		if (!ToolSafe.isEmpty(stringFields))
			m_string_fields.addAll(stringFields);
	}    
	
	public void putAll(List<String> fields, List<String> values, Set<String> stringFields){
		Iterator<String> iter_field = fields.iterator();
		Iterator<String> iter_value = values.iterator();
		while (iter_value.hasNext()){
			String field = iter_field.next();
			String value = iter_value.next();

			if (!ToolSafe.isEmpty(value))
				m_data.put(field, value);
		}
		if (!ToolSafe.isEmpty(stringFields))
			m_string_fields.addAll(stringFields);
	}    
	
	public void copy(DataSmartMap sf){
		this.putAll(sf.m_data, sf.m_string_fields);
	}  	
	
	/**
	 * return the value of an entry as string
	 * 
	 * @param property
	 * @return
	 */
	public String getAsString(String property){
        if (null == m_data)
            return null;

        Object obj =m_data.get(property);
        if (null== obj)
        	return null;
        
        return obj.toString();
    }
	public String getAsDbString(String property){
        if (null == m_data)
            return null;
        
        Object value = m_data.get(property);
        if (value instanceof Boolean){
        	return parseBooleanToDbString((Boolean)value);
        }else{
        	return value.toString();
        }
    }
	
	public static boolean parseDbStringToBoolean(String temp){
		return DB_TRUE.equals(temp);
	}
	public static String parseBooleanToDbString(Boolean temp){
		if (temp)
			return DB_TRUE;
		else
			return DB_FALSE;
	}
	
	public void clearData(){
		m_data.clear();
	}
    

	/**
	 * test if the value of a property is not null
	 * 
	 * @param property
	 * @return
	 */
	public boolean testEntry(String property){
        Object obj = m_data.get(property);
        return (null!=obj);
    }	

	public void print(){
    	System.out.println(this.toString());
    }
    
	public int compareTo(String arg0) {
		return this.toString().compareTo(arg0.toString());
	}

	
    @Override
	public String toString(){
    	//return this.m_string_fields+ "\n"+ToolSafe.printMapToString(this.m_data);
    	return m_data.toString();
    }
    

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
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
		final DataSmartMap other = (DataSmartMap) obj;
		if (m_data == null) {
			if (other.m_data != null)
				return false;
		} else if (!m_data.equals(other.m_data))
			return false;
		return true;
	}

	////////////////////////////////////////////////
	// translation  (to java property)
	////////////////////////////////////////////////
	/**
	 * translate to java property
	 */
    public Properties toProperty(){
    	Properties ret = new Properties();
    	ret.putAll(this.m_data);
    	return ret;
    }
	
	////////////////////////////////////////////////
	// translation  (to string template)
	////////////////////////////////////////////////
	/**
	 * translate to wiki template
	 */
    public String toTemplate(String templatename){
    	String ret = "{{"+ templatename;
	    Iterator<Map.Entry<String,Object>> iter = m_data.entrySet().iterator();
	    while (iter.hasNext()){
		    Map.Entry<String,Object>  entry = iter.next();
		    
		    ret += String.format("\n |%s=%s", entry.getKey(), entry.getValue());
	    }
	    ret +="\n}}";
	    return ret;
    }
	
	
	////////////////////////////////////////////////
	// translation (to XML)
	////////////////////////////////////////////////
    
    
	public String toXml(){
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		toXml(out);
		return sw.toString();
	}
    
    public void toXml(PrintWriter out){
		try{
			TransformerHandler hd = xmlStartDocument(out);
			xmlStartContent(hd);
			xmlEndContent(hd);
			xmlEndDocument(hd);		
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
    }
    
    public TransformerHandler xmlStartDocument(PrintWriter out) throws TransformerConfigurationException, SAXException{
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		// SAX2.0 ContentHandler.
		TransformerHandler hd;
		hd = tf.newTransformerHandler();
		Transformer serializer = hd.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
		//serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
		serializer.setOutputProperty(OutputKeys.INDENT,"yes");
		hd.setResult(streamResult);
		hd.startDocument();
    	
		return hd;
    }
    
    @SuppressWarnings("unchecked")
	public void xmlStartContent(TransformerHandler hd) throws SAXException{
		AttributesImpl atts = new AttributesImpl();
		  atts.addAttribute("", "", "xmlns:xsi", "", "http://www.w3.org/2001/XMLSchema-instance");
		// Entry
		hd.startElement("","",m_szName,atts);
		// properties
	    Iterator<Map.Entry<String,Object>> iter = m_data.entrySet().iterator();
	    while (iter.hasNext()){

		    Map.Entry<String,Object>  entry = iter.next();
  		    Object value = entry.getValue();
	        if (null!=value){
		      atts.clear();
		      Class[] myclass = new Class[]{
		    		  String.class,
		    		  Integer.class,
		    		  Boolean.class,
		      };
		      for (int i=0; i<myclass.length; i++){
		    	  Class theclass = myclass[i];
		    	  if (theclass.isInstance(value)){
		    		  atts.addAttribute("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi:type", "", theclass.getSimpleName().toLowerCase());
		    		  break;
		    	  }
		      }
	  		  hd.startElement("","",entry.getKey(),atts);
	  		  //hd.startCDATA();
	  		  String szTemp = entry.getValue().toString();
			  hd.characters(szTemp.toCharArray(),0,szTemp.length());
	  		  //hd.endCDATA();
	  		  hd.endElement("","",entry.getKey());
	        }
	    }
	}
    
    public void xmlEndContent(TransformerHandler hd) throws SAXException{
		hd.endElement("","",m_szName);
	}   
    
    public void xmlEndDocument(TransformerHandler hd) throws SAXException{
		hd.endDocument();		
    }

	////////////////////////////////////////////////
	// translation (to HTML)
	////////////////////////////////////////////////
    
    
	public String toHTMLtablerow(){
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		Iterator<Map.Entry<String, Object>> iter = getData().entrySet().iterator();
		//out.println(String.format("<tr>"));
		while (iter.hasNext()){
			Map.Entry<String, Object> entry = iter.next();
			
			out.println(String.format("<td>%s</td>", entry.getValue()));
		}
		//out.println(String.format("</tr>"));
		return sw.toString();
	}
    
	public String toHTMLtablerowheader(){
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		Iterator<Map.Entry<String, Object>> iter = getData().entrySet().iterator();
		//out.println(String.format("<tr>"));
		while (iter.hasNext()){
			Map.Entry<String, Object> entry = iter.next();
			
			out.println(String.format("<td>%s</td>", entry.getKey()));
		}
		//out.println(String.format("</tr>"));
		return sw.toString();
	}

	
	////////////////////////////////////////////////
	// translation (to CSV)
	////////////////////////////////////////////////
	public String toCSVheader() {
		String content = "";
		Iterator<Map.Entry<String, Object>> iter = getData().entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, Object> entry = iter.next();
			
			if (content.length()>0)
				content+=",";
			content+= String.format("\"%s\"", escapeCSV(entry.getKey()));
		}
		return content;
	}

	public String toCSVrow() {
		String content = "";
		Iterator<Map.Entry<String, Object>> iter = getData().entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, Object> entry = iter.next();
			
			if (content.length()>0)
				content+=",";
			content +=String.format("\"%s\"", escapeCSV(entry.getValue().toString()));
		}
		return content;
	}

	private static String escapeCSV(String text){
		return text.replace("\"","\"\"");
	}
	////////////////////////////////////////////////
	// translation (to TSV)
	////////////////////////////////////////////////
	public String toTSVheader() {
		String content = "";
		Iterator<Map.Entry<String, Object>> iter = getData().entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, Object> entry = iter.next();
			
			if (content.length()>0)
				content+="\t";
			content+= String.format("%s", escapeTSV(entry.getKey()));
		}
		return content;
	}

	public String toTSVrow() {
		String content = "";
		Iterator<Map.Entry<String, Object>> iter = getData().entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, Object> entry = iter.next();
			
			if (content.length()>0)
				content+="\t";
			content +=String.format("%s", escapeTSV(entry.getValue().toString()));
		}
		return content;
	}

	private static String escapeTSV(String text){
		return text.replace("\\s+"," ");
	}

	////////////////////////////////////////////////
	// translation (to JSON object)
	////////////////////////////////////////////////
	public String toJSON(){
		String content = "";
		Iterator<Map.Entry<String, Object>> iter = getData().entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry<String, Object> entry = iter.next();
			
			if (content.length()>0)
				content+=",";
			content +=String.format("\"%s\":\"%s\"",escapeJSON(entry.getKey()), escapeJSON(entry.getValue().toString()));
		}
		return String.format("{%s}",content);
		
	}
	private static String escapeJSON(String text){
		return text.replace("\"","\\\"");
	}
	////////////////////////////////////////////////
	// translation (to SQL)
	////////////////////////////////////////////////
	
	public String getSQLUpdate(){
		Iterator<String> iter = this.getAllFieldName().iterator();
		String szSQL="";
		while (iter.hasNext()){
			String property = (String) iter.next();

			if (szSQL.length()>0)
				szSQL+=",";
				
			szSQL +=" " + property+"=";
			szSQL += getSQLField(property);
		}
		return szSQL;
	}
	
	public String getSQLInsert(){
		return  " ("+ getSQLInsertFields()
				+" ) VALUES ( "
				+ getSQLInsertValues()
				+ ") ";
	}
    
	public String getSQLWhere(String property){
		return property + " = "+getSQLField(property);
	}
	
	public String getSQLWhere(Set<String> fields){
		Iterator<String> iter = fields.iterator();
		String temp = " 1 ";
		while (iter.hasNext()){
			String property = (String)iter.next();
			temp += " AND "+property + " = "+getSQLField(property);
		}
		return temp;
	}
	
	private String getSQLField(String property){
		
		String temp = getAsDbString(property);
		if (null==temp)
			return "null";
		
		//if (temp.length()==0)
		//	return temp;
			
		if (isStringProperty( property)){
			return "'"+encodeSQLString(temp)+"'";
		}else
			return 	temp;	
	}
   
	public static String encodeSQLString(String value){
		String temp = value;
		temp = temp.replaceAll("\\\\","\\\\\\\\");
		temp = temp.replaceAll("'","\\\\'");
		return temp;		
	}
	
	public String getSQLInsertFields(){
		Iterator<String> iter = this.getAllFieldName().iterator();
    	String szSQL="";
		while (iter.hasNext()){
			if (szSQL.length()>0)
				szSQL+=",";
			szSQL += (String) iter.next();
		}
    	return szSQL;
    }
    
	public String getSQLInsertValues(){
		Iterator<String> iter = this.getAllFieldName().iterator();
		String szSQL="";
		while (iter.hasNext()){
			String property = (String) iter.next();

			if (szSQL.length()>0)
				szSQL+=",";

			szSQL += getSQLField(property);
		}
		return szSQL;
	}

	public boolean isEmpty(){
		return getData().size()==0;
	}
}
