package com.hutchgroup.elog.common;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class StorageDirectory {

    public static String getUSBDirectory() {
        String dir = "";
        String[] dirs = getStorageDirectories();

        for (String d:dirs) {
            if (d.toLowerCase().contains("usb")) {
                dir = d;
            }
        }
        return dir;
    }

    public static String[] getStorageDirectories() {
        String[] dirs = null;
        BufferedReader bufReader = null;
        try {
            bufReader = new BufferedReader(new FileReader("/proc/mounts"));
            ArrayList<String> list = new ArrayList<String>();
            String line;
            while ((line = bufReader.readLine()) != null) {
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String s = tokens.nextToken();
                    s = tokens.nextToken(); // Take the second token, i.e. mount point

                    if (s.equals(Environment.getExternalStorageDirectory().getPath())) {
                        list.add(s);
                    } else if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure") && !line.contains("/mnt/asec") && !line.contains("/mnt/obb") && !line.contains("/dev/mapper") && !line.contains("tmpfs")) {
                            list.add(s);
                        }
                    }
                }
            }

            dirs = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                dirs[i] = list.get(i);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                }
            }

            return dirs;
        }
    }
}
