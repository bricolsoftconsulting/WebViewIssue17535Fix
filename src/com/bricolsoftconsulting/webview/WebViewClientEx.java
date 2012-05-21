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
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class WebViewClientEx extends WebViewClient
{	
	// Members
	private Context mContext;
	private boolean mDebug;

	// Constructors
	public WebViewClientEx(Context context)
	{
		init(context, false);
	}
	
	public WebViewClientEx(Context context, boolean debug)
	{
		init(context, debug);
	}
    
	private void init(Context context, boolean debug)
	{
		mContext = context;
		mDebug = debug;
	}

	// Overriden functions
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{		
		// Handle android:///asset URLs
		if ((view instanceof WebViewEx) && ((WebViewEx)(view)).isAffectedUrl(url))
		{
			view.loadUrl(url);
			return true;
		}

		return false;
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url)
	{
		if (view instanceof WebViewEx)
		{
			String cacheRootUrl = ((WebViewEx)view).getCacheRootUrl();
			if (needsCacheCopy(url, cacheRootUrl))
			{
				String sourceFileName = getSourceFileNameFromCacheUrl(url, cacheRootUrl);
				String destFileName = getDestFileNameFromCacheUrl(url); 
				copyFile(sourceFileName, destFileName);
			}
		}

		return null;
	}

	// Utility functions
	private boolean needsCacheCopy(String url, String cacheRootUrl)
	{
		return (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && url.startsWith(cacheRootUrl)  && ((mDebug == false)?(getFileSize(getPathFromUrl(url)) == 0):true));
	}
	
	public long getFileSize(String filePath)
	{
		File file = new File(filePath);
		return file.length(); // Returns 0 if file does not exist
    }
	
	private String getSourceFileNameFromCacheUrl(String url, String cacheRootUrl)
	{
		
		String sourceUrl = url.replaceFirst(cacheRootUrl, "");
		String sourceFileName = getPathFromUrl(sourceUrl);
		return sourceFileName;
	}
	
	private String getDestFileNameFromCacheUrl(String url)
	{
		return getPathFromUrl(url);
	}

	public String getPathFromUrl(String url)
	{
		Uri uri = Uri.parse(url);
		return uri.getPath();
	}
    
	private boolean copyFile(String sourceFileName, String destFileName)
	{
		AssetManager assetManager = mContext.getAssets();

		File destFile = new File(destFileName);

		if (mDebug) destFile.delete();

		File destParentDir = destFile.getParentFile();
		destParentDir.mkdirs();

		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = assetManager.open(sourceFileName);
			out = new FileOutputStream(destFile);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
}
