package com.example.whatsapp.Fragments;

import com.example.whatsapp.Notifications.MyResponse;
import com.example.whatsapp.Notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService
{
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAFWl9cVo:APA91bFuKAesy_e8vDCl0icaoKwSd4vKQIE3Xq_fdgWOAQ_jfO4GvOs2jEOEXaxB7b-fWNeLuWSBTcZGUd-Dk9FxjoJP1AHvSbt4hK2mrlHkx4kZmqHwOaZKr5oMWxuHVNnSlCwMkiq7"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
