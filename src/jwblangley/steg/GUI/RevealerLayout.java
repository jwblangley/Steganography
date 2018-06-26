package jwblangley.steg.GUI;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import jwblangley.steg.run.Steganography;

public class RevealerLayout {

  public static Pane layout() {
    HBox layout = new HBox();
    layout.setPrefSize(Steganography.WIDTH * 0.075, Steganography.HEIGHT * 0.1);

    Font font = new Font("Arial", Steganography.HEIGHT / 35);

    Button imageButton = new Button("Choose Image");
    imageButton.setFont(font);
    HBox.setHgrow(imageButton, Priority.ALWAYS);
    imageButton.setMaxWidth(Double.MAX_VALUE);
    imageButton.setMaxHeight(Double.MAX_VALUE);
    // TODO action handler

    layout.getChildren().add(imageButton);

    return layout;
  }

}
