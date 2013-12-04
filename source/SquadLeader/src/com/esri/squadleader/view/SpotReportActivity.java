package com.esri.squadleader.view;

import java.util.GregorianCalendar;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.esri.militaryapps.model.SpotReport;
import com.esri.militaryapps.model.SpotReport.Equipment;
import com.esri.militaryapps.model.SpotReport.Size;
import com.esri.militaryapps.model.SpotReport.Unit;
import com.esri.squadleader.R;

public class SpotReportActivity extends ActionBarActivity {
    
    public static final String MGRS_EXTRA_NAME = "MgrsExtra";
    public static final String SPOT_REPORT_EXTRA_NAME = "SpotReportExtra";
    
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
            Size size = (Size) ((Spinner) findViewById(R.id.spinner_spotrep_size)).getSelectedItem();
            SpotReport.Activity activity = (SpotReport.Activity) ((Spinner) findViewById(R.id.spinner_spotrep_activity)).getSelectedItem();
            String locationMgrs = ((EditText) findViewById(R.id.editText_spotrep_location)).getText().toString();
            Unit unit = (Unit) ((Spinner) findViewById(R.id.spinner_spotrep_unit)).getSelectedItem();
            DatePicker datePicker = ((DatePicker) findViewById(R.id.datepicker_spotrep_date));
            TimePicker timePicker = ((TimePicker) findViewById(R.id.timepicker_spotrep_time));
            GregorianCalendar time = new GregorianCalendar(
                    datePicker.getYear(),
                    datePicker.getMonth(),
                    datePicker.getDayOfMonth(),
                    timePicker.getCurrentHour(),
                    timePicker.getCurrentMinute());
            Equipment equipment = (Equipment) ((Spinner) findViewById(R.id.spinner_spotrep_equipment)).getSelectedItem();

            SpotReport spotReport = new SpotReport(size, activity, 0, 0, 0, unit, time, equipment);
            getIntent().putExtra(getPackageName() + "." + SPOT_REPORT_EXTRA_NAME, spotReport);
            getIntent().putExtra(getPackageName() + "." + MGRS_EXTRA_NAME, locationMgrs);
            setResult(RESULT_OK, getIntent());
        //Stop the home/up button from restarting the parent activity
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
        
    }

}
