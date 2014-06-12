#StreamPlayer plugin for Android Cordova 3.0 

The Stream player allows you to display stream from your Android Cordova application. Based in 
VLC player
https://play.google.com/store/apps/details?id=org.videolan.vlc.betav7neon&hl=zh-TW

This command fires an Intent to have your devices VLC player show the stream.

#Adding the Plugin to your project

cordova plugin add https://github.com/yutin1987/StreamPlayer.git

#Removing the Plugin to your project

cordova plugin rm com.phonegap.plugins.streamplayer

#Using the plugin

cordova.plugins.streamplayer.play("http://path.to.my/video.mp4");
