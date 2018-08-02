package jwblangley.steg.run;

import static jwblangley.steg.run.Utils.addBytesToList;
import static jwblangley.steg.run.Utils.byteListToByteArray;
import static jwblangley.steg.run.Utils.longToByteArray;
import static jwblangley.steg.run.Utils.noOverrideFile;
import static jwblangley.steg.run.Utils.saveImage;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import jwblangley.steg.GUI.HiderLayout;
import jwblangley.steg.GUI.MenuLayout;
import jwblangley.steg.GUI.RevealerLayout;

public class Steganography extends Application {

  public static File sourceFile;
  public static BufferedImage baseImage, toBeRevealedImage;
  public static long maxFileSize;

  // per colour channel, 1,2,4 or 8
  //TODO: as slider
  public static int bitsToStore = 2;
  // Allows file names to be up to 255 characters long
  public static final int NAME_HEADER_SIZE = 255;
  // R G B
  public static final int CHANNELS = 3;
  // Must be a multiple of CHANNELS - 3KB
  public static final int BUFFER_SIZE = CHANNELS * 16 * 1024;

  public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
  public static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

  public static String[] IMAGE_EXTENSIONS = Arrays.stream(ImageIO.getReaderFileSuffixes())
      .map(s -> "*." + s)
      .toArray(String[]::new);

  private static Stage displayWindow;

  public static void main(String[] args) {
    launch(args);
  }

  public static void compileHide() {
    //TODO assertions (size of image)

    BufferedImage resultImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    // Store bitsToStore within first pixel for rest of image
    int bitNum;
    for (bitNum = 0; bitNum < 4; bitNum++) {
      if (1 >> bitNum == bitsToStore) {
        break;
      }
    }

    Color firstCol = new Color(baseImage.getRGB(0, 0));
    int r = firstCol.getRed() & ((2 & bitNum) >> 1);
    int b = firstCol.getBlue() & 1 & bitNum;

    resultImage.setRGB(0, 0, new Color(r, firstCol.getGreen(), b).getRGB());

    List<Byte> headerBytes = new ArrayList<>();

    // Filename: size then bytes in UTF-8
    //TODO check that getName includes file extension
    System.out.println(sourceFile.getName());
    byte[] fileNameBytes = sourceFile.getName().getBytes(StandardCharsets.UTF_8);

    if (fileNameBytes.length > NAME_HEADER_SIZE) {
      HiderLayout.statusLabel.setText("Filename too long");
      return;
    }
    // Cannot overflow byte - enforced with NAME_HEADER_SIZE
    byte nameSize = (byte) fileNameBytes.length;

    headerBytes.add(nameSize);
    addBytesToList(headerBytes, fileNameBytes);

    // Data size
    long dataSize = sourceFile.length();
    addBytesToList(headerBytes, longToByteArray(dataSize));

    // Data and write to image
    try {
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
      byte[] buf = Arrays.copyOf(byteListToByteArray(headerBytes), BUFFER_SIZE);
      int b = 0;
      int len = headerBytes.size();
      for (int y = 0; y < resultImage.getHeight(); y++) {
        for (int x = 0; x < resultImage.getWidth(); x++) {
          // Base colour
          Color col = new Color(baseImage.getRGB(x, y));
          int[] pixelRGB = new int[]{col.getRed(), col.getGreen(), col.getGreen()};

          for (int c = 0; c < CHANNELS; c++) {
            if (bitsToStore == 8) {
              byte dataByte = buf[b];
              pixelRGB[c] = dataByte;
              if (b < len - 1) {
                b++;
              } else {
                len = in.read(buf);
                b = 0;
              }
            } else if (bitsToStore == 4) {
              //TODO
            } else if (bitsToStore == 2) {
              //TODO
            } else if (bitsToStore == 1) {
              //TODO
            }
          }

          resultImage.setRGB(x, y, new Color(pixelRGB[0], pixelRGB[1], pixelRGB[2]).getRGB());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      HiderLayout.statusLabel.setText("Error reading file");
      return;
    }

    saveImage(resultImage, "out");
    HiderLayout.statusLabel.setText("Done");


  }

  public static void compileReveal() {
    //TODO: calculate data in dataOut and fileName
    byte[] dataOut = null;
    String filename = null;

    try {
      FileOutputStream stream = new FileOutputStream(noOverrideFile(filename));
      stream.write(dataOut);
      stream.close();
    } catch (FileNotFoundException e) {
      RevealerLayout.statusLabel.setText("Error finding file");
    } catch (IOException e) {
      RevealerLayout.statusLabel.setText("Error writing file");
    }
    RevealerLayout.statusLabel.setText("Done");

  }

  @Override
  public void start(Stage window) {
    displayWindow = window;

    Scene scene = new Scene(MenuLayout.layout(window));

    window.setTitle("Steganography");
    window.setScene(scene);
    window.centerOnScreen();
    window.show();
  }
}
