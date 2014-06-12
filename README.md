#StreamPlayer plugin for Android Cordova 3.0 

The Stream player allows you to display stream from your Android Cordova application. Based in 


This command fires an Intent to have your devices video player show the video.

#Adding the Plugin to your project

cordova plugin add https://github.com/raulduran/VideoPlayer.git

#Removing the Plugin to your project

cordova plugin rm com.phonegap.plugins.videoplayer

#Using the plugin

cordova.plugins.videoPlayer.play("http://path.to.my/video.mp4");
cordova.plugins.videoPlayer.play("file:///path/to/my/video.mp4");
cordova.plugins.videoPlayer.play("file:///android_asset/www/path/to/my/video.mp4");
cordova.plugins.videoPlayer.play("https://www.youtube.com/watch?v=en_sVVjWFKk");
