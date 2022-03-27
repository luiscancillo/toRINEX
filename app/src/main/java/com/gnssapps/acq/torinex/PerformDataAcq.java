package com.gnssapps.acq.torinex;

import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Toast;
import android.content.SharedPreferences;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.location.GnssMeasurement.ADR_STATE_CYCLE_SLIP;
import static android.location.GnssMeasurement.ADR_STATE_RESET;
import static android.location.GnssMeasurement.ADR_STATE_UNKNOWN;
import static android.location.GnssMeasurement.ADR_STATE_VALID;
import static android.location.GnssMeasurement.STATE_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GAL_E1BC_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GAL_E1B_PAGE_SYNC;
import static android.location.GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GLO_STRING_SYNC;
import static android.location.GnssMeasurement.STATE_GLO_TOD_DECODED;
import static android.location.GnssMeasurement.STATE_GLO_TOD_KNOWN;
import static android.location.GnssMeasurement.STATE_MSEC_AMBIGUOUS;
import static android.location.GnssMeasurement.STATE_SBAS_SYNC;
import static android.location.GnssMeasurement.STATE_SUBFRAME_SYNC;
import static android.location.GnssMeasurement.STATE_TOW_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_KNOWN;
import static android.location.GnssStatus.CONSTELLATION_BEIDOU;
import static android.location.GnssStatus.CONSTELLATION_GALILEO;
import static android.location.GnssStatus.CONSTELLATION_GLONASS;
import static android.location.GnssStatus.CONSTELLATION_GPS;
import static android.location.GnssStatus.CONSTELLATION_QZSS;
import static android.location.GnssStatus.CONSTELLATION_SBAS;
import static com.gnssapps.acq.torinex.AppUtils.fillFromDir;
import static com.gnssapps.acq.torinex.AppUtils.showInfoHelp;
import static com.gnssapps.acq.torinex.AppUtils.tagTextColor;

import static java.lang.Integer.parseInt;
import static com.gnssapps.acq.torinex.Constants.*;

public class PerformDataAcq extends AppCompatActivity {
	//to manage views and their contents
	private TextView dataAcq_epoch;
	private TextView dataAcq_satState;
	private TextView dataAcq_navState;
	private TextView dataAcq_survey;
	private TextView dataAcq_point;
	private ToggleButton dataAcq_startStop;
	private ProgressBar dataAcq_progress;
	private TextView dataAcq_pointFiles;
	private String survey;
	//to manage acquisition of GNSS data
	private LocationManager mLocationManager;
	//to store acquired observation raw data
	private File gnssObsFile;
	private PrintStream gnssObsOut;
	//a place to save satellite raw data before store them in the file
	private class satRawData implements Comparable<satRawData> {
		long timeTag;
		int order;
		//satellite and signal identification
		char constellation; //constellation (as per RINEX: G, R, E, C, S, ...)
		int satellite;      //satellite number inside constellation
		char obsBand;       //RINEX observation descriptor band (1, 2, 5, 6, 7, 8)
		char obsAttribute;  //RINEX observation descriptor attribute (P, C, D, Y, ...)
		//GNSS clock parameters
		long epochTimeNanos;
		long epochFullBiasNanos;
		double epochBiasNanos;
		double epochDriftNanos;
		int epochClockDiscontinuityCount;
		int epochLeapSeconds;
		//GNSS measurement related
		int synchState;
		long receivedSatTimeNanos;
		double timeOffsetNanos;
		int carrierPhaseState;
		double carrierPhase;
		double cn0db;
		double carrierFrequencyMHz;
		double psRangeRate;
		double psRangeRateUncert;
		long receivedSvTimeUncert;
		//constructor for objects
		public satRawData(
				long tTag,
				int cns, int satId, char band, char attr,
				long clkTimeNanos, long clkFullBiasNanos, double clkBiasNanos, double clkDriftNanos, int clkDiscotyCount, int clkLeapSec,
				int syncSt, long rcvSatTn, double timeOffsetNanos,
				int adrSt, double adrMeters, double carrFq,
				double cn0db,
				double psRate, double psRateUnc,
				long rcvTimUnc
				) {
			this.timeTag = tTag;
			this.order = cns * 1000 + satId;
			switch (cns) {
				case CONSTELLATION_GPS: this.constellation = 'G'; break;
				case CONSTELLATION_GALILEO: this.constellation = 'E'; break;
				case CONSTELLATION_GLONASS: this.constellation = 'R'; break;
				case CONSTELLATION_BEIDOU: this.constellation = 'C'; break;
				case CONSTELLATION_SBAS: this.constellation = 'S'; break;
				case CONSTELLATION_QZSS: this.constellation = 'J'; break;
				default: this.constellation = '?';
			}
			this.satellite = satId;
			this.obsBand = band;
			this.obsAttribute = attr;
			this.epochTimeNanos = clkTimeNanos;
			this.epochFullBiasNanos = clkFullBiasNanos;
			this.epochBiasNanos = clkBiasNanos;
			this.epochDriftNanos = clkDriftNanos;
			this.epochClockDiscontinuityCount = clkDiscotyCount;
			this.epochLeapSeconds = clkLeapSec;
			this.synchState = syncSt;
			this.receivedSatTimeNanos = rcvSatTn;
            this.carrierPhaseState = adrSt;
			this.carrierPhase = adrMeters;
			this.carrierFrequencyMHz = carrFq;
			this.cn0db = cn0db;
			this.timeOffsetNanos = timeOffsetNanos;
			this.psRangeRate = psRate;
			this.psRangeRateUncert = psRateUnc;
			this.receivedSvTimeUncert = rcvTimUnc;
		}
		@Override
		public int compareTo (satRawData out) {
			if (this.timeTag < out.timeTag) return -1;
			if (this.timeTag > out.timeTag) return 1;
			if (this.order < out.order) return -1;
			if (this.order > out.order) return 1;
			return 0;
		}
	}
	//data to process epochs
	private int epochCounter;   //the order number of the last epoch acquired
	private int totalEpochs;    //the number of total epochs to acquire
	private boolean storeEpoch; //if epoch data will be stored in a file or not
	private ArrayList<satRawData> epochRawData = new ArrayList<satRawData>();
	private Spanned epochTimeTagged; //a place to format epoch time data to be displayed

	private Spanned satStateTagged; //a place to format satellite observation state (to be displayed) during a epoch
	private long acquisitionRateMs; //the rate to acquire epoch data
	//to save and store acquired navigation data
	private int navStatus;      //the navigation data acquisition status
	private String navMsgConstellations;	//the constellations from which receiver is providing navigation message
	private String navMsgFrom;
	private Spanned navStatusTagged; //a place to format the navigation data acquisition status
	private long navGloAlmanac;		// a bit pattern to flag the frame-strings of the GLONASS almanac received
	private boolean acquireNavData; //if true navigation data would be acquired (if available) and stored
	private boolean timeNavDataStored;	//if true the time of the first epoch acquired has been already stored in the nav file
	private File gnssNavFile;		//the file to print the navigation messages
	private PrintStream gnssNavOut;	//the related stream
	private int[][] satsWithNavMsg = new int[MAX_SATNAV_MSG][MAX_SATNAV]; //a row for each message type where it is
																		//placed the list of satellites who sent it
	private int[] totalSatsWithNavMsg = new int[MAX_SATNAV_MSG];	//counters for each message type with the total number
																	// of different satellites which have sent it
	private boolean limitNavMsgTo4;	//if true, it is limited the number of navigation messages to store

	private final LocationListener mLocationListener =
			new LocationListener() {
				@Override
				public void onProviderEnabled(String provider) {}
				@Override
				public void onProviderDisabled(String provider) {}
				@Override
				public void onLocationChanged(Location location) {
					//fix available
				}
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {}
			};

	private final GnssMeasurementsEvent.Callback gnssMeasurementsEventListener =
			new GnssMeasurementsEvent.Callback() {
				@Override
				public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
					GnssClock gnssClock = event.getClock();
					for (GnssMeasurement measurement : event.getMeasurements())
						saveGnssObservables(gnssClock, measurement);
				}
				@Override
				public void onStatusChanged(int status) {
				}
			};

	private final GnssNavigationMessage.Callback gnssNavigationMessageListener =
			new GnssNavigationMessage.Callback() {
				@Override
				public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
						saveGnssNavMessage(event);
				}
				@Override
				public void onStatusChanged(int status) {
					navStatus = status;
				}
			};
	/*TODO: to decide on the following code
	private final GnssStatus.Callback gnssStatusListener =
			new GnssStatus.Callback() {
				@Override
				public void onStarted() {}
				@Override
				public void onStopped() {}
				@Override
				public void onSatelliteStatusChanged(GnssStatus status) {}
			};
	*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar appBar;
		SharedPreferences sharedPref;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_perform_data_acq);
		//modify action bar title
		appBar = getSupportActionBar();
		appBar.setTitle(R.string.dataAcq_header);
		//to get values from options set in SettingsActivity
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		//get ids for elements in the view and perform their initial settings
		dataAcq_epoch = (TextView) findViewById(R.id.perf_dataAcq_epoch);
		dataAcq_satState = (TextView) findViewById(R.id.perf_dataAcq_satState);
		dataAcq_satState.setMovementMethod(new ScrollingMovementMethod());
		dataAcq_navState = (TextView) findViewById(R.id.perf_dataAcq_navState);
		dataAcq_survey = (TextView) findViewById(R.id.perf_dataAcq_surveyName);
		dataAcq_point = (TextView) findViewById(R.id.perf_dataAcq_pointName);
		dataAcq_progress = (ProgressBar) findViewById(R.id.perf_dataAcq_progress);
		dataAcq_startStop = (ToggleButton) findViewById(R.id.perf_dataAcq_startStop);
		dataAcq_startStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(dataAcq_startStop.isChecked()) {
					// The toggle is enabled. Start acquisition
					Calendar calendar = Calendar.getInstance();
					String fileName = String.format(Locale.US, "P%tH_%tM_%tS",calendar, calendar, calendar);
					if (createObsStore(survey, fileName)) {
						storeEpoch = true;
						epochCounter = 0;
						dataAcq_point.setText(fileName);
						dataAcq_progress.setMax(totalEpochs);
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_rawFileOpen) + getString(R.string.dataAcq_rawObs) , Toast.LENGTH_SHORT).show();
					}
				}
				else {
					// The toggle is disabled. Stop acquisition and delete acquired data
					Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_acqAborted), Toast.LENGTH_SHORT).show();
					gnssObsOut.close();
					gnssObsFile.delete();
					dataAcq_progress.setProgress(0);
					dataAcq_startStop.setChecked(false);
					storeEpoch = false;
				}
			}
		});
		dataAcq_pointFiles = (TextView) findViewById(R.id.perf_dataAcq_points);
		dataAcq_pointFiles.setMovementMethod(new ScrollingMovementMethod());
		navStatus = GnssNavigationMessage.Callback.STATUS_NOT_SUPPORTED;    //default value
		//Get a LocationManager
		mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		mLocationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventListener);
		mLocationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageListener);
		//Set initial values extracted from preferences in the view elements and perform related initializations
		survey = sharedPref.getString("survey_name", getString(R.string.pref_default_survey_name));
		epochCounter = 0;
		try {
			totalEpochs = parseInt(sharedPref.getString("total_epochs", getString(R.string.pref_default_total_epochs)));
		} catch (NumberFormatException e) {
			totalEpochs = 0;
		}
		storeEpoch = false;
		try {
			acquisitionRateMs = TimeUnit.SECONDS.toMillis(
					Long.parseLong(sharedPref.getString(
							"measurements_interval",
							getString(R.string.pref_default_interval_value))));
		} catch (NumberFormatException e) {
			acquisitionRateMs = TimeUnit.SECONDS.toMillis(1L);
		}
		acquireNavData = sharedPref.getBoolean("acquire_nav_message", false);
		if (acquireNavData) {
			Calendar calendar = Calendar.getInstance();
			String fileName = String.format(Locale.US, "N%tH_%tM_%tS",calendar, calendar, calendar);
			acquireNavData = createNavStore(survey, fileName);
		}
		limitNavMsgTo4 = getString(R.string.enable_allFuntions).equalsIgnoreCase("true");
		satsWithNavMsg = new int[MAX_SATNAV_MSG][MAX_SATNAV];
		navMsgConstellations = "";
		navMsgFrom = "";
		for (int i = 0; i < MAX_SATNAV_MSG; i++) totalSatsWithNavMsg[i] = 0;
		navGloAlmanac = 0x0003_FFFF_FFFF_FFFFL;	//50 bits set to 1
	}
	@Override
	public void onStart() {
		//This callback contains what amounts to the activity’s final preparations
		// for coming to the foreground and becoming interactive.
		super.onStart();
	}
	@Override
	public void onResume() {
		//The system invokes this callback just before the activity starts interacting
		// with the user. At this point, the activity is at the top of the activity stack,
		// and captures all user input. Most of an app’s core functionality is implemented
		// in the onResume() method. The onPause() callback always follows onResume().
		super.onResume();
		dataAcq_survey.setText(survey);
		showRawFiles();
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				acquisitionRateMs,0.0f, mLocationListener);
	}
	@Override
	public void onPause() {
		/*The system calls onPause() when the activity loses focus and enters a Paused state.
		 This state occurs when, for example, the user taps the Back or Recents button.
		 When the system calls onPause() for your activity, it technically means your activity
		 is still partially visible, but most often is an indication that the user is leaving
		 the activity, and the activity will soon enter the Stopped or Resumed state.
		 An activity in the Paused state may continue to update the UI if the user is expecting
		 the UI to update. Examples of such an activity include one showing a navigation map
		 screen or a media player playing. Even if such activities lose focus, the user expects
		 their UI to continue updating. You should not use onPause() to save application or user data,
		 make network calls, or execute database transactions. For information about saving data,
		 see Saving and restoring activity state.
		 Once onPause() finishes executing, the next callback is either onStop() or onResume(),
		 depending on what happens after the activity enters the Paused state.
		 */
		super.onPause();
		//mLocationManager.removeUpdates(mLocationListener);
	}
	@Override
	public void onStop() {
		/* The system calls onStop() when the activity is no longer visible to the user.
		This may happen because the activity is being destroyed, a new activity is starting,
		or an existing activity is entering a Resumed state and is covering the stopped activity.
		In all of these cases, the stopped activity is no longer visible at all.
		The next callback that the system calls is either onRestart(), if the activity is coming
		back to interact with the user, or by onDestroy() if this activity is completely terminating.
		*/
		super.onStop();
	}
	@Override
	public void onRestart() {
		/* The system invokes this callback when an activity in the Stopped state is about to restart.
		 onRestart() restores the state of the activity from the time that it was stopped.
		This callback is always followed by onStart().
		*/
		super.onRestart();
	}
	@Override
	public void onDestroy() {
	   /* The system invokes this callback before an activity is destroyed. This callback is the final one
		that the activity receives. onDestroy() is usually implemented to ensure that all of an activity’s
		resources are released when the activity, or the process containing it, is destroyed.
		*/
	   super.onDestroy();
	   mLocationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsEventListener);
	   mLocationManager.unregisterGnssNavigationMessageCallback(gnssNavigationMessageListener);
	   mLocationManager.removeUpdates(mLocationListener);
	   if (acquireNavData)gnssNavOut.close();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_activity_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_setting_info:
				showInfoHelp(
						getString(R.string.perform_acquisition_info_title),
						getString(R.string.perform_acquisition_info_body),
						this);
				return true;
			case android.R.id.home:
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	/**
	 * saveGnssObservables acquires satellite observables data from GNSS receiver measurements.
	 * Acquisition is performed grouping satellite observables by epochs: all observables belonging
	 * to the same epoch are saved in the global array variable epochRawData.
	 * When a measurement appears belonging to a new epoch, current contents of epochRawData
	 * would be stored (depending on value of storeEpoch) in a file, acquired observables displayed,
	 * and current epoch data cleared.
	 * The new raw data are added to epochRawData.
	 *
	 *
	 * @param clock the GnssClock data the measurement data
	 * @param measurement the GnssMeasurement data adquired from GNSS receiver
	 */
	private void saveGnssObservables(GnssClock clock, GnssMeasurement measurement) {
		//Get GNSS clock data. Note: this app version assumes receiver is using the GPS time reference
		double carrFqMHz;
		long timeNanos = clock.getTimeNanos();
		long fullBiasNanos = 0;
		if (clock.hasFullBiasNanos()) fullBiasNanos = clock.getFullBiasNanos();
		long tTag = timeNanos - fullBiasNanos;
		//save observables of the current epoch
		if (!epochRawData.isEmpty()) {
			if ((epochRawData.get(0)).timeTag != tTag) {
				//epoch has changed, sort and store and/or display saved values as requested
				Collections.sort(epochRawData);
				epochCounter++;
				if (storeEpoch && (epochCounter <= totalEpochs))  storeEpochData();
				//update data to be displayed
				epochTimeTagged = formatEpochTime(tTag);
				satStateTagged = formatSatStateData();
				navStatusTagged = formatNavStatus();
				//display data immediately
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dataAcq_epoch.setText(epochTimeTagged);
						dataAcq_satState.setText(satStateTagged);
						dataAcq_navState.setText(navStatusTagged);
						if(storeEpoch) {
							if (epochCounter > totalEpochs) {
								dataAcq_startStop.setChecked(false);
								dataAcq_progress.setProgress(0);
								storeEpoch = false;
								gnssObsOut.close();
								dataAcq_point.setText("");
								showRawFiles();
							} else {
								dataAcq_progress.setProgress(epochCounter);
							}
						}
					}
				} );
				epochRawData.clear();
			}
		}
		if (epochRawData.size() < MAX_MEASUREMENTS) {
			if (measurement.hasCarrierFrequencyHz()) carrFqMHz = measurement.getCarrierFrequencyHz() / 1.0E6;
			else carrFqMHz = CARRIER_FREQUENCY_DEFAULT;
			int syncState = measurement.getState();
			int cnsType = measurement.getConstellationType();
			char band = getBandId(cnsType, carrFqMHz);
			char attr = getAttributeId(cnsType, band, syncState);
			int phaseLevel = measurement.getAccumulatedDeltaRangeState();
			if ((phaseLevel & ADR_STATE_VALID) == 0) phaseLevel = 0;
			epochRawData.add( new satRawData(
					tTag,
					cnsType, measurement.getSvid(), band, attr,
					timeNanos, fullBiasNanos,
					clock.hasBiasNanos()? clock.getBiasNanos(): 0.0D,
					clock.hasDriftNanosPerSecond()? clock.getDriftNanosPerSecond():0.0D,
					clock.getHardwareClockDiscontinuityCount(),
					clock.hasLeapSecond()? clock.getLeapSecond():0,
					syncState, measurement.getReceivedSvTimeNanos(),
					measurement.getTimeOffsetNanos(),
                    phaseLevel, measurement.getAccumulatedDeltaRangeMeters(), carrFqMHz,
					measurement.getCn0DbHz(),
					measurement.getPseudorangeRateMetersPerSecond(),
					measurement.getPseudorangeRateUncertaintyMetersPerSecond(),
					measurement.getReceivedSvTimeUncertaintyNanos()));
		} else {
			//a lot of measurements have been acquired belonging to the same epoch.
			//may be the receiver is unable to compute the GPST time
			this.runOnUiThread(new Runnable() {     //display data inmediately
				@Override
				public void run() {
					dataAcq_epoch.setText(getString(R.string.dataAcq_badClock));
				}
			} );
		}
	}
	/**
	 * getAttributeId provides the signal attribute for the given constellation and band.
	 *
	 * Values provided are based on reasonable assumptions.
	 * Android version 10 provides this value
	 *
	 * @param  cnsType is the constellation identifier
	 * @param band is the band identifier
	 * @param state is the getReceivedSvTimeNanos state
	 * @return the RINEX band identifier
	 */
	private char getAttributeId (int cnsType, char band, int state) {
		switch (cnsType) {
			case CONSTELLATION_GPS:
				switch (band) {
					case '1': return 'C'; //it is assumed that L1 C/A is being tracked
					case '5': return 'X'; //it is assumed that I+Q signals are being tracked
					default: break;
				}
				break;
			case CONSTELLATION_GALILEO:
				switch (band) {
					case '1':
						if ((state & STATE_GAL_E1BC_CODE_LOCK) != 0) return 'X';
						else if ((state & STATE_GAL_E1C_2ND_CODE_LOCK) != 0) return 'C';
						else if ((state & STATE_GAL_E1B_PAGE_SYNC) == 0) return 'B';
						break;
					case '5': return 'X'; //it is assumed that I+Q signals are being tracked
					default:
						break;
				}
				break;
			case CONSTELLATION_GLONASS:
				switch (band) {
					case '1': return 'C';    //it is assumed that G1 C/A is being tracked
					default: break;
				}
				break;
			case CONSTELLATION_BEIDOU:
				switch (band) {
					case '1':
					case '2':return 'I';    //it is assumed that B1 & 2 I is being tracked
					default: break;
				}
				break;
			case CONSTELLATION_SBAS:
			case CONSTELLATION_QZSS:
				switch (band) {
					case '1': return 'C';    //it is assumed that B1 C/A is being tracked
					default: break;
				}
				break;
			default:
				break;
		}
		return '?';
	}
	/**
	 * getBandId determines the RINEX band identification for a given constellation and carrier frequency
	 *
	 * @param  cnsType the constellation type
	 * @param  carrFreq the measurement signal frequency in MHz
	 * @return the RINEX band identifier
	 */
	private char getBandId(int cnsType, double carrFreq) {
		int cf = (int) carrFreq;
		switch (cnsType) {
			case CONSTELLATION_GPS:
				switch (cf) {
					case 1575: return '1';
					case 1227: return '2';
					case 1176: return '5';
					default: return '?';
				}
			case CONSTELLATION_GLONASS:
				if ((cf >= 1598) && (cf <= 1610)) return '1';
				if ((cf >= 1242) && (cf <= 1252)) return '2';
				return '?';
			case CONSTELLATION_GALILEO:
				switch (cf) {
					case 1575: return '1';
					case 1176: return '5';
					case 1207: return '7';
					case 1191: return '8';
					default: return '?';
				}
			case CONSTELLATION_BEIDOU:
				switch (cf) {
					case 1575: return '1';
					case 1561: return '2';
					default: return '?';
				}
			case CONSTELLATION_SBAS:
			case CONSTELLATION_QZSS:
				switch (cf) {
					case 1575: return '1';
					case 1176: return '5';
					default: return '?';
				}
			default:
				return '?';
		}
	}
	/**
	 * saveGnssNavMessage gets from the navigation message in the event passed relevant data for further
	 * RINEX navigation files generation. Navigation message data will be stored in specific NRD files.
	 *
	 * @param event the GNSS navigation message event
	 */
	private void saveGnssNavMessage(GnssNavigationMessage event) {
		//get status, satellite and navigation message data
		long gloAlmanacMask;
		int status = event.getStatus();
		int navMsgType = event.getType();
		int satellite = event.getSvid();
		int navMsgId = event.getMessageId();
		int navMsgSubId = event.getSubmessageId();
		byte[] navMsg = event.getData();
		//store data according to the type of message
		switch (navMsgType) {
			case GnssNavigationMessage.TYPE_GPS_L1CA:
				//only subframes 1, 2 & 3, and page 18 in subframe 4 have RINEX nav data
				if (((navMsgSubId > 0) && (navMsgSubId < 4)) || ((navMsgSubId == 4) && (navMsgId == 18))) {
					storeNavMsg(Constants.MT_SATNAV_GPS_L1_CA, status, 'G', satellite, navMsgSubId, navMsgId, 40, navMsg);
				}
				break;
			case GnssNavigationMessage.TYPE_GPS_L5CNAV:
				//TODO add a feature to filter data not used to generate RINEX files
				storeNavMsg(Constants.MT_SATNAV_GPS_L5_C, status,'G', satellite, navMsgSubId, navMsgId, 0, navMsg);
				break;
			case GnssNavigationMessage.TYPE_GPS_CNAV2:
				//TODO add a feature to filter data not used to generate RINEX files
				storeNavMsg(Constants.MT_SATNAV_GPS_C2, status,'G', satellite, navMsgSubId, navMsgId, 0, navMsg);
				break;
			case GnssNavigationMessage.TYPE_GPS_L2CNAV:
				//TODO add a feature to filter data not used to generate RINEX files
				storeNavMsg(Constants.MT_SATNAV_GPS_L2_C, status,'G', satellite, navMsgSubId, navMsgId, 0, navMsg);
				break;
			case GnssNavigationMessage.TYPE_BDS_D1:
				//TODO add a feature to filter data not used to generate RINEX files
				storeNavMsg(Constants.MT_SATNAV_BEIDOU_D1, status, 'C', satellite, navMsgSubId, navMsgId, 40, navMsg);
				break;
			case GnssNavigationMessage.TYPE_BDS_D2:
				//TODO add a feature to filter data not used to generate RINEX files
				storeNavMsg(Constants.MT_SATNAV_BEIDOU_D2, status, 'C', satellite, navMsgSubId, navMsgId, 40, navMsg);
				break;
			case GnssNavigationMessage.TYPE_GAL_I:
				//only words 1 to 6 and 10 have RINEX nav data
				if (((navMsgSubId > 0) && (navMsgSubId < 7)) || (navMsgSubId == 10)) {
					storeNavMsg(Constants.MT_SATNAV_GALILEO_INAV, status, 'E', satellite, navMsgSubId, navMsgId, 29, navMsg);
				}
				break;
			case GnssNavigationMessage.TYPE_GAL_F:
				//TODO add a feature to filter data not used to generate RINEX files
				storeNavMsg(Constants.MT_SATNAV_GALILEO_FNAV, status, 'E', satellite, navMsgSubId, navMsgId, 30, navMsg);
				break;
			case GnssNavigationMessage.TYPE_GLO_L1CA:
				//only strings 1 to 5 (navMsgSubId)  in frames 1 to 5 (navMsgId) have aphemeris plus corrections,
				if ((navMsgId < 1) || (navMsgId > 5)) return;	//ignore unknown frames
				//but almanac strings 6 to 15 in frames 1 to 5 are needed to build the OSN (Orbital Slot Number) - FCN (Frequency Channel Number) table
				//note that the same almanac string are transmited across all satellites and it is needed only once
				//The logic is:
				// - the bit pattern gloAlmReceived has a bit for each frame-string (50 bits). They are initially set to 1
				// - when a given frame-string is stored, the correspondig bit is reset
				// - do not store any frame-string already stored
				// - when gloAlmReceived is 0 it means that all string in the almanac have been already stored
				if ((navMsgSubId >= 6) && (navMsgSubId <= 15)) {
					//It is an almanac string. If the full almanac has been alredy stored, do not continue
					if (navGloAlmanac == 0L) return;
					//check if this frame-string has been already stored. If true, do not continue
					gloAlmanacMask = 0x1L << (10 * (navMsgId - 1) + (navMsgSubId - 6));
					if ((navGloAlmanac & gloAlmanacMask) == 0) return;
					//Reset the bit corresponding to this almanac frame-string to be stored
					navGloAlmanac = navGloAlmanac & (~gloAlmanacMask);
				}
				storeNavMsg(Constants.MT_SATNAV_GLONASS_L1_CA, status, 'R', satellite, navMsgSubId, navMsgId, 11, navMsg);
				break;
			case GnssNavigationMessage.TYPE_UNKNOWN:
				storeNavMsg(Constants.MT_SATNAV_UNKNOWN, status, 'U', satellite, navMsgSubId, navMsgId, 0, navMsg);
				break;
			default:
				break;
		}
	}
	/**
	 * createAppDirStore creates a directory to store files containing raw data acquired and RINEX files generated (if
	 * it does not exists).
	 * The directory will be a subdirectory of the APP_NAME directory in the public "Documents" directory of the system.
	 * @param dirName the name of the application directory
	 * @return the abstract File associated to this directory (existing or created)
	 * @throws IOException
	 */
	private File createAppDirStore(String dirName) throws IOException {
		File appDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), APP_NAME + "/" + dirName);
		if (!appDir.exists()) appDir.mkdirs();
		return appDir;
	}
	/**
	 * createObsStore created a file to store GNSS observation raw data acquired from receiver.
	 * The file is created to store raw data for a given observation point.
	 * The file is created in a directory where all point-files for a given survey are stored.
	 * The related File is saved in the gnssObsFile, and the associated PrintStream in gnssObsOut
	 * for further use.
	 * @param intoDir the directory name where the file will be created. It is the survey name.
	 * @param forPoint the name of the file-point to be created (without extension)
	 * @return true is the file has been created, false otherwise
	 */
	private boolean createObsStore(String intoDir, String forPoint) {
		try {
			File gnssDir = createAppDirStore(intoDir);
			gnssObsFile = new File(gnssDir, forPoint + ORD_FILE_EXTENSION);
			gnssObsOut = new PrintStream(gnssObsFile);
			storeRxIdMsg(gnssObsOut, ORD_FILE_EXTENSION, ORD_FILE_VERSION, true);
			return true;
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_rawFileOpen) + getString(R.string.dataAcq_rawObs) + e, Toast.LENGTH_LONG).show();
			return false;
		}
	}
	/**
	 * createNavStore created a file to store GNSS navigation raw data acquired from receiver.
	 * The file is created to store raw data for a given acquisition period in a survey.
	 * The file is created in a directory where all survey data are stored.
	 * The related File is saved in the gnssNavFile, and the associated PrintStream in gnssNavOut
	 * for further use.
	 * @param intoDir the directory name where the file will be created. It is the survey name.
	 * @param forAcq the name of the file to be created which is related to an acquisition period
	 * @return true is the file has been created, false otherwise
	 */
	private boolean createNavStore(String intoDir, String forAcq) {
		try {
			File gnssDir = createAppDirStore(intoDir);
			gnssNavFile = new File(gnssDir, forAcq + NRD_FILE_EXTENSION);
			gnssNavOut = new PrintStream(gnssNavFile);
			storeRxIdMsg(gnssNavOut, NRD_FILE_EXTENSION, NRD_FILE_VERSION, false);
			timeNavDataStored = false;
			return true;
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_rawFileOpen) + getString(R.string.dataAcq_rawNav) + e, Toast.LENGTH_LONG).show();
			return false;
		}
	}
	/**
	 * formatEpochTime converts to tagged HTML text the existing relevant time data for the current epoch.
	 * This data will be used for further displaying in a text view.
	 * @param epochTime the epoch time in nanoseconds, assuming GPS time reference
	 * @return the tagged HTML text to be displayed
	 */
	private Spanned formatEpochTime(long epochTime) {
		String epochTimeTxt = "<b>EPOCH"
				+ String.format(Locale.US, " (%d of %d)", epochCounter, storeEpoch? totalEpochs:0)
				+ String.format(Locale.US, " week %5d", epochTime / NUMBER_NANOSECONDS_WEEK)
				+ String.format(Locale.US, " tow %ds.", (epochTime % NUMBER_NANOSECONDS_WEEK)/1000000000);
		return (Html.fromHtml(epochTimeTxt, Html.FROM_HTML_MODE_COMPACT));
	}
	/**
	 * formatSatStateData converts to tagged HTML text the existing relevant data and satellite status for the current epoch.
	 * This data will be used for further displaying in a text view.
	 * @return the tagged HTML text to be displayed
	 */
	private Spanned formatSatStateData() {
		String color;
		String code;
		String currentSat;
		String lineBrk = "";
		String printedSat = "";
		String epochObsTxt = "";
		for (satRawData svo : epochRawData) {
			currentSat = String.format(Locale.US, "%c%02d", svo.constellation, svo.satellite);
			if (currentSat.compareTo(printedSat) != 0) {
				epochObsTxt += lineBrk + currentSat;
				lineBrk = "<br>";
				printedSat = currentSat;
			}
			//put S/N value
			epochObsTxt += "&nbsp;" + String.format(Locale.US, "%4.1f", svo.cn0db);
			//set and tag pseudorange
			code = String.format(Locale.US, " C%c%c", svo.obsBand, svo.obsAttribute);
			color = "red";
			if ((((svo.synchState & STATE_TOW_DECODED) != 0) && ((svo.synchState & STATE_CODE_LOCK) != 0))
				|| (((svo.synchState & STATE_GLO_TOD_DECODED) != 0) && (svo.synchState & STATE_CODE_LOCK) != 0)) color = "black";
			epochObsTxt += tagTextColor(code, color);
			//set and tag obsBand phase
			code = String.format(Locale.US, " L%c%c", svo.obsBand, svo.obsAttribute);
			color = "red";
			if ((svo.carrierPhaseState & ADR_STATE_VALID) == 1) color = "black";
			else if ((svo.carrierPhaseState & ADR_STATE_CYCLE_SLIP) != 0) color = "orange";
			epochObsTxt += tagTextColor(code, color);
			//set and tag doppler
			code = String.format(Locale.US, " D%c%c", svo.obsBand, svo.obsAttribute);
			if (svo.psRangeRate == 0.0D) color = "red";
			else color = "black";
			epochObsTxt += tagTextColor(code, color);
			//set and tag S/N
			code = String.format(Locale.US, " S%c%c", svo.obsBand, svo.obsAttribute);
			if (svo.cn0db == 0.0D) color = "red";
			else color = "black";
			epochObsTxt += tagTextColor(code, color);
		}
		return (Html.fromHtml(epochObsTxt, Html.FROM_HTML_MODE_COMPACT));
	}
	/**
	 * storeEpochData stores in the observation raw data file the data acquired during the
	 * current epoch from all tracked satellites,
	 * To do that, a message type MT_EPOCH is generated containing all clock data for this epoch,
	 * and, for each satellite having measurements in this epoch, a message type MT_OBS is generated.
	 */
	private void storeEpochData() {
		try {
			storeTimeEpoch(gnssObsOut);
			for (satRawData svo : epochRawData) {
				gnssObsOut.printf(Locale.US, "%d;%c%02d;%c%c",
						Constants.MT_SATOBS,
						svo.constellation, svo.satellite, svo.obsBand, svo.obsAttribute);
				gnssObsOut.printf(Locale.US, ";%d;%d;%g;%d;%g;%g;%g;%g;%g;%d\n",
						svo.synchState, svo.receivedSatTimeNanos, svo.timeOffsetNanos,
						svo.carrierPhaseState, svo.carrierPhase,
						svo.cn0db, svo.carrierFrequencyMHz,
						svo.psRangeRate, svo.psRangeRateUncert,
						svo.receivedSvTimeUncert);
			}
		} catch (IllegalFormatException e) {
			Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_rawDataWrite) + getString(R.string.dataAcq_rawObs) + e, Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * storeRxIdMsg stores in the given raw data file a set of receiver identification messages and
	 * other data needed for the RINEX header.
	 * @param gnssDataOut the output file where raw data will stored
	 * @param fileType the output file type: .ORD (Obervation Raw Data) or .NRD (Navigation Raw Data
	 * @param fileVersion the current version of the output file
	 * @param storeLLA obtain and store current Latitude, Longitude, Altirude
	 */
	private void storeRxIdMsg(PrintStream gnssDataOut, String fileType, String fileVersion, boolean storeLLA) {
        String errorOn =  "(" + fileType + "):";
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		try {
			gnssDataOut.printf(Locale.US, "%d;%s;%s\n", Constants.MT_GRDVER, fileType.toUpperCase(), fileVersion);
			gnssDataOut.printf(Locale.US, "%d;%s %s\n", Constants.MT_PGM, APP_NAME, APP_VERSION);
			gnssDataOut.printf(Locale.US, "%d;%s %s %s\n", Constants.MT_DVTYPE, Build.MANUFACTURER, Build.BRAND, Build.MODEL);
			gnssDataOut.printf(Locale.US, "%d;Android %s SDK %s\n", Constants.MT_DVVER, Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
			gnssDataOut.printf(Locale.US, "%d;%tY%tm%td %tH%tM%tS UTC\n", Constants.MT_DATE, calendar,calendar,calendar,calendar,calendar,calendar);
			gnssDataOut.printf(Locale.US, "%d;%d\n", Constants.MT_INTERVALMS, acquisitionRateMs);
            gnssDataOut.printf(Locale.US, "%d;DBHZ\n", Constants.MT_SIGU);
            try {
	            if (storeLLA) {
					Location mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					gnssDataOut.printf(Locale.US, "%d;%g;%g;%g\n", Constants.MT_LLA, mLocation.getLatitude(), mLocation.getLongitude(), mLocation.getAltitude());
				}
			} catch (NullPointerException e) {
				Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_rawDataWrite) + errorOn + e, Toast.LENGTH_LONG).show();
			}
			if (gnssDataOut.checkError()) {
				Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_rawDataWrite) + errorOn, Toast.LENGTH_LONG).show();
			}
		} catch (IllegalFormatException e) {
			Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_rawDataWrite) + e, Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * storeNavMsg stores in the navigation raw data file a message with navigation data acquired from a satellite.
	 * At the begining, if GPST time data is available, a MT_EPOCH is output to allow further processing of navigation
	 * data: f.e. to know the continuous week number it is needed to know the number of roll over occurred.
	 * @param msgType the message type to be generated. It depends on the satellite / constellation
	 * @param cnsID the constellation identifier (G, R, E, etc.)
	 * @param sat the satellite number (prn) in the constellation
	 * @param navMsgSubId the  sub-message identifier, relevant to the navMsgType of the message (subframe, string, page, word, ...)
	 * @param navMsgId an index to help with complete Navigation Message assembly (frame id, -1, frame nuber, subframe number, ...)
	 * @param msgSize the message size (number of bytes in the message)
	 * @param msg the navigation message data
	 */
	private void storeNavMsg(int msgType, int stat, char cnsID, int sat, int navMsgSubId, int navMsgId, int msgSize, byte[] msg) {
		String cnsAndMsg;	//to identify constellation, message type and satellite number (C_MM@nn)
		int msgTypeIndex = msgType - MT_SATNAV_OFFSET;
		if ((msgTypeIndex < 0) || (msgTypeIndex > MAX_SATNAV_MSG)) return;
		int satPos, iPos;
		boolean addSat = true;
		try {
			if (!(epochRawData.isEmpty() || timeNavDataStored)) {
				//one MT_EPOCH record is printed at the beginning of the file to have a GPST time reference for the data in the file
				storeTimeEpoch(gnssNavOut);
				timeNavDataStored = true;
			}
			//before storing the message, setup data to be presented in the user interface in the form NavMsg@Sats . For example:
			//NavMsg@Sats: G_CA@11 R_CA@2 E_IN@3
			//It means that they have been received GPS C/A nav messages from 11 satellites, GLONASS C/A nav messages from 2 satellites, and
			//Galileo I/NAV messages from 3 satellites
			//update the list of satellites for this message type if necessary
			for (satPos = 0; addSat && satPos < totalSatsWithNavMsg[msgTypeIndex]; satPos++) {
				if (satsWithNavMsg[msgTypeIndex][satPos] == sat) addSat = false;
			}
			if (addSat) {
				//update the list adding this satellite, and also the counter (shall be
				satsWithNavMsg[msgTypeIndex][satPos] = sat;
				totalSatsWithNavMsg[msgTypeIndex]++;
				totalSatsWithNavMsg[msgTypeIndex] %= MAX_SATNAV;	//should not be necessary ...
				cnsAndMsg = navSrcId(msgType);
				iPos = navMsgConstellations.indexOf(cnsAndMsg);
				cnsAndMsg +=  String.format(Locale.US, "@%d ", totalSatsWithNavMsg[msgType - MT_SATNAV_OFFSET]);
				if (iPos == -1) {
					//this constallation-nav message type (C_MM) was not in the status message displayed
					navMsgConstellations += cnsAndMsg;
					navMsgFrom = getString(R.string.dataAcq_navStatus_READYfrom) + " ";
				} else {
					//replace the constallation-nav message type (C_MM@sats) by the updated one
					navMsgConstellations = navMsgConstellations.substring(0, iPos) + cnsAndMsg + navMsgConstellations.substring(navMsgConstellations.indexOf(' ', iPos) + 1);
				}
			}
			//some app versions, like Lite, could limit the number of satellites to process
			if (limitNavMsgTo4 && satPos > 4) return;
			//store the navigation message
			gnssNavOut.printf(Locale.US, "%d;%d;%c%02d;%d;%d;%d", msgType, stat, cnsID, sat, navMsgSubId, navMsgId, msgSize);
			for (int i = 0; i < msgSize; i++) gnssNavOut.printf(Locale.US, ";%02X", msg[i]);
			gnssNavOut.printf(Locale.US, "\n");
		} catch (IllegalFormatException e) {
			Toast.makeText(getApplicationContext(), getString(R.string.dataAcq_rawDataWrite) + getString(R.string.dataAcq_rawObs) + e, Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * formatNavStatus formats in HTML a readable message with the satus of the navigation data being (or not)
	 * acquired to be used to display it.
	 * @return the HTML text with the status
	 */
	private Spanned formatNavStatus() {
		String navStatusTxt;
		switch (navStatus) {
			case GnssNavigationMessage.Callback.STATUS_READY:
				navStatusTxt = tagTextColor(getString(R.string.dataAcq_navStatus_READY) + navMsgFrom + navMsgConstellations,"black");
				break;
			case GnssNavigationMessage.Callback.STATUS_LOCATION_DISABLED:
				navStatusTxt = tagTextColor(getString(R.string.dataAcq_navStatus_DISABLED),"orange");
				break;
			case GnssNavigationMessage.Callback.STATUS_NOT_SUPPORTED:
				navStatusTxt = tagTextColor(getString(R.string.dataAcq_navStatus_NOT_SUPPORTED),"red");
				break;
			default:
				navStatusTxt = tagTextColor(getString(R.string.dataAcq_navStatus_UNKNOWN),"red");
				break;
		}
		return (Html.fromHtml(navStatusTxt, Html.FROM_HTML_MODE_COMPACT));
	}
	/**
	 * showRawFiles place in dataAcq_pointFiles the file names of existing raw files in the current survey directory
	 *
	 */
	private void showRawFiles() {
		List<String> existingFiles = new ArrayList<String>();
		int count = 0;
		dataAcq_pointFiles.setText("");
		fillFromDir(existingFiles, APP_NAME + "/" + survey, true);
		for (String tmp : existingFiles) {
			dataAcq_pointFiles.append(tmp);
			if ((++count % 3) == 0)  dataAcq_pointFiles.append("\n");
			else dataAcq_pointFiles.append("  ");
		}
	}
	/**
	 * storeTimeEpoch print into the given raw data PrintStream the current epoch time data (MT_EPOCH message)
	 * @param gnssDataOut the print stream where data will be printed
	 */
	private void storeTimeEpoch(PrintStream gnssDataOut) {
		gnssDataOut.printf(Locale.US, "%d;%d;%d;%g;%g;%d;%d;%d\n",
				Constants.MT_EPOCH,
				epochRawData.get(0).epochTimeNanos,
				epochRawData.get(0).epochFullBiasNanos,
				epochRawData.get(0).epochBiasNanos,
				epochRawData.get(0).epochDriftNanos,
				epochRawData.get(0).epochClockDiscontinuityCount,
				epochRawData.get(0).epochLeapSeconds,
				epochRawData.size());
	}
	/**
	 * navSrcId gives a printable string to identify the source of navigation messages.
	 * It includes constellation identification and a short for the message type.
	 *
	 * @param msgType the type of navigatio message acquired from satellite (f.e. GPS L1 CA, GAL I/NAV, etc.)
	 * @return the constellation plus message type identification
	 */
	private String navSrcId(int  msgType) {
		switch(msgType) {
			case Constants.MT_SATNAV_GPS_L1_CA:		return "G_CA";
			case Constants.MT_SATNAV_GLONASS_L1_CA:	return "R_CA";
			case Constants.MT_SATNAV_GALILEO_INAV:	return "E_IN";
			case Constants.MT_SATNAV_GALILEO_FNAV:	return "E_FR";
			case Constants.MT_SATNAV_BEIDOU_D1:		return "C_D1";
			case Constants.MT_SATNAV_GPS_L5_C:		return "C_5C";
			case Constants.MT_SATNAV_GPS_C2:		return "G_C2";
			case Constants.MT_SATNAV_GPS_L2_C:		return "G_2C";
			case Constants.MT_SATNAV_BEIDOU_D2:		return "C_D2";
			default: 								return "UNKN";
		}
	}
}
