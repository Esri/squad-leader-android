package com.esri.squadleader.controller;

import com.esri.core.geometry.Point;

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

    public EditingState(ArrayList<Point> points, boolean midpointSelected, boolean vertexSelected, int insertingIndex) {
        this.points.addAll(points);
        this.midPointSelected = midpointSelected;
        this.vertexSelected = vertexSelected;
        this.insertingIndex = insertingIndex;
    }

    public List<Point> getPoints() {
        return new ArrayList<>(points);
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

}
