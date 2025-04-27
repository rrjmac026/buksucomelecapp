package com.example.appdevfinal.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;

public class SettingsFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private static final String THEME_PREF = "theme_preference";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        Switch themeSwitch = view.findViewById(R.id.switchTheme);
        
        // Set initial switch state
        boolean isDarkMode = sharedPreferences.getBoolean(THEME_PREF, false);
        themeSwitch.setChecked(isDarkMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveThemePreference(isChecked);
            updateTheme(isChecked);
        });

        return view;
    }

    private void saveThemePreference(boolean isDarkMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(THEME_PREF, isDarkMode);
        editor.apply();
    }

    private void updateTheme(boolean isDarkMode) {
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
