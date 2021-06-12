package net.imyeyu.exe4FX.core;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import net.imyeyu.betterjava.IO;
import net.imyeyu.exe4FX.Exe4FX;
import net.imyeyu.exe4FX.bean.PackageBuilderException;
import net.imyeyu.exe4FX.bean.Params;
import net.imyeyu.exe4FX.service.ico.ICOEncoder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.imageio.ImageIO;
import javax.naming.NoPermissionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>打包执行。value 返回打包后整个项目大小（字节）
 * <ul>
 *     <li>检验 pom.xml 文件</li>
 *     <li>通过 mvn package 打包</li>
 *     <li>复制可执行 jar 到输出目录</li>
 *     <li>检验 JDK</li>
 *     <li>解析该可执行程序所使用的模块</li>
 *     <li>生成精简 JRE 到输出目录</li>
 *     <li>生成 bat 并转为 exe 到输出目录</li>
 * </ul>
 *
 * 夜雨 创建于 2021-06-05 00:32
 */
@Slf4j
public class PackageBuilder extends Service<Long> {

	private static final String SEP = File.separator;

	private final Params params;
	private final CMDRunner runner;

	private double nowPB = 0; // 当前进度
	private double maxPB = 0; // 最大允许进度（由每个步骤决定），取值范围 [0, 100]

	public PackageBuilder(CMDRunner runner, Params params) {
		this.runner = runner;
		this.params = params;
	}

	@Override
	protected Task<Long> createTask() {
		return new Task<Long>() {

			private File jdeps;                                   // jdeps，checkJDK() 后才有
			private File jlink;                                   // jlink，checkJDK() 后才有
			private File pomFile;                                 // pom 文件
			private File jarFile;                                 // 编译后 jar 文件（maven）
			private File outJarFile;                              // 在输出目录的 jar 文件
			private Document pom;                                 // pom.xml 文档对象
			private Set<String> jdepsResult;                      // jdeps 执行结果
			private final String workEnv = "work" + SEP;          // 转换工作环境
			private final String outEnv  = params.getOut() + SEP; // 输出环境

			@Override
			protected Long call() throws Exception {
				log.info("## 开始构造 ##");
				runner.setOnUpdate(this::pb);
				updateProgress(0, 1);

				// 工作文件夹
				File work = new File("work");
				if (!work.exists() && !work.mkdirs()) {
					throw new NoPermissionException("无权限创建工作文件夹 work，无法继续");
				}
				// 开始流程
				checkPOM();    // 校验 pom.xml
				runPackage();  // 执行 Maven 打包
				copyRunJar();  // 复制可执行 jar
				checkJDK();    // 校验 JDK
				parseModule(); // 解析引用模块
				buildJRE();    // 构建 jre
				buildEXE();    // 生成 exe

				// 计算项目大小
				Long size = IO.calcSize(new File(params.getOut()));

				updateProgress(100, 100);
				Thread.sleep(500);
				updateProgress(-1, -1);
				log.info("## 构造完成 ##");
				return size;
			}

			/**
			 * 校验 pom.xml
			 *
			 * @throws PackageBuilderException 执行异常
			 * @throws DocumentException pom.xml 解析异常
			 */
			public void checkPOM() throws PackageBuilderException, DocumentException {
				nowPB = 0;
				maxPB = 8;

				pomFile = new File(params.getPom());
				if (!pomFile.exists() || pomFile.isDirectory() || !pomFile.canRead()) {
					throw new PackageBuilderException("无法加载 pom.xml：", pomFile.getAbsolutePath());
				}
				// pom 文件数据
				String pomString = IO.toString(pomFile);
				pom = new SAXReader().read(pomFile);
				if (!pomString.contains("maven-assembly-plugin")) {
					throw new PackageBuilderException("pom.xml 构建插件缺少 maven-assembly-plugin", PackageBuilderException.Action.OPEN_EXPLAIN);
				}
				// 启动页
				String splashScreen = params.getSplashScreen();
				// MANIFEST.MF 插入启动页路径参数
				Element pluginsEl = pom.getRootElement().element("build").element("plugins");
				List<?> plugins = pluginsEl.elements();
				for (int i = 0; i < plugins.size(); i++) {
					if (plugins.get(i) instanceof Element plugin) {
						// maven-assembly-plugin 插件配置
						if (plugin.element("artifactId").getText().equals("maven-assembly-plugin")) {
							Element archive = plugin.element("configuration").element("archive");
							// configuration > manifestEntries > SplashScreen-Image[启动页地址]
							Element manifestEntries = archive.element("manifestEntries");
							if (manifestEntries == null) {
								archive.addElement("manifestEntries");
								manifestEntries = archive.element("manifestEntries");
							}
							Element ssi = manifestEntries.element("SplashScreen-Image");
							if (ssi == null) {
								manifestEntries.addElement("SplashScreen-Image");
								ssi = manifestEntries.element("SplashScreen-Image");
							}
							boolean requestUpdatePOM = false;
							// pom 有启动页但用户配置没有
							if ("".equals(ssi.getText().trim()) && (splashScreen == null || !splashScreen.equals(""))) {
								ssi.setText("");
								requestUpdatePOM = true;
							}
							// pom 无启动页但用户配置有
							if (splashScreen != null && !splashScreen.equals("") && !ssi.getText().equals("splash-screen.png")) {
								ssi.setText("splash-screen.png");
								requestUpdatePOM = true;

								// 复制启动页到项目
								File sourceSSI = new File(splashScreen);
								File targetSSI = new File(pomFile.getParent() + SEP + "src" + SEP + "main" + SEP + "resources" + SEP + "splash-screen.png");
								// 复制到项目资源文件夹
								if (!sourceSSI.getAbsolutePath().equals(targetSSI.getAbsolutePath())) {
									try {
										if (!targetSSI.getParentFile().exists()) {
											throw new PackageBuilderException("找不到项目资源文件夹：" + targetSSI.getParent());
										}
										if (targetSSI.exists()) {
											if (!targetSSI.delete()) {
												throw new PackageBuilderException("无法删除已存在的启动页：", targetSSI.getAbsolutePath());
											}
										}
										Files.copy(sourceSSI.toPath(), targetSSI.toPath());
									} catch (IOException e) {
										throw new PackageBuilderException("无法复制启动页到项目中：" + e.getMessage());
									}
								}
							}
							if (requestUpdatePOM) {
								// 更新 pom.xml
								try {
									FileOutputStream fos = new FileOutputStream(pomFile);
									OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
									XMLWriter writer = new XMLWriter(osw);
									writer.write(pom);
									osw.flush();
									writer.close();
									osw.close();
									fos.close();
								} catch (IOException e) {
									throw new PackageBuilderException("无法修改 pom.xml 插入启动页参数：" + e.getMessage());
								}
							}
							break;
						}
					}
				}
			}

			/**
			 * Maven 打包
			 *
			 * @throws PackageBuilderException 执行异常
			 */
			public void runPackage() throws PackageBuilderException {
				nowPB = 8;
				maxPB = 40;

				String fileName = params.getFileName();
				if (fileName == null || fileName.equals("")) {
					throw new PackageBuilderException("文件名不能为空", PackageBuilderException.Action.FOCUS_FILE_NAME);
				}
				// 进入目录
				if (!runner.toPath(pomFile.getParent())) {
					throw new PackageBuilderException("无法进入该目录：", pomFile.getParent());
				}
				// Maven 打包
				if (!runner.run("mvn package")) {
					throw new PackageBuilderException("Maven 打包异常，请检查 pom.xml 构建参数", PackageBuilderException.Action.OPEN_EXPLAIN);
				}
				// 校验打包文件
				{
					Element root = pom.getRootElement();
					// 在项目 target 的携带依赖的 jar
					String jarFileName = root.element("artifactId").getText() + "-" + root.element("version").getText() + "-jar-with-dependencies.jar";
					jarFile = new File(pomFile.getParent() + SEP + "target" + SEP + jarFileName);
					if (!jarFile.isFile() || !jarFile.exists() || !jarFile.canRead()) {
						throw new PackageBuilderException("Maven 构造的 jar 文件不存在或无法读取，无法继续进行：", jarFile.getAbsolutePath());
					}
				}
			}

			/**
			 * 复制 Maven 构造的 jar 到输出目录
			 *
			 * @throws PackageBuilderException 执行异常
			 */
			private void copyRunJar() throws PackageBuilderException {
				nowPB = 40;
				maxPB = 42;

				String fileName = params.getFileName().substring(0, params.getFileName().lastIndexOf("."));
				outJarFile = new File(outEnv + fileName + ".jar");
				try {
					Files.copy(jarFile.toPath(), outJarFile.toPath());
				} catch (IOException e) {
					throw new PackageBuilderException("无法复制 Maven 构造的 jar 程序到输出目录：", outJarFile.getAbsolutePath());
				}
			}

			/**
			 * 校验 JDK
			 *
			 * @throws PackageBuilderException 执行异常
			 */
			private void checkJDK() throws PackageBuilderException {
				nowPB = 42;
				maxPB = 50;

				File jdkBin = new File(params.getJdk() + SEP + "bin");
				if (!jdkBin.exists() || !jdkBin.isDirectory()) {
					throw new PackageBuilderException("找不到 JDK：", jdkBin.getAbsolutePath(), PackageBuilderException.Action.FOCUS_JDK);
				}
				// 检验 jdeps
				jdeps = new File(jdkBin.getAbsolutePath() + SEP + "jdeps.exe");
				if (!jdeps.exists() || !jdeps.isFile() || !jdeps.canRead()) {
					throw new PackageBuilderException("找不到该 JDK 的 jdeps.exe：", jdeps.getAbsolutePath(), PackageBuilderException.Action.FOCUS_JDK);
				}
				// 检验 jlink
				jlink = new File(jdkBin.getAbsolutePath() + SEP + "jlink.exe");
				if (!jlink.exists() || !jlink.isFile() || !jlink.canRead()) {
					throw new PackageBuilderException("找不到该 JDK 的 jlink.exe：", jlink.getAbsolutePath(), PackageBuilderException.Action.FOCUS_JDK);
				}
			}

			/**
			 * 解析引用模块
			 *
			 * @throws PackageBuilderException 执行异常
			 */
			private void parseModule() throws PackageBuilderException {
				nowPB = 50;
				maxPB = 90;

				// SDK
				File sdk = new File(params.getSdk() + SEP + "lib");
				if (!sdk.exists() || !sdk.isDirectory() || !sdk.canRead()) {
					throw new PackageBuilderException("无法加载 JavaFX SDK：", sdk.getAbsolutePath(), PackageBuilderException.Action.OPEN_SDK_DOWNLOADER);
				}
				// 输出 jar 位置
				String absPathJar = outJarFile.getAbsolutePath();
				// 解析模块
				String modulePath = " --module-path " + qmValue(sdk.getAbsolutePath());
				String addModules = " --add-modules=ALL-MODULE-PATH " + qmValue(absPathJar);
				if (!runner.run(qmValue(jdeps.getAbsolutePath()) + modulePath + addModules)) {
					throw new PackageBuilderException("jdeps 解析引用模块异常：", runner.getLastCommand());
				}
				// 分细解析模块结果
				jdepsResult = new HashSet<>();
				String runnerResult = runner.getLastResult();
				String[] lines = runnerResult.split("\r\n|[\r\n]");
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i].trim();
					if (line.startsWith("requires")) {      // 找模块 requires mandated java.base (@15)
						String[] strings = line.split(" ");
						for (int j = 0; j < strings.length; j++) {
							if (strings[j].contains(".")) { // 找包 java.base
								jdepsResult.add(strings[j]);
							}
						}
					}
				}
				// 夹带私货
				jdepsResult.add("java.naming");
				jdepsResult.add("jdk.sctp");
				jdepsResult.add("java.sql");
				jdepsResult.add("jdk.charsets");
				jdepsResult.add("jdk.net");
				jdepsResult.add("java.rmi");
				// 自定义附加模块
				jdepsResult.addAll(Arrays.asList(params.getCustomModule().trim().split(",")));
			}

			/**
			 * 构造 JRE
			 *
			 * @throws PackageBuilderException 构造异常
			 */
			private void buildJRE() throws PackageBuilderException {
				nowPB = 90;
				maxPB = 95;

				// jdk 引用模块
				StringBuilder sb = new StringBuilder();
				for (String module : jdepsResult) {
					sb.append(module.trim()).append(',');
				}
				// javafx jmods 模块
				File jmods = new File(params.getJmods());
				if (!jmods.exists() || !jmods.isDirectory() || !jmods.canRead()) {
					throw new PackageBuilderException("无法加载 JavaFX jmods：", jmods.getAbsolutePath(), PackageBuilderException.Action.OPEN_SDK_DOWNLOADER);
				}
				// 执行模块化 JRE
				String jlinkPath = qmValue(jlink.getAbsolutePath());
				String jlinkParams = " --strip-debug --compress 2 --no-header-files --no-man-pages";
				String modulePath = " --module-path " + qmValue(jmods.getAbsolutePath());
				String output = " --output " + qmValue(new File(outEnv).getAbsolutePath() + SEP + "jre");
				String addModules = " --add-modules " + sb.substring(0, sb.length() - 1);
				if (!runner.run(jlinkPath + jlinkParams + modulePath + output + addModules)) {
					throw new PackageBuilderException("jlink 模块化 JRE 异常：", runner.getLastCommand());
				}
			}

			private void buildEXE() throws PackageBuilderException, IOException, InterruptedException {
				nowPB = 95;
				maxPB = 100;

				// 批处理脚本
				String fileName = params.getFileName().substring(0, params.getFileName().lastIndexOf("."));
				File batFile = new File(outEnv + fileName + ".bat");
				String batScript = params.getRunScript().replaceAll("%fileName%", fileName);
				log.info("运行脚本：" + batScript);
				IO.toFile(batFile, batScript);
				// 转换器
				File converter = new File(workEnv + "converter.exe");
				if (converter.length() == 0 || !converter.canRead() || !converter.exists()) {
					try {
						IO.jarFileToDisk("converter.exe", workEnv + "converter.exe");
					} catch (IOException e) {
						e.printStackTrace();
						throw new PackageBuilderException("无法导出转换程序 converter.exe");
					}
				}
				// 构造参数
				Map<String, String> exeParams = new LinkedHashMap<>(); // 转换器参数
				exeParams.put("bat", batFile.getAbsolutePath());
				exeParams.put("exe", batFile.getParentFile().getAbsolutePath() + SEP + params.getFileName());
				File icon;
				if (Exe4FX.config.has("cache-iconPath")) {
					// 自定义图标
					icon = new File(Exe4FX.config.getString("cache-iconPath"));
					if (!icon.exists() || !icon.canRead()) {
						throw new PackageBuilderException("无法读取图标：", icon.getAbsolutePath());
					}
				} else {
					// 默认图标
					icon = new File(workEnv + "icon.png");
					if (!icon.exists()) {
						try {
							IO.jarFileToDisk("icon.png", workEnv + "icon.png");
						} catch (IOException e) {
							throw new PackageBuilderException("无法导出默认图标：" + e.getMessage());
						}
					}
				}
				// 转 ICO 图标
				File icoOut = new File(outEnv + SEP + fileName + ".ico");
				ICOEncoder.write(ImageIO.read(icon), icoOut);
				exeParams.put("icon", icoOut.getAbsolutePath());
				exeParams.put("fileversion", params.getFileVersion());
				exeParams.put("productname", params.getInternalName());
				exeParams.put("productversion", params.getProductVersion());
				exeParams.put("internalname", params.getInternalName());
				exeParams.put("description", params.getDescription());
				exeParams.put("copyright", params.getCopyright());
				exeParams.put("company", params.getCompany());
				exeParams.put("trademarks", params.getTrademarks());
				exeParams.put("comments", params.getComments());
				// 参数拼接
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String, String> param : exeParams.entrySet()) {
					if (!"".equals(param.getValue().trim())) {
						sb.append(" /").append(param.getKey()).append(' ').append(qmValue(param.getValue()));
					}
				}
				if (params.isInvisible()) {
					sb.append(" /invisible");
				}
				if (params.isX64()) {
					sb.append(" /x64");
				}
				if (params.isNeedAdmin()) {
					sb.append(" /uac-admin");
				}
				if (params.isUseUPX()) {
					sb.append(" /upx");
				}
				// 稍等 PNG -> ICO 转码
				pb();
				Thread.sleep(3000);
				pb();
				// exe 编译命令
				String command = qmValue(converter.getAbsolutePath()) + " " + sb.toString().trim();
				// 独立 CMD 进程编译（CMDRunner 执行会乱码）
				log.info("构造命令 > " + command);
				try {
					Runtime.getRuntime().exec(command);
				} catch (Exception e) {
					throw new PackageBuilderException("exe 编译失败：", "异常：\n\t" + e.getMessage() + "\n执行命令：\n\t" + command);
				}
				// 检测结果
				pb();
				Thread.sleep(2000);
				pb();
				File exe = new File(outEnv + fileName + ".exe");
				for (int i = 0; i < 6; i++) {
					pb();
					if (exe.exists()) {
						break;
					} else {
						Thread.sleep(2000);
						if (i == 5) {
							throw new PackageBuilderException("exe 可能编译失败，检查输出文件夹和执行命令：", command);
						}
					}
				}
				// 可能编译了但没完全编译 :p
				pb();
				Thread.sleep(2000);
				pb();
				if (!batFile.delete() || !icoOut.delete()) {
					throw new PackageBuilderException("无法删除临时文件：", batFile.getAbsolutePath());
				}
			}

			/** 增加进度 */
			private void pb() {
				if (nowPB < maxPB) {
					nowPB += .4;
					updateProgress(nowPB, 100);
				}
			}
		};
	}

	/**
	 * 参数加引号
	 *
	 * @param value 参数
	 * @return 包括引号的参数
	 */
	private String qmValue(String value) {
		if (!value.startsWith("\"") && !value.endsWith("\"")) {
			return "\"" + value + "\"";
		}
		return value;
	}
}