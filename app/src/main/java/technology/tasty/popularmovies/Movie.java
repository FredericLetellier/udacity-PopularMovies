package technology.tasty.popularmovies;

import java.util.Date;

/**
 * Popular Movies
 * Created on 10/06/2016 by Espace de travail.
 */
public class Movie{

    /**
     * Path of movie poster image
     */
    String mPosterPath;
    /**
     * Overview of movie
     */
    String mOverview;
    /**
     * Release date of movie
     */
    Date mReleaseDate;
    /**
     * Title of movie
     */
    String mOriginalTitle;
    /**
     * User rating of movie
     */
    Double mVoteAverage;

    /**
     * Get Path of movie poster image
     * @return mPosterPath
     */
    public String getPosterPath() {
        return mPosterPath;
    }

    /**
     * Set Path of movie poster image
     * @param posterPath ; the new poster path
     */
    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    /**
     * Get Overview of movie
     * @return mOverview
     */
    public String getOverview() {
        return mOverview;
    }

    /**
     * Set Overview of movie
     * @param overview ; the new overview
     */
    public void setOverview(String overview) { mOverview = overview; }

    /**
     * Get Release date of movie
     * @return mReleaseDate
     */
    public Date getReleaseDate() {
        return mReleaseDate;
    }

    /**
     * Set Release date of movie
     * @param releaseDate ; the new release date
     */
    public void setReleaseDate(Date releaseDate) {
        mReleaseDate = releaseDate;
    }

    /**
     * Get Original Title
     * @return mOriginalTitle
     */
    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    /**
     * Set Original Title
     * @param title ; the new title
     */
    public void setOriginalTitle(String title) {
        mOriginalTitle = title;
    }

    /**
     * Get User rating of movie
     * @return mVoteAverage
     */
    public Double getVoteAverage() {
        return mVoteAverage;
    }

    /**
     * Set User rating of movie
     * @param voteAverage ; the new vote average
     */
    public void setVoteAverage(Double voteAverage) {
        mVoteAverage = voteAverage;
    }
}