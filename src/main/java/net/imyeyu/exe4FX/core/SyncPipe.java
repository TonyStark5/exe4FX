package net.imyeyu.exe4FX.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CMD 异步单线程管道
 *
 * 夜雨 创建于 2021-06-03 23:47
 */
public abstract class SyncPipe implements Runnable {

	private String runSpace;
	private boolean isShutdown;
	private final InputStream is;
	private final StringBuilder sb; // 缓冲

	public SyncPipe(InputStream is) {
		this.is = is;
		sb = new StringBuilder();
	}

	@Override
	public void run() {
		try {
			int l;
			String bufferString;
			Matcher matcher;
			final Pattern pattern = Pattern.compile(":\\\\(.+)>");
			final byte[] buffer = new byte[4096];
			while (!isShutdown) {
				// 读取长度
				l = is.read(buffer);
				// 读取结果
				if (l != -1) {
					bufferString = new String(buffer, 0, l, "GBK");
					// 回调结果
					onUpdate(bufferString);
					sb.append(bufferString);
					matcher = pattern.matcher(bufferString);
					// 已结束
					if (matcher.find() || bufferString.trim().endsWith(":\\>")) {
						// 回调运行结果
						onFinish(sb.toString());
						sb.setLength(0);
					}
				} else {
					break;
				}
			}
			is.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 缓冲数据返回（调用 super() 来稍微阻塞线程，等待 FX 线程）
	 *
	 * @param bufferString 缓冲数据
	 */
	public synchronized void onUpdate(String bufferString) throws InterruptedException {
		Thread.sleep(16);
	}

	/**
	 * 命令执行完成返回
	 *
	 * @param result 运行结果
	 */
	public abstract void onFinish(String result);

	public void shutdown() {
		this.isShutdown = true;
	}
}