package blackbox.petsnaps;

import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

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

public class VisionAnalysisRetriever {

    private final static String API_KEY = BuildConfig.API_KEY;
    private static String imageUrl;

    public VisionAnalysisRetriever() {
        new GetAnalysisTask().execute();
    }
    /*public VisionAnalysisRetriever(String imgUrl) {
        imageUrl = imgUrl;
    }

    public interface OnAnalysisRetrievedListener {
        void OnAnalysisRetrieved(String thing);
    } */

    private class GetAnalysisTask extends AsyncTask<Void, Void, Void> {
       // private OnAnalysisRetrievedListener listener;

        /*public GetAnalysisTask(OnAnalysisRetrievedListener listener) {
            this.listener = listener;
        }*/
        public GetAnalysisTask() {}


        @Override
        protected Void doInBackground(Void... params) {
            Log.d("RETRIEVER", "DOINBACKGROUND INITIATED");
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

                String imageUrl = "";
                BufferedWriter httpRequestBodyWriter = new BufferedWriter(new
                        OutputStreamWriter(httpConnection.getOutputStream()));
                httpRequestBodyWriter.write
                        ("{\"requests\":  [{ \"features\":  [ {\"type\": \"LABEL_DETECTION\""
                                +"}], \"image\": {\"source\": { \"imageUri\":"
                                + imageUrl + "}}}]}");
                httpRequestBodyWriter.close();
                String response = httpConnection.getResponseMessage();

                Log.d("RETRIEVER", "ABOUT TO PRINT");
                System.out.println(response);
                if (httpConnection.getInputStream() == null) {
                    System.out.println("No stream");
                    return null;
                }

                Scanner httpResponseScanner = new Scanner (httpConnection.getInputStream());
                String resp = "";
                while (httpResponseScanner.hasNext()) {
                    String line = httpResponseScanner.nextLine();
                    resp += line;
                    System.out.println(line);  //  alternatively, print the line of response
                }
                httpResponseScanner.close();

            }
            catch (Exception e) {
                Log.d("EXCEPTION", "GOOGLE VISION ERROR");
            }
            return null;
        }
    }

}
