package com.example.sensys.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.sensys.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public final class AppChrome {
    private AppChrome() {
    }

    public static void applyEdgeToEdge(Activity activity, View topInsetView, @Nullable View bottomInsetView) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        boolean isDarkMode = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!isDarkMode);
        controller.setAppearanceLightNavigationBars(!isDarkMode);

        applyInsetMargin(topInsetView, true, false, true);
        if (bottomInsetView != null) {
            applyInsetPadding(bottomInsetView, false, true);
        }
    }

    public static void bindToolbar(
            MaterialToolbar toolbar,
            @StringRes int titleRes,
            boolean showBackButton,
            @Nullable Runnable onBackPressed
    ) {
        toolbar.setTitle(titleRes);
        if (showBackButton) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
            toolbar.setNavigationOnClickListener(v -> {
                if (onBackPressed != null) {
                    onBackPressed.run();
                }
            });
        } else {
            toolbar.setNavigationIcon(null);
            toolbar.setNavigationOnClickListener(null);
        }
    }

    public static void bindThemeSwitch(Activity activity, SwitchMaterial themeModeSwitch) {
        boolean isDarkMode = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        themeModeSwitch.setChecked(isDarkMode);
        
        themeModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private static void applyInsetMargin(View view, boolean includeTopInset, boolean includeBottomInset, boolean addExtraTopSpacing) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        final int initialTop = lp.topMargin;
        final int initialBottom = lp.bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(view, (target, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) target.getLayoutParams();
            
            if (includeTopInset) {
                float density = view.getContext().getResources().getDisplayMetrics().density;
                int extraTop = addExtraTopSpacing ? (int) (12 * density) : 0;
                layoutParams.topMargin = initialTop + systemBars.top + extraTop;
            }
            if (includeBottomInset) {
                layoutParams.bottomMargin = initialBottom + systemBars.bottom;
            }
            target.setLayoutParams(layoutParams);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    private static void applyInsetPadding(View view, boolean includeTopInset, boolean includeBottomInset) {
        final int initialLeft = view.getPaddingLeft();
        final int initialTop = view.getPaddingTop();
        final int initialRight = view.getPaddingRight();
        final int initialBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (target, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int topPadding = initialTop + (includeTopInset ? systemBars.top : 0);
            int bottomPadding = initialBottom + (includeBottomInset ? systemBars.bottom : 0);
            target.setPadding(initialLeft, topPadding, initialRight, bottomPadding);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}
