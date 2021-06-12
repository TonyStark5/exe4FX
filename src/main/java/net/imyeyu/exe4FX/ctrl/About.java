package net.imyeyu.exe4FX.ctrl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.Cursor;
import lombok.extern.slf4j.Slf4j;
import net.imyeyu.betterfx.service.RunAsync;
import net.imyeyu.betterjava.Encode;
import net.imyeyu.betterjava.Network;
import net.imyeyu.exe4FX.bean.Version;
import net.imyeyu.exe4FX.view.ViewAbout;

/**
 * 关于控制
 *
 * 夜雨 创建于 2021-06-02 22:23
 */
@Slf4j
public class About extends ViewAbout {

	private boolean isVersionChecked = false;

	public About() {
		name.setOnMouseClicked(event -> Network.openURIInBrowser("https://www.imyeyu.net/article/software/aid141.html"));
		setOnShowing(event -> {
			if (!isVersionChecked) {
				checkVersion();
			}
			blog.requestFocus();
		});
	}

	private void checkVersion() {
		version.setText(VERSION + " - 正在检查更新");
		new RunAsync<String>() {

			@Override
			public String call() throws Exception {
				return Network.doGet("https://blogapi.imyeyu.net/versions/Exe4FX", true).trim();
			}

			@Override
			public void onFinish(String response) {
				if (Encode.isJson(response)) {
					JsonObject root = JsonParser.parseString(response).getAsJsonObject();
					if (root.get("code").getAsInt() == 20000) {
						Version respVersion = new Gson().fromJson(root.get("data").getAsJsonObject(), Version.class);
						if (!respVersion.getVersion().equals(VERSION)) {
							version.setText("当前版本：" + VERSION + "，最新版本：" + respVersion.getVersion());
							version.setURL(respVersion.getUrl());
						} else {
							version.setText(VERSION);
							version.setURL(null);
							version.getLabel().setCursor(Cursor.DEFAULT);
							version.getLabel().setOnMouseClicked(null);
						}
					}
				}
			}

			@Override
			public void onException(Throwable e) {
				log.error("检查更新失败：", e);
				isVersionChecked = false;
				version.setText(VERSION + " - 检查新版本失败，点击重试");
				version.getLabel().setCursor(Cursor.HAND);
				version.getLabel().setOnMouseClicked(event -> checkVersion());
			}
		}.start();
	}
}