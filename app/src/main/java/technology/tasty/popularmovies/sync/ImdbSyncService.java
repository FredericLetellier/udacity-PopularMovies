package technology.tasty.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Popular Movies
 * Created on 30/06/2016 by Espace de travail.
 */
public class ImdbSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ImdbSyncAdapter sImdbSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sImdbSyncAdapter == null) {
                sImdbSyncAdapter = new ImdbSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sImdbSyncAdapter.getSyncAdapterBinder();
    }
}