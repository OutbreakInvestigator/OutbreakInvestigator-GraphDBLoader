/***** Copyright 2014 University of Washington (Neil Abernethy, Wilson Lau, Todd Detwiler)***/
/***** http://faculty.washington.edu/neila/ ****/
/**
 * 
 */
package edu.uw.obi.publichealth;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author detwiler
 *
 */
public class PersistentPublicHealthGraph extends OrientGraph
{
	private OClass case_class = this.createVertexType("case");
	
	public PersistentPublicHealthGraph(String graphPath)
	{
		super(graphPath);
		init();
	}
	
	private void init()
	{

	}
	
	public Vertex addCase(Object caseID)
	{
		Vertex v = this.addVertex("class:Case");
		return v;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

	}

}
