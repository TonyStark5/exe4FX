package net.imyeyu.exe4FX.service.ico;

import java.io.IOException;

/**
 * 夜雨 创建于 2021-06-08 20:09
 */
public class IconEntry {

	public int bWidth;
	public int bHeight;
	public int bColorCount;
	public byte bReserved;
	public short sPlanes;
	public short sBitCount;
	public int iSizeInBytes;
	public int iFileOffset;

	public IconEntry() {
		bWidth = 0;
		bHeight = 0;
		bColorCount = 0;
		sPlanes = 1;
		bReserved = 0;
		sBitCount = 0;
		iSizeInBytes = 0;
		iFileOffset = 0;
	}

	public String toString() {
		return "width=" + bWidth + ",height=" + bHeight + ",bitCount=" + sBitCount + ",colorCount=" + bColorCount;
	}

	public void write(LittleEndianOutputStream out) throws IOException {
		out.writeByte(bWidth);
		out.writeByte(bHeight);
		out.writeByte(bColorCount);
		out.writeByte(bReserved);
		out.writeShortLE(sPlanes);
		out.writeShortLE(sBitCount);
		out.writeIntLE(iSizeInBytes);
		out.writeIntLE(iFileOffset);
	}
}