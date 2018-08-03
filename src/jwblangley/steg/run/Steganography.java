package jwblangley.steg.run;

import static jwblangley.steg.run.Utils.addBytesToList;
import static jwblangley.steg.run.Utils.byteListToByteArray;
import static jwblangley.steg.run.Utils.longToByteArray;
import static jwblangley.steg.run.Utils.noOverrideFile;
import static jwblangley.steg.run.Utils.numBitsToMask;
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
import javafx.application.Platform;
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

  // per colour channel, 1,2,4 or 8 - 2 by default
  public static int bitsToStore = 2;
  // Allows file names to be up to 255 characters long
  public static final int NAME_HEADER_SIZE = 255;
  // R G B
  public static final int CHANNELS = 3;
  // Must be a multiple of CHANNELS - 54KB
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
    int pixR = firstCol.getRed() & ((2 & bitNum) >> 1);
    int pixB = firstCol.getBlue() & 1 & bitNum;
    resultImage.setRGB(0, 0, new Color(pixR, firstCol.getGreen(), pixB).getRGB());

    // To store indeterminate number of bytes in header
    List<Byte> headerBytes = new ArrayList<>();

    // Filename: length then bytes in UTF-8
    byte[] fileNameBytes = sourceFile.getName().getBytes(StandardCharsets.UTF_8);

    if (fileNameBytes.length > NAME_HEADER_SIZE) {
      Platform.runLater(() -> HiderLayout.statusLabel.setText("Filename too long"));
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

      byte[] buf = byteListToByteArray(headerBytes);

      byte bufferByte = 0;
      int bitPos = 0;
      int bytePos = 0;

      boolean complete = false;

      // Data write loop
      for (int y = 0; y < resultImage.getHeight(); y++) {
        for (int x = (y == 0 ? 1 : 0); x < resultImage.getWidth(); x++) {

          // Base colour
          Color baseCol = new Color(baseImage.getRGB(x, y));
          int[] pixelRGB = new int[]{baseCol.getRed(), baseCol.getGreen(), baseCol.getBlue()};

          int readLen = 0;
          for (int c = 0; c < CHANNELS; c++) {
            if (!complete) {
              if (bitPos >= 8) {
                bitPos = 0;
                bytePos++;
                if (bytePos < buf.length) {
                  bufferByte = buf[bytePos];
                } else {
                  bytePos = 0;
                  buf = new byte[BUFFER_SIZE];
                  readLen = in.read(buf);
                  if (readLen < 0) {
                    // byte stream complete
                    complete = true;
                    break;
                  } else {
                    bufferByte = buf[bytePos];
                    bytePos++;
                  }
                }
              }

              switch (bitsToStore) {
                case 8:
                  pixelRGB[c] = bufferByte + 128;
                  break;
                case 4:
                  pixelRGB[c] &= 0xF0;
                  pixelRGB[c] += (bufferByte & (0xF << 4 - bitPos)) >> (4 - bitPos);
                  break;
                case 2:
                  pixelRGB[c] &= 0xFC;
                  pixelRGB[c] += (bufferByte & (0x3 << 6 - bitPos)) >> (6 - bitPos);
                  break;
                case 1:
                  pixelRGB[c] &= 0xFE;
                  pixelRGB[c] += (bufferByte & (0x1 << 7 - bitPos)) >> (7 - bitPos);
              }
              bitPos += bitsToStore;
            }
          }
          resultImage.setRGB(x, y, new Color(pixelRGB[0], pixelRGB[1], pixelRGB[2]).getRGB());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      Platform.runLater(() -> HiderLayout.statusLabel.setText("Error reading file"));
      return;
    }

    saveImage(resultImage, "out");
    Platform.runLater(() -> HiderLayout.statusLabel.setText("Done"));

  }

  public static void compileReveal() {

    // Calculate bits per pixel
    Color firstCol = new Color(toBeRevealedImage.getRGB(0, 0));
    int bitsPerPixel = (firstCol.getRed() & 0x1) << 1 + firstCol.getBlue() & 0x1;
    Byte fileNameLength = null;
    String fileName = null;
    Long dataSize = null;

    List<Byte> byteList = new ArrayList<>();
    byte currentByte = 0;
    int bitIndex = 0;

    // Start reading in bytes from image
    for (int y = 0; y < toBeRevealedImage.getHeight(); y++) {
      for (int x = (y == 0 ? 1 : 0); x < toBeRevealedImage.getWidth(); x++) {
        Color baseCol = new Color(toBeRevealedImage.getRGB(x, y));
        int[] pixelRGB = new int[]{baseCol.getRed(), baseCol.getGreen(), baseCol.getBlue()};

        for (int c = 0; c < CHANNELS; c++) {
          currentByte <<= bitsPerPixel;
          currentByte += pixelRGB[c] & numBitsToMask(bitsPerPixel);
          bitIndex += bitsPerPixel;

          // Byte complete
          if (bitIndex >= 8) {
            bitIndex = 0;
            byteList.add(currentByte);
            currentByte = 0;

            if (fileNameLength == null) {
              // No data queue size check needed as fileNameLength is only one byte
              assert byteList.size() == 1 : "Filename length not correctly processed";
              fileNameLength = byteList.get(0);
              byteList.clear();
            } else if (fileName == null && byteList.size() >= fileNameLength) {
              // Comparison order chosen for efficiency
              assert byteList.size() == fileNameLength
                  : "Filename has not been processed correctly";
              fileName = new String(byteListToByteArray(byteList), StandardCharsets.UTF_8);
              byteList.clear();
            } else if (dataSize == null && byteList.size() >= 8) {
              assert byteList.size() == 8 : "Data size has not been processed correctly";
              // 8 bytes in a long
              dataSize = 0L;
              for (int i = 0; i < 8; i++) {
                dataSize += byteList.get(i);
                dataSize <<= i != 7 ? 8 : 0;
              }
              byteList.clear();
            } else {
              // TODO read in data as usual. List will only contain bytes of file after
              System.out.println(fileName + "  " + dataSize);
              return;
            }
          }
        }

      }
    }

    //TODO: calculate data in dataOut and fileName
    byte[] dataOut = null;
    String filename = null;

    try {
      FileOutputStream stream = new FileOutputStream(noOverrideFile(filename));
      stream.write(dataOut);
      stream.close();
    } catch (FileNotFoundException e) {
      Platform
          .runLater(() -> RevealerLayout.statusLabel.setText("Error finding location to write"));
    } catch (IOException e) {
      Platform.runLater(() -> RevealerLayout.statusLabel.setText("Error writing file"));
    }
    Platform.runLater(() -> RevealerLayout.statusLabel.setText("Done"));

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
