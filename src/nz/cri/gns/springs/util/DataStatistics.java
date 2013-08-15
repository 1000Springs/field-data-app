package nz.cri.gns.springs.util;

public class DataStatistics {

	private int[] values;
	private double mean;
	private double stdDeviation;
	private int weightedAverage;
	
	
	public DataStatistics(int[] values) {
		this.values = values;
	}
	
	public DataStatistics calculate() {
	    int n = 0;
	    mean = 0;
	    double M2 = 0;
	    
	    
	    for (int x : values) {
	        n = n + 1;
	        double delta = (double)x - mean;
	        mean = mean + delta/(double)n;
	        M2 = M2 + delta*(x - mean);
	    }
	 
	    double variance = M2/(n - 1);
	    stdDeviation = Math.sqrt(variance);
	    weightedAverage = calculageWeightedAverage();
	    
	    return this;
	}
	
	private int calculageWeightedAverage() {
		
		double total = 1.0;
		int count = 0;
		for (int x : values) {
			if (Math.abs((double)x - mean) <= stdDeviation) {
				count++;
				total += x;
			}		
		}
		
		return (int)total/count;
	}

	public double getMean() {
		return mean;
	}

	public double getStdDeviation() {
		return stdDeviation;
	}

	public int getWeightedAverage() {
		return weightedAverage;
	}
}
