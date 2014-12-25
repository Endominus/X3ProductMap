import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Sector
{

	private String name;
	private ArrayList<Factory> factoryList = new ArrayList<>();
	private ArrayList<Sector> sectorList = new ArrayList<>();
	// private ArrayList<Integer[]> resourcesInTransit = new ArrayList<>();
	private HashMap<Integer, Integer> resourceDemand = new HashMap<>();
	private HashMap<Integer, Integer> resourceSupply = new HashMap<>();
	private HashMap<Integer, Integer> resourceStockpile = new HashMap<>();

	public Sector(String n)
	{
		this.name = n;
	}

	public int NetFlow(int id)
	{
		int demand = 0;
		int supply = 0;

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
		return (-1.0) * this.resourceSupply.get(id)
				/ this.resourceDemand.get(id);
	}

	// The current resources demanded by this sector (resourceDemand -
	// resourceStockpile)
	public HashMap<Integer, Integer> Demand()
	{
		HashMap<Integer, Integer> toReturn = new HashMap<>();
		int key, value;
		for (Entry<Integer, Integer> entry : this.resourceDemand.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue() - this.resourceStockpile.get(key);
			toReturn.put(key, value);
		}
		return toReturn;
	}

	public String toString()
	{
		int key, value;
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
		for (Entry<Integer, Integer> entry : this.resourceSupply.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();
			if (this.resourceDemand.containsKey(key)
					&& value + this.resourceDemand.get(key) > 0)
			{
				value += this.resourceDemand.get(key);
			}

			sb.append(String.format("\t%s: %d\n",
					Controller.MASTER_RESOURCE_LIST.get(key), value));
		}
		sb.append("Aggregate Resource Demand:\n");
		for (Entry<Integer, Integer> entry : this.resourceDemand.entrySet())
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

			sb.append(String.format("\t%s: %d\n",
					Controller.MASTER_RESOURCE_LIST.get(key), value));
		}

		return sb.toString();
	}

	public void AddFactory(Factory fact)
	{
		this.factoryList.add(fact);
		int[][] res = fact.getResources();
		this.resourceSupply
				.put(res[0][0],
						res[0][1]
								+ (this.resourceSupply.containsKey(res[0][0]) ? this.resourceSupply
										.get(res[0][0]) : 0));
		for (int i = 1; i < res.length; i++)
		{
			this.resourceDemand
					.put(res[i][0],
							res[i][1]
									+ (this.resourceDemand
											.containsKey(res[i][0]) ? this.resourceDemand
											.get(res[i][0]) : 0));
			if (!this.resourceStockpile.containsKey(res[i][0]))
				this.resourceStockpile.put(res[i][0], 0);
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

	public HashMap<Integer, Integer> getResourceSupply()
	{
		return resourceSupply;
	}

	public void Pulse()
	{
		DistributeResources();
		ProduceGoods();
		//TODO Implement searching for and dissemination of goods
		
	}
	
	private void DistributeResources()
	{
		ArrayList<double[]> allDemands = new ArrayList<>();
		double[] demand;
		double totalDemand, portion;
		
		for (Entry<Integer, Integer> res : this.resourceStockpile.entrySet())
		{
			totalDemand = 0;
			for (int i = 0; i < this.factoryList.size(); i++)
			{
				demand = this.factoryList.get(i).DemandRatio(res.getKey());
				if (demand[0] == 1)
					continue;
				totalDemand += demand[1];
				allDemands.add(new double[] { i, demand[1] });
			}

			for (double[] pair : allDemands)
			{
				portion = res.getValue() * pair[1] / totalDemand;
				this.factoryList.get((int) pair[0]).AddToStockpile(
						new int[] { res.getKey(), (int) portion });
			}
			
			res.setValue(0);
		}
	}
	
	private void ProduceGoods()
	{
		int[][] production;
		
		for (Factory f : this.factoryList)
		{
			production = f.Produce();
			for (int[] res : production)
			{
				this.resourceStockpile.put(res[0], this.resourceStockpile.get(res[0]) + res[1]);
			}
		}
	}
}
