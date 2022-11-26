/*
Copyright (C) 2022-2023  PsiCodes

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.termoneplus.services;

import android.os.Process;
import android.text.TextUtils;

import com.wildzeus.pythonktx.BuildConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

import jackpal.androidterm.TermService;
import jackpal.androidterm.compat.PathSettings;


public class CommandService implements UnixSocketServer.ConnectionHandler {
    private static String socket_prefix = BuildConfig.APPLICATION_ID + "-app_info-";

    private TermService service;
    private UnixSocketServer socket;

    public CommandService(TermService service) {
        this.service = service;
        try {
            socket = new UnixSocketServer(socket_prefix + Process.myUid(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (socket == null) return;
        socket.start();
    }

    public void stop() {
        if (socket == null) return;
        socket.stop();
        socket = null;
    }

    @Override
    public void handle(InputStream basein, OutputStream baseout) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(basein));

        // Note only one command per connection!
        String line = in.readLine();
        if (TextUtils.isEmpty(line)) return;

        PrintStream out = new PrintStream(baseout);
        switch (line) {
            case "get aliases":
                // force interactive shell
                out.println("alias sh='sh -i'");

                printExternalAliases(out);
                break;
        }
        out.flush();
    }

    private void printExternalAliases(PrintStream out) {
        final Pattern pattern = Pattern.compile("libexec-(.*).so");

        for (String entry : PathSettings.buildPATH().split(File.pathSeparator)) {
            File dir = new File(entry);

            File[] cmdlist = null;
            try {
                cmdlist = dir.listFiles(file -> pattern.matcher(file.getName()).matches());
            } catch (Exception ignore) {
            }
            if (cmdlist == null) continue;

            for (File cmd : cmdlist) {
                ProcessBuilder pb = new ProcessBuilder(cmd.getPath(), "aliases");
                try {
                    java.lang.Process p = pb.start();

                    // close process "input stream" to prevent command
                    // to wait for user input.
                    p.getOutputStream().close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while (true) {
                        String line = in.readLine();
                        if (line == null) break;
                        out.println(line);
                    }
                    out.flush();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
