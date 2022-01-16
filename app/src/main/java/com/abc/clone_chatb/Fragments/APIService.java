package com.abc.clone_chatb.Fragments;

import com.abc.clone_chatb.Notifications.MyResponse;
import com.abc.clone_chatb.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA4Ydgn1w:APA91bFENs40wxb6uy5GZk9UNcVnIPCF7wkNI-S9IyyJ9SxHyTsnsYw3djIroKB-yw5LcBts1O8wJEv8K-CJQgalWqSmAzLfiIoUFTejK4sSU9Y4YbbTR_w075X05w8PwXp5CQr73oj-"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
