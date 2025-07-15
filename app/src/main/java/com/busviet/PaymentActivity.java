package com.busviet;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class PaymentActivity extends AppCompatActivity {

    private WebView webView;
    public static final String EXTRA_PAYMENT_URL = "paymentUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        webView = findViewById(R.id.webViewPayment);

        // Enable JavaScript
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        // Xử lý điều hướng
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("WebView", "URL: " + url);

                // Nếu returnUrl chứa vnp_ResponseCode
                if (url.contains("vnp_ResponseCode=")) {
                    Uri uri = Uri.parse(url);
                    String code = uri.getQueryParameter("vnp_ResponseCode");

                    if ("00".equals(code)) {
                        Toast.makeText(PaymentActivity.this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                    }

                    finish(); // Đóng WebView
                    return true;
                }

                view.loadUrl(url);
                return true;
            }
        });

        // Nhận URL từ Intent
        String url = getIntent().getStringExtra(EXTRA_PAYMENT_URL);
        if (url != null) {
            webView.loadUrl(url);
        } else {
            Toast.makeText(this, "Không có URL thanh toán", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
