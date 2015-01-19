import static org.junit.Assert.*;

import java.util.Hashtable;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SectorTest
{
	Hashtable<Integer, Sector> ss;
	
	@Before
	public void setUp() throws Exception
	{
		Controller.init("res/X3_sectors.txt");
		this.ss = Controller.SECTOR_LIST;
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testProduceGoods()
	{
		for (double total : ss.get(0).getResourceStockpile().values())
			assertTrue(total == 0.0);
		
		this.ss.get(0).ProduceGoods(3600);
		
		for (Entry<Integer, Double> res : ss.get(0).getResourceStockpile().entrySet())
		{
			if (res.getKey() == 32)
				assertTrue(res.getValue() == 8420.34);
			else
				assertTrue(res.getValue() == 0);
		}
		
		for (double total : ss.get(1).getResourceStockpile().values())
			assertTrue(total == 0);
	}
	
	//@Test
	public void testNetFlow()
	{
		//assertTrue(ss.get(0).NetFlow(0) == 100);
		//ss.get(0).AddFactory(new Factory(1, 'A', 'M'));
		//assertTrue(ss.get(0).NetFlow(0) == 80);
	}

}
