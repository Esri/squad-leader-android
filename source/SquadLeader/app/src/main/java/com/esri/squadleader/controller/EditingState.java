package com.esri.squadleader.controller;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * A feature editing state.
 */
public class EditingState {

    private ArrayList<Point> points = new ArrayList<>();
    private boolean midPointSelected = false;
    private boolean vertexSelected = false;
    private int insertingIndex = 0;

    public EditingState() {

    }

    /**
     * Copy constructor.
     *
     * @param editingState the object to be copied.
     */
    public EditingState(EditingState editingState) {
        this(editingState.points, editingState.midPointSelected, editingState.vertexSelected, editingState.insertingIndex);
    }

    public EditingState(List<Point> points, boolean midpointSelected, boolean vertexSelected, int insertingIndex) {
        this.points.addAll(points);
        this.midPointSelected = midpointSelected;
        this.vertexSelected = vertexSelected;
        this.insertingIndex = insertingIndex;
    }

    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }

    public Point getPoint(int index) {
        return points.get(index);
    }

    public int getPointCount() {
        return points.size();
    }

    public boolean addPoint(Point point) {
        return points.add(point);
    }

    public void addPoint(int index, Point point) {
        points.add(index, point);
    }

    public boolean addAllPoints(List<Point> points) {
        return this.points.addAll(points);
    }

    public Point removePoint(int index) {
        return points.remove(index);
    }

    public void clearPoints() {
        points.clear();
    }

    public boolean isMidPointSelected() {
        return midPointSelected;
    }

    public void setMidPointSelected(boolean midPointSelected) {
        this.midPointSelected = midPointSelected;
    }

    public boolean isVertexSelected() {
        return vertexSelected;
    }

    public void setVertexSelected(boolean vertexSelected) {
        this.vertexSelected = vertexSelected;
    }

    public int getInsertingIndex() {
        return insertingIndex;
    }

    public void setInsertingIndex(int insertingIndex) {
        this.insertingIndex = insertingIndex;
    }

    public void deletePoint() {
        /**
         * If a vertex is selected, remove it. Otherwise, remove the last point.
         */
        removePoint(vertexSelected ? insertingIndex : points.size() - 1);
        midPointSelected = false;
        vertexSelected = false;
    }

    /**
     * @param editMode the type of geometry being edited. Supported values are POINT, POLYLINE, and
     *                 POLYGON.
     * @return the geometry that this EditingState represents, or null if 1) editMode is not
     * supported or 2) this EditingState has no points.
     */
    public Geometry getGeometry(GeometryEditController.EditMode editMode) {
        if (0 < points.size()) {
            switch (editMode) {
                case POINT:
                    return points.get(0);

                case POLYLINE:
                case POLYGON:
                    MultiPath multiPath = GeometryEditController.EditMode.POLYLINE == editMode ? new Polyline() : new Polygon();
                    multiPath.startPath(points.get(0));
                    for (int i = 0; i < points.size(); i++) {
                        multiPath.lineTo(points.get(i));
                    }
                    return multiPath;

                default:
                    return null;
            }
        } else {
            return null;
        }
    }

}
