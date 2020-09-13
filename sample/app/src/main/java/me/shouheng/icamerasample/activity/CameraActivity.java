package me.shouheng.icamerasample.activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.shouheng.icamera.config.ConfigurationProvider;
import me.shouheng.icamera.config.size.Size;
import me.shouheng.icamera.config.size.SizeMap;
import me.shouheng.icamera.enums.CameraFace;
import me.shouheng.icamera.enums.CameraSizeFor;
import me.shouheng.icamera.enums.FlashMode;
import me.shouheng.icamera.enums.MediaType;
import me.shouheng.icamera.listener.CameraCloseListener;
import me.shouheng.icamera.listener.CameraOpenListener;
import me.shouheng.icamera.listener.CameraPhotoListener;
import me.shouheng.icamera.listener.CameraPreviewListener;
import me.shouheng.icamera.listener.CameraSizeListener;
import me.shouheng.icamera.listener.CameraVideoListener;
import me.shouheng.icamera.listener.OnMoveListener;
import me.shouheng.icamera.listener.OnOrientationChangedListener;
import me.shouheng.icamera.util.ImageHelper;
import me.shouheng.icamera.util.XLog;
import me.shouheng.icamerasample.R;
import me.shouheng.icamerasample.databinding.ActivityCameraBinding;
import me.shouheng.uix.common.listener.NoDoubleClickListener;
import me.shouheng.utils.app.ResUtils;
import me.shouheng.utils.stability.L;
import me.shouheng.utils.ui.BarUtils;
import me.shouheng.utils.ui.ViewUtils;
import me.shouheng.vmlib.base.CommonActivity;
import me.shouheng.vmlib.comn.EmptyViewModel;

import static me.shouheng.icamerasample.utils.FileHelper.getSavedFile;
import static me.shouheng.icamerasample.utils.FileHelper.saveImageToGallery;
import static me.shouheng.icamerasample.utils.FileHelper.saveVideoToGallery;

public class CameraActivity extends CommonActivity<EmptyViewModel, ActivityCameraBinding> {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_camera;
    }


    /**
     * Is currently recording video.
     */
    private boolean isCameraRecording = false;

    @Override
    protected void doCreateView(@Nullable Bundle savedInstanceState) {
        XLog.d("CameraActivity", "doCreateView");
        BarUtils.setStatusBarLightMode(getWindow(), false);
        configDrawer();
        configMain();
    }

    @Override
    protected void onPause() {
        final ActivityCameraBinding binding = this.getBinding();
        super.onPause();
        binding.cv.closeCamera(new CameraCloseListener() {
            @Override
            public void onCameraClosed(int cameraFace) {
                L.d("closeCamera : $it");
            }
        });
    }

    @Override
    protected void onDestroy() {
        final ActivityCameraBinding binding = this.getBinding();
        super.onDestroy();
        binding.cv.releaseCamera();
    }

    @Override
    protected void onResume() {
        final ActivityCameraBinding binding = this.getBinding();

        super.onResume();
        if (!binding.cv.isCameraOpened()) {
            binding.cv.openCamera(new CameraOpenListener() {

                @Override
                public void onCameraOpened(int cameraFace) {
                    L.d("onCameraOpened");
                }

                @Override
                public void onCameraOpenError(Throwable throwable) {
                    L.e(throwable);
                    toast("Camera open error : " + throwable);
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        XLog.d("CameraActivity", "onConfigurationChanged");
    }

    private void configDrawer() {
        final ActivityCameraBinding binding = this.getBinding();

        binding.scVoice.setChecked(ConfigurationProvider.get().isVoiceEnable());
        binding.scVoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                binding.cv.setVoiceEnable(isChecked);
            }
        });
        binding.scFocus.setChecked(ConfigurationProvider.get().isAutoFocus());
        binding.scFocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                binding.cv.setAutoFocus(isChecked);
            }
        });
        binding.scTouchFocus.setChecked(true);
        binding.scTouchFocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                binding.cv.setUseTouchFocus(isChecked);
            }
        });

        binding.scTouchZoom.setChecked(true);
        binding.scTouchZoom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                binding.cv.setTouchZoomEnable(isChecked);
            }
        });

        binding.tvPreviewSizes.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(@NotNull View view) {
                showPopDialog(view, binding.cv.getSizes(CameraSizeFor.SIZE_FOR_PREVIEW));
            }
        });

        binding.tvPictureSizes.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(@NotNull View view) {
                showPopDialog(view, binding.cv.getSizes(CameraSizeFor.SIZE_FOR_PICTURE));
            }
        });
        binding.tvVideoSizes.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(@NotNull View view) {
                showPopDialog(view, binding.cv.getSizes(CameraSizeFor.SIZE_FOR_VIDEO));
            }
        });
    }

    private void configMain() {
        final ActivityCameraBinding binding = this.getBinding();

        binding.ivSetting.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(@NotNull View view) {
                binding.drawer.openDrawer(Gravity.END);
            }
        });
        switch (ConfigurationProvider.get().getDefaultFlashMode()) {
            case FlashMode.FLASH_AUTO:
                binding.ivFlash.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                break;
            case FlashMode.FLASH_OFF:
                binding.ivFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
                break;
            case FlashMode.FLASH_ON:
                binding.ivFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
                break;
            default:
                binding.ivFlash.setImageResource(R.drawable.ic_flash_auto_white_24dp);
        }

        binding.ivFlash.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(@NotNull View view) {
                int mode;
                switch (binding.cv.getFlashMode()) {
                    case FlashMode.FLASH_AUTO:
                        mode = FlashMode.FLASH_ON;
                        break;
                    case FlashMode.FLASH_OFF:
                        mode = FlashMode.FLASH_AUTO;
                        break;
                    case FlashMode.FLASH_ON:
                        mode = FlashMode.FLASH_OFF;
                        break;
                    default:
                        mode = FlashMode.FLASH_AUTO;
                        break;
                }
                binding.cv.setFlashMode(mode);
                switch (mode) {
                    case FlashMode.FLASH_AUTO:
                        binding.ivFlash.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                        break;
                    case FlashMode.FLASH_OFF:
                        binding.ivFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
                        break;
                    case FlashMode.FLASH_ON:
                        binding.ivFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
                        break;
                    default:
                        binding.ivFlash.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                        break;
                }
            }
        });

        binding.sb.animate()
                .translationX(Integer.valueOf(ViewUtils.dp2px(130f)).floatValue())
                .setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.sb.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });


        binding.sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float zoom = 1 + (binding.cv.getMaxZoom() - 1) * (1.0f * progress / seekBar.getMax());
                binding.cv.setZoom(zoom);
                displayCameraInfo();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float zoom = 1 + (binding.cv.getMaxZoom() - 1) * (1.0f * progress / seekBar.getMax());
                binding.cv.setZoom(zoom);
                displayCameraInfo();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        binding.ivSwitch.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (binding.cv.getCameraFace() == CameraFace.FACE_FRONT)
                    binding.cv.switchCamera(CameraFace.FACE_REAR);
                else {
                    binding.cv.switchCamera(CameraFace.FACE_FRONT);
                }
            }
        });

        binding.ivTypeSwitch.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(@NotNull View view) {
                // switch camera between video and picture mode
                if (binding.cv.getMediaType() == MediaType.TYPE_PICTURE) {
                    binding.ivTypeSwitch.setImageResource(R.drawable.ic_videocam_white_24dp);
                    binding.cv.setMediaType(MediaType.TYPE_VIDEO);
                } else {
                    binding.ivTypeSwitch.setImageResource(R.drawable.ic_photo_camera_white_24dp);
                    binding.cv.setMediaType(MediaType.TYPE_PICTURE);
                }
                displayCameraInfo();
            }
        });

        binding.cv.setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(boolean left) {
                toast(left ? "LEFT" : "RIGHT");
            }
        });

        binding.cv.addCameraSizeListener(new CameraSizeListener() {
            @Override
            public void onPreviewSizeUpdated(Size previewSize) {
                L.d("onPreviewSizeUpdated : $previewSize");
                displayCameraInfo();
            }

            @Override
            public void onVideoSizeUpdated(Size videoSize) {
                L.d("onVideoSizeUpdated : $videoSize");
                displayCameraInfo();
            }

            @Override
            public void onPictureSizeUpdated(Size pictureSize) {
                L.d("onPictureSizeUpdated : $pictureSize");
                displayCameraInfo();
            }
        });


        binding.ivShot.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (binding.cv.getMediaType() == MediaType.TYPE_PICTURE) {
                    takePicture();
                } else {
                    takeVideo();
                }
            }
        });

        binding.cv.addOrientationChangedListener(new OnOrientationChangedListener() {

            @Override
            public void onOrientationChanged(int degree) {
                ViewCompat.setRotation(binding.ivFlash, (float) degree);
                ViewCompat.setRotation(binding.ivSwitch, (float) degree);
                ViewCompat.setRotation(binding.ivTypeSwitch, (float) degree);
                ViewCompat.setRotation(binding.ivSetting, (float) degree);
            }
        });

        binding.cv.setCameraPreviewListener(new CameraPreviewListener() {
            private int frame = 0;

            @Override
            public void onPreviewFrame(byte[] data, Size size, int format) {
                L.d("onPreviewFrame");
                if (frame % 25 == 0) {
                    frame = 1;
                    try {
                        int light = ImageHelper.convertYUV420_NV21toARGB8888(data, size.width, size.height);
                        if (light <= 30) {
                            binding.tvLightTip.setText(ResUtils.getText(R.string.camera_main_light_tip));
                        } else {
                            binding.tvLightTip.setText("");
                        }
                        binding.ivPreview.setImageBitmap(ImageHelper.convertNV21ToBitmap(data, size.width, size.height));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                frame++;
            }
        });

        displayCameraInfo();
    }

    private void takePicture() {
        final ActivityCameraBinding binding = this.getBinding();

        File fileToSave = getSavedFile("jpg");
        binding.cv.takePicture(fileToSave, new CameraPhotoListener() {

            @Override
            public void onPictureTaken(byte[] data, File picture) {
                saveImageToGallery(getContext(), fileToSave, fileToSave.getName());
                toast("Saved to $fileToSave");
                binding.cv.resumePreview();
            }

            @Override
            public void onCaptureFailed(Throwable throwable) {
                L.e(throwable);
                toast("onCaptureFailed : " + throwable);
            }
        });
    }

    private void takeVideo() {
        final ActivityCameraBinding binding = this.getBinding();

        if (!isCameraRecording) {
            int seconds;
            try {
                seconds = Integer.parseInt(binding.etVideoDuration.getText().toString());
            } catch (Exception e) {
                seconds = 0;
            }

            binding.cv.setVideoDuration(seconds * 1000);
            File fileToSave = getSavedFile("mp4");

            binding.cv.startVideoRecord(fileToSave, new CameraVideoListener() {

                @Override
                public void onVideoRecordStart() {
                    toast("Video record START!");
                    isCameraRecording = true;
                }

                @Override
                public void onVideoRecordStop(File file) {
                    isCameraRecording = false;
                    saveVideoToGallery(getContext(), fileToSave, fileToSave.getName());
                    toast("Saved to $file");
                }

                @Override
                public void onVideoRecordError(Throwable throwable) {
                    isCameraRecording = false;
                    toast(throwable + "");
                    L.e(throwable);
                }
            });
        } else {
            binding.cv.stopVideoRecord();
        }
    }

    @SuppressLint("NewApi")
    private void showPopDialog(View view, SizeMap sizes) {
        final ActivityCameraBinding binding = this.getBinding();

        PopupMenu pop = new PopupMenu(this, view);
        List<Size> list = new ArrayList<>();
        for (List<Size> size : sizes.values()) {
            list.addAll(size);
        }
        for (Size size : list) {
            pop.getMenu().add(size.toString());
        }
        pop.setGravity(Gravity.END);
        pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = (String) item.getTitle();
                String txt = title.substring(1, title.length() - 1);
                String[] arr = txt.split(",");
                binding.cv.setExpectSize(Size.of(Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim())));
                return true;
            }
        });
        pop.show();
    }

    @SuppressLint("SetTextI18n")
    private void displayCameraInfo() {
        final ActivityCameraBinding binding = this.getBinding();

        binding.tvInfo.setText("Camera Info:\n" +
                "1.Preview Size: " + String.valueOf(binding.cv.getSize(CameraSizeFor.SIZE_FOR_PREVIEW)) + "\n" +
                "2.Picture Size: " + String.valueOf(binding.cv.getSize(CameraSizeFor.SIZE_FOR_PICTURE)) + "\n" +
                "3.Video Size: " + String.valueOf(binding.cv.getSize(CameraSizeFor.SIZE_FOR_VIDEO)) + "\n" +
                "4.Media Type (0:Picture, 1:Video): " + String.valueOf(binding.cv.getMediaType()) + "\n" +
                "5.Zoom: " + String.valueOf(binding.cv.getZoom()) + "\n" +
                "6.MaxZoom: " + String.valueOf(binding.cv.getMaxZoom()) + "\n" +
                "7.CameraFace: " + String.valueOf(binding.cv.getCameraFace()) + "");
    }

}
