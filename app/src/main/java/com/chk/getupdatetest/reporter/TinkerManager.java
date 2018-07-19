package com.chk.getupdatetest.reporter;

import android.util.Log;

import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.reporter.DefaultLoadReporter;
import com.tencent.tinker.lib.reporter.DefaultPatchReporter;
import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.UpgradePatchRetry;
import com.tencent.tinker.loader.app.ApplicationLike;

/**
 * Created by CHK on 18-7-17.
 */
public class TinkerManager {
    private static final String TAG = "TinkerManager";

    private static ApplicationLike applicationLike;
    private static boolean isInstalled  = false;

    public static void setApplicationLike (ApplicationLike appLike) {
        applicationLike = appLike;
    }

    public static ApplicationLike getApplicationLike() {
        return applicationLike;
    }

    public static void setUpgradeRetryEnable(boolean enable) {
        UpgradePatchRetry.getInstance(applicationLike.getApplication()).setRetryEnable(enable);
    }

    public static void simpleInstallTinker(ApplicationLike appLike) {
        if (isInstalled) {
            Log.w(TAG,"the tinker has been installed,ignoring it!");
            return;
        }

        TinkerInstaller.install(appLike);
    }

    public static void customInstallTinker(ApplicationLike appLike) {
        if (isInstalled) {
            Log.w(TAG,"the tinker has been installed,ignoring it!");
            return;
        }

        DefaultPatchListener defaultPatchListener = new DefaultPatchListener(appLike.getApplication());
        DefaultPatchReporter defaultPatchReporter = new DefaultPatchReporter(appLike.getApplication());
        DefaultLoadReporter defaultLoadReporter = new DefaultLoadReporter(appLike.getApplication());
        TinkerInstaller.install(appLike,
                defaultLoadReporter,
                defaultPatchReporter,
                defaultPatchListener,
                SimpleResultService.class,
                new UpgradePatch());



//        TinkerInstaller.install(appLike,new SimpleLoadReporter(appLike.getApplication()),
//                null,null,null);
    }

}
