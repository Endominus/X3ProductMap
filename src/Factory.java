import java.util.ArrayList;
import java.util.Arrays;

public class Factory
{

	private double[][] resDemanded;
	private double[][] resStockpiled;
	private int template;
	private char size;
	private char race;
	private String name;
	private ArrayList<Ship> inboundShips = new ArrayList<>();

	public Factory(double[][] res, int t, char s, String n)
	{
		this.resDemanded = Arrays.copyOf(res, res.length);
		this.template = t;
		this.size = s;
		this.name = n;
	}

	public Factory(int defaultType, char race, char size)
	{
		Factory factoryTemplate = Controller.MASTER_FACTORY_LIST
				.get(defaultType);
		double[][] resources = factoryTemplate.getResources().clone();
		this.resDemanded = new double[resources.length][2];
		this.resStockpiled = new double[resources.length][2];
		for (int i = 0; i < resources.length; i++)
		{
			this.resDemanded[i] = resources[i].clone();
			this.resStockpiled[i] = resources[i].clone();
			this.resStockpiled[i][1] = 0;
		}
		
		this.race = race;
		this.size = size;
		this.template = defaultType;

		CorrectRacialResources();

		if (size != factoryTemplate.getSize())
		{
			int multiplier = size == 'L' ? 5 : 10;
			AdjustResources(multiplier, factoryTemplate.getSize() == 'M');
		}

	}

	public Factory(ArrayList<double[]> io, int t, char s, String n)
	{
		this.resDemanded = new double[io.size()][2];
		int i = 0;
		for (double[] res : io)
		{
			this.resDemanded[i++] = res;
		}
		this.template = t;
		this.size = s;
		this.name = n;
	}

	private void CorrectRacialResources()
	{
		for (int i = 1; i < this.resDemanded.length; i++)
		{
			if (this.resDemanded[i][0] == -1)
			{
				switch (this.race)
				{
				case 'A':
					this.resDemanded[i][0] = 47;
					this.resDemanded[i][1] = -600;
					break;
				case 'B':
					this.resDemanded[i][0] = 23;
					this.resDemanded[i][1] = -150;
					break;
				case 'P':
					this.resDemanded[i][0] = 62;
					this.resDemanded[i][1] = -120;
					break;
				case 'S':
					this.resDemanded[i][0] = 57;
					this.resDemanded[i][1] = -90;
					break;
				case 'T':
					this.resDemanded[i][0] = 53;
					this.resDemanded[i][1] = -600;
					break;
				}
				break;
			}
		}
	}

	private void AdjustResources(int multiplier, boolean divide)
	{
		int divisor = divide ? 2 : 1;
		for (int i = 0; i < this.resDemanded.length; i++)
		{
			this.resDemanded[i][1] = this.resDemanded[i][1] * multiplier
					/ divisor;
		}
		// TODO Implement yield calculations
	}

	public double[][] Produce()
	{
		double[] prod = this.resDemanded[0];
		double ratio = SatisfactionRatio();
		prod[1] *= ratio;
		ConsumeResources(ratio);

		AddToStockpile(prod);
		return this.resStockpiled;
	}

	private void ConsumeResources(double ratio)
	{
		for (int i = 1; i < this.resStockpiled.length; i++)
		{
			this.resStockpiled[i][1] += this.resDemanded[i][1] * ratio;
		}
	}

	public void AddToStockpile(double[] prod)
	{
		for (double[] dem : this.resStockpiled)
		{
			if (prod[0] == dem[0])
			{
				dem[1] += prod[1];
				break;
			}
		}
	}

	private double SatisfactionRatio()
	{
		double ratio = 1, temp;
		for (int i = 1; i < this.resDemanded.length; i++)
		{
			temp = (-1.0) * this.resStockpiled[i][1] / this.resDemanded[i][1];
			ratio = Math.min(ratio, temp);
		}

		return ratio;
	}

	public double[] DemandRatio(int id)
	{
		double ratio = 1;
		int i;
		for (i = 1; i < this.resDemanded.length; i++)
		{
			if (this.resDemanded[i][0] == id)
			{
				ratio = (-1.0) * this.resStockpiled[i][1]
						/ this.resDemanded[i][1];
				break;
			}
		}

		return new double[] { ratio,
				this.resStockpiled[i][0] + this.resDemanded[i][0] };
	}

	public String toString()
	{
		String s;
		StringBuilder sb = new StringBuilder();

		s = "Factory type: "
				+ Controller.MASTER_FACTORY_LIST.get(this.template).getName()
				+ "\n";
		sb.append(s);
		s = "\tSize: " + this.size + "\n";
		sb.append(s);
		s = "\tRace: " + this.race + "\n";
		sb.append(s);
		sb.append("\tResources:\n");
		for (int i = 0; i < this.resDemanded.length; i++)
		{
			s = String
					.format("\t\t%s: %f\n", Controller.MASTER_RESOURCE_LIST
							.get(this.resDemanded[i][0]),
							this.resDemanded[i][1]);
			sb.append(s);
		}
		return sb.toString();
	}
	
	public void RequestDocking(Ship s)
	{
		this.inboundShips.add(s);
	}
	
	public void Takeoff(Ship s)
	{
		this.inboundShips.remove(s);
	}

	public double[][] getResources()
	{
		return resDemanded;
	}
	
	public double[][] getStockpile()
	{
		return resDemanded;
	}

	public int getTemplate()
	{
		return template;
	}

	public char getSize()
	{
		return size;
	}

	public char getRace()
	{
		return race;
	}

	public String getName()
	{
		return name;
	}
	
	public void Transfer(int wareLocation, int wareAmount)
	{
		this.resStockpiled[wareLocation][1] += wareAmount;		
	}

	public void NotifyDockingQueue(int ware)
	{
		for (Ship s : this.inboundShips)
		{
			s.InterruptJourney(ware);
		}
	}
}
