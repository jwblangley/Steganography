package jwblangley.steg.GUI;

import java.io.File;
import java.io.IOException;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import jwblangley.steg.run.Steganography;

public class RevealerLayout {

  public static Label statusLabel;

  public static Pane layout(Stage window) {
    BorderPane topNode = new BorderPane();
    Font font = new Font("Arial", Steganography.HEIGHT / 50);

    // Border top
    HBox borderTopLayout = new HBox();
    borderTopLayout.setPrefSize(Steganography.WIDTH * 0.25, Steganography.HEIGHT * 0.1);

    Button imageButton = new Button("Choose Image");
    imageButton.setFont(font);
    HBox.setHgrow(imageButton, Priority.ALWAYS);
    imageButton.setMaxWidth(Double.MAX_VALUE);
    imageButton.setMaxHeight(Double.MAX_VALUE);
    imageButton.setOnAction(actionEvent -> reveal(window));

    borderTopLayout.getChildren().add(imageButton);
    topNode.setTop(borderTopLayout);

    // Rest of BorderPane
    statusLabel = new Label();
    statusLabel.setFont(Font.font("Arial", Steganography.HEIGHT / 75));
    topNode.setCenter(statusLabel);

    return topNode;
  }

  private static void reveal(Stage window) {
    // File chooser
    FileChooser fc = new FileChooser();
    fc.setTitle("Image file to reveal from");
    FileChooser.ExtensionFilter imageFilter
        = new ExtensionFilter("Image Files", Steganography.IMAGE_EXTENSIONS);
    fc.getExtensionFilters().add(imageFilter);

    File imageFile = fc.showOpenDialog(window);
    if (imageFile != null) {
      try {
        statusLabel.setText("Working...");
        Steganography.toBeRevealedImage = ImageIO.read(imageFile);
        new Thread(Steganography::compileReveal).start();
      } catch (IOException e) {
        e.printStackTrace();
        statusLabel.setText("Error: Failed to read file");
      }
    } else {
      statusLabel.setText("Error: Cannot read file");
      return;
    }
  }

}
