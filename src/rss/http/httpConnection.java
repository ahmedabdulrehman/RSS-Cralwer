package rss.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class httpConnection {

    public int httpConn (URL url) {
        int code;
        try {
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.connect();
            code = conn.getResponseCode();
            System.out.println("Http response code from server... " + code);
            conn.disconnect();
            return code;
        } catch (IOException e) {
            System.out.println("......Http connection timeout......");
            return 600;
        }
    }

    public URL httpToHttps (URL url) {
        String newUrlString;

        if (url.toString().matches("^http://.*$")) {
             newUrlString = url.toString().replaceFirst("^http", "https");
        } else if (url.toString().matches("^https://.*$")) {
            newUrlString = url.toString().replaceFirst("^https", "http");
        } else {
            return url;
        }

        try {
            return new URL(newUrlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return url;
        }
    }

}
