package com.esri.squadleader.controller;

import android.graphics.Color;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Logic for the editing of a geometry by a user.
 */
public class GeometryEditController {

    /**
     * An EditMode is a geometry type to be edited, as well as NONE for times when no edit is
     * happening and SAVING for when edits are being saved.
     */
    public enum EditMode {
        NONE, POINT, POLYLINE, POLYGON, SAVING
    }

    private final ArrayList<EditingState> editingStates = new ArrayList<>();
    private final ArrayList<Point> midpoints = new ArrayList<>();

    private EditingState currentEditingState = new EditingState();
    private EditMode editMode = EditMode.NONE;
    private MarkerSymbol midpointMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
    private MarkerSymbol selectedMidpointMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

    /**
     * Undoes the last edit to the geometry.
     */
    public void undo() {
        editingStates.remove(editingStates.size() - 1);
        currentEditingState = 0 == editingStates.size() ?
                new EditingState() :
                new EditingState(editingStates.get(editingStates.size() - 1));
    }

    /**
     * Deletes the last point added to the geometry.
     */
    public void deletePoint() {
        currentEditingState.deletePoint();
        editingStates.add(new EditingState(currentEditingState));
    }

    public void discardEdits() {
        editingStates.clear();
        midpoints.clear();
        currentEditingState = new EditingState();
        editMode = EditMode.NONE;
    }

    /**
     * Draws the midpoints on a GraphicsLayer.
     * @param graphicsLayer the GraphicsLayer on which to draw the midpoints.
     */
    public void drawMidpoints(GraphicsLayer graphicsLayer) {
        int index;
        Graphic graphic;

        midpoints.clear();
        if (currentEditingState.getPointCount() > 1) {

            // Build new list of mid-points
            for (int i = 1; i < currentEditingState.getPointCount(); i++) {
                Point p1 = currentEditingState.getPoint(i - 1);
                Point p2 = currentEditingState.getPoint(i);
                midpoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
            }
            if (GeometryEditController.EditMode.POLYGON == editMode && currentEditingState.getPointCount() > 2) {
                // Complete the circle
                Point p1 = currentEditingState.getPoint(0);
                Point p2 = currentEditingState.getPoint(currentEditingState.getPointCount() - 1);
                midpoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
            }

            // Draw the mid-points
            index = 0;
            for (Point pt : midpoints) {
                if (currentEditingState.isMidPointSelected() && currentEditingState.getInsertingIndex() == index) {
                    graphic = new Graphic(pt, selectedMidpointMarkerSymbol);
                } else {
                    graphic = new Graphic(pt, midpointMarkerSymbol);
                }
                graphicsLayer.addGraphic(graphic);
                index++;
            }
        }
    }

    /**
     * @return the current editing state's geometry.
     */
    public Geometry getCurrentGeometry() {
        return currentEditingState.getGeometry(editMode);
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    public int getEditingStatesCount() {
        return editingStates.size();
    }

    public boolean addEditingState(EditingState editingState) {
        return editingStates.add(editingState);
    }

    public EditingState getCurrentEditingState() {
        return currentEditingState;
    }

    /**
     * @return a copy of the list of midpoints; the points themselves are not copied.
     */
    public List<Point> getMidpoints() {
        return new ArrayList<>(midpoints);
    }

    public MarkerSymbol getMidpointMarkerSymbol() {
        return midpointMarkerSymbol;
    }

    public void setMidpointMarkerSymbol(MarkerSymbol midpointMarkerSymbol) {
        this.midpointMarkerSymbol = midpointMarkerSymbol;
    }

    public MarkerSymbol getSelectedMidpointMarkerSymbol() {
        return selectedMidpointMarkerSymbol;
    }

    public void setSelectedMidpointMarkerSymbol(MarkerSymbol selectedMidpointMarkerSymbol) {
        this.selectedMidpointMarkerSymbol = selectedMidpointMarkerSymbol;
    }

}
