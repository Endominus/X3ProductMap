import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class Controller
{
	public static Hashtable<Integer, String> MASTER_RESOURCE_LIST;
	public static Hashtable<Integer, Factory> MASTER_FACTORY_LIST;
	public static Hashtable<Integer, Sector> SECTOR_LIST;
	private static PriorityQueue<Ship> shipQueue = new PriorityQueue<Ship>((sa,
			sb) -> sa.GetDistance() - sb.GetDistance());

	public static long TIME = 1000000;

	public static void main(String[] args) throws ClassNotFoundException
	{
		Controller.init(null);
		
		View v = new View();
		for (Sector sect : SECTOR_LIST.values())
		{
			v.AddSector(sect.getName(), sect.getId(), sect.getCoords()[0],
					sect.getCoords()[1]);
		}
		
		//System.out.print(SECTOR_LIST.get(0).toString());
		//System.out.print(SECTOR_LIST.get(1).toString());

		/*
		 * while (TIME < 1000000) { Pulse(); // shipQueue = new
		 * PriorityQueue<Ship>((sa, sb) -> sa.GetDistance() // -
		 * sb.GetDistance()); }/*
		 */
		// return 0;

	}

	private static void InitializeSectorList()
	{
		try
		{
			SECTOR_LIST = new Hashtable<>();
			
			String name;
			int x, y, id, factid, sSize;
			char race, size;
			ResultSet rs = DatabaseShell.GetSectors();
			// sector (id integer primary key, name text, x integer, y integer)
			// sectorlink (sect1id integer, sect2id integer, distance integer
			// sectorcontent (sectid integer, factid integer, race text, size
			// text, distance integer, yield real
			while (rs.next())
			{
				id = rs.getInt(1);
				name = rs.getString(2);
				sSize = rs.getInt(3);
				x = rs.getInt(4);
				y = rs.getInt(5);
				Sector sect = new Sector(name, id, sSize, x, y);
				
				ResultSet contents = DatabaseShell.GetSectorContents(id);
				
				while (contents.next())
				{
					factid = contents.getInt(2);
					race = contents.getString(3).trim().charAt(0);
					size = contents.getString(4).trim().charAt(0);
					Factory fact = new Factory(factid, race, size);
					
					sect.AddFactory(fact);
				}
				
				SECTOR_LIST.put(id, sect);
			}
			
			rs = DatabaseShell.GetSectorLinks();
			
			while (rs.next())
			{
				LinkSectors(SECTOR_LIST.get(rs.getInt(1)), SECTOR_LIST.get(rs.getInt(2)));
			}
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void InitializeMasterLists()
	{
		try
		{
			MASTER_RESOURCE_LIST = new Hashtable<>();
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
				io.clear();
				id = rs.getInt(1);
				name = rs.getString(2);
				size = rs.getString(3).trim().charAt(0);

				ResultSet wares = DatabaseShell.GetFactoryIO(id);

				while (wares.next())
				{
					io.add(new double[] { wares.getInt(2), wares.getDouble(3) });
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
		TIME = 0;
		while (TIME < MAX_TIME)
		{

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
						+ ResourceRound(Math.abs(supply.get(-key)))
						+ " %1$-15s\n", MASTER_RESOURCE_LIST.get(key)));
			} else if (value < 0)
			{
				sb.append(String.format("\t    " + ResourceRound(value)
						+ " %1$-15s\n", MASTER_RESOURCE_LIST.get(key)));
			} else
			{
				sb.append(String.format(ResourceRound(value) + " %1$-15s\n",
						MASTER_RESOURCE_LIST.get(key)));
			}
		}

		return sb.toString();
	}

	private static String ResourceRound(double value)
	{
		return Double.toString(value);
	}

	// Constants
	public static final int MAX_TRAVEL_DISTANCE = 2;
	public static final int DEMAND_DISTANCE = 1;
	private static final long MAX_TIME = 1000000;
	public static final int TICK_TIME = 1000;
	public static final double CAP_MULT = 8;

	public static void AddShipEvent(Ship ship)
	{
		shipQueue.add(ship);
	}

	public static void init(String sectorSource) throws ClassNotFoundException
	{
		DatabaseShell.InitializeDatabase(sectorSource);
		InitializeMasterLists();
		InitializeSectorList();
	}
}
