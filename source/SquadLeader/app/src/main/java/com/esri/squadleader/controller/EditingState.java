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

}
