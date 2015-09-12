package alt.termos.util;

public final class DoubleShortter {

	public static double format(double val) {
	
		double aval = Math.abs(val);
		
		for (double i = 1000; ; i /= 10.0) {
			if (aval > i || i < 0.1) {
				double m = 1000.0 / i;
				return Math.round(val * m) / m;
			}
		}

	}
	
	public static void append(StringBuilder str, double val) {
		
		if (val < 0.0) {
			str.append('-');
			val *= -1.0;
		}
		final int precision = 5;		
		
	    double mval = val * 100000.0;
	    long lval = (long) mval;
	    
	    String sval = Long.toString(lval);
	    ZeroBufferedStringBuilder buff = new ZeroBufferedStringBuilder(str);

	    if (sval.length() <= precision) {
	    	buff.append('0');
	    	buff.append('.');
	    	for (int i = 0; i != precision-sval.length(); ++i) {
	    		buff.append('0');
	    	}
		    for (int i = 0; i != sval.length(); ++i) {
		    	buff.append(sval.charAt(i));
		    }
	    }
	    else {
		    for (int i = 0; i != sval.length(); ++i) {
		    	if (sval.length() - i == precision) {
		    		buff.append('.');
		    	}
		    	buff.append(sval.charAt(i));
		    }
	    }
	    

	}
	
	private final static class ZeroBufferedStringBuilder {
		
		private StringBuilder str;
		private boolean dot = false;
		private boolean firstDot = true;
		private int zeros = 0;
		
		public ZeroBufferedStringBuilder(StringBuilder str) {
			this.str = str;
		}
		
		void append(char ch) {
			if (dot) {
				if (ch == '0') {
					zeros++;
					return;
				}
				if (firstDot) {
					str.append('.');
					firstDot = false;
				}
				while(zeros > 0) {
					str.append('0');
					zeros--;
				}
			}
			if (ch == '.') {
				dot = true;
				if (firstDot) {
					return;
				}
			}
			str.append(ch);
		}
		
	}
	
}
