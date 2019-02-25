package com.sellio.pos.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.sellio.pos.R;

import im.delight.android.webview.AdvancedWebView;

/**
 * Created by ivan on 5/24/2018.
 */

public class AboutActivity extends FragmentActivity implements AdvancedWebView.Listener{

    private static final String PAGE_URL = "file:///android_asset/about.html";
//webView.loadUrl("file:///android_asset/filename.html");.
    AdvancedWebView mWebView;
    ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);

        mWebView = findViewById(R.id.web_view);
        spinner = findViewById(R.id.progressBar);

        spinner.setVisibility(View.VISIBLE);

        mWebView.setListener( this, this );
        mWebView.setGeolocationEnabled(false);
        mWebView.setMixedContentAllowed(true);
        mWebView.setCookiesEnabled(true);
        mWebView.setThirdPartyCookiesEnabled(true);

        mWebView.addHttpHeader("X-Requested-With", "");
        mWebView.loadUrl(PAGE_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        spinner.setVisibility(View.GONE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {


    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }
}
