package com.esri.squadleader.controller;

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
    private EditingState currentEditingState = new EditingState();

    private EditMode editMode = EditMode.NONE;

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
