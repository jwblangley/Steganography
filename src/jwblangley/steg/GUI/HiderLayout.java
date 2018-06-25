package jwblangley.steg.GUI;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import jwblangley.steg.run.Steganography;

public class HiderLayout {

  public static Pane layout() {
    HBox layout = new HBox(5);
    layout.setPrefSize(Steganography.WIDTH * 0.15, Steganography.HEIGHT * 0.1);

    Font font = new Font("Arial", Steganography.HEIGHT / 35);

    Button hideButton = new Button("Hide File");
    hideButton.setFont(font);
    hideButton.setMinWidth(layout.getPrefWidth() / 2);
    hideButton.setMaxHeight(Double.MAX_VALUE);
    //TODO action handler

    Button revealButton = new Button("Reveal File");
    revealButton.setFont(font);
    revealButton.setMinWidth(layout.getPrefWidth() / 2);
    revealButton.setMaxHeight(Double.MAX_VALUE);
    //TODO action handler

    layout.getChildren().addAll(hideButton, revealButton);

    return layout;
  }

}
