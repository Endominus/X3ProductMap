import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Scanner;
import java.sql.*;

public class Controller
{

	// TODO Turn the master lists into database tables
	public static Hashtable<Integer, String> MASTER_RESOURCE_LIST;
	public static Hashtable<Integer, Factory> MASTER_FACTORY_LIST;
	private static ArrayList<Sector> SECTOR_LIST;
	// private ArrayList<Integer[]> resourcesInTransit = new ArrayList<>();

	static Scanner INPUT_SCANNER = new Scanner(System.in);

	private static long TIME = 1000000;

	public static void main(String[] args) throws ClassNotFoundException
	{
		Class.forName("org.sqlite.JDBC");
		Connection connection = null;
		try
	    {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
	
			statement.executeUpdate("drop table if exists person");
			statement.executeUpdate("create table person (id integer, name string)");
			statement.executeUpdate("insert into person values(1, 'leo')");
			statement.executeUpdate("insert into person values(2, 'yui')");
			ResultSet rs = statement.executeQuery("select * from person");
			while(rs.next())
			{
				// read the result set
				System.out.println("name = " + rs.getString("name"));
				System.out.println("id = " + rs.getInt("id"));
			}
			statement.executeUpdate("drop table if exists person");
	    }
	    catch(SQLException e)
	    {
	    	// if the error message is "out of memory", 
	    	// it probably means no database file is found
	    	System.err.println(e.getMessage());
	    }
	    finally
	    {
	    	try
	    	{
	    		if(connection != null)
	    			connection.close();
	    	}
	    	catch(SQLException e)
	    	{
	    		// connection close failed.
	    		System.err.println(e);
	    	}
	    }
		
		InitializeMasterLists();
		InitializeSectorList();
		//System.out.printf(SECTOR_LIST.get(0).toString());

		View v = new View();
		for (Sector sect : SECTOR_LIST)
		{
			v.AddSector(sect.getName(), 0, sect.getCoords()[0],
					sect.getCoords()[1]);
		}

		while (TIME < 1000000)
		{
			Pulse();
		}
		// return 0;
	}

	private static void InitializeSectorList()
	{
		SECTOR_LIST = new ArrayList<>();
		Sector sect1 = new Sector("Argon Prime", 0, 0);
		Sector sect2 = new Sector("Power Circle", 0, 1);
		Sector sect3 = new Sector("Ore Belt", 1, 0);

		SECTOR_LIST.add(sect1);
		SECTOR_LIST.add(sect2);
		SECTOR_LIST.add(sect3);

		LinkSectors(sect1, sect2);
		LinkSectors(sect1, sect3);

		Factory f;
		f = new Factory(0, 'A', 'L');
		sect1.AddFactory(f);
		f = new Factory(1, 'A', 'M');
		sect1.AddFactory(f);
		f = new Factory(3, 'A', 'S');
		sect1.AddFactory(f);

	}

	private static void InitializeMasterLists()
	{
		MASTER_RESOURCE_LIST = new Hashtable<>();
		MASTER_RESOURCE_LIST.put(0, "Energy");
		MASTER_RESOURCE_LIST.put(1, "Food");
		MASTER_RESOURCE_LIST.put(2, "Ore");
		MASTER_RESOURCE_LIST.put(3, "Product");

		// Factory f = Factory([[0,0], [1, 1]], 0, 'M');

		MASTER_FACTORY_LIST = new Hashtable<>();
		Factory f = new Factory(new int[][] { { 0, 100 } }, 0, 'M',
				"Solar Power Plant");
		MASTER_FACTORY_LIST.put(0, f);
		f = new Factory(new int[][] { { 1, 100 }, { 0, -20 } }, 1, 'M',
				"Wheat Farm");
		MASTER_FACTORY_LIST.put(1, f);
		f = new Factory(new int[][] { { 2, 100 }, { 0, -20 } }, 2, 'M',
				"Ore Mine");
		MASTER_FACTORY_LIST.put(2, f);
		f = new Factory(new int[][] { { 3, 100 }, { 0, -20 }, { 1, -50 },
				{ 2, -50 } }, 3, 'S', "Weapon Component Factory");
		MASTER_FACTORY_LIST.put(3, f);

	}

	private static void LinkSectors(Sector sect1, Sector sect2)
	{
		sect1.AddSector(sect2);
		sect2.AddSector(sect1);
	}

	/**
	 * Pulses every sector for production and requisition. Consists of these
	 * steps; 1. Production using stockpiled resources 2. Searching for
	 * somewhere to offload produced resources
	 */
	private static void Pulse()
	{
		for (Sector sec : SECTOR_LIST)
		{
			sec.Pulse();
		}
	}

	// Because of the issues involved in properly spacing out the production and
	// consumption strings, this may work better returning three separate
	// strings. The other options are inefficient, requiring multiple list
	// traversals to assemble the proper information; on the other hand, it
	// means that information will not be properly aligned.
	public static String GetStats(int sectorID)
	{
		StringBuilder sb = new StringBuilder();
		Sector sect = SECTOR_LIST.get(sectorID);
		int key, value;

		sb.append("Name: " + sect.getName() + "\n\n");
		sb.append("Produces\t    Consumes\n");
		HashMap<Integer, Integer> demand = sect.getResourceDemand();
		HashMap<Integer, Integer> supply = sect.getResourceSupply();

		// Clever, but not useful right now
		for (Entry<Integer, Integer> res : demand.entrySet())
		{
			key = res.getKey();
			value = res.getValue();
			// The lambda expression requires the full res.GetKey()
			supply.merge(key, value, (oldValue, newValue) -> {
				supply.put(res.getKey(), newValue);
				return oldValue;
			});
		}/**/

		for (Entry<Integer, Integer> res : supply.entrySet())
		{
			key = res.getKey();
			value = res.getValue();
			if (key < 0)
				continue;
			if (supply.containsKey(-key))
			{
				sb.append(String.format(ResourceRound(value) + " %1$-15s"
						+ ResourceRound(Math.abs(supply.get(-key))) + " %1$-15s\n",
						MASTER_RESOURCE_LIST.get(key)));
			} else if (value < 0)
			{
				sb.append(String.format(
						"\t    " + ResourceRound(value)
								+ " %1$-15s\n", MASTER_RESOURCE_LIST.get(key)));
			} else
			{
				sb.append(String.format(ResourceRound(value)
						+ " %1$-15s\n", MASTER_RESOURCE_LIST.get(key)));
			}
		}

		return sb.toString();
	}

	private static String ResourceRound(int value)
	{
		// TODO Auto-generated method stub
		return Integer.toString(value);
	}

	// Constants
	public static int MAX_TRAVEL_DISTANCE = 2;
	public static int DEMAND_DISTANCE = 1;
}
