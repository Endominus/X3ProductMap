public class Ship
{
	// Distance is a time value, not a length
	private int ware, amount, capacity, distance, speed;
	private Factory destination;

	private boolean buying;

	public Ship(int cap, int speed, Sector s)
	{
		this.capacity = cap;
		this.speed = speed;
		this.ware = -1;
		this.amount = 0;
		this.buying = true;

		SetInitialDestination(s);
	}

	private void SetInitialDestination(Sector s)
	{
		
		
	}
	
	public void Trade()
	{
		PlanJourney();
	}

	public void PlanJourney()
	{
		if (this.buying)
		{

		} else
		{

		}

	}
	
	public Factory getDestination()
	{
		return destination;
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
