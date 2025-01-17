package com.getstream.sdk.chat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.Utils;

/**
 * An Activity showing attachments such as websites, youtube and giphy.
 */
public class AttachmentActivity extends AppCompatActivity {

    private final String TAG = AttachmentActivity.class.getSimpleName();
    WebView webView;

    ImageView iv_image;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_activity_attachment);

        webView = findViewById(R.id.webView);
        iv_image = findViewById(R.id.iv_image);
        progressBar = findViewById(R.id.progressBar);

        configUIs();

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        String url = intent.getStringExtra("url");
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Something error!", Toast.LENGTH_SHORT);
            return;
        }
        showAttachment(type, url);
    }


    private void configUIs() {
        iv_image.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        // WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebViewClient(new AppWebViewClients());
    }

    private void showAttachment(final String type, final String url) {
        switch (type) {
            case ModelType.attach_video:
                loadUrlToWeb(url);
                break;
            case ModelType.attach_giphy:
                showGiphy(url);
                break;
            case ModelType.attach_image:
                break;
            case ModelType.attach_link:
            case ModelType.attach_product:
                loadUrlToWeb(url);
                break;
            case ModelType.attach_file:
                break;
            default:
                break;
        }
    }

    /**
     * Show web view with url
     *
     * @param url web url
     */
    public void loadUrlToWeb(String url) {
        iv_image.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }


    /**
     * Play giphy with url
     *
     * @param url giphy url
     */
    public void showGiphy(String url) {
        if (url == null) {
            Utils.showMessage(this, "Error!");
            return;
        }
        iv_image.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);

        Glide.with(this)
                .asGif()
                .load(url)
                .into(iv_image);
    }

    private class AppWebViewClients extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }
}
