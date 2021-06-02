package net.imyeyu.exe4FX.view;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.imyeyu.betterfx.BetterFX;
import net.imyeyu.betterfx.component.BorderPaneGroup;
import net.imyeyu.betterfx.component.HBoxGroup;
import net.imyeyu.betterfx.component.Switch;
import net.imyeyu.betterfx.service.PopupTipsService;

/**
 * 主界面视图
 *
 * 夜雨 创建于 2021-06-02 17:00
 */
public abstract class ViewMain extends Application {

	protected static final Image ICON_DEFAULT = new Image("/icon.png");

	protected VBox settings;
	protected Label advancedIcon;
	protected Button selectPom, selectIcon, selectExport, explain, about, advanced, run, selectJDK;
	protected Switch isX64, isInvisible, needAdmin, useUPX;
	protected TextArea log;
	protected GridPane advancedSettings;
	protected TextField pom, export, jdk, fileName, fileVersion, productVersion, internalName, description, company, trademarks, copyright, comments;
	protected ImageView icon;
	protected ProgressBar pb;
	protected SimpleBooleanProperty isShowAdvancedPane = new SimpleBooleanProperty(false);

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
		HBox iconPane = new HBox(24, icon, selectIcon);
		iconPane.setAlignment(Pos.CENTER_LEFT);
		// 导出
		Label labelExport = new Label("导出到：");
		export = new TextField();
		selectExport = new Button("选择");
		BorderPaneGroup exportPane = new BorderPaneGroup();
		exportPane.setCenter(export);
		exportPane.setRight(selectExport);
		// 基本选项面板
		final ColumnConstraints bsCol0 = new ColumnConstraints();
		final ColumnConstraints bsCol1 = new ColumnConstraints();
		bsCol0.setHalignment(HPos.RIGHT);
		bsCol1.setHgrow(Priority.ALWAYS);

		GridPane baseSettings = new GridPane();
		baseSettings.setHgap(4);
		baseSettings.setVgap(6);
		baseSettings.getColumnConstraints().addAll(bsCol0, bsCol1);
		baseSettings.addColumn(0, labelPom, labelIcon, labelExport);
		baseSettings.addColumn(1, pomPane, iconPane, exportPane);

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
		PopupTipsService.install(jdk, "示例：C:\\Program Files\\Java\\jdk-16.0.1");
		selectJDK = new Button("选择");
		BorderPaneGroup jdkPane = new BorderPaneGroup();
		jdkPane.setCenter(jdk);
		jdkPane.setRight(selectJDK);
		// X64 架构
		Label labelX64 = new Label("X64：");
		isX64 = new Switch();
		// 隐藏控制台
		Label labelInvisible = new Label("隐藏控制台：");
		isInvisible = new Switch();
		// UPX 压缩
		Label labelUPX = new Label("使用 UPX 压缩：");
		useUPX = new Switch();
		// 需要管理员运行
		Label labelNeedAdmin = new Label("需要管理员运行：");
		needAdmin = new Switch();
		// 文件名
		Label labelFileName = new Label("文件名：");
		fileName = new TextField();
		// 文件版本
		Label labelFileVersion = new Label("文件版本：");
		fileVersion = new TextField();
		// 产品版本
		Label labelProductVersion = new Label("产品版本：");
		productVersion = new TextField();
		// 内部名称
		Label labelInternalName = new Label("内部名称：");
		internalName = new TextField();
		// 描述
		Label labelDescription = new Label("描述：");
		description = new TextField();
		// 公司
		Label labelCompany = new Label("公司：");
		company = new TextField();
		// 商标
		Label labelTrademarks = new Label("商标：");
		trademarks = new TextField();
		// 版权
		Label labelCopyright = new Label("版权：");
		copyright = new TextField();
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

		final Insets vInsets = new Insets(6, 0, 6, 0);
		advancedSettings = new GridPane();
		advancedSettings.setPadding(vInsets);
		advancedSettings.setBorder(BetterFX.BORDER_TOP);
		advancedSettings.setHgap(4);
		advancedSettings.setVgap(6);
		advancedSettings.getColumnConstraints().addAll(asCol0, asCol1, asCol2, asCol3);
		advancedSettings.add(jdkPane, 1, 0, 3, 1);
		advancedSettings.addColumn(0, labelJDK, labelX64, labelUPX, labelFileName, labelProductVersion, labelDescription, labelTrademarks, labelComments);
		advancedSettings.addColumn(1, isX64, useUPX, fileName, productVersion, description, trademarks);
		advancedSettings.addColumn(2, labelInvisible, labelNeedAdmin, labelFileVersion, labelInternalName, labelCompany, labelCopyright);
		advancedSettings.addColumn(3, isInvisible, needAdmin, fileVersion, internalName, company, copyright);
		advancedSettings.add(comments, 1, 7, 3, 1);

		// 进度
		pb = new ProgressBar();
		pb.setPrefHeight(16);

		// 所有设置
		settings = new VBox(6, baseSettings, ctrlPane, pb);
		settings.setPadding(vInsets);
		pb.prefWidthProperty().bind(settings.widthProperty());

		// 日志
		log = new TextArea();
		log.setEditable(false);

		// 根布局
		BorderPane root = new BorderPane();
		root.setBorder(BetterFX.BORDER_TOP);
		root.setPadding(new Insets(8));
		root.setTop(settings);
		root.setCenter(log);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(BetterFX.CSS);
		stage.getIcons().add(ICON_DEFAULT);
		stage.setScene(scene);
		stage.setTitle("Exe4FX - 夜雨");
		stage.setWidth(860);
		stage.setHeight(720);
		stage.show();
	}
}