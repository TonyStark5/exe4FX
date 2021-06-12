package net.imyeyu.exe4FX.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.imyeyu.betterfx.BetterFX;
import net.imyeyu.betterfx.component.XHyperlink;
import net.imyeyu.betterfx.component.dialog.Alert;
import net.imyeyu.betterfx.util.MinecraftFont;

/**
 * 关于视图
 *
 * 夜雨 创建于 2021-06-02 22:18
 */
public class ViewAbout extends Stage implements BetterFX {

	protected static final String VERSION = "1.0.0";

	protected Label name;
	protected XHyperlink version, converter, betterJava, betterFX, blog;

	public ViewAbout() {
		VBox center = new VBox();
		name = new Label("Exe4FX", new ImageView("/logo.png"));
		name.setCursor(Cursor.HAND);
		MinecraftFont.css(name, MinecraftFont.M);
		converter = new XHyperlink("https://www.battoexeconverter.com/");
		converter.setBorder(Border.EMPTY);
		converter.setTextAlignment(TextAlignment.CENTER);
		betterJava = new XHyperlink("https://github.com/imyeyu/BetterJava", "BetterJava 1.1.5");
		betterJava.setTextAlignment(TextAlignment.CENTER);
		betterFX = new XHyperlink("https://www.imyeyu.net/article/public/aid136.html", "BetterFX 1.3.4");
		betterFX.setTextAlignment(TextAlignment.CENTER);

		version = new XHyperlink(null, VERSION);
		version.setTextAlignment(TextAlignment.CENTER);
		version.getLabel().setWrapText(false);
		center.setAlignment(Pos.TOP_CENTER);
		center.setSpacing(4);
		center.getChildren().addAll(name, version, converter, betterJava, betterFX);

		VBox bottom = new VBox();
		Label developer = new Label("开发者：夜雨");
		Label labelBlog = new Label("个人博客：");
		blog = new XHyperlink("https://www.imyeyu.net");
		HBox blogPane = new HBox(labelBlog, blog);
		blogPane.setAlignment(Pos.CENTER);

		Label cr = new Label("Copyright © 夜雨 2021 All Rights Reserved 版权所有");
		bottom.setSpacing(4);
		bottom.setAlignment(Pos.CENTER);
		bottom.getChildren().addAll(developer, blogPane, cr);

		BorderPane root = new BorderPane();
		root.setCenter(center);
		root.setBottom(bottom);
		root.setPadding(new Insets(8));
		root.setBorder(BORDER_TOP);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(CSS);
		setScene(scene);
		getIcons().add(Alert.ICON_INFO);
		setTitle("关于");
		setWidth(520);
		setHeight(360);
		initModality(Modality.APPLICATION_MODAL);
	}
}