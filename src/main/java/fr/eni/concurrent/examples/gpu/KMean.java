package fr.eni.concurrent.examples.gpu;

import java.util.List;

public class KMean {
	private final List<double[]> vectors;
	private final List<double[]> centers;
	private final Distance distance;
	private final int[] assign;
	private final int vectorSize;
	
	public KMean(List<double[]> vectors, List<double[]> centers, Distance distance) {
		this.centers = centers;
		this.vectors = vectors;
		this.distance = distance;
		assign = new int[vectors.size()];
		vectorSize = vectors.get(0).length;
	}
	
	/**
	 * Search which center is the closest to a given point
	 * @return
	 * Center assignment
	 */
	public int[] step1() {
		int vectorIndex = 0;
		for(double[] v: vectors) {
			int centerIndex = 0;
			double minD = Double.POSITIVE_INFINITY;
			int minCenterIndex = 0;
			for(double[] c: centers) {
				double d = distance.d(v, c);
				if (d < minD) {
					minD = d;
					minCenterIndex = centerIndex;
				}
				
				centerIndex++;
			}
			assign[vectorIndex++] = minCenterIndex;
		}
		
		return assign;
	}
	
	
	
	/**
	 * Compute mean average of points belonging to a center. The result is the new center.
	 */
	public List<double[]> step2() {
		final int nbCenters = centers.size();
		final double[][] newCenters = new double[centers.size()][vectorSize];
		final int[] nbElements = new int[nbCenters];
		
		/*
		 * Add vectors 
		 */
		int nbVectors = vectors.size();
		for(int i=0; i<nbVectors; i++) {
			double[] v = vectors.get(i);
			int index = assign[i];
			
			nbElements[index]++;
			double[] c = newCenters[index];
			for(int j=0; j<vectorSize; j++) {
				c[j] += v[j];
			}
		}
		
		/*
		 * Average
		 */
		centers.clear();
		for(int i=0; i<nbCenters; i++) {
			if(nbElements[i]==0) {
				continue;
			}
			
			final double[] c = newCenters[i];
			final double f = 1.0/nbElements[i];
			for(int j=0; j<vectorSize; j++) {
				c[j] *= f;
			}
			centers.add(c);
		}
		
		return centers;
	}
	
	public interface Distance {
		public double d(double[] v, double[] c);
	}
}
