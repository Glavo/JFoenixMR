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

package com.jfoenix.controls;

import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.converters.ButtonTypeConverter;
import com.jfoenix.skins.JFXButtonSkin;
import com.sun.javafx.css.converters.BooleanConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JFXButton is the material design implementation of a button.
 * it contains ripple effect , the effect color is set according to text fill of the button 1st
 * or the text fill of graphic node (if it was set to Label) 2nd.
 *
 * @author Shadi Shaheen
 * @version 1.0
 * @since 2016-03-09
 */
public class JFXButton extends Button {

    /**
     * {@inheritDoc}
     */
    public JFXButton() {
        initialize();
        // init in scene builder workaround ( TODO : remove when JFoenix is well integrated in scenebuilder by gluon )
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length && i < 15; i++) {
            if (stackTraceElements[i].getClassName().toLowerCase().contains(".scenebuilder.kit.fxom.")) {
                this.setText("Button");
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public JFXButton(String text) {
        super(text);
        initialize();
    }

    /**
     * {@inheritDoc}
     */
    public JFXButton(String text, Node graphic) {
        super(text, graphic);
        initialize();
    }

    private void initialize() {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new JFXButtonSkin(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgentStylesheet() {
        return USER_AGENT_STYLESHEET;
    }


    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/
    /**
     * the ripple color property of JFXButton.
     */
    private final ObjectProperty<Paint> ripplerFill = new SimpleObjectProperty<>(null);

    public final ObjectProperty<Paint> ripplerFillProperty() {
        return this.ripplerFill;
    }

    /**
     * @return the ripple color
     */
    public final Paint getRipplerFill() {
        return this.ripplerFillProperty().get();
    }

    /**
     * set the ripple color
     *
     * @param ripplerFill the color of the ripple effect
     */
    public final void setRipplerFill(final Paint ripplerFill) {
        this.ripplerFillProperty().set(ripplerFill);
    }


    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    /**
     * Initialize the style class to 'jfx-button'.
     * <p>
     * This is the selector class from which CSS can be used to style
     * this control.
     */
    private static final String DEFAULT_STYLE_CLASS = "jfx-button";
    private static final String USER_AGENT_STYLESHEET = JFoenixResources.load("css/controls/jfx-button.css").toExternalForm();

    public enum ButtonType {FLAT, RAISED}

    /**
     * according to material design the button has two types:
     * - flat : only shows the ripple effect upon clicking the button
     * - raised : shows the ripple effect and change in depth upon clicking the button
     */
    private final StyleableObjectProperty<ButtonType> buttonType = new SimpleStyleableObjectProperty<>(
        StyleableProperties.BUTTON_TYPE,
        JFXButton.this,
        "buttonType",
        ButtonType.FLAT);

    public ButtonType getButtonType() {
        return buttonType == null ? ButtonType.FLAT : buttonType.get();
    }

    public StyleableObjectProperty<ButtonType> buttonTypeProperty() {
        return this.buttonType;
    }

    public void setButtonType(ButtonType type) {
        this.buttonType.set(type);
    }

    /**
     * Disable the visual indicator for focus
     */
    private final StyleableBooleanProperty disableVisualFocus = new SimpleStyleableBooleanProperty(StyleableProperties.DISABLE_VISUAL_FOCUS,
        JFXButton.this,
        "disableVisualFocus",
        false);

    /**
     * Setting this property disables this {@link JFXButton} from showing keyboard focus.
     * @return A property that will disable visual focus if true and enable it if false.
     */
    public final StyleableBooleanProperty disableVisualFocusProperty() {
        return this.disableVisualFocus;
    }

    /**
     * Indicates whether or not this {@link JFXButton} will show focus when it receives keyboard focus.
     * @return False if this {@link JFXButton} will show visual focus and true if it will not.
     */
    public final Boolean isDisableVisualFocus() {
        return disableVisualFocus != null && this.disableVisualFocusProperty().get();
    }

    /**
     * Setting this to true will disable this {@link JFXButton} from showing focus when it receives keyboard focus.
     * @param disabled True to disable visual focus and false to enable it.
     */
    public final void setDisableVisualFocus(final Boolean disabled) {
        this.disableVisualFocusProperty().set(disabled);
    }

    private static class StyleableProperties {
        private static final CssMetaData<JFXButton, ButtonType> BUTTON_TYPE =
            new CssMetaData<JFXButton, ButtonType>("-jfx-button-type",
                ButtonTypeConverter.getInstance(), ButtonType.FLAT) {
                @Override
                public boolean isSettable(JFXButton control) {
                    return control.buttonType == null || !control.buttonType.isBound();
                }

                @Override
                public StyleableProperty<ButtonType> getStyleableProperty(JFXButton control) {
                    return control.buttonTypeProperty();
                }
            };

        private static final CssMetaData<JFXButton, Boolean> DISABLE_VISUAL_FOCUS =
            new CssMetaData<JFXButton, Boolean>("-jfx-disable-visual-focus",
                BooleanConverter.getInstance(), false) {
                @Override
                public boolean isSettable(JFXButton control) {
                    return control.disableVisualFocus == null || !control.disableVisualFocus.isBound();
                }

                @Override
                public StyleableBooleanProperty getStyleableProperty(JFXButton control) {
                    return control.disableVisualFocusProperty();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> CHILD_STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<>(Button.getClassCssMetaData());
            Collections.addAll(styleables, BUTTON_TYPE,DISABLE_VISUAL_FOCUS);
            CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.CHILD_STYLEABLES;
    }


}
