package com.loy.kit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Loy
 * @time 2022/1/10 17:27
 * @des 使用该类需要配置 provider , 参考 manifest 文件. 导入"@xml/file_paths" 文件
 */
public class CameraUseFragment extends Fragment {

    public static final String TAG = "CameraFragment";

    private CameraUseUtil.Type mType;

    private Uri fileOutputUri;
    private String path;

    private CameraUseUtil.Callback mCallback;

    public CameraUseFragment(String path, CameraUseUtil.Type type, CameraUseUtil.Callback callback) {
        this.path = path;
        this.mType = type;
        this.mCallback = callback;
        Objects.requireNonNull(path);
        Objects.requireNonNull(callback);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mType == CameraUseUtil.Type.Image) {
            captureAction();
        } else if (mType == CameraUseUtil.Type.Video) {
            recordAction();
        }
        Log.e(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    private void captureAction() {
        Intent imageCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imageCapture.resolveActivity(getActivity().getPackageManager()) != null) {
            if (!initUri()){
                return;
            }
            imageCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileOutputUri);
            //imageCapture.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, 0);
            startActivityForResult(imageCapture, CameraUseUtil.Type.Image.ordinal());
        } else { // 没有可响应拍照的应用
            mCallback.onError(CameraUseUtil.Error.NoTargetApp);
        }
    }

    private boolean initUri() {
        //File imageFile = CameraHelper.createImageFile();
        File imageFile = new File(path);
        if (!imageFile.getParentFile().exists()) {
            if (!imageFile.getParentFile().mkdirs()) {
                mCallback.onError(CameraUseUtil.Error.PathErrorOrNoPermission);
                return false;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 确保注册组件 FileProvider 的授权字符串与 getUriForFile(Context, String, File) 的第二个参数匹配。
            fileOutputUri = FileProvider.getUriForFile(getActivity(),
                                                       "com.loy.wheelkit.android.fileprovider",
                                                       imageFile);
        } else {
            fileOutputUri = Uri.fromFile(imageFile);
        }
        return true;
    }

    private void recordAction() {
        Intent videoCapture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoCapture.resolveActivity(getActivity().getPackageManager()) != null) {
            if (!initUri()){
                return;
            }
            //videoCapture.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            videoCapture.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1); //视频的质量[0,1] ,越大质量越好, 同时文件也越大
            videoCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileOutputUri); //录制路径，不写, 默认回调返回
            //videoCapture.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);  //限定录制时长
            startActivityForResult(videoCapture, CameraUseUtil.Type.Video.ordinal());
        } else { // 没有可响应录像的应用
            mCallback.onError(CameraUseUtil.Error.NoTargetApp);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) { // 成功返回结果
            mCallback.onSuccess(fileOutputUri);
            /*if (requestCode == Type.Image.ordinal()) { // 请求拍照返回
                Bitmap thumbnail = null;
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    thumbnail = (Bitmap) bundle.get("data"); // 获取缩略图, 未指定path时返回
                }
            } else if (requestCode == Type.Video.ordinal()) { // 请求录像返回
                Uri videoUri = data.getData(); // 录制视频的uri
            }*/
        } else if (resultCode == Activity.RESULT_CANCELED) { // 主动取消结果
            mCallback.onError(CameraUseUtil.Error.Cancel);
        }
    }

    public Bitmap getImage() {
        Bitmap image = null;
        InputStream inputStream = null;
        try {
            inputStream = getActivity().getContentResolver().openInputStream(fileOutputUri);
            image = BitmapFactory.decodeStream(inputStream); // 图片
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return image;
    }

    // 将照片添加到图库
    // 通过 Intent 制作照片时，您应该知道图片所在的位置，因为您一开始就指定了保存该图片的位置。
    // 对于所有其他人而言，为了让您的照片可供访问，最简单的方式可能是让其可从系统的媒体提供商访问。
    // 注意：如果您将照片保存到 getExternalFilesDir() 提供的目录中，媒体扫描器将无法访问相应的文件，
    // 因为这些文件对您的应用保持私密状态。
    //
    // 下面的示例方法演示了如何调用系统的媒体扫描器以将您的照片添加到媒体提供商的数据库中，
    // 使 Android 图库应用中显示这些照片并使它们可供其他应用使用。
    private void galleryAddPic(String currentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);

        /*try {
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), currentPhotoPath, f.getName(), f.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */
    }
}
