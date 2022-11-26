package jackpal.androidterm.libtermexec.v1;

import android.content.IntentSender;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
interface ITerminal {
    IntentSender startSession(in ParcelFileDescriptor pseudoTerminalMultiplexerFd, in ResultReceiver callback);
}
