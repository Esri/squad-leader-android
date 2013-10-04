/*******************************************************************************
 * Copyright 2013 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.squadleader.view;

import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.RestServiceReader;
import com.esri.squadleader.R;

/**
 * A dialog for adding a layer from the web.
 */
public class AddLayerFromWebDialogFragment extends DialogFragment {
    
    /**
     * A listener for this class to pass objects back to the Activity that called it.
     */
    public interface AddLayerListener {
        
        /**
         * Called when the add layer dialog retrieves valid layers based on the URL
         * specified by the user.
         * @param layerInfos the LayerInfo objects for the layers being returned. The
         *                   first layer on the list should display on top, the next layer
         *                   next until the last layer, which should display on the bottom.
         *                   Of course, you can use these LayerInfo objects in any order
         *                   desired.
         */
        public void onValidLayerInfos(LayerInfo[] layerInfos);
    }
    
    private static final String TAG = AddLayerFromWebDialogFragment.class.getSimpleName();
    
    private AddLayerListener listener = null;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AddLayerListener) {
            listener = (AddLayerListener) activity;
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null != listener) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final View inflatedView = inflater.inflate(R.layout.add_layer_from_web, null);
            builder.setView(inflatedView);
            builder.setTitle(getString(R.string.add_layer_from_web));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.add_layer, new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    final boolean useAsBasemap;
                    boolean _useAsBasemap = false;
                    View checkboxView = inflatedView.findViewById(R.id.checkBox_basemap);
                    if (null != checkboxView && checkboxView instanceof CheckBox) {
                        _useAsBasemap = ((CheckBox) checkboxView).isChecked();
                    }
                    useAsBasemap = _useAsBasemap;
                    View serviceUrlView = inflatedView.findViewById(R.id.editText_serviceUrl);
                    if (null != serviceUrlView && serviceUrlView instanceof EditText) {
                        final String urlString = ((EditText) serviceUrlView).getText().toString();
                        
                        new AsyncTask<Void, Void, LayerInfo[]>() {

                            @Override
                            protected LayerInfo[] doInBackground(Void... params) {
                                try {
                                    return RestServiceReader.readService(new URL(urlString), useAsBasemap);
                                } catch (Exception e) {
                                    Log.e(TAG, "Couldn't read and parse " + urlString, e);
                                    Toast.makeText(getActivity(), "Couldn't add layer from web: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    return null;
                                }
                            }
                            
                            @Override
                            protected void onPostExecute(LayerInfo[] layerInfos) {
                                if (null != layerInfos) {
                                    listener.onValidLayerInfos(layerInfos);
                                }
                            };
                            
                        }.execute((Void[]) null);
                    }
                }
            });
            return builder.create();
        } else {
            return null;
        }
    }

}
