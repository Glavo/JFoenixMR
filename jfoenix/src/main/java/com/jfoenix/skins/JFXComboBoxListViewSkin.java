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

import com.jfoenix.controls.JFXComboBox;
import com.sun.javafx.css.converters.PaintConverter;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <h1>Material Design ComboBox Skin</h1>
 *
 * @author Shadi Shaheen
 * @version 2.0
 * @since 2017-01-25
 */

public class JFXComboBoxListViewSkin<T> extends ComboBoxListViewSkin<T> {

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    private boolean invalid = true;

    private Text promptText;
    private final ValidationPane<JFXComboBox<?>> errorContainer;
    private final PromptLinesWrapper<JFXComboBox<?>> linesWrapper;

    protected final ObjectProperty<Paint> promptTextFill = new StyleableObjectProperty<Paint>(Color.GRAY) {
        @Override
        public Object getBean() {
            return JFXComboBoxListViewSkin.this;
        }

        @Override
        public String getName() {
            return "promptTextFill";
        }

        @Override
        public CssMetaData<JFXComboBox<?>, Paint> getCssMetaData() {
            return StyleableProperties.PROMPT_TEXT_FILL;
        }
    };

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public JFXComboBoxListViewSkin(final JFXComboBox<T> comboBox) {
        super(comboBox);


        linesWrapper = new PromptLinesWrapper<>(
            comboBox,
            promptTextFill,
            comboBox.valueProperty(),
            comboBox.promptTextProperty(),
            () -> promptText);

        linesWrapper.init(this::createPromptNode);
        linesWrapper.clip.widthProperty().bind(linesWrapper.promptContainer.widthProperty().subtract(arrowButton.widthProperty()));

        errorContainer = new ValidationPane<>(comboBox);

        getChildren().addAll(linesWrapper.line, linesWrapper.focusedLine, linesWrapper.promptContainer, errorContainer);

        if (comboBox.isEditable()) {
            comboBox.getEditor().setStyle("-fx-background-color:TRANSPARENT;-fx-padding: 0.333333em 0em;");
            comboBox.getEditor().promptTextProperty().unbind();
            comboBox.getEditor().setPromptText(null);
            comboBox.getEditor().textProperty().addListener((o, oldVal, newVal) -> {
                linesWrapper.usePromptText.invalidate();
                comboBox.setValue(getConverter().fromString(newVal));
            });
        }

        registerChangeListener(comboBox.disableProperty(), "DISABLE_NODE");
        registerChangeListener(comboBox.focusColorProperty(), "FOCUS_COLOR");
        registerChangeListener(comboBox.unFocusColorProperty(), "UNFOCUS_COLOR");
        registerChangeListener(comboBox.disableAnimationProperty(), "DISABLE_ANIMATION");
    }

    @Override
    protected void handleControlPropertyChanged(String propertyReference) {
        if ("DISABLE_NODE".equals(propertyReference)) {
            linesWrapper.updateDisabled();
        } else if ("FOCUS_COLOR".equals(propertyReference)) {
            linesWrapper.updateFocusColor();
        } else if ("UNFOCUS_COLOR".equals(propertyReference)) {
            linesWrapper.updateUnfocusColor();
        } else if ("DISABLE_ANIMATION".equals(propertyReference)) {
            // remove error clip if animation is disabled
            errorContainer.updateClip();
        } else {
            super.handleControlPropertyChanged(propertyReference);
        }
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    protected void layoutChildren(final double x, final double y,
                                  final double w, final double h) {
        super.layoutChildren(x, y, w, h);
        final double height = getSkinnable().getHeight();
        linesWrapper.layoutLines(x, y, w, h, height,
            promptText == null ? 0 : snapPosition(promptText.getBaselineOffset() + promptText.getLayoutBounds().getHeight() * .36));
        linesWrapper.layoutPrompt(x, y, w, h);
        errorContainer.layoutPane(x, height + linesWrapper.focusedLine.getHeight(), w, h);

        linesWrapper.updateLabelFloatLayout();

        if (invalid) {
            invalid = false;
            // update validation container
            errorContainer.invalid(w);
            // focus
            linesWrapper.invalid();
        }
    }

    private void createPromptNode() {
        if (promptText != null || !linesWrapper.usePromptText.get()) {
            return;
        }
        promptText = new Text();
        // create my custom pane for the prompt node
        promptText.textProperty().bind(getSkinnable().promptTextProperty());
        promptText.fillProperty().bind(linesWrapper.animatedPromptTextFill);
        promptText.getStyleClass().addAll("text");
        promptText.getTransforms().add(linesWrapper.promptTextScale);
        promptText.visibleProperty().bind(linesWrapper.usePromptText);
        promptText.setTranslateX(1);
        linesWrapper.promptContainer.getChildren().add(promptText);

        if (getSkinnable().isFocused() && ((JFXComboBox<T>) getSkinnable()).isLabelFloat()) {
            promptText.setTranslateY(-snapPosition(promptText.getBaselineOffset() + promptText.getLayoutBounds().getHeight() * .36));
            linesWrapper.promptTextScale.setX(0.85);
            linesWrapper.promptTextScale.setY(0.85);
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<JFXComboBox<?>, Paint> PROMPT_TEXT_FILL =
            new CssMetaData<JFXComboBox<?>, Paint>("-fx-prompt-text-fill",
                PaintConverter.getInstance(), Color.GRAY) {

                @Override
                public boolean isSettable(JFXComboBox<?> n) {
                    final JFXComboBoxListViewSkin<?> skin = (JFXComboBoxListViewSkin<?>) n.getSkin();
                    return skin.promptTextFill == null || !skin.promptTextFill.isBound();
                }

                @Override
                @SuppressWarnings("unchecked")
                public StyleableProperty<Paint> getStyleableProperty(JFXComboBox n) {
                    final JFXComboBoxListViewSkin<?> skin = (JFXComboBoxListViewSkin<?>) n.getSkin();
                    return (StyleableProperty<Paint>) skin.promptTextFill;
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<>(ComboBoxListViewSkin.getClassCssMetaData());
            styleables.add(PROMPT_TEXT_FILL);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }


    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}
