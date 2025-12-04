package com.example.finalproject;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class CloudinaryUploader {

    public static final String CLOUD_NAME = CloudinaryConfig.CLOUD_NAME;
    public static final String UPLOAD_PRESET = CloudinaryConfig.UPLOAD_PRESET;

    public static String uploadUriStreaming(ContentResolver resolver, Uri uri, String fileName) throws Exception {

        final String mimeType = resolver.getType(uri) != null
                ? resolver.getType(uri)
                : "application/pdf";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();

        // STREAM FILE
        RequestBody fileBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(mimeType);
            }

            @Override
            public void writeTo(BufferedSink sink) {
                try (InputStream is = resolver.openInputStream(uri)) {
                    if (is == null) throw new RuntimeException("InputStream null");

                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        sink.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build();

        Request request = new Request.Builder()
                .url("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload")
                .post(requestBody)
                .build();

        Response resp = client.newCall(request).execute();

        if (!resp.isSuccessful()) {
            throw new RuntimeException("Upload error: " + resp.code());
        }

        String respStr = resp.body().string();
        JSONObject json = new JSONObject(respStr);

        return json.getString("secure_url");
    }
}
