public class Factory {

	private int[][] resources;
	private int template;
	private char size;
	private char race;
	private String name;

	public Factory(int[][] res, int t, char s, String n) {
		this.resources = res.clone();
		this.template = t;
		this.size = s;
		this.name = n;
	}

	public Factory(int defaultType, char race, char size) {
		Factory factoryTemplate = Controller.MASTER_FACTORY_LIST
				.get(defaultType);
		this.resources = factoryTemplate.getResources().clone();
		this.race = race;
		this.size = size;
		this.template = defaultType;

		CorrectRacialResources();

		if (size != factoryTemplate.getSize()) {
			int multiplier = size == 'L' ? 5 : 10;
			AdjustResources(multiplier, factoryTemplate.getSize() == 'M');
		}

	}

	private void CorrectRacialResources() {
		// TODO Implement racial resources

	}

	private void AdjustResources(int multiplier, boolean divide) {
		int divisor = divide ? 2 : 1;
		for (int i = 0; i < this.resources.length; i++) {
			this.resources[i][1] = this.resources[i][1] * multiplier / divisor;
		}
		// TODO Implement yield calculations
	}

	public String toString() {
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
		for (int i = 0; i < this.resources.length; i++) {
			s = String.format("\t\t%s: %d\n",
					Controller.MASTER_RESOURCE_LIST.get(this.resources[i][0]),
					this.resources[i][1]);
			sb.append(s);
		}
		return sb.toString();
	}

	public int[][] getResources() {
		return resources;
	}

	public int getTemplate() {
		return template;
	}

	public char getSize() {
		return size;
	}

	public char getRace() {
		return race;
	}

	public String getName() {
		return name;
	}
}
