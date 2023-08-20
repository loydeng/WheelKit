package com.loy.wheelkit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.loy.wheelkit.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        // 方式1
        // binding.camera1.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_CameraFragment_to_Camera1Fragment));
        // 方式2
        binding.camera1.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_CameraFragment_to_Camera1Fragment));
        binding.camera2.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_CameraFragment_to_Camera2Fragment));
        binding.camerax.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_CameraFragment_to_CameraxFragment));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}