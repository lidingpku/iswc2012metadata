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
 * group object by equal relations
 *     
 * @author Li Ding
 * 
 */
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class DataObjectGroupMap <V>{

	//eq relation
	DataPVCMap<V,V> m_map_uri_uri = new DataPVCMap<V,V>();
	
	// original node to group (merged node)
	HashMap<V,Integer> m_map_uri_gid = new HashMap<V,Integer>();
	
	//group (merged node) to original node
	DataPVCMap<Integer,V> m_map_gid_uris = new DataPVCMap<Integer,V>();
	
	private int ggid= 0;
	public Integer create_gid(){
		return new Integer(ggid++);
	}
	
	public void add(DataObjectGroupMap <V> dog){
		for(V uri : dog.m_map_uri_gid.keySet()){
			this.addObject(uri);
		}
	}
	
	public Integer addObject(V uri){
		Integer gid = getGid(uri);
		if (null==gid){
			gid = create_gid();
			do_add(uri, gid);
		}
		return gid;
	}
	
	public Integer addObject(V uri, Integer gid){
		if (null!=gid){
			do_remove(uri);
			do_add(uri,gid);
			return gid;
		}else{
			return addObject(uri);
		}
	}

	public Iterator<Integer> listGids(){
		return this.m_map_gid_uris.keySet().iterator();
	}

	public Set<Integer> getGids(){
		return this.m_map_gid_uris.keySet();
	}

	public Set<V> getObjects(){
		return this.m_map_uri_gid.keySet();
	}
	
	
	public Integer getGid(V uri){
		Integer gid = this.m_map_uri_gid.get(uri);
		return gid;
	}

	public Collection<V> getObjectsByGid(Integer gid){
		return this.m_map_gid_uris.getValues(gid);
	}

	public void addSameObjectAs(V uri1, V uri2){
		do_merge(uri1,uri2);
		m_map_uri_uri.add(uri1,uri2);
	}

	public void addObjectAllSame(Collection<V> uris){
		if (null==uris || uris.size()<=1)
			return;
		
		Iterator<V> iter = uris.iterator();
		V v1 = iter.next();
		while (iter.hasNext()){
			addSameObjectAs(v1,iter.next());
		}		
	}
	
	public void normalize(){
		Iterator<V> iter = this.m_map_uri_uri.keySet().iterator();
		while (iter.hasNext()){
			V uri1 = iter.next();
			Iterator <V> iter_2 = this.m_map_uri_uri.getValues(uri1).iterator();
			while (iter_2.hasNext()){
				V uri2 = iter_2.next();
				
				do_merge(uri1,uri2);
			}
		}
	}
	
	private void do_merge(V uri1, V uri2){
		Integer gid1 = this.m_map_uri_gid.get(uri1);
		Integer gid2 = this.m_map_uri_gid.get(uri2);
		
		if (null!=gid1){
			if (null!=gid2){
				//both non-empty
				if (gid1.equals(gid2))
					return ;
				
				Collection<V> group1 = this.m_map_gid_uris.getValues(gid1);  
				Collection<V> group2 = this.m_map_gid_uris.getValues(gid2);
				
				Iterator<V> iter = group2.iterator();
				while (iter.hasNext()){
					V uri2x = iter.next();
					this.m_map_uri_gid.put(uri2x, gid1);
				}
				
				group1.addAll(group2);
				
				this.m_map_gid_uris.remove(gid2);
				
			}else{
				// empty gid2
				do_add(uri2,gid1);
			}
		}else{
			if (null!=gid2){
				// empty gid1
				do_add(uri1,gid2);
				
			}else{
				// both empty
				gid1 =addObject(uri1);
				do_add(uri2,gid1);
				
			}
		}
	}
	
	private void do_remove(V uri){
		Integer gid = this.m_map_uri_gid.get(uri);
		if (null==gid){
			this.m_map_gid_uris.getValues(gid).remove(uri);
			this.m_map_uri_gid.put(uri, null);
		}
	}

	private void do_add(V uri, Integer gid){
		this.m_map_gid_uris.add(gid, uri);
		this.m_map_uri_gid.put(uri,gid);
	}
	

	public String toString(){
		return this.m_map_gid_uris.m_index.toString();// +"\n"+ this.m_map_uri_uri.toString()+"\n";
	}

	public int getTotalObjects() {
		return this.m_map_uri_gid.size();
	}
	public int getTotalGids() {
		return this.m_map_gid_uris.keySet().size();
	}

	public String toPrettyString() {
		String ret = "";
		Iterator <Integer> iter = this.m_map_gid_uris.keySet().iterator();
		while (iter.hasNext()){
			Integer gid = iter.next();
			ret+=String.format("\n--------------\n%d\n%s",
			 gid,
			 ToolString.printCollectionToString(this.getObjectsByGid(gid)));
		}
		return ret;
	}
	
	public void clear(){
		this.m_map_gid_uris.clear();
		this.m_map_uri_gid.clear();
		this.m_map_uri_uri.clear();
		ggid=0;
	}
}
