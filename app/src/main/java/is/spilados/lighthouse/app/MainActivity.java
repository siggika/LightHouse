package is.spilados.lighthouse.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.hardware.ConsumerIrManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

/*
*   Layout Related
* */
    private TextView mSensTxt;
    private TextView mIrEmTxt;
    private TextView mLightTxt;
    private TextView mIrStream;
    private RelativeLayout mLayout;

/*
*   Light sensor
*   The goal of the light sensor is to read IR-signals from any IR-remote and convert
*   to a broadcast signal for the embedded IR-blaster of any android device.
* */

    private Sensor mLight;
    private SensorManager mSensorMgr;
    private List<String> irCode = new ArrayList<String>(); //TODO: gather ir code from light sensor here
    private int readCounter = 0; // for debugging purposes
    private float maxRead = 0;
    private int sampleFrequency = 100000;

/*
*   IR-blaster
*   The goal of the IR-blaster is to emmit IR-codes from read IR-signals that were
*   picked up by the light sensor
* */
    private ConsumerIrManager mEmitterMgr;

    /*
    * Use:
    * Before:
    * After:
    * */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);

        mEmitterMgr = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);

        mLayout = (RelativeLayout) findViewById(R.id.layout);
        mSensTxt = (TextView) findViewById(R.id.lSens);
        mIrEmTxt = (TextView) findViewById(R.id.irEm);
        mLightTxt = (TextView) findViewById(R.id.light);
        mIrStream = (TextView) findViewById(R.id.irStream);

        brightness(Color.BLACK,
                Color.WHITE,
                "0");
        if(mSensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            mSensTxt.setText("This device has an light sensor, congrats!");
        } else {
            mSensTxt.setText("This device does not have an light sensor, sorry...");
            Toast.makeText(this,
                    "Light Sensor not present, reading capabilities are disabled...",
                    Toast.LENGTH_SHORT).show(); //TODO: disable light sensor capabilities
        }
        if(mEmitterMgr.hasIrEmitter()){
            mIrEmTxt.setText("This device has an IR emitter, congrats!");
        } else {
            mSensTxt.setText("This device does not have an IR emitter, sorry...");
            Toast.makeText(this,
                    "IR emitter not present, sending capabilities are disabled...",
                    Toast.LENGTH_SHORT).show(); //TODO: disable IR emitter capabilities
        }
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    private void brightness(int bground, int txt, String lux) {

        mLightTxt.setBackgroundColor(bground);
        mLightTxt.setTextColor(txt);
        mLightTxt.setText("LUX: " + lux + ".\t Signal count: " + readCounter);
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        brightness(Color.TRANSPARENT,
                Color.BLACK,
                String.valueOf(0));
        return true;
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1){

        //TODO: Auto-generated method stub
        //TODO: Executes if the sensor turns of for some reason, stop reading
        // resetReader();
        lightSensorSwitch(findViewById(R.id.switchLight));
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    @Override
    public void onSensorChanged(SensorEvent event){

        //The light sensor returns one value, witch is the ambient light level
        float luxf = event.values[0];
        short lux = (short)luxf;

        if(lux <= 5){ //TODO: if light is below threshold, return 0 lux(blank frequency)
            brightness(Color.RED,
                    Color.WHITE,
                    String.valueOf(lux));
        } else { //TODO: if light is present, return lux value
            brightness(Color.GREEN,
                    Color.BLACK,
                    String.valueOf(lux));
            if(maxRead < lux){
                maxRead = lux;
            }
            irCode.add(String.valueOf(lux));
            readCounter++;
        }
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    @Override
    protected void onResume(){

        super.onResume();
        mSensorMgr.registerListener(this,
                mLight,
                sampleFrequency);// back to SENSOR_DELAY_FASTEST or SensorManager.SENSOR_DELAY_UI
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    @Override
    protected void onPause(){

        super.onPause();
        mSensorMgr.unregisterListener(this);
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    public void lightSensorSwitch(View view) {

        boolean on = ((Switch) view).isChecked();

        if(sampleFrequency > SensorManager.SENSOR_DELAY_FASTEST) {
            sampleFrequency = sampleFrequency - 10000;
        }

        if (on) {
            onResume();
        } else {
            onPause();
        }
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    public void displayCode(View view) {

        readCounter = 0;
        maxRead = 0;
        mIrStream.setText("");
        Iterator<String> iters = irCode.iterator();
        while(iters.hasNext()){
            String reading = iters.next();
            mIrStream.append(reading+",");
        }
        irCode.clear();
    }

    /*
    * Use:
    * Before:
    * After:
    * */
    public void emitIRsignal(View view) {
        Toast.makeText(this,
                "SEND CODE ",
                Toast.LENGTH_SHORT).show();

        int[] a = new int[10];
        for(int i = 0; i < 10; i++){
            a[i] = 1000 * i;
        }
        Toast.makeText(this,
                "SENT CODE: "+a[0],
                Toast.LENGTH_SHORT).show();

    }
}