package net.imyeyu.exe4FX.service.ico;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ICO 图标编码
 *
 * 夜雨 创建于 2021-06-08 19:42
 */
public class ICOEncoder {

	public static void write(BufferedImage img, File file) throws IOException {
		try (FileOutputStream fout = new FileOutputStream(file)) {
			BufferedOutputStream out = new BufferedOutputStream(fout);
			LittleEndianOutputStream los = new LittleEndianOutputStream(out);

			int count = 1;

			writeFileHeader(count, los);

			InfoHeader ih = createInfoHeader(img);
			IconEntry e = createIconEntry(ih);

			ih.iHeight *= 2;
			e.iFileOffset = 22;
			e.write(los);
			ih.write(los);

			if (ih.sBitCount <= 8) {
				IndexColorModel icm = (IndexColorModel) img.getColorModel();
				writeColorMap(icm, los);
			}
			writeXorBitmap(img, ih, los);
			writeAndBitmap(img, los);

			los.flush();
		}
	}

	private static void writeFileHeader(int count, LittleEndianOutputStream out) throws IOException {
		out.writeShortLE((short) 0);
		out.writeShortLE((short) 1);
		out.writeShortLE((short) count);
	}

	private static IconEntry createIconEntry(InfoHeader ih) {
		IconEntry ret = new IconEntry();
		ret.bWidth = ih.iWidth == 256 ? 0 : ih.iWidth;
		ret.bHeight = ih.iHeight == 256 ? 0 : ih.iHeight;
		ret.bColorCount = ih.iNumColors >= 256 ? 0 : ih.iNumColors;
		ret.bReserved = 0;
		ret.sPlanes = 1;
		ret.sBitCount = ih.sBitCount;
		int cmapSize = getColorMapSize(ih.sBitCount);
		int xorSize = getBitmapSize(ih.iWidth, ih.iHeight, ih.sBitCount);
		int andSize = getBitmapSize(ih.iWidth, ih.iHeight, 1);
		ret.iSizeInBytes = ih.iSize + cmapSize + xorSize + andSize;
		ret.iFileOffset = 0;
		return ret;
	}

	private static void writeAndBitmap(BufferedImage img, LittleEndianOutputStream out) throws IOException {
		WritableRaster alpha = img.getAlphaRaster();
		if (img.getColorModel() instanceof IndexColorModel icm && img.getColorModel().hasAlpha()) {
			int w = img.getWidth();
			int h = img.getHeight();

			int bytesPerLine = getBytesPerLine1(w);

			byte[] line = new byte[bytesPerLine];

			Raster raster = img.getRaster();

			for (int y = h - 1; 0 <= y; y--) {
				for (int x = 0; x < w; x++) {
					int bi = x / 8;
					int i = x % 8;
					int p = raster.getSample(x, y, 0);
					int a = icm.getAlpha(p);
					int b = ~a & 1;
					line[bi] = setBit(line[bi], i, b);
				}
				out.write(line);
			}
		} else if (alpha == null) {
			int h = img.getHeight();
			int w = img.getWidth();
			int bytesPerLine = getBytesPerLine1(w);
			byte[] line = new byte[bytesPerLine];
			for (int i = 0; i < bytesPerLine; i++) {
				line[i] = (byte) 0;
			}
			for (int y = h - 1; 0 <= y; y--) {
				out.write(line);
			}
		} else {
			int w = img.getWidth();
			int h = img.getHeight();
			int bytesPerLine = getBytesPerLine1(w);
			byte[] line = new byte[bytesPerLine];
			for (int y = h - 1; 0 <= y; y--) {
				for (int x = 0; x < w; x++) {
					int bi = x / 8;
					int i = x % 8;
					int a = alpha.getSample(x, y, 0);
					int b = ~a & 1;
					line[bi] = setBit(line[bi], i, b);
				}
				out.write(line);
			}
		}
	}

	private static byte setBit(byte bits, int index, int bit) {
		int mask = 1 << (7 - index);
		bits &= ~mask;
		bits |= bit << (7 - index);
		return bits;
	}

	private static void writeXorBitmap(BufferedImage img, InfoHeader ih, LittleEndianOutputStream out) throws IOException {
		Raster raster = img.getRaster();
		switch (ih.sBitCount) {
			case 1  -> write1(raster, out);
			case 4  -> write4(raster, out);
			case 8  -> write8(raster, out);
			case 24 -> write24(raster, out);
			case 32 -> {
				Raster alpha = img.getAlphaRaster();
				write32(raster, alpha, out);
			}
		}
	}

	private static InfoHeader createInfoHeader(BufferedImage img) {
		InfoHeader ret = new InfoHeader();
		ret.iColorsImportant = 0;
		ret.iColorsUsed = 0;
		ret.iCompression = 0;
		ret.iHeight = img.getHeight();
		ret.iWidth = img.getWidth();
		ret.sBitCount = (short) img.getColorModel().getPixelSize();
		ret.iNumColors = 1 << (ret.sBitCount == 32 ? 24 : ret.sBitCount);
		ret.iImageSize = 0;
		return ret;
	}

	private static void writeColorMap(IndexColorModel icm, LittleEndianOutputStream out) throws IOException {
		int mapSize = icm.getMapSize();
		for (int i = 0; i < mapSize; i++) {
			int rgb = icm.getRGB(i);
			int r = (rgb >> 16) & 0xFF;
			int g = (rgb >> 8) & 0xFF;
			int b = (rgb) & 0xFF;
			out.writeByte(b);
			out.writeByte(g);
			out.writeByte(r);
			out.writeByte(0);
		}
	}

	private static int getBitmapSize(int w, int h, int bpp) {
		int bytesPerLine = switch (bpp) {
			case 1  -> getBytesPerLine1(w);
			case 4  -> getBytesPerLine4(w);
			case 8  -> getBytesPerLine8(w);
			case 24 -> getBytesPerLine24(w);
			case 32 -> w * 4;
			default -> 0;
		};
		return bytesPerLine * h;
	}

	private static int getBytesPerLine1(int width) {
		int ret = width / 8;
		if (ret * 8 < width) {
			ret++;
		}
		if (ret % 4 != 0) {
			ret = (ret / 4 + 1) * 4;
		}
		return ret;
	}

	private static int getBytesPerLine4(int width) {
		int ret = width / 2;
		if (ret % 4 != 0) {
			ret = (ret / 4 + 1) * 4;
		}
		return ret;
	}

	private static int getBytesPerLine8(int width) {
		int ret = width;
		if (ret % 4 != 0) {
			ret = (ret / 4 + 1) * 4;
		}
		return ret;
	}

	private static int getBytesPerLine24(int width) {
		int ret = width * 3;
		if (ret % 4 != 0) {
			ret = (ret / 4 + 1) * 4;
		}
		return ret;
	}

	private static int getColorMapSize(short sBitCount) {
		int ret = 0;
		if (sBitCount <= 8) {
			ret = (1 << sBitCount) * 4;
		}
		return ret;
	}

	private static void write1(Raster raster, LittleEndianOutputStream out) throws IOException {
		int bytesPerLine = getBytesPerLine1(raster.getWidth());
		byte[] line = new byte[bytesPerLine];
		for (int y = raster.getHeight() - 1; 0 <= y; y--) {
			for (int i = 0; i < bytesPerLine; i++) {
				line[i] = 0;
			}
			for (int x = 0; x < raster.getWidth(); x++) {
				int bi = x / 8;
				int i = x % 8;
				int index = raster.getSample(x, y, 0);
				line[bi] = setBit(line[bi], i, index);
			}
			out.write(line);
		}
	}

	private static void write4(Raster raster, LittleEndianOutputStream out) throws IOException {
		int width = raster.getWidth();
		int height = raster.getHeight();
		int bytesPerLine = getBytesPerLine4(width);
		byte[] line = new byte[bytesPerLine];
		for (int y = height - 1; 0 <= y; y--) {
			for (int i = 0; i < bytesPerLine; i++) {
				line[i] = 0;
			}
			for (int x = 0; x < width; x++) {
				int bi = x / 2;
				int i = x % 2;
				int index = raster.getSample(x, y, 0);
				line[bi] = setNibble(line[bi], i, index);
			}
			out.write(line);
		}
	}

	private static void write8(Raster raster, LittleEndianOutputStream out) throws IOException {
		int width = raster.getWidth();
		int height = raster.getHeight();
		int bytesPerLine = getBytesPerLine8(width);
		for (int y = height - 1; 0 <= y; y--) {
			for (int x = 0; x < width; x++) {
				int index = raster.getSample(x, y, 0);
				out.writeByte(index);
			}
			for (int i = width; i < bytesPerLine; i++) {
				out.writeByte(0);
			}
		}
	}

	private static void write24(Raster raster, LittleEndianOutputStream out) throws IOException {
		int width = raster.getWidth();
		int height = raster.getHeight();
		int bytesPerLine = getBytesPerLine24(width);
		for (int y = height - 1; 0 <= y; y--) {
			for (int x = 0; x < width; x++) {
				int r = raster.getSample(x, y, 0);
				int g = raster.getSample(x, y, 1);
				int b = raster.getSample(x, y, 2);
				out.writeByte(b);
				out.writeByte(g);
				out.writeByte(r);
			}
			for (int i = width * 3; i < bytesPerLine; i++) {
				out.writeByte(0);
			}
		}
	}

	private static void write32(Raster raster, Raster alpha, LittleEndianOutputStream out) throws IOException {
		int width = raster.getWidth();
		int height = raster.getHeight();
		for (int y = height - 1; 0 <= y; y--) {
			for (int x = 0; x < width; x++) {
				int r = raster.getSample(x, y, 0);
				int g = raster.getSample(x, y, 1);
				int b = raster.getSample(x, y, 2);
				int a = alpha.getSample(x, y, 0);
				out.writeByte(b);
				out.writeByte(g);
				out.writeByte(r);
				out.writeByte(a);
			}
		}
	}

	private static byte setNibble(byte nibbles, int index, int nibble) {
		nibbles |= (nibble << ((1 - index) * 4));
		return nibbles;
	}
}