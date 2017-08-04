package blackbox.petsnaps;

import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

/* RESOURCE:
   https://cloud.google.com/community/tutorials/make-an-http-request-to-the-cloud-vision-api-from-java */

public class SafetyAnalysisRetriever {

    private final static String API_KEY = BuildConfig.API_KEY;
    private static String imageUrl;

    public SafetyAnalysisRetriever(String imgUrl, OnAnalysisRetrievedListener listener) {
        imageUrl = imgUrl;
        new GetAnalysisTask(listener).execute();
    }

    public interface OnAnalysisRetrievedListener {
        void OnAnalysisRetrieved(Boolean isImageSafe);
    }

    private class GetAnalysisTask extends AsyncTask<Void, Void, Boolean> {
        private OnAnalysisRetrievedListener listener;

        public GetAnalysisTask(OnAnalysisRetrievedListener listener) {
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // USING GOOGLE VISION TO CHECK FOR EXPLICIT IMAGE CONTENT
            Log.d("RETRIEVER", "DOINBACKGROUND INITIATED");
            Boolean isImageSafe = true;
            try {
                Log.d("RETRIEVER", "IN TRY BLOCK");
                String TARGET_URL = "https://vision.googleapis.com/v1/images:annotate?";
                String KEY = "key=" + API_KEY;
                URL serverUrl = new URL(TARGET_URL + KEY);
                URLConnection urlConnection = serverUrl.openConnection();
                HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
                httpConnection.setRequestMethod("POST");
                httpConnection.setRequestProperty("Content-Type", "application/json");
                httpConnection.setDoOutput(true);

                String formattedUrl = "\"" + imageUrl + "\"";
                BufferedWriter httpRequestBodyWriter = new BufferedWriter(new
                        OutputStreamWriter(httpConnection.getOutputStream()));
                httpRequestBodyWriter.write
                        ("{\"requests\":  [{ \"features\":  [ {\"type\": \"SAFE_SEARCH_DETECTION\""
                                +"}], \"image\": {\"source\": { \"imageUri\":"
                                + formattedUrl + "}}}]}");
                httpRequestBodyWriter.close();
                String response = httpConnection.getResponseMessage();

                Log.d("RETRIEVER", "ABOUT TO PRINT");
                System.out.println(response);
                if (httpConnection.getInputStream() == null) {
                    System.out.println("No stream");
                    return null;
                }

                // GET RESPONSE AS STRING
                Scanner httpResponseScanner = new Scanner (httpConnection.getInputStream());
                String resp = "";
                while (httpResponseScanner.hasNext()) {
                    String line = httpResponseScanner.nextLine();
                    resp += line;
                }
                httpResponseScanner.close();

                // PARSE RESPONSE FOR ADULT AND VIOLENCE LEVELS
                JSONObject jsonObject = new JSONObject(resp);
                JSONArray responses = jsonObject.getJSONArray("responses");
                JSONObject firstResponse = responses.getJSONObject(0);
                JSONObject safeSearchAnnotation = firstResponse.getJSONObject("safeSearchAnnotation");
                String adultContentLvl = safeSearchAnnotation.getString("adult");
                String violenceContentLvl = safeSearchAnnotation.getString("violence");

                System.out.println("ADULT: " + adultContentLvl + " VIOLENCE: " + violenceContentLvl);
                if (adultContentLvl.equals("VERY_LIKELY") || violenceContentLvl.equals("VERY_LIKELY")) {
                    isImageSafe = false;
                }
            }
            catch (Exception e) {
                Log.d("EXCEPTION", "GOOGLE VISION ERROR");
                e.printStackTrace();
            }
            return isImageSafe;
        }

        @Override
        protected void onPostExecute(Boolean isImageSafe) {
            listener.OnAnalysisRetrieved(isImageSafe);
        }
    }

}
