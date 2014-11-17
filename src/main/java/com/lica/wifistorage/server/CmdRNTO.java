/*
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lica.wifistorage.server;

import android.util.Log;

import com.lica.wifistorage.Logger;

import java.io.File;
import java.io.IOException;

public class CmdRNTO extends FtpCmd implements Runnable {
    private static final String TAG = CmdRNTO.class.getSimpleName();

    protected String input;

    public CmdRNTO(SessionThread sessionThread, String input) {
        super(sessionThread);
        this.input = input;
    }

    @Override
    public void run() {
        Logger.d(TAG, "RNTO executing");
        String param = getParameter(input);
        String errString = null;
        File toFile = null;
        mainblock: {
            Logger.i(TAG, "param: " + param);
            toFile = inputPathToChrootedFile(sessionThread.getWorkingDir(), param);
            Logger.i(TAG, "RNTO to file: " + toFile.getPath());
            if (violatesChroot(toFile)) {
                errString = "550 Invalid name or chroot violation\r\n";
                break mainblock;
            }
            File fromFile = sessionThread.getRenameFrom();
            if (fromFile == null) {
                errString = "550 Rename error, maybe RNFR not sent\r\n";
                break mainblock;
            }
            Logger.i(TAG, "RNTO from file: " + fromFile.getPath());
            // TODO: this code is working around a bug that java6 and before cannot
            // reliable move a file, once java7 is supported by Dalvik, this code can
            // be replaced with Files.move()
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile("temp_" + fromFile.getName(), null,
                        sessionThread.getWorkingDir());
                if (fromFile.isDirectory()) {
                    String tmpFilePath = tmpFile.getPath();
                    tmpFile.delete();
                    tmpFile = new File(tmpFilePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
                errString = "550 Error during rename operation\r\n";
                break mainblock;
            }
            if (!fromFile.renameTo(tmpFile)) {
                errString = "550 Error during rename operation\r\n";
                break mainblock;
            }
            fromFile.delete();
            if (!tmpFile.renameTo(toFile)) {
                errString = "550 Error during rename operation\r\n";
                break mainblock;
            }
        }
        if (errString != null) {
            sessionThread.writeString(errString);
            Logger.i(TAG, "RNFR failed: " + errString.trim());
        } else {
            sessionThread.writeString("250 rename successful\r\n");
        }
        sessionThread.setRenameFrom(null);
        Logger.d(TAG, "RNTO finished");
    }
}
