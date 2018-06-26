package jwblangley.steg.GUI;

import java.io.File;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import jwblangley.steg.run.Steganography;

public class HiderLayout {

  private static Button baseButton, fileButton;
  private static Stage window;
  public static Label statusLabel;

  public static Pane layout(Stage windowIn) {

    window = windowIn;

    BorderPane topNode = new BorderPane();
    Font font = new Font("Arial", Steganography.HEIGHT / 35);

    // Border Top
    HBox borderTopLayout = new HBox(5);
    borderTopLayout.setPrefSize(Steganography.WIDTH * 0.15, Steganography.HEIGHT * 0.1);

    baseButton = new Button("Choose Base");
    baseButton.setFont(font);
    baseButton.setMinWidth(borderTopLayout.getPrefWidth() / 2);
    baseButton.setMaxHeight(Double.MAX_VALUE);
    baseButton.setOnAction(HiderLayout::handleEvent);

    fileButton = new Button("Choose File");
    fileButton.setFont(font);
    fileButton.setMinWidth(borderTopLayout.getPrefWidth() / 2);
    fileButton.setMaxHeight(Double.MAX_VALUE);
    fileButton.setDisable(true);
    fileButton.setOnAction(HiderLayout::handleEvent);

    borderTopLayout.getChildren().addAll(baseButton, fileButton);
    topNode.setTop(borderTopLayout);

    // Rest of BorderPane
    statusLabel = new Label();
    statusLabel.setFont(Font.font("Arial", Steganography.HEIGHT / 50));
    topNode.setCenter(statusLabel);

    return topNode;
  }

  private static void handleEvent(ActionEvent e) {
    // File chooser
    FileChooser fc = new FileChooser();

    if (e.getSource() == baseButton) {
      fc.setTitle("Base image");
      FileChooser.ExtensionFilter imageFilter
          = new ExtensionFilter("Image Files", Steganography.IMAGE_EXTENSIONS);
      fc.getExtensionFilters().add(imageFilter);
      baseImageSelected(fc.showOpenDialog(window));
    } else if (e.getSource() == fileButton) {
      fileSelected(fc.showOpenDialog(window));
    }
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
      if (Steganography.baseImage.getWidth() * Steganography.baseImage.getHeight()
          > (Steganography.EXT_HEADER_BITS + Steganography.SIZE_HEADER_BITS)
          / 3 /* 3 colour channels */) {
        Steganography.maxFileSize =
            ((Steganography.baseImage.getWidth() * Steganography.baseImage.getHeight())
                - (Steganography.EXT_HEADER_BITS + Steganography.SIZE_HEADER_BITS) / (3
                * Steganography.BITS_TO_STORE)) * (3 * Steganography.BITS_TO_STORE)
                / 8; // bits to store per channel and 3 channels per pixel
        Steganography.maxFileSize = Math
            .min(Steganography.maxFileSize, Steganography.ABSOLUTE_FILE_SIZE_LIMIT);
        statusLabel.setText(
            "Max File size:\t".toUpperCase() + humanReadableByteCount(Steganography.maxFileSize,
                false));
      } else {
        statusLabel.setText("Base image too small");
        return;
      }
    } catch (IOException e) {
      statusLabel.setText("Cannot process this image type");
    }
    fileButton.setDisable(false);
  }

  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

}
