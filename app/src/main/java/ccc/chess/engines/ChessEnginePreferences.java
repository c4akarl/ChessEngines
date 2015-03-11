package ccc.chess.engines;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class ChessEnginePreferences extends Activity
{	//>190
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		enginePrefs = getSharedPreferences("engine", 0);
		setContentView(R.layout.engineprefs);
		logOn = (CheckBox) findViewById(R.id.logOn);
		getPrefs();
		//	strength
		strengthValue = (TextView) this.findViewById(R.id.strengthValue);
		strengthValue.setText(progressStrength + "% " + getString(R.string.prefPlayingStrength));
		strength = (SeekBar) this.findViewById(R.id.strength);
		strength.setProgress(progressStrength);
		strength.setOnTouchListener(new OnTouchListener() 
		{       
	        @Override
	        public boolean onTouch(View v, MotionEvent event) 
	        {
		        switch(event.getAction())
		        {
			        case MotionEvent.ACTION_DOWN:
			        case MotionEvent.ACTION_MOVE:
			        	progressStrength = strength.getProgress();
			            strengthValue.setText(progressStrength + "% " + getString(R.string.prefPlayingStrength));
			        	break;
		        }
	        return false;
		    }
		});
	}
	@Override
    protected void onDestroy()
    {
    	super.onDestroy();
     }
	public void myClickHandler(View view) 				
    {	// ClickHandler	(ButtonEvents)
		Intent returnIntent;
		switch (view.getId()) 
		{
		case R.id.btnOk:
			setPrefs();
        	returnIntent = new Intent();
       		setResult(RESULT_OK, returnIntent);
			finish();
			break;
		}
	}
	protected void setPrefs() 
	{
		SharedPreferences.Editor ed = enginePrefs.edit();
		ed.putInt("strength", progressStrength);
        ed.putBoolean("logOn", logOn.isChecked());
        ed.commit();
	}
	protected void getPrefs() 
	{
		progressStrength = enginePrefs.getInt("strength", 100);
		logOn.setChecked(enginePrefs.getBoolean("logOn", false));
	}
	
	SharedPreferences enginePrefs;
	CheckBox logOn;
	SeekBar strength;
	TextView strengthValue;
	int progressStrength = 100;
}
