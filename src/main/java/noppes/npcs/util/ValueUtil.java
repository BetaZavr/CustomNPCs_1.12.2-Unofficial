package noppes.npcs.util;

public class ValueUtil {

	// New
	public static double correctDouble(double given, double min, double max) {
		if (given < min) {
			return min;
		}
		if (given > max) {
			return max;
		}
		return given;
	}

	public static float correctFloat(float given, float min, float max) {
		if (given < min) {
			return min;
		}
		if (given > max) {
			return max;
		}
		return given;
	}

	public static int correctInt(int given, int min, int max) {
		if (given < min) {
			return min;
		}
		if (given > max) {
			return max;
		}
		return given;
	}

	public static double max(double... obj) {
		if (obj == null || obj.length == 0) {
			return Double.MAX_VALUE;
		}
		double max = obj[0];
		for (double i : obj) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}

	public static double max(float... obj) {
		if (obj == null || obj.length == 0) {
			return Float.MAX_VALUE;
		}
		float max = obj[0];
		for (float i : obj) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}

	public static int max(int... obj) {
		if (obj == null || obj.length == 0) {
			return Integer.MAX_VALUE;
		}
		int max = obj[0];
		for (int i : obj) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}

	public static double min(double... obj) {
		if (obj == null || obj.length == 0) {
			return Double.MIN_VALUE;
		}
		double min = obj[0];
		for (double i : obj) {
			if (i < min) {
				min = i;
			}
		}
		return min;
	}

	public static double min(float... obj) {
		if (obj == null || obj.length == 0) {
			return Float.MIN_VALUE;
		}
		float min = obj[0];
		for (float i : obj) {
			if (i < min) {
				min = i;
			}
		}
		return min;
	}

	public static int min(int... obj) {
		if (obj == null || obj.length == 0) {
			return Integer.MIN_VALUE;
		}
		int min = obj[0];
		for (int i : obj) {
			if (i < min) {
				min = i;
			}
		}
		return min;
	}

}
