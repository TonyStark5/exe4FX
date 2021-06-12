package net.imyeyu.exe4FX.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.imyeyu.betterfx.BetterFX;
import net.imyeyu.betterfx.component.dialog.Alert;

/**
 * 说明视图
 *
 * 夜雨 创建于 2021-06-04 01:12
 */
public class ViewExplain extends Stage implements BetterFX {

	protected TextArea tips, pom;

	public ViewExplain() {
		tips = new TextArea();
		tips.setPrefHeight(220);
		tips.setEditable(false);
		pom = new TextArea();
		pom.setEditable(false);

		BorderPane root = new BorderPane();
		BorderPane.setMargin(tips, new Insets(0, 0, 10, 0));
		root.setPadding(new Insets(10));
		root.setBorder(BORDER_TOP);
		root.setTop(tips);
		root.setCenter(pom);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(CSS);
		getIcons().add(Alert.ICON_INFO);
		initModality(Modality.APPLICATION_MODAL);
		setScene(scene);
		setTitle("说明");
		setWidth(920);
		setHeight(860);
	}
}