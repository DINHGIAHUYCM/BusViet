package com.busviet;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VNPay {

    // Giao diện callback để xử lý kết quả
    public interface PaymentCallback {
        void onSuccess(String paymentUrl);    // Trả URL để mở WebView
        void onFailure(String errorMessage);  // Báo lỗi nếu có
    }

    public static void requestPaymentUrl(Context context, int amount, PaymentCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // Tạo JSON gửi lên backend
        JSONObject json = new JSONObject();
        try {
            json.put("amount", amount);               // Số tiền (VND)
            json.put("language", "vn");               // Ngôn ngữ hiển thị
            json.put("bankCode", "");                 // Để trống nếu không muốn chỉ định
        } catch (JSONException e) {
            callback.onFailure("Lỗi tạo dữ liệu JSON: " + e.getMessage());
            return;
        }

        // Khai báo kiểu dữ liệu gửi đi
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, json.toString());

        // Tạo HTTP request đến backend của bạn
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8888/order/create_payment_url") // ⚠️ Đổi lại thành IP hoặc domain thật khi build
                .post(body)
                .build();

        // Gửi request bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("VNPay", "Lỗi kết nối đến server", e); // 🟠 In log lỗi đầy đủ stacktrace

                ((Activity) context).runOnUiThread(() ->
                        callback.onFailure("Lỗi kết nối server: " + e.getMessage())
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resStr = response.body().string();
                        JSONObject jsonRes = new JSONObject(resStr);
                        String url = jsonRes.getString("paymentUrl");

                        // Gửi kết quả về UI thread
                        ((Activity) context).runOnUiThread(() -> {
                            callback.onSuccess(url);
                        });

                    } catch (JSONException e) {
                        ((Activity) context).runOnUiThread(() ->
                                callback.onFailure("Phân tích JSON lỗi: " + e.getMessage()));
                    }
                } else {
                    ((Activity) context).runOnUiThread(() ->
                            callback.onFailure("Server trả về lỗi: " + response.code()));
                }
            }
        });
    }
}
