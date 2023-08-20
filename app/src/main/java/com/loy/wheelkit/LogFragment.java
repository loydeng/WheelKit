package com.loy.wheelkit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.loy.kit.log.SdkLog;
import com.loy.kit.log.core.LoggerManager;
import com.loy.kit.log.core.ViewPrinterProvider;
import com.loy.wheelkit.databinding.FragmentLogBinding;

public class LogFragment extends Fragment {

    private FragmentLogBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
                            ) {

        binding = FragmentLogBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.debugLogBtn.setOnClickListener((v)-> SdkLog.e("debug e log"));

        binding.viewLogBtn.setOnClickListener((v) -> {
            ViewPrinterProvider viewPrinterProvider = LoggerManager.getViewLogger().getViewPrinterProvider();
            if (viewPrinterProvider != null) {
                viewPrinterProvider.showFloatView();
            }
            LoggerManager.getViewLogger().d("view d log");
            LoggerManager.getViewLogger().i("view i log");
            LoggerManager.getViewLogger().w("view w log");
            LoggerManager.getViewLogger().e("view e log");
        });

        binding.fileLogBtn.setOnClickListener((v) -> {
            LoggerManager.getFileLogger().e("file test log");
        });
        binding.crashLogBtn.setOnClickListener((v) -> {
            throw new IllegalStateException("test crash log");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}