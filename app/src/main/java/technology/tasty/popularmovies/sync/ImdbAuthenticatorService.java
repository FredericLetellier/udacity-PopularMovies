package technology.tasty.popularmovies.sync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Popular Movies
 * Created on 30/06/2016 by Espace de travail.
 */
/**
 * The service which allows the sync adapter framework to access the authenticator.
 */
public class ImdbAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private ImdbAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new ImdbAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
