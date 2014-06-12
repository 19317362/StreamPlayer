/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2011, IBM Corporation
 */

package com.plugin.streamplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class StreamPlayer extends CordovaPlugin {
    private static final String YOU_TUBE = "youtube.com";
    private static final String ASSETS = "file:///android_asset/";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {

        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        try {
            if (action.equals("playStream")) {
                playStream(args.getString(0));
            }
            else {
                status = PluginResult.Status.INVALID_ACTION;
            }
            callbackContext.sendPluginResult(new PluginResult(status, result));
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
        } catch (IOException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION));
        }
        return true;
    }

    private void playStream(String url) throws IOException {

    	if (url.contains("bit.ly/") || url.contains("goo.gl/") || url.contains("tinyurl.com/") || url.contains("youtu.be/")) {
			//support for google / bitly / tinyurl / youtube shortens
			URLConnection con = new URL(url).openConnection();
			con.connect();
			InputStream is = con.getInputStream();
			//new redirected url
	        url = con.getURL().toString();
			is.close();
		}
        
        // Create URI
        Uri uri = Uri.parse(url);

        Intent intent = null;
        // Check to see if someone is trying to play a YouTube page.
        if (url.contains(YOU_TUBE)) {
            // If we don't do it this way you don't have the option for youtube
            uri = Uri.parse("vnd.youtube:" + uri.getQueryParameter("v"));
            if (isYouTubeInstalled()) {
                intent = new Intent(Intent.ACTION_VIEW, uri);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.google.android.youtube"));
            }
        } else if(url.contains(ASSETS)) {
            // get file path in assets folder
            String filepath = url.replace(ASSETS, "");
            // get actual filename from path as command to write to internal storage doesn't like folders
            String filename = filepath.substring(filepath.lastIndexOf("/")+1, filepath.length());

            // Don't copy the file if it already exists
            File fp = new File(this.cordova.getActivity().getFilesDir() + "/" + filename);
            if (!fp.exists()) {
                this.copy(filepath, filename);
            }

            // change uri to be to the new file in internal storage
            uri = Uri.parse("file://" + this.cordova.getActivity().getFilesDir() + "/" + filename);

            // Display stream player
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
        } else {
            if (isVLCInstalled()) {
                intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("org.videolan.vlc.betav7neon");
            } else {
                Builder intAlertDialog = new AlertDialog.Builder(this);
                intAlertDialog.setTitle("安裝 VLC Player");
                intAlertDialog.setMessage("播放串流影音需安裝VLC Player, \n請依指示完成安裝後, 返回inLiveTW收看直播");
                DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=org.videolan.vlc.betav7neon"));
                    }
                };
                intAlertDialog.setNeutralButton("安裝", OkClick);
                intAlertDialog.show();
            }
        }

        this.cordova.getActivity().startActivity(intent);
    }

    private void copy(String fileFrom, String fileTo) throws IOException {
        // get file to be copied from assets
        InputStream in = this.cordova.getActivity().getAssets().open(fileFrom);
        // get file where copied too, in internal storage.
        // must be MODE_WORLD_READABLE or Android can't play it
        FileOutputStream out = this.cordova.getActivity().openFileOutput(fileTo, Context.MODE_WORLD_READABLE);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }
    
    private boolean isVLCInstalled() {
        PackageManager pm = this.cordova.getActivity().getPackageManager();
        try {
            pm.getPackageInfo("org.videolan.vlc.betav7neon", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }   
    }
    private boolean isYouTubeInstalled() {
        PackageManager pm = this.cordova.getActivity().getPackageManager();
        try {
            pm.getPackageInfo("com.google.android.youtube", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
