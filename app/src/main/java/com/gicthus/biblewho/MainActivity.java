package com.gicthus.biblewho;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ScrollView ;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;


    String measure = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
    String introA =  null;


    String[] nam = new  String[500] ; //Short name like John
    String[] nameLong = new  String[500] ; // Long name like John the Baptist
    int[] testament = new  int[500] ;
    int[] difficulty = new  int[500] ;
    int[] namIndex = new  int[500] ;
    String[] allNames = new  String[500] ;
    String[] fakeName = new  String[600] ;
    String[] clue = new  String[20] ;  //clues for one person
    String[] ans = new  String[5] ; // The multiple choices


//All the orderings of 4 multiple choices
// Could not get 2D array to work so using 1D array


    int[] ord = {0,1,2,3,4,
            1,2,4,3,
            1,3,2,4,
            1,3,4,2,
            1,4,2,3,
            1,4,3,2,
            2,1,3,4,
            2,1,4,3,
            2,3,1,4,
            2,3,4,1,
            2,4,1,3,
            2,4,3,1,
            3,1,2,4,
            3,1,4,2,
            3,2,1,4,
            3,2,4,1,
            3,4,1,2,
            3,4,2,1,
            4,1,2,3,
            4,1,3,2,
            4,2,1,3,
            4,2,3,1,
            4,3,1,2,
            4,3,2,1} ;

    int nc = 0; // # of clues processed
    int nm =0; // Index of the person being guessed
    int nn =0; // No. of Bible names
    int ng =0; // No. of names in set being guessed
    int nf =0; // No. of fake names
    int na =0; // the answer 1-4
    int ic =0; // Index of the chosen clue
    int ntried=0; // No. of questions tried
    int nright=0; //No you got right
    int settingTestament =2 ; // 0=OT, 1=NT, 2=Whole Bible
    int settingHowHard =3 ; // 1-easy, 2=easy+medium, 3=easy, medium, & hard

    File fileEvents = null;
    BufferedReader br = null;

    public final static String EXTRA_MESSAGE = "com.gicthus.biblewho.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = getWindow().findViewById(Window.ID_ANDROID_CONTENT);

    // Get all bible names to use as wrong answers
        try {
            String tempStr = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("nameOnly.txt")));
            int i = 1;
            tempStr = reader.readLine();
            while (tempStr != null) {
                allNames[i] = tempStr;
                i++;
                tempStr = reader.readLine();
            }
            nn=i-1;
            reader.close();
        } catch (Exception e){
            String you = "are here";
        }

        // Get fake names to use as wrong answers
        try {
            String tempStr = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("fakeNames.txt")));
            int i = 1;
            tempStr = reader.readLine();
            while (tempStr != null) {
                fakeName[i] = tempStr;
                i++;
                tempStr = reader.readLine();
            }
            nf=i-1;
            reader.close();
        } catch (Exception e){
            String you = "are here";
        }


        getSettings(view);

    }

    public void getSettings(View view) {

        // Retrieve  settings
        String inline1 = null;
        String inline2 = null;

        try {
            File myDir = getFilesDir();
            String myPath = myDir.getPath();
            fileEvents = new File(myPath + "/settings.txt");
            br = new BufferedReader(new FileReader(fileEvents));
            inline1 = br.readLine();
            inline2 = br.readLine();
            br.close();

            if (inline1.contains("0")) {
                settingTestament = 0;
            } else if (inline1.contains("1")) {
                settingTestament = 1;
            } else {
                settingTestament = 2;
            }

            if (inline2.contains("1")) {
                settingHowHard = 1;
            } else if (inline2.contains("2")) {
                settingHowHard = 2;
            } else {
                settingHowHard = 3;
            }
        } catch (Exception e) {
            // No settings file so set defaults;
            settingTestament = 2;
            settingHowHard = 3;
        }
        intro(view);
    }
    public void intro(View view){


        String credits  =
                "This app uses information from the www.aboutbibleprophecy.com ";
        credits = credits +
                "website.  It was was created by GICTHUS.inc.  Your feedback and comments would be appreciated via email at gicthus.inc@gmail.com.";
        credits = credits + "\n\nDo you want to play a game?";
        TextView  tv1 = findViewById(R.id.textView1);
        tv1.setText(credits);
        tv1.setVisibility(View.VISIBLE);

        // Get  names based on testament and difficulty
        try {
            String tempStr = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("namesRanked.txt")));
            int i = 1;
            tempStr = reader.readLine();
            while (tempStr != null) {
                String[] arrSplit = tempStr.split("#");
                int ntest = Integer.valueOf(arrSplit[1]);
                int howHard = Integer.valueOf(arrSplit[2]);
                if ((ntest==settingTestament || settingTestament==2) &&
                        (howHard <= settingHowHard)){
                    namIndex[i] = Integer.valueOf(arrSplit[0]);
                    testament[i] = ntest;
                    difficulty[i] = howHard;
                    nameLong[i] = arrSplit[3]; //short name
                    nam[i] = arrSplit[4]; //short name
                    i++;
                }

                tempStr = reader.readLine();
            }
            ng = i-1;
            reader.close();
        } catch (Exception e){
            String you = "are here";
        }

         TextView tv = new TextView(this);

        //tv.setTextSize();
        tv.setText(introA);
        LinearLayout child = (LinearLayout)findViewById( R.id.child);
        ScrollView  scrollView = findViewById(R.id.sv);
        scrollView.setVisibility(View.VISIBLE);
        Button button = findViewById(R.id.submit);
        button.setVisibility(View.GONE);

        // Hide settings objects
        RadioGroup rg1 = findViewById(R.id.radioOTNT);
        rg1.setVisibility(View.GONE);
        TextView line = findViewById(R.id.line);
        line.setVisibility(View.GONE);
        RadioGroup rg2 = findViewById(R.id.radioHowHard);
        rg2.setVisibility(View.GONE);
        Button b1 = findViewById(R.id.save);
        b1.setVisibility(View.GONE);
        Button cancel = findViewById(R.id.cancel);
        cancel.setVisibility(View.GONE);
        Button reset = findViewById(R.id.reset);
        reset.setVisibility(View.GONE);

        try {
            // scrollView.removeAllViews();
            child.removeAllViews();
            child.addView(tv);
        } catch (Exception ex) {
            String errmsg = ex.getMessage();
        }



//        intro.setText(introA);
//        intro.setVisibility(View.VISIBLE);  // for now
        Button btn = findViewById(R.id.chSettings);
        btn.setVisibility(View.GONE);
        btn = findViewById(R.id.exit);
        btn.setVisibility(View.GONE);
        RadioGroup rg = findViewById(R.id.radioAns);
        rg.setVisibility(View.GONE);

        GridLayout yesNoStart = findViewById(R.id.yesNoStart);
        yesNoStart.setVisibility(View.VISIBLE);

    }

    public void yesStart(View view){
//        TextView intro = findViewById(R.id.intro);
//        intro.setVisibility(View.GONE);
        GridLayout yesNoStart = findViewById(R.id.yesNoStart);
        yesNoStart.setVisibility(View.GONE);

        // Retrieve  settings
//        String  inline = null;
//        try{
//            File myDir = getFilesDir();
//            String myPath = myDir.getPath();
//            fileEvents = new File(myPath+"/settings.txt");
//            br = new BufferedReader(new FileReader(fileEvents));
//            inline = br.readLine();
            /********************************
            if (inline.startsWith("O")){
                onlyNT=true;
            } else {
                onlyNT=false;
            }
            inline = br.readLine();
            if (inline.startsWith("T")){
                minorProphsAs1=true;
            } else {
                minorProphsAs1=false;
            }
            *******************************/

//        }catch (Exception e) {
//        }


    }

    public void settings(View view){

        //Hide objects on the main page
        GridLayout yesNoStart = findViewById(R.id.yesNoStart);
        yesNoStart.setVisibility(View.GONE);
        Button obj1 = findViewById(R.id.chSettings);
        obj1.setVisibility(View.GONE);
        ScrollView obj2 = findViewById(R.id.sv);
        obj2.setVisibility(View.GONE);
        Button obj3 = findViewById(R.id.submit);
        obj3.setVisibility(View.GONE);
        RadioGroup obj4 = findViewById(R.id.radioAns);
        obj4.setVisibility(View.GONE);
        TextView obj5 = findViewById(R.id.textView2);
        obj5.setVisibility(View.GONE);
        LinearLayout obj6 = findViewById(R.id.child);
        obj6.setVisibility(View.GONE);
        Button obj7 = findViewById(R.id.yesStart);
        obj7.setVisibility(View.GONE);
        Button obj8 = findViewById(R.id.noStart);
        obj8.setVisibility(View.GONE);
        Button exit = findViewById(R.id.exit);
        exit.setVisibility(View.GONE);

        // Set radio buttons to display current settings


        RadioGroup rg = findViewById(R.id.radioOTNT);
        rg.setVisibility(View.VISIBLE);
        TextView line = findViewById(R.id.line);
        line.setText("-----------------------------------");
        line.setVisibility(View.VISIBLE);

        RadioGroup rg2 = findViewById(R.id.radioHowHard);
        rg2.setVisibility(View.VISIBLE);
//        cb.setVisibility(View.VISIBLE);
        Button save = findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);
        Button cancel = findViewById(R.id.cancel);
        cancel.setVisibility(View.VISIBLE);
        Button reset = findViewById(R.id.reset);
        reset.setVisibility(View.VISIBLE);

        // Set OT/NT radio buttons to display current settings
        RadioButton nt = findViewById(R.id.radioNT);
        RadioButton ot = findViewById(R.id.radioOT);
        RadioButton both = findViewById(R.id.radioAll);

        if (settingTestament == 0){
            nt.setChecked(false);
            ot.setChecked(true);
            both.setChecked(false);
        } else if (settingTestament == 1){
            nt.setChecked(true);
            ot.setChecked(false);
            both.setChecked(false);
        } else {
            nt.setChecked(false);
            ot.setChecked(false);
            both.setChecked(true);
        }

        // Set howHard radio buttons to display current settings
        RadioButton easy = findViewById(R.id.radioEasy);
        RadioButton medium = findViewById(R.id.radioMedium);
        RadioButton hard = findViewById(R.id.radioHard);

        if (settingHowHard == 1){
            easy.setChecked(true);
            medium.setChecked(false);
            hard.setChecked(false);
        } else if (settingHowHard == 2){
            easy.setChecked(false);
            medium.setChecked(true);
            hard.setChecked(false);
        } else {
            easy.setChecked(false);
            medium.setChecked(false);
            hard.setChecked(true);
        }

    }

    public void saveSettings(View view){

        String testamentStr = "2";

        // Save the settings

        // get selected radio button from OT/NT radioGroup
        RadioGroup rg = findViewById(R.id.radioOTNT);
        int selectedId = rg.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        RadioButton selectedButton = findViewById(selectedId);
        String selectedText = (String) selectedButton.getText();

        if (selectedText.contains("whole")){
            testamentStr="2";
        }
        if (selectedText.contains("New")){
            testamentStr="1";
        }
        if (selectedText.contains("Old")){
            testamentStr="0";
        }

        // get selected radio button from how hard radioGroup
        RadioGroup rg2 = findViewById(R.id.radioHowHard);
        int selectedId2 = rg2.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        RadioButton selectedButton2 = findViewById(selectedId2);
        String selectedText2 = (String) selectedButton2.getText();

        String howHard = "3";
        if (selectedText2.contains("Just easy")){
            howHard="1";
        }
        if (selectedText2.contains("Medium and easy")){
            howHard="2";
        }


        // Write settings
        try {

            OutputStream os = openFileOutput("settings.txt", MODE_PRIVATE);
            os.write( testamentStr.getBytes());
            os.write( "\n".getBytes());
            os.write( howHard.getBytes());
            os.close();
        } catch (Exception e2) {

        }

    // Hide the settings objects

         rg = findViewById(R.id.radioOTNT);
        rg.setVisibility(View.GONE);
//        cb.setVisibility(View.VISIBLE);
        Button save = findViewById(R.id.save);
        save.setVisibility(View.GONE);
        Button cancel = findViewById(R.id.cancel);
        cancel.setVisibility(View.GONE);
        Button reset = findViewById(R.id.reset);
        reset.setVisibility(View.GONE);

        //Hide the score
        TextView tv2 = findViewById(R.id.textView2);
        tv2.setVisibility(View.GONE);

    // Make the quiz objects visible
        GridLayout yesNoStart = findViewById(R.id.yesNoStart);
        yesNoStart.setVisibility(View.VISIBLE);
        Button obj1 = findViewById(R.id.chSettings);
        obj1.setVisibility(View.VISIBLE);
        ScrollView obj2 = findViewById(R.id.sv);
        obj2.setVisibility(View.VISIBLE);
        Button obj3 = findViewById(R.id.submit);
        obj3.setVisibility(View.VISIBLE);
        RadioGroup obj4 = findViewById(R.id.radioAns);
        obj4.setVisibility(View.VISIBLE);
        LinearLayout obj6 = findViewById(R.id.child);
        obj6.setVisibility(View.VISIBLE);
        Button obj7 = findViewById(R.id.yesStart);
        obj7.setVisibility(View.VISIBLE);
        Button obj8 = findViewById(R.id.noStart);
        obj8.setVisibility(View.VISIBLE);
        Button exit = findViewById(R.id.exit);
        exit.setVisibility(View.VISIBLE);

        getSettings(view);
    }

    public void systemExit (View view){
        System.exit(0);
    }
    public void cancelSettings (View view){
        //Exit from the settings page without making any changes.
        // Hide the settings objects

       RadioGroup rg = findViewById(R.id.radioOTNT);
        rg.setVisibility(View.GONE);
//        cb.setVisibility(View.VISIBLE);
        Button save = findViewById(R.id.save);
        save.setVisibility(View.GONE);
        Button cancel = findViewById(R.id.cancel);
        cancel.setVisibility(View.GONE);
        Button reset = findViewById(R.id.reset);
        reset.setVisibility(View.GONE);

        TextView tv2 = findViewById(R.id.textView2);
        tv2.setVisibility(View.GONE);

        // Make the quiz objects visible
        GridLayout yesNoStart = findViewById(R.id.yesNoStart);
        yesNoStart.setVisibility(View.VISIBLE);
        Button obj1 = findViewById(R.id.chSettings);
        obj1.setVisibility(View.VISIBLE);
        ScrollView obj2 = findViewById(R.id.sv);
        obj2.setVisibility(View.VISIBLE);
        Button obj3 = findViewById(R.id.submit);
        obj3.setVisibility(View.VISIBLE);
        RadioGroup obj4 = findViewById(R.id.radioAns);
        obj4.setVisibility(View.VISIBLE);
        LinearLayout obj6 = findViewById(R.id.child);
        obj6.setVisibility(View.VISIBLE);
        Button obj7 = findViewById(R.id.yesStart);
        obj7.setVisibility(View.VISIBLE);
        Button obj8 = findViewById(R.id.noStart);
        obj8.setVisibility(View.VISIBLE);
        Button exit = findViewById(R.id.exit);
        exit.setVisibility(View.VISIBLE);

        intro(view);
    }

    public void sendMessage(View view) {


        //Remove the credits
        TextView  tv1 = findViewById(R.id.textView1);
        tv1.setVisibility(View.GONE);


        Random randomGenerator = new Random();
        String verse1 = null;

        // Retrieve  score

            File myDir = getFilesDir();
            String myPath = myDir.getPath(); // Path to the highScores.txt file
            fileEvents = new File(myPath+"/score.txt");
        try {
            br = new BufferedReader(new FileReader(fileEvents));
            try {
                nright = Integer.valueOf(br.readLine());
                ntried = Integer.valueOf(br.readLine());
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            ntried=0;
            nright=0;
        }


        TextView tv = new TextView(this);
         nc++;
         // Get random name
         nm = randomGenerator.nextInt(ng)+1;
         //nm=95;
         //verse1 = "Random name # "+nv+" is " + nam[nv] +"\n";

        // Get clue for the randomly chosen person

        //Choose one of the 24 possible orderings of the 4 choices
        na = randomGenerator.nextInt(23)+1;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("indexed.txt")));
            String tempStr = reader.readLine();
            while (tempStr != null) {
                if(tempStr.contains(" "+Integer.toString(namIndex[nm] )+"{")){
                    verse1=tempStr;
                    break;
                }
                tempStr = reader.readLine();
            }
            if (tempStr==null){
                verse1 = "Could not find nm="+nm;
            } else {
                // Get all clues and choose one at random
                int i = 1;
                tempStr = reader.readLine();
                while (!tempStr.contains("{") && tempStr != null) {

                    clue[i] = tempStr.replaceAll(nam[nm], "_____");
                    i++;
                    tempStr = reader.readLine();

                }
                nc=i-1;
                //If there is more than one clue, pick one at random
                    ic = randomGenerator.nextInt(nc)+1;

//                verse1 = "ord id = " + na + "ord1-4=" +  ord[4*na+1] +""+ ord[4*na+2] +""+ ord[4*na+3] +""+ ord[4*na+4]+" ";
//                verse1="nm="+nm+" nam="+nam[nm];
                verse1= clue[ic] + "\n";
            }
            reader.close();
        }
        catch (Exception e){

        }

        //Get wrong answers


        //Make an array of multiple choices with 1 = right answer, 2 = fake name
        //and 3,4 = wrong bible names

        ans[1]=nam[nm];
        ans[2]=fakeName[randomGenerator.nextInt(nf)+1];

        //Make sure randomly chosen Bible name is not the right answer
       String tempStr = nam[randomGenerator.nextInt(ng)+1];
//        String tempStr = allNames[randomGenerator.nextInt(nn)+1];
        while (tempStr.equals(nam[nm])) {
            //tempStr = allNames[randomGenerator.nextInt(nn)+1];
            tempStr = nam[randomGenerator.nextInt(ng)+1];
        }
        ans[3]= tempStr;

        //tempStr = allNames[randomGenerator.nextInt(nn)+1];
        tempStr = nam[randomGenerator.nextInt(ng)+1];
        while (tempStr.equals(nam[nm]) || tempStr.equals(ans[3])) {
            //tempStr = allNames[randomGenerator.nextInt(nn)+1];
            tempStr = nam[randomGenerator.nextInt(ng)+1];
        }
        ans[4]= tempStr;

    // Get random ordering of the answers nord = 1-23
//        int nord = randomGenerator.nextInt(23);

        RadioButton b1 =  findViewById(R.id.radioAns1);
        try{
            b1.setText(ans[ord[na*4+1]]);
            b1.setChecked(false);
            RadioButton b2 =  findViewById(R.id.radioAns2);
            b2.setText(ans[ord[na*4+2]]);
            b2.setChecked(false);
            RadioButton b3 =  findViewById(R.id.radioAns3);
            b3.setText(ans[ord[na*4+3]]);
            b3.setChecked(false);
            RadioButton b4 =  findViewById(R.id.radioAns4);
            b4.setText(ans[ord[na*4+4]]);
            b4.setChecked(false);
            RadioButton b5 =  findViewById(R.id.radioAns5);
            b5.setChecked(true);

        } catch (Exception e){
            String you = "are here";
        }


        //verse1=measure;
        tv.setTextSize(16);
//        verse1="ans1="+ans[1]+verse1;
         tv.setText(verse1);
        LinearLayout child = (LinearLayout)findViewById( R.id.child);
        ScrollView  scrollView = findViewById(R.id.sv);
        scrollView.setVisibility(View.VISIBLE);
        // Multiple choices
        RadioGroup rg = findViewById(R.id.radioAns);
        rg.setVisibility(View.VISIBLE);

        Button btn= findViewById(R.id.chSettings);
        btn.setVisibility(View.VISIBLE);
        btn= findViewById(R.id.exit);
        btn.setVisibility(View.VISIBLE);
//        GridLayout gl = findViewById(R.id.bookCatContext);
//        gl.setVisibility(View.VISIBLE);
        btn= findViewById(R.id.submit);
        //Submit button must be inactive until an answer is chosen.
        btn.setVisibility(View.VISIBLE);
//        btn.setEnabled(false);

        GridLayout yesNoStart = findViewById(R.id.yesNoStart);
        yesNoStart.setVisibility(View.GONE);

        try {
           // scrollView.removeAllViews();
            child.removeAllViews();
            child.addView(tv);
        } catch (Exception ex) {
            String errmsg = ex.getMessage();
        }
        //tv = findViewById(R.id.textView2);
        //tv.setText(verse1);
        //String bookChapter = v + " " + chapter ;

    }
    public void checkAnswer(View view) {
        RadioGroup rg = findViewById(R.id.radioAns);
        int btnId = rg.getCheckedRadioButtonId();
        //Do nothing if no answer was picked
        if(btnId == -1){
            return;
        }
        // find the radiobutton by returned id
        RadioButton selectedButton = findViewById(btnId);
        String selectedText = (String) selectedButton.getText();

        TextView tv = findViewById(R.id.textView2);

        NumberFormat formatter = new DecimalFormat("#0.0");

        if (selectedText.equals(ans[1])) {
            // Make good sound
            MediaPlayer mp = MediaPlayer.create(this,R.raw.got_verse);
            mp.start();
            nright++;
            ntried++;
            String percent = formatter.format(100.*nright/ntried)+"%";
            tv.setText("Yes! "+nright+" right of "+ntried+" = "+ percent);
        } else {
            // Make bad sound`
            MediaPlayer mp = MediaPlayer.create(this,R.raw.zero_on_verse);
            mp.start();
            ntried++;
            String percent = formatter.format(100.*nright/ntried)+"%";
            tv.setText("Wrong. "+nright+" right of "+ntried+" = "+ percent);
        }
        tv.setVisibility(View.VISIBLE);

        // Write scores
        try {

            OutputStream os = openFileOutput("score.txt", MODE_PRIVATE);

            String tempStr = nright+ "\n";
            os.write( tempStr.getBytes());
            tempStr = ntried + "\n";
            os.write( tempStr.getBytes());
            os.close();
        } catch (Exception e2) {

        }





        sendMessage(view);
    }
    public void resetScore(View view) {
        try {

            OutputStream os = openFileOutput("score.txt", MODE_PRIVATE);

            String tempStr = "0\n";
            os.write( tempStr.getBytes());
            tempStr = "0\n";
            os.write( tempStr.getBytes());
            os.close();
        } catch (Exception e2) {

        }
        Toast.makeText(this, "Your score was reset.",
                Toast.LENGTH_LONG).show();


    }

    }
