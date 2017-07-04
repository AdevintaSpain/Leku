package com.schibstedspain.leku.geocoder.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class NetworkClient {
  private static final int REPONSE_MAX_LENGTH = 1024;
  private static final int READ_TIMEOUT = 3000;
  private static final int CONNECT_TIMEOUT = 3000;

  public String requestFromLocationName(String request) {
    String result = null;
    InputStream stream = null;
    HttpsURLConnection connection = null;
    try {
      URL url = new URL(request);
      connection = (HttpsURLConnection) url.openConnection();
      connection.setReadTimeout(READ_TIMEOUT);
      connection.setConnectTimeout(CONNECT_TIMEOUT);
      connection.setRequestMethod("GET");
      connection.setDoInput(true);
      connection.connect();
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpsURLConnection.HTTP_OK) {
        throw new NetworkException("HTTP error code: " + responseCode);
      }
      stream = connection.getInputStream();
      if (stream != null) {
        result = readStream(stream, REPONSE_MAX_LENGTH);
      }
    } catch (IOException ioException) {
      throw new NetworkException(ioException);
    } finally {

      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ioException) {
          throw new NetworkException(ioException);
        }
      }
      if (connection != null) {
        connection.disconnect();
      }
    }
    return result;
  }


  private String readStream(InputStream stream, int maxLength) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[maxLength];
    int length;
    while ((length = stream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result.toString("UTF-8");
  }

}
