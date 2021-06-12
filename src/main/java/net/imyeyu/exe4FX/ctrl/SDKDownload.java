package net.imyeyu.exe4FX.ctrl;

import lombok.extern.slf4j.Slf4j;
import net.imyeyu.betterfx.component.dialog.Alert;
import net.imyeyu.betterfx.service.ByteSpeed;
import net.imyeyu.betterjava.Tools;
import net.imyeyu.exe4FX.Exe4FX;
import net.imyeyu.exe4FX.service.SDKDownloader;
import net.imyeyu.exe4FX.view.ViewSDKDownload;

/**
 * SDK 下载控制
 *
 * 夜雨 创建于 2021-06-06 23:02
 */
@Slf4j
public class SDKDownload extends ViewSDKDownload {

	private SDKDownloader sdkDownloader; // sdk 下载线程
	private final ByteSpeed byteSpeed;

	public SDKDownload() {
		byteSpeed = new ByteSpeed();
		byteSpeed.start();

		// 网速
		byteSpeed.valueProperty().addListener((obs, o, b) -> {
			if (b != null && b != 0) {
				speed.setText(Tools.byteFormat(b, 2) + "/s");
			} else {
				speed.setText("");
			}
		});
		// 控制
		ctrl.setOnAction(event -> {
			if (sdkDownloader == null) {
				// 下载
				sdkDownloader = new SDKDownloader(api.getValue(), Exe4FX.config.getString("cache-fx-version"));
				// 文本提示
				tips.textProperty().bind(sdkDownloader.messageProperty());
				// 进度
				pb.progressProperty().bind(sdkDownloader.progressProperty());
				// 取消按钮
				ctrl.setText("取消");
				// 解压时不可取消
				ctrl.disableProperty().bind(sdkDownloader.unzipingProperty());
				// 执行完成
				sdkDownloader.setOnSucceeded(e -> {
					hide();
					sdkDownloader = null;
					ctrl.setText("开始下载");
				});
				// 异常
				sdkDownloader.exceptionProperty().addListener((obs, o, e) -> {
					log.error("SDK 下载异常：", e);
					new Alert(Alert.AlertType.ERROR, "SDK 下载异常：" + e.getMessage()).show();
				});
				// 开始下载
				sdkDownloader.start();
			} else {
				// 取消
				tips.textProperty().unbind();
				pb.progressProperty().unbind();
				ctrl.disableProperty().unbind();

				pb.setProgress(0);
				tips.setText("");
				ctrl.setText("开始下载");

				sdkDownloader.shutdown();
			}
		});
		// 显示时设置标题
		setOnShown(e -> {
			if (Exe4FX.config.has("cache-fx-version")) {
				setTitle("下载 JavaFX " + Exe4FX.config.getString("cache-fx-version") + " SDK");
			} else {
				hide();
				new Alert(Alert.AlertType.ERROR, "无法从 pom.xml 中解析到 JavaFX 版本，请重新选择").show();
			}
		});
		// 关闭时检测是否正在解压
		setOnCloseRequest(e -> {
			if (sdkDownloader.isRunning() && sdkDownloader.isIsUnziping()) {
				new Alert(Alert.AlertType.ERROR, "正在解压，无法中止，请稍后再试。").show();
				e.consume();
			}
		});
	}

	public void shutdown() {
		byteSpeed.shutdown();
	}
}
