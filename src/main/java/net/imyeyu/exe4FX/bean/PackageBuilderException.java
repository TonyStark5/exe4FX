package net.imyeyu.exe4FX.bean;

/**
 * 构造异常
 *
 * 夜雨 创建于 2021-06-05 00:49
 */
public class PackageBuilderException extends Exception {

	private final Action action;   // 异常动作
	private final String areaText; // 文本域内容（为空时弹出普通弹窗 Alert，否则弹出 AlertTextArea）

	public enum Action {
		OPEN_EXPLAIN,
		OPEN_SDK_DOWNLOADER,
		FOCUS_FILE_NAME,
		FOCUS_JDK,
	}

	public PackageBuilderException() {
		this("");
	}
	public PackageBuilderException(String msg) {
		this(msg, null, null);
	}

	public PackageBuilderException(String msg, String areaText) {
		this(msg, areaText, null);
	}

	public PackageBuilderException(String msg, Action action) {
		this(msg, null, action);
	}

	public PackageBuilderException(String msg, String areaText, Action action) {
		super(msg);
		this.action = action;
		this.areaText = areaText;
	}

	public Action getAction() {
		return action;
	}

	public String getAreaText() {
		return areaText;
	}
}