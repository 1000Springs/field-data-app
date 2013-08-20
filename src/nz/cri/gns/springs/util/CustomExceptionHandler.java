package nz.cri.gns.springs.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.os.Environment;
import android.util.Log;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

    private String localPath;
    private UncaughtExceptionHandler systemExceptionHandler;

    public CustomExceptionHandler() {
        this.localPath = Environment.getExternalStorageDirectory().getPath() + "/logs";
        systemExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        String timestamp = Util.getTimestampMillis();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = timestamp + "-stacktrace.txt";

        if (localPath != null) {
            writeToFile(stacktrace, filename);
        }

        systemExceptionHandler.uncaughtException(t, e);
    }
    
    private void writeToFile(String stacktrace, String filename) {
        try {
    		File logDir = new File(localPath);
    		if (!logDir.exists()) {
    			if (!logDir.mkdirs()) {
    				Log.e(this.getClass().getName(), "Failed to create log directory "+logDir.getAbsolutePath());
    				return;
    			}
    		}
            BufferedWriter bos = new BufferedWriter(new FileWriter(
            		logDir.getAbsoluteFile() + "/" + filename));
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Error creating error log", e);
        }       
    }  
    
}
