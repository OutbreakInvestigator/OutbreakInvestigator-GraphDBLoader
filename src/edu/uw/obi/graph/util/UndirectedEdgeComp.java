/**
 * 
 */
package edu.uw.obi.graph.util;

import java.util.Comparator;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author detwiler
 *
 */
public class UndirectedEdgeComp implements Comparator<Edge>
{
	/**
	 * 
	 */
	public UndirectedEdgeComp()
	{
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Edge e1, Edge e2)
	{
		int returnVal = -1;
		Vertex e1_head = e1.getVertex(Direction.IN);
		Vertex e2_head = e2.getVertex(Direction.IN);
		Vertex e1_tail = e1.getVertex(Direction.OUT);
		Vertex e2_tail = e2.getVertex(Direction.OUT);
		
		boolean identicalEdge = e1_head.equals(e2_head) && e1_tail.equals(e2_tail);
		boolean reverseEdge = e1_head.equals(e2_tail) && e1_tail.equals(e2_head);
		if(identicalEdge||reverseEdge)
			returnVal = 0;
		
		return returnVal;
	}

}
