package com.gnssapps.acq.torinex;

import android.Manifest;

/**
 * Constants class includes all global constants used in other Java classes of the toRINEX app.
 */
public class Constants {
    //to identify this application and its products
    public static final String APP_NAME = "toRINEX"; //application name for APP internal use (naming directories)
    public static final String APP_VERSION = "1"; //current application version code
    public static final String ORD_FILE_VERSION = "1";   //current GNSS Observation Raw Data file version code
    public static final String ORD_FILE_EXTENSION = ".ORD"; //GNSS Observation Raw Data file extension code
    public static final String NRD_FILE_VERSION = "1";   //current GNSS Navigation Raw Data file version
    public static final String NRD_FILE_EXTENSION = ".NRD"; //GNSS Navigation Raw Data file extension
    public static final String NRD_FILE_NAME = "NAVRAWDATA.NRD";
    //to manage the permissions needed in the app
    public static final int ACQ_REQUEST_ID = 1;
    public static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //to manage acquisition of GNSS data
    public static final long NUMBER_NANOSECONDS_DAY = 24L * 60L * 60L * 1000000000L;
    public static final long NUMBER_NANOSECONDS_WEEK = 7L * NUMBER_NANOSECONDS_DAY;
    public static final double CARRIER_FREQUENCY_DEFAULT = 1575.45;   //L1 frequency in MHz
    public static final double BAND1_LOWER_FREQ = 1559.0;   //RINEX band 1 lower frequency in MHz
    public static final double BAND1_UPPER_FREQ = 1610.0;   //RINEX band 1 upper frequency in MHz
    public static final double BAND2_LOWER_FREQ = 1215.0;   //RINEX band 2 lower frequency in MHz
    public static final double BAND2_UPPER_FREQ = 1254.0;   //RINEX band 2 upper frequency in MHz
    public static final double BAND5_LOWER_FREQ = 1164.0;   //RINEX band 5 lower frequency in MHz
    public static final double BAND5_UPPER_FREQ = 1189.0;   //RINEX band 5 upper frequency in MHz
    public static final double BAND6_LOWER_FREQ = 1260.0;   //RINEX band 6 lower frequency in MHz
    public static final double BAND6_UPPER_FREQ = 1300.0;   //RINEX band 6 upper frequency in MHz
    public static final double BAND7_LOWER_FREQ = 1189.0;   //RINEX band 7 lower frequency in MHz
    public static final double BAND7_UPPER_FREQ = 1214.0;   //RINEX band 7 upper frequency in MHz
    public static final double BAND8_LOWER_FREQ = 1188.0;   //RINEX band 8 is GAL E5a+b (band 5 + band 7)
    public static final double BAND8_UPPER_FREQ = 1190.0;   //with central frequency at 1189
    //tracking states allowing unambiguous pseudorange computation when ReceivedSvTimeNanos was taken
    public static final int TRACKING_ST_AMBIGUOUS = 0;   //When: STATE_TOW_DECODED or _KNOWN or STATE_GLO_TOD_DECODED
    public static final int TRACKING_ST_TOW_TOD = 1;   //When: STATE_TOW_DECODED or _KNOWN or STATE_GLO_TOD_DECODED
    public static final int TRACKING_ST_SUBF_PAGE = 2;  //When: STATE_SUBFRAME_SYNC or STATE_GLO_STRING_SYNC or STATE_GAL_E1B_PAGE_SYNC
    public static final int TRACKING_ST_2NDCODE = 4;   //When: STATE_E1C_2ND_CODE_LOCK
    public static final int TRACKING_ST_WHOLE = 8;   //When: STATE_E1C_2ND_CODE_LOCK
    //an estimate of the maximum visible sats and measurements
    public static final int MAX_SATELLITES = 60;
    public static final int MAX_MEASUREMENTS = MAX_SATELLITES * 4;
    //to manage file storage
    public static final String RINEX_FILES_DIRECTORY = "/RINEX";
    //The type of messages that GNSS Raw Data files or setup arguments can contain
    public static final int MT_EPOCH = 1;    //Epoch data
    public static final int MT_SATOBS = 2;   //Satellite observations data
    public static final int MT_SATNAV_GPS_l1_CA = 10;   //Satellite navigation data from GPS L1 C/A
    public static final int MT_SATNAV_GLONASS_L1_CA = 11;   //Satellite navigation data from GLONASS L1 C/A
    public static final int MT_SATNAV_GALILEO_FNAV = 12;   //Satellite navigation data from Galileo F/NAV
    public static final int MT_SATNAV_BEIDOU_D1 = 13;   //Satellite navigation data from Beidou D1 & D2
    public static final int MT_GRDVER = 50;    //Observation or navigation raw data files version
    public static final int MT_PGM = 51;       //Program used to generate data (toRINEX Vx.x)
    public static final int MT_DVTYPE = 52;   //Device type
    public static final int MT_DVVER = 53;    //Device version
    public static final int MT_LLA = 54;      //Latitude, Longitude, Altitude given by receiver
    public static final int MT_DATE = 55;      //Date of the file
    public static final int MT_INTERVALMS = 56;   //Acquisition interval, in milliseconds
    public static final int MT_SIGU = 57;       //signal strength units
    public static final int MT_RINEXVER = 70;  //RINEX file version to be generated
    public static final int MT_SITE = 71;
    public static final int MT_RUN_BY = 72;
    public static final int MT_MARKER_NAME = 73;
    public static final int MT_MARKER_TYPE = 74;
    public static final int MT_OBSERVER = 75;
    public static final int MT_AGENCY = 76;
    public static final int MT_RECNUM = 77;
    public static final int MT_COMMENT = 80;
    public static final int MT_MARKER_NUM = 81;
    public static final int MT_CLKOFFS= 82;
    public static final int MT_FIT = 95;      //To pass data on measurements per duty cycle w.r.t. interval stated
    public static final int MT_LOGLEVEL = 96;
    public static final int MT_CONSTELLATIONS = 97;
    public static final int MT_SATELLITES = 98;
    public static final int MT_OBSERVABLES = 99;
    //other useful constants
    public static final String EMPTY = "";
    public static final String SEMICOLON = ";";
    public static final int RINEX_SINGLE = 0;
    public static final int RINEX_MULTIPLE = 1;
    //return codes
    public static final int RET_ERR_OPENRAW = 1;
    public static final int RET_ERR_READRAW = 2;
    public static final int RET_ERR_CREOBS = 4;
    public static final int RET_ERR_WRIOBS = 8;
    public static final int RET_ERR_CRENAV = 16;
    public static final int RET_ERR_WRINAV = 32;
}
