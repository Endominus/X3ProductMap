import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SectorTest
{
	Sector sect;
	
	@Before
	public void setUp() throws Exception
	{
		Controller.main(null);
		this.sect = new Sector("A", 1, 1);
		Factory fact = new Factory(0, 'A', 'M', 150);
		this.sect.AddFactory(fact);
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testNetFlow()
	{
		assertTrue(this.sect.NetFlow(0) == 100);
		sect.AddFactory(new Factory(1, 'A', 'M', 150));
		assertTrue(this.sect.NetFlow(0) == 80);
	}

	@Test
	public void testDemandFactor()
	{
		assertTrue(sect.DemandFactor(0) == 0);
		sect.AddFactory(new Factory(1, 'A', 'X', 150));
		sect.AddFactory(new Factory(1, 'A', 'X', 150));
		assertTrue(sect.DemandFactor(0) == 0.5);
		assertTrue(sect.DemandFactor(2) == 0);
	}

	@Test
	public void testDemand()
	{
		assertTrue(sect.Demand().size() == 0);
		sect.AddFactory(new Factory(3, 'A', 'S', 150));
		assertTrue(sect.Demand().size() == 3);
		sect.AddFactory(new Factory(1, 'A', 'M', 150));
		assertTrue(sect.Demand().get(0) == -40);
		assertTrue(sect.Demand().get(1) == -50);
		assertTrue(sect.Demand().get(2) == -50);
	}

	/*@Test
	public void testSendResource()
	{
		sect.AddFactory(new Factory(3, 'A', 'S'));
		assertTrue(sect.Demand().get(1) == -50);
		sect.SendResource(new int[] {1, 1, 50});
		assertTrue(sect.getResourcesInTransit().size() == 1);
		sect.ReceiveShipments();
		assertTrue(sect.getResourceStockpile().get(1) == 0);
		sect.SendResource(new int[] {1, 2, 500});
		assertTrue(sect.getResourcesInTransit().size() == 2);
		sect.ReceiveShipments();
		assertTrue(sect.getResourceStockpile().get(1) == 50);
		assertTrue(sect.getResourcesInTransit().size() == 1);
	}*/

	@Test
	public void testPulse()
	{
		fail("Not yet implemented");
	}

}
