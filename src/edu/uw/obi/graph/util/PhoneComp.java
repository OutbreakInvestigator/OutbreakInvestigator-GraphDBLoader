/***** Copyright 2014 University of Washington (Neil Abernethy, Wilson Lau, Todd Detwiler)***/
/***** http://faculty.washington.edu/neila/ ****/
/**
 * 
 */
package edu.uw.obi.graph.util;

import java.util.Comparator;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author detwiler
 *
 */
public class PhoneComp implements Comparator<Vertex>
{
	/**
	 * 
	 */
	public PhoneComp()
	{
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Vertex v1, Vertex v2)
	{
		String cluster1 = (String) v1.getProperty("CLUSTER_NM");
		String cluster2 = (String) v2.getProperty("CLUSTER_NM");
		return cluster1.compareTo(cluster2);
	}

}
