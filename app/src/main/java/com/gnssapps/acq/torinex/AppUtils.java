package com.gnssapps.acq.torinex;

import android.content.Context;
import android.location.GnssNavigationMessage;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;

public class AppUtils {
    /**showInfoHelp shows an alert dialog to present help information (title and body) in the given context.
     * <br>If the context is null, it does nothing.
     * @param infoTitle the title of the information to be presented
     * @param infoBody the body text of the information to be presented
     * @param context the context where the alert dialog will be shown
     */
    public static void showInfoHelp(String infoTitle, String infoBody, Context context) {
        // setup the alert builder
        if (context == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(infoTitle);
        builder.setMessage(Html.fromHtml(infoBody, Html.FROM_HTML_MODE_COMPACT));
        builder.setPositiveButton("OK", null);
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /**tagTextColor adds HTML tags to the given text to allow display of the text in the color given.
     * @param textToTag the text to be tagged
     * @param color the color to be used when tagging
     * @return the HTML tagged text
     */
    public static String tagTextColor (String textToTag, String color) {
        if (color.isEmpty()) return textToTag;
        return "<font color=\"" + color + "\">" + textToTag + "</font>";
    }
    /**
     * fillFromDir creates in the toFill list the list of existing files or directories in the fromDir directory.
     * The list of files will exclude any existing *.txt file.
     * @param toFill the string list where file names in the directory will be placed and sorted
     * @param fromDir the directory to be inspected for files
     * @param fileORdir if true, extract files only, if false extract directories ony
     */
    public static void fillFromDir(List<String> toFill, String fromDir, boolean fileORdir) {
        String[] dirContents;
        toFill.clear();
        File appDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS),
                fromDir);
        if (appDir.exists()) {
            if (fileORdir) {
                dirContents = appDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return (new File(dir, name).isFile() && !name.toUpperCase().endsWith(".TXT"));
                    }
                });
            } else {
                dirContents = appDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return new File(dir, name).isDirectory();
                    }
                });
            }
            if (dirContents != null) {
                Collections.addAll(toFill, dirContents);
                Collections.sort(toFill);
            }
        }
    }
}
