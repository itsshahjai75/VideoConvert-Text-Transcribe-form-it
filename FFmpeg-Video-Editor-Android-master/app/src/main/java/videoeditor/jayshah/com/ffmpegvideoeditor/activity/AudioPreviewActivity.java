package videoeditor.jayshah.com.ffmpegvideoeditor.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.services.language.v1beta2.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta2.CloudNaturalLanguage;
import com.google.api.services.language.v1beta2.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta2.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta2.model.Document;
import com.google.api.services.language.v1beta2.model.Entity;
import com.google.api.services.language.v1beta2.model.Features;
import com.google.api.services.speech.v1beta1.Speech;
import com.google.api.services.speech.v1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1beta1.model.RecognitionAudio;
import com.google.api.services.speech.v1beta1.model.RecognitionConfig;
import com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeRequest;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeResponse;
import com.google.api.services.speech.v1beta1.Speech;
import com.google.api.services.speech.v1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeRequest;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeResponse;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okio.ByteString;
import videoeditor.jayshah.com.ffmpegvideoeditor.NLApiData;
import videoeditor.jayshah.com.ffmpegvideoeditor.R;
import videoeditor.jayshah.com.ffmpegvideoeditor.views.VisualizerView;


import static videoeditor.jayshah.com.ffmpegvideoeditor.APICall.ResponseAPI;
import static videoeditor.jayshah.com.ffmpegvideoeditor.LanguagesGoogle.getLanguageTransletCodeName;
import static videoeditor.jayshah.com.ffmpegvideoeditor.LanguagesGoogle.getLanguageTransletEnglishName;
import static videoeditor.jayshah.com.ffmpegvideoeditor.LanguagesGoogle.getlanguageCodeName;
import static videoeditor.jayshah.com.ffmpegvideoeditor.LanguagesGoogle.getlanguageEnglishName;



public class AudioPreviewActivity extends AppCompatActivity {

    private VisualizerView mVisualizerView;

    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private static final String FILEPATH = "filepath";
    String filePath;

    ProgressDialog pd1,pd2 ;
    String selected_languageVideo="en",selected_languageTranslate="";
    Spinner spinnerLanguage,spinnerLanguageCode,spinnerLanguageTranslate,spinnerLanguageCodeTranslate;
    TextView tvTrancribeClick,tvNLText,tvVideolanguage,tvTranslateLanguage;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_preview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
        pd1 = new ProgressDialog(this);
        pd1.setCancelable(false);
        pd2 = new ProgressDialog(this);
        pd2.setCancelable(false);

        // Spinner Drop down elements


        // Spinner element
        spinnerLanguage = (Spinner) findViewById(R.id.spinnerLanguage);
        spinnerLanguageCode = (Spinner) findViewById(R.id.spinnerLanguageCode);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapterNameLAnguage = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, getLanguageTransletEnglishName() );

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapterCodeLanguage = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, getLanguageTransletCodeName() );

        // Drop down layout style - list view with radio button
        dataAdapterNameLAnguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Drop down layout style - list view with radio button
        dataAdapterCodeLanguage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnerLanguage.setAdapter(dataAdapterNameLAnguage);
        // attaching data adapter to spinner
        spinnerLanguageCode.setAdapter(dataAdapterCodeLanguage);
        spinnerLanguageCode.setEnabled(false);

        // Spinner click listener
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                spinnerLanguageCode.setSelection(position,false);
                selected_languageVideo = spinnerLanguageCode.getSelectedItem().toString();

                String item = adapterView.getItemAtPosition(position).toString();
                // Showing selected spinner item
                Toast.makeText(adapterView.getContext(), "Selected: " + item+"  "+selected_languageVideo, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Spinner element
        spinnerLanguageTranslate = (Spinner) findViewById(R.id.spinnerLanguageTranslate);
        spinnerLanguageCodeTranslate = (Spinner) findViewById(R.id.spinnerLanguageCodeTranslate);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapterNameLAnguageTranslate = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, getLanguageTransletEnglishName() );

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapterCodeLanguageTrasnlsate = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, getLanguageTransletCodeName() );

        // Drop down layout style - list view with radio button
        dataAdapterNameLAnguageTranslate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Drop down layout style - list view with radio button
        dataAdapterCodeLanguageTrasnlsate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnerLanguageTranslate.setAdapter(dataAdapterNameLAnguageTranslate);
        // attaching data adapter to spinner
        spinnerLanguageCodeTranslate.setAdapter(dataAdapterCodeLanguageTrasnlsate);
        spinnerLanguageCodeTranslate.setEnabled(false);

        // Spinner click listener
        spinnerLanguageTranslate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                spinnerLanguageCodeTranslate.setSelection(position,false);
                selected_languageTranslate = spinnerLanguageCodeTranslate.getSelectedItem().toString();

                String item = adapterView.getItemAtPosition(position).toString();
                // Showing selected spinner item
                Toast.makeText(adapterView.getContext(), "Selected: " + item+"  "+selected_languageTranslate, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        initAudio();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


    TextView tvInstruction;
    String base64EncodedData;
    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        filePath = getIntent().getStringExtra(FILEPATH);
        tvInstruction=(TextView) findViewById(R.id.tvInstruction);
        tvTrancribeClick=(TextView) findViewById(R.id.tvTrancribe);
        tvNLText=(TextView) findViewById(R.id.tvNLText);
        tvNLText.setVisibility(View.GONE);
        tvVideolanguage=(TextView) findViewById(R.id.tvVideolanguage);
        tvTranslateLanguage=(TextView) findViewById(R.id.tvTranslateLanguage);

        tvInstruction.setText("Audio stored at path "+filePath);
        mMediaPlayer = MediaPlayer.create(this, Uri.parse(filePath));

        setupVisualizerFxAndUI();
        // Make sure the visualizer is enabled only when you actually want to
        // receive data, and
        // when it makes sense to receive data.
        mVisualizer.setEnabled(true);
        // When the stream ends, we don't need to collect any more data. We
        // don't do this in
        // setupVisualizerFxAndUI because we likely  to have more,
        // non-Visualizer related code
        // in this callback.
        mMediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mVisualizer.setEnabled(false);
                    }
                });
        mMediaPlayer.start();
        mMediaPlayer.setLooping(false);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    InputStream stream = new FileInputStream(filePath);
                    //getContentResolver().openInputStream(Uri.parse(filePath));
                    byte[] audioData = IOUtils.toByteArray(stream);
                    stream.close();

                    base64EncodedData = Base64.encodeBase64String(audioData);
                    Log.i("Base64 Audio ===",base64EncodedData);


                    /*
                    THIS CODE FOR GOOLGE CLOUD SPACE

                    StorageOptions.Builder storage1 = StorageOptions.newBuilder().setProjectId("jabri-209007");
                    Storage storage = storage1.build().getService();

                    //continue as normal
                    // The name for the new bucket
                    String bucketName = "jayJabri";  // "my-new-bucket";

                    // Creates the new bucket
                    Bucket bucket = storage.create(BucketInfo.of(bucketName));

                    System.out.printf("Bucket %s created.%n", bucket.getName());*/

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // More code here
            }

        });

        tvTrancribeClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd1.show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                    new GoogleSpeechTransciption().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    //new IBMWatsonAudioTranscription().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }else{
                    new GoogleSpeechTransciption().execute();
                    //new IBMWatsonAudioTranscription().execute();
                }

                //new GoogleLongSpeechTranslate().execute(API_KEY);

                tvNLText.setVisibility(View.VISIBLE);



            }
        });


        tvNLText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tvVideolanguage.getText().length()>1){
                    googleNatulraLanguageAPI();
                }else{
                    Toast.makeText(AudioPreviewActivity.this,"No text found for analysis ",Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    public  class IBMWatsonAudioTranscription extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd2.show();
        }

        @Override
        protected String doInBackground(Object... parametros) {

            IamOptions options = new IamOptions.Builder()
                    .apiKey("RCykcYiINjmEo704j0mTbJZ3139EPXtzOQZVKp04gGVs")
                    .build();

            SpeechToText speechToText = new SpeechToText();
            speechToText.setUsernameAndPassword("d361958a-84df-460f-b9f4-eb6865bc3fbf", "vNrjOQOJR5FN");

            speechToText.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");


            String outputSpeechFormWatson="";
            try {
                List<String> files = Arrays.asList(filePath /*,"audio-file2.flac"*/);
                for (String file : files) {
                    RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
                            .audio(new File(file))
                            .contentType("audio/flac")
                            .timestamps(true)
                            //.wordAlternativesThreshold((float) 0.9)
                            //.keywords(Arrays.asList("colorado", "tornado", "tornadoes"))
                            //.keywordsThreshold((float) 0.5)
                            .build();

                    SpeechRecognitionResults speechRecognitionResults = speechToText.recognize(recognizeOptions).execute();
                    System.out.println("Speech Result -------------------------------------"+speechRecognitionResults);

                    JsonObject data= new JsonParser().parse(String.valueOf(speechRecognitionResults)).getAsJsonObject();
                    JsonArray result = data.get("results") .getAsJsonArray();

                    for(int a=0;a<result.size();a++){
                        JsonArray alternatives = result.get(a).getAsJsonObject().get("alternatives").getAsJsonArray();
                        for(int b=0;b<alternatives.size();b++) {
                            String transcript = alternatives.get(b).getAsJsonObject().get("transcript").getAsString();
                            outputSpeechFormWatson=  outputSpeechFormWatson+" "+transcript;
                        }
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception anyexcpetion){
                anyexcpetion.printStackTrace();
            }
            return outputSpeechFormWatson;
        }

        @Override
        protected void onPostExecute(String resultFinal) {
            super.onPostExecute(resultFinal);
            tvVideolanguage.setText("Watson Replies this---"+resultFinal);
            pd2.dismiss();
        }
    }


    String API_KEY = "AIzaSyB7ZDcen4uTiK9gzq4HhMo0gLes2aCPWpA";
    String resultOutput="",transcript="";
    String finalOutput = "";

    public static void asyncRecognizeGcs(String gcsUri) throws Exception {
        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create()) {

            // Configure remote file request for Linear16
            com.google.cloud.speech.v1p1beta1.RecognitionConfig config =
                    com.google.cloud.speech.v1p1beta1.RecognitionConfig.newBuilder()
                            .setEncoding(com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding.FLAC)
                            .setLanguageCode("en-US")
                            .setSampleRateHertz(16000)
                            .build();
            com.google.cloud.speech.v1p1beta1.RecognitionAudio audio = com.google.cloud.speech.v1p1beta1.RecognitionAudio.newBuilder().setUri(gcsUri).build();

            // Use non-blocking call for getting file transcription
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speech.longRunningRecognizeAsync(config, audio);
            while (!response.isDone()) {
                System.out.println("Waiting for response...");
                Thread.sleep(10000);
            }

            List<com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult> results = response.get().getResultsList();

            for (com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s\n", alternative.getTranscript());
            }
        }
    }


    public class GoogleSpeechTransciption extends AsyncTask<Object,Void,String>{

        @Override
        protected String doInBackground(Object... objects) {

            try {

                if (mMediaPlayer != null) {
                    mVisualizer.release();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }

                Speech speechService = new Speech.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setSpeechRequestInitializer(new SpeechRequestInitializer(API_KEY))
                        .build();


                com.google.api.services.speech.v1beta1.model.RecognitionConfig recognitionConfig = new com.google.api.services.speech.v1beta1.model.RecognitionConfig();
                recognitionConfig.setLanguageCode(selected_languageVideo);
                recognitionConfig.setSampleRate(16000);
                recognitionConfig.setEncoding("FLAC");
                //recognitionConfig.setLanguageCode("en-AU");
                //recognitionConfig.setLanguageCode("en-US");


                com.google.api.services.speech.v1beta1.model.RecognitionAudio recognitionAudio = new com.google.api.services.speech.v1beta1.model.RecognitionAudio();
                recognitionAudio.setContent(base64EncodedData);


                // Create request
                SyncRecognizeRequest request = new SyncRecognizeRequest();
                request.setConfig(recognitionConfig);
                request.setAudio(recognitionAudio);


                // Generate response
                SyncRecognizeResponse response = speechService.speech()
                        .syncrecognize(request)
                        .execute();


                // Extract transcript
                com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult result = response.getResults().get(0);
                resultOutput= String.valueOf(response);
                //transcript = result.getAlternatives().get(0).getTranscript(); //===this is zero index data from array-----
                //Log.e("transcript Outout ----", transcript);



                //==========================================================================================

                /*
                THIS IS FOR ASYNC METHOD TO READ FILE FROM GOOGLE STORE BUT GETTING ERROR OF AUTHENTICATION----
                try {
                    asyncRecognizeGcs("gs://jabri-speech/testaudio.flac");
                } catch (Exception e) {
                    e.printStackTrace();
                }*/




            }catch (Exception SpeechException) {
                SpeechException.printStackTrace();
            }




            return resultOutput;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd1.dismiss();
            Log.e("Result is---",resultOutput);

            try {

                JsonObject data = new JsonParser().parse(resultOutput).getAsJsonObject();
                JsonArray results = data.get("results").getAsJsonArray();

                for(int a=0;a<results.size();a++){
                    JsonArray alternatives = results.get(a).getAsJsonObject().get("alternatives").getAsJsonArray();
                    for (int b=0;b<alternatives.size();b++){
                        String transcript = alternatives.get(b).getAsJsonObject().get("transcript").getAsString();
                        finalOutput = finalOutput+" "+transcript;
                    }
                }
                tvVideolanguage.setText(finalOutput);
                //new GoogleLangDetect().execute(finalOutput,API_KEY);
              // new GoogleLangTranlate().execute(finalOutput,API_KEY);




            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }

    private void googleNatulraLanguageAPI(){

        pd1.show();

        final CloudNaturalLanguage naturalLanguageService =
                new CloudNaturalLanguage.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(),
                        null
                ).setCloudNaturalLanguageRequestInitializer(
                        new CloudNaturalLanguageRequestInitializer(API_KEY)
                ).build();

        Document document = new Document();
        document.setType("PLAIN_TEXT");
        document.setLanguage("en-US");
        document.setContent(finalOutput);


        Features features = new Features();
        features.setExtractEntities(true);
        features.setExtractEntitySentiment(true);
        features.setExtractSyntax(true);
        features.setExtractDocumentSentiment(true);

        final AnnotateTextRequest request = new AnnotateTextRequest();
        request.setDocument(document);
        request.setFeatures(features);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final AnnotateTextResponse response =
                            naturalLanguageService.documents()
                                    .annotateText(request).execute();


                    final List<Entity> entityList = response.getEntities();
                    final float sentiment = response.getDocumentSentiment().getScore();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String entities = "";
                            for(Entity entity:entityList) {
                                entities += "\n" +entity.getType() + entity.getName().toUpperCase();
                            }
                            Log.e("NLA goolge entities ---",entities+"----"+response.toString());

                            AlertDialog dialog =
                                    new AlertDialog.Builder(AudioPreviewActivity.this)
                                            .setTitle("Sentiment: " + sentiment)
                                            .setMessage("This audio file talks about :"
                                                    + entities)
                                            .setNeutralButton("Result", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startActivity(new Intent(AudioPreviewActivity.this, NLApiData.class)
                                                    .putExtra("data",response.toString()));
                                                }
                                            })
                                            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .create();
                            dialog.show();
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                    pd1.dismiss();
                }
                pd1.dismiss();

                // More code here
            }
        });


    }


    private void setupVisualizerFxAndUI() {

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    String lanResult;
    protected ProgressDialog progressDialog;
    private class GoogleLangDetect extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute()//execute thaya pela
        {
            super.onPreExecute();
            // Log.d("pre execute", "Executando onPreExecute ingredients");
            //inicia diálogo de progress, mostranto processamento com servidor.
            progressDialog = ProgressDialog.show(AudioPreviewActivity.this, "Loading", "Detecting...", true, false);

        }

        @Override
        protected String doInBackground(Object... parametros) {

            try {

                JsonArray langArray = new JsonArray();
                langArray.add(parametros[0].toString());
               // landObj.addProperty("key","AIzaSyAghzY4C-5vUMIKZ9lQOyY-EV2pSckhm_c");
                //Log.d("data send--",""+LoginJson.toString());

                JsonObject langObj = new JsonObject();
                langObj.add("q",langArray);

                String responseUSerTitles = ResponseAPI(getBaseContext(), "https://translation.googleapis.com/language/translate/v2/detect?&key="+API_KEY , langObj.toString(),"post");
                // Log.d("URL ====",Const.SERVER_URL_API+"filter_venues?"+upend);
                lanResult=responseUSerTitles;


            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return lanResult;
        }


        @Override
        protected void onPostExecute(String result) {

            String response_string = "";
            // System.out.println("OnpostExecute----done-------");
            super.onPostExecute(result);


            try {
                Log.i("RES lanResult---", lanResult);
                /*JsonParser parser = new JsonParser();
                JsonObject rootObj = parser.parse(resUserDetails).getAsJsonObject();

                String status = rootObj.get("status").getAsString();
                String message = rootObj.get("message").getAsString();*/


            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            progressDialog.dismiss();
        }
    }

    TextToSpeech t1;
    String TanslateResult;
    private class GoogleLangTranlate extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute()//execute thaya pela
        {
            super.onPreExecute();
            // Log.d("pre execute", "Executando onPreExecute ingredients");
            //inicia diálogo de progress, mostranto processamento com servidor.
            progressDialog = ProgressDialog.show(AudioPreviewActivity.this, "Loading", "Detecting...", true, false);

        }

        @Override
        protected String doInBackground(Object... parametros) {

            try {

                JsonObject langObj = new JsonObject();
                langObj.addProperty("q",parametros[0].toString());
                langObj.addProperty("source",selected_languageVideo);
                langObj.addProperty("target",selected_languageTranslate);
                langObj.addProperty("format","text");

                String responseUSerTitles = ResponseAPI(getBaseContext(), "https://translation.googleapis.com/language/translate/v2?&key="+parametros[1].toString() , langObj.toString(),"post");
                // Log.d("URL ====",Const.SERVER_URL_API+"filter_venues?"+upend);
                TanslateResult=responseUSerTitles;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return TanslateResult;
        }


        @Override
        protected void onPostExecute(String result) {

            String response_string = "";
            // System.out.println("OnpostExecute----done-------");
            super.onPostExecute(result);


            try {
                Log.i("RES TanslateResult---", TanslateResult);
                JsonParser parser = new JsonParser();
                JsonObject rootObj = parser.parse(TanslateResult).getAsJsonObject();

                String translatedText = rootObj.get("data").getAsJsonObject().get("translations").getAsJsonArray().get(0).getAsJsonObject().get("translatedText").getAsString();

                tvTranslateLanguage.setText(translatedText);

                t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {


                        if (status == TextToSpeech.SUCCESS) {

                            int result = t1.setLanguage(Locale.US);

                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "This Language is not supported");
                            } else {
                                speakOut();
                            }

                        } else {
                            Log.e("TTS", "Initilization Failed!");
                        }


                    }
                });
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            progressDialog.dismiss();
        }
    }


    private void speakOut() {
        t1.speak(tvTranslateLanguage.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
    }



    String LongSpeech;
    private class GoogleLongSpeechTranslate extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute()//execute thaya pela
        {
            super.onPreExecute();
            // Log.d("pre execute", "Executando onPreExecute ingredients");
            //inicia diálogo de progress, mostranto processamento com servidor.
            progressDialog = ProgressDialog.show(AudioPreviewActivity.this, "Loading", "sending...", true, false);

        }

        @Override
        protected String doInBackground(Object... parametros) {

            try {



                JsonObject mainJson = new JsonObject();


                JsonObject config = new JsonObject();
                config.addProperty("encoding","FLAC");
                config.addProperty("sampleRateHertz",16000);
                config.addProperty("languageCode","en-US");


                JsonObject audio = new JsonObject();
                audio.addProperty("content",base64EncodedData);

                mainJson.add("config",config);
                mainJson.add("audio",audio);

                String responseUSerTitles = ResponseAPI(getBaseContext(), "https://speech.googleapis.com/v1/speech:longrunningrecognize?key="+parametros[0].toString() , mainJson.toString(),"post");
                // Log.d("URL ====",Const.SERVER_URL_API+"filter_venues?"+upend);
                TanslateResult=responseUSerTitles;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return TanslateResult;
        }


        @Override
        protected void onPostExecute(String result) {

            String response_string = "";
            // System.out.println("OnpostExecute----done-------");
            super.onPostExecute(result);


            try {
                Log.i("LongTanslateResult---", TanslateResult);
               /* JsonParser parser = new JsonParser();
                JsonObject rootObj = parser.parse(TanslateResult).getAsJsonObject();

                String translatedText = rootObj.get("data").getAsJsonObject().get("translations").getAsJsonArray().get(0).getAsJsonObject().get("translatedText").getAsString();

                tvTranslateLanguage.setText(translatedText);

                t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {


                        if (status == TextToSpeech.SUCCESS) {

                            int result = t1.setLanguage(Locale.US);

                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "This Language is not supported");
                            } else {
                                speakOut();
                            }

                        } else {
                            Log.e("TTS", "Initilization Failed!");
                        }


                    }
                });*/
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            progressDialog.dismiss();
        }
    }

}
