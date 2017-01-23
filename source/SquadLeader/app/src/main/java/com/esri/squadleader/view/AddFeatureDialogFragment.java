/*******************************************************************************
 * Copyright 2017 Esri
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.squadleader.R;
import com.esri.squadleader.controller.MapController;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog for adding a feature to a feature table. An Activity that creates this dialog must implement
 * AddFeatureDialogFragment.MapControllerReturner in order to work properly.
 */
public class AddFeatureDialogFragment extends DialogFragment implements View.OnClickListener {

    /**
     * A listener for this class to get a MapController for manipulating layers.
     */
    public interface MapControllerReturner {

        public MapController getMapController();

    }

    public static final String ARG_FEATURE_LAYERS = "feature layers";

    private static final String TAG = AddFeatureDialogFragment.class.getSimpleName();

    private View fragmentView = null;
    private MapController mapController = null;

//    /**
//     * Sets the request code for adding a layer from a file.
//     * @param requestCode
//     */
//    public void setAddLayerFromFileRequestCode(int requestCode) {
//        this.addLayerFromFileRequestCode = requestCode;
//    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
////        if (activity instanceof AddLayerListener) {
////            listener = (AddLayerListener) activity;
////        }
//    }

//    private void addLayerFromWeb(final boolean useAsBasemap, final String urlString) {
//        new AsyncTask<Void, Void, LayerInfo[]>() {
//
//            @Override
//            protected LayerInfo[] doInBackground(Void... params) {
//                try {
//                    return RestServiceReader.readService(new URL(urlString), useAsBasemap);
//                } catch (final Exception e) {
//                    Log.e(TAG, "Couldn't read and parse " + urlString, e);
//                    if (e instanceof SSLHandshakeException) {
//                        activity.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                boolean foundCpve = false;
//                                Throwable cause = e;
//                                while (null != cause && !foundCpve) {
//                                    if (cause instanceof CertPathValidatorException) {
//                                        foundCpve = true;
//                                    } else {
//                                        cause = cause.getCause();
//                                    }
//                                }
//                                if (!foundCpve) {
//                                    Toast.makeText(activity, "Couldn't add layer from web: " + e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
//                                } else {
//                                    Toast.makeText(activity, "Couldn't add layer: Untrusted certificate for " + urlString, Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
//                        return null;
//                    } else {
//                        activity.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                Toast.makeText(activity, "Couldn't add layer from web: " + e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        return null;
//                    }
//                }
//            }
//
//            @Override
//            protected void onPostExecute(LayerInfo[] layerInfos) {
//                if (null != layerInfos) {
//                    listener.onValidLayerInfos(layerInfos);
//                }
//            };
//
//        }.execute((Void[]) null);
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        if (null != listener) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View inflatedView = inflater.inflate(R.layout.add_feature, null);
        fragmentView = inflatedView;

//            inflatedView.findViewById(R.id.radioButton_fromFile).setOnClickListener(this);
//            inflatedView.findViewById(R.id.radioButton_fromWeb).setOnClickListener(this);

        builder.setView(inflatedView);
        builder.setTitle(getString(R.string.add_feature));

        ArrayList<String> list = new ArrayList<String>();
        if (getActivity() instanceof MapControllerReturner) {
            mapController = ((MapControllerReturner) getActivity()).getMapController();
            if (null != mapController) {
                final List<Layer> layers = mapController.getNonBasemapLayers();
                for (Layer layer : layers) {
                    if (layer instanceof FeatureLayer) {
                        FeatureLayer featureLayer = (FeatureLayer) layer;
                        list.add(featureLayer.getName());
                    }
                }
            }
        } else {
            Log.w(TAG, "Activity must implement this dialog class's MapControllerReturner interface, but " + getActivity().getClass().getName() + " does not");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.layer_list_item, list);
        ((ListView) inflatedView.findViewById(R.id.listView_layersToEdit)).setAdapter(adapter);
//            builder.setNegativeButton(R.string.cancel, null);
//            builder.setPositiveButton(R.string.add_feature, new DialogInterface.OnClickListener() {
//
//                public void onClick(DialogInterface dialog, int which) {
////                    if (((RadioButton) inflatedView.findViewById(R.id.radioButton_fromFile)).isChecked()) {
////                        //Add from file
////                        Intent getContentIntent = FileUtils.createGetContentIntent();
////                        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
////                        getActivity().startActivityForResult(intent, addLayerFromFileRequestCode);
////                    } else {
////                        //Add from web
////                        boolean useAsBasemap = false;
////                        View checkboxView = inflatedView.findViewById(R.id.checkBox_basemap);
////                        if (null != checkboxView && checkboxView instanceof CheckBox) {
////                            useAsBasemap = ((CheckBox) checkboxView).isChecked();
////                        }
////
////                        View serviceUrlView = inflatedView.findViewById(R.id.editText_serviceUrl);
////                        if (null != serviceUrlView && serviceUrlView instanceof EditText) {
////                            final String urlString = ((EditText) serviceUrlView).getText().toString();
////                            addLayerFromWeb(useAsBasemap, urlString);
////                        }
////                    }
//                }
//            });
            return builder.create();
//        } else {
//            return null;
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == addLayerFromFileRequestCode) {
//            if (resultCode == Activity.RESULT_OK) {
//                final Uri uri = data.getData();
//                File file = new File(FileUtils.getPath(activity.getApplicationContext(), uri));
//                LayerInfo[] layerInfos = new LayerInfo[1];
//                layerInfos[0] = new LayerInfo();
//                layerInfos[0].setDatasetPath(file.getAbsolutePath());
//                final LayerType layerType = file.getAbsolutePath().toLowerCase().endsWith(".gpkg") ? LayerType.GEOPACKAGE
//                        : file.getAbsolutePath().toLowerCase().endsWith(".shp") ? LayerType.SHAPEFILE
//                        : LayerType.MIL2525C_MESSAGE;
//                if (LayerType.GEOPACKAGE == layerType) {
//                    layerInfos[0].setShowVectors(true);
//                    layerInfos[0].setShowRasters(true);
//                }
//                layerInfos[0].setLayerType(layerType);
//                layerInfos[0].setName(file.getName());
//                layerInfos[0].setVisible(true);
//                listener.onValidLayerInfos(layerInfos);
//            }
//        } else {
            super.onActivityResult(requestCode, resultCode, data);
//        }
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.radioButton_fromFile:
//                fragmentView.findViewById(R.id.layout_fromWeb).setVisibility(View.GONE);
//                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setText(getText(R.string.choose_file));
//                break;
//
//            case R.id.radioButton_fromWeb:
//                fragmentView.findViewById(R.id.layout_fromWeb).setVisibility(View.VISIBLE);
//                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setText(getText(R.string.add_layer));
//                break;
//        }
    }
}
