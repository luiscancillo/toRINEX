package com.gnssapps.acq.torinex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.gnssapps.acq.torinex.AppUtils.showInfoHelp;
import static com.gnssapps.acq.torinex.AppUtils.fillFromDir;
import static com.gnssapps.acq.torinex.Constants.*;

/**
 * GenerateRinex activity allows user to select the survey files where GNSS data have been stored
 * to generate from them the related RINEX files.
 * The selection is performed through the GUI layout activity_generate_rinex where the user would
 * perform the following tasks:
 * 1 - To select the survey name of interest in the spinner(genRinex_surveyList). This spinner is
 *  initially filled with the existing directories in the application data directory. When a
 *  survey is selected the list of acquisition files (genRinex_fileList) is updated with the
 *  existing ones in its directory. Also, if already generated RINEX files exist in the RINEX
 *  subdirectory of the survey, they are shown in the "Existing RINEX file list"
 * 2 - To check one or several acquisition files containing acquired GNSS data in the spinner.
 *  It is assumed that each one corresponds to a given point of the survey.
 * 3 - To request generation of RINEX files for the selected point, according to the following:
 *  A - If Navigation Raw Data files are selected, it is generated a RINEX navigation data
 *  B - If Observation Raw data files are selected, they are generated RINEX observation files
 *  C - If "ONE FOR EACH SELECTED" one RINEX file is generated for each acquisition file in the
 *      version stated in the RINEX setup
 *  D - If "ONE FILE FOR ALL" one RINEX file is generated with data from all acquisition
 *      files selected
 */
public class GenerateRinex extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    //to manage views and their contents
    private ActionBar appBar;
    private SharedPreferences sharedPref;
    private Spinner genRinex_surveyList;
    private ListView genRinex_fileList;
    private List<String> existingSurveys = new ArrayList<String>();
    private String selectedSurvey;
    private String surveySubdir;
    ArrayAdapter<String> genRinex_surveyList_adapter;
    private List<String> existingOnrdFiles = new ArrayList<String>();
    private List<String> selectedOnrdFiles = new ArrayList<String>();
    private SparseBooleanArray existingOnrdFileFlags;
    private Button genRinex_multipleFile;
    private Button genRinex_singleFile;
    ArrayAdapter<String> gen_Rinex_fileList_adapter;
    private TextView genRinex_existingRinexFiles;
    private List<String> existingRinexFiles = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //to get values from options set in SettingsActivity
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //fill de list of existing survey directories
        fillFromDir(existingSurveys, APP_NAME, false);
        existingSurveys.add(0, getString(R.string.genRinex_empty));
        setContentView(R.layout.activity_generate_rinex);
        //modify action bar title
        appBar = getSupportActionBar();
        appBar.setTitle(R.string.genRinex_header);
        //get ids for elements in the view
        genRinex_surveyList = (Spinner) findViewById(R.id.perf_genRinex_surveyList);
        genRinex_fileList = (ListView)findViewById(R.id.perf_genRinex_fileList);
        genRinex_existingRinexFiles = (TextView)findViewById(R.id.perf_genRinex_rinexFiles);
        genRinex_multipleFile = (Button) findViewById((R.id.perf_genRinex_multipleFile));
        genRinex_singleFile = (Button) findViewById((R.id.perf_genRinex_singleFile));
        //perform initial settings of the spinner with the survey directories
        genRinex_surveyList_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, existingSurveys);
        genRinex_surveyList.setAdapter(genRinex_surveyList_adapter);
        genRinex_surveyList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                //whe a new survey is selected get the lists of files in its directory
                if (pos > 0) {
                    selectedSurvey = adapterView.getItemAtPosition(pos).toString();
                    surveySubdir = APP_NAME + "/" + selectedSurvey;
                    fillFromDir(existingOnrdFiles, surveySubdir, true);
                    fillFromDir(existingRinexFiles, surveySubdir + RINEX_FILES_DIRECTORY, true);
                } else {   //in pos 0 is allways "Select survey"
                    surveySubdir = Constants.EMPTY;
                    selectedOnrdFiles.clear();
                    existingRinexFiles.clear();
                }
                selectedOnrdFiles.clear();
                for (int i=0; i < gen_Rinex_fileList_adapter.getCount(); i++) genRinex_fileList.setItemChecked(i, false);
                //now display the contents of both lists
                gen_Rinex_fileList_adapter.notifyDataSetChanged();
                genRinex_existingRinexFiles.setText("");
                for (String rf : existingRinexFiles) genRinex_existingRinexFiles.append(rf + "\n");
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        //perform initial settings of the listview with checkboxes for point files
        gen_Rinex_fileList_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, android.R.id.text1, existingOnrdFiles);
        genRinex_fileList.setAdapter(gen_Rinex_fileList_adapter);
        genRinex_fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //each time a point file is click the complete list of selected files is updated
                existingOnrdFileFlags = genRinex_fileList.getCheckedItemPositions();
                selectedOnrdFiles.clear();
                for (int i = 0; i < existingOnrdFileFlags.size(); i++)
                    if (existingOnrdFileFlags.valueAt(i))
                        selectedOnrdFiles.add(existingOnrdFiles.get(existingOnrdFileFlags.keyAt(i)));
            }
        });
        //perform initial settings for buttons
        genRinex_multipleFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    callRINEXgen(Constants.RINEX_MULTIPLE);
            }
        });
        genRinex_singleFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callRINEXgen(Constants.RINEX_SINGLE);
            }
        });
        //perform initial setting in the list of existing RINEX files
        genRinex_existingRinexFiles.setMovementMethod(new ScrollingMovementMethod());
    }
    @Override
    protected void onStart() {
        super.onStart();
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
                        getString(R.string.generate_RINEX_info_title),
                        getString(R.string.generate_RINEX_info_body),
                        this);
                return true;
            case android.R.id.home:
                //return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Private methods
    /**
     * getSurveyPath gets the absolute path to the current survey (the one having name surveySubdir) directory
     * @return the absolute path to the survey directory if exists, or an empty string otherwise
     */
    private String getSurveyPath() {
        if (!surveySubdir.equals(Constants.EMPTY)) {
            File theSubdir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS),
                    surveySubdir);
            if (theSubdir.exists()) return theSubdir.getAbsolutePath();
        }
        return Constants.EMPTY;
    }
    /**
     * makeRinexDir creates a directory (if it does not exist) to store RINEX and log files.
     * @param intoPath the absolute path where directory will be created
     * @return the absolute path to the directory created if the directory exists or was created, or the intoPath value otherwise
     */
    private String makeRinexDir(String intoPath) {
        File theRinexDir = new File(intoPath, Constants.RINEX_FILES_DIRECTORY);
        if (theRinexDir.exists() || theRinexDir.mkdirs()) return theRinexDir.getAbsolutePath();
        return intoPath;
    }
    /**
     * setOutParams puts into an array of strings the values stored in shared preferences with
     * data set by user in the setup RINEX headers screen.
     * Each array element will contain a message consisting of: MESSAGE_TYPE_IDENTIFIER;MESSAGE_VALUE
     *
     * @return  the strings array with messages, one for each header data item in shared preferences
     */
    private String[] setOutParams() {
        List<String> params = new ArrayList<String>();
        params.add(Integer.toString(Constants.MT_LOGLEVEL) + SEMICOLON +
                sharedPref.getString("rinex_loglevel", getString(R.string.pref_default_rinex_loglevel)));
        params.add(Integer.toString(Constants.MT_RINEXVER) + SEMICOLON +
                sharedPref.getString("rinex_version", getString(R.string.pref_default_rinex_version)));
//        params.add(Integer.toString(Constants.MT_SITE) + SEMICOLON +
//                sharedPref.getString("rinex_site", getString(R.string.pref_default_rinex_unknown)));
        params.add(Integer.toString(Constants.MT_RUN_BY) + SEMICOLON +
                sharedPref.getString("rinex_runby", getString(R.string.pref_default_rinex_unknown)));
        String anString = sharedPref.getString("rinex_markername", getString(R.string.pref_default_rinex_unknown));
        if (anString.compareTo(getString(R.string.pref_default_rinex_unknown)) == 0) anString = "";
        params.add(Integer.toString(Constants.MT_MARKER_NAME) + SEMICOLON + anString);
        params.add(Integer.toString(Constants.MT_MARKER_TYPE) + SEMICOLON +
                sharedPref.getString("rinex_markertype", getString(R.string.pref_default_rinex_unknown)));
        params.add(Integer.toString(Constants.MT_OBSERVER) + SEMICOLON +
                sharedPref.getString("rinex_observer", getString(R.string.pref_default_rinex_unknown)));
        params.add(Integer.toString(Constants.MT_AGENCY) + SEMICOLON +
                sharedPref.getString("rinex_agency", getString(R.string.pref_default_rinex_unknown)));
        params.add(Integer.toString(Constants.MT_RECNUM) + SEMICOLON +
                sharedPref.getString("rinex_receivernumber", getString(R.string.pref_default_rinex_one)));
        params.add(Integer.toString(Constants.MT_COMMENT) + SEMICOLON +
                sharedPref.getString("rinex_comment", getString(R.string.pref_default_rinex_empty)));
        params.add(Integer.toString(Constants.MT_MARKER_NUM) + SEMICOLON +
                sharedPref.getString("rinex_markernumber", getString(R.string.pref_default_rinex_one)));
        params.add(Integer.toString(Constants.MT_CLKOFFS) + SEMICOLON +
                sharedPref.getString("rinex_clkoffs", getString(R.string.pref_default_rinex_clkoffs)));
        params.add(Integer.toString(Constants.MT_FIT) + SEMICOLON +
                (sharedPref.getBoolean("rinex_fitinterval", false)? "TRUE" : "FALSE"));
        params.add(Integer.toString(Constants.MT_CONSTELLATIONS) + SEMICOLON +
                Arrays.deepToString(sharedPref.getStringSet("rinex_constellations", new HashSet<String>()).toArray(new String[0])));
        params.add(Integer.toString(Constants.MT_SATELLITES) + SEMICOLON +
                sharedPref.getString("rinex_satellites", ""));
        params.add(Integer.toString(Constants.MT_OBSERVABLES) + SEMICOLON +
                Arrays.deepToString(sharedPref.getStringSet("rinex_observables", new HashSet<String>()).toArray(new String[0])));
        return params.toArray(new String[0]);
    }
    /**
     * callRINEXgen calls the RINEX generation JNI routine after verifying that survey has been selected and
     * it exists the path to the acquisition files selected. It gives results messages using Toast
      * @param mode the mode to use for generating RINEX files (RINEX_SINGLE or RINEX_MULTIPLE)
     */
    private void callRINEXgen (int mode) {
        String srvPath = getSurveyPath();
        if (srvPath.equals(Constants.EMPTY)) {
            Toast.makeText(getApplicationContext(), getString(R.string.genRinex_surveyNotSel), Toast.LENGTH_SHORT).show();
            return;
        }
        String response = generateRinexFilesJNI(mode,
                srvPath,
                selectedOnrdFiles.toArray(new String[0]),
                makeRinexDir(srvPath),
                setOutParams());
        int retCode = Integer.parseInt(response);
        if (retCode == 0) {
            response = getString(R.string.genRinex_retCodeOK);
        }
        else {
            response = getString(R.string.genRinex_retCodeERR);
            if ((retCode & RET_ERR_OPENRAW) != 0) response += getString(R.string.genRinex_retERR_OPENRAW);
            else if ((retCode & RET_ERR_READRAW) != 0) response += getString(R.string.genRinex_retERR_READRAW);
            else if ((retCode & RET_ERR_WRINAV) != 0) response += getString(R.string.genRinex_retERR_CREOBS);
            else if ((retCode & RET_ERR_WRIOBS) != 0) response += getString(R.string.genRinex_retERR_WRIOBS);
            else if ((retCode & RET_ERR_CRENAV) != 0) response += getString(R.string.genRinex_retERR_CRENAV);
            else response += getString(R.string.genRinex_retERR_UNK);
        }
        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
        //now update the list of RINEX files
        fillFromDir(existingRinexFiles, surveySubdir + RINEX_FILES_DIRECTORY, true);
        genRinex_existingRinexFiles.setText("");
        for (String rf : existingRinexFiles) genRinex_existingRinexFiles.append(rf + "\n");
    }
    //JNI methods
    public native String generateRinexFilesJNI(int toDo, String inFilesPath, String[] infilesName, String outfilesPath, String[] outParams);
}
