package alt.termos.util;


/**
 * 
 * @author Albert Shift
 *
 */
public class MathUtil {

	public static double avg(double[] values) {
		double sum = 0.0;
		for (int i = 0; i != values.length; ++i) {
			double value = values[i];
			sum += value;
		}
		double avg = sum / values.length;
		return avg;
	}

	public static double stdev(double[] values) {
		return stdev(values, avg(values));
	}

	public static double stdev(double[] values, double avg) {
		double sum = 0.0;
		for (int i = 0; i != values.length; ++i) {
			double value = values[i];
			value -= avg;
			value *= value;
			sum += value;
		}
		double stdev = Math.sqrt(sum / values.length);
		return stdev;
	}

}
