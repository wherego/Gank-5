package com.bboylin.gank.Data;

import android.content.Context;

import com.baoyz.treasure.Preferences;
import com.baoyz.treasure.Treasure;
import com.bboylin.gank.Utils.GsonConverterFactory;

/**
 * Created by lin on 2016/11/13.
 */

@Preferences
public interface CommonPref {

    void setWebViewUrl(String webViewUrl);

    String getWebViewUrl();

    void setCategory(String category);

    String getCategory();

    class Factory {
        public static CommonPref create(Context context) {
            Treasure.setConverterFactory(new GsonConverterFactory());
            return Treasure.get(context, CommonPref.class);
        }
    }
}