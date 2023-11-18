package com.demo.amime.ui.base;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public abstract class AbsFragment extends Fragment {

    protected abstract int getLayout();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        init(inflater, view);
        initBefore(inflater, view);
        initBind(inflater, view);
        return view;
    }

    protected void init(LayoutInflater inflater, View view){};

    protected void initBefore(LayoutInflater inflater, View view){}

    protected void initBind(LayoutInflater inflater, View view){};

    protected void setVisible(View view, boolean show){
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public AbsFragment getThis(){
        return this;
    }
}
