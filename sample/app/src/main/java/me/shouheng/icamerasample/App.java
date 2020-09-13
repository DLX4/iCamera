package me.shouheng.icamerasample;

import android.annotation.SuppressLint;
import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import java.io.File;

import me.shouheng.icamera.config.ConfigurationProvider;
import me.shouheng.utils.permission.Permission;
import me.shouheng.utils.permission.PermissionUtils;
import me.shouheng.utils.stability.CrashHelper;
import me.shouheng.utils.stability.L;
import me.shouheng.utils.store.PathUtils;
import me.shouheng.vmlib.VMLib;


public class App extends Application {
    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        // initialize the vmlib
        VMLib.onCreate(this);
        // log
        L.getConfig().setLogSwitch(BuildConfig.DEBUG);
        configCrashHelper(application);
        // set iCamera log switch
        ConfigurationProvider.get().setDebug(BuildConfig.DEBUG);
        // leak canary used to detect memory leak of camera
        LeakCanary.install(this);
    }

    @SuppressLint("MissingPermission")
    public static void configCrashHelper(Application application) {
        // crash detect tools, the crash log was saved to : data/data/package_name/files/crash
        if (PermissionUtils.hasPermissions(Permission.STORAGE)) {
            CrashHelper.init(application,
                    new File(PathUtils.getExternalAppFilesPath(), "crash"),
                    new CrashHelper.OnCrashListener() {
                        @Override
                        public void onCrash(String crashInfo, Throwable e) {
                        }
                    });
        }
    }
}

