package alt.termos.integration;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import alt.termos.util.MathUtil;
import alt.termos.util.Plotter;

public class GenTest {

	@Test
	public void test() throws IOException  {

		/*
		List<Double> list = Lists.newArrayList( new ExponentialGenerator(600, 100.0));
		double[] values = toDoubleArray(list.toArray());
		
		for (int i = 0; i != values.length; ++i) {
			values[i] = Math.round(values[i]);
		}
		*/
		
		ExponentialDistribution exponentialDistribution = new ExponentialDistribution(100.0);
		
		double[] values = new double[10000];
		for (int i = 0; i != values.length; ++i) {
			values[i] = FastMath.round(exponentialDistribution.sample());
		}
		
		
		double mean = StatUtils.mean(values);
		System.out.println("mean = " + mean);
		
		double variance = StatUtils.variance(values);
		System.out.println("variance = " + variance);
		System.out.println("sqrt_variance = " + Math.sqrt(variance));
		
		double avg = MathUtil.avg(values);
		System.out.println("avg = " + avg);
		
		double stdev = MathUtil.stdev(values);
		System.out.println("stdev = " + stdev);
		
		double[] freq = new double[600];
		for (int i = 0; i != values.length; ++i) {
			int value = (int) Math.round(values[i]);
			if (value >=0 && value < 600) {
				freq[value] += 1.0;
			}
		}
		
		StringBuilder str = Plotter.chart("freq", new DoubleArrayIterator(freq));
		FileUtils.writeStringToFile(new File("exp.html"), str.toString());


	}

	public static double[] toDoubleArray(Object[] src) {
		double[] des = new double[src.length];
		for (int i = 0; i != src.length; ++i) {
			des[i] = ((Double)src[i]).doubleValue();
		}
		return des;
	}
	
	public static class DoubleArrayIterator implements Iterator<Double> {
		
		private final double[] arr;
		private int i = 0;
		
		public DoubleArrayIterator(double[] arr) {
			this.arr = arr;
		}

		@Override
		public boolean hasNext() {
			return i != arr.length;
		}

		@Override
		public Double next() {
			return arr[i++];
		}

		@Override
		public void remove() {
			throw new IllegalStateException("unsupported operation");
		}
		
		
	}
	
	public static class ExponentialGenerator implements Iterator<Double> {
		
		public static final Random defaultRandom = new Random();
		
		private final double lambda;
		private int counter;
		
		public ExponentialGenerator(int counter) {
			this(counter, 25.0);
		}
		
		public ExponentialGenerator(int counter, double mean) {
			this.counter = counter;
			this.lambda = 1.0 / mean;
		}
		
		@Override
		public boolean hasNext() {
			return counter > 0;
		}

		@Override
		public Double next() {
			counter--;
			return generateExponential(lambda);
		}

		@Override
		public void remove() {
			throw new IllegalStateException("unsupported operation");
		}

		public static double generateExponential(double lambda) {
			return -(Math.log(defaultRandom.nextDouble()) / lambda);
		}

	}
	
}
