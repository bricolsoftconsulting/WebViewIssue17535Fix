/*
Copyright 2012 Bricolsoft Consulting

   ----------------------------------------------------------------------------

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   ----------------------------------------------------------------------------

   Fix for Android webview issue 17535: parameters and anchors do not 
   work when using HTML files located in the file:///android_asset/ folder. 
   The issue affects all Android versions from Honeycomb onward: 
   http://code.google.com/p/android/issues/detail?id=17535
   
   Unlike other partial fixes, this fix addresses both the parameters and anchors 
   issues. It works transparently with any file:///android_asset/ URLs.
*/

package com.bricolsoftconsulting.webview;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.URLUtil;
import android.webkit.WebView;

import java.util.Map;

public class WebViewEx extends WebView
{
	// Constants
	private static final String ANDROID_ASSET = "file:///android_asset/";
	private static final int SDK_INT_JELLYBEAN = 16;

	// Members
	private String mCacheRootPath;

	// Constructors
	public WebViewEx(Context context)
	{
		super(context);
		init(context);
	}

	public WebViewEx(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public WebViewEx(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	public WebViewEx(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing)
	{
		super(context, attrs, defStyle, privateBrowsing);
		init(context);
	}

	private void init(Context context)
	{
		mCacheRootPath =  getDefaultCacheRootPath(context);
	}

	// Overridden functions
	@Override
	public void loadDataWithBaseURL(String baseUrl, String data,
		String mimeType, String encoding, String historyUrl)
	{
		super.loadDataWithBaseURL(getLoadUrl(baseUrl), data, mimeType, encoding, historyUrl);
	}

	@Override
	public void loadUrl(String url)
	{
		super.loadUrl(getLoadUrl(url));
	}

	@Override
	public void loadUrl(String url, Map<String, String> extraHeaders)
	{
		super.loadUrl(getLoadUrl(url), extraHeaders);
	}

	// Utility functions
	public String getLoadUrl(String url)
	{
		if (isAffectedAssetUrl(url))
		{
			return getCacheUrlFromAssetUrl(url);
		}
		else
		{
			return url;
		}
	}
	
	public String getNonCacheUrl(String url)
	{
		if (isCacheUrl(url))
		{
			return getAssetUrlFromCacheUrl(url);
		}
		else
		{
			return url;
		}
	}
	
	public boolean isCacheUrl(String url)
	{
		return (isAffectedOsVersion() && url != null && url.startsWith(getCacheRootUrl()));
	}

	public static boolean isAffectedAssetUrl(String url)
	{
		return (isAffectedOsVersion() && url != null && URLUtil.isAssetUrl(url));
	}
	
	public static boolean isAffectedOsVersion()
	{
		return (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && android.os.Build.VERSION.SDK_INT < SDK_INT_JELLYBEAN);
	}
	
	public String getCacheUrlFromAssetUrl(String url)
	{
		return url.replaceFirst(ANDROID_ASSET, getCacheRootUrl());
	}
	
	public String getAssetUrlFromCacheUrl(String url)
	{
		return url.replaceFirst(getCacheRootUrl(), ANDROID_ASSET);
	}

	// Accessors
	public String getCacheRootPath()
	{
		return mCacheRootPath;
	}

	public void setCacheRootPath(String cacheRootPath)
	{
		mCacheRootPath = cacheRootPath;
	}

	public String getCacheRootUrl()
	{
		return "file://" + getCacheRootPath();
	}

	public static String getDefaultCacheRootPath(Context context)
	{
		return "/data/data/" + context.getPackageName() + "/webviewfix/";
	}
}
