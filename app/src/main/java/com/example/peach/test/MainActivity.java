package com.example.peach.test;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.BitmapFactory;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    String result = "empty";
    private TessBaseAPI tessBaseApi;
    private static final String lang = "tha";
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";

    Button btnSpeak,btnLoad,btnOcr,btn1,btn2,btn3,btn4,btn5;
    ImageView imageView;
    ImageView imageView2;
    Uri imageUri;
    String datapath = "";
    Bitmap image;
    TextView textView;
    int selimg;
    private static final String FILEapp = "onapp.txt";
    private TessBaseAPI mTess;
    private String selectedImagePath;
    private static final int PICK_IMAGE=100;
    private int SELECT_IMAGE;
    private static final String TESSDATA = "tessdata";

    TextToSpeech t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLoad = (Button) findViewById(R.id.btnLoad);
        btnOcr = (Button) findViewById(R.id.btnOcr);
        btnSpeak = (Button)findViewById(R.id.btnSpeak);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        textView = (TextView) findViewById(R.id.textView);
        btn1 = (Button)findViewById(R.id.btn1);
        btn2 = (Button)findViewById(R.id.btn2);
        btn3 = (Button)findViewById(R.id.btn3);
        btn4 = (Button)findViewById(R.id.btn4);
        btn5 = (Button)findViewById(R.id.btn5);

        //initialize Tesseract API
        String language = "tha+eng";
        datapath = getFilesDir() + "/tesseract/";
        mTess = new TessBaseAPI();
        checkFile(new File(datapath + "tessdata/"));
        mTess.init(datapath, language);
        mTess.setDebug(true);

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // Intent intent = new Intent();
                //intent.setType("image/*");
                //intent.setAction(Intent.ACTION_GET_CONTENT);//
                //startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
                opengally();
            }
        });
        btnOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), Integer.toString(imageView.getWidth()) , Toast.LENGTH_SHORT).show();
                //startOCR(imageUri);
                textView.setText("Processing...");
                textView.setText(processImage(imageView));
                //textView.append(processImage(imageView2));
                //saveapp();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selimg = 1;
                opengally();
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selimg = 2;
                opengally();
            }
        });
/*
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
*/

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(new Locale("th", "TH"));
                }
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!t1.isSpeaking()) {
                    String toSpeak = textView.getText().toString();
                    Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            //imageView.setImageURI(imageUri);
            if(selimg == 1) {
                imageView.setImageURI(imageUri);
            }
            else if(selimg == 2) {
                imageView2.setImageURI(imageUri);
            }
            //PhotoPath = imageUri.getPath();
        }
    }

    private void opengally() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/tha.traineddata";
            String datafilepath1 = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            File datafile1 = new File(datafilepath1);
            if (!datafile.exists()) {
                copyFiles();
            }
            if (!datafile1.exists()){
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/tha.traineddata";
            String filepath1 = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/tha.traineddata");
            InputStream instream1 = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            OutputStream outstream1 = new FileOutputStream(filepath1);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

            while ((read = instream1.read(buffer)) != -1) {
                outstream1.write(buffer, 0, read);
            }
            outstream1.flush();
            outstream1.close();
            instream1.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
            File file1 = new File(filepath1);
            if (!file1.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String processImage(ImageView img){
        String OCRresult = "";
        img.buildDrawingCache();
        image = img.getDrawingCache();

        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        //TextView OCRTextView = (TextView) findViewById(R.id.textView);
        return OCRresult;
        //textView.setText(OCRresult);
        //OCRTextView.setText(OCRresult);
    }

    public void saveapp() {
        String text = textView.getText().toString();
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILEapp, MODE_PRIVATE);
            fos.write(text.getBytes());

            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILEapp,
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startOCR(Uri imgUri) {
        try {
            //Toast.makeText(this, "Saved to " + imgUri + "/" ,
            //       Toast.LENGTH_LONG).show();
            //Log.e(TAG, imgUri.getPath());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath(), options);
            imageView.buildDrawingCache();
            bitmap = imageView.getDrawingCache();
            result = extractText(bitmap);

            textView.setText(result);
            //textView.setText(imgUri.getPath());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }
        tessBaseApi.init(datapath, lang);
        //datapath = getFilesDir() + "/tesseract/";
        //tessBaseApi.init(datapath, lang);
//       //EXTRA SETTINGS
//        //For example if we only want to detect numbers
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//
//        //blackList Example
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }

    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }
    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
    }

    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }

}
