package com.murach.tipcalculator;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

public class TipCalculatorActivity extends Activity 
implements OnEditorActionListener, OnClickListener {

    // define variables for the widgets
    private EditText billAmountEditText;
    private TextView percentTextView;   
    private Button   percentUpButton;
    private Button   percentDownButton;
    private Button   buttonSaveTip;
    private TextView tipTextView;
    private TextView totalTextView;
    
    // define instance variables that should be saved
    private String billAmountString = "";
    private float tipPercent = .15f;
    private final String TAG = "com.murach.tipcalculator";
    private long lastTipID;
    
    // set up preferences
    private SharedPreferences prefs;

    // set up a tips database
    private TipsDB tipsDB;


    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);
        
        // get references to the widgets
        billAmountEditText = (EditText) findViewById(R.id.billAmountEditText);
        percentTextView = (TextView) findViewById(R.id.percentTextView);
        percentUpButton = (Button) findViewById(R.id.percentUpButton);
        percentDownButton = (Button) findViewById(R.id.percentDownButton);
        buttonSaveTip = (Button) findViewById(R.id.buttonSaveTip);
        tipTextView = (TextView) findViewById(R.id.tipTextView);
        totalTextView = (TextView) findViewById(R.id.totalTextView);

        // set the listeners
        billAmountEditText.setOnEditorActionListener(this);
        percentUpButton.setOnClickListener(this);
        percentDownButton.setOnClickListener(this);
        buttonSaveTip.setOnClickListener(this);


        
        // get default SharedPreferences object
        prefs = PreferenceManager.getDefaultSharedPreferences(this);        
    }
    
    @Override
    public void onPause() {
        // save the instance variables       
        Editor editor = prefs.edit();        
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.commit();        

        super.onPause();      
    }
    
    @SuppressLint("LongLogTag")
    @Override
    public void onResume() {
        super.onResume();
        
        // get the instance variables
        billAmountString = prefs.getString("billAmountString", "");
        tipPercent = prefs.getFloat("tipPercent", 0.15f);

        // set the bill amount on its widget
        billAmountEditText.setText(billAmountString);
        
        // calculate and display


        tipsDB = new TipsDB(this);
        ArrayList<Tip> tipsList = tipsDB.getTips();

        for (Tip tip : tipsList)
        {
            Log.i(TAG, "Tip Data: " + tip.getId() + " " + tip.getDateMillis() + "  $" +
                    tip.getBillAmount() + " " + tip.getTipPercent());
        }
        Tip lastTip = tipsDB.getLastTipDate();
        Log.i("com.lull.TipCalculator", "Last Tip Date: " + lastTip.getDateStringFormatted());
        Log.i("com.lull.TipCalculator", "Average Tip Percent: %" + (tipsDB.setAvgTipPercent() * 100));
        calculateAndDisplay();

    }
    
    public void calculateAndDisplay() {        

        // get the bill amount
        billAmountString = billAmountEditText.getText().toString();
        float billAmount; 
        if (billAmountString.equals("")) {
            billAmount = 0;
        }
        else {
            billAmount = Float.parseFloat(billAmountString);
        }
        
        // calculate tip and total 
        float tipAmount = billAmount * tipPercent;
        float totalAmount = billAmount + tipAmount;
        
        // display the other results with formatting
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));
        
        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTextView.setText(percent.format(tipPercent));
    }
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
    		actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }        
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.percentDownButton:
            tipPercent = tipPercent - .01f;
            calculateAndDisplay();
            break;
        case R.id.percentUpButton:
            tipPercent = tipPercent + .01f;
            calculateAndDisplay();
            break;
            case R.id.buttonSaveTip:
                Tip newTip = saveTip();
                if (newTip == null){
                    break;
                }
                else{
                    tipsDB.saveTip(newTip);
                    billAmountEditText.setText("");
                }
                tipPercent = tipsDB.setAvgTipPercent();
                calculateAndDisplay();
                break;
            default:
        }
    }

    public Tip saveTip(){
        Tip newTip;
        if (billAmountEditText.getText().toString().equals("")){
            Toast.makeText(this, "Please enter a Bill Amount before Saving Tips", Toast.LENGTH_LONG).show();
            newTip = null;
        }
        else{
            newTip = new Tip();
            newTip.setBillAmount(Float.parseFloat(billAmountEditText.getText().toString()));
            newTip.setTipPercent(tipPercent);
            newTip.setDateMillis(System.currentTimeMillis());
        }
        return newTip;
    }
}