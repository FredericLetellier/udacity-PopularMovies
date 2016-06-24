package technology.tasty.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Popular Movies
 * Created on 24/06/2016 by Espace de travail.
 */
public class Utility {

    public static boolean isOnline(Context context) {
        ConnectivityManager mngr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mngr.getActiveNetworkInfo();

        return !(info == null || (info.getState() != NetworkInfo.State.CONNECTED));
    }
}
