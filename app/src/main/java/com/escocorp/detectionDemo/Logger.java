package com.escocorp.detectionDemo;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A logger that uses the standard Android Log class to log exceptions, and also logs them to a
 * file on the device. Requires permission WRITE_EXTERNAL_STORAGE in AndroidManifest.xml.
 * @author Cindy Potvin
 */
public class Logger
{
    /**
     * Sends an error message to LogCat and to a log file.
     * @param logMessageTag A tag identifying a group of log messages. Should be a constant in the
     *                      class calling the logger.
     * @param logMessage The message to add to the log.
     */
    public static void e(String logMessageTag, String logMessage)
    {
        if (!Log.isLoggable(logMessageTag, Log.ERROR))
            return;

        int logResult = Log.e(logMessageTag, logMessage);
        if (logResult > 0)
            logToFile(logMessageTag, logMessage);
    }

    /**
     * Sends an error message and the exception to LogCat and to a log file.
     * @param logMessageTag A tag identifying a group of log messages. Should be a constant in the
     *                      class calling the logger.
     * @param logMessage The message to add to the log.
     * @param throwableException An exception to log
     */
    public static void e(String logMessageTag, String logMessage, Throwable throwableException)
    {
        int logResult = Log.e(logMessageTag, logMessage, throwableException);
        if (logResult > 0)
            logToFile(logMessageTag, logMessage + "\r\n" + Log.getStackTraceString(throwableException));
    }

// The i and w method for info and warning logs should be implemented in the same way as the e method for error logs.

    /**
     * Sends a message to LogCat and to a log file.
     * @param logMessageTag A tag identifying a group of log messages. Should be a constant in the
     *                      class calling the logger.
     * @param logMessage The message to add to the log.
     */
    public static void v(String logMessageTag, String logMessage)
    {
        int logResult = Log.v(logMessageTag, logMessage);
        if (logResult > 0)
            logToFile(logMessageTag, logMessage);
    }

    /**
     * Sends a message to LogCat and to a log file.
     * @param logMessageTag A tag identifying a group of log messages. Should be a constant in the
     *                      class calling the logger.
     * @param logMessage The message to add to the log.
     * @param logTime whether to include time in the message
     */
    public static void v(String logMessageTag, String logMessage, boolean logTime)
    {
        int logResult = Log.v(logMessageTag, logMessage);
        if (logResult > 0)
            logToFile(logMessageTag, logMessage, logTime);
    }

    /**
     * Sends a message and the exception to LogCat and to a log file.
     * @param logMessageTag A tag identifying a group of log messages. Should be a constant in the
     *                      class calling the logger.
     * @param logMessage The message to add to the log.
     * @param throwableException An exception to log
     */
    public static void v(String logMessageTag, String logMessage, Throwable throwableException)
    {
        int logResult = Log.v(logMessageTag, logMessage, throwableException);
        if (logResult > 0)
            logToFile(logMessageTag,  logMessage + "\r\n" + Log.getStackTraceString(throwableException));
    }

// The d method for debug logs should be implemented in the same way as the v method for verbose logs.

    /**
     * Writes a message to the log file on the device.
     * @param logMessageTag A tag identifying a group of log messages.
     * @param logMessage The message to add to the log.
     */
    private static void logToFile(String logMessageTag, String logMessage)
    {
        logToFile(logMessageTag, logMessage, true);
    }

    /**
     * Writes a message to the log file on the device.
     * @param logMessageTag A tag identifying a group of log messages.
     * @param logMessage The message to add to the log.
     * @param logTime Whether to include a timestamp in the message
     */
    private static void logToFile(String logMessageTag, String logMessage, boolean logTime)
    {
        try
        {
            // Gets the log file from the root of the primary storage. If it does
            // not exist, the file is created.
           // File logFile = new File(Environment.getExternalStoragePublicDirectory(
           //         Environment.DIRECTORY_DOCUMENTS), logMessageTag + ".txt");
            File logFolder = new File(Environment.getExternalStorageDirectory(), File.separator + "ESCO");
            if (!logFolder.exists())
                logFolder.mkdir();
            File logFile = new File(Environment.getExternalStorageDirectory(), File.separator + "ESCO" + File.separator + logMessageTag + ".txt");
            if (!logFile.exists())
                logFile.createNewFile();
            // Write the message to the log with a timestamp
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            if(logTime)
                writer.write(String.format("%s,%s\r\n", DateTime.getTimeStamp(), logMessage));
            else
                writer.write(String.format("%s\r\n", logMessage));
            writer.close();

        }
        catch (IOException e)
        {
            Log.e("Logger", "Unable to log exception to file.");
        }
    }
}

class DateTime {
    /**
     * Gets a stamp containing the current date and time to write to the log.
     * @return The stamp for the current date and time.
     */
    public static String getDateTimeStamp()
    {
        Date dateNow = Calendar.getInstance().getTime();
        // My locale, so all the log files have the same date and time format
        return (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH).format(dateNow));
    }

    public static String getDateStamp()
    {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);;
        return dateFormat.format(new Date());
    }

    public static String getTimeStamp()
    {
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);
        return timeFormat.format(new Date());
    }
}