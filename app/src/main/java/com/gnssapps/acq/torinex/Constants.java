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
    public static final double L1_CARRIER_FREQUENCY = 1575.45;   //frequency in MHz
    public static final double L2_CARRIER_FREQUENCY = 1227.60;   //frequency in MHz
    public static final double L5_CARRIER_FREQUENCY = 1176.45;   //frequency in MHz
    public static final int MAX_SATELLITES = 60;   //an estimate of the maximum visible sats in the future
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

}
