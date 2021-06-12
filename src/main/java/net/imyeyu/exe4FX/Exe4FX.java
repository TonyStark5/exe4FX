package net.imyeyu.exe4FX;

import ch.qos.logback.classic.util.ContextInitializer;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import net.imyeyu.betterjava.config.Config;
import net.imyeyu.betterjava.config.Configer;
import net.imyeyu.exe4FX.ctrl.Main;

import java.io.FileNotFoundException;

/**
 * Maven JavaFX 打包 exe
 *
 * 夜雨 创建于 2021-06-02 16:59
 */
@Slf4j
public class Exe4FX {

	public static Config config;

	public static void main(String[] args) {
		try {
			// 禁止 DPI 缩放
			System.setProperty("glass.win.minHiDPI", "1");
			// 日志配置
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback.xml");

			// 配置文件
			Configer configer = new Configer("Exe4FX.ini");
			try {
				config = configer.get();
			} catch (FileNotFoundException e) {
				config = configer.reset();
			} catch (Exception e) {
				config = configer.reset();
				config.put("cache-isResetConfig", true);
			}
			// 启动
			Application.launch(Main.class, args);

		} catch (Exception e) {
			log.error("栈底捕获运行异常：", e);
		}
	}
}