package me.tousifosman.appveto_manager;

import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RpProcessUtils {

    private static final String TAG = RpProcessUtils.class.getSimpleName();

    /**
     * Hidden public constructor.
     */
    private RpProcessUtils() {}

    @Nullable
    public static CurrentFocusedApp getCurrentFocusedApp() {
        try {

            String out = "";

            Process process = Runtime.getRuntime().exec("su -c dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'");
            Scanner inputScanner = new Scanner(process.getInputStream());
            while (inputScanner.hasNext()) {
                out += inputScanner.nextLine();
            }

            inputScanner.close();

            Pattern pattern = Pattern.compile(".*mCurrentFocus=(.*)mFocusedApp=(.*)");
            Matcher matcher = pattern.matcher(out);

            if (matcher.find()) {

                String curFocusAppStr = matcher.group(2);

                if (curFocusAppStr != null) {
                    if (!curFocusAppStr.trim().equals("null")) {
                        pattern = Pattern.compile(".*\\{.*\\{.*\\{[^ ]* [^ ]* ([^ ]*) [^ ]*\\}\\}\\}.*");
                        matcher = pattern.matcher(curFocusAppStr);

                        if (matcher.find()) {

                            curFocusAppStr = matcher.group(1);

                            if (curFocusAppStr != null) {

                                String[] parts = curFocusAppStr.split("/");
                                return new CurrentFocusedApp(parts[0], parts[1]);

                            }

                        } else {
                            Log.e(TAG, "getCurrentFocusedApp: " + "Error parsing Focus App variable. Look into the regex and and content of the variable");
                        }
                    } else {
                        Log.d(TAG, "getCurrentFocusedApp: " + "No Application is focused");
                    }
                } else {
                    Log.e(TAG, "getCurrentFocusedApp: " + "Error parsing Sys Dump Data. Look into the regex and dump");
                }
            } else {
                Log.d(TAG, "getCurrentFocusedApp: Nothing found");
            }

        } catch (IOException e) {
            Log.e(TAG, "getCurrentFocusedApp: Error executing system command", e);
        }
        return null;
    }

    public static class CurrentFocusedApp {
        private String focusAppName;
        private String focusActivityName;
        final HashMap<Integer, Boolean> cachedTypeAccessMap = new HashMap<>();
        Boolean cachedCameraAccess = null;
        Boolean cachedMicAccess = null;

        CurrentFocusedApp(String focusAppName, String focusActivityName) {
            this.focusAppName = focusAppName;
            this.focusActivityName = focusActivityName;
        }

        public static boolean isValid(@Nullable CurrentFocusedApp focusedApp) {
            return focusedApp != null
                    && focusedApp.getFocusAppName() != null
                    && !focusedApp.getFocusAppName().equals("null");
        }

        public String getFocusAppName() {
            return focusAppName;
        }

        public String getFocusActivityName() {
            return focusActivityName;
        }

        @Override
        public String toString() {
            return "CurrentFocusedApp{" +
                    "focusAppName='" + focusAppName + '\'' +
                    ", focusActivityName='" + focusActivityName + '\'' +
                    '}';
        }
    }

}
