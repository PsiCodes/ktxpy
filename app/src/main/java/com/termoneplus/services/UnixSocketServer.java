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

import android.net.Credentials;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import com.wildzeus.pythonktx.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;


public class UnixSocketServer {
    private static final int SO_TIMEOUT = (3 /*sec.*/ * 1000);

    private final ServerThread server;


    public UnixSocketServer(String address, ConnectionHandler handler) throws IOException {
        final LocalServerSocket socket = new LocalServerSocket(address);
        server = new ServerThread(socket, handler);
        server.setName(BuildConfig.APPLICATION_ID + "-UnixSockets-" + android.os.Process.myPid());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        LocalSocketAddress address = server.socket.getLocalSocketAddress();
        LocalSocket client = new LocalSocket();
        server.interrupted = true;
        try {
            client.connect(address);
        } catch (IOException ignore) {
        }
        try {
            client.close();
        } catch (IOException ignore) {
        }
    }


    public interface ConnectionHandler {
        void handle(InputStream inputStream, OutputStream outputStream) throws IOException;
    }


    private static class ServerThread extends Thread {
        private final LocalServerSocket socket;
        private final ConnectionHandler handler;
        boolean interrupted = false;

        private ServerThread(LocalServerSocket socket, ConnectionHandler handler) {
            this.socket = socket;
            this.handler = handler;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final LocalSocket connection = socket.accept();
                    if (interrupted) {
                        connection.close();
                        break;
                    }
                    connection.setSoTimeout(SO_TIMEOUT);

                    Credentials credentials = connection.getPeerCredentials();
                    int uid = credentials.getUid();
                    // accept requests only from same user id
                    if (uid != android.os.Process.myUid())
                        return;

                    Random random = new Random();
                    WorkerThread worker = new WorkerThread(connection, handler);
                    worker.setName(BuildConfig.APPLICATION_ID
                            + ":unix_socket_" + android.os.Process.myPid()
                            + "." + random.nextInt());
                    worker.setDaemon(true);
                    worker.start();
                } catch (IOException ignore) {
                    break;
                }
            }
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static class WorkerThread extends Thread {
        private final LocalSocket socket;
        private final ConnectionHandler handler;


        WorkerThread(LocalSocket socket, ConnectionHandler handler) {
            this.socket = socket;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                handler.handle(socket.getInputStream(), socket.getOutputStream());
            } catch (IOException ignore) {
            }
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
    }
}
