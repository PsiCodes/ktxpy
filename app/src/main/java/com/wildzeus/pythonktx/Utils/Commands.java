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
package com.wildzeus.pythonktx.Utils;
import android.content.Context;

public class Commands {
    public static String makeCommand(Context context, String Path){
        String fileDir=context.getFilesDir().toString();
       return  ("clear && cd "+fileDir+"/build/build  && export PATH=$PATH:"+context.getApplicationInfo().nativeLibraryDir.toString()+"&& export PYTHONHOME="+fileDir+"/build/build/usr && export PYTHONPATH="+context.getApplicationInfo().nativeLibraryDir.toString()+" &&  export LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH:\" && export LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH$PWD/usr/lib\" && export HOME=$HOME/build/build && cd ../../../app_HOME && libpython3.so "+Path+" && echo '[Enter to Exit]' && read junk && exit");
    }
    public static String getInitialCommand(Context context){
        String fileDir=context.getFilesDir().toString();
        return  ("clear && cd "+fileDir+"/build/build  && export PATH=$PATH:"+context.getApplicationInfo().nativeLibraryDir.toString()+"&& export PYTHONHOME="+fileDir+"/build/build/usr && export PYTHONPATH="+context.getApplicationInfo().nativeLibraryDir.toString()+" &&  export LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH:\" && export LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH$PWD/usr/lib\" && cd /storage/emulated/0 && alias python='libpython3.so' && alias pip='python $HOME/../files/usr/bin/pip3' && alias pip='python $HOME/../files/build/build/usr/bin/pip'");
    }
    public static String getInteractiveMode(Context context){
        String fileDir=context.getFilesDir().toString();
        return  ("clear && cd "+fileDir+"/build/build  && export PATH=$PATH:"+context.getApplicationInfo().nativeLibraryDir.toString()+"&& export PYTHONHOME="+fileDir+"/build/build/usr && export PYTHONPATH="+context.getApplicationInfo().nativeLibraryDir.toString()+" &&  export LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH:\" && export LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH$PWD/usr/lib\" && export HOME=$HOME/build/build && cd ../../../app_HOME && libpython3.so && echo '[Enter to Exit]' && read junk && exit");
    }}