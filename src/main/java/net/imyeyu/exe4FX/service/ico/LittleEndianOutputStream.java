package net.imyeyu.exe4FX.service.ico;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 夜雨 创建于 2021-06-08 20:03
 */
public class LittleEndianOutputStream extends DataOutputStream {

	public LittleEndianOutputStream(OutputStream out) {
		super(out);
	}

	public void writeShortLE(short value) throws IOException {
		value = EndianUtils.swapShort(value);
		super.writeShort(value);
	}

	public void writeIntLE(int value) throws IOException {
		value = EndianUtils.swapInteger(value);
		super.writeInt(value);
	}
}