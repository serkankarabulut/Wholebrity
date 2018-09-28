package com.example.serkan.wholebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public class FetchTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            try {
                URL sourceURL = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) sourceURL.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(input);
                int data = reader.read();
                while (data != -1) {
                    result += (char) data;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }
        }
    }

    public class FetchImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL imageURL = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) imageURL.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap imageBitmap = BitmapFactory.decodeStream(input);
                return imageBitmap;
            } catch (Exception e) {
                System.out.println(e);
                return null;
            }
        }
    }

    private int answerIndex;
    private Button firstButton;
    private Button secondButton;
    private Button thirdButton;
    private Button fourthButton;
    private ImageView celebrityImage;
    private FetchTask task;
    private FetchImage imageFetcher;
    private ArrayList<String> celebrityImageList = new ArrayList<String>();
    private ArrayList<String> celebrityNameList = new ArrayList<String>();
    private ArrayList<String> answerList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.celebrityImage = findViewById(R.id.celebrityImageView);
        this.firstButton = findViewById(R.id.firstButton);
        this.secondButton = findViewById(R.id.secondButton);
        this.thirdButton = findViewById(R.id.thirdButton);
        this.fourthButton = findViewById(R.id.fourthButton);
        this.task = new FetchTask();
        fetchData();
        play();
    }

    public void parseImage(String data) {
        Pattern p = Pattern.compile("img src=\"(.*?)\"");
        Matcher m = p.matcher(data);
        while (m.find()) {
            this.celebrityImageList.add(m.group(1));
        }
    }

    public void parseNames(String data) {
        Pattern p = Pattern.compile("alt=\"(.*?)\"");
        Matcher m = p.matcher(data);
        while (m.find()) {
            this.celebrityNameList.add(m.group(1));
        }
    }

    public void fetchData() {
        String fetchedData = null;

        try {
            fetchedData = task.execute("http://www.posh24.se/kandisar").get();
            String[] fetchedDataArray = fetchedData.split("<div class=\"listedArticles\">");
            parseImage(fetchedDataArray[0]);
            parseNames(fetchedDataArray[0]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        Random rand = new Random();
        this.answerIndex = rand.nextInt(this.celebrityImageList.size());
        try {
            this.imageFetcher = new FetchImage();
            Bitmap imageBitmap = this.imageFetcher.execute(this.celebrityImageList.get(this.answerIndex)).get();
            this.celebrityImage.setImageBitmap(imageBitmap);
            setAnswers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAnswers() {
        int i = 0;
        Random rand = new Random();
        while (i < 4) {
            int randIndex = rand.nextInt(this.celebrityNameList.size());
            if (!this.answerList.contains(this.celebrityNameList.get(randIndex))) {
                this.answerList.add(this.celebrityNameList.get(randIndex));
                i++;
            }
        }
        if(!this.answerList.contains(this.celebrityNameList.get(this.answerIndex))){
            int randIndex = rand.nextInt(4);
            this.answerList.set(randIndex, this.celebrityNameList.get(this.answerIndex));
        }
        fillAnswerButtons();
    }

    public void fillAnswerButtons(){
        this.firstButton.setText(this.answerList.get(0));
        this.secondButton.setText(this.answerList.get(1));
        this.thirdButton.setText(this.answerList.get(2));
        this.fourthButton.setText(this.answerList.get(3));
    }

    public void answerButtonClick(View view){
        this.answerList.clear();
        Button choosenAnswer = findViewById(view.getId());
        if(choosenAnswer.getText().toString() == this.celebrityNameList.get(this.answerIndex)){
            Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "False", Toast.LENGTH_SHORT).show();
        }
        this.celebrityImageList.remove(this.answerIndex);
        this.celebrityNameList.remove(this.answerIndex);
        play();
    }
}
