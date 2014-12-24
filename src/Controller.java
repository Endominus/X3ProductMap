import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class Controller {

	//TODO Turn the master lists into database tables
	public static Hashtable<Integer, String> MASTER_RESOURCE_LIST;
	public static Hashtable<Integer, Factory> MASTER_FACTORY_LIST;
	private static ArrayList<Sector> SECTOR_LIST;

	static Scanner INPUT_SCANNER = new Scanner(System.in);
	
	// private Hashtable<Integer, String> factoryNames;

	public static void main(String[] args) {
		InitializeMasterLists();
		InitializeSectorList();
		System.out.printf(SECTOR_LIST.get(0).toString());
		
		
		// return 0;
	}

	private static void InitializeSectorList() {
		SECTOR_LIST = new ArrayList<>();
		Sector sect1 = new Sector("Argon Prime");
		Sector sect2 = new Sector("Power Circle");
		Sector sect3 = new Sector("Ore Belt");
		
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

	private static void InitializeMasterLists() {
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
}
