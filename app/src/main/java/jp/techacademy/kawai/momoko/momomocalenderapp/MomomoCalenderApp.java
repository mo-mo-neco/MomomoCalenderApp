package jp.techacademy.kawai.momoko.momomocalenderapp;

import android.app.Application;
import io.realm.Realm;

/**
 * Created by momon on 2017/09/13.
 */

public class MomomoCalenderApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
