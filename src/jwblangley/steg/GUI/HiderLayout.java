package jwblangley.steg.GUI;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import jwblangley.steg.run.Steganography;

public class HiderLayout {

  public static Pane layout() {

    BorderPane topNode = new BorderPane();
    Font font = new Font("Arial", Steganography.HEIGHT / 35);


    // Border Top
    HBox borderTopLayout = new HBox(5);
    borderTopLayout.setPrefSize(Steganography.WIDTH * 0.15, Steganography.HEIGHT * 0.1);

    Button hideButton = new Button("Choose Base");
    hideButton.setFont(font);
    hideButton.setMinWidth(borderTopLayout.getPrefWidth() / 2);
    hideButton.setMaxHeight(Double.MAX_VALUE);
    //TODO action handler

    Button revealButton = new Button("Choose File");
    revealButton.setFont(font);
    revealButton.setMinWidth(borderTopLayout.getPrefWidth() / 2);
    revealButton.setMaxHeight(Double.MAX_VALUE);
    revealButton.setDisable(true);
    //TODO action handler

    borderTopLayout.getChildren().addAll(hideButton, revealButton);
    topNode.setTop(borderTopLayout);

    // Rest of BorderPane
    Label statusLabel = new Label();
    statusLabel.setFont(font);
    topNode.setCenter(statusLabel);

    return topNode;
  }

}
