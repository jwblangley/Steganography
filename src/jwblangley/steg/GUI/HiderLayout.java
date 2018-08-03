package jwblangley.steg.GUI;

import static jwblangley.steg.run.Utils.humanReadableByteCount;

import java.io.File;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import jwblangley.steg.run.Steganography;

public class HiderLayout {

  private static Button baseButton, fileButton, optionsButton;
  private static Stage window;
  public static Label statusLabel;

  public static Pane layout(Stage windowIn) {

    window = windowIn;

    BorderPane topNode = new BorderPane();
    Font font = new Font("Arial", Steganography.HEIGHT / 35);

    // Border Top
    HBox borderTopLayout = new HBox(5);
    borderTopLayout.setPrefSize(Steganography.WIDTH * 0.25, Steganography.HEIGHT * 0.1);

    baseButton = new Button("Choose Base");
    baseButton.setFont(font);
    baseButton.setMinWidth(borderTopLayout.getPrefWidth() / 2);
    baseButton.setMaxHeight(Double.MAX_VALUE);
    baseButton.setOnAction(actionEvent -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("Base image");
      FileChooser.ExtensionFilter imageFilter
          = new ExtensionFilter("Image Files", Steganography.IMAGE_EXTENSIONS);
      fc.getExtensionFilters().add(imageFilter);
      baseImageSelected(fc.showOpenDialog(window));
    });

    fileButton = new Button("Choose File");
    fileButton.setFont(font);
    fileButton.setMinWidth(borderTopLayout.getPrefWidth() / 2);
    fileButton.setMaxHeight(Double.MAX_VALUE);
    fileButton.setDisable(true);
    fileButton.setOnAction(actionEvent -> {
      FileChooser fc = new FileChooser();
      fileSelected(fc.showOpenDialog(window));
    });

    borderTopLayout.getChildren().addAll(baseButton, fileButton);
    topNode.setTop(borderTopLayout);

    // Rest of BorderPane
    statusLabel = new Label();
    statusLabel.setFont(Font.font("Arial", Steganography.HEIGHT / 50));
    topNode.setCenter(statusLabel);

    // Options panel

    optionsButton = new Button("▼");
    optionsButton.setFont(font);
    optionsButton.setDisable(true);
    optionsButton.setOnAction(HiderLayout::handleEvent);
    topNode.setRight(optionsButton);

    VBox sliderBox = new VBox(5);

    Label optionsLabel = new Label("Alteration Level");
    optionsLabel.setFont(font);

    Slider bitsToStoreSlider = new Slider();
    bitsToStoreSlider.setMin(0);
    bitsToStoreSlider.setMax(3);
    bitsToStoreSlider.setValue(1);
    bitsToStoreSlider.setMinorTickCount(0);
    bitsToStoreSlider.setMajorTickUnit(1);
    bitsToStoreSlider.setBlockIncrement(1);
    bitsToStoreSlider.setSnapToTicks(true);
    bitsToStoreSlider.setShowTickMarks(true);

    sliderBox.getChildren().addAll(optionsLabel, bitsToStoreSlider);
    topNode.setBottom(sliderBox);



    return topNode;
  }

  private static void fileSelected(File sourceFile) {
    if (sourceFile == null) {
      statusLabel.setText("Error reading image");
      return;
    }

    if (sourceFile.length() < Steganography.maxFileSize) {
      statusLabel.setText("Working...");
      Steganography.sourceFile = sourceFile;
      Steganography.compileHide();
    } else {
      statusLabel.setText("file too large".toUpperCase());
    }
  }

  private static void baseImageSelected(File baseImageFile) {
    if (baseImageFile == null) {
      statusLabel.setText("Error reading image");
      return;
    }
    try {
      Steganography.baseImage = ImageIO.read(baseImageFile);

      // Calculate maximum file size
      // First pixel taken to encode bitsToStore
      long potentialSize =
          (Steganography.baseImage.getWidth() * Steganography.baseImage.getHeight() - 1)
          * Steganography.CHANNELS * Steganography.bitsToStore / 8;
      // 1 byte for filename size and a maximum of 255 bytes for filename;
      potentialSize -= 256;
      // 4 bytes for the long that stores the size of the source file;
      potentialSize -= 4;
      Steganography.maxFileSize = potentialSize;

      statusLabel.setText("Max File size: "
          + humanReadableByteCount(Steganography.maxFileSize, false));

    } catch (IOException e) {
      statusLabel.setText("Cannot process this image type");
    }
    fileButton.setDisable(false);
    optionsButton.setDisable(false);
  }


}
