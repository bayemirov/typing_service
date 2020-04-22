package com.example.wifiinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.text.*;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.text.*;

public class MainActivity extends AppCompatActivity {

    final double beta = 0.85;
    final int K = 10;
    final int topCnt = 10;
    final List<String> stopwords = Arrays.asList(
            "i", "me", "my", "myself", "we", "our",
            "ours", "ourselves", "you", "your", "yours",
            "yourself", "yourselves", "he", "him", "his",
            "himself", "she", "her", "hers", "herself",
            "it", "its", "itself", "they", "them", "their",
            "theirs", "themselves", "what", "which", "who",
            "whom", "this", "that", "these", "those", "am",
            "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "having", "do", "does",
            "did", "doing", "a", "an", "the", "and", "but",
            "if", "or", "because", "as", "until", "while",
            "of", "at", "by", "for", "with", "about", "against",
            "between", "into", "through", "during", "before",
            "after", "above", "below", "to", "from", "up", "down",
            "in", "out", "on", "off", "over", "under", "again",
            "further", "then", "once", "here", "there", "when",
            "where", "why", "how", "all", "any", "both", "each",
            "few", "more", "most", "other", "some", "such", "no",
            "nor", "not", "only", "own", "same", "so", "than", "too",
            "very", "s", "t", "can", "will", "just", "don", "should", "now");

    EditText userInput;
    TextView content;
    TextView headline;
    Button startButton, learnButton, genButton;
    String contentString, initString;
    long[] dtime = new long[500];
    int offset = 0;
    Graph graph = new Graph();
    Bot bot = new Bot();
    long startTime, dLength;

    public static final String mPath = "data.txt";
    private QuoteBank mQuoteBank;
    private List<String> mLines;

    private Map<String, Integer> WPM = new HashMap<String, Integer>();
    private Map<String, Boolean> used = new HashMap<String, Boolean>();

    private String clearData(String data) {
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(data);
        int start = iterator.first();
        String res = "";
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            res += data.substring(start,end) + "\n";
        }
        return res;
    }

    private void updateTextRank(Graph graph) {
        long startTime = System.currentTimeMillis();
        String currentNode = graph.getRandomNode();
        if (currentNode.equals("x"))
            return;
        while(System.currentTimeMillis() - startTime <= 5000) {
            graph.increaseRank(currentNode); // We update the Rank on each time we visit new node
            if (Math.random() <= beta) {
                currentNode = graph.getRandomNode(); // Separating Gigantic path into smaller one
            }
            currentNode = graph.getNextRandomNeighbour(currentNode); // Probablistic pick based on WPM
        }
    }

    private void updateWPM() {
        // Update WPM map
        int prefset = 0;
        while(initString.equals("") == false) {
            String ar[] = initString.split(" ", 2);
            String curWord = ar[0];
            String rest = "";
            if (initString.contains(" "))
                rest = ar[1];
            for (int i = 0; i < curWord.length(); i++)
                for (int j = i + 1; j < curWord.length(); j++)
                    WPM.put(initString.substring(i, j + 1), (int)((double) (j - i + 1) / (((double) (dtime[prefset + j] - startTime) / 60000.0))));
            initString = rest;
            startTime = dtime[prefset + curWord.length()];
            prefset += curWord.length() + 1;
        }
    }

    private void updateBotWPM(String initString) {
        // Update WPM map
        int prefset = 0;
        while(initString.equals("") == false) {
            String ar[] = initString.split(" ", 2);
            String curWord = ar[0];
            String rest = "";
            if (initString.contains(" "))
                rest = ar[1];
            for (int i = 0; i < curWord.length(); i++)
                for (int j = i + 1; j < curWord.length(); j++) {
                    String x = initString.substring(i, j + 1);
                    if (WPM.containsKey(x))
                        continue;
                    WPM.put(initString.substring(i, j + 1), 200);
                }
            initString = rest;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = (EditText) findViewById(R.id.editText);
        content = (TextView) findViewById(R.id.textView);
        headline = (TextView) findViewById(R.id.textView2);
        startButton = (Button) findViewById(R.id.button);
        learnButton = (Button) findViewById(R.id.LEARN);
        genButton = (Button) findViewById(R.id.GENERATE);

        mQuoteBank = new QuoteBank(this);
        String data = mQuoteBank.readLine(mPath);

        graph.initNodes(clearData(data));
        /*WPM = bot.WPM;
        graph.updateWPM(bot.WPM);
        updateTextRank(graph);

        List<Integer> botres = new ArrayList<Integer>();

        for (int j = 1; j <= 20; j++) {
            String[] resultText = graph.getTopRankedNodes(1, used);
            contentString = "";
            for (int i = 0; i < resultText.length; i++) {
                used.put(resultText[i], true);
                contentString += (contentString.length() > 0) ? " " + resultText[i] : resultText[i];
            }
            updateBotWPM(contentString);
            Integer wpm = 0, cnt = 0;
            for (Map.Entry<String, Integer> entry : WPM.entrySet()) {
                int lastIndex = 0;
                int count = 0;
                while (lastIndex != -1) {
                    lastIndex = contentString.indexOf(entry.getKey(), lastIndex);
                    if (lastIndex != -1) {
                        count++;
                        lastIndex += entry.getKey().length();
                    }
                }
                wpm += entry.getValue() * count;
                cnt += count;
            }
            Integer speed = 0;
            if (cnt > 0)
                speed = wpm / cnt;
            botres.add(speed);
            System.out.println(j + " --- " + speed);
            graph.updateWPM(WPM);
            updateTextRank(graph);
        }

        Collections.reverse(botres);
        System.out.println("[res]");
        for (Integer w: botres)
            System.out.println(w);*/

        contentString = "sample";

        userInput.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String ar[] = contentString.split(" ", 2);
                String curWord = ar[0];
                String curStr = userInput.getText().toString();
                if (curStr.equals("") || contentString.equals(""))
                    return;
                if (curStr.equals(contentString)) {
                    dtime[offset + curStr.length() - 1] = System.currentTimeMillis();
                    contentString = "";
                    content.setText(contentString);
                    userInput.setText("");
                    headline.setText("Your speed (WPM): " + (int)((double) (initString.length()) / (((double) (System.currentTimeMillis() - startTime) / 60000.0))));
                    updateWPM();
                    graph.updateWPM(WPM);
                } else if (curStr.equals(curWord + " ")) {
                    dtime[offset + curStr.length() - 1] = System.currentTimeMillis();
                    offset += curWord.length() + 1;
                    contentString = ar[1];
                    content.setText(contentString);
                    userInput.setText("");
                    headline.setText("Your speed (WPM): " + (int)((double) (offset) / (((double) (System.currentTimeMillis() - startTime) / 60000.0))));
                } else if (curWord.startsWith(curStr)) {
                    dtime[offset + curStr.length() - 1] = System.currentTimeMillis();
                }
            }
        });

        startButton.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
                startTime = System.currentTimeMillis();
                contentString = contentString.replace("\n", " ").replace("\r", " ");
                content.setText(contentString);
                initString = contentString;
                dLength = contentString.length();
                offset = 0;
            }
        });

        learnButton.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
                updateTextRank(graph); // Update Ranks of the graph
            }
        });

        genButton.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
                String[] resultText = graph.getTopRankedNodes(1, used);
                if (contentString.equals("sample")/* || Math.random() <= 0.20*/) {
                    resultText = graph.getAbsRandomNodes(1, used);
                } else
                    contentString = "";
                for (int i = 0; i < resultText.length; i++) {
                    used.put(resultText[i], true);
                    if (resultText[i].substring(resultText[i].length() - 1).equals(" "))
                        resultText[i] = resultText[i].substring(0, resultText[i].length() - 1);
                    contentString += (contentString.length() > 0) ? " " + resultText[i] : resultText[i];
                }
            }
        });
    }

}
