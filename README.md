WebViewIssue17535Fix
====================

What is this project?
---------------------
This project is a workaround for Android issue 17535: http://code.google.com/p/android/issues/detail?id=17535

Issue 17535 affects the webview on Android 3.0+ and Android 4.0. Hybrid Android apps typically load HTML files from the assets folder into the webview via special android_asset URLs. On affected Android OS versions, if the android_asset URLs contain parameters (e.g. `file:///android_asset/mypage.html?test=test`) or anchors (e.g. `file:///android_asset/mypage.html#test`), the webview incorrectly displays a page not found error -- even though the file exists.

This workaround fixes the problem completely and allows you to use both parameters and anchors in android_asset URLs. The project is provided as a JAR, so you can easily include it in your app.

How does it work?
-----------------

This workaround extends the `WebView` and `WebViewClient` classes to create an assets cache in your app's data directory and instruct the webview to load pages from this cache. Whenever a new asset request is processed in the webview, the following things happen:

1. The asset URL is translated into a corresponding cache URL. For example, `file:///android_asset/mypage.html` becomes `file:///data/data/com.yourcompany.yourpackage/webviewfix/mypage.html`.
2. The asset file is copied from the assets directory to the cache directory.
3. The webview gets redirected to the new file in the cache.

Because the webview now loads files from the cache directory instead of the assets directory, the assets issues are effectively side-stepped.

Installation
------------
To include this webview fix in your apps, follow these steps:

1. Download a copy of the JAR from the following URL: https://github.com/bricolsoftconsulting/WebViewIssue17535Fix/raw/master/bin/webviewissue17535fix.jar
2. Add the JAR to your project.

   How you do this will depend on the specific tools you use. For example, in Eclipse you can right click on your project in `Package Explorer`, select `Properties` from the context menu, select `Java Build Path` on the left hand side of the Properties dialog, click the `Add External JAR` button in the dialog and finally browse to and select the `WebViewIssue17535Fix.jar` file.
   
3. Locate all `WebView` references in your XML layouts and change them to `com.bricolsoftconsulting.webview.WebViewEx`.
4. When initializing each `WebViewEx` object in your code, always provide a reference to a `WebViewClientEx` object:

    ```java
    mWebView = (WebViewEx) findViewById(R.id.webview);
    mWebView.setWebViewClient(new WebViewClientEx(WebViewActivity.this));
    ```

   If you want to override the `shouldOverrideUrlLoading` and `shouldInterceptRequest` events you need to use `shouldOverrideUrlLoadingEx` and `shouldInterceptRequestEx` instead. Sample code is posted below:

    ```java
    mWebView = (WebViewEx) findViewById(R.id.webview);
    mWebView.setWebViewClient(new WebViewClientEx(WebViewActivity.this)
    {
        @Override
        public boolean shouldOverrideUrlLoadingEx(WebView view, String url)
        {
            // Override shouldOverrideUrlLoadingEx instead of shouldOverrideUrlLoading
            
            // Optional, if you need the original non-cache url
            if (view instanceof WebViewEx)
            {
                url = ((WebViewEx)view).getNonCacheUrl(url);
            }

            // Do your own url replacements here
            if (...)
            {
                ...
                return true;
            }

            return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequestEx(WebView view, String url)
        {
            // Override shouldInterceptRequestEx instead of shouldInterceptRequest

            // Optional, if you need the original non-cache url
            if (view instanceof WebViewEx)
            {
                url = ((WebViewEx)view).getNonCacheUrl(url);
            }

            // Do your own resource replacements here
            if (...)
            {
                wrr = new WebResourceResponse...
            }
				
            return wrr;
        }
    });
    ```
    
    Any event type function in WebViewEx, WebViewClientEx or WebChromeClient that contains an `url` parameter will now return a cache URL for affected asset URLs. If you need to get the original non-cache URL you can use the code below:
    
    ```java
    if (view instanceof WebViewEx)
    {
        url = ((WebViewEx)view).getNonCacheUrl(url);
    }
    ```

5. Make sure that your Android manifest declares a target SDK version of at least 11:

    ```java
    <uses-sdk android:targetSdkVersion="11" />
    ```
    
   If using Eclipse, also make sure that you change the Android target in the project properties.

6. Add the appropriate webview permissions to your manifest, as shown below:

    ```java
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    ```

   Optionally, if you want to set the cache path to the SDCARD:
   
    ```java
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    ```

Copyright
---------
Copyright 2012 Bricolsoft Consulting

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.