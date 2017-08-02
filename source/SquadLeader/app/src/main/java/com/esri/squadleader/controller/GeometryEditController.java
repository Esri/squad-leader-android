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
    private EditingState currentEditingState = new EditingState();

    private EditMode editMode = EditMode.NONE;

    public void undo() {
        editingStates.remove(editingStates.size() - 1);
        currentEditingState = 0 == editingStates.size() ?
                new EditingState() :
                new EditingState(editingStates.get(editingStates.size() - 1));
    }

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

    public int getEditingStatesCount() {
        return editingStates.size();
    }

    public boolean addEditingState(EditingState editingState) {
        return editingStates.add(editingState);
    }

    public void clearEditingStates() {
        editingStates.clear();
    }

    public EditingState getCurrentEditingState() {
        return currentEditingState;
    }

    public void setCurrentEditingState(EditingState editingState) {
        this.currentEditingState = editingState;
    }

}
