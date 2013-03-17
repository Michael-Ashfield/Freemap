package freemap.hikar;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.view.MenuItem;


public class Hikar extends Activity 
{
    LocationProcessor locationProcessor;
    ViewFragment viewFragment;
    HUD hud;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hud=new HUD(this);
        setContentView(R.layout.activity_main);
        addContentView(hud, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        viewFragment = (ViewFragment)getFragmentManager().findFragmentById(R.id.view_fragment);        
    }

   
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
   
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem item = menu.findItem(R.id.menu_calibrate);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean retcode=false;
        
        switch(item.getItemId())
        {
            case R.id.menu_calibrate:
                viewFragment.toggleCalibrate();
                item.setTitle(item.getTitle().equals("Calibrate") ? "Stop calibrating": "Calibrate");
                retcode=true;
                break;
        }
        return retcode;
    }
    
    public void onPause()
    {
        super.onPause();
    }
    
    public HUD getHUD()
    {
        return hud;
    }
}
