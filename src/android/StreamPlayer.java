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
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class StreamPlayer extends CordovaPlugin {
    private static final String ASSETS = "file:///android_asset/";
    private static final String YOUTUBE = "youtube.com";
    private static final String USTREAM = "ustream.tv";

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
        Uri uri = null;

        Intent intent = null;
        // Check to see if someone is trying to play a YouTube page.
        if (url.contains(YOUTUBE)) {
            // If we don't do it this way you don't have the option for youtube
            if (isYouTubeInstalled()) {
                uri = Uri.parse(url);
                uri = Uri.parse("vnd.youtube:" + uri.getQueryParameter("v"));
                intent = new Intent(Intent.ACTION_VIEW, uri);
                cordova.getActivity().startActivity(intent);
            } else {
                new AlertDialog.Builder(this.cordova.getActivity())
                    .setTitle("安裝 Youtube Player")
                    .setMessage("觀看直播,\n需安裝串流影音播放器（Youtube Player）,\n請依指示完成安裝後,\n返回inLiveTW觀看直播.")
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .setNegativeButton("安裝", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = null;
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=com.google.android.youtube"));
                            cordova.getActivity().startActivity(intent);
                        }
                    })
                    .show();
            }
        } else if (url.contains(USTREAM)) {
            if (isUstreamInstalled()) {
                intent = new Intent(Intent.ACTION_VIEW);
                uri = Uri.parse(url.replace("channel","mobile/view/channel"));
                intent.setData(uri);
                intent.setPackage("tv.ustream.ustream");
                cordova.getActivity().startActivity(intent);
            } else {
                new AlertDialog.Builder(this.cordova.getActivity())
                    .setTitle("安裝 Ustream Player")
                    .setMessage("觀看直播,\n需安裝串流影音播放器（Ustream Player）,\n請依指示完成安裝後,\n返回inLiveTW觀看直播.")
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .setNegativeButton("安裝", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = null;
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=tv.ustream.ustream"));
                            cordova.getActivity().startActivity(intent);
                        }
                    })
                    .show();
            }
        } else if (url.contains(ASSETS)) {
            // get file path in assets folder
            String filepath = url.replace(ASSETS, "");
            // get actual filename from path as command to write to internal storage doesn't like folders
            String filename = filepath.substring(filepath.lastIndexOf("/")+1, filepath.length());

            // Don't copy the file if it already exists
            File fp = new File(this.cordova.getActivity().getFilesDir() + "/" + filename);
            if (!fp.exists()) {
                this.copy(filepath, filename);
            }

            intent = new Intent(Intent.ACTION_VIEW);
            uri = Uri.parse("file://" + this.cordova.getActivity().getFilesDir() + "/" + filename);
            intent.setDataAndType(uri, "video/*");
            cordova.getActivity().startActivity(intent);
        } else {
            if (isVLCInstalled()) {
                uri = Uri.parse(url);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("org.videolan.vlc.betav7neon");
                this.cordova.getActivity().startActivity(intent);
            } else {
                new AlertDialog.Builder(this.cordova.getActivity())
                    .setTitle("安裝 VLC Player")
                    .setMessage("觀看直播,\n需安裝串流影音播放器（VLC Player）,\n請依指示完成安裝後,\n返回inLiveTW觀看直播.")
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .setNegativeButton("安裝", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = null;
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=org.videolan.vlc.betav7neon"));
                            cordova.getActivity().startActivity(intent);
                        }
                    })
                    .show();
            }
        }

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
    
    private boolean isUstreamInstalled() {
        PackageManager pm = this.cordova.getActivity().getPackageManager();
        try {
            pm.getPackageInfo("tv.ustream.ustream", PackageManager.GET_ACTIVITIES);
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
