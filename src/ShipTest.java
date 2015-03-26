import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;


public class ShipTest
{

	/*
	 * Cases to test;
	 * 1. Ships correctly navigate to overstocked factories.
	 * 2. Ships correctly filter destination based on distance.
	 * 3. Ships react correctly when their destination is reached by another
	 */
	ArrayList<Sector> ss;
	ArrayList<Ship> shs = new ArrayList<>();
	
	@Before
	public void setUp() throws Exception
	{
		Controller.init("res/X3_sectors.txt");
		this.ss = new ArrayList<Sector>(Controller.SECTOR_LIST.values());
		this.ss.sort((a, b) -> a.getName().compareTo(b.getName()));
		Ship sh = new Ship(16000, 100, ss.get(0), 0);
		shs.add(sh);
		sh = new Ship(16000, 100, ss.get(1), 1);
		shs.add(sh);
		sh = new Ship(16000, 100, ss.get(2), 2);
		shs.add(sh);
		sh = new Ship(16000, 100, ss.get(3), 3);
		shs.add(sh);
	}

	@Test
	public void testInitialNavigation()
	{
		for (Ship sh : this.shs)
		{
			assertTrue(sh.GetDestination() == null);
		}
		
		Tick();
		
		for (Ship sh : this.shs)
		{
			sh.Trade();
			assertTrue(sh.GetDestination() == null);
		}
		
		Tick();
		Tick();
		Tick();
		
		for (Ship sh : this.shs)
		{
			sh.Trade();
			assertTrue(sh.GetDestination() == null);
		}
		
		Tick();
		Factory f = FindFactory("SectorA", 57);
		
		for (Ship sh : this.shs)
		{
			sh.Trade();
			if (sh.GetId() == 3)
				assertTrue(sh.GetDestination() == null);
			else
				assertTrue(sh.GetDestination() == f);
		}

		assertEquals(5000, this.shs.get(0).GetDistance());
		assertEquals(20000, this.shs.get(1).GetDistance());
		assertEquals(20000, this.shs.get(2).GetDistance());
		
		int newTime = shs.get(0).GetArrivalTime();
		
		for (Sector s : ss)
		{
			s.ProduceGoods(newTime - Controller.TIME);
		}
		Controller.TIME = newTime;
		shs.get(0).Trade();
		
		Factory f2 = FindFactory("SectorA", 41);
		
		assertFalse(shs.get(0).isBuying());
		assertTrue(shs.get(0).GetWareId() == 32);
		assertTrue(shs.get(0).GetWareAmount() == 16000);
		assertTrue(shs.get(0).GetDestination() == f2);
		assertTrue(shs.get(1).GetDestination() == f);
		assertTrue(shs.get(2).GetDestination() == f);
		assertTrue(shs.get(3).GetDestination() == null);
		
		double[][] stock = f2.getStockpile();
		assertTrue(stock[1][1] == 0);
		shs.get(0).Trade();
		stock = f2.getStockpile();
		assertTrue(stock[1][1] == 16000);
	}
	
	private void Tick()
	{
		for (Sector s : this.ss)
		{
			s.ProduceGoods(Controller.TICK_TIME);
		}
	}
	
	private Factory FindFactory(String sN, int fid)
	{
		for (Sector s : this.ss)
		{
			if (s.getName().equals(sN))
			{
				for (Factory f : s.getFactoryList())
				{
					if (f.getTemplate() == fid)
					{
						return f;
					}
				}
			}
		}
		return null;
	}
}
