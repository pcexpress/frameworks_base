/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.quicksettings;

import java.util.List;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsController;

public class InputMethodTile extends QuickSettingsTile {

    private boolean showTile = false;
    private static final String TAG_TRY_SUPPRESSING_IME_SWITCHER = "TrySuppressingImeSwitcher";
    public static QuickSettingsTile mInstance;

    public static QuickSettingsTile getInstance(Context context, LayoutInflater inflater,
            QuickSettingsContainerView container, final QuickSettingsController qsc, Handler handler, String id) {
        mInstance = null;
        mInstance = new InputMethodTile(context, inflater, container, qsc);
        return mInstance;
    }

    public InputMethodTile(Context context, LayoutInflater inflater,
            QuickSettingsContainerView container, QuickSettingsController qsc) {
        super(context, inflater, container, qsc);

        mDrawable = R.drawable.ic_qs_ime;

        mOnClick = new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mQsc.mBar.collapseAllPanels(true);
                    Intent intent = new Intent(Settings.ACTION_SHOW_INPUT_METHOD_PICKER);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
                    pendingIntent.send();
                } catch (Exception e) {}
 if (isEnabled()) {
                    flipTile(0);
                } 
            }
        };

    }

    public void toggleVisibility(boolean show) {
        InputMethodManager imm =
                (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> imis = imm.getInputMethodList();

        showTile = (show && needsToShowImeSwitchOngoingNotification(imm));
        mLabel = getCurrentInputMethodName(mContext, mContext.getContentResolver(),
                imm, imis, mContext.getPackageManager());
        updateQuickSettings();
    }

    private static String getCurrentInputMethodName(Context context, ContentResolver resolver,
            InputMethodManager imm, List<InputMethodInfo> imis, PackageManager pm) {
        if (resolver == null || imis == null) return null;
        final String currentInputMethodId = Settings.Secure.getString(resolver,
                Settings.Secure.DEFAULT_INPUT_METHOD);
        if (TextUtils.isEmpty(currentInputMethodId)) return null;
        for (InputMethodInfo imi : imis) {
            if (currentInputMethodId.equals(imi.getId())) {
                final InputMethodSubtype subtype = imm.getCurrentInputMethodSubtype();
                final CharSequence summary = subtype != null
                        ? subtype.getDisplayName(context, imi.getPackageName(),
                                imi.getServiceInfo().applicationInfo)
                        : context.getString(R.string.quick_settings_ime_label);
                return summary.toString();
            }
        }
        return null;
    }

    private boolean needsToShowImeSwitchOngoingNotification(InputMethodManager imm) {
        List<InputMethodInfo> imis = imm.getEnabledInputMethodList();
        final int N = imis.size();
        if (N > 2) return true;
        if (N < 1) return false;
        int nonAuxCount = 0;
        int auxCount = 0;
        InputMethodSubtype nonAuxSubtype = null;
        InputMethodSubtype auxSubtype = null;
        for(int i = 0; i < N; ++i) {
            final InputMethodInfo imi = imis.get(i);
            final List<InputMethodSubtype> subtypes = imm.getEnabledInputMethodSubtypeList(imi,
                    true);
            final int subtypeCount = subtypes.size();
            if (subtypeCount == 0) {
                ++nonAuxCount;
            } else {
                for (int j = 0; j < subtypeCount; ++j) {
                    final InputMethodSubtype subtype = subtypes.get(j);
                    if (!subtype.isAuxiliary()) {
                        ++nonAuxCount;
                        nonAuxSubtype = subtype;
                    } else {
                        ++auxCount;
                        auxSubtype = subtype;
                    }
                }
            }
        }
        if (nonAuxCount > 1 || auxCount > 1) {
            return true;
        } else if (nonAuxCount == 1 && auxCount == 1) {
            if (nonAuxSubtype != null && auxSubtype != null
                    && (nonAuxSubtype.getLocale().equals(auxSubtype.getLocale())
                            || auxSubtype.overridesImplicitlyEnabledSubtype()
                            || nonAuxSubtype.overridesImplicitlyEnabledSubtype())
                    && nonAuxSubtype.containsExtraValueKey(TAG_TRY_SUPPRESSING_IME_SWITCHER)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    void updateQuickSettings() {
        TextView tv = (TextView) mTile.findViewById(R.id.tile_textview);
        tv.setText(mLabel);
        tv.setTextSize(1, mTileTextSize);
        if (mTileTextColor != -2) {
            tv.setTextColor(mTileTextColor);
        }
        tv.setCompoundDrawablesWithIntrinsicBounds(0, mDrawable, 0, 0);
        mTile.setVisibility(showTile ? View.VISIBLE : View.GONE);
        super.updateQuickSettings();
    }

}
