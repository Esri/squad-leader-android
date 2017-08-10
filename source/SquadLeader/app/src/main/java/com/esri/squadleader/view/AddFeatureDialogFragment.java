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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.popup.Popup;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geodatabase.GeopackageFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.map.Feature;
import com.esri.core.table.FeatureTable;
import com.esri.core.table.TableException;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.squadleader.R;
import com.esri.squadleader.controller.GeometryEditController;
import com.esri.squadleader.controller.MapController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * A dialog for adding a feature to a feature table. An Activity that creates this dialog must implement
 * AddFeatureDialogFragment.AddFeatureListener in order to work properly.<br/>
 * <br/>
 * A lot of this code comes from the GeometryEditorActivity editor class in the ArcGIS Runtime SDK
 * for Android samples.
 */
public class AddFeatureDialogFragment extends DialogFragment {

    /**
     * A listener for this class to interact with the calling class.
     */
    public interface AddFeatureListener {

        /**
         * @return the MapController containing the layers to which a feature can be added.
         */
        MapController getMapController();

        /**
         * Called when a feature is added.
         *
         * @param popup a Popup for the added feature.
         */
        void featureAdded(Popup popup);

        /**
         * @return an OnSingleTapListener to be used when this fragment is no longer in use.
         */
        OnSingleTapListener getDefaultOnSingleTapListener();

    }

    private static final String TAG = AddFeatureDialogFragment.class.getSimpleName();
    private static final String TAG_DIALOG_FRAGMENTS = "dialog";

    private final GeometryEditController geometryEditController = new GeometryEditController();
    private final OnSingleTapListener editingListener = new OnSingleTapListener() {
        @Override
        public void onSingleTap(final float x, final float y) {
            geometryEditController.handleScreenPoint(x, y, mapController);
            refresh();
        }
    };

    private Activity activity = null;
    private AddFeatureListener addFeatureListener = null;
    private MapController mapController = null;
    private Menu editingMenu = null;
    private GraphicsLayer graphicsLayerEditing = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        if (activity instanceof AddFeatureListener) {
            addFeatureListener = (AddFeatureListener) activity;
        } else {
            Log.w(TAG, getString(R.string.no_add_feature_listener, activity.getClass().getName()));
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final View inflatedView = inflater.inflate(R.layout.add_feature, null);

        builder.setView(inflatedView);
        builder.setTitle(getString(R.string.add_feature));

        final ArrayList<String> layerNames = new ArrayList<String>();
        final ArrayList<FeatureLayer> featureLayers = new ArrayList<FeatureLayer>();
        if (null != addFeatureListener) {
            mapController = addFeatureListener.getMapController();
            if (null != mapController) {
                List<Layer> layers = mapController.getNonBasemapLayers();
                for (Layer layer : layers) {
                    if (layer instanceof FeatureLayer) {
                        FeatureLayer featureLayer = (FeatureLayer) layer;
                        if (featureLayer.getFeatureTable().isEditable()) {
                            layerNames.add(featureLayer.getName());
                            featureLayers.add(featureLayer);
                        }
                    }
                }
            }
        } else {
            Log.w(TAG, "Activity must implement this dialog class's AddFeatureListener interface, but " + activity.getClass().getName() + " does not");
        }

        ArrayAdapter<FeatureLayer> adapter = new ArrayAdapter<FeatureLayer>(activity, R.layout.layer_list_item, featureLayers) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView dropDownView = (TextView) getDropDownView(position, convertView, parent);
                dropDownView.setText(getItem(position).getName());
                return dropDownView;
            }
        };
        ListView listView_layersToEdit = inflatedView.findViewById(R.id.listView_layersToEdit);
        listView_layersToEdit.setAdapter(adapter);
        listView_layersToEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                activity.startActionMode(new ActionMode.Callback() {
                    private final FeatureLayer layerToEdit = featureLayers.get(position);

                    @Override
                    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                        geometryEditController.discardEdits();
                        actionMode.getMenuInflater().inflate(R.menu.add_feature_context_menu, menu);
                        actionMode.setTitle(layerNames.get(position));
                        editingMenu = menu;

                        final GeometryEditController.EditMode newEditMode = geometryEditController.setEditMode(layerToEdit.getGeometryType());
                        if (GeometryEditController.EditMode.NONE == newEditMode) {
                            discard();
                            dismiss();
                        }
                        updateActionBar();

                        mapController.setShowMagnifierOnLongPress(true);

                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                        editingMenu = menu;
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                        boolean returnValue = false;
                        switch (menuItem.getItemId()) {
                            case R.id.save:
                                try {
                                    actionSave(layerToEdit);
                                } catch (TableException e) {
                                    Log.e(TAG, "Couldn't save edits", e);
                                }
                                actionMode.finish();
                                returnValue = true;
                                break;

                            case R.id.delete_point:
                                geometryEditController.deletePoint();
                                refresh();
                                returnValue = true;
                                break;

                            case R.id.undo:
                                geometryEditController.undo();
                                refresh();
                                returnValue = true;
                                break;
                        }
                        updateActionBar();
                        return returnValue;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode actionMode) {
                        discard();
                    }
                });
                dismiss();
            }
        });

        return builder.create();
    }

    private void discard() {
        mapController.removeLayer(graphicsLayerEditing);
        graphicsLayerEditing = null;
        mapController.setOnSingleTapListener(addFeatureListener == null ? null : addFeatureListener.getDefaultOnSingleTapListener());
    }

    private void updateActionBar() {
        if (geometryEditController.getEditMode() == GeometryEditController.EditMode.NONE || geometryEditController.getEditMode() == GeometryEditController.EditMode.SAVING) {
            // We are not editing
            discard();
        } else {
            // We are editing
            showAction(R.id.save, geometryEditController.isSaveValid());
            showAction(R.id.delete_point, geometryEditController.getEditMode() != GeometryEditController.EditMode.POINT && geometryEditController.getCurrentEditingState().getPointCount() > 0 && !geometryEditController.getCurrentEditingState().isMidPointSelected());
            showAction(R.id.undo, geometryEditController.getEditingStatesCount() > 0);
            mapController.setOnSingleTapListener(editingListener);
        }
    }

    private void refresh() {
        if (null == graphicsLayerEditing) {
            graphicsLayerEditing = new GraphicsLayer();
            mapController.addLayer(graphicsLayerEditing);
        } else {
            graphicsLayerEditing.removeAll();
        }
        geometryEditController.draw(graphicsLayerEditing);

        updateActionBar();
    }

    private void showAction(int resId, boolean show) {
        if (null != editingMenu) {
            MenuItem item = editingMenu.findItem(resId);
            item.setVisible(show);
        }
    }

    private void actionSave(final FeatureLayer layerToEdit) throws TableException {
        new AsyncTask<Void, Void, Long>() {

            private Throwable throwable = null;

            @Override
            protected Long doInBackground(Void... nothing) {
                Long featureId = null;
                final FeatureTable featureTable = layerToEdit.getFeatureTable();
                Geometry geom = geometryEditController.getCurrentGeometry();
                if (null != geom) {
                    geometryEditController.setEditMode(GeometryEditController.EditMode.SAVING);
                    Feature newFeature = null;
                    if (featureTable instanceof GeopackageFeatureTable) {
                        try {
                            newFeature = ((GeopackageFeatureTable) featureTable).createNewFeature(null, geom);
                        } catch (TableException e) {
                            Log.e(TAG, "Could not create new GeoPackage feature", e);
                            throwable = e;
                        }
                    } else if (featureTable instanceof GeodatabaseFeatureTable) {
                        try {
                            newFeature = ((GeodatabaseFeatureTable) featureTable).createNewFeature(null, geom);
                        } catch (TableException e) {
                            Log.e(TAG, "Could not create new geodatabase feature", e);
                            throwable = e;
                        }
                    }

                    if (null != newFeature) {
                        try {
                            featureId = featureTable.addFeature(newFeature);
                        } catch (Throwable t) {
                            Log.e(TAG, "Could not add feature", t);
                            throwable = t;
                        }
                    }

                }
                return featureId;
            }

            @Override
            protected void onPostExecute(Long featureId) {
                activity.setProgressBarIndeterminateVisibility(false);
                exitEditMode();

                if (null != featureId) {
                    QueryParameters queryParameters = new QueryParameters();
                    queryParameters.setObjectIds(new long[]{featureId});
                    final FutureTask<List<Popup>> identifyFuture = mapController.queryFeatureLayer(layerToEdit, queryParameters);
                    Executors.newSingleThreadExecutor().submit(identifyFuture);
                    try {
                        final List<Popup> popups = identifyFuture.get();
                        if (1 == popups.size()) {
                            if (null != addFeatureListener) {
                                addFeatureListener.featureAdded(popups.get(0));
                            } else {
                                Log.w(TAG, getString(R.string.no_add_feature_listener, activity.getClass().getName()));
                            }
                        } else {
                            Log.w(TAG, getString(R.string.feature_id_query_expected_single_result, featureId, popups.size()));
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(TAG, "Exception while identifying feature layers", e);
                    }
                } else {
                    Toast.makeText(activity,
                            "Could not add feature" + (null == throwable ? "" : ": " + throwable.getMessage()),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }.execute();
    }

    private void exitEditMode() {
        if (graphicsLayerEditing != null) {
            graphicsLayerEditing.removeAll();
        }

        updateActionBar();
        mapController.setShowMagnifierOnLongPress(false);
    }

}
