package com.calamus.easykorean.app;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public class XUtils
{


    public static int toPx (Context c, int dp){
        float scale = c.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static String fetch (String s){
        String result = "";

        try {
            URL url = new URL(s);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){

                ByteArrayOutputStream ba = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;

                while ((length = conn.getInputStream().read(buffer)) != -1){
                    ba.write(buffer, 0, length);
                }
                ba.close();
                result = ba.toString("UTF-8");
            }}
        catch (Exception e)
        {
            result = e.getMessage();
        }
        return result;
    }


    public static String parseDate(String t) {
        try {
            t = t.substring(0, t.length() - 6);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat ndf = new SimpleDateFormat("hh:mm a, dd/MM");
            Date d = df.parse(t);
            return ndf.format(Objects.requireNonNull(d));
        } catch (ParseException e) {
            return e.getMessage();
        }
    }
}
