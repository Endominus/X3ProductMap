import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Sector
{

	private String name;
	private ArrayList<Factory> factoryList = new ArrayList<>();
	private ArrayList<Sector> sectorList = new ArrayList<>();
	// private ArrayList<Double[]> resourcesInTransit = new ArrayList<>();
	private HashMap<Integer, Double> resourceDemand = new HashMap<>();
	private HashMap<Integer, Double> resourceSupply = new HashMap<>();
	private HashMap<Integer, Double> resourceStockpile = new HashMap<>();
	private int distance, id, size;
	private int[] coords;

	// TODO Change this to a struct to find trends more easily
	// Need avg delta in addition to raw average
	private HashMap<Integer, Double> avgStockpile = new HashMap<>();

	public Sector(String n, int id, int size, int x, int y)
	{
		this.name = n;
		this.id = id;
		this.size = size;
		this.coords = new int[] { x, y };
	}

	public void ProduceGoods(int interval)
	{
		for (Factory f : this.factoryList)
		{
			f.Produce(interval);
		}
	}

	/***
	 * The net flow (ideal) of a good from this sector.
	 * 
	 * @param id
	 *            of good
	 * @return net flow if production and consumption were maximized
	 */
	public double NetFlow(int id)
	{
		double demand = 0;
		double supply = 0;

		if (this.resourceDemand.containsKey(id))
		{
			demand = this.resourceDemand.get(id);
		}
		if (this.resourceSupply.containsKey(id))
		{
			supply = this.resourceSupply.get(id);
		}
		return supply + demand;
	}

	public double DemandFactor(int id)
	{
		if (!this.resourceDemand.containsKey(id))
			return 0;
		double demand = -8 * this.resourceDemand.get(id);
		double stock = this.resourceStockpile.get(id);

		return Math.max(0, (demand - stock) / demand);
	}

	public double SupplyFactor(int id)
	{
		if (!this.resourceSupply.containsKey(id))
			return 0;
		double supply = 8 * this.resourceSupply.get(id);
		double stock = this.resourceStockpile.get(id);

		return Math.max(0, (supply - stock) / supply);
	}

	public void TakeCensus()
	{
		for (Entry<Integer, Double> res : this.resourceStockpile.entrySet())
		{
			avgStockpile.put(res.getKey(), avgStockpile.get(res.getKey())
					+ (res.getValue() / Controller.CAP_MULT));
		}
	}
	
	public void RegulateCensus(int div)
	{
		for (Entry<Integer, Double> res : this.avgStockpile.entrySet())
		{
			avgStockpile.put(res.getKey(), res.getValue() / div);
		}
	}

	public String toString()
	{
		int key;
		double value;
		String s;
		StringBuilder sb = new StringBuilder();

		s = "Sector Name: " + this.name + "\n";
		sb.append(s);
		s = "Attached Sectors:\n";
		sb.append(s);
		// for (i = 0; i < this.sectorList.size(); i++)
		for (Sector sect : this.sectorList)
		{
			sb.append("\t" + sect.getName() + "\n");
		}
		// for (i = 0; i < this.factoryList.size(); i++)
		for (Factory fact : this.factoryList)
		{
			sb.append(fact.toString());
		}
		sb.append("Aggregate Resource Supply:\n");
		for (Entry<Integer, Double> entry : this.resourceSupply.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();
			if (this.resourceDemand.containsKey(key)
					&& value + this.resourceDemand.get(key) > 0)
			{
				value += this.resourceDemand.get(key);
			}

			sb.append(String.format("\t%s: %f\n",
					Controller.MASTER_RESOURCE_LIST.get(key), value));
		}
		sb.append("Aggregate Resource Demand:\n");
		for (Entry<Integer, Double> entry : this.resourceDemand.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();
			if (this.resourceSupply.containsKey(key))
			{
				if (value + this.resourceSupply.get(key) < 0)
				{
					value += this.resourceSupply.get(key);
				} else
				{
					continue;
				}
			}

			sb.append(String.format("\t%s: %f\n",
					Controller.MASTER_RESOURCE_LIST.get(key), value));
		}

		return sb.toString();
	}

	public void AddFactory(Factory fact)
	{
		this.factoryList.add(fact);
		double[][] res = fact.getResources();
		this.resourceSupply
				.put((int) res[0][0],
						res[0][1]
								+ (this.resourceSupply.containsKey(res[0][0]) ? this.resourceSupply
										.get(res[0][0]) : 0));
		if (!this.resourceStockpile.containsKey(res[0][0]))
		{
			this.resourceStockpile.put((int) res[0][0], 0.0);
			this.avgStockpile.put((int) res[0][0], 0.0);
		}
		for (int i = 1; i < res.length; i++)
		{
			this.resourceDemand
					.put((int) res[i][0],
							res[i][1]
									+ (this.resourceDemand
											.containsKey(res[i][0]) ? this.resourceDemand
											.get(res[i][0]) : 0));
			if (!this.resourceStockpile.containsKey(res[i][0]))
				this.resourceStockpile.put((int) res[i][0], 0.0);
		}
	}

	public void AddSector(Sector sect)
	{
		if (!this.sectorList.contains(sect))
			this.sectorList.add(sect);
	}

	public String getName()
	{
		return name;
	}

	public int[] getCoords()
	{
		return this.coords;
	}

	public HashMap<Integer, Double> getResourceSupply()
	{
		return resourceSupply;
	}

	/*
	 * public void Pulse() { DistributeResources(); ProduceGoods();
	 * DisseminateProducts(); ReceiveShipments(); }
	 */

	/*
	 * public void ReceiveShipments() { double[] res; for (int i = 0; i <
	 * this.resourcesInTransit.size(); i++) { if
	 * (this.resourcesInTransit.get(i)[0] == 0) { res =
	 * this.resourcesInTransit.get(i); this.resourceStockpile.put(res[1],
	 * this.resourceStockpile.get(res[1]) + res[2]);
	 * this.resourcesInTransit.remove(i); i--; } else {
	 * this.resourcesInTransit.get(i)[0]--; } } }
	 * 
	 * private void DisseminateProducts() { Queue<Sector> q = new
	 * LinkedList<>(); ArrayList<Sector> traversed = new ArrayList<>(); Sector
	 * s; HashMap<Integer, Double> localDemand = new HashMap<>(); int key;
	 * double value, transferAmount; boolean quit = false;
	 * 
	 * q.addAll(this.sectorList); q.forEach((sect) -> sect.setDistance(1));
	 * 
	 * while (!q.isEmpty() && !quit) { s = q.poll(); localDemand = s.Demand();
	 * 
	 * for (Entry<Integer, Double> res : this.resourceSupply .entrySet()) { key
	 * = res.getKey(); value = res.getValue(); if (localDemand.containsKey(key))
	 * { transferAmount = Math.min(localDemand.get(key), value);
	 * s.SendShipment(new double[] { s.getDistance(), key, transferAmount } );
	 * res.setValue(value - transferAmount); } }
	 * 
	 * if (s.getDistance() < Controller.MAX_TRAVEL_DISTANCE) { //Watch this;
	 * expecting a bug from wrong distance reporting s.sectorList.forEach((sect)
	 * -> { if (sect.getDistance() == 0) { sect.setDistance(this.getDistance() +
	 * 1); q.add(sect); traversed.add(sect); } }); }
	 * 
	 * quit = true; for (int res : this.resourceStockpile.values()) { if (res >
	 * 0) { quit = false; break; } } }
	 * 
	 * for (Sector sect : traversed) { sect.setDistance(0); } }
	 * 
	 * private void SendShipment(double[] ds) { this.resourcesInTransit.add(ds);
	 * 
	 * }
	 * 
	 * private void DistributeResources() { ArrayList<double[]> allDemands = new
	 * ArrayList<>(); double[] demand; double totalDemand, portion;
	 * 
	 * for (Entry<Integer, Integer> res : this.resourceStockpile.entrySet()) {
	 * totalDemand = 0; for (int i = 0; i < this.factoryList.size(); i++) {
	 * demand = this.factoryList.get(i).DemandRatio(res.getKey()); if (demand[0]
	 * == 1) continue; totalDemand += demand[1]; allDemands.add(new double[] {
	 * i, demand[1] }); }
	 * 
	 * for (double[] pair : allDemands) { portion = res.getValue() * pair[1] /
	 * totalDemand; this.factoryList.get((int) pair[0]).AddToStockpile( new
	 * int[] { res.getKey(), (int) portion }); }
	 * 
	 * res.setValue(0); } }
	 */

	public int getDistance()
	{
		return distance;
	}

	public void setDistance(int distance)
	{
		this.distance = distance;
	}

	public ArrayList<Factory> getFactoryList()
	{
		return factoryList;
	}

	public ArrayList<Sector> getSectorList()
	{
		return sectorList;
	}

	/*
	 * public ArrayList<int[]> getResourcesInTransit() { return
	 * resourcesInTransit; }
	 */

	public HashMap<Integer, Double> getResourceStockpile()
	{
		this.resourceStockpile.entrySet().forEach((a) -> a.setValue(0.0));

		for (Factory f : this.factoryList)
		{
			for (double[] res : f.getStockpile())
			{
				double value = this.resourceStockpile.get((int) res[0])
						+ res[1];
				this.resourceStockpile.put((int) res[0], value);
			}
		}

		return resourceStockpile;
	}

	public HashMap<Integer, Double> getResourceDemand()
	{
		return this.resourceDemand;
	}
	
	public HashMap<Integer, Double> getAvgStockpile()
	{
		return this.avgStockpile;
	}

	public int getId()
	{
		return this.id;
	}

	public int getSize()
	{
		return size;
	}
}
