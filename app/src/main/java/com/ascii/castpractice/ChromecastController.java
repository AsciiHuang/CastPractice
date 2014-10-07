package com.ascii.castpractice;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;
import org.json.JSONObject;
import java.io.IOException;

public class ChromecastController {

	public static final String APPLICATION_DEFAULT_ID = "D4209BA4";
	public static final String APPLICATION_CUSTOM_ID = "37C19D63";
	private String appId = APPLICATION_DEFAULT_ID;
	private Context context;
	private MediaRouter Router;
	private GoogleApiClient googleApiClient;
	private RemoteMediaPlayer remoteMediaPlayer;
	public ChromeCastListener UIListener;
	private MessageChannel messageChannel;

	public MediaRouteSelector RouteSelector;

	public ChromecastController(Context context) {
		this.context = context;
		Router = MediaRouter.getInstance(context);
		this.RouteSelector = new MediaRouteSelector.Builder()
				.addControlCategory(CastMediaControlIntent.categoryForCast(appId))
				.build();
		Router.addCallback(RouteSelector, routerCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	}

	public void setAppId(String appId) {
		if (!this.appId.equals(appId)) {
			this.appId = appId;
			this.RouteSelector = new MediaRouteSelector.Builder()
					.addControlCategory(CastMediaControlIntent.categoryForCast(this.appId))
					.build();
			Router.updateSelectedRoute(RouteSelector);
		}
	}

	public void onStopChromeCast() {
		Router.removeCallback(routerCallback);
	}

	private void refreshSeekPosition() {
		if (isConnected()) {
			long position = remoteMediaPlayer.getApproximateStreamPosition();
			if (position > 0) {

			}
		}
	}

	public Integer getPosition() {
		Integer nRes = 0;
		try {
			nRes = (int)remoteMediaPlayer.getStreamDuration();
			nRes /=1000;
		} catch (Exception e) {
			nRes = 0;
		}
		return nRes;
	}

	public Integer getDuration() {
		Integer nRes = 0;
		try {
			nRes = (int)remoteMediaPlayer.getApproximateStreamPosition();
			nRes /=1000;
		} catch (Exception e) {
			nRes = 0;
		}
		return nRes;
	}

	public void seek(long pos) {
		remoteMediaPlayer.seek(googleApiClient, pos, RemoteMediaPlayer.RESUME_STATE_UNCHANGED);
	}

	public void pause() {
		remoteMediaPlayer.pause(googleApiClient);
	}

	public void play() {
		remoteMediaPlayer.play(googleApiClient);
	}

	public void stop() {
		remoteMediaPlayer.stop(googleApiClient);
	}

	public void playTrack(String title, String album, String artist, String albumURL, String trackURL, String albumIntroURL) {
		MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
		metadata.putString(MediaMetadata.KEY_TITLE, title);
		metadata.putString(MediaMetadata.KEY_ARTIST, artist);
		metadata.putString(MediaMetadata.KEY_ALBUM_TITLE, album);
		metadata.addImage(new WebImage(Uri.parse(albumURL)));

		String customData = String.format("{\"album_intro\": \"%s\"}", albumIntroURL);
		JSONObject custonObject;
		try {
			custonObject = new JSONObject(customData);
		} catch	(Exception e) {
			custonObject = null;
		}

		MediaInfo mediaInfo = new MediaInfo.Builder(trackURL).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
				.setContentType("audio/mp3").setMetadata(metadata).setCustomData(custonObject).build();
		remoteMediaPlayer.load(googleApiClient, mediaInfo, true).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
			@Override
			public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
				if (mediaChannelResult.getStatus().isSuccess()) {
				}
			}
		});
	}

	public void sendMessage() {
		String strNamespace = messageChannel.getNamespace();
		Cast.CastApi.sendMessage(googleApiClient, strNamespace, "Ascii Demo Message").setResultCallback(messageHandler);
	}

	private ResultCallback<Status> messageHandler =  new ResultCallback<Status>() {
		@Override
		public void onResult (Status status){
			if (!status.isSuccess()) {
				Log.e("CastPractice", "Send Message Fail!");
			}
		}
	};

	private MediaRouter.Callback routerCallback = new MediaRouter.Callback() {
		@Override
		public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
			if (Router.getRoutes().size() > 1) {
				if(UIListener != null) {
					UIListener.onRouteShow();
				}
			}
		}

		@Override
		public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
			if (Router.getRoutes().size() <= 1) {
				if(UIListener != null) {
					UIListener.onRouteHide();
				}
			}
		}

		@Override
		public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
			try {
				Cast.CastOptions.Builder apiOptionsBuilder =
						Cast.CastOptions.builder(CastDevice.getFromBundle(route.getExtras()), castListener);
				apiOptionsBuilder.setVerboseLoggingEnabled(true);
				googleApiClient = new GoogleApiClient.Builder(context)
						.addApi(Cast.API, apiOptionsBuilder.build())
						.addConnectionCallbacks(connectionCallbacks)
						.addOnConnectionFailedListener(connectionFailedListener)
						.build();
				googleApiClient.connect();
			} catch (IllegalStateException e) {
			}
		}

		@Override
		public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
			disconnect();
		}
	};

	private Cast.Listener castListener = new Cast.Listener() {
		@Override
		public void onApplicationDisconnected(int statusCode) {
			if(remoteMediaPlayer != null) {
				finish();
			} else {
				disconnect();
			}
		}
	};

	private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
		@Override
		public void onConnectionSuspended(int cause) {
			disconnect();
		}

		@Override
		public void onConnected(final Bundle connectionHint) {
			try {
				if (googleApiClient != null && googleApiClient.isConnected()) {
					if (connectionHint != null && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
						disconnect();
					} else {
						Cast.CastApi.requestStatus(googleApiClient);
						Cast.CastApi.launchApplication(googleApiClient, appId, true).setResultCallback(resultCallback);
					}
				}
			} catch (IOException e) {
			}
		}
	};

	private ResultCallback<Cast.ApplicationConnectionResult> resultCallback = new ResultCallback<Cast.ApplicationConnectionResult>() {
		@Override
		public void onResult(Cast.ApplicationConnectionResult result) {
			try {
				if (result.getStatus().isSuccess() && googleApiClient != null && googleApiClient.isConnected()) {
					remoteMediaPlayer = new RemoteMediaPlayer();
					remoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
						@Override
						public void onStatusUpdated() {
							refreshSeekPosition();
						}
					});
					Cast.CastApi.setMessageReceivedCallbacks(googleApiClient, remoteMediaPlayer.getNamespace(), remoteMediaPlayer);
					messageChannel = new MessageChannel();
					Cast.CastApi.setMessageReceivedCallbacks(googleApiClient, messageChannel.getNamespace(), messageChannel);
					if (UIListener != null) {
						UIListener.onCastConnect();
					}
				}
			} catch (IOException e) {
			}
		}
	};

	private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
		@Override
		public void onConnectionFailed(ConnectionResult result) {

		}
	};

	public boolean isConnected() {
		return googleApiClient != null && remoteMediaPlayer != null ? googleApiClient.isConnected() : false;
	}

	public void disconnect() {
		if(isConnected()) {
			Cast.CastApi.stopApplication(googleApiClient).setResultCallback(new ResultCallback<Status>() {
				@Override
				public void onResult(Status result) {
					if (result.isSuccess() && isConnected()) {
						finish();
					}
				}
			});
		}
	}

	private void finish() {
		try {
			Router.selectRoute(Router.getDefaultRoute());
			Cast.CastApi.removeMessageReceivedCallbacks(googleApiClient, remoteMediaPlayer.getNamespace());
			Cast.CastApi.removeMessageReceivedCallbacks(googleApiClient, messageChannel.getNamespace());
			googleApiClient.disconnect();
			remoteMediaPlayer = null;
		} catch (IOException e) {
		}
	}

	class MessageChannel implements Cast.MessageReceivedCallback {

		public String getNamespace() {
			return context.getString(R.string.namespace);
		}

		/*
		 * Receive message from the receiver app
		 */
		@Override
		public void onMessageReceived(CastDevice castDevice, String namespace, String message) {

		}

	}
}
