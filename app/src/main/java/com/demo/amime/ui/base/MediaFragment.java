package com.demo.amime.ui.base;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.demo.amime.handler.PermissionHandler;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import java.util.Map;

/**
 * Created by ZTMIDGO 2023/9/1
 */
public abstract class MediaFragment extends AbsFragment {
    private ActivityResultLauncher baseLauncher;
    private ActivityResultLauncher pickLauncher;
    private ActivityResultLauncher ucropLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLauncher();
    }

    private void initLauncher(){
        baseLauncher = PermissionHandler.createPermissionsWithArray(this, new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                for (Boolean success : result.values()){
                    if (!success){
                        Toast.makeText(getActivity(), "ERROR", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                launcherPick();
            }
        });

        pickLauncher = PermissionHandler.createPermissionsWithDocument(this, new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) onPickResult(result);
            }
        });

        ucropLauncher = PermissionHandler.createPermissionsWithIntent(this, new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result != null && result.getData() != null && result.getResultCode() == Activity.RESULT_OK){
                    onCropResult(result);
                }
            }
        });
    }

    protected void launcherPick(){
        String[] array = getPermissions();
        if (!PermissionHandler.checkPermissionAllGranted(getActivity(), array)){
            baseLauncher.launch(array);
        }else {
            pickLauncher.launch(new String[]{"*/*"});
        }
    }

    protected void launcherCrop(Uri source, Uri destination, AspectRatio[] ratios, int width, int height){
        UCrop.Options options = new UCrop.Options();
        if (ratios != null) options.setAspectRatioOptions(0, ratios);

        UCrop uCrop = UCrop.of(source, destination).withOptions(options).withMaxResultSize(width, height);
        ucropLauncher.launch(uCrop.getIntent(getActivity()));
    }

    protected String[] getPermissions(){
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 ? PermissionHandler.PERMISSIONS : PermissionHandler.PERMISSIONS_API33;
    }

    protected void onPickResult(Uri result){};
    protected void onCropResult(ActivityResult result){};

    protected void onCameraLauncher(){};
}
