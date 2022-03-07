package com.gnssapps.acq.torinex;

import android.Manifest;

/**
 * Constants class includes all global constants used in other Java classes of the toRINEX app.
 */
public class Constants {
    //to identify this application and its products
    public static final String APP_NAME = "toRINEX"; //application name for APP internal use (naming directories)
    public static final String APP_VERSION = "1"; //current application version code
    public static final String ORD_FILE_VERSION = "2";   //current GNSS Observation Raw Data file version code
    public static final String ORD_FILE_EXTENSION = ".ORD"; //GNSS Observation Raw Data file extension code
    public static final String NRD_FILE_VERSION = "2";   //current GNSS Navigation Raw Data file version
    public static final String NRD_FILE_EXTENSION = ".NRD"; //GNSS Navigation Raw Data file extension
    //to manage the permissions needed in the app
    public static final int ACQ_REQUEST_ID = 1;
    public static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //to manage acquisition of GNSS data
    public static final long NUMBER_NANOSECONDS_DAY = 24L * 60L * 60L * 1000000000L;
    public static final long NUMBER_NANOSECONDS_WEEK = 7L * NUMBER_NANOSECONDS_DAY;
    public static final double CARRIER_FREQUENCY_DEFAULT = 1575.42;   //L1 frequency in MHz
    //an estimate of the maximum visible sats and measurements
    public static final int MAX_SATELLITES = 60;
    public static final int MAX_MEASUREMENTS = MAX_SATELLITES * 4;
    //to manage file storage
    public static final String RINEX_FILES_DIRECTORY = "/RINEX";
    //The type of messages that GNSS Raw Data files or setup arguments can contain
    public static final int MT_EPOCH = 1;    //Epoch data
    public static final int MT_SATOBS = 2;   //Satellite observations data
    public static final int MT_SATNAV_OFFSET = 2;   //an offset to convert MT_SATNAVs into indexes
            //all MT_SATNAV values shall be between MT_SATNAV_OFFSET and MT_SATNAV_UNKNOWN
                                         //number 2 is empty. For future assignment
    public static final int MT_SATNAV_GPS_L1_CA = 3;   //Satellite navigation data from GPS L1 C/A
    public static final int MT_SATNAV_GLONASS_L1_CA = 4;   //Satellite navigation data from GLONASS L1 C/A
    public static final int MT_SATNAV_GALILEO_INAV = 5;   //Satellite navigation data from Galileo I/NAV
    public static final int MT_SATNAV_GALILEO_FNAV = 6;   //Satellite navigation data from Galileo F/NAV
    public static final int MT_SATNAV_BEIDOU_D1 = 7;   //Satellite navigation data from Beidou D1
    public static final int MT_SATNAV_GPS_L5_C = 8;   //Satellite navigation data from GPS L5 C
    public static final int MT_SATNAV_GPS_C2 = 9;   //Satellite navigation data from GPS C2
    public static final int MT_SATNAV_GPS_L2_C = 10;   //Satellite navigation data from GPS L2 C
    public static final int MT_SATNAV_BEIDOU_D2 = 11;   //Satellite navigation data from Beidou D2
                                        //numbers 12 to 14 are empty
    public static final int MT_SATNAV_UNKNOWN = 15;   //Satellite navigation data unknown type
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
    public static final int MAX_SATNAV_MSG = MT_SATNAV_UNKNOWN - MT_SATNAV_OFFSET;    //number of different messages (from MT_SATNAV_GPS_L1_CA to MT_SATNAV_UNKNOWN)
    public static final int MAX_SATNAV = 38;    //maximum number for satellite identification for any constellation (GPS: 32; GLO: 38; GAL: 36; BDS: 32)
    public static final int MAX_SATNAV_LIMSAT = 4;  //maximum number of satellite identification to be considered when a limit is stated
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
