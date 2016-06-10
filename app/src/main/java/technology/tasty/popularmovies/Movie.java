package technology.tasty.popularmovies;

import java.util.Date;
import java.util.List;

/**
 * Popular Movies
 * Created on 10/06/2016 by Espace de travail.
 */
public class Movie{

    String mPosterPath;
    Boolean mAdult;
    String mOverview;
    Date mReleaseDate;
    List<Integer> mGenreIds;
    /**
     * Id of movie
     */
    int mId;
    String mOriginalTitle;
    String mOriginalLanguage;
    /**
     * Title Text
     */
    String mTitle;
    String mBackdropPath;
    Double mPopularity;
    int mVoteCount;
    Boolean mVideo;
    Double mVoteAverage;

    /**
     * Get Id
     * @return mId
     */
    public int getId() {
        return mId;
    }

    /**
     * Set Id
     * @param id : the new resource id
     */
    public void setId(int id) {
        mId = id;
    }

    /**
     * Get Title
     * @return mTitle
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Set Title
     * @param title ; the new title
     */
    public void setTitle(String title) {
        mTitle = title;
    }
}