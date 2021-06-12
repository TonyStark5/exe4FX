package net.imyeyu.exe4FX.service.ico;

/**
 *
 *
 * 夜雨 创建于 2021-06-08 20:03
 */
public class EndianUtils {

	public static short swapShort(short value) {
		return (short) (((value & 0xFF00) >> 8) | ((value & 0x00FF) << 8));
	}

	public static int swapInteger(int value) {
		return ((value & 0xFF000000) >> 24) | ((value & 0x00FF0000) >> 8) | ((value & 0x0000FF00) << 8) | ((value & 0x000000FF) << 24);
	}

	public static long swapLong(long value) {
		return ((value & 0xFF00000000000000L) >> 56) | ((value & 0x00FF000000000000L) >> 40) | ((value & 0x0000FF0000000000L) >> 24) | ((value & 0x000000FF00000000L) >> 8) | ((value & 0x00000000FF000000L) << 8) | ((value & 0x0000000000FF0000L) << 24) | ((value & 0x000000000000FF00L) << 40) | ((value & 0x00000000000000FFL) << 56);
	}

	public static float swapFloat(float value) {
		int i = Float.floatToIntBits(value);
		i = swapInteger(i);
		return Float.intBitsToFloat(i);
	}

	public static double swapDouble(double value) {
		long l = Double.doubleToLongBits(value);
		l = swapLong(l);
		return Double.longBitsToDouble(l);
	}
}