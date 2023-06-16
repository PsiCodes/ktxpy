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
package com.termoneplus

import jackpal.androidterm.util.TermSettings
import jackpal.androidterm.TermService
import android.content.Intent
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.Application
import jackpal.androidterm.compat.PathCollector
import jackpal.androidterm.compat.PathCollector.OnPathsReceivedListener

open class RemoteActionActivity : AppCompatActivity() {
    @JvmField
    protected var mSettings: TermSettings? = null
    private var path_collected = false
    protected var termService: TermService? = null
        private set
    private var service_intent: Intent? = null
    private val service_connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            termService = null
            if (service == null) return
            val binder = service as TermService.TSBinder
            termService = binder.service
            processIntent()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            termService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* intent is required */
        val intent = intent
        if (intent == null) {
            finish()
            return
        }
        mSettings = TermSettings(this)
        val path_collector = PathCollector(this)
        path_collector.setOnPathsReceivedListener {
            path_collected = true
            processIntent()
        }
        service_intent = Intent(this, TermService::class.java)
        startService(service_intent)
    }

    override fun onStart() {
        super.onStart()
        if (!bindService(service_intent, service_connection, BIND_AUTO_CREATE)) {
            Log.e(Application.APP_TAG, "bind to service failed!")
            finish()
        }
    }

    override fun onStop() {
        unbindService(service_connection)
        super.onStop()
    }

    override fun onDestroy() {
        if (termService != null) {
            if (termService!!.sessionCount == 0) stopService(service_intent)
            termService = null
        }
        super.onDestroy()
    }

    protected open fun processAction(intent: Intent, action: String) {
        //nop, override at child level
    }

    private fun processIntent() {
        /* process intent after path collection and start of service */
        if (termService == null) return
        if (!path_collected) return

        /* intent is required - see onCreate() */
        val intent = intent
        val action = intent.action
        action?.let { processAction(intent, it) }
        finish()
    }
}