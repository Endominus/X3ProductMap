import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A Ship acts as the carrier for wares between Factories.
 * 
 * There are three behaviors to concern ourselves with; 1. Initial setting of a
 * Factory, which may result in delaying 2. Successful arrival and trade with a
 * factory, after which a new target is set 3. Interrupted journey, when a new
 * target must be found in the middle of the old one
 * 
 * Flow for a successful trade operation are as follows;
 * 
 * 1. Find a factory to trade with, using GenerateDestination
 * 
 * 1a. If no factory is found, delay for 1000 units and GOTO 1.
 * 
 * 2. Plan the journey to that factory with LogFlightPlan
 * 
 * 2a. If the InterruptJourney signal is received, AcquireLocation and GOTO 1.
 * 
 * 3. Arriving at the factory, receive the Trade command
 * 
 * 4. GOTO 1.
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
	long startTime;
	private Queue<int[]> milestones = new LinkedList<>();
	private Factory fDest = null;
	private Sector sStart, sEnd;
	private boolean buying;

	private static PriorityQueue<Node> pns = new PriorityQueue<Node>(
			(a, b) -> Integer.compare(a.manhattan, b.manhattan));
	private static HashMap<Integer, Node> nm = new HashMap<>();

	public Ship(int cap, int speed, Sector s)
	{
		this.waMax = cap;
		this.speed = speed;
		this.wi = -1;
		this.wa = 0;
		this.buying = true;
		this.sStart = s;

		GenerateDestination();
	}

	private void LogFlightPlan(int traversed)
	{
		if (fDest != null)
		{
			this.distance = PlanJourney(traversed);
			fDest.RequestDocking(this);
		} else
		{
			this.distance = 1000;
		}

		Controller.AddShipEvent(this);
	}

	/**
	 * On successful arrival at destination factory, initiate trading sequence
	 */
	public void Trade()
	{
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

		int remainder = this.sEnd.getSize() / 2;
		this.sStart = this.sEnd;
		GenerateDestination();
		LogFlightPlan(remainder);

	}

	/**
	 * Finds the best factory to land at for trading purposes, whether buying or
	 * selling.
	 * 
	 * If no factory can be found,
	 */
	private boolean GenerateDestination()
	{
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

		return minStock != 0.5;
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
		if (this.buying)
		{
			double stock = fact.getStockpile()[0][1]
					/ (fact.getResources()[0][1] * Controller.CAP_MULT)
					- distance / 10.0;
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
		int interval = (int) (Controller.TIME - this.startTime);

		for (int[] a : this.milestones)
		{
			if (a[1] > interval)
			{
				this.sStart = Controller.SECTOR_LIST.get(a[0]);
				return a[1] - interval;
			}
		}
		return -1;
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
		this.startTime = Controller.TIME;

		int totalDistance;
		if (this.sEnd == this.sStart)
		{
			totalDistance = this.sStart.getSize() / 4;

			this.milestones.clear();
			this.milestones
					.add(new int[] { this.sStart.getId(), totalDistance });
		} else
		{
			totalDistance = this.sStart.getSize() - traversed;
			// Impatient algorithm for now.
			// TODO Replace with something more efficient
			Ship.pns.clear();
			Ship.nm.clear();

			Node n = new Node(null, sStart, totalDistance);
			nm.put(this.sStart.getId(), n);

			while (n.s != this.sEnd)
			{
				for (Sector s : n.s.getSectorList())
				{
					if (!nm.containsKey(s.getId()))
					{
						Node nNew = new Node(n, s, n.distance);
						nm.put(s.getId(), nNew);
						pns.add(nNew);
					} else if (nm.get(s.getId()).getParentDistance() > n.distance)
					{
						nm.get(s.getId()).setParent(n);
					}
				}

				n = pns.poll();
			}

			n.distance -= n.s.getSize() / 2;

			totalDistance = n.getDistance();

			this.milestones.clear();
			Enqueue(n);
		}
		return totalDistance;
	}

	private void Enqueue(Node n)
	{
		if (n.parent != null)
		{
			Enqueue(n.parent);
		}
		this.milestones.add(new int[] { n.s.getId(),
				n.getDistance() * 100 / this.speed });
	}

	public void InterruptJourney(int wareID)
	{
		if (wareID == this.wi)
		{
			this.fDest.Takeoff(this);

			int remainder = AcquireLocation();
			GenerateDestination();
			LogFlightPlan(remainder);
		}
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

	private class Node extends Object
	{
		public int distance, manhattan;
		private Node parent;
		public Sector s;

		public Node(Node p, Sector s, int dist)
		{
			this.parent = p;
			this.s = s;
			this.distance = dist + s.getSize();

			this.manhattan = Math.abs(s.getCoords()[0] - sEnd.getCoords()[0])
					+ Math.abs(s.getCoords()[1] - sEnd.getCoords()[1]);
		}

		// Called after distance is reduced to change settings in
		// children/reacquire them.
		public void PollChildren()
		{
			for (Sector s : this.s.getSectorList())
			{
				if (Ship.nm.get(s.getId()).getParentDistance() > this.distance)
				{
					Ship.nm.get(s.getId()).setParent(this);
				}
			}
		}

		public int getParentDistance()
		{
			if (this.parent != null)
			{
				return this.parent.distance;
			}
			return 0;
		}

		public int getDistance()
		{
			return this.distance;
		}

		public void setParent(Node parent)
		{
			this.parent = parent;
			this.distance = parent.getDistance() + s.getSize();
			PollChildren();
		}
	}

}
