package net.imyeyu.exe4FX.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.imyeyu.betterfx.BetterFX;
import net.imyeyu.exe4FX.service.SDKDownloader;

/**
 * 夜雨 创建于 2021-06-06 23:01
 */
public class ViewSDKDownload extends Stage implements BetterFX {

	private static final Image ICON = new Image("download.png");

	protected Label tips, speed;
	protected Button ctrl;
	protected ProgressBar pb;
	protected ComboBox<SDKDownloader.API> api;

	public ViewSDKDownload() {
		// API 选择
		Label labelAPI = new Label("下载源：");
		api = new ComboBox<>();
		api.setConverter(new StringConverter<>() {

			@Override
			public String toString(SDKDownloader.API api) {
				if (api == SDKDownloader.API.OPEN_JFX) {
					return "官方";
				} else {
					return "夜雨博客";
				}
			}

			@Override
			public SDKDownloader.API fromString(String string) {
				return null;
			}
		});
		api.getItems().addAll(SDKDownloader.API.OPEN_JFX, SDKDownloader.API.BLOG);
		api.setValue(SDKDownloader.API.OPEN_JFX);
		HBox apiPane = new HBox(labelAPI, api);
		apiPane.setAlignment(Pos.CENTER_LEFT);

		// 文本提示
		tips = new Label();
		tips.setTextFill(GRAY);
		tips.setPadding(new Insets(0, 4, 0, 4));
		// 速度提示
		speed = new Label();
		speed.setTextFill(GRAY);
		// 顶部
		BorderPane top = new BorderPane();
		BorderPane.setAlignment(speed, Pos.CENTER_RIGHT);
		top.setCenter(apiPane);
		top.setRight(speed);
		// 进度
		pb = new ProgressBar();
		pb.setPrefHeight(14);
		pb.prefWidthProperty().bind(widthProperty());
		// 控制
		ctrl = new Button("开始下载");
		// 根容器
		VBox root = new VBox(12, top, pb, tips, ctrl);
		root.setFillWidth(true);
		root.setPadding(new Insets(2, 8, 2, 8));
		root.setAlignment(Pos.CENTER);
		root.setBorder(BORDER_TOP);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(CSS);
		setScene(scene);
		getIcons().add(ICON);
		initModality(Modality.APPLICATION_MODAL);
		setTitle("下载 JavaFX SDK");
		setWidth(520);
		setHeight(180);
	}
}