/*
 * Copyright (C) 2023-2026 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.settings.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.launcher3.R;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

public class ColorPreference extends Preference {

    private static final int DEFAULT_COLOR = 0xFF000000;

    private int mColor = DEFAULT_COLOR;
    private View mPreviewView;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.color_preference_widget);
    }

    public ColorPreference(Context context) {
        this(context, null);
    }

    public void setValue(int color) {
        mColor = color;
        updatePreview(mPreviewView, mColor);
    }

    public int getValue() {
        return mColor;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mPreviewView = holder.findViewById(R.id.color_preview);
        updatePreview(mPreviewView, mColor);
    }

    private void updatePreview(View view, int color) {
        if (view != null) {
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(color);
            shape.setStroke(2, Color.GRAY);
            view.setBackground(shape);
        }
    }

    @Override
    protected void onClick() {
        showColorPickerDialog();
    }

    private void showColorPickerDialog() {
        Context context = getContext();
        Context dialogContext = context;

        int themeId = context.getResources().getIdentifier(
                "Theme.Material3.DynamicColors.DayNight", "style", context.getPackageName());
        if (themeId == 0) {
            themeId = context.getResources().getIdentifier(
                    "Theme.MaterialComponents.DayNight", "style", context.getPackageName());
        }
        if (themeId == 0) {
            themeId = context.getResources().getIdentifier(
                    "Theme.AppCompat.DayNight", "style", context.getPackageName());
        }

        if (themeId != 0) {
            dialogContext = new android.view.ContextThemeWrapper(context, themeId);
        }

        View root = LayoutInflater.from(dialogContext).inflate(R.layout.color_picker_dialog, null);

        final View preview = root.findViewById(R.id.color_picker_preview);
        final TabLayout tabs = root.findViewById(R.id.color_picker_tabs);
        final LinearLayout hsbContainer = root.findViewById(R.id.hsb_container);
        final LinearLayout rgbContainer = root.findViewById(R.id.rgb_container);

        final Slider sliderHue = root.findViewById(R.id.slider_hue);
        final Slider sliderSaturation = root.findViewById(R.id.slider_saturation);
        final Slider sliderBrightness = root.findViewById(R.id.slider_brightness);

        final Slider sliderRed = root.findViewById(R.id.slider_red);
        final Slider sliderGreen = root.findViewById(R.id.slider_green);
        final Slider sliderBlue = root.findViewById(R.id.slider_blue);

        final EditText editHex = root.findViewById(R.id.edit_hex);

        final int[] currentColor = {mColor};
        final float[] hsv = new float[3];
        Color.colorToHSV(mColor, hsv);

        Runnable updateUI = () -> {
            updatePreview(preview, currentColor[0]);
            editHex.setText(String.format("#%08X", currentColor[0]));

            sliderHue.setValue(hsv[0]);
            sliderSaturation.setValue(hsv[1] * 100);
            sliderBrightness.setValue(hsv[2] * 100);

            sliderRed.setValue(Color.red(currentColor[0]));
            sliderGreen.setValue(Color.green(currentColor[0]));
            sliderBlue.setValue(Color.blue(currentColor[0]));
        };

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    hsbContainer.setVisibility(View.VISIBLE);
                    rgbContainer.setVisibility(View.GONE);
                } else {
                    hsbContainer.setVisibility(View.GONE);
                    rgbContainer.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        Slider.OnChangeListener hsbListener = (slider, value, fromUser) -> {
            if (fromUser) {
                hsv[0] = sliderHue.getValue();
                hsv[1] = sliderSaturation.getValue() / 100f;
                hsv[2] = sliderBrightness.getValue() / 100f;
                int alpha = Color.alpha(currentColor[0]);
                currentColor[0] = Color.HSVToColor(alpha, hsv);

                updatePreview(preview, currentColor[0]);
                editHex.setText(String.format("#%08X", currentColor[0]));
                sliderRed.setValue(Color.red(currentColor[0]));
                sliderGreen.setValue(Color.green(currentColor[0]));
                sliderBlue.setValue(Color.blue(currentColor[0]));
            }
        };
        sliderHue.addOnChangeListener(hsbListener);
        sliderSaturation.addOnChangeListener(hsbListener);
        sliderBrightness.addOnChangeListener(hsbListener);

        Slider.OnChangeListener rgbListener = (slider, value, fromUser) -> {
            if (fromUser) {
                int r = (int) sliderRed.getValue();
                int g = (int) sliderGreen.getValue();
                int b = (int) sliderBlue.getValue();
                int alpha = Color.alpha(currentColor[0]);
                currentColor[0] = Color.argb(alpha, r, g, b);

                updatePreview(preview, currentColor[0]);
                editHex.setText(String.format("#%08X", currentColor[0]));
                Color.colorToHSV(currentColor[0], hsv);
                sliderHue.setValue(hsv[0]);
                sliderSaturation.setValue(hsv[1] * 100);
                sliderBrightness.setValue(hsv[2] * 100);
            }
        };
        sliderRed.addOnChangeListener(rgbListener);
        sliderGreen.addOnChangeListener(rgbListener);
        sliderBlue.addOnChangeListener(rgbListener);

        editHex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int color = Color.parseColor(s.toString());
                    if (color != currentColor[0]) {
                        currentColor[0] = color;
                        updatePreview(preview, currentColor[0]);
                        // update sliders without triggering infinite loop
                        Color.colorToHSV(currentColor[0], hsv);
                        sliderHue.setValue(hsv[0]);
                        sliderSaturation.setValue(hsv[1] * 100);
                        sliderBrightness.setValue(hsv[2] * 100);
                        sliderRed.setValue(Color.red(currentColor[0]));
                        sliderGreen.setValue(Color.green(currentColor[0]));
                        sliderBlue.setValue(Color.blue(currentColor[0]));
                    }
                } catch (Exception e) {}
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        updateUI.run();

        boolean isNightMode = (dialogContext.getResources().getConfiguration().uiMode 
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        int defaultPopupBg = isNightMode ? 0xFF2B2930 : 0xFFEAEAF5;
        int defaultTabBg = isNightMode ? 0xFF141218 : 0xFFFFFFFF;

        int popupBgColor = getThemeColor(dialogContext, "colorSurfaceContainerHigh", 0);
        if (popupBgColor == 0) {
            popupBgColor = getThemeColor(dialogContext, "colorSurfaceContainer", 0);
        }
        if (popupBgColor == 0) {
            popupBgColor = getThemeColor(dialogContext, "colorSurface", 0);
        }
        if (popupBgColor == 0) {
            popupBgColor = getThemeColor(dialogContext, "colorBackground", defaultPopupBg);
        }

        int tabBgColor = getThemeColor(dialogContext, "colorSurface", 0);
        if (tabBgColor == 0) {
            tabBgColor = getThemeColor(dialogContext, "colorBackground", defaultTabBg);
        }

        // Apply background to HSB/RGB tab bar
        GradientDrawable tabBgDrawable = new GradientDrawable();
        tabBgDrawable.setColor(popupBgColor);
        tabBgDrawable.setCornerRadius(dpToPx(dialogContext, 28));
        tabs.setBackground(tabBgDrawable);

        // Add padding to tab bar for inset aesthetic
        int paddingPx = dpToPx(dialogContext, 4);
        tabs.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        AlertDialog dialog = new AlertDialog.Builder(dialogContext)
            .setTitle(getTitle())
            .setView(root)
            .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> {
                mColor = currentColor[0];
                persistInt(mColor);
                if (getOnPreferenceChangeListener() != null) {
                    getOnPreferenceChangeListener().onPreferenceChange(this, mColor);
                }
                updatePreview(mPreviewView, mColor);
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.show();

        // Apply background to main popup
        GradientDrawable dialogBgDrawable = new GradientDrawable();
        dialogBgDrawable.setColor(tabBgColor);
        dialogBgDrawable.setCornerRadius(dpToPx(dialogContext, 28));

        int marginPx = dpToPx(dialogContext, 24);
        android.graphics.drawable.InsetDrawable insetDrawable = 
                new android.graphics.drawable.InsetDrawable(dialogBgDrawable, marginPx, marginPx, marginPx, marginPx);
        dialog.getWindow().setBackgroundDrawable(insetDrawable);
    }

    private int getThemeColor(Context context, String attrName, int defaultColor) {
        int attrId = context.getResources().getIdentifier(attrName, "attr", context.getPackageName());
        if (attrId == 0) {
            attrId = context.getResources().getIdentifier(attrName, "attr", "android");
        }
        if (attrId != 0) {
            TypedValue typedValue = new TypedValue();
            if (context.getTheme().resolveAttribute(attrId, typedValue, true)) {
                if (typedValue.resourceId != 0) {
                    try {
                        return context.getColor(typedValue.resourceId);
                    } catch (Exception e) {
                    }
                }
                return typedValue.data;
            }
        }
        return defaultColor;
    }

    private int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Support both integer and string color literals in XML defaultValue.
        try {
            return a.getColor(index, DEFAULT_COLOR);
        } catch (Exception ignored) {
        }

        TypedValue value = a.peekValue(index);
        if (value != null && value.type == TypedValue.TYPE_STRING && value.string != null) {
            try {
                return Color.parseColor(value.string.toString());
            } catch (IllegalArgumentException ignored) {
            }
        }

        try {
            return a.getInt(index, DEFAULT_COLOR);
        } catch (Exception ignored) {
        }

        return DEFAULT_COLOR;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        mColor = getPersistedInt(defaultValue instanceof Integer ? (Integer) defaultValue : DEFAULT_COLOR);
    }
}
