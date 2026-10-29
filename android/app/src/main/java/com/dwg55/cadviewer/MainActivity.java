
package com.dwg55.cadviewer;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

public class MainActivity extends Activity {

    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                WebView view,
                ValueCallback<Uri[]> callback,
                FileChooserParams params
            ) {
                if (fileCallback != null) {
                    fileCallback.onReceiveValue(null);
                }

                fileCallback = callback;

                Intent intent = new Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                );
                intent.addCategory(
                    Intent.CATEGORY_OPENABLE
                );
                intent.setType("*/*");
                intent.putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    new String[]{
                        "application/acad",
                        "application/dwg",
                        "application/octet-stream",
                        "application/dxf"
                    }
                );

                try {
                    startActivityForResult(
                        intent, FILE_REQUEST
                    );
                    return true;
                } catch (Exception e) {
                    fileCallback = null;
                    callback.onReceiveValue(null);
                    return false;
                }
            }
        });

        webView.loadUrl(
            "https://dwg55.github.io/DWG55-CAD-Viewer/"
        );
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {
        super.onActivityResult(
            requestCode, resultCode, data
        );

        if (requestCode == FILE_REQUEST &&
            fileCallback != null) {

            Uri[] results = null;

            if (resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

                results = new Uri[]{
                    data.getData()
                };
            }

            fileCallback.onReceiveValue(results);
            fileCallback = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (fileCallback != null) {
            fileCallback.onReceiveValue(null);
            fileCallback = null;
        }

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}
