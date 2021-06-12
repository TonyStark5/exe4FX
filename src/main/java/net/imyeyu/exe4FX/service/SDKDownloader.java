package net.imyeyu.exe4FX.service;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import net.imyeyu.betterfx.service.ByteSpeed;
import net.imyeyu.betterjava.Tools;

import javax.naming.NoPermissionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import net.lingala.zip4j.core.ZipFile;

/**
 * SDK 下载
 *
 * 夜雨 创建于 2021-06-06 20:16
 */
public class SDKDownloader extends Service<Boolean> {

	/**
	 * 使用 API
	 *
	 * 夜雨 创建于 2021-06-06 22:34
	 */
	public enum API {

		BLOG("http://dl.imyeyu.net/java/javafx-sdk/"),      // 博客
		OPEN_JFX("https://download2.gluonhq.com/openjfx/"); // 官方

		private final String url;

		API(String url) {
			this.url = url;
		}
	}

	private static final String SEP = File.separator;

	private final API api;
	private final String version;
	private final SimpleBooleanProperty isUnziping = new SimpleBooleanProperty(false);

	private boolean isShutdown = false;

	public SDKDownloader(API api, String version) {
		this.api = api;
		this.version = version;
	}

	@Override
	protected Task<Boolean> createTask() {
		return new Task<>() {

			private final String sdkPath = "work" + SEP + "sdk" + SEP;
			private final String jmodsPath = "work" + SEP + "jmods" + SEP;

			@Override
			protected Boolean call() throws Exception {
				// 请求地址前缀
				String prefixURL = api.url + (api == API.OPEN_JFX ? version + "/" : "");
				// 下载 SDK
				String sdkName = "openjfx-" + version + "_windows-x64_bin-sdk.zip";
				String sdkURL = prefixURL + sdkName;
				downloadFile(sdkURL, sdkPath, sdkName);
				// 下载 jmods
				String jmodsName = "openjfx-" + version + "_windows-x64_bin-jmods.zip";
				String jmodsURL = prefixURL + jmodsName;
				downloadFile(jmodsURL, jmodsPath, jmodsName);

				if (!isShutdown) {
					isUnziping.set(true);
					// 解压 SDK
					updateMessage("正在解压：" + sdkName);
					new ZipFile(sdkPath + sdkName).extractAll(sdkPath);
					// 解压 jmods
					updateMessage("正在解压：" + jmodsName);
					new ZipFile(jmodsPath + jmodsName).extractAll(jmodsPath);
					isUnziping.set(false);
				}
				return true;
			}

			/**
			 * 下载文件
			 *
			 * @param url        下载地址
			 * @param path       文件存放路径
			 * @param fileName   文件名
			 * @throws Exception 异常
			 */
			private void downloadFile(String url, String path, String fileName) throws Exception {
				updateMessage("正在下载：" + fileName);
				File dir = new File(path);
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						throw new NoPermissionException("没有权限创建文件夹：" + dir.getAbsolutePath());
					}
				}
				HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
				connect.setRequestProperty("accept", "*/*");
				connect.setRequestProperty("referer", "https://www.pixiv.net/");
				connect.setRequestProperty("connection", "Keep-Alive");
				connect.setRequestProperty(
						"user-agent",
						"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/10.7.4313.400"
				);
				connect.setConnectTimeout(8000);
				long lengthTotal = connect.getContentLength();

				updateMessage("正在下载：" + fileName + " " + Tools.byteFormat(lengthTotal, 2));

				File file = new File(dir + File.separator + fileName);
				if (file.exists() && file.length() == lengthTotal) {
					// 已存在并且文件大小一致
					return;
				}
				int l;
				double length = 0;
				byte[] buffer = new byte[1024];
				InputStream is = connect.getInputStream();
				FileOutputStream fos = new FileOutputStream(file);
				while ((l = is.read(buffer)) != -1 && !isShutdown) {
					ByteSpeed.BUFFER += l;
					updateProgress((length += l), lengthTotal);
					fos.write(buffer, 0, l);
				}
				updateProgress(1, 1);

				fos.close();
				is.close();
				if (isShutdown && !file.delete()) {
					throw new Exception("无法删除未完成的文件：" + file.getAbsolutePath());
				}
			}
		};
	}

	public boolean isIsUnziping() {
		return isUnziping.get();
	}

	public ReadOnlyBooleanProperty unzipingProperty() {
		return isUnziping;
	}

	public void shutdown() {
		isShutdown = true;
	}
}