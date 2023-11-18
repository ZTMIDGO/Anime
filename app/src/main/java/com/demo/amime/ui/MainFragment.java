package com.demo.amime.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.amime.R;
import com.demo.amime.bean.MyRunnable;
import com.demo.amime.bean.OptionsConfig;
import com.demo.amime.core.style.DCTNet;
import com.demo.amime.core.style.WrapFace;
import com.demo.amime.manager.PathManager;
import com.demo.amime.ui.base.MediaFragment;
import com.demo.amime.utils.FileUtils;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFragment extends MediaFragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    public static MainFragment newInstance() {
        
        Bundle args = new Bundle();
        
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private final BitmapFactory.Options options = new BitmapFactory.Options();
    private final ExecutorService exec = Executors.newCachedThreadPool();
    private final int cropResolution = 720;
    private final int MAX = 4;

    private View mAddView;
    private ProgressBar mProgressView;
    private RadioGroup mRadioGroup;
    private ImageView mImageView;

    private String basePath;
    private String fileName = "style.jpg";

    private Bitmap resultBitmap;
    private MyRunnable runnable;
    private boolean isRunning;

    private DCTNet net;
    private Handler uiHandler;
    private WrapFace wrapFace;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        basePath = PathManager.getCachePath(getActivity());
        net = new DCTNet(getActivity());
        uiHandler = new Handler();
        wrapFace = new WrapFace(getActivity());

        options.inScaled = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destory();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_main;
    }

    @Override
    protected void init(LayoutInflater inflater, View view) {
        super.init(inflater, view);
        mAddView = view.findViewById(R.id.add);
        mRadioGroup = view.findViewById(R.id.radio_group);
        mImageView = view.findViewById(R.id.image);
        mProgressView = view.findViewById(R.id.progressBar);

        mProgressView.setMax(MAX);
    }

    @Override
    protected void initBefore(LayoutInflater inflater, View view) {
        super.initBefore(inflater, view);
        mAddView.setOnClickListener(this);
        mRadioGroup.setOnCheckedChangeListener(this);
        boolean isExists = new File(getFilePath()).exists();
        setEnable(isExists);
        if (isExists) mImageView.setImageBitmap(BitmapFactory.decodeFile(getFilePath()));
    }

    public void destory(){
        try {
            if (runnable != null) runnable.interrupt();
            exec.shutdownNow();
            wrapFace.close();
            net.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private MyRunnable newRunnable(){
        return new MyRunnable() {
            @Override
            public void run() {
                try {
                    Bitmap source = BitmapFactory.decodeFile(getFilePath(), options);
                    int srcWidth = source.getWidth();
                    int srcHeight = source.getHeight();

                    OptionsConfig config = PathManager.getStyleConfig(getActivity(), getSelect(), srcWidth, srcHeight, false);
                    net.init(config.getModelPath() + "/" + config.getModelName(), config.getWidth(), config.getHeight());
                    syncProgress(2);

                    resultBitmap = net.forward(source);
                    resultBitmap = Bitmap.createScaledBitmap(resultBitmap, srcWidth, srcHeight, false);
                    syncProgress(3);

                    source = BitmapFactory.decodeFile(getFilePath(), options);
                    config =  PathManager.getStyleConfig(getActivity(), getSelect(), srcWidth, srcHeight, true);

                    resultBitmap = wrapFace.forward(source, resultBitmap, net, config);
                    net.close();
                }catch (Exception e){
                    resultBitmap = null;
                    e.printStackTrace();
                }finally {
                    syncResult();
                }
            }
        };
    }

    private void syncResult(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                isRunning = false;
                mProgressView.setVisibility(View.GONE);
                setEnable(true);
                if (resultBitmap != null) mImageView.setImageBitmap(resultBitmap);
            }
        });
    }

    private void syncProgress(int progress){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressView.setProgress(progress);
            }
        });
    }

    private String getFilePath(){
        return basePath + "/" + fileName;
    }

    private int getSelect(){
        int type = -1;
        if (mRadioGroup.getCheckedRadioButtonId() == R.id.d_3){
            type = 0;
        }else if (mRadioGroup.getCheckedRadioButtonId() == R.id.anime){
            type = 1;
        }else if (mRadioGroup.getCheckedRadioButtonId() == R.id.artstyle){
            type = 2;
        }else if (mRadioGroup.getCheckedRadioButtonId() == R.id.design){
            type = 3;
        }else if (mRadioGroup.getCheckedRadioButtonId() == R.id.handdrawn){
            type = 4;
        }else if (mRadioGroup.getCheckedRadioButtonId() == R.id.illustration){
            type = 5;
        }else if (mRadioGroup.getCheckedRadioButtonId() == R.id.sketch){
            type = 6;
        }
        return type;
    }

    private void forware(){
        if (isRunning || getSelect() == -1) return;
        isRunning = true;
        mProgressView.setVisibility(View.VISIBLE);

        mProgressView.setProgress(1);
        runnable = newRunnable();
        exec.execute(runnable);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == -1 || isRunning || !new File(getFilePath()).exists()) return;
        setEnable(false);
        forware();
    }

    @Override
    public void onClick(View v) {
        launcherPick();
    }

    @Override
    protected void onPickResult(Uri result) {
        FileUtils.createPath(new File(basePath));
        launcherCrop(result, Uri.fromFile(new File(getFilePath())), null, cropResolution, cropResolution);
    }

    @Override
    protected void onCropResult(ActivityResult result) {
        mRadioGroup.setOnCheckedChangeListener(null);
        mRadioGroup.clearCheck();
        mRadioGroup.setOnCheckedChangeListener(this);
        resultBitmap = null;
        mImageView.setImageBitmap(BitmapFactory.decodeFile(getFilePath()));
        setEnable(true);
    }

    private void setEnable(boolean enable){
        for (int i = 0; i < mRadioGroup.getChildCount(); i++){
            mRadioGroup.getChildAt(i).setEnabled(enable);
        }
    }
}
