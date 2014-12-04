package com.datumdroid.android.ocr.simple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;


public class MyAsyncTask extends AsyncTask<String, Void, String> {

    private static final int REGISTRATION_TIMEOUT = 3 * 1000;
    private static final int WAIT_TIMEOUT = 30 * 1000;
    private final HttpClient httpclient = new DefaultHttpClient();

    HttpParams params = httpclient.getParams();
    HttpResponse response;
    private String content =  null;
    private boolean error = false;

    private Context mContext;
    private int NOTIFICATION_ID = 1;
    private Notification mNotification;
    private NotificationManager mNotificationManager;

    public AsyncResponse delegate=null;

    public MyAsyncTask(Context context){

        this.mContext = context;

        //Get the notification manager
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    protected void onPreExecute() {
        createNotification("Data download is in progress","");
    }

    protected String doInBackground(String... urls) {

        String url_toCall = null;
        try {
            url_toCall = urls[0];
            String input_string = urls[1];
            HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, WAIT_TIMEOUT);
            ConnManagerParams.setTimeout(params, WAIT_TIMEOUT);

            HttpPost httpPost = new HttpPost(url_toCall);
            httpPost.setEntity(new StringEntity(input_string));

            response = httpclient.execute(httpPost);

            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                content = out.toString();
            }
            else{
                //Closes the connection.
                Log.w("HTTP1:",statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }


        } catch (ClientProtocolException e) {
            Log.w("HTTP2:",e );
            content = e.getMessage();
            error = true;
            cancel(true);
        } catch (IOException e) {
            Log.w("HTTP3:",e );
            content = e.getMessage();
            error = true;
            cancel(true);
        }catch (Exception e) {
            Log.w("HTTP4:",e );
            content = e.getMessage();
            error = true;
            cancel(true);
        }

        return content;
    }

    protected void onCancelled() {
        createNotification("Error occured during data download",content);
    }

    protected void onPostExecute(String content) {
        if (error) {
            createNotification("Data download ended abnormally!",content);
        } else {
            createNotification("Data download is complete!","");
            delegate.processFinish(content);
        }
    }

    private void createNotification(String contentTitle, String contentText) {

        //Build the notification using Notification.Builder
        Notification.Builder builder = new Notification.Builder(mContext)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText);

        //Get current notification
        mNotification = builder.getNotification();

        //Show the notification
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
}