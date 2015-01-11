import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Controller
{

	// TODO Turn the master lists into database tables
	public static Hashtable<Integer, String> MASTER_RESOURCE_LIST;
	public static Hashtable<Integer, Factory> MASTER_FACTORY_LIST;
	private static ArrayList<Sector> SECTOR_LIST;
	private static PriorityQueue<Ship> ShipQueue = new PriorityQueue<>();
	// private ArrayList<Integer[]> resourcesInTransit = new ArrayList<>();

	static Scanner INPUT_SCANNER = new Scanner(System.in);

	private static long TIME = 1000000;

	public static void main(String[] args) throws ClassNotFoundException
	{
		DatabaseShell.InitializeDatabase();
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
		}/**/
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
		try
		{
			ResultSet rs = DatabaseShell.GetWares();
			
			while (rs.next())
			{
				MASTER_RESOURCE_LIST.put(rs.getInt(1), rs.getString(2));
			}
			rs = DatabaseShell.GetFactories();
			ArrayList<double[]> io = new ArrayList<>();
			int id;
			String name;
			char size;
			Factory f;
			
			MASTER_FACTORY_LIST = new Hashtable<>();
			while (rs.next())
			{
				id = rs.getInt(0);
				name = rs.getString(1);
				size = rs.getString(0).charAt(0);
				
				ResultSet wares = DatabaseShell.GetFactoryIO(id);

				while (wares.next())
				{
					io.add(new double[] {wares.getInt(2), wares.getDouble(2)});
				}
				
				f = new Factory(io, id, size, name);
				MASTER_FACTORY_LIST.put(id, f);
			}
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public static void Pulse()
	{
		for (Sector sec : SECTOR_LIST)
		{
			//sec.Pulse();
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
		int key;
		double value;

		sb.append("Name: " + sect.getName() + "\n\n");
		sb.append("Produces\t    Consumes\n");
		HashMap<Integer, Double> demand = sect.getResourceDemand();
		HashMap<Integer, Double> supply = sect.getResourceSupply();

		// Clever, but not useful right now
		for (Entry<Integer, Double> res : demand.entrySet())
		{
			key = res.getKey();
			value = res.getValue();
			// The lambda expression requires the full res.GetKey()
			supply.merge(key, value, (oldValue, newValue) -> {
				supply.put(res.getKey(), newValue);
				return oldValue;
			});
		}/**/

		for (Entry<Integer, Double> res : supply.entrySet())
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

	private static String ResourceRound(double value)
	{
		// TODO Auto-generated method stub
		return Double.toString(value);
	}

	// Constants
	public static int MAX_TRAVEL_DISTANCE = 2;
	public static int DEMAND_DISTANCE = 1;
}
