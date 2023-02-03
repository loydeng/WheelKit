package com.loy.wheelkit;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.loy.kit.jni.NativeLib;
import com.loy.kit.log.SdkLog;
import com.loy.wheelkit.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private NativeLib nativeLib;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        nativeLib = new NativeLib(new NativeLib.Callback() {
            @Override
            public void onStop() {
                binding.buttonPlay.setText(nativeLib.isPlaying() ? "pause_play" : "Play");
            }

            @Override
            public void onProgress(long current, long total) {
                SdkLog.color(String.format("current:%d, total:%d", current, total));
            }
        });
        binding = FragmentMainBinding.inflate(inflater, container, false);
        AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        String sampleRate = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        String framesPerBuffer = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        SdkLog.color("sampleRate:" + sampleRate);
        SdkLog.color("framesPerBuffer:" + framesPerBuffer);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonFirst.setOnClickListener(view1 -> NavHostFragment.findNavController(MainFragment.this)
                .navigate(R.id.action_MainFragment_to_FirstFragment));
        binding.buttonSecond.setOnClickListener(view12 -> NavHostFragment.findNavController(MainFragment.this)
                .navigate(R.id.action_MainFragment_to_SecondFragment));

        binding.buttonPlay.setOnClickListener(v -> {
            nativeLib.play();
            binding.buttonPlay.setText(nativeLib.isPlaying() ? "pause_play" : "Play");
        });

        binding.buttonStop.setOnClickListener(v -> {
            nativeLib.stop();
        });

        binding.buttonRecord.setOnClickListener(v -> {
            nativeLib.record();
            binding.buttonRecord.setText(nativeLib.isRecording() ? "pause_record" : "Record");
        });

        binding.buttonStopRecord.setOnClickListener(v -> {
            nativeLib.stopRecord();
            binding.buttonRecord.setText(nativeLib.isRecording() ? "pause_record" : "Record");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}