package edu.stanford.math.plex4.homology.barcodes;

import edu.stanford.math.plex4.utility.ExceptionUtility;
import edu.stanford.math.plex4.utility.Infinity;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * This class implements functionality for storing a collection of barcodes. 
 * It is designed to be the output of a persistent homology or cohomology algorithm
 * so that it contains the persistence intervals at each dimension.
 * 
 * @author Andrew Tausz
 *
 */
public class BarcodeCollection {
	private final TIntObjectHashMap<Barcode> barcodeMap = new TIntObjectHashMap<Barcode>();
	
	public BarcodeCollection getInfiniteIntervals() {
		BarcodeCollection collection = new BarcodeCollection();
		
		for (TIntObjectIterator<Barcode> iterator = this.barcodeMap.iterator(); iterator.hasNext(); ) {
			iterator.advance();
			collection.barcodeMap.put(iterator.key(), iterator.value().getInfiniteIntervals());
		}
		
		return collection;
	}
	
	/**
	 * This function adds an interval at the specified dimension.
	 * 
	 * @param dimension the dimension to add to
	 * @param interval the interval to add
	 */
	public void addInterval(int dimension, HalfOpenInterval interval) {
		ExceptionUtility.verifyNonNull(interval);
		if (!this.barcodeMap.containsKey(dimension)) {
			this.barcodeMap.put(dimension, new Barcode(dimension));
		}
		this.barcodeMap.get(dimension).addInterval(interval);
	}
	
	/**
	 * This function adds the specified finite interval [start, end] at the
	 * supplied dimension.
	 * 
	 * @param dimension the dimension to add to
	 * @param start the starting point of the interval
	 * @param end the ending point of the interval
	 */
	public void addInterval(int dimension, double start, double end) {
		this.addInterval(dimension, new FiniteInterval(start, end));
	}
	
	/**
	 * This function adds the specified semi-infinite interval [start, infinity)
	 * at the supplied dimension.
	 * 
	 * @param dimension the dimension to add to
	 * @param start the starting point of the interval
	 */
	public void addRightInfiniteInterval(int dimension, double start) {
		this.addInterval(dimension, new RightInfiniteInterval(start));
	}
	
	public void addLeftInfiniteInterval(int dimension, double end) {
		this.addInterval(dimension, new LeftInfiniteInterval(end));
	}
	
	/**
	 * This function computes the Betti numbers for a particular filtration
	 * value. It returns the results as a map which maps the dimension to the
	 * Betti number.
	 * 
	 * @param filtrationValue the filtrationValue to compute the Betti numbers at
	 * @return a TIntIntHashMap mapping dimension to the Betti number
	 */
	public TIntIntHashMap getBettiNumbers(double filtrationValue) {
		TIntIntHashMap map = new TIntIntHashMap();
		
		for (TIntObjectIterator<Barcode> iterator = this.barcodeMap.iterator(); iterator.hasNext(); ) {
			iterator.advance();
			map.put(iterator.key(), iterator.value().getSliceCardinality(filtrationValue));
		}
		
		return map;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (TIntObjectIterator<Barcode> iterator = this.barcodeMap.iterator(); iterator.hasNext(); ) {
			iterator.advance();
			builder.append(iterator.value().toString());
			builder.append("\n");
		}
		
		return builder.toString();
	}
	
	public String getBettiNumbers() {
		StringBuilder builder = new StringBuilder();
		int maxDimension = Infinity.Int.getNegativeInfinity();
		int minDimension = Infinity.Int.getPositiveInfinity();
		
		for (TIntObjectIterator<Barcode> iterator = this.barcodeMap.iterator(); iterator.hasNext(); ) {
			iterator.advance();
			maxDimension = Math.max(maxDimension, iterator.key());
			minDimension = Math.min(minDimension, iterator.key());
		}
		
		int[] bettiNumbers = new int[maxDimension - minDimension + 1];
		
		for (TIntObjectIterator<Barcode> iterator = this.barcodeMap.iterator(); iterator.hasNext(); ) {
			iterator.advance();
			bettiNumbers[iterator.key() - minDimension] = iterator.value().getCardinality();
		}
		
		builder.append("{");
		
		for (int i = 0; i < bettiNumbers.length; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append((i + minDimension) + ": " + bettiNumbers[i]);
		}
		
		builder.append("}");
		
		return builder.toString();
	}
	
	public String getSliceBettiNumbers(double point) {
		StringBuilder builder = new StringBuilder();
		int maxDimension = Infinity.Int.getNegativeInfinity();
		int minDimension = Infinity.Int.getPositiveInfinity();
		
		for (TIntObjectIterator<Barcode> iterator = this.barcodeMap.iterator(); iterator.hasNext(); ) {
			iterator.advance();
			maxDimension = Math.max(maxDimension, iterator.key());
			minDimension = Math.min(minDimension, iterator.key());
		}
		
		int[] bettiNumbers = new int[maxDimension - minDimension + 1];
		
		for (TIntObjectIterator<Barcode> iterator = this.barcodeMap.iterator(); iterator.hasNext(); ) {
			iterator.advance();
			bettiNumbers[iterator.key() - minDimension] = iterator.value().getSliceCardinality(point);
		}
		
		builder.append("{");
		
		for (int i = 0; i < bettiNumbers.length; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append((i + minDimension) + ": " + bettiNumbers[i]);
		}
		
		builder.append("}");
		
		return builder.toString();
	}
}
