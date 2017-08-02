package com.esri.squadleader.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Logic for the editing of a geometry by a user.
 */
public class GeometryEditController {

    public enum EditMode {
        NONE, POINT, POLYLINE, POLYGON, SAVING
    }

    private final ArrayList<EditingState> editingStates = new ArrayList<>();

    private EditMode editMode = EditMode.NONE;

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    /**
     * @return a copy of the list that references the editing states.
     */
    public List<EditingState> getEditingStates() {
        return new ArrayList<>(editingStates);
    }

    public EditingState getEditingState(int index) {
        return editingStates.get(index);
    }

    public int getEditingStatesCount() {
        return editingStates.size();
    }

    public boolean addEditingState(EditingState editingState) {
        return editingStates.add(editingState);
    }

    public EditingState removeEditingState(int index) {
        return editingStates.remove(index);
    }

    public void clearEditingStates() {
        editingStates.clear();
    }

}
