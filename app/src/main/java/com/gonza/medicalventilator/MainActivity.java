package com.gonza.medicalventilator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.gonza.medicalventilator.BreathService.ACTION_LOOP;
import static com.gonza.medicalventilator.BreathService.BREATH_ACTION;

public class MainActivity extends AppCompatActivity {
	private boolean mEndWait;
	private boolean mAbort;
	private View mExhalation;
	private View mInhalation;
	private View mPause;
	private View mFreqInh;
	private View mFreqExhal;

	public static int ExhalationVal = 3000;
	public static int InhalationVal = 3000;
	public static int PauseVal = 2000;

	public static int ExhalationFreqToneVal = 220;
	public static int InhalationFreqToneVal = 440;

	public static boolean IS_ON = false;


	// originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android
	// .html
	// and modified by Steve Pomeroy <steve@staticfree.info>

	final BroadcastReceiver breathReceiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getAction().equals(BREATH_ACTION)) {
				int status = intent.getIntExtra("status", 0);
				switch (status) {
					case 0:
						findViewById(R.id.viewLineIndicatorInit).setBackgroundColor(Color.GREEN);
						findViewById(R.id.viewLineIndicatorEnd).setBackgroundColor(Color.GREEN);
						break;
					case 1:
						findViewById(R.id.viewLineIndicatorInit).setBackgroundColor(Color.YELLOW);
						findViewById(R.id.viewLineIndicatorEnd).setBackgroundColor(Color.YELLOW);
						break;
					case 2:
						findViewById(R.id.viewLineIndicatorInit).setBackgroundColor(Color.RED);
						findViewById(R.id.viewLineIndicatorEnd).setBackgroundColor(Color.RED);
						break;
					default:
						break;
				}
			}
		}
	};

	@Override protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this)
							 .registerReceiver(breathReceiver, new IntentFilter(BREATH_ACTION));
	}

	@Override protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(breathReceiver);
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initControls();
		findViewById(R.id.ButtonApply).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				applyVal();
				hideSoftKeyboard(MainActivity.this);

			}
		});
		final ToggleButton toggle = findViewById(R.id.toggleOnOff);
		toggle.setChecked(IS_ON);
		toggle.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				IS_ON = toggle.isChecked();

				startService(
				  new Intent(MainActivity.this, BreathService.class).setAction(ACTION_LOOP));
				if (IS_ON) {

					new AlertDialog.Builder(MainActivity.this).setTitle("").setMessage(
					  "Please ensure to set the device on silent mode and disable alarms to " +
					  "avoid" +
					  " " + "other unexpected sounds.")
															  .setPositiveButton("Ok", null).show();

				}


			}
		});


	}

	private void initControls() {
		mInhalation = findViewById(R.id.inhalationControl);
		TextView tvIn = mInhalation.findViewById(R.id.textViewTitle);
		tvIn.setText("Inhalation Time (milliseconds)");
		mExhalation = findViewById(R.id.exhalationControl);
		TextView tvEx = mExhalation.findViewById(R.id.textViewTitle);
		tvEx.setText("Exhalation Time (milliseconds)");
		mPause = findViewById(R.id.pauseControl);
		TextView tvPause = mPause.findViewById(R.id.textViewTitle);
		tvPause.setText("Pause Time (milliseconds)");


		initControl(mInhalation);
		initControl(mExhalation);
		initControl(mPause);

	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);


		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {


			case R.id.item_menu_action_freq:
				View v = getLayoutInflater().inflate(R.layout.advanced, null);
				final View vFreqInh = v.findViewById(R.id.inhalationFreq);

				TextView tvInh = vFreqInh.findViewById(R.id.textViewTitle);
				tvInh.setText("Tone freq Inh (hz");

				final View vFreqExh = v.findViewById(R.id.exhalationFreq);
				TextView tvExh = vFreqExh.findViewById(R.id.textViewTitle);
				tvExh.setText("Tone freq Exh (hz)");
				initControl(vFreqExh);
				initControl(vFreqInh);
				new AlertDialog.Builder(MainActivity.this).setView(v)
														  .setPositiveButton("Apply",
															new DialogInterface.OnClickListener() {
																@Override public void onClick(
																  DialogInterface dialog,
																  int which) {
																	if (setVal(vFreqInh) && setVal(vFreqExh) ) {
																		Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
																	}
																}
															}).setNegativeButton("X",
				  new DialogInterface.OnClickListener() {
					  @Override public void onClick(DialogInterface dialog, int which) {
					  }
				  }).show();
				break;
		}
		return true;

	}


	@Override protected void onPostResume() {
		super.onPostResume();
		hideSoftKeyboard(this);
		findViewById(R.id.hiddeFocus).requestFocus();
	}

	private void initControl(View viewById) {
		final EditText edCt = viewById.findViewById(R.id.EdittextControl);
		switch (viewById.getId()) {
			case R.id.inhalationControl:
				edCt.setText("" + InhalationVal);
				break;
			case R.id.exhalationControl:
				edCt.setText("" + ExhalationVal);
				break;
			case R.id.pauseControl:
				edCt.setText("" + PauseVal);
				break;
			case R.id.exhalationFreq:
				edCt.setText("" + ExhalationFreqToneVal);
				break;
			case R.id.inhalationFreq:
				edCt.setText("" + InhalationFreqToneVal);
				break;


			default:
				break;
		}
		initInc(viewById, viewById.findViewById(R.id.TextViewInc));
		initDec(viewById, viewById.findViewById(R.id.TextViewDecr));

	}

	private void initInc(final View viewById, View viewInc) {
		final EditText edCt = viewById.findViewById(R.id.EdittextControl);

		viewInc.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				int val = Integer.parseInt(edCt.getText().toString());
				val += 100;
				if (val < 60000) {
					edCt.setText("" + val);


				}

			}
		});

	}

	private void applyVal() {
		if (setVal(mExhalation) && setVal(mInhalation) && setVal(mPause)) {
			Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
		}

	}

	private boolean setVal(View v) {

		int val =
		  Integer.parseInt(((EditText) v.findViewById(R.id.EdittextControl)).getText().toString());


		switch (v.getId()) {
			case R.id.inhalationControl:
				if (val < 1000 || val > 20000) {
					Toast.makeText(this, "Invalid inhalation value", Toast.LENGTH_SHORT).show();
					((EditText) v.findViewById(R.id.EdittextControl)).setText("" + InhalationVal);
				} else {
					InhalationVal = val;
					return true;
				}

				break;
			case R.id.exhalationControl:
				if (val < 1000 || val > 20000) {
					Toast.makeText(this, "Invalid Exhalation value", Toast.LENGTH_SHORT).show();
					((EditText) v.findViewById(R.id.EdittextControl)).setText("" + ExhalationVal);

				} else {
					ExhalationVal = val;
					return true;
				}
				break;
			case R.id.pauseControl:
				if (val < 10 || val > 10000) {
					Toast.makeText(this, "Invalid Pause value", Toast.LENGTH_SHORT).show();
					((EditText) v.findViewById(R.id.EdittextControl)).setText("" + PauseVal);

				} else {
					PauseVal = val;
					return true;
				}
				break;
			case R.id.exhalationFreq:
				if (val < 100 || val > 10000) {
					Toast.makeText(this, "Invalid Freq tone exhalation value", Toast.LENGTH_SHORT).show();
					((EditText) v.findViewById(R.id.EdittextControl)).setText("" + ExhalationFreqToneVal);

				} else {
					ExhalationFreqToneVal = val;
					return true;
				}
				break;
			case R.id.inhalationFreq:
				if (val < 10 || val > 10000) {
					Toast.makeText(this, "Invalid Freq tone Inhalation value", Toast.LENGTH_SHORT).show();
					((EditText) v.findViewById(R.id.EdittextControl)).setText("" + InhalationFreqToneVal);

				} else {
					InhalationFreqToneVal = val;
					return true;
				}
				break;

			default:
				break;
		}
		return false;
	}

	private void initDec(final View viewById, View viewDec) {
		final EditText edCt = viewById.findViewById(R.id.EdittextControl);
		viewDec.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				int val = Integer.parseInt(edCt.getText().toString());
				val -= 100;
				if (val > 50) {
					edCt.setText("" + val);


				}

			}
		});
	}

	public static void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager =
		  (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			View focus = activity.getCurrentFocus();
			if (focus != null) {
				inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
				focus.clearFocus();
				activity.findViewById(R.id.hiddeFocus).requestFocus();

			}

		}
	}

}
