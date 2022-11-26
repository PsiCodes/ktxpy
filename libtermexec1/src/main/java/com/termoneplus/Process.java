package com.termoneplus;

import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class Process {

    static {
        System.loadLibrary("term-system");
    }

    public static int createSubprocess(
            ParcelFileDescriptor masterPty,
            String cmd, String[] arguments, String[] environment
    ) throws IOException {
        // Let convert to UTF-8 in java code instead in native methods
        try {
            // prepare command path
            byte[] path = cmd.getBytes("UTF-8");

            // prepare command arguments
            byte[][] argv;
            argv = new byte[arguments.length][0];
            for (int k = 0; k < arguments.length; k++) {
                String val = arguments[k];
                argv[k] = val.getBytes("UTF-8");
            }

            // prepare command environment
            byte[][] envp;
            envp = new byte[environment.length][0];
            for (int k = 0; k < environment.length; k++) {
                String val = environment[k];
                envp[k] = val.getBytes("UTF-8");
            }

            // create terminal process ...
            int ptm = masterPty.getFd();
            return Native.createSubprocess(ptm, path, argv, envp);
        } catch (UnsupportedEncodingException ignore) {
            // TODO: ignore for now
        }
        return -1;
    }

    public static int waitExit(int pid) {
        return Native.waitExit(pid);
    }

    public static void finishChilds(int pid) {
        Native.finishChilds(pid);
    }


    private static class Native {
        private static native int createSubprocess(
                int ptm,
                byte[] path, byte[][] argv, byte[][] envp
        ) throws IOException;
        private static native int waitExit(int pid);
        private static native void finishChilds(int pid);
    }
}
