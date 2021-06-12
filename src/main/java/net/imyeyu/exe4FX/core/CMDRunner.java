package net.imyeyu.exe4FX.core;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;

/**
 * <p>CMD 执行对象
 * <pre>
 *     CMDRunner runner = new CMDRunner();
 *     runner.start();
 *     // 执行命令（会阻塞，返回 true 为执行成功）
 *     runner.run("cd /");
 * </pre>
 *
 * 夜雨 创建于 2021-06-03 01:22
 */
@Slf4j
public class CMDRunner extends Service<String> {

	// 命令执行空间
	private String runSpace;
	// 线程运行状态，命令执行结果
	private boolean isRunning = false, isSuccessed = false;
	// 默认管道，错误管道
	private SyncPipe defSyncPipe, errSyncPipe;
	// 命令输出
	private PrintWriter writer;
	// 执行锁
	private final Object lock = new Object();
	// 上一次执行命令
	private String lastCommand;
	// 上一次命令执行结果
	private String lastResult;

	private OnReady onReady;
	private OnUpdate onUpdate;

	@Override
	protected Task<String> createTask() {
		return new Task<>() {
			@Override
			protected String call() throws Exception {
				Process process = Runtime.getRuntime().exec("cmd");
				writer = new PrintWriter(process.getOutputStream());

				// 默认管道
				defSyncPipe = new SyncPipe(process.getInputStream()) {

					@Override
					public synchronized void onUpdate(String bufferString) throws InterruptedException {
						super.onUpdate(bufferString);
						// 回调数据
						updateValue(bufferString);
						if (onUpdate != null) {
							onUpdate.handler();
						}
					}

					@Override
					public void onFinish(String result) {
						CMDRunner.this.lastResult = result;
						synchronized (lock) {
							CMDRunner.this.isSuccessed = true;
							lock.notify();
						}
					}
				};
				// 错误管道
				errSyncPipe = new SyncPipe(process.getErrorStream()) {

					@Override
					public synchronized void onUpdate(String bufferString) throws InterruptedException {
						super.onUpdate(bufferString);
						// 回调数据
						updateValue(bufferString);
						if (onUpdate != null) {
							onUpdate.handler();
						}
					}

					@Override
					public synchronized void onFinish(String result) {
						CMDRunner.this.lastResult = result;
						synchronized (lock) {
							CMDRunner.this.isSuccessed = false;
							lock.notify();
						}
					}
				};
				new Thread(defSyncPipe).start();
				new Thread(errSyncPipe).start();
				isRunning = true;
				if (onReady != null) {
					onReady.handler();
				}
				return null;
			}
		};
	}

	/**
	 * 运行命令
	 *
	 * @param command 命令
	 * @return true 为成功
	 */
	public synchronized boolean run(String command) {
		if (isRunning) {
			// 运行锁
			synchronized (lock) {
				lastResult = null;
				lastCommand = command;
				isSuccessed = false;

				log.info("执行命令 > " + command);
				writer.println(command);
				writer.flush();
				// 等待
				try {
					lock.wait();
					return isSuccessed;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return isSuccessed;
		} else {
			throw new RuntimeException("CMD 进程未运行");
		}
	}

	/** @return 当前运行空间 */
	public String getRunSpace() {
		String[] lines = lastResult.split("\r\n|[\r\n]");
		return lines[lines.length - 1];
	}

	/** @return 最新执行命令 */
	public String getLastCommand() {
		return lastCommand;
	}

	/** @return 最新执行结果 */
	public String getLastResult() {
		return lastResult;
	}

	public void setOnReady(OnReady onReady) {
		this.onReady = onReady;
	}

	public void setOnUpdate(OnUpdate onUpdate) {
		this.onUpdate = onUpdate;
	}

	/** 终止进程 */
	public void shutdown() {
		if (defSyncPipe != null) {
			defSyncPipe.shutdown();
		}
		if (errSyncPipe != null) {
			errSyncPipe.shutdown();
		}
		isRunning = false;
	}

	/**
	 * 前往目录
	 *
	 * @param path 位置
	 * @return true 为成功
	 */
	public boolean toPath(String path) {
		// 进入磁盘
		String disk = path.substring(0, 1).toUpperCase();
		if (!getRunSpace().startsWith(disk)) {
			if (!run(disk + ':')) {
				return false;
			}
		}
		// 进入目录
		return run("cd \"" + path.substring(2) + "\"");
	}

	/**
	 * 准备就绪事件
	 *
	 * 夜雨 创建于 2021-06-08 23:38
	 */
	public interface OnReady {
		void handler();
	}

	/**
	 * 更新事件
	 *
	 * 夜雨 创建于 2021-06-07 23:17
	 */
	public interface OnUpdate {
		void handler();
	}
}