
public class Variable {
	private String varID;
	private int value;
	private int [] bitCounter; 
	public boolean wasUsed;
	private int age_in_millis;
	
	public Variable(String name, int val) {
	//	bitCounter = new int[8];
		bitCounter = new int[] {0,0,0,0,0,0,0,0};
		wasUsed = false;
		age_in_millis = 0;
		varID = name;
		value = val;
	}
	
	public void aging() {
		for(int i = 6; i >= 0; i--) {
			bitCounter[i+1] = bitCounter[i];
		}
		if(wasUsed) {
			bitCounter[0] = 1;
		} else {
			bitCounter[0] = 0;
		}
		wasUsed = false;
	}
	
	public double getBitCounterValue() {
		double bitCounterValue = 0;
		int counter = 1;
		for(int i = 7; i >= 0; i--) {
			if(bitCounter[i] == 1) {
				bitCounterValue += Math.pow(2, (7-i));
			}
		}
		
		return bitCounterValue;
		
	}

	public String getVarID() {
		return varID;
	}

	public void setVarID(String varID) {
		this.varID = varID;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int[] getBitCounter() {
		return bitCounter;
	}

	public void setBitCounter(int[] bitCounter) {
		this.bitCounter = bitCounter;
	}
	/*
	@Override
	public String toString() {
		return getVarID()
	}
	*/
	
}
