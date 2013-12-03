package com.esri.squadleader.view;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.militaryapps.model.SpotReport;
import com.esri.militaryapps.model.SpotReport.Equipment;
import com.esri.militaryapps.model.SpotReport.Size;
import com.esri.militaryapps.model.SpotReport.Unit;
import com.esri.squadleader.R;

public class SpotReportActivity extends ActionBarActivity {
    
    public static final String MGRS_EXTRA_NAME = "MgrsExtra";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_report);
        
        String mgrs = getIntent().getExtras().getString(getPackageName() + "." + MGRS_EXTRA_NAME);
        if (null != mgrs) {
            ((EditText) findViewById(R.id.editText_spotrep_location)).setText(mgrs);
        }
        
        ArrayAdapter<Size> sizeAdapter = new ArrayAdapter<Size>(this, android.R.layout.simple_spinner_item, Size.values());
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) findViewById(R.id.spinner_spotrep_size)).setAdapter(sizeAdapter);

        ArrayAdapter<SpotReport.Activity> activityAdapter = new ArrayAdapter<SpotReport.Activity>(this, android.R.layout.simple_spinner_item, SpotReport.Activity.values());
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) findViewById(R.id.spinner_spotrep_activity)).setAdapter(activityAdapter);

        ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<Unit>(this, android.R.layout.simple_spinner_item, Unit.values());
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) findViewById(R.id.spinner_spotrep_unit)).setAdapter(unitAdapter);

        ArrayAdapter<Equipment> equipmentAdapter = new ArrayAdapter<Equipment>(this, android.R.layout.simple_spinner_item, Equipment.values());
        equipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) findViewById(R.id.spinner_spotrep_equipment)).setAdapter(equipmentAdapter);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.spot_report_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                //TODO send spot report
                Toast.makeText(this, "action_send for spot report!", Toast.LENGTH_SHORT).show();
            //Stop the home/up button from restarting the parent activity
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        
    }

}
