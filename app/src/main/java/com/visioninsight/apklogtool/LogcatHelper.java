package com.visioninsight.apklogtool;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogcatHelper {
    private String pathLogcat;
    private LogThread logThread;

    private LogcatListener listener;

    public LogcatHelper(Context context) {
        init(context);
    }

    private void init(Context context) {
        pathLogcat = context.getExternalFilesDir(null) + File.separator + "log";
        File file = new File(pathLogcat);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public void start() {
        if (logThread == null) {
            logThread = new LogThread(pathLogcat);
        }
        logThread.start();
        if (listener != null) {
            listener.startLog();
        }
    }

    public void stop() {
        if (logThread != null) {
            logThread.stopLogs();
            if (listener != null) {
                listener.stopLog(logThread.getLogFilePath());
            }
            logThread = null;
        }
    }

    public void setLogcatListener(LogcatListener listener) {
        this.listener = listener;
    }

    static class LogThread extends Thread {

        private String path;
        private String cmd;

        private boolean running = false;
        private Process process;
        private BufferedReader reader;
        private FileOutputStream ops;
        private File logFile;

        public LogThread(String path) {
            this.path = path;
            cmd = "logcat";
        }

        public void stopLogs() {
            running = false;
        }

        @Override
        public void run() {
            try {
                logFile = new File(path, getLogFileName());
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }

                ops = new FileOutputStream(logFile);
                process = Runtime.getRuntime().exec(new String[]{"bugreport"});
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while (running && (line = reader.readLine()) != null) {
                    if (!running)
                        break;
                    if (line.length() == 0) {
                        continue;
                    }
                    ops.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    process.destroy();
                    process = null;
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    reader = null;
                }
                if (ops != null) {
                    try {
                        ops.flush();
                        ops.close();
                        ops = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private String getLogFileName() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String date = format.format(new Date(System.currentTimeMillis()));
            return "log-" + date + ".log";
        }

        public String getLogFilePath() {
            if (logFile != null) {
                return logFile.getPath();
            }
            return "";
        }

        @Override
        public synchronized void start() {
            running = true;
            super.start();
        }
    }

    interface LogcatListener {

        void startLog();

        void stopLog(String logFile);
    }
}
