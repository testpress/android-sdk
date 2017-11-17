package in.testpress.models.greendao;

import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.database.Database;

import in.testpress.core.TestpressSdk;

public class TestpressSDK {

    private static DaoSession daoSession;
    private static Database database;

    private static Database getDatabase(@NonNull Context context) {
        if (database == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                    context.getApplicationContext(), TestpressSdk.TESTPRESS_SDK_DATABASE);

            database = helper.getWritableDb();
        }
        return database;
    }

    private static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            database = getDatabase(context);
            daoSession = new DaoMaster(database).newSession();
        }
        return daoSession;
    }

    public static ExamDao getExamDao(Context context) {
        return getDaoSession(context).getExamDao();
    }

    public static LanguageDao getLanguageDao(Context context) {
        return getDaoSession(context).getLanguageDao();
    }
}
