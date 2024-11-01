/*
 * Copyright (c) 2016 JFoenix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.jfoenix.skins;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.behavior.JFXDatePickerBehavior;
import com.jfoenix.svg.SVGGlyph;
import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * <h1>Material Design Date Picker Skin</h1>
 *
 * @author Shadi Shaheen
 * @version 1.0
 * @since 2016-03-09
 */
public class JFXDatePickerSkin extends ComboBoxPopupControl<LocalDate> {

    /**
     * TODO:
     * 1. Handle different Chronology
     */
    private final JFXDatePicker jfxDatePicker;

    // displayNode is the same as editorNode
    private TextField displayNode;
    private JFXDatePickerContent content;

    private JFXDialog dialog;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JFXDatePickerSkin(final JFXDatePicker datePicker) {
        super(datePicker, new JFXDatePickerBehavior(datePicker));
        this.jfxDatePicker = datePicker;
        try {
            Field helper = datePicker.focusedProperty().getClass().getSuperclass()
                .getDeclaredField("helper");
            helper.setAccessible(true);
            ExpressionHelper<?> value = (ExpressionHelper<?>) helper.get(datePicker.focusedProperty());
            Field changeListenersField = value.getClass().getDeclaredField("changeListeners");
            changeListenersField.setAccessible(true);
            ChangeListener[] changeListeners = (ChangeListener<?>[]) changeListenersField.get(value);
            // remove parent focus listener to prevent editor class cast exception
            for (int i = changeListeners.length - 1; i > 0; i--) {
                if (changeListeners[i] != null && changeListeners[i].getClass().getName().contains("ComboBoxPopupControl")) {
                    datePicker.focusedProperty().removeListener(changeListeners[i]);
                    break;
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        // add focus listener on editor node
        datePicker.focusedProperty().addListener((obj, oldVal, newVal) -> {
            if (getEditor() != null && !newVal) {
                setTextFromTextFieldIntoComboBoxValue();
            }
        });

        // create calender or clock button
        arrow = new SVGGlyph(0,
            "calendar",
            "M320 384h128v128h-128zM512 384h128v128h-128zM704 384h128v128h-128zM128 "
            + "768h128v128h-128zM320 768h128v128h-128zM512 768h128v128h-128zM320 "
            + "576h128v128h-128zM512 576h128v128h-128zM704 576h128v128h-128zM128 "
            + "576h128v128h-128zM832 0v64h-128v-64h-448v64h-128v-64h-128v1024h960v-1024h-128zM896"
            + " 960h-832v-704h832v704z",
            null);
        ((SVGGlyph) arrow).setFill(jfxDatePicker.getDefaultColor());
        ((SVGGlyph) arrow).setSize(20, 20);
        arrowButton.getChildren().setAll(arrow);

        ((JFXTextField) getEditor()).setFocusColor(jfxDatePicker.getDefaultColor());

        registerChangeListener(datePicker.converterProperty(), "CONVERTER");
        registerChangeListener(datePicker.dayCellFactoryProperty(), "DAY_CELL_FACTORY");
        registerChangeListener(datePicker.showWeekNumbersProperty(), "SHOW_WEEK_NUMBERS");
        registerChangeListener(datePicker.valueProperty(), "VALUE");
        registerChangeListener(datePicker.defaultColorProperty(), "DEFAULT_COLOR");
    }

    @Override
    protected Node getPopupContent() {
        if (content == null) {
            // different chronologies are not supported yet
            content = new JFXDatePickerContent(jfxDatePicker);
        }
        return content;
    }

    @Override
    public void show() {
        if (!jfxDatePicker.isOverLay()) {
            super.show();
        }
        if (content != null) {
            content.init();
            content.clearFocus();
        }
        if (dialog == null && jfxDatePicker.isOverLay()) {
            StackPane dialogParent = jfxDatePicker.getDialogParent();
            if (dialogParent == null) {
                dialogParent = (StackPane) jfxDatePicker.getScene().getRoot();
            }
            dialog = new JFXDialog(dialogParent, (Region) getPopupContent(), DialogTransition.CENTER, true);
            arrowButton.setOnMouseClicked((click) -> {
                if (jfxDatePicker.isOverLay()) {
                    StackPane parent = jfxDatePicker.getDialogParent();
                    if (parent == null) {
                        parent = (StackPane) jfxDatePicker.getScene().getRoot();
                    }
                    dialog.show(parent);
                }
            });
        }
    }


    @Override
    protected void handleControlPropertyChanged(String p) {
        if ("DEFAULT_COLOR".equals(p)) {
            ((JFXTextField) getEditor()).setFocusColor(jfxDatePicker.getDefaultColor());
        } else if ("DAY_CELL_FACTORY".equals(p)) {
            updateDisplayNode();
            content = null;
            popup = null;
        } else if ("CONVERTER".equals(p)) {
            updateDisplayNode();
        } else if ("EDITOR".equals(p)) {
            getEditableInputNode();
        } else if ("SHOWING".equals(p)) {
            if (jfxDatePicker.isShowing()) {
                if (content != null) {
                    LocalDate date = jfxDatePicker.getValue();
                    // set the current date / now when showing the date picker content
                    content.displayedYearMonthProperty().set((date != null) ?
                        YearMonth.from(date) : YearMonth.now());
                    content.updateValues();
                }
                show();
            } else {
                hide();
            }
        } else if ("SHOW_WEEK_NUMBERS".equals(p)) {
            if (content != null) {
                // update the content grid to show week numbers
                content.updateContentGrid();
                content.updateWeekNumberDateCells();
            }
        } else if ("VALUE".equals(p)) {
            updateDisplayNode();
            if (content != null) {
                LocalDate date = jfxDatePicker.getValue();
                content.displayedYearMonthProperty().set((date != null) ?
                    YearMonth.from(date) : YearMonth.now());
                content.updateValues();
            }
            jfxDatePicker.fireEvent(new ActionEvent());
        } else {
            super.handleControlPropertyChanged(p);
        }
    }

    @Override
    protected TextField getEditor() {
        return ((DatePicker) getSkinnable()).getEditor();
    }

    @Override
    protected StringConverter<LocalDate> getConverter() {
        return ((DatePicker) getSkinnable()).getConverter();
    }

    @Override
    public Node getDisplayNode() {
        if (displayNode == null) {
            displayNode = getEditableInputNode();
            displayNode.getStyleClass().add("date-picker-display-node");
            updateDisplayNode();
        }
        displayNode.setEditable(jfxDatePicker.isEditable());
        return displayNode;
    }

    /*
     * this method is called from the behavior class to make sure
     * DatePicker button is in sync after the popup is being dismissed
     */
    public void syncWithAutoUpdate() {
        if (!getPopup().isShowing() && jfxDatePicker.isShowing()) {
            jfxDatePicker.hide();
        }
    }
}

