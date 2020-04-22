package com.example.wifiinfo;

import java.io.*;
import java.lang.Object;
import java.lang.*;
import java.util.*;

import android.content.*;
import android.content.res.AssetManager;


public class QuoteBank {

    private Context mContext;

    public QuoteBank(Context context) {
        this.mContext = context;
    }

    public String readLine(String path) {
        String mLines = "";

        AssetManager am = mContext.getAssets();

        try {
            InputStream is = am.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null)
                mLines += line;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mLines;
    }
}
