package net.imyeyu.exe4FX.ctrl;

import javafx.stage.Stage;
import net.imyeyu.exe4FX.view.ViewMain;

/**
 * 夜雨 创建于 2021-06-02 17:00
 */
public class Main extends ViewMain {

	@Override
	public void start(Stage stage) throws Exception {
		super.start(stage);

		isShowAdvancedPane.addListener((obs, o, isShowAdvancedPane) -> {
			if (isShowAdvancedPane) {
				settings.getChildren().add(2, advancedSettings);
				advancedIcon.setRotate(90);
			} else {
				settings.getChildren().remove(advancedSettings);
				advancedIcon.setRotate(0);
			}
		});
		advanced.setOnAction(event -> {
			isShowAdvancedPane.set(!isShowAdvancedPane.get());
		});
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}
}