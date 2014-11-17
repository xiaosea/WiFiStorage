package com.lica.wifistorage.gui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lica.wifistorage.FsService;
import com.lica.wifistorage.FsSettings;
import com.lica.wifistorage.Logger;
import com.lica.wifistorage.R;

import java.net.InetAddress;

public class MainActivity extends Activity {

    private static String TAG = MainActivity.class.getSimpleName();
    
	private TextView mServiceStateTextView;
	private Button mStartButton;
	private Button mStopButton;
	private Button mSettingsButton;
	private ScrollView mLogScrollView;
	private TextView mLogTextView;

	private OnClickListener mOnClickListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Logger.setLogHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String color = "";
                switch (msg.what) {
                    case Log.ERROR:
                        color = "0xFF0000";
                        break;
                    case Log.WARN:
                        color = "0xFF7F00";
                        break;
                    case Log.INFO:
                        color = "0x007F00";
                        break;
                    case Log.DEBUG:
                        color = "0x00007F";
                        break;
                    default:
                        color = "0x007F00";
                        break;
                }
                mLogTextView.append(Html.fromHtml("<font color='" + color + "'>"
                        + msg.getData().getString("text") + "</font><br>"));
                scrollToBottom(this, mLogScrollView, mLogTextView);
            }
        });

		mOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.startButton:
					startServer();
					break;
				case R.id.stopButton:
					stopServer();
					break;
				case R.id.settingButton:
					startActivity(new Intent(MainActivity.this,
							FsPreferenceActivity.class));
					break;
				default:
					break;
				}
			}
		};

		mServiceStateTextView = (TextView) findViewById(R.id.serviceStateTextView);
		mStartButton = (Button) findViewById(R.id.startButton);
		mStopButton = (Button) findViewById(R.id.stopButton);
		mSettingsButton = (Button) findViewById(R.id.settingButton);
		mLogScrollView = (ScrollView) findViewById(R.id.logScrollView);
		mLogTextView = (TextView) findViewById(R.id.logTextView);

		mStartButton.setOnClickListener(mOnClickListener);
		mStopButton.setOnClickListener(mOnClickListener);
		mSettingsButton.setOnClickListener(mOnClickListener);
	}

    @Override
	protected void onResume() {
		super.onResume();
		refreshViewsState();

		Logger.d(TAG, "Registering the FsService actions");
		IntentFilter filter = new IntentFilter();
		filter.addAction(FsService.ACTION_STARTED);
		filter.addAction(FsService.ACTION_STOPPED);
		filter.addAction(FsService.ACTION_FAILEDTOSTART);
		registerReceiver(mFtpServerReceiver, filter);

        tips();
	}

    @Override
    protected void onDestroy() {
        Logger.setLogHandler(null);
        super.onDestroy();
    }

    private void tips() {
		InetAddress address = FsService.getLocalInetAddress();
		String url = !FsService.isRunning() ? this
				.getString(R.string.unknown) : "ftp://"
				+ address.getHostAddress() + ":" + FsSettings.getPortNumber()
                + "/";
		Logger.e(TAG, getString(R.string.service_state)
				+ (FsService.isRunning() ? this
						.getString(R.string.service_state_running) : this
						.getString(R.string.service_state_stopped)));
		Logger.e(TAG, getString(R.string.server_url) + url);
		Logger.e(TAG, getString(R.string.username_label)
				+ "："
				+ FsSettings.getUserName());
		Logger.e(TAG, getString(R.string.password_label)
				+ "："
				+ FsPreferenceActivity.transformPassword(FsSettings.getPassWord()));
		Logger.e(TAG, getString(R.string.tips));
	}

	@Override
	protected void onPause() {
		super.onPause();
		Logger.d(TAG, "Unregistering the FsService actions");
		unregisterReceiver(mFtpServerReceiver);
	}

	/**
	 * This receiver will check FsService.ACTION* messages and will update the
	 * button, running_state, if the server is running and will also display at
	 * what url the server is running.
	 */
	BroadcastReceiver mFtpServerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.d(TAG, "FsService action received: "
					+ intent.getAction());
			if (intent.getAction().equals(FsService.ACTION_STARTED)) {
				mStartButton.setEnabled(false);
				mStopButton.setEnabled(true);
				mServiceStateTextView.setText(R.string.started);
				// Fill in the FTP server address
				InetAddress address = FsService.getLocalInetAddress();
				if (address == null) {
					Logger.e(TAG, "Unable to retreive wifi ip address");
					Logger.e(TAG, getString(R.string.cant_get_url));
					return;
				}
				String iptext = "ftp://" + address.getHostAddress() + ":"
						+ FsSettings.getPortNumber() + "/";
				Resources resources = getResources();
				String summary = resources.getString(
						R.string.running_summary_started, iptext);
				Logger.i(TAG, summary);

				tips();
			} else if (intent.getAction().equals(
                    FsService.ACTION_STOPPED)) {
				mStartButton.setEnabled(true);
				mStopButton.setEnabled(false);
				mServiceStateTextView.setText(R.string.stopped);
			} else if (intent.getAction().equals(
					FsService.ACTION_FAILEDTOSTART)) {
				mStartButton.setEnabled(true);
				mStopButton.setEnabled(false);
				mServiceStateTextView.setText(R.string.running_summary_failed);
				Logger.i(TAG, getString(R.string.running_summary_failed));
			}
		}
	};

	private void refreshViewsState() {
		boolean running = FsService.isRunning();
		mServiceStateTextView.setText(running ? R.string.started
                : R.string.stopped);
		mStartButton.setEnabled(!running);
		mStopButton.setEnabled(running);
	}

	private void startServer() {
        sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
	}

	private void stopServer() {
        sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
	}

	private void scrollToBottom(final Handler handler, final View scroll, final View inner) {
        handler.post(new Runnable() {
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }
                scroll.scrollTo(0, offset);
            }
        });
	}
}