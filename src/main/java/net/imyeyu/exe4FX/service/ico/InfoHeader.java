package net.imyeyu.exe4FX.service.ico;

import java.io.IOException;

/**
 * 夜雨 创建于 2021-06-08 19:50
 */
public class InfoHeader {

	public int iSize;
	public int iWidth;
	public int iHeight;
	public short sPlanes;
	public short sBitCount;
	public int iCompression;
	public int iImageSize;
	public int iXpixelsPerM;
	public int iYpixelsPerM;
	public int iColorsUsed;
	public int iColorsImportant;
	public int iNumColors;

	public InfoHeader() {
		iSize = 40;
		iWidth = 0;
		iHeight = 0;
		sPlanes = 1;
		sBitCount = 0;

		iNumColors = 0;

		iCompression = 0;
		iImageSize = 0;
		iXpixelsPerM = 0;
		iYpixelsPerM = 0;
		iColorsUsed = 0;
		iColorsImportant = 0;
	}

	public void write(LittleEndianOutputStream out) throws IOException {
		out.writeIntLE(iSize);
		out.writeIntLE(iWidth);
		out.writeIntLE(iHeight);
		out.writeShortLE(sPlanes);
		out.writeShortLE(sBitCount);

		out.writeIntLE(iCompression);
		out.writeIntLE(iImageSize);
		out.writeIntLE(iXpixelsPerM);
		out.writeIntLE(iYpixelsPerM);
		out.writeIntLE(iColorsUsed);
		out.writeIntLE(iColorsImportant);
	}
}