package com.esri.squadleader.controller;

/**
 * Logic for the editing of a geometry by a user.
 */
public class GeometryEditController {

    public enum EditMode {
        NONE, POINT, POLYLINE, POLYGON, SAVING
    }

    private EditMode editMode = EditMode.NONE;

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

}
