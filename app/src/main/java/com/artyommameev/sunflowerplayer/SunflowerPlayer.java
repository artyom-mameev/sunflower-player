package com.artyommameev.sunflowerplayer;

import android.app.Application;

import com.artyommameev.sunflowerplayer.domain.DaoMaster;
import com.artyommameev.sunflowerplayer.domain.DaoSession;

import lombok.val;

/**
 * The main class of the application.
 *
 * @author Artyom Mameev
 */
public class SunflowerPlayer extends Application {

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        // regular SQLite database
        val devOpenHelper = new DaoMaster.DevOpenHelper(this, getString(R.string.database_name));

        val database = devOpenHelper.getWritableDb();

        daoSession = new DaoMaster(database).newSession();
    }

    /**
     * Returns the greenDAO session to interact with the application database.
     *
     * @return the greenDAO {@link DaoSession}.
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }

}
