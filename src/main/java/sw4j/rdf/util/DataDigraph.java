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
package sw4j.rdf.util;
/**
 * Data structure that encodes a directed graph
 * @author Li Ding
 * 
 */

import java.util.Set;
import java.util.TreeSet;

import sw4j.util.AbstractPropertyValuesMap;
import sw4j.util.ToolMath;


public class DataDigraph  {
	boolean [][] m_mat_adj= null;

	public DataDigraph(int max_index){
		//plus 1 here for play safe, some time user just put a max index here.
		m_mat_adj = new boolean [max_index+1][max_index+1];
		
		//init all false
		int max = this.m_mat_adj.length;
		 for(int i = 0;i <max; i++)
			  for(int j = 0;j < max; j++)
				  this.m_mat_adj[i][j]=false;

	}
	
	public static DataDigraph create(AbstractPropertyValuesMap<Integer,Integer> apvm){
		int max = 0;
		max=Math.max(max,ToolMath.max(apvm.getValues(),0));
		max=Math.max(max,ToolMath.max(apvm.keySet(),0));
		
		DataDigraph dd = new DataDigraph(max);
		for (Integer from: apvm.keySet()){
			dd.add(from, apvm.getValuesAsSet(from));
		}
		return dd;
	}
	
	public DataDigraph(DataDigraph other){		
		m_mat_adj = new boolean [other.m_mat_adj.length][other.m_mat_adj[0].length];
		for (int i=0; i< other.m_mat_adj.length; i++)
			for (int j=0; j< other.m_mat_adj[i].length; j++)
				m_mat_adj[i][j]=other.m_mat_adj[i][j];
			//System.arraycopy(other.m_mat_adj[i],0,this.m_mat_adj[i],0,other.m_mat_adj[i].length);
	}
	
	public DataDigraph create_tc(){
		DataDigraph dd = new DataDigraph(this);
		dd.tc();
		return dd;
	}

	/**
	 * make transitive closure without using self-reflective relation (v,v)
	 */
	public void tc(){
		int max = m_mat_adj.length;
		int k,row,col;
	      for ( k = 0; k < max; k++ )
	      {  
	         for ( row = 0; row < max; row++ )
	         // In Warshall's original paper, the inner-most loop is
	         // guarded by the boolean value in [row][k] --- omitting
	         // the loop on false and removing the "&" in the evaluation.
	            if ( m_mat_adj[row][k] )
	               for ( col = 0; col < max; col++ )
	            	   m_mat_adj[row][col] = m_mat_adj[row][col] | m_mat_adj[k][col];
	      }
		/*
		 for(int i = 0;i <max; i++)
			  for(int j = 0;j < max; j++)
			   if(isReachable(i,j))
			    for(int k = 0; k < max; k++)
			      if(isReachable(j,k))
			    	  add(i,k);
		 */
	}
	
	/**
	 * add self-reflex link
	 */
	public void reflex(){
		for (int i:getVertex()){
			this.add(i, i);
		}
	}
	
	public void add(int from, int to){
		this.m_mat_adj[from][to]=true;
	}
	   
	public void add(int from, Set<Integer> set_to){
		for (int to: set_to)
			this.m_mat_adj[from][to]=true;
	}
	
	public boolean isReachable(int from, int to){
		if (from>=this.m_mat_adj.length)
			return false;
		
		if (to>=this.m_mat_adj[0].length)
			return false;
		return this.m_mat_adj[from][to];
	}

	public boolean isReachable(Set<Integer> set_from, Set<Integer> set_to){
		for (int from : set_from){
			for (int to : set_to){
				if (isReachable(from,to))
					return true;
			}
		}
		return false;
	}
	
	public boolean hasCycle(){
		for (int i=0; i<this.m_mat_adj.length; i++)
			if (this.isReachable(i,i) )
				return true;
		return false;
	}
	
	public Set<Integer> getTo(int from){
		TreeSet<Integer> ret = new TreeSet<Integer>();
		for (int i=0; i<this.m_mat_adj[from].length; i++){
			if (isReachable(from, i))
					ret.add(i);
		}
		return ret;
	}
	
	public Set<Integer> getFrom(int to){
		TreeSet<Integer> ret = new TreeSet<Integer>();
		for (int i=0; i<this.m_mat_adj.length; i++){
			if (isReachable(i, to))
					ret.add(i);
		}
		return ret;
	}

	public Set<Integer> getVertex(){
		TreeSet<Integer> ret = new TreeSet<Integer>();
		for (int i=0; i<this.m_mat_adj.length; i++){
			for (int j=0; j<this.m_mat_adj[i].length; j++)
				if (isReachable(i,j)){
					ret.add(i);
					ret.add(j);
				}
		}
		return ret;
	}
	
	public int size(){
		int size =0;
		for (int i=0; i<this.m_mat_adj.length; i++){
			for (int j=0; j<this.m_mat_adj[i].length; j++)
				if (isReachable(i, j))
					size++;
		}
		return size;
	}

	public Set<Integer> getFrom(){
		TreeSet<Integer> ret = new TreeSet<Integer>();
		for (int i=0; i<this.m_mat_adj.length; i++){
			for (int j=0; j<this.m_mat_adj[i].length; j++)
				if (isReachable(i, j))
					ret.add(i);
		}
		return ret;
	}

	@Override
	public String toString(){
		String ret = "";
		for (int i=0; i<this.m_mat_adj.length; i++){
			ret += String.format ("%d -> %s\n", i, getTo(i));
		}			
		return ret;
	}
}
