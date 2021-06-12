package net.imyeyu.exe4FX.service.ico;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

/**
 * 夜雨 创建于 2021-06-08 20:22
 */
public class ConvertUtil {

	public static BufferedImage convert1(BufferedImage src) {
		IndexColorModel icm = new IndexColorModel(
			1,
			2,
			new byte[]{(byte) 0, (byte) 0xFF},
			new byte[]{(byte) 0, (byte) 0xFF},
			new byte[]{(byte) 0, (byte) 0xFF}
		);
		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_BINARY, icm);
		ColorConvertOp cco = new ColorConvertOp(src.getColorModel().getColorSpace(), dest.getColorModel().getColorSpace(), null);
		cco.filter(src, dest);
		return dest;
	}

	public static BufferedImage convert4(BufferedImage src) {
		int[] cmap = new int[]{0x000000, 0x800000, 0x008000, 0x808000, 0x000080, 0x800080, 0x008080, 0x808080, 0xC0C0C0, 0xFF0000, 0x00FF00, 0xFFFF00, 0x0000FF, 0xFF00FF, 0x00FFFF, 0xFFFFFF};
		return convert4(src, cmap);
	}

	public static BufferedImage convert4(BufferedImage src, int[] cmap) {
		IndexColorModel icm = new IndexColorModel(
			4,
			cmap.length,
			cmap,
			0,
			false,
			Transparency.OPAQUE,
			DataBuffer.TYPE_BYTE
		);
		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_BINARY, icm);
		ColorConvertOp cco = new ColorConvertOp(src.getColorModel().getColorSpace(), dest.getColorModel().getColorSpace(), null);
		cco.filter(src, dest);
		return dest;
	}

	public static BufferedImage convert8(BufferedImage src) {
		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
		ColorConvertOp cco = new ColorConvertOp(src.getColorModel().getColorSpace(), dest.getColorModel().getColorSpace(), null);
		cco.filter(src, dest);
		return dest;
	}

	public static BufferedImage convert24(BufferedImage src) {
		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		ColorConvertOp cco = new ColorConvertOp(src.getColorModel().getColorSpace(), dest.getColorModel().getColorSpace(), null);
		cco.filter(src, dest);
		return dest;
	}

	public static BufferedImage convert32(BufferedImage src) {
		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		ColorConvertOp cco = new ColorConvertOp(src.getColorModel().getColorSpace(), dest.getColorModel().getColorSpace(), null);
		cco.filter(src, dest);
		return dest;
	}
}