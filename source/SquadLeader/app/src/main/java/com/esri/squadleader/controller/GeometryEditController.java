package com.esri.squadleader.controller;

import android.graphics.Color;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
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
    private MarkerSymbol selectedMidpointMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
    private MarkerSymbol vertexMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
    private MarkerSymbol midpointMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
    private LineSymbol lineSymbol = new SimpleLineSymbol(Color.BLACK, 4);
    private FillSymbol fillSymbol = new SimpleFillSymbol(Color.YELLOW);

    public GeometryEditController() {
        fillSymbol.setAlpha(100);
        fillSymbol.setOutline(lineSymbol);
    }

    /**
     * Uses the current editing state to handle a new screen point. The point could be a new vertex
     * to add to the geometry, an existing vertex to be selected, or an existing midpoint to be
     * selected.
     *
     * @param x             the x-value of the screen point, in pixels.
     * @param y             the y-value of the screen point, in pixels.
     * @param mapController a MapController for converting between screen points and map points.
     */
    public void handleScreenPoint(final float x, final float y, MapController mapController) {
        final Point point = mapController.toMapPointObject(Math.round(x), Math.round(y));
        if (EditMode.POINT == editMode) {
            currentEditingState.clearPoints();
        }
        if (currentEditingState.isMidPointSelected() || currentEditingState.isVertexSelected()) {
            movePoint(point);
        } else {
            // If tap coincides with a mid-point, select that mid-point
            int idx1 = GeometryEditController.getSelectedIndex(x, y, midpoints, mapController);
            if (idx1 != -1) {
                currentEditingState.setMidPointSelected(true);
                currentEditingState.setInsertingIndex(idx1);
            } else {
                // If tap coincides with a vertex, select that vertex
                int idx2 = GeometryEditController.getSelectedIndex(x, y, currentEditingState.getPoints(), mapController);
                if (idx2 != -1) {
                    currentEditingState.setVertexSelected(true);
                    currentEditingState.setInsertingIndex(idx2);
                } else {
                    // No matching point above, add new vertex at tap point
                    currentEditingState.addPoint(point);
                    editingStates.add(new EditingState(currentEditingState));
                }
            }
        }
    }

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
     * Draws the current editing state on a GraphicsLayer.
     *
     * @param graphicsLayer the GraphicsLayer on which the current editing state will be drawn.
     */
    public void draw(GraphicsLayer graphicsLayer) {
        drawPolylineOrPolygon(graphicsLayer);
        drawMidpoints(graphicsLayer);
        drawVertices(graphicsLayer);
    }

    /**
     * Draws the midpoints on a GraphicsLayer.
     *
     * @param graphicsLayer the GraphicsLayer on which to draw the midpoints.
     */
    private void drawMidpoints(GraphicsLayer graphicsLayer) {
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
     * Draws the vertices on a GraphicsLayer.
     *
     * @param graphicsLayer the GraphicsLayer on which to draw the vertices.
     */
    private void drawVertices(GraphicsLayer graphicsLayer) {
        MarkerSymbol symbol;

        for (int index = 0; index < currentEditingState.getPointCount(); index++) {
            if (currentEditingState.isVertexSelected() && index == currentEditingState.getInsertingIndex()) {
                // This vertex is currently selected
                symbol = selectedMidpointMarkerSymbol;
            } else if (index == currentEditingState.getPointCount() - 1 && !currentEditingState.isMidPointSelected() && !currentEditingState.isVertexSelected()) {
                // Last vertex and none currently selected
                symbol = selectedMidpointMarkerSymbol;
            } else {
                // An ordinary vertex
                symbol = vertexMarkerSymbol;
            }
            Graphic graphic = new Graphic(currentEditingState.getPoint(index), symbol);
            graphicsLayer.addGraphic(graphic);
        }
    }

    private void movePoint(Point point) {
        if (currentEditingState.isMidPointSelected()) {
            // Move mid-point to the new location and make it a vertex
            currentEditingState.addPoint(currentEditingState.getInsertingIndex() + 1, point);
        } else {
            // Must be a vertex: move it to the new location
            ArrayList<Point> pointsToAdd = new ArrayList<>();
            for (int i = 0; i < currentEditingState.getPointCount(); i++) {
                if (i == currentEditingState.getInsertingIndex()) {
                    pointsToAdd.add(point);
                } else {
                    pointsToAdd.add(currentEditingState.getPoint(i));
                }
            }
            currentEditingState.clearPoints();
            currentEditingState.addAllPoints(pointsToAdd);
        }
        // Go back to the normal drawing mode and save the new editing state
        currentEditingState.setMidPointSelected(false);
        currentEditingState.setVertexSelected(false);
        editingStates.add(new EditingState(currentEditingState));
    }

    /**
     * Checks if a given location coincides (within a tolerance) with a point in a given array.
     *
     * @param x             Screen coordinate of location to check.
     * @param y             Screen coordinate of location to check.
     * @param points        List of points to check. This method must not modify this List!
     * @param mapController the MapController for the editing app.
     * @return Index within points of matching point, or -1 if none.
     */
    private static int getSelectedIndex(double x, double y, List<Point> points, MapController mapController) {
        final int TOLERANCE = 40; // Tolerance in pixels

        if (points == null || points.size() == 0) {
            return -1;
        }

        // Find closest point
        int index = -1;
        double distSQ_Small = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            Point mapPoint = points.get(i);
            double[] screenCoords = mapController.toScreenPoint(mapPoint.getX(), mapPoint.getY());
            Point p = new Point(screenCoords[0], screenCoords[1]);
            double diffx = p.getX() - x;
            double diffy = p.getY() - y;
            double distSQ = diffx * diffx + diffy * diffy;
            if (distSQ < distSQ_Small) {
                index = i;
                distSQ_Small = distSQ;
            }
        }

        // Check if it's close enough
        if (distSQ_Small < (TOLERANCE * TOLERANCE)) {
            return index;
        }
        return -1;
    }

    /**
     * @return true if there is enough info to save a geometry (e.g. at least three points for a
     * polygon) and false otherwise.
     */
    public boolean isSaveValid() {
        int minPoints;
        switch (editMode) {
            case POINT:
                minPoints = 1;
                break;
            case POLYGON:
                minPoints = 3;
                break;
            case POLYLINE:
                minPoints = 2;
                break;
            default:
                return false;
        }
        return currentEditingState.getPointCount() >= minPoints;
    }

    private void drawPolylineOrPolygon(GraphicsLayer graphicsLayer) {
        Graphic graphic;
        MultiPath multipath;

        if (currentEditingState.getPointCount() > 1) {
            // Build a MultiPath containing the vertices
            multipath = EditMode.POLYLINE == editMode ? new Polyline() : new Polygon();
            multipath.startPath(currentEditingState.getPoint(0));
            for (int i = 1; i < currentEditingState.getPointCount(); i++) {
                multipath.lineTo(currentEditingState.getPoint(i));
            }

            // Draw it using a line or fill symbol
            graphic = new Graphic(multipath, EditMode.POLYLINE == editMode ? lineSymbol : fillSymbol);
            graphicsLayer.addGraphic(graphic);
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

    /**
     * Sets the edit mode using a geometry type.
     *
     * @param geometryType the geometry type.
     * @return the EditMode that was set.
     */
    public EditMode setEditMode(Geometry.Type geometryType) {
        switch (geometryType) {
            case MULTIPOINT:
            case POINT:
                editMode = EditMode.POINT;
                break;
            case LINE:
            case POLYLINE:
                editMode = EditMode.POLYLINE;
                break;
            case ENVELOPE:
            case POLYGON:
                editMode = EditMode.POLYGON;
                break;
            default:
                editMode = EditMode.NONE;
        }
        return editMode;
    }

    public int getEditingStatesCount() {
        return editingStates.size();
    }

    public EditingState getCurrentEditingState() {
        return currentEditingState;
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

    public MarkerSymbol getVertexMarkerSymbol() {
        return vertexMarkerSymbol;
    }

    public void setVertexMarkerSymbol(MarkerSymbol vertexMarkerSymbol) {
        this.vertexMarkerSymbol = vertexMarkerSymbol;
    }

    public LineSymbol getLineSymbol() {
        return lineSymbol;
    }

    public void setLineSymbol(LineSymbol lineSymbol) {
        this.lineSymbol = lineSymbol;
        this.fillSymbol.setOutline(lineSymbol);
    }

}
