package com.ascii.castpractice;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.MediaRouteButton;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	private RadioGroup radioReceiverApp;
	private Button buttonStartTrack1;
	private Button buttonStartTrack2;
	private Button buttonStartTrack3;
	private Button buttonSendMessage;
	private Button buttonSeek1;
	private Button buttonSeek2;
	private Button buttonPause;
	private Button buttonPlay;
	private Button buttonStop;
	private TextView lblPos;
	private TextView lblDur;

	private Handler mHandlerTime = new Handler();

	private MediaRouteButton mediaRouteButton;
	private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		app = (App)getApplication();

        setContentView(R.layout.activity_main);

		radioReceiverApp = (RadioGroup) findViewById(R.id.radio_receiver_app);
		radioReceiverApp.setOnCheckedChangeListener(appChangeHandler);

		buttonStartTrack1 = (Button) findViewById(R.id.button_start_track1);
		buttonStartTrack1.setOnClickListener(startButton1Handler);

		buttonStartTrack2 = (Button) findViewById(R.id.button_start_track2);
		buttonStartTrack2.setOnClickListener(startButton2Handler);

		buttonStartTrack3 = (Button) findViewById(R.id.button_start_track3);
		buttonStartTrack3.setOnClickListener(startButton3Handler);

		buttonSendMessage = (Button) findViewById(R.id.button_send_message);
		buttonSendMessage.setOnClickListener(sentMessageButtonHandler);

		buttonSeek1 = (Button) findViewById(R.id.button_seek_to_start);
		buttonSeek1.setOnClickListener(seek1ButtonHandler);
		buttonSeek2 = (Button) findViewById(R.id.button_seek_to_mid);
		buttonSeek2.setOnClickListener(seek2ButtonHandler);

		buttonPause = (Button) findViewById(R.id.button_pause);
		buttonPause.setOnClickListener(pauseButtonHandler);
		buttonStop = (Button) findViewById(R.id.button_stop);
		buttonStop.setOnClickListener(stopButtonHandler);
		buttonPlay = (Button) findViewById(R.id.button_play);
		buttonPlay.setOnClickListener(playButtonHandler);

		lblPos = (TextView) findViewById(R.id.lbl_pos);
		lblDur = (TextView) findViewById(R.id.lbl_dur);

		mediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);

		radioReceiverApp.check(R.id.radio_default);

		mHandlerTime.postDelayed(timerRun, 500);
    }

	@Override
	protected void onResume() {
		super.onResume();
		app = (App)getApplication();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.Cast.onStopChromeCast();
	}

	private final Runnable timerRun = new Runnable()
	{
		public void run()
		{
			Integer nPos = app.Cast.getPosition();
			Integer nDur = app.Cast.getDuration();
			lblPos.setText(nPos.toString());
			lblDur.setText(nDur.toString());
			mHandlerTime.postDelayed(this, 500);
		}
	};

	private RadioGroup.OnCheckedChangeListener appChangeHandler = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (checkedId == R.id.radio_custom) {
				app.Cast.setAppId(ChromecastController.APPLICATION_CUSTOM_ID);
			} else {
				app.Cast.setAppId(ChromecastController.APPLICATION_DEFAULT_ID);
			}
			
			mediaRouteButton.setRouteSelector(app.Cast.RouteSelector);
		}
	};

	private View.OnClickListener startButton1Handler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.playTrack("Not Your Kind Of People",
					"Not Your Kind Of People",
					"Garbage(垃圾合唱團)",
					"http://i.kfs.io/album/tw/331979,0v1/cropresize/600x600.jpg",
					"http://fs.kkbox.com.tw/sps.php?content_id=S9rO.DL03A3I3DmfaCXcW0P4&mtime=3&mode=mp3&ver=1",
					"http://www.kkbox.com/tw/tc/album/ZNhR0Ak7.ktJbR0F1H3B0091-index.html");
		}
	};

	private View.OnClickListener startButton2Handler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.playTrack("moonlight",
					"moonlight",
					"moumoon (沐月)",
					"http://i.kfs.io/album/tw/159727,0v1/cropresize/600x600.jpg",
					"http://fs.kkbox.com.tw/sps.php?content_id=jzDZBRE06.cGYRCHLQ.Hb0P4&mtime=1&mode=mp3&ver=1",
					"http://www.kkbox.com/tw/tc/album/8.yyS4eAm0QexO20Fazj008l-index.html");
		}
	};

	private View.OnClickListener startButton3Handler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.playTrack("Only One",
					"Only One",
					"寶兒 (BoA)",
					"http://i.kfs.io/album/tw/375120,0v3/cropresize/600x600.jpg",
					"http://fs.kkbox.com.tw/sps.php?content_id=HLNr8Yx01YNW-8euk7FLX0P4&mtime=1&mode=mp3&ver=1",
					"http://www.kkbox.com/tw/tc/album/bB5ZGKeArtJN-Q0F1R.G0091-index.html");
		}
	};

	private View.OnClickListener sentMessageButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.sendMessage();
		}
	};

	private View.OnClickListener seek1ButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.seek(0);
		}
	};

	private View.OnClickListener seek2ButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.seek(15000);
		}
	};

	private View.OnClickListener pauseButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.pause();
		}
	};

	private View.OnClickListener stopButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.stop();
		}
	};

	private View.OnClickListener playButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			app.Cast.play();
		}
	};
}
