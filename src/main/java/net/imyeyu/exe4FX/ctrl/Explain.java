package net.imyeyu.exe4FX.ctrl;

import javafx.scene.control.TextArea;
import net.imyeyu.betterfx.service.RunAsync;
import net.imyeyu.betterjava.IO;
import net.imyeyu.exe4FX.view.ViewExplain;

/**
 * 说明控制
 *
 * 夜雨 创建于 2021-06-09 17:23
 */
public class Explain extends ViewExplain {

	public Explain() {
		load(tips,"tips.txt");
		load(pom,"pom.txt");
	}

	private void load(TextArea textArea, String file) {
		if (textArea.getText() == null || "".equals(textArea.getText())) {
			new RunAsync<String>() {

				@Override
				public String call() throws Exception {
					return IO.jarFileToString(file);
				}

				@Override
				public void onFinish(String s) {
					textArea.setText(s);
				}
			}.start();
		}
	}
}