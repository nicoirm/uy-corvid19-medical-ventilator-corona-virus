package com.gonza.medicalventilator;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import utils.NotificationUtils;

import static com.gonza.medicalventilator.MainActivity.ExhalationFreqToneVal;
import static com.gonza.medicalventilator.MainActivity.ExhalationVal;
import static com.gonza.medicalventilator.MainActivity.IS_ON;
import static com.gonza.medicalventilator.MainActivity.InhalationFreqToneVal;
import static com.gonza.medicalventilator.MainActivity.InhalationVal;
import static com.gonza.medicalventilator.MainActivity.PauseVal;


public class BreathService extends Service {
	public static final String BREATH_ACTION = "breath_action";
	private final int duration = 10; // seconds
	private final int sampleRate = 8000;
	private final int numSamples = duration * sampleRate;


	private final double sampleInhale[] = new double[numSamples];
	private final byte generatedSndInhale[] = new byte[2 * numSamples];

	private final double sampleExhale[] = new double[numSamples];
	private final byte generatedSndExhale[] = new byte[2 * numSamples];

	AudioTrack audioTrackInale;

	public static final String ACTION_LOOP = "ACTION_LOOP";


	public static SharedPreferences prefs;
	public static SharedPreferences.Editor ed;
	public static boolean updatingPrices;


	public static SharedPreferences prefs_own;
	public static SharedPreferences.Editor edOwn;

	public BreathService() {
	}

	@Override public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override public void onCreate() {
		super.onCreate();


	}

	Thread threadOrders, threadPrices;

	@Override public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null) {


			if (ACTION_LOOP.equals(intent.getAction())) {
				Log.e("okAG", "okAG service started" + " " + new Exception().getStackTrace()[0]
				  .toString());//TODO okAG edit clear this logs


				if (threadOrders == null && IS_ON) {
					startForeground(NotificationUtils.NOTIF_ID_WORKING,
					  NotificationUtils.getForeground("Breathing..", "Service working..", this));
					threadOrders = new Thread() {
						@Override public void run() {
							super.run();

							while (IS_ON) {
								playSound();

							}
							//stopSound();


						}
					};
					threadOrders.start();
				} else {
					if (!IS_ON) {
						//stopSound();
						stopForeground(true);
						stopSelf();
					} else {
						threadOrders.start();
					}
				}


			}

		}
		return super.onStartCommand(intent, flags, startId);
	}


	void genTones() {
		// fill out the array
		for (int i = 0; i < numSamples; ++i) {
			sampleInhale[i] = Math.sin(2 * Math.PI * i / (sampleRate / InhalationFreqToneVal));
			sampleExhale[i] = Math.sin(2 * Math.PI * i / (sampleRate / ExhalationFreqToneVal));
		}

		// convert to 16 bit pcm sound array
		// assumes the sample buffer is normalised.
		int idx = 0;
		for (final double dVal : sampleInhale) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSndInhale[idx++] = (byte) (val & 0x00ff);
			generatedSndInhale[idx++] = (byte) ((val & 0xff00) >>> 8);

		}
		idx = 0;
		for (final double dVal : sampleExhale) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSndExhale[idx++] = (byte) (val & 0x00ff);
			generatedSndExhale[idx++] = (byte) ((val & 0xff00) >>> 8);

		}
	}

	private void waitFor(long timeToWait) {
		long timeFuture = System.currentTimeMillis() + timeToWait;

		while (timeFuture > System.currentTimeMillis()) {
			if (!IS_ON) {
				break;
			}
		}
	}

	LocalBroadcastManager mBroadcaster;

	void playSound() {
		if (mBroadcaster == null) {
			mBroadcaster = LocalBroadcastManager.getInstance(this);
		}
		genTones();
		audioTrackInale =
		  new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT, generatedSndInhale.length, AudioTrack.MODE_STATIC);
		audioTrackInale.write(generatedSndInhale, 0, generatedSndInhale.length);
		if (IS_ON) {
			audioTrackInale.play();
		}

		broadcast(0);
		Log.e("okAG", "okAG playing sound " + InhalationVal + " " +
					  new Exception().getStackTrace()[0]
						.toString());//TODO okAG edit clear this logs
		waitFor(InhalationVal);
		audioTrackInale.pause();
		audioTrackInale.release();
		//audioTrackInale.stop();
		broadcast(1);
		Log.e("okAG", "okAG pausing sound " + PauseVal + " " + new Exception().getStackTrace()[0]
		  .toString());//TODO okAG edit clear this logs
		waitFor(PauseVal);
		audioTrackInale =
		  new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT, generatedSndExhale.length, AudioTrack.MODE_STATIC);
		audioTrackInale.write(generatedSndExhale, 0, generatedSndExhale.length);
		if (IS_ON) {
			audioTrackInale.play();
		}
		broadcast(2);
		Log.e("okAG", "okAG playing sound " + ExhalationVal + " " +
					  new Exception().getStackTrace()[0]
						.toString());//TODO okAG edit clear this logs
		waitFor(ExhalationVal);
		audioTrackInale.pause();
		audioTrackInale.release();
		//audioTrackExhale.stop();
		broadcast(1);
		Log.e("okAG", "okAG pausing sound " + PauseVal + " " + new Exception().getStackTrace()[0]
		  .toString());//TODO okAG edit clear this logs
		waitFor(PauseVal);


	}

	void broadcast(int i) {
		mBroadcaster.sendBroadcast(new Intent(BREATH_ACTION).putExtra("status", i));
	}

	void stopSound() {

		if (audioTrackInale != null) {
			audioTrackInale.stop();
		}


	}


}
