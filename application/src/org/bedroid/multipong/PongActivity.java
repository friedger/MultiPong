package org.bedroid.multipong;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.FindNameListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;
import org.bedroid.multipong.service.PongService;
import org.bedroid.multipong.service.PongServiceInterface;
import org.bedroid.multipong.service.PongSignal;
import org.bedroid.multipong.service.PongSignalInterface;

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

	private static final int MESSAGE_DISPLAY_HELLO = 1;
	private static final int MESSAGE_POST_TOAST = 2;
	public static final int MESSAGE_MOVE_RECEIVED = 3;

	private ServiceBusHandler mServiceBusHandler;
	protected ClientBusHandler mClientBusHandler;
	private Menu menu;
	private Thread mThread;
	private volatile boolean mStopped;
	private boolean mFound;
	public PongService mService = new PongService();

	/* UI Handler */
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/*
			 * case MESSAGE_ACTION: { mListViewArrayAdapter.add((String)
			 * msg.obj); break; }
			 */
			case MESSAGE_DISPLAY_HELLO:
				Log.i(TAG, (String) msg.obj);
				Toast.makeText(getApplicationContext(), (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_POST_TOAST:
				Toast.makeText(getApplicationContext(), (String) msg.obj,
						Toast.LENGTH_LONG).show();
				break;
			case MESSAGE_MOVE_RECEIVED:
				mPongView.setIncomingMove((BallMove) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	public PongSignalInterface mPongSignalInterface;

	private PongView mPongView;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!mStopped) {
			try {
				// Send Hello
				Message msg = mClientBusHandler
						.obtainMessage(ServiceBusHandler.HELLO);
				mClientBusHandler.sendMessage(msg);
				// Wait a bit
				Thread.sleep(5000);
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mPongView = (PongView) findViewById(R.id.board);

		Log.v(TAG, Pong.class.getSimpleName());

		/*
		 * Make all AllJoyn calls through a separate handler thread to prevent
		 * blocking the UI.
		 */
		HandlerThread clientBusThread = new HandlerThread("ClientBusHandler");
		clientBusThread.start();
		mClientBusHandler = new ClientBusHandler(clientBusThread.getLooper());
		HandlerThread serviceBusThread = new HandlerThread("ServiceBusHandler");
		serviceBusThread.start();
		mServiceBusHandler = new ServiceBusHandler(serviceBusThread.getLooper());

		/* Start the client. */
		mClientBusHandler.sendEmptyMessage(ServiceBusHandler.CONNECT);

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
		case R.id.new_game:
			if (!mFound) {
				/* Start our service. */
				mServiceBusHandler.sendEmptyMessage(ServiceBusHandler.CONNECT);
			}
			return true;
		case R.id.start_move:
			// Send Start Move
			Message msg = mClientBusHandler
					.obtainMessage(ServiceBusHandler.START_MOVE);
			mClientBusHandler.sendMessage(msg);
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

		mClientBusHandler.sendEmptyMessage(ServiceBusHandler.DISCONNECT);
		mServiceBusHandler.sendEmptyMessage(ServiceBusHandler.DISCONNECT);
	}

	class ServiceBusHandler extends Handler {
		private static final String SERVICE_NAME = "org.bedroid.multipong.service";

		BusAttachment mBus;

		/* These are the messages sent to the BusHandler from the UI. */
		public static final int CONNECT = 1;
		public static final int DISCONNECT = 2;
		public static final int HELLO = 3;
		public static final int START_MOVE = 4;
		public static final int MOVE = 5;

		public ServiceBusHandler(Looper looper) {
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

				Status status = mBus.registerBusObject(mService, "/");
				logStatus("S BusAttachment.registerBusObject()", status);
				if (status != Status.OK) {
					finish();
					return;
				}

				status = mBus.connect(SERVICE_NAME);
				logStatus("S BusAttachment.connect()", status);
				if (status != Status.OK) {
					finish();
					return;
				}

				break;
			}
			case DISCONNECT: {
				mBus.deregisterBusObject(mService);
				mBus.disconnect();
				getLooper().quit();
				break;
			}
			default:
				break;

			}
		}
	}

	class ClientBusHandler extends Handler {
		BusAttachment mBus;
		private ProxyBusObject mProxyObj;
		private PongServiceInterface mPongServiceInterface;

		public ClientBusHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ServiceBusHandler.CONNECT: {
				mBus = new BusAttachment("Pong",
						BusAttachment.RemoteMessage.Receive);// Receives from
				// remote
				// devices.

				PongSignal mSignalInterface = new PongSignal(mHandler);
				mBus.registerBusObject(mSignalInterface, "/pongsignal");

				Status status = mBus.connect();
				logStatus("C BusAttachment.connect()", status);
				if (status != Status.OK) {
					finish();
					return;
				}

				// Get a remote object
				mProxyObj = mBus.getProxyBusObject(
						ServiceBusHandler.SERVICE_NAME, "/",
						new Class[] { PongServiceInterface.class });
				mPongServiceInterface = mProxyObj
						.getInterface(PongServiceInterface.class);

				status = mBus.findName(ServiceBusHandler.SERVICE_NAME,
						new FindNameListener() {

							public void foundName(String name, String guid,
									String namePrefix, String busAddress) {

								Status status = mProxyObj.connect(busAddress);
								logStatus("C ProxyBusObject.connect()", status);
								if (status != Status.OK) {
									finish();
									return;
								}

								mFound = true;

								// We're only looking for one instance, so stop
								// looking
								// for the name.//TODO
								/*
								 * mBus.cancelFindName(ServiceBusHandler.
								 * SERVICE_NAME);
								 * logStatus("BusAttachment.cancelFindName()",
								 * status); if (status != Status.OK) { finish();
								 * return; }
								 */
							}

							public void lostAdvertisedName(String name,
									String guid, String namePrefix,
									String busAddr) {
								// TODO
							}
						});
				logStatus("C BusAttachment.findName()", status);

				if (status != Status.OK) {
					finish();
					return;
				}

				// setup emitter
				SignalEmitter emitter = new SignalEmitter(mSignalInterface,
						SignalEmitter.GlobalBroadcast.On);
				mPongSignalInterface = emitter
						.getInterface(PongSignalInterface.class);

				// register signal handlers
				mBus.registerSignalHandlers(mSignalInterface);
				break;
			}
			case ServiceBusHandler.DISCONNECT: {
				mProxyObj.disconnect();
				mBus.disconnect();
				getLooper().quit();
				break;
			}

			case ServiceBusHandler.HELLO: {
				try {
					String reply = mPongServiceInterface.Hello();
					Message replyMsg = mHandler.obtainMessage(
							MESSAGE_DISPLAY_HELLO, reply);
					mHandler.sendMessage(replyMsg);
				} catch (BusException ex) {
					logException("C PongServiceInterface.Hello()", ex);
				}
				break;
			}

			case ServiceBusHandler.START_MOVE: {
				try {
					mPongView.ballX = 50;
					mPongView.ballY = -1;
					mPongSignalInterface.Move(PongView.INITIAL_BALL_MOVE);
				} catch (BusException ex) {
					logException("C PongSignalInterface.start_move()", ex);
				}
				break;
			}

			case ServiceBusHandler.MOVE: {
				try {
					mPongSignalInterface.Move((BallMove) msg.obj);
				} catch (BusException ex) {
					logException("C PongSignalInterface.move()", ex);
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
