package net.imyeyu.exe4FX.view;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import net.imyeyu.betterfx.BetterFX;
import net.imyeyu.betterfx.bean.PopupTips;
import net.imyeyu.betterfx.component.BorderPaneGroup;
import net.imyeyu.betterfx.component.HBoxGroup;
import net.imyeyu.betterfx.component.Switch;
import net.imyeyu.betterfx.component.TextAreaField;
import net.imyeyu.betterfx.extend.BgFill;
import net.imyeyu.betterfx.extend.XAnchorPane;
import net.imyeyu.betterfx.service.PopupTipsService;
import net.imyeyu.exe4FX.Exe4FX;

import java.awt.SplashScreen;

/**
 * 主界面视图
 *
 * 夜雨 创建于 2021-06-02 17:00
 */
public abstract class ViewMain extends Application implements BetterFX {

	protected static final Image ICON_DEFAULT = new Image("/icon.png");

	protected VBox settings;
	protected Label advancedIcon, logClean;
	protected Button selectPom, selectIcon, backIcon, selectSplashScreen, removeSplashScreen, selectOut, openOut, explain,
					 about, advanced, run, selectJDK, selectSDK, selectJmods;
	protected Switch isX64, isInvisible, needAdmin, useUPX;
	protected TextArea log;
	protected GridPane advancedSettings;
	protected TextField pom, splashScreen, out, jdk, sdk, jmods, fileName, fileVersion, productVersion, internalName, description, company, trademarks, copyright, comments;
	protected ImageView icon;
	protected PopupTips splashScreenTips;
	protected ProgressBar pb;
	protected TextAreaField runScript, customModule;
	protected SimpleBooleanProperty isShowAdvancedPane = new SimpleBooleanProperty(false);

	protected String splashScreenTipsText = "";

	@Override
	public void start(Stage stage) throws Exception {
		// pom.xml
		Label labelPom = new Label("pom.xml：");
		pom = new TextField();
		pom.setPromptText("Maven pom.xml 文件位置");
		selectPom = new Button("选择");
		BorderPaneGroup pomPane = new BorderPaneGroup();
		pomPane.setCenter(pom);
		pomPane.setRight(selectPom);
		// 图标
		Label labelIcon = new Label("图标：");
		icon = new ImageView(ICON_DEFAULT);
		selectIcon = new Button("选择");
		backIcon = new Button("还原默认");
		backIcon.setVisible(false);
		HBox iconPane = new HBox(20, icon, selectIcon, backIcon);
		iconPane.setAlignment(Pos.CENTER_LEFT);
		// 启动页
		Label labelSplashScreen = new Label("启动页：");
		splashScreen = new TextField();
		selectSplashScreen = new Button("选择");
		removeSplashScreen = new Button("移除");
		splashScreenTips = new PopupTips();
		splashScreenTips.setOnShow((root, node) -> {
			if (node instanceof ImageView) {
				root.setBorder(Border.EMPTY);
				root.setBackground(BG_TP);
			}
		});
		PopupTipsService.install(splashScreen, splashScreenTips);
		BorderPaneGroup splashScreenPane = new BorderPaneGroup();
		splashScreenPane.setCenter(splashScreen);
		splashScreenPane.setRight(new HBoxGroup(selectSplashScreen, removeSplashScreen));
		// 导出
		Label labelExport = new Label("导出到：");
		out = new TextField();
		Exe4FX.config.bindTextProperty(out, "outPath");
		selectOut = new Button("选择");
		openOut = new Button("打开");
		BorderPaneGroup exportPane = new BorderPaneGroup();
		exportPane.setCenter(out);
		exportPane.setRight(new HBoxGroup(selectOut, openOut));
		// 基本选项面板
		final ColumnConstraints bsCol0 = new ColumnConstraints();
		final ColumnConstraints bsCol1 = new ColumnConstraints();
		bsCol0.setHalignment(HPos.RIGHT);
		bsCol1.setHgrow(Priority.ALWAYS);

		GridPane baseSettings = new GridPane();
		baseSettings.setHgap(4);
		baseSettings.setVgap(6);
		baseSettings.getColumnConstraints().addAll(bsCol0, bsCol1);
		baseSettings.addColumn(0, labelPom, labelIcon, labelSplashScreen, labelExport);
		baseSettings.addColumn(1, pomPane, iconPane, splashScreenPane, exportPane);

		// 说明和关于
		explain = new Button("说明");
		explain.setPrefHeight(27);
		about = new Button("关于");
		about.setPrefHeight(27);
		HBoxGroup ctrlLeft = new HBoxGroup(explain, about);

		// 控制
		advancedIcon = new Label(">");
		advancedIcon.setPadding(new Insets(0, 4, 2, 2));
		advanced = new Button("高级", advancedIcon);
		run = new Button("执行");
		run.setPrefSize(90, 27);
		HBox ctrlRight = new HBox(6, advanced, run);

		// 中间按钮
		BorderPane ctrlPane = new BorderPane();
		ctrlPane.setPadding(new Insets(12, 0, 12, 0));
		ctrlPane.setLeft(ctrlLeft);
		ctrlPane.setRight(ctrlRight);

		// JDK
		Label labelJDK = new Label("JDK：");
		jdk = new TextField();
		jdk.setPromptText("JDK 11 及以上路径");
		Exe4FX.config.bindTextProperty(jdk, "jdkPath");
		PopupTipsService.install(jdk, "示例：C:\\Program Files\\Java\\jdk-16.0.1");
		selectJDK = new Button("选择");
		BorderPaneGroup jdkPane = new BorderPaneGroup();
		jdkPane.setCenter(jdk);
		jdkPane.setRight(selectJDK);
		// FX SDK
		Label labelSDK = new Label("JavaFX SDK：");
		sdk = new TextField();
		sdk.setPromptText("JavaFX SDK 位置");
		selectSDK = new Button("选择");
		BorderPaneGroup sdkPane = new BorderPaneGroup();
		sdkPane.setCenter(sdk);
		sdkPane.setRight(selectSDK);
		// FX jmods
		Label labelJmods = new Label("JavaFX jmods：");
		jmods = new TextField();
		jmods.setPromptText("JavaFX jmods 位置");
		selectJmods = new Button("选择");
		BorderPaneGroup jmodsPane = new BorderPaneGroup();
		jmodsPane.setCenter(jmods);
		jmodsPane.setRight(selectJmods);
		// 运行脚本
		Label labelRunScript = new Label("运行脚本：");
		runScript = new TextAreaField(Exe4FX.config.getString("runScript"));
		final String runScriptTips = "%fileName%：文件名";
		PopupTipsService.install(runScript.getTextField(), runScriptTips);
		PopupTipsService.install(runScript.getTextArea(), runScriptTips);
		// 附加模块
		Label labelCustomModule = new Label("附加模块：");
		customModule = new TextAreaField(Exe4FX.config.getString("customModule"));
		PopupTipsService.install(customModule.getTextField(), "添加 jdeps 可能扫描不到的模块（半角逗号分隔）：\n\tHTTPS SSL 请求：jdk.crypto.ec");
		// X64 架构
		Label labelX64 = new Label("X64：");
		isX64 = new Switch();
		Exe4FX.config.bindSelectedProperty(isX64.selectedProperty(), "x64");
		// 隐藏控制台
		Label labelInvisible = new Label("隐藏控制台：");
		isInvisible = new Switch();
		Exe4FX.config.bindSelectedProperty(isInvisible.selectedProperty(), "invisible");
		// UPX 压缩
		Label labelUPX = new Label("使用 UPX 压缩：");
		useUPX = new Switch();
		Exe4FX.config.bindSelectedProperty(useUPX.selectedProperty(), "useUPX");
		// 需要管理员运行
		Label labelNeedAdmin = new Label("需要管理员运行：");
		needAdmin = new Switch();
		Exe4FX.config.bindSelectedProperty(needAdmin.selectedProperty(), "needAdmin");
		// 文件名
		Label labelFileName = new Label("文件名：");
		fileName = new TextField();
		// 内部名称
		Label labelInternalName = new Label("内部名称：");
		internalName = new TextField();
		// 文件版本
		Label labelFileVersion = new Label("文件版本：");
		fileVersion = new TextField();
		// 产品版本
		Label labelProductVersion = new Label("产品版本：");
		productVersion = new TextField();
		// 描述
		Label labelDescription = new Label("描述：");
		description = new TextField();
		// 公司
		Label labelCompany = new Label("公司：");
		company = new TextField();
		Exe4FX.config.bindTextProperty(company, "company");
		// 商标
		Label labelTrademarks = new Label("商标：");
		trademarks = new TextField();
		Exe4FX.config.bindTextProperty(trademarks, "trademarks");
		// 版权
		Label labelCopyright = new Label("版权：");
		copyright = new TextField();
		Exe4FX.config.bindTextProperty(copyright, "copyright");
		// 说明
		Label labelComments = new Label("说明：");
		comments = new TextField();

		// 高级设置面板
		final ColumnConstraints asCol0 = new ColumnConstraints();
		final ColumnConstraints asCol1 = new ColumnConstraints();
		final ColumnConstraints asCol2 = new ColumnConstraints();
		final ColumnConstraints asCol3 = new ColumnConstraints();
		asCol0.setHalignment(HPos.RIGHT);
		asCol1.setHgrow(Priority.ALWAYS);
		asCol2.setHalignment(HPos.RIGHT);
		asCol3.setHgrow(Priority.ALWAYS);

		final Insets vInsets = new Insets(12, 0, 6, 0);
		advancedSettings = new GridPane();
		advancedSettings.setPadding(vInsets);
		advancedSettings.setBorder(BORDER_TOP);
		advancedSettings.setHgap(4);
		advancedSettings.setVgap(6);
		advancedSettings.getColumnConstraints().addAll(asCol0, asCol1, asCol2, asCol3);
		advancedSettings.add(jdkPane, 1, 0, 3, 1);
		advancedSettings.add(sdkPane, 1, 1, 3, 1);
		advancedSettings.add(jmodsPane, 1, 2, 3, 1);
		advancedSettings.add(runScript, 1, 3, 3, 1);
		advancedSettings.add(customModule, 1, 4, 3, 1);
		advancedSettings.addColumn(0, labelJDK, labelSDK, labelJmods, labelRunScript, labelCustomModule, labelX64, labelUPX, labelFileName, labelInternalName, labelDescription, labelTrademarks, labelComments);
		advancedSettings.addColumn(1, isX64, useUPX, fileName, internalName, description, trademarks);
		advancedSettings.addColumn(2, labelInvisible, labelNeedAdmin, labelFileVersion, labelProductVersion, labelCompany, labelCopyright);
		advancedSettings.addColumn(3, isInvisible, needAdmin, fileVersion, productVersion, company, copyright);
		advancedSettings.add(comments, 1, 11, 3, 1);

		// 进度
		pb = new ProgressBar();
		pb.setPrefHeight(16);

		// 所有设置
		settings = new VBox(6, baseSettings, ctrlPane);
		settings.getChildren().add(pb);
		settings.setPadding(vInsets);
		pb.prefWidthProperty().bind(settings.widthProperty());

		// 日志
		log = new TextArea();
		log.setEditable(false);

		logClean = new Label("清空");
		logClean.setPadding(new Insets(2, 6, 2, 6));
		logClean.setBackground(new BgFill(WHITE).build());
		logClean.setCursor(Cursor.HAND);
		logClean.setTextFill(Paint.valueOf("#0096C9"));
		logClean.underlineProperty().bind(logClean.hoverProperty());
		logClean.visibleProperty().bind(log.hoverProperty().or(logClean.hoverProperty()));

		AnchorPane logPane = new AnchorPane(log, logClean);
		XAnchorPane.def(log);
		XAnchorPane.def(logClean, 4, 8, null, null);

		// 根布局
		BorderPane root = new BorderPane();
		root.setBorder(BORDER_TOP);
		root.setPadding(new Insets(8));
		root.setTop(settings);
		root.setCenter(logPane);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(CSS);
		stage.getIcons().add(ICON_DEFAULT);
		stage.setScene(scene);
		stage.setTitle("Exe4FX - 夜雨");
		stage.setWidth(860);
		stage.setHeight(820);

		// 关闭启动页
		if (SplashScreen.getSplashScreen() != null) SplashScreen.getSplashScreen().close();
		stage.show();
	}

	/** 显示高级设置 */
	protected void showAdvancedSettings() {
		if (!settings.getChildren().contains(advancedSettings)) {
			settings.getChildren().add(2, advancedSettings);
			advancedIcon.setRotate(90);
		}
	}

	/** 隐藏高级设置 */
	protected void hideAdvancedSettings() {
		settings.getChildren().remove(advancedSettings);
		advancedIcon.setRotate(0);
	}
}