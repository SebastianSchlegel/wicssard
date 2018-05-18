package de.sebastianschlegel.wicssard;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Wicssard extends Application {

    final static Logger LOGGER = Logger.getLogger("Wicssard");

    final CssParser cssParser = new CssParser();

    final FlowPane colorWidgets = new FlowPane();

    final ScrollPane sp = new ScrollPane(this.colorWidgets);

    final ColorPicker colorPicker = new ColorPicker();

    final Label toolBarInfoLabel = new Label();

    final Label statusBar = new Label();

    Rectangle leadingColorWidget;

    CSSStyleSheet styleSheet;

    final Set<Rectangle> selectedColorWidgets = new HashSet<>();

    public static void main (final String[] args) {
        LOGGER.info("Starting up..");
        launch(args);
    }

    private Collection<ColorModel> getSelectedColorModels () {
        final Collection<ColorModel> result = new ArrayList<>();
        for (final Rectangle selectedColorWidget : this.selectedColorWidgets) {
            result.add(((ColorModel) selectedColorWidget.getUserData()));
        }
        return result;
    }

    @Override
    public void start (final Stage primaryStage) {
        primaryStage.setTitle("Wicssard");
        final BorderPane root = new BorderPane();
        final ToolBar toolBar = new ToolBar();
        this.sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.sp.setFitToWidth(true);

        final Button open = new Button("Open");
        open.setOnAction(event -> {
            final FileChooser fileChooser = new FileChooser();
            final File cssFile = fileChooser.showOpenDialog(primaryStage);
            Wicssard.this.styleSheet = Wicssard.this.parseFile(cssFile);
            Wicssard.this.generateColorWidgets();
        });

        final Button save = new Button("Save");
        save.setOnAction(event -> {
            if (this.styleSheet != null) {
                final FileChooser fileChooser = new FileChooser();
                final File cssFile = fileChooser.showSaveDialog(primaryStage);
                LOGGER.info("Start saving...");
                this.writeFile(cssFile, this.styleSheet, false);
            }
        });

        final Button saveDifferences = new Button("Save Difference");
        saveDifferences.setOnAction(event -> {
            if (this.styleSheet != null) {
                final FileChooser fileChooser = new FileChooser();
                final File cssFile = fileChooser.showSaveDialog(primaryStage);
                LOGGER.info("Start saving...");
                this.writeFile(cssFile, this.styleSheet, true);
            }
        });

        final Button deselectAll = new Button("Deselect all");
        deselectAll.setOnAction(event -> {
            for (final Rectangle selectedColorWidget : new ArrayList<>(this.selectedColorWidgets)) {
                this.deselect(selectedColorWidget);
            }
            this.updateStatus();
        });

        final Button selectAll = new Button("Select all");
        selectAll.setOnAction(event -> {
            for (final Node node : this.colorWidgets.getChildren()) {
                this.select((Rectangle) node);
            }
            this.updateStatus();
        });

        this.colorPicker.setOnAction(event -> {
            if (this.colorPicker.getUserData() instanceof Rectangle) {
                final Rectangle rectangle = (Rectangle) this.colorPicker.getUserData();
                final ColorModel colorModel = (ColorModel) rectangle.getUserData();
                (colorModel).getRgbColor().applyColor(this.colorPicker.getValue());
                ColorMixer.followTheLeader(colorModel, this.getSelectedColorModels());
                this.updateColorWidgets();
            }
        });

        toolBar.getItems().add(open);
        toolBar.getItems().add(save);
        toolBar.getItems().add(saveDifferences);
        toolBar.getItems().add(selectAll);
        toolBar.getItems().add(deselectAll);
        toolBar.getItems().add(this.colorPicker);
        toolBar.getItems().add(this.toolBarInfoLabel);

        final ScrollPane bottomScrollPane = new ScrollPane();
        bottomScrollPane.setContent(this.statusBar);
        bottomScrollPane.setPrefHeight(100);

        this.sp.setStyle("-fx-background: #101010;");

        root.setCenter(this.sp);
        root.setTop(toolBar);
        root.setBottom(bottomScrollPane);

        primaryStage.setScene(new Scene(root, 850, 650));
        primaryStage.show();
    }

    private CSSStyleSheet parseFile (final File cssFile) {
        if (cssFile == null) {
            return null;
        }
        final String fileName = cssFile.getAbsolutePath();
        LOGGER.info("Start parsing " + fileName);
        final FileReader fileReader;
        try {
            fileReader = new FileReader(fileName);
            final CSSStyleSheet cssStyleSheet = this.cssParser.parseCss(fileReader);
            this.cssParser.parseColors(cssStyleSheet);
            fileReader.close();
            return cssStyleSheet;
        } catch (final IOException e) {
            LOGGER.severe("Parsing failed: " + e.toString());
            return null;
        }
    }

    private void writeFile (final File cssFile, final CSSStyleSheet cssStyleSheet, final boolean writeDifferencesOnly) {
        if (cssFile == null) {
            return;
        }
        final String fileName = cssFile.getAbsolutePath();
        LOGGER.info("Start saving " + fileName);
        final FileWriter fileWriter;
        final Collection<CSSValue> changes = new ArrayList<>();
        for (final Node node : this.colorWidgets.getChildren()) {
            changes.addAll(((ColorModel) node.getUserData()).updateReferences());
        }
        final String styleSheetText;
        if (writeDifferencesOnly) {
            styleSheetText = this.cssParser.writeStyleSheet(this.cssParser.getDiffStyleSheet(this.styleSheet, changes));
        } else {
            styleSheetText = this.cssParser.writeStyleSheet(cssStyleSheet);
        }

        try {
            fileWriter = new FileWriter(fileName);
            fileWriter.write(styleSheetText);
            fileWriter.flush();
            fileWriter.close();
            LOGGER.info("Saving completed to " + fileName);
        } catch (final IOException e) {
            LOGGER.severe("Writing failed: " + e.toString());
        }
    }

    private void updateColorWidgets () {
        for (final Node node : this.colorWidgets.getChildren()) {
            final Rectangle rectangle = (Rectangle) node;
            final ColorModel colorModel = (ColorModel) rectangle.getUserData();
            rectangle.setFill(colorModel.getRgbColor().getColor());
        }
        this.updateStatus();
    }

    private void generateColorWidgets () {
        this.colorWidgets.getChildren().clear();

        Collection<ColorModel> colors = this.cssParser.getColors().values();

        colors = colors.stream().sorted(Comparator.comparingDouble(value -> {
                    final Color color = value.getRgbColor().getColor();
                    return (color.getHue() * 10 + 1000) * color.getSaturation() * -100 - color.getBrightness();
                }
        )).collect(Collectors.toList());

        for (final ColorModel colorEntry : colors) {
            final Rectangle colorWidget = new Rectangle(75, 75, colorEntry.getRgbColor().getPaint());
            colorWidget.setStrokeWidth(10);
            colorWidget.setUserData(colorEntry);
            colorWidget.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    this.setAsLeading(colorWidget, colorEntry.getInfo());
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    if (this.selectedColorWidgets.contains(colorWidget)) {
                        this.deselect(colorWidget);
                    } else {
                        this.select(colorWidget);
                    }
                    this.updateStatus();
                }
            });
            this.colorWidgets.getChildren().add(colorWidget);
            this.deselect(colorWidget);
        }

        this.updateStatus();
    }

    private void updateStatus () {
        this.toolBarInfoLabel.setText("Total: " + this.cssParser.getColors().size() + " Selected: " + this.selectedColorWidgets.size());
    }

    private void select (final Rectangle colorWidget) {
        this.selectedColorWidgets.add(colorWidget);
        colorWidget.setStroke(Paint.valueOf("#D0D0D0"));
    }

    private void deselect (final Rectangle colorWidget) {
        this.selectedColorWidgets.remove(colorWidget);
        colorWidget.setStroke(Paint.valueOf("#303030"));
    }

    private void setAsLeading (final Rectangle colorWidget, final List<String> list) {
        if (this.leadingColorWidget != null) {
            this.leadingColorWidget.setWidth(75);
            this.leadingColorWidget.setHeight(75);
            this.leadingColorWidget.setStrokeWidth(10);
            this.leadingColorWidget = null;
        }
        if (colorWidget != null) {
            final ColorModel color = (ColorModel) colorWidget.getUserData();
            this.colorPicker.setValue(color.getRgbColor().getColor());
            this.colorPicker.setUserData(colorWidget);
            this.statusBar.setText(String.join("\n", list));
            this.leadingColorWidget = colorWidget;
            this.leadingColorWidget.setWidth(80);
            this.leadingColorWidget.setHeight(80);
            this.leadingColorWidget.setStrokeWidth(5);
        }
    }
}