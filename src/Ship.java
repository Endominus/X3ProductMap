import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A Ship acts as the carrier for wares between Factories.
 * 
 * There are three behaviors to concern ourselves with; 1. Initial setting of a
 * Factory, which may result in delaying 2. Successful arrival and trade with a
 * factory, after which a new target is set 3. Interrupted journey, when a new
 * target must be found in the middle of the old one
 * 
 * Flow for a successful trade operation are as follows; 1. Find a factory to
 * buy from, using FindWaresToBuy 2. Plan the journey to that factory with
 * PlanJourney 2a. If the InterruptJourney signal is received, GOTO 1. 3.
 * Arriving at the factory, receive the Trade command 4. Find a factory to sell
 * to, using FindBuyer. 5. Plan the journey to that factory with PlanJourney 5a.
 * If the InterruptJourney signal is received, GOTO 1. 6. Arriving at the
 * factory, receive the Trade command 7. If cargo bay is empty, GOTO 1. 8. Else,
 * GOTO 4.
 * 
 * Whenever a Ship arrives at a Factory, it can safely assume that it is the
 * first Ship at that factory to complete whatever transaction it is attempting
 * since it set the Factory as a target.
 * 
 * @author Endominus
 *
 */
public class Ship
{
	// Distance is a time value, not a length
	private int wi, wa, waMax, distance, speed;
	private Queue<int[]> milestones = new LinkedList<>();
	private Factory fDest = null;
	private Sector sStart, sEnd;

	private boolean buying;

	public Ship(int cap, int speed, Sector s)
	{
		this.waMax = cap;
		this.speed = speed;
		this.wi = -1;
		this.wa = 0;
		this.buying = true;
		this.sStart = s;

		FindWaresToBuy();
	}

	private void FindWaresToBuy()
	{
		this.sStart.setDistance(-1);
		this.fDest = null;
		double maxStock = 0.5;
		Queue<Sector> q = new LinkedList<>();
		ArrayList<Sector> traversed = new ArrayList<>();
		ArrayList<Factory> factories = new ArrayList<>();
		Sector s;

		q.add(this.sStart);
		q.addAll(this.sStart.getSectorList());
		q.forEach((sect) -> sect.setDistance(sect.getDistance() + 1));

		while (!q.isEmpty())
		{
			s = q.poll();
			final int distance = s.getDistance();
			factories = s.getFactoryList();

			for (Factory fact : factories)
			{
				maxStock = FindOptimalFactory(maxStock, s, fact);
			}

			if (distance < Controller.MAX_TRAVEL_DISTANCE)
			{
				// Watch this; expecting a bug from wrong distance reporting
				s.getSectorList().forEach((sect) -> {
					if (sect.getDistance() == 0)
					{
						sect.setDistance(distance + 1);
						q.add(sect);
						traversed.add(sect);
					}
				});
			}
		}

		for (Sector sect : traversed)
		{
			sect.setDistance(0);
		}
	}

	private void LogFlightPlan(int traversed)
	{
		if (fDest != null)
		{
			this.distance = PlanJourney(traversed);
			fDest.RequestDocking(this);
		} else
		{
			this.distance = Controller.TICK_TIME + 1000;
		}

		Controller.AddShipEvent(this);
	}

	/**
	 * On successful arrival at destination factory, initiate trading sequence
	 */
	public void Trade()
	{
		// TODO Implement trading, including reassigning other interested ships
		this.fDest.Takeoff(this);
		this.fDest.NotifyDockingQueue(this.wi);
		if (this.buying)
		{
			int transferAmount = (int) Math.min(this.waMax,
					this.fDest.getStockpile()[0][1]);
			this.wi = (int) this.fDest.getResources()[0][0];
			this.wa = transferAmount;
			this.fDest.Transfer(0, -transferAmount);
			this.buying = !this.buying;
		} else
		{
			int wii;
			for (wii = 1; (int) this.fDest.getResources()[wii][0] != this.wi; wii++)
				;
			int wa = (int) Math.min(this.wa, this.fDest.getResources()[wii][1]
					* 8 - this.fDest.getStockpile()[wii][1]);
			this.wa -= wa;
			this.fDest.Transfer(wii, wa);

			if (this.wa == 0)
			{
				this.wi = -1;
				this.buying = !this.buying;
			}
		}

		int remainder = this.sEnd.getDistance();
		this.sStart = this.sEnd;
		if (this.buying)
		{
			FindWaresToBuy();
		} else
		{
			FindBuyer();
		}
		LogFlightPlan(remainder);
	}

	/**
	 * Finds the best buyer for the wares carried among all sectors within
	 * travel distance
	 */
	private void FindBuyer()
	{
		// TODO Lots of code duplication between this and FindWaresToBuy;
		// reduce?
		this.sStart.setDistance(-1);
		this.fDest = null;
		double minStock = 0.5;
		Queue<Sector> q = new LinkedList<>();
		ArrayList<Sector> ss = new ArrayList<>();
		ArrayList<Factory> fs = new ArrayList<>();
		Sector s;
		q.add(this.sStart);
		q.addAll(this.sStart.getSectorList());
		q.forEach((sect) -> sect.setDistance(sect.getDistance() + 1));

		while (!q.isEmpty())
		{
			s = q.poll();
			final int distance = s.getDistance();
			fs = s.getFactoryList();

			for (Factory fact : fs)
			{
				minStock = FindOptimalFactory(minStock, s, fact);
			}

			if (distance < Controller.MAX_TRAVEL_DISTANCE)
			{
				// Watch this; expecting a bug from wrong distance reporting
				s.getSectorList().forEach((sect) -> {
					if (sect.getDistance() == 0)
					{
						sect.setDistance(distance + 1);
						q.add(sect);
						ss.add(sect);
					}
				});
			}
		}

		for (Sector s2 : ss)
		{
			s2.setDistance(0);
		}
	}

	/**
	 * Finds the best factory to buy from or sell cargo at, for a given sector.
	 * 
	 * If that factory has a better demand ratio than the current baseline
	 * factory, it changes the sector and factory destination to match.
	 * 
	 * @param baseline
	 * @param sect
	 * @param fact
	 * @return
	 */
	private double FindOptimalFactory(double baseline, Sector sect, Factory fact)
	{
		// TODO Magic number here; shall I keep it? Check factory
		// stockpile max
		if (this.buying)
		{
			double stock = fact.getStockpile()[0][1]
					/ (fact.getResources()[0][1] * 8) - distance / 10.0;
			if (stock > baseline)
			{
				baseline = stock;
				fDest = fact;
				this.sEnd = sect;
			}
		} else
		{
			double[][] demand;
			demand = fact.getStockpile();
			for (int i = 1; i < demand.length; i++)
			{
				if (demand[i][0] == this.wi)
				{
					double stock = demand[i][1]
							/ (fact.getResources()[i][1] * 8)
							+ sect.getDistance() / 10.0;

					if (stock < baseline)
					{
						baseline = stock;
						fDest = fact;
						this.sEnd = sect;
					}
				}
			}
		}
		return baseline;
	}

	private int AcquireLocation()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Sets the milestones for the journey to make finding the current location
	 * easier if trading is interrupted. Also returns distance.
	 * 
	 * 
	 * @return
	 */
	private int PlanJourney(int traversed)
	{
		int totalDistance = 100;
		if (this.sEnd == this.sStart)
		{
			totalDistance = 100;
		}
		return totalDistance;
	}

	public void InterruptJourney(int wareID)
	{
		// TODO Implement this function
		int remainder = AcquireLocation();
	}

	public Factory getDestination()
	{
		return fDest;
	}

	public boolean isBuying()
	{
		return buying;
	}

	public int GetDistance()
	{
		return this.distance;
	}

}
