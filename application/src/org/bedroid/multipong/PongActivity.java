package org.bedroid.multipong;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.FindNameListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.Status;
import org.bedroid.multipong.service.PongService;
import org.bedroid.multipong.service.PongServiceInterface;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class PongActivity extends Activity implements Runnable {
	static {
		System.loadLibrary("alljoyn_java");
	}

	private static final String TAG = "PongActivity";

	// private static final int MESSAGE_ACTION = 1;
	private static final int MESSAGE_POST_TOAST = 2;

	private BusHandler mBusHandler;
	private Menu menu;
	private Thread mThread;
	private volatile boolean mStopped;

	/* UI Handler */
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/*
			 * case MESSAGE_ACTION: { mListViewArrayAdapter.add((String)
			 * msg.obj); break; }
			 */
			case MESSAGE_POST_TOAST:
				Toast.makeText(getApplicationContext(), (String) msg.obj,
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!mStopped) {
			try {
				// Send Hello
				Message msg = mBusHandler.obtainMessage(BusHandler.HELLO);
				mBusHandler.sendMessage(msg);
				// Wait a bit
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	public void start() {
		if (mThread == null) {
			mStopped = false;
			mThread = new Thread(this, "HelloSayer");
			mThread.start();
		}
	}

	public void stop() {
		if (mThread != null) {
			mStopped = true;
			mThread.interrupt();
			mThread = null;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.v(TAG, Pong.class.getSimpleName());

		/*
		 * Make all AllJoyn calls through a separate handler thread to prevent
		 * blocking the UI.
		 */
		HandlerThread busThread = new HandlerThread("BusHandler");
		busThread.start();
		mBusHandler = new BusHandler(busThread.getLooper());

		/* Start our service. */
		mBusHandler.sendEmptyMessage(BusHandler.CONNECT);

		start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.quit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Called when the activity is exited. */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		stop();

		mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
	}

	class BusHandler extends Handler {
		private static final String SERVICE_NAME = "org.bedroid.multipong.service";

		BusAttachment mBus;
		private ProxyBusObject mProxyObj;
		private PongService mService;
		private PongServiceInterface mPongServiceInterface;

		/* These are the messages sent to the BusHandler from the UI. */
		public static final int CONNECT = 1;
		public static final int DISCONNECT = 2;
		public static final int HELLO = 3;

		public BusHandler(Looper looper) {
			super(looper);

			mService = new PongService();
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CONNECT: {
				mBus = new BusAttachment("Pong",
						BusAttachment.RemoteMessage.Receive);// Receives from
																// remote
																// devices.

				Status status = mBus.registerBusObject(mService, "/hello");
				logStatus("BusAttachment.registerBusObject()", status);
				if (status != Status.OK) {
					finish();
					return;
				}

				status = mBus.connect(SERVICE_NAME);
				logStatus("BusAttachment.connect()", status);
				if (status != Status.OK) {
					finish();
					return;
				}

				// Get a remote object
				mProxyObj = mBus.getProxyBusObject(SERVICE_NAME, "/hello",
						new Class[] { PongServiceInterface.class });
				mPongServiceInterface = mProxyObj
						.getInterface(PongServiceInterface.class);

				status = mBus.findName(SERVICE_NAME, new FindNameListener() {
					public void foundName(String name, String guid,
							String namePrefix, String busAddress) {

						Status status = mProxyObj.connect(busAddress);
						logStatus("ProxyBusObject.connect()", status);
						if (status != Status.OK) {
							finish();
							return;
						}

						// We're only looking for one instance, so stop looking
						// for the name.//TODO
						mBus.cancelFindName(SERVICE_NAME);
						logStatus("BusAttachment.cancelFindName()", status);
						if (status != Status.OK) {
							finish();
							return;
						}
					}

					public void lostAdvertisedName(String name, String guid,
							String namePrefix, String busAddr) {
						// TODO
					}
				});
				logStatus("BusAttachment.findName()", status);
				if (status != Status.OK) {
					finish();
					return;
				}
				break;
			}
			case DISCONNECT: {
				mBus.deregisterBusObject(mService);
				mBus.disconnect();
				mBusHandler.getLooper().quit();
				break;
			}
			case HELLO: {
				try {
					mPongServiceInterface.Hello();
				} catch (BusException ex) {
					logException("AddressBookInterface.getContact()", ex);
				}
				break;
			}
			default:
				break;

			}
		}
	}

	private void logStatus(String msg, Status status) {
		String log = String.format("%s: %s", msg, status);
		if (status == Status.OK) {
			Log.i(TAG, log);
		} else {
			Message toastMsg = mHandler.obtainMessage(MESSAGE_POST_TOAST, log);
			mHandler.sendMessage(toastMsg);
			Log.e(TAG, log);
		}
	}

	private void logException(String msg, BusException ex) {
		String log = String.format("%s: %s", msg, ex);
		Message toastMsg = mHandler.obtainMessage(MESSAGE_POST_TOAST, log);
		mHandler.sendMessage(toastMsg);
		Log.e(TAG, log, ex);
	}
}