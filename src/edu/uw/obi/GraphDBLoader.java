/**
 * 
 */
package edu.uw.obi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONWriter;

import edu.uw.obi.publichealth.PersistentPublicHealthGraph;

public class GraphDBLoader
{
	// could switch to a file system based graph db like Neo4J
	//Graph graph = new Neo4jGraph("/tmp/my_graph");
//	PublicHealthGraph graph = new PublicHealthGraph();
//	PersistentPublicHealthGraph graph = new PersistentPublicHealthGraph("local:graph_db/synth");
	PersistentPublicHealthGraph graph;
	Connection conn = null;
	Set<Edge> edgeSet = new HashSet<Edge>();
	
	public GraphDBLoader(PersistentPublicHealthGraph graph)
	{
		this.graph = graph;
		if(!init())
		{
			System.err.println("initialization failed!");
			System.exit(-1);
		}
	}
	
	public boolean init()
	{
		try
		{
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
			conn=DriverManager.getConnection("jdbc:ucanaccess://resources/SynthesizedPublicHealthDataAddr.mdb;");
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/*
	private void parseData(File dataFile)
	{
		try {
		    FileInputStream fs = new FileInputStream(dataFile);
		    Workbook wb = new XSSFWorkbook(fs);
		    Sheet sheet = wb.getSheetAt(0);
		    Row row;
		    Cell cell;

		    int rows; // No of rows
		    rows = sheet.getPhysicalNumberOfRows();

		    int cols = 0; // No of columns
		    int tmp = 0;

		    // This trick ensures that we get the data properly even if it doesn't start from first few rows
		    for(int i = 0; i < 10 || i < rows; i++) {
		        row = sheet.getRow(i);
		        if(row != null) {
		            tmp = sheet.getRow(i).getPhysicalNumberOfCells();
		            if(tmp > cols) cols = tmp;
		        }
		    }

		    for(int r = 0; r < rows; r++) {
		        row = sheet.getRow(r);
		        if(row != null) {
		            for(int c = 0; c < cols; c++) {
		                cell = row.getCell((short)c);
		                if(cell != null) {
		                    
		                	// Each row represents a new case
		                	Vertex caseV = graph.addCase(caseID);
		                }
		            }
		        }
		    }
		} catch(Exception ioe) {
		    ioe.printStackTrace();
		}
	}
	*/
	
	private void readDataIntoVertices()
	{
	    Statement st = null;
	    ResultSet rs = null;
	    
		 try
		{
			st =conn.createStatement();
			rs = st.executeQuery("SELECT Case.*,Address.Lat,Address.StreetAddress,Address.Lng FROM Case,Address where Case.DbID=Address.Case_DbID");
			
			ResultSetMetaData meta = rs.getMetaData();
			while(rs.next())
			{
				Vertex currCase = graph.addCase(rs.getObject("DBID"));
				
				for(int columnInd=1; columnInd<=meta.getColumnCount(); columnInd++)
				{
					String label = meta.getColumnLabel(columnInd);
					Object value = rs.getObject(columnInd);
					if(value!=null)
						currCase.setProperty(label,value);
				}
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			// release database resources
			try
			{
				rs.close();
				st.close();
				conn.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void generateEdges()
	{
		//LinkedList vQueue = new LinkedList(graph.getVertices());
		//ClusterNameComp comp = new ClusterNameComp();
		for(Vertex vert :graph.getVertices())
		{
			// first add edge if two vertices have the same cluster name
			Object cluster_val = vert.getProperty("CLUSTER_NM");
			if(cluster_val!=null&&!cluster_val.equals(""))
			{
				//System.err.println(vert+" has cluster value "+cluster_val);
				for(Vertex match : graph.getVertices("CLUSTER_NM", cluster_val))
				{
					if(!match.equals(vert))
					{
						graph.addEdge(null, vert, match, "cluster_link");
					}
				}
			}
			
			/*
			// add edge if two vertices have the same home phone number
			Object phone_val = vert.getProperty("HOME_PHONE");
			for(Vertex match : graph.getVertices("HOME_PHONE", phone_val))
			{
				graph.addEdge(null, vert, match, "home_phone_link");
			}
			
			// add edge if contact phone of v1 is the home phone or cell phone of v2
			Object contact_phone_val = vert.getProperty("OTHERCONTACT_PHONE");
			for(Vertex match : graph.getVertices("HOME_PHONE", contact_phone_val))
			{
				graph.addEdge(null, vert, match, "contact_phone");
			}
			for(Vertex match : graph.getVertices("CELL_PHONE", contact_phone_val))
			{
				graph.addEdge(null, vert, match, "contact_phone");
			}
			*/
			
		}
		//graph.commit();	
	}
	
	private void generatePhoneEdges()
	{
		for(Vertex vert :graph.getVertices())
		{
			// add edge if two vertices have the same home phone number
			Object phone_val = vert.getProperty("HOME_PHONE");
			if(phone_val!=null&&!phone_val.equals(""))
			{
				for(Vertex match : graph.getVertices("HOME_PHONE", phone_val))
				{
					if(!match.equals(vert))
						graph.addEdge(null, vert, match, "home_phone_link");
				}
			}
			
			// add edge if contact phone of v1 is the home phone or cell phone of v2
			Object contact_phone_val = vert.getProperty("OTHERCONTACT_PHONE");
			if(contact_phone_val!=null&&!contact_phone_val.equals(""))
			{
				for(Vertex match : graph.getVertices("HOME_PHONE", contact_phone_val))
				{
					if(!match.equals(vert))
						graph.addEdge(null, vert, match, "contact_phone_link");
				}
				for(Vertex match : graph.getVertices("CELL_PHONE", contact_phone_val))
				{
					if(!match.equals(vert))
					{
						Edge testE = graph.addEdge(null, vert, match, "contact_phone_link");
						//System.err.println(testE);
					}
				}	
			}
		}
	}
	
	public static void printResultSet(ResultSet result) throws SQLException
	{
		while(result.next())
		{
			ResultSetMetaData md = result.getMetaData();
			for (int j = 1; j <= md.getColumnCount(); j++)
			{
				String colName = md.getColumnLabel(j);
				String value = result.getString(j);
				System.out.println(colName + " --- " + value);
			}
		}
	}
	
	private boolean performWriteTest(String graphFileName)
	{
		try
		{
			FileOutputStream oFile = new FileOutputStream("results/"+graphFileName, false);
			GraphSONWriter.outputGraph(graph, oFile);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void eraseDB()
	{
		for(Edge e : graph.getEdges())
		{
			graph.removeEdge(e);
		}
		for(Vertex v : graph.getVertices())
		{
			graph.removeVertex(v);
		}
	}
	
	public void shutdown()
	{
		graph.shutdown();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		PersistentPublicHealthGraph graph = null;
		try {
                        graph = new PersistentPublicHealthGraph("local:F:/RO1/graphdbfromrelationaldb/TinkerPopTest/graph_db/synth");
			graph.setUseLightweightEdges(false);
			
			GraphDBLoader test = new GraphDBLoader(graph);
			
			//clear all existing V and E
			test.eraseDB();
			graph.commit();
			
			// re-populate
			test.readDataIntoVertices();
			graph.commit();
			
			test.generateEdges();
			//test.generatePhoneEdges();
			graph.commit();
			
			//test.performWriteTest(args[0]);
			
			// count vertices
			int counter = 0;
			Iterator<Vertex> vertIt = graph.getVertices().iterator();
			while (vertIt.hasNext()) 
			{
				vertIt.next();
				counter++;
			}
			System.err.println("generated "+counter+" vertices");
			
			// count edges
			counter = 0;
			Iterator<Edge> edgeIt = graph.getEdges().iterator();
			while (edgeIt.hasNext()) 
			{
				edgeIt.next();
				counter++;
			}
			System.err.println("generated "+counter+" edges");
                } catch(Throwable e)
                {
                    e.printStackTrace();
		}finally{
		  graph.shutdown();
		}

	}

}
