package me.shouheng.icamerasample.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import me.shouheng.icamera.config.ConfigurationProvider;
import me.shouheng.icamera.config.creator.CameraManagerCreator;
import me.shouheng.icamera.config.creator.CameraPreviewCreator;
import me.shouheng.icamera.config.creator.impl.Camera2OnlyCreator;
import me.shouheng.icamera.config.creator.impl.CameraManagerCreatorImpl;
import me.shouheng.icamera.config.creator.impl.CameraPreviewCreatorImpl;
import me.shouheng.icamera.config.creator.impl.SurfaceViewOnlyCreator;
import me.shouheng.icamera.config.creator.impl.TextureViewOnlyCreator;
import me.shouheng.icamerasample.R;
import me.shouheng.icamerasample.databinding.ActivityMainBinding;
import me.shouheng.utils.permission.Permission;
import me.shouheng.utils.permission.PermissionUtils;
import me.shouheng.utils.permission.callback.OnGetPermissionCallback;
import me.shouheng.utils.store.SPUtils;
import me.shouheng.utils.ui.BarUtils;
import me.shouheng.vmlib.base.CommonActivity;
import me.shouheng.vmlib.comn.EmptyViewModel;

public class MainActivity extends CommonActivity<EmptyViewModel, ActivityMainBinding> {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void doCreateView(Bundle savedInstanceState) {
        final ActivityMainBinding binding = this.getBinding();

        Log.d("MainActivity", "doCreateView");
        BarUtils.setStatusBarLightMode(getWindow(), false);
        ConfigurationProvider.get().setDebug(true);
        setSupportActionBar(binding.toolbar);

//        binding.rbCamera1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    switchToCameraOption(0);
//                }
//            }
//        });
        binding.rbCamera2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchToCameraOption(1);
                }
            }
        });

        binding.rbCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchToCameraOption(2);
                }
            }
        });

        binding.rbSurface.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchToPreviewOption(0);
                }

            }
        });
        binding.rbTexture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchToPreviewOption(1);
                }
            }
        });
        binding.rbPlatform.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchToPreviewOption(2);
                }
            }
        });

        switchToCameraOption(SPUtils.get().getInt("__camera_option", 2));
        switchToPreviewOption(SPUtils.get().getInt("__preview_option", 2));

        // pre-prepare camera2 params, this option will save few milliseconds while launch camera2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConfigurationProvider.get().prepareCamera2(this);
        }
    }

    public void openCamera(View view) {
        PermissionUtils.checkPermissions(this, new OnGetPermissionCallback() {
            @Override
            public void onGetPermission() {
                startActivity(CameraActivity.class);
            }
        }, Permission.CAMERA, Permission.STORAGE, Permission.MICROPHONE);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void switchToCameraOption(int option) {
        final ActivityMainBinding binding = this.getBinding();

        CameraManagerCreator creator;
        switch (option) {
//            case 0:
//                creator = new Camera1OnlyCreator();
//                break;
            case 1:
                creator = new Camera2OnlyCreator();
                break;
            default:
                creator = new CameraManagerCreatorImpl();
        }
        ConfigurationProvider.get().setCameraManagerCreator(creator);

        switch (option) {
//            case 0:
//                binding.rbCamera1.setChecked(true);
//                break;
            case 1:
                binding.rbCamera2.setChecked(true);
                break;
            default:
                binding.rbCamera.setChecked(true);
        }

        SPUtils.get().put("__camera_option", option);
    }

    private void switchToPreviewOption(int option) {
        final ActivityMainBinding binding = this.getBinding();

        CameraPreviewCreator creator;
        switch (option) {
            case 0:
                creator = new SurfaceViewOnlyCreator();
                break;
            case 1:
                creator = new TextureViewOnlyCreator();
                break;
            default:
                creator = new CameraPreviewCreatorImpl();
        }
        ConfigurationProvider.get().setCameraPreviewCreator(creator);

        switch (option) {
            case 0:
                binding.rbSurface.setChecked(true);
                break;
            case 1:
                binding.rbTexture.setChecked(true);
                break;
            default:
                binding.rbPlatform.setChecked(true);
        }

        SPUtils.get().put("__preview_option", option);
    }

}
