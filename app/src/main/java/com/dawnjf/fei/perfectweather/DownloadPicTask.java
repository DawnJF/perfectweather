package com.dawnjf.fei.perfectweather;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by fei on 2017/3/10.
 */

public class DownloadPicTask extends AsyncTask<String, Integer, Integer>{
    private static final String TAG = "wwww";
    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_EXISTS = 2;

    private DownLoadPicListener mListener;
    private int lastProgress;

    public DownloadPicTask(DownLoadPicListener listener) {
        mListener = listener;
    }

    // 任务
    @Override
    protected Integer doInBackground(String... params) {
        InputStream inputStream = null;
        // 使用可以保存状态的流操作(可以使用seek()方法)
        RandomAccessFile savedFile = null;
        File file;
        try {
            long downloadLength = 0;
            String url = params[0];
            Log.i(TAG, "doInBackground: " + url);
            String fileName = url.substring(url.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS).getPath();
            // 存在判断，记录已经下载长度
            file = new File(directory + fileName);
            if (file.exists()) {
                downloadLength = file.length();
            }
            long contentLength = getContentLength(url);
            if (contentLength == 0) {
                return TYPE_FAILED;
            }else if (downloadLength == contentLength) {
                return TYPE_EXISTS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "byte=" + downloadLength + "-")
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            if (response == null) {
                return TYPE_FAILED;
            }
            inputStream = response.body().byteStream();
            savedFile = new RandomAccessFile(file, "rw");
            savedFile.seek(downloadLength);
            byte[] b = new byte[1024];
            int total = 0;
            int len;
            while ((len = inputStream.read(b)) != -1) {
                total += len;
                savedFile.write(b, 0, len);
                int progress = (int) ((total + downloadLength)
                        / contentLength * 100);
                publishProgress(progress);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (savedFile != null)
                    savedFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_SUCCESS;
    }

    // 进度更新
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            mListener.showProgress(progress);
            lastProgress = progress;
        }
    }

    // 执行结果
    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer) {
            case TYPE_EXISTS:
                mListener.exists();
                break;
            case TYPE_FAILED:
                mListener.onFailed();
                break;
            case TYPE_SUCCESS:
                mListener.onSuccess();
                break;
            default:
        }
    }

    /**
     * 获取文件总大小
     * @param url
     * @return
     * @throws IOException
     */
    private long getContentLength(String url)
            throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long length = response.body().contentLength();
            return length;
        }
        return 0;
    }
}
