/*
 * 作成日: 2009/03/15
 */
package jp.ac.fit.asura.naoji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jp.ac.fit.asura.naoji.jal.JALBroker;
import jp.ac.fit.asura.naoji.jal.JALModule;
import sun.misc.Service;
import sun.misc.ServiceConfigurationError;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiModule extends JALModule {
	private List<ClassLoader> loaders;

	static {
		System.err.println("Loading naojin library.");
		System.loadLibrary("naojin");
	}

	private JALBroker pBroker;

	private NaojiContext context;
	private List<Naoji> brothers;

	/**
	 *
	 */
	public NaojiModule(long ptr) {
		objPtr = ptr;
		long brokerPtr = _createJALBroker(objPtr);
		pBroker = new JALBroker(brokerPtr);
		loaders = new ArrayList<ClassLoader>();
		brothers = new ArrayList<Naoji>();
	}

	public void init() {
		System.err.println("NaojiModule init called.");

		initClassLoaders();
		loadNaojiModules();

		context = new NaojiContext(this);
		for (Naoji naoji : brothers) {
			try {
				naoji.init(context);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.err.println("NaojiModule initilized.");
	}

	/**
	 * モジュールのクラスローダーを初期化します.
	 *
	 * モジュールはnaoji.modulesファイルから読み込まれます.
	 *
	 * このファイルは一行ごとにURLClassLoaderの引数を記述します.
	 *
	 * 引数は空白文字で区切られたURLのリストです.
	 *
	 */
	private void initClassLoaders() {
		// クラスローダーを初期化
		System.err.println("Loading Classloaders.");
		URL naojiList = ClassLoader.getSystemResource("naoji.modules");

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					naojiList.openStream()));

			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty())
					continue;
				if (line.startsWith("#") || line.startsWith("//"))
					continue;
				System.err.println("Load " + line);
				String[] cpStrings = line.split("\\s+");
				URLClassLoader loader = null;
				try {
					URL[] urls = new URL[cpStrings.length];
					for (int i = 0; i < cpStrings.length; i++)
						urls[i] = new URL(cpStrings[i]);
					loader = new URLClassLoader(urls);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				if (loader != null) {
					loaders.add(loader);
					System.err.println("Add Classloader:" + loader.toString()
							+ " " + Arrays.toString(loader.getURLs()));
				} else {
					System.err.println("Add Classloader failed. line:" + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadNaojiModules() {
		// Naojiインスタンスを読み込み
		for (ClassLoader cl : loaders) {
			try {
				Iterator<NaojiFactory> it = Service.providers(
						NaojiFactory.class, cl);
				while (it.hasNext()) {
					NaojiFactory f = it.next();
					System.err.println("Loading factory:" + f);
					try {
						brothers.add(f.create());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (ServiceConfigurationError e) {
				e.printStackTrace();
			}
		}
	}

	public void start() {
		System.err.println("NaojiModule start called.");
		for (Naoji naoji : brothers) {
			start(naoji);
		}
		System.err.println("NaojiModule started.");
	}

	public void exit() {
		System.err.println("NaojiModule exit called.");
		stop();
		for (Naoji naoji : brothers) {
			try {
				naoji.exit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dispose();
	}

	protected void start(final Naoji naoji) {
		System.err.println("NaojiModule start " + naoji);
		try {
			naoji.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * すべてのスレッドを停止し、モジュールを終了可能状態にします.
	 *
	 * 呼び出しは終了可能状態になるまでブロックされます.
	 *
	 */
	protected void stop() {
		for (Naoji naoji : brothers) {
			try {
				naoji.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void test() {
		System.out.println("Hello World from JNI!");
	}

	public static int testSquare(int i) {
		return i * i;
	}

	public JALBroker getParentBroker() {
		return pBroker;
	}

	public void dispose() {
	}

	native static private long _createJALBroker(long objPtr);
}
