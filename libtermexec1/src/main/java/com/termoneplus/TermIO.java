package com.termoneplus;

import android.os.ParcelFileDescriptor;

import java.io.IOException;


public class TermIO {

    static {
        System.loadLibrary("term-system");
    }

    public static void setUTF8Input(ParcelFileDescriptor masterPty, boolean flag) throws IOException {
        int fd = masterPty.getFd();
        Native.setUTF8Input(fd, flag);
    }

    public static void setWindowSize(ParcelFileDescriptor masterPty, int row, int col) throws IOException {
        int fd = masterPty.getFd();
        Native.setWindowSize(fd, row, col, 0, 0);
    }


    private static class Native {
        private static native void setUTF8Input(int fd, boolean flag) throws IOException;

        private static native void setWindowSize(int fd, int row, int col, int xpixel, int ypixel) throws IOException;
    }
}
