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

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;


public class HaloTile extends QuickSettingsTile{

public int mHaloEnabled;


    public static QuickSettingsTile mInstance;

    public static QuickSettingsTile getInstance(Context context, LayoutInflater inflater,
            QuickSettingsContainerView container, final QuickSettingsController qsc, Handler handler, String id) {
        mInstance = null;
        mInstance = new HaloTile(context, inflater, container, qsc, handler);
        return mInstance;
	
    }

    private Context mContext;

    public HaloTile(Context context, LayoutInflater inflater,
            QuickSettingsContainerView container,
            QuickSettingsController qsc, Handler handler) {
        super(context, inflater, container, qsc);
 
	mContext = context;
	mHaloEnabled = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.HALO_ENABLED);

        mDrawable = R.drawable.ic_qs_alarm_on;

        mOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
		
		updateHalo();		
                
 if (isEnabled()) {
                    flipTile(0);
                } 
            }
        };
       
    }

  public void updateHalo() {

	if (mHaloEnabled == 0) mHaloEnabled =1;
		else mHaloEnabled = 0;
		
		Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HALO_ENABLED, mHaloEnabled);
	}
}
