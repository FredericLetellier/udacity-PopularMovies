package technology.tasty.popularmovies;

import java.util.List;

/**
 * Popular Movies
 * Created on 24/06/2016 by Espace de travail.
 */
public interface AsyncResponse {
    void processFinish(List<Movie> result);
}
