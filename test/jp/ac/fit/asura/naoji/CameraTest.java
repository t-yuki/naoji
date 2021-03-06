/*
 * 作成日: 2009/04/19
 */
package jp.ac.fit.asura.naoji;

import java.io.IOException;

import jp.ac.fit.asura.naoji.i2c.I2Cdev;
import jp.ac.fit.asura.naoji.i2c.I2CdevTest;
import jp.ac.fit.asura.naoji.robots.NaoV3R;
import jp.ac.fit.asura.naoji.v4l2.V4L2Control;
import jp.ac.fit.asura.naoji.v4l2.V4L2PixelFormat;
import jp.ac.fit.asura.naoji.v4l2.Videodev;
import jp.ac.fit.asura.naoji.v4l2.VideodevTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class CameraTest extends TestCase {
	private Videodev video;
	private I2Cdev i2c;

	public static class Factory implements NaojiFactory {
		public Naoji create() {
			return new NaojiImpl();
		}
	}

	private static class NaojiImpl implements Naoji {
		Thread mainThread;

		public NaojiImpl() {
		}

		public void init(NaojiContext context) {
			mainThread = new Thread() {
				public void run() {
					try {
						Thread.sleep(10000);
						TestRunner.run(CameraTest.class);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		}

		public void exit() {
		}

		public void start() {
			System.err.println("NaojiTest start called.");
			mainThread.start();
		}

		public void stop() {
			System.err.println("NaojiTest stop called.");
			try {
				mainThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				assert false;
			}
		}
	}

	public void testSwitch() throws Exception {
		int res;

		res = i2c.init();
		assertEquals(0, res);

		res = i2c.getSelectedCamera();
		System.out.println("Current Camera:" + res);

		System.out.println("Select camera:" + NaoV3R.I2C_CAMERA_TOP);
		res = i2c.selectCamera(NaoV3R.I2C_CAMERA_TOP);
		assertEquals(0, res);

		assertEquals(0, video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0));
		assertEquals(0, video.setControl(V4L2Control.V4L2_CID_AUTOEXPOSURE, 0));
		assertEquals(0, video.setControl(
				V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE, 0));
		assertEquals(0, video.setControl(V4L2Control.V4L2_CID_AUTOGAIN, 0));
		assertEquals(0, video.setControl(V4L2Control.V4L2_CID_HFLIP, 1));
		assertEquals(0, video.setControl(V4L2Control.V4L2_CID_VFLIP, 1));

		// set format
		V4L2PixelFormat format = new V4L2PixelFormat();
		format.setWidth(320);
		format.setHeight(240);
		format.setPixelFormat(V4L2PixelFormat.PixelFormat.V4L2_PIX_FMT_YUYV
				.getFourccCode());
		res = video.setFormat(format);
		assertEquals(0, res);

		//
		res = video.setFPS(30);
		assertEquals(0, res);

		res = video.init(3);
		assertEquals("Result:" + res, 3, res);

		res = video.start();
		assertEquals(0, res);
		VideodevTest._testRetrieveImage(video, true);

		res = video.stop();
		assertEquals(0, res);

		i2c.selectCamera(NaoV3R.I2C_CAMERA_BOTTOM);
		assertEquals(0, video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0));
		assertEquals(0, video.setControl(V4L2Control.V4L2_CID_AUTOEXPOSURE, 0));
		assertEquals(0, video.setControl(
				V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE, 0));
		assertEquals(0, video.setControl(V4L2Control.V4L2_CID_AUTOGAIN, 0));
		res = video.setFormat(format);
		assertEquals(0, res);
		res = video.start();

		VideodevTest._testRetrieveImage(video, true);

		_testSwitchCamera1(NaoV3R.I2C_CAMERA_TOP);
		for (int i = 0; i < 10; i++)
			VideodevTest._testRetrieveImage(video, false);
		_testSwitchCamera1(NaoV3R.I2C_CAMERA_BOTTOM);

		for (int i = 0; i < 10; i++)
			VideodevTest._testRetrieveImage(video, false);
		_testSwitchCamera2(NaoV3R.I2C_CAMERA_TOP);
		for (int i = 0; i < 10; i++)
			VideodevTest._testRetrieveImage(video, false);
		_testSwitchCamera2(NaoV3R.I2C_CAMERA_BOTTOM);
		for (int i = 0; i < 10; i++)
			VideodevTest._testRetrieveImage(video, false);
		_testSwitchCamera3(NaoV3R.I2C_CAMERA_TOP);
		for (int i = 0; i < 10; i++)
			VideodevTest._testRetrieveImage(video, false);
		_testSwitchCamera3(NaoV3R.I2C_CAMERA_BOTTOM);
		for (int i = 0; i < 10; i++)
			VideodevTest._testRetrieveImage(video, false);

		System.out.println("_testSwitchCameraLoop1 1");
		_testSwitchCameraLoop1();
		System.out.println("_testSwitchCameraLoop1 2");
		_testSwitchCameraLoop1();
		System.out.println("_testSwitchCameraLoop2 1");
		_testSwitchCameraLoop2();
		System.out.println("_testSwitchCameraLoop2 2");
		_testSwitchCameraLoop2();
		System.out.println("testSwitch end");

		res = video.stop();
		assertEquals(0, res);
	}

	/*
	 * もっとも慎重なカメラの切り替え. 3秒ぐらいかかる.
	 *
	 * それぞれのカメラでパラメータの設定(ホワイトバランスなど)が必要.
	 */
	private void _testSwitchCamera1(int camera) throws IOException {
		System.out.println("_testSwitchCamera1");
		long beforeswitch = System.nanoTime();
		System.out.println("Switching camera...");

		int res = video.stop();
		assertEquals(0, res);

		video.dispose();

		System.out.println("Select camera:" + camera);
		i2c.selectCamera(camera);

		video = VideodevTest.createDevice();

		video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0);

		V4L2PixelFormat format = new V4L2PixelFormat();
		format.setWidth(320);
		format.setHeight(240);
		format.setPixelFormat(V4L2PixelFormat.PixelFormat.V4L2_PIX_FMT_YUYV
				.getFourccCode());
		res = video.setFormat(format);
		assertEquals(0, res);

		//
		res = video.setFPS(30);
		assertEquals(0, res);

		res = video.init(3);
		assertEquals("Result:" + res, 3, res);

		res = video.start();
		assertEquals(0, res);

		long afterswitch = System.nanoTime();
		System.out.println("Camera switched. time:"
				+ (afterswitch - beforeswitch) / 1e6 + " [ms]");

	}

	private void _testSwitchCameraLoop1() throws IOException {
		System.out.println("_testSwitchCameraLoop1");
		System.out.println("Switching camera...");

		long beginTime = System.nanoTime();
		int camera = NaoV3R.I2C_CAMERA_TOP;
		for (int i = 0; i < 100; i++) {
			System.out.println("_testSwitchCameraLoop1 Loop " + i);
			int res = video.stop();
			video.dispose();
			i2c.selectCamera(camera);
			video = VideodevTest.createDevice();
			video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0);
			V4L2PixelFormat format = new V4L2PixelFormat();
			format.setWidth(320);
			format.setHeight(240);
			format.setPixelFormat(V4L2PixelFormat.PixelFormat.V4L2_PIX_FMT_YUYV
					.getFourccCode());
			res = video.setFormat(format);
			res = video.setFPS(30);
			res = video.init(3);
			res = video.start();
			camera = camera == NaoV3R.I2C_CAMERA_TOP ? NaoV3R.I2C_CAMERA_BOTTOM
					: NaoV3R.I2C_CAMERA_TOP;
		}
		long time2 = System.nanoTime();
		System.out.println(" Camera switching time1:" + (time2 - beginTime)
				/ 1.0e6 / 100);
	}

	/*
	 * かなり手を抜いたカメラの切り替え. 10msぐらいかかる.
	 *
	 * それぞれのカメラでパラメータの設定(ホワイトバランスなど)が必要.
	 */
	private void _testSwitchCamera2(int camera) throws IOException {
		System.out.println("_testSwitchCamera2");
		long beforeswitch = System.nanoTime();
		System.out.println("Switching camera...");

		int res = video.stop();
		assertEquals(0, res);

		System.out.println("Select camera:" + camera);
		i2c.selectCamera(camera);

		res = video.start();
		assertEquals(0, res);

		long afterswitch = System.nanoTime();
		System.out.println("Camera switched. time:"
				+ (afterswitch - beforeswitch) / 1e6 + " [ms]");
	}

	private void _testSwitchCameraLoop2() throws IOException {
		System.out.println("_testSwitchCameraLoop2");
		System.out.println("Switching camera...");

		long beginTime = System.nanoTime();
		int camera = NaoV3R.I2C_CAMERA_TOP;
		for (int i = 0; i < 100; i++) {
			int res = video.stop();

			System.out.println("Select camera:" + camera);
			i2c.selectCamera(camera);

			res = video.start();
			camera = camera == NaoV3R.I2C_CAMERA_TOP ? NaoV3R.I2C_CAMERA_BOTTOM
					: NaoV3R.I2C_CAMERA_TOP;
		}
		long time2 = System.nanoTime();
		System.out.println(" Camera switching time2:" + (time2 - beginTime)
				/ 1.0e6 / 100);
	}

	/*
	 * 手を抜きすぎなカメラの切り替え. 5msぐらいかかる.
	 *
	 * それぞれのカメラでパラメータの設定(ホワイトバランスなど)が必要.
	 *
	 * 切り替え直後の数フレームは切り替える前のカメラの画像がきたり、画像が乱れたりする.
	 */
	private void _testSwitchCamera3(int camera) throws IOException {
		System.out.println("_testSwitchCamera3");
		long beforeswitch = System.nanoTime();
		System.out.println("Switching camera...");

		System.out.println("Select camera:" + camera);
		i2c.selectCamera(camera);

		long afterswitch = System.nanoTime();
		System.out.println("Camera switched. " + camera + " time:"
				+ (afterswitch - beforeswitch) / 1e6 + " [ms]");
	}

	@Override
	protected void setUp() throws Exception {
		video = VideodevTest.createDevice();
		i2c = I2CdevTest.createDevice();
	}

	@Override
	protected void tearDown() throws Exception {
		video.dispose();
		i2c.dispose();
	}

	public void testSwitchThread() throws Exception {
		System.out.println("testSwitchThread");
		int res;

		res = i2c.init();
		assertEquals(0, res);

		res = i2c.getSelectedCamera();
		System.out.println("Current Camera:" + res);

		System.out.println("Select camera:" + NaoV3R.I2C_CAMERA_TOP);
		res = i2c.selectCamera(NaoV3R.I2C_CAMERA_TOP);
		assertEquals(0, res);

		video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0);
		video.setControl(V4L2Control.V4L2_CID_AUTOEXPOSURE, 0);
		video.setControl(V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE, 0);
		video.setControl(V4L2Control.V4L2_CID_AUTOGAIN, 0);
		video.setControl(V4L2Control.V4L2_CID_HFLIP, 1);
		video.setControl(V4L2Control.V4L2_CID_VFLIP, 1);

		// set format
		V4L2PixelFormat format = new V4L2PixelFormat();
		format.setWidth(320);
		format.setHeight(240);
		format.setPixelFormat(V4L2PixelFormat.PixelFormat.V4L2_PIX_FMT_YUYV
				.getFourccCode());
		res = video.setFormat(format);
		assertEquals(0, res);

		//
		res = video.setFPS(30);
		assertEquals(0, res);

		res = video.init(3);
		assertEquals("Result:" + res, 3, res);

		res = video.start();
		assertEquals(0, res);
		// VideodevTest._testRetrieveImage(video, true);

		res = video.stop();
		assertEquals(0, res);

		i2c.selectCamera(NaoV3R.I2C_CAMERA_BOTTOM);
		video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0);
		video.setControl(V4L2Control.V4L2_CID_AUTOEXPOSURE, 0);
		video.setControl(V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE, 0);
		video.setControl(V4L2Control.V4L2_CID_AUTOGAIN, 0);
		res = video.setFormat(format);
		assertEquals(0, res);
		res = video.start();

		// VideodevTest._testRetrieveImage(video, true);

		Thread camThread = new Thread() {
			@Override
			public void run() {
				try {
					_testSwitchCamera1(NaoV3R.I2C_CAMERA_TOP);
					for (int i = 0; i < 10; i++)
						VideodevTest._testRetrieveImage(video, false);
					_testSwitchCamera1(NaoV3R.I2C_CAMERA_BOTTOM);

					for (int i = 0; i < 10; i++)
						VideodevTest._testRetrieveImage(video, false);
					_testSwitchCamera2(NaoV3R.I2C_CAMERA_TOP);
					for (int i = 0; i < 10; i++)
						VideodevTest._testRetrieveImage(video, false);
					_testSwitchCamera2(NaoV3R.I2C_CAMERA_BOTTOM);
					for (int i = 0; i < 10; i++)
						VideodevTest._testRetrieveImage(video, false);
					_testSwitchCamera3(NaoV3R.I2C_CAMERA_TOP);
					for (int i = 0; i < 10; i++)
						VideodevTest._testRetrieveImage(video, false);
					_testSwitchCamera3(NaoV3R.I2C_CAMERA_BOTTOM);
					for (int i = 0; i < 10; i++)
						VideodevTest._testRetrieveImage(video, false);
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		};
		camThread.start();
		camThread.join();
		res = video.stop();
		assertEquals(0, res);
	}
}
