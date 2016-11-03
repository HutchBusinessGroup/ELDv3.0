package com.hutchgroup.elog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class TermsAndCondition extends AppCompatActivity {

    WebView webView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_condition);
        initialize();
    }

    private void initialize() {
        String htmlText = "<html><body style=\"text-align:justify\"> %s </body></Html>";
        webView1 = (WebView) findViewById(R.id.webView1);
        WebView webView = (WebView) findViewById(R.id.webView1);
        webView.loadData(String.format(htmlText, getResources().getString(R.string.terms_of_service_01)), "text/html", "utf-8");
    }
}
