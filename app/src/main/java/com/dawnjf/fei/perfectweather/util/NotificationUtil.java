package com.dawnjf.fei.perfectweather.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.dawnjf.fei.perfectweather.R;

/**
 * Created by fei on 2017/3/10.
 */

public class NotificationUtil {

    public static Notification getNotification(Context context, String title, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource
                (context.getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(title);
        builder.setContentText(progress + "%");
        builder.setProgress(50, progress, false);
        return builder.build();
    }

    public static Notification getSuccessNotification(Context context, Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "image/*");
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource
                (context.getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("下载完成");
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pi);
        builder.setAutoCancel(true);
        return builder.build();
    }
}
