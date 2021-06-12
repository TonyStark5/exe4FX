package net.imyeyu.exe4FX.ctrl;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.imyeyu.betterfx.component.dialog.Alert;
import net.imyeyu.betterfx.component.dialog.AlertTextArea;
import net.imyeyu.betterfx.service.RunAsync;
import net.imyeyu.betterfx.service.RunLater;
import net.imyeyu.betterjava.DateTimeDifference;
import net.imyeyu.betterjava.IO;
import net.imyeyu.betterjava.Tools;
import net.imyeyu.betterjava.UnixTime;
import net.imyeyu.betterjava.config.Configer;
import net.imyeyu.exe4FX.Exe4FX;
import net.imyeyu.exe4FX.bean.PackageBuilderException;
import net.imyeyu.exe4FX.bean.Params;
import net.imyeyu.exe4FX.core.CMDRunner;
import net.imyeyu.exe4FX.core.PackageBuilder;
import net.imyeyu.exe4FX.view.ViewMain;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.imyeyu.betterfx.component.dialog.Alert.NO;
import static net.imyeyu.betterfx.component.dialog.Alert.YES;

/**
 * 主界面控制
 *
 * 夜雨 创建于 2021-06-02 17:00
 */
@Slf4j
public class Main extends ViewMain {

	private SDKDownload sdkDownload;
	private CMDRunner cmdRunner;

	private Explain explain;
	private final SimpleBooleanProperty runningProperty = new SimpleBooleanProperty(false);

	@Override
	public void start(Stage stage) throws Exception {
		super.start(stage);
		explain = new Explain();
		// 运行禁用
		run.disableProperty().bind(runningProperty);
		pom.disableProperty().bind(runningProperty);
		selectPom.disableProperty().bind(runningProperty);
		// CMD 命令执行器
		cmdRunner = new CMDRunner();
		cmdRunner.valueProperty().addListener((obs, o, result) -> {
			if (result != null) {
				super.log.appendText(result);
			}
		});
		// pom.xml 输入
		pom.focusedProperty().addListener((obs, o, isFocused) -> {
			if (!isFocused && !isRunning()) {
				parsePOM(new File(pom.getText()));
			}
		});
		// 选择 pom.xml
		selectPom.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll( new FileChooser.ExtensionFilter("pom.xml", "*.xml"));
			File path = new File(Exe4FX.config.getString("pomPath"));
			chooser.setInitialDirectory(new File(path.exists() ? path.getAbsolutePath() : "./"));
			chooser.setTitle("选择 pom.xml");
			File file = chooser.showOpenDialog(stage);
			if (file == null) return;
			parsePOM(file);
		});
		// 图标
		selectIcon.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll( new FileChooser.ExtensionFilter("PNG 图像", "*.png"));
			File path = new File(Exe4FX.config.getString("iconPath"));
			chooser.setInitialDirectory(new File(path.exists() ? path.getAbsolutePath() : "./"));
			chooser.setTitle("选择图标");
			File file = chooser.showOpenDialog(stage);
			if (file == null) return;
			if (file.getName().endsWith(".png")) {
				try {
					Image img = new Image(new FileInputStream(file));
					icon.setImage(img);
					stage.getIcons().setAll(img);
					backIcon.setVisible(true);
					// 更新配置
					Exe4FX.config.put("cache-iconPath", file.getAbsolutePath());
					Exe4FX.config.put("iconPath", file.getParentFile().getAbsolutePath());
				} catch (FileNotFoundException e) {
					new Alert(Alert.AlertType.ERROR, "文件不存在").show();
				}
			} else {
				new Alert(Alert.AlertType.WARNING, "请选择 PNG 图像文件").show();
			}
		});
		// 还原图标
		backIcon.setOnAction(event -> {
			icon.setImage(ICON_DEFAULT);
			stage.getIcons().setAll(ICON_DEFAULT);
			backIcon.setVisible(false);
			selectIcon.requestFocus();
		});
		// 启动页
		selectSplashScreen.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll( new FileChooser.ExtensionFilter("PNG 图像", "*.png"));
			File path = new File(Exe4FX.config.getString("splashScreenPath"));
			chooser.setInitialDirectory(new File(path.exists() ? path.getAbsolutePath() : "./"));
			chooser.setTitle("选择启动页");
			File file = chooser.showOpenDialog(stage);
			if (file == null) return;
			if (file.getName().endsWith(".png")) {
				try {
					Image img = new Image(new FileInputStream(file));
					splashScreen.setText(file.getAbsolutePath());
					splashScreenTips.setImage(img);
					// 更新配置
					Exe4FX.config.put("splashScreenPath", file.getParentFile().getAbsolutePath());
				} catch (FileNotFoundException e) {
					new Alert(Alert.AlertType.ERROR, "文件不存在").show();
				}
			} else {
				new Alert(Alert.AlertType.WARNING, "请选择 PNG 图像文件").show();
			}
		});
		// 移除启动页
		removeSplashScreen.setOnAction(event -> {
			splashScreen.setText(null);
			splashScreenTips.setText(splashScreenTipsText);
		});
		// 选择导出位置
		selectOut.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			File path = new File(Exe4FX.config.getString("outPath"));
			chooser.setInitialDirectory(new File(path.exists() ? path.getAbsolutePath() : "./out"));
			chooser.setTitle("选择导出位置");
			File file = chooser.showDialog(stage);
			if (file == null) return;
			if (file.isDirectory() && file.canWrite()) {
				out.setText(file.getAbsolutePath());
				// 更新配置
				Exe4FX.config.put("outPath", file.getAbsolutePath());
			} else {
				new Alert(Alert.AlertType.WARNING, "请选择有效的文件夹").show();
				selectOut.fire();
			}
		});
		// 打开导出位置
		openOut.setOnAction(event -> {
			File directory = new File(out.getText());
			try {
				if (directory.exists() && directory.isDirectory()) {
					Desktop.getDesktop().open(directory);
				}
			} catch (IOException e) {
				new Alert(Alert.AlertType.ERROR, e.getMessage() + "\n无法打开该文件夹：" + directory.getAbsolutePath());
			}
		});
		// 说明
		super.explain.setOnAction(event -> explain.show());
		// 关于
		final About about = new About();
		super.about.setOnAction(event -> about.show());
		// 显示或隐藏高级设置
		isShowAdvancedPane.addListener((obs, o, isShowAdvancedPane) -> {
			if (isShowAdvancedPane) {
				showAdvancedSettings();
			} else {
				hideAdvancedSettings();
			}
		});
		// 保持显示高级设置
		isShowAdvancedPane.set(Exe4FX.config.is("keepShowAdvanced"));
		advanced.setOnAction(event -> isShowAdvancedPane.set(!isShowAdvancedPane.get()));
		// 执行打包
		run.setOnAction(event -> {
			// 检测输出文件夹
			int clearOutCount = Exe4FX.config.getInt("clearOut");
			File outPath = new File(out.getText());
			File[] outFiles = outPath.listFiles();
			if (outFiles != null && 0 < outFiles.length) {
				if (clearOutCount < 12) {
					String text = "注意!!!\n\t导出目录存在文件，需要清空文件夹才能继续。确定删除导出目录的所有文件吗？\n" + outPath.getAbsolutePath();
					Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, YES, NO);
					alert.getStage().getIcons().setAll(alert.getIcon(Alert.AlertType.ERROR));
					alert.setTitle("警告");
					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent()) {
						ButtonType buttonType = result.get();
						if (YES.equals(buttonType)) {
							try {
								distroyFiles(outFiles);
							} catch (IOException e) {
								new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
								return;
							}
							Exe4FX.config.put("clearOut", ++clearOutCount);
						} else if (NO.equals(buttonType)) {
							Exe4FX.config.put("clearOut", 0);
							return;
						}
					}
				} else {
					// 超过 12 次连续清空输出文件夹，不再弹窗
					try {
						distroyFiles(outFiles);
					} catch (IOException e) {
						new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
						return;
					}
				}
			}
			setRunning(true);
			super.log.requestFocus();
			long startAt = System.currentTimeMillis();
			// 执行参数
			Params params = new Params();
			params.setPom(pom.getText());
			params.setIcon(icon.getImage().getUrl());
			params.setSplashScreen(splashScreen.getText());
			params.setOut(out.getText());
			params.setJdk(jdk.getText());
			params.setSdk(sdk.getText());
			params.setJmods(jmods.getText());
			params.setRunScript(runScript.getText());
			params.setCustomModule(customModule.getText());
			params.setX64(isX64.isSelected());
			params.setInvisible(isInvisible.isSelected());
			params.setUseUPX(useUPX.isSelected());
			params.setNeedAdmin(needAdmin.isSelected());
			params.setFileName(fileName.getText());
			params.setFileVersion(fileVersion.getText());
			params.setProductVersion(productVersion.getText());
			params.setInternalName(internalName.getText());
			params.setDescription(description.getText());
			params.setCompany(company.getText());
			params.setTrademarks(trademarks.getText());
			params.setCopyright(copyright.getText());
			params.setComments(comments.getText());
			// 构造服务
			PackageBuilder builder = new PackageBuilder(cmdRunner, params);
			builder.exceptionProperty().addListener((obs, o, e) -> {
				log.error("## 构造异常 ##", e);
				if (e instanceof PackageBuilderException builderException) {
					// 执行构造异常
					if (builderException.getAreaText() != null) {
						new AlertTextArea(Alert.AlertType.ERROR, e.getMessage(), builderException.getAreaText()).showAndWait();
					} else {
						new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
					}
					// 构造异常动作
					if (builderException.getAction() != null) {
						showAdvancedSettings();
						switch (builderException.getAction()) {
							case FOCUS_JDK           -> jdk.requestFocus();
							case OPEN_EXPLAIN        -> super.explain.fire();
							case FOCUS_FILE_NAME     -> fileName.requestFocus();
							case OPEN_SDK_DOWNLOADER -> sdkDownload.show();
						}
					}
				} else {
					// 其他异常
					log.error("运行异常", e);
					new AlertTextArea(Alert.AlertType.ERROR, "运行异常：", e.getMessage()).showAndWait();
				}
			});
			// 构造进度
			pb.progressProperty().bind(builder.progressProperty());
			// 构造完成
			builder.valueProperty().addListener((obs, o, size) -> {
				if (size != null) {
					DateTimeDifference dtd = UnixTime.calcDifference(startAt);
					String useTime = "耗时：" + String.format("%02d", dtd.getMinute()) + ":" + String.format("%02d", dtd.getSecond());
					String msg = "构造完成：\n\t" + useTime + "\n\t大小：" + Tools.byteFormat(size, 2);
					final Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
					alert.setTitle("提示");
					alert.show();
				}
			});
			// 结束事件
			builder.setOnSucceeded(e -> {
				cmdRunner.setOnUpdate(null);
				pb.progressProperty().unbind();
				setRunning(false);

			});
			builder.setOnFailed(e -> {
				cmdRunner.setOnUpdate(null);
				pb.progressProperty().unbind();
				pb.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
				setRunning(false);
			});
			super.log.clear();
			// 开始
			builder.start();
		});
		// JDK 选择
		if (jdk.getText().equals("./")) {
			jdk.setText(System.getProperty("java.home"));
		}
		selectJDK.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			File path = new File(Exe4FX.config.getString("jdkPath"));
			chooser.setInitialDirectory(new File(path.exists() ? path.getAbsolutePath() : "./"));
			chooser.setTitle("选择 JDK 位置");
			File file = chooser.showDialog(stage);
			if (file == null) return;
			if (file.isDirectory() && file.canRead()) {
				String jdkPath = file.getAbsolutePath();
				// jdeps.exe 校验
				File jdeps = new File(jdkPath + File.separator + "bin" + File.separator + "jdeps.exe");
				if (!jdeps.exists()) {
					new Alert(Alert.AlertType.ERROR, "jdeps.exe 不存在，可能无法执行").showAndWait();
				}
				// jlink.exe 校验
				File jlink = new File(jdkPath + File.separator + "bin" + File.separator + "jlink.exe");
				if (!jlink.exists()) {
					new Alert(Alert.AlertType.ERROR, "jlink.exe 不存在，可能无法执行").showAndWait();
				}
				jdk.setText(jdkPath);
			} else {
				new Alert(Alert.AlertType.WARNING, "请选择有效的文件夹").show();
			}
		});
		// 选择 SDK 位置
		selectSDK.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			File path = new File(sdk.getText());
			chooser.setInitialDirectory(new File(path.exists() ? path.getAbsolutePath() : "./"));
			chooser.setTitle("选择 JavaFX SDK 目录");
			File file = chooser.showDialog(stage);
			if (file == null) return;
			if (file.isDirectory() && file.canRead()) {
				sdk.setText(file.getAbsolutePath());
			} else {
				new Alert(Alert.AlertType.WARNING, "请选择有效的文件夹").show();
				selectSDK.fire();
			}
		});
		// 选择 jmods 位置
		selectJmods.setOnAction(event -> {
			DirectoryChooser chooser = new DirectoryChooser();
			File path = new File(jmods.getText());
			chooser.setInitialDirectory(new File(path.exists() ? path.getAbsolutePath() : "./"));
			chooser.setTitle("选择 JavaFX jmods 目录");
			File file = chooser.showDialog(stage);
			if (file == null) return;
			if (file.isDirectory() && file.canRead()) {
				jmods.setText(file.getAbsolutePath());
			} else {
				new Alert(Alert.AlertType.WARNING, "请选择有效的文件夹").show();
				selectJmods.fire();
			}
		});
		// 文件名
		fileName.focusedProperty().addListener((obs, o, isFocused) -> {
			if (!isFocused && !fileName.getText().endsWith(".exe")) {
				fileName.setText(fileName.getText() + ".exe");
			}
		});
		// 日志操作
		logClean.setOnMouseClicked(e -> super.log.clear());

		// 准备就绪
		File out = new File("out");
		if (!out.exists() && !out.mkdirs()) {
			new Alert(Alert.AlertType.ERROR, "无法创建输出文件夹，可能会影响程序运行").show();
		}
		File work = new File("work");
		if (!work.exists() && !work.mkdirs()) {
			new Alert(Alert.AlertType.ERROR, "无法创建工作文件夹，可能会影响程序运行").show();
		}
		if (Exe4FX.config.has("cache-isResetConfig")) {
			new Alert(Alert.AlertType.WARNING, "配置文件异常，已还原默认配置").show();
		}
		try {
			// 启动页提示
			splashScreenTipsText = '\t' + IO.jarFileToString("splash-screen.txt").trim();
			splashScreenTips.setText(splashScreenTipsText);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		selectPom.requestFocus();
		RunLater.time(1000).event(() -> cmdRunner.start());
	}

	@Override
	public void stop() throws Exception {
		cmdRunner.shutdown();
		if (sdkDownload != null) {
			sdkDownload.shutdown();
		}
		Exe4FX.config.put("keepShowAdvanced", settings.getChildren().contains(advancedSettings));
		Exe4FX.config.bindUpdate();
		new Configer("Exe4FX").set(Exe4FX.config);
		super.stop();
	}

	/**
	 * 递归清除文件
	 *
	 * @param files 文件列表
	 * @throws IOException 删除异常
	 */
	private void distroyFiles(File[] files) throws IOException {
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File[] subFiles = files[i].listFiles();
				if (subFiles != null) {
					distroyFiles(subFiles);
				}
			}
			if (!files[i].delete()) {
				throw new IOException("无法删除文件：\n" + files[i].getAbsolutePath());
			}
		}
	}

	/**
	 * 解析 pom 文件
	 *
	 * @param file pom.xml 文件
	 */
	public void parsePOM(File file) {
		if (file.exists() && file.canRead()) {
			setRunning(true);
			pom.setText(file.getAbsoluteFile().getAbsolutePath());
			// 更新配置
			Exe4FX.config.put("pomPath", file.getParentFile().getAbsolutePath());
			// 异步解析
			new RunAsync<Params>() {

				@Override
				public Params call() throws Exception {
					Params params = new Params();
					Document dom = new SAXReader().read(file);
					Element root = dom.getRootElement();
					// 项目名
					Element artifactId = root.element("artifactId");
					if (artifactId != null) {
						String name = artifactId.getText();
						params.setFileName(name + ".exe");
						params.setInternalName(name);
					}
					// 项目版本
					Element version = root.element("version");
					if (version != null) {
						params.setFileVersion(version.getText());
						params.setProductVersion(version.getText());
					}
					// 获取 JavaFX 版本
					String fxVersion = getJavaFXVersion(file.getParent());
					Exe4FX.config.put("cache-fx-version", fxVersion);
					// 检测是否存在 SDK 和 jmods
					final String sep = File.separator;
					File sdk = new File("work" + sep + "sdk" + sep + "javafx-sdk-" + fxVersion);
					File jmods = new File("work" + sep + "jmods" + sep + "javafx-jmods-" + fxVersion);
					params.setSdk(sdk.getAbsolutePath());
					params.setJmods(jmods.getAbsolutePath());
					// 查找启动页
					Element pluginsEl = root.element("build").element("plugins");
					List<?> plugins = pluginsEl.elements();
					for (int i = 0; i < plugins.size(); i++) {
						if (plugins.get(i) instanceof Element plugin) {
							// maven-assembly-plugin 插件配置
							if (plugin.element("artifactId").getText().equals("maven-assembly-plugin")) {
								Element archive = plugin.element("configuration").element("archive");
								// configuration > manifestEntries > SplashScreen-Image[启动页地址]
								Element manifestEntries = archive.element("manifestEntries");
								if (manifestEntries != null) {
									Element ssi = manifestEntries.element("SplashScreen-Image");
									if (ssi != null) {
										// 启动页位置
										final String SEP = File.separator;
										File ssiPath = new File(file.getParent() + SEP + "src" + SEP + "main" + SEP + "resources" + SEP + ssi.getText());
										if (ssiPath.exists() && ssiPath.isFile()) {
											params.setSplashScreen(ssiPath.getAbsolutePath());
										}
									}
								}
							}
						}
					}
					// 是否需要下载 SDK
					params.setNeedDownloadSDK(!sdk.exists() || !sdk.isDirectory() || !jmods.exists() || !jmods.isDirectory());
					return params;
				}

				@Override
				public void onFinish(Params params) {
					sdk.setText(params.getSdk());
					jmods.setText(params.getJmods());
					fileName.setText(params.getFileName());
					fileVersion.setText(params.getFileVersion());
					internalName.setText(params.getInternalName());
					splashScreen.setText(params.getSplashScreen());
					productVersion.setText(params.getProductVersion());
					// 需要下载 SDK
					if (params.isNeedDownloadSDK()) {
						if (sdkDownload == null) {
							sdkDownload = new SDKDownload();
						}
						sdkDownload.show();
					}
					if (splashScreen.getText() != null && !splashScreen.getText().trim().equals("")) {
						try {
							Image img = new Image(new FileInputStream(splashScreen.getText()));
							splashScreenTips.setImage(img);
						} catch (FileNotFoundException | NullPointerException e) {
							new Alert(Alert.AlertType.ERROR, "找不到启动页文件：" + splashScreen.getText());
						}
					}
					setRunning(false);
				}

				@Override
				public void onException(Throwable e) {
					new Alert(Alert.AlertType.ERROR, "解析 pom.xml 失败：" + e.getMessage());
					setRunning(false);
				}
			}.start();
		} else {
			new Alert(Alert.AlertType.ERROR, "请选择有效的 pom.xml 文件").show();
		}
	}

	/**
	 * 获取 JavaFX 版本
	 *
	 * @throws Exception 执行异常
	 */
	public String getJavaFXVersion(String pomPath) throws Exception {
		if (!cmdRunner.toPath(pomPath)) {
			throw new Exception("无法进入该目录：" + pomPath);
		}
		if (!cmdRunner.run("mvn dependency:tree")) {
			throw new Exception("无法读取 pom 的依赖树");
		}
		Pattern pattern = Pattern.compile("javafx-base:jar:(.+):compile");
		Matcher matcher = pattern.matcher(cmdRunner.getLastResult());
		if (matcher.find()) {
			return matcher.group(matcher.groupCount());
		}
		throw new Exception("无法获取 JavaFX 版本：" + cmdRunner.getLastResult());
	}

	private void setRunning(boolean isRunning) {
		runningProperty.set(isRunning);
	}

	private boolean isRunning() {
		return runningProperty.get();
	}
}