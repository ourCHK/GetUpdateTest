package com.chk.getupdatetest.reporter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;

import java.io.File;

/**
 * Created by CHK on 18-7-17.
 */
public class SimpleResultService extends DefaultTinkerResultService {

    private final String TAG = SimpleResultService.class.getSimpleName();

    public SimpleResultService() {
    }

    @Override
    public void onPatchResult(final PatchResult result) {
//        super.onPatchResult(result);
        if (result == null) {
            Log.i(TAG,"patch result is null!!!!");
            return;
        }
        Log.i(TAG,"patch result:"+result.toString());

        //first, we want to kill the recover process
        TinkerServiceInternals.killTinkerPatchServiceProcess(getApplicationContext());

//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (result.isSuccess) {
//                    Toast.makeText(getApplicationContext(), "patch success, please restart process fully", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "patch fail, please check reason", Toast.LENGTH_LONG).show();
//                }
//            }
//        });

        // is success and newPatch, it is nice to delete the raw file, and restart at once
        // for old patch, you can't delete the patch file
        if (result.isSuccess) {
            deleteRawPatchFile(new File(result.rawPatchFilePath));
            Handler handler = new Handler(Looper.getMainLooper());  //仅在补丁成功的情况下我们才弹出toast,不知道为什么service会走两次
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "patch success, please restart process fully", Toast.LENGTH_LONG).show();
                }
            });

            if(!checkIfNeedKill(result)) {
                if (Utils.isBackground()) {     //被切后台更新
                    TinkerLog.i(TAG, "it is in background, just restart process");
                    restartProcess();
                } else {    //关闭屏幕更新
                    //we can wait process at background, such as onAppBackground
                    //or we can restart when the screen off
                    TinkerLog.i(TAG, "tinker wait screen to restart process");
                    new Utils.ScreenState(getApplicationContext(), new Utils.ScreenState.IOnScreenOff() {
                        @Override
                        public void onScreenOff() {
                            restartProcess();
                        }
                    });
                }
                Log.i(TAG, "I have already install the newly patch version!");
            }

            //not like TinkerResultService, I want to restart just when I am at background!
            //if you have not install tinker this moment, you can use TinkerApplicationHelper api
//            if (checkIfNeedKill(result)) {
//                Toast.makeText(this, "done nothing", Toast.LENGTH_SHORT).show();
////                if (Utils.isBackground()) {
////                    TinkerLog.i(TAG, "it is in background, just restart process");
////                    restartProcess();
////                } else {
////                    //we can wait process at background, such as onAppBackground
////                    //or we can restart when the screen off
////                    TinkerLog.i(TAG, "tinker wait screen to restart process");
////                    new Utils.ScreenState(getApplicationContext(), new Utils.ScreenState.IOnScreenOff() {
////                        @Override
////                        public void onScreenOff() {
////                            restartProcess();
////                        }
////                    });
////                }
//            } else {
//                TinkerLog.i(TAG, "I have already install the newly patch version!");
//            }

//            if (!checkIfNeedKill(result)) {  //如果当前的补丁版本和我们即将打补丁的版本不一致的话,那么这里返回true表示需要重启进程
//                Log.i(TAG, "I have already install the newly patch version!");
//            }
        }
    }

    /**
     * you can restart your process through service or broadcast
     */
    private void restartProcess() {
        TinkerLog.i(TAG, "app is background now, i can kill quietly");
        //you can send service or broadcast intent to restart your process
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * this comment is made by myself,from the source code,we can know that
     * they just compare the loaded patch version and the patch version we are
     * about to patching,if they are different,so we need to restart the process
     * in order to apply the newest patch,otherwise,we just return false,it means
     * we have no need to restart the process.
     * @param result
     * @return
     */
    @Override
    public boolean checkIfNeedKill(PatchResult result) {
        return super.checkIfNeedKill(result);
    }
}
