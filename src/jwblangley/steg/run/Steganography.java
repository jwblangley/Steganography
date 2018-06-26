package jwblangley.steg.run;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  // 2 per colour channel, 1,2,4 or 8
  public static final int BITS_TO_STORE = 2;
  // Allows up to 2^56 bytes (72 petabytes) - 56 is divisible by 1,2,4,8 (bits per pixel)
  public static final int SIZE_HEADER_BITS = 56;
  // Allows file extensions to be up to 255 characters long
  public static final int NAME_HEADER_SIZE = 255;

  // R G B
  public static final int CHANNELS = 3;

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
    //TODO assertions

    BufferedImage resultImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    try {
      List<Byte> computedBytes = new ArrayList<>();

      // Bits to store
      computedBytes.add((byte) BITS_TO_STORE);

      // Filename: size then bytes in UTF-8
      byte[] fileNameBytes = sourceFile.getName().getBytes(StandardCharsets.UTF_8);
      assert fileNameBytes.length <= NAME_HEADER_SIZE : "Filename too long";
      byte nameSize = (byte) fileNameBytes.length;
      computedBytes.add(nameSize);
      addBytesToList(computedBytes, fileNameBytes);


      //TODO: Done up to here

      byte[] dataArray = Files.readAllBytes(sourceFile.toPath());

      long fileLength = sourceFile.length();





      for (char l : fileExt.toCharArray()) {
        headerBitString =
            headerBitString + new String(new char[8 - Integer.toBinaryString((int) (l)).length()])
                .replace("\0", "0") + Integer.toBinaryString((int) (l));
      }
      for (int i = 0; i < EXT_HEADER_BITS / 8 - fileExt.length(); i++) {
        headerBitString = headerBitString + new String(new char[8]).replace("\0", "0");
      }

      // file Size header
      headerBitString = headerBitString + new String(
          new char[SIZE_HEADER_BITS - Long.toBinaryString(fileLength).length()]).replace("\0", "0")
          + Long.toBinaryString(fileLength);

      int counter = 0, dataCounter = 0;
      for (int y = 0; y < resultImage.getHeight(); y++) {
        for (int x = 0; x < resultImage.getWidth(); x++) {
          Color originalPixel = new Color(baseImage.getRGB(x, y));
          int[] pixelRGB = new int[]{originalPixel.getRed(), originalPixel.getGreen(),
              originalPixel.getBlue()};
          for (int c = 0; c < 3; c++) {
            if (counter < EXT_HEADER_BITS + SIZE_HEADER_BITS) {
              // header
              String bitSequence = headerBitString.substring(counter, counter + BITS_TO_STORE);
              byte addValue = 0;
              for (int i = 0; i < BITS_TO_STORE; i++) {
                addValue += (Integer.parseInt(bitSequence.charAt(i) + "") << BITS_TO_STORE - i - 1);
              }
              pixelRGB[c] = (((pixelRGB[c] >> BITS_TO_STORE) << BITS_TO_STORE) + addValue);
              counter += BITS_TO_STORE;
            } else {
              // data bits
              int byteIndex = (dataCounter * BITS_TO_STORE) / 8;
              if (byteIndex >= dataArray.length) {
                continue;
              }
              int bitIndex = (dataCounter * BITS_TO_STORE) % 8;
              String byteString = new String(
                  new char[8 - Integer.toBinaryString(Byte.toUnsignedInt(dataArray[byteIndex]))
                      .length()]).replace("\0", "0") + Integer
                  .toBinaryString(Byte.toUnsignedInt(dataArray[byteIndex]));
              String bitSequence = byteString.substring(bitIndex, bitIndex + BITS_TO_STORE);
              byte addValue = 0;
              for (int i = 0; i < BITS_TO_STORE; i++) {
                addValue += (Integer.parseInt(bitSequence.charAt(i) + "") << BITS_TO_STORE - i - 1);
              }
              pixelRGB[c] = (((pixelRGB[c] >> BITS_TO_STORE) << BITS_TO_STORE) + addValue);
              dataCounter++;
            }
          }
          resultImage.setRGB(x, y, new Color(pixelRGB[0], pixelRGB[1], pixelRGB[2]).getRGB());
        }
      }
      saveImage(resultImage, "out");
      HiderLayout.statusLabel.setText("Done");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void compileReveal() {
    String fileExt = "";
    long fileSize = 0;
    byte[] dataOut = null;

    int bitCounter = 0, currentByte = 0, byteArrayCounter = 0;
    outer:
    for (int y = 0; y < toBeRevealedImage.getHeight(); y++) {
      for (int x = 0; x < toBeRevealedImage.getWidth(); x++) {
        for (int c = 0; c < 3; c++) {
          Color pixel = new Color(toBeRevealedImage.getRGB(x, y));
          int[] pixelRGB = new int[]{pixel.getRed(), pixel.getGreen(), pixel.getBlue()};
          int extractedData = pixelRGB[c] & ((BITS_TO_STORE << 1) - 1);
          if (bitCounter < EXT_HEADER_BITS) {
            // file Ext
            if (bitCounter != 0 && bitCounter % 8 == 0) {
              fileExt = fileExt + (char) currentByte;
              currentByte = 0;
            }
            currentByte +=
                extractedData << (8 - BITS_TO_STORE * (1 + (bitCounter % 8) / BITS_TO_STORE));
            bitCounter += BITS_TO_STORE;
          } else {
            if (bitCounter == EXT_HEADER_BITS) {
              fileExt = fileExt.replaceAll("\0", "");
            }
            if (bitCounter < EXT_HEADER_BITS + SIZE_HEADER_BITS) {
              // fileSize
              fileSize <<= BITS_TO_STORE;
              fileSize += extractedData;
              bitCounter += BITS_TO_STORE;
            } else {
              if (byteArrayCounter >= fileSize) {
                break outer;
              }
              if (bitCounter == EXT_HEADER_BITS + SIZE_HEADER_BITS) {
                dataOut = new byte[(int) fileSize];
                currentByte = 0;
              }
              if (bitCounter != EXT_HEADER_BITS + SIZE_HEADER_BITS && bitCounter % 8 == 0) {
                dataOut[byteArrayCounter] = (byte) currentByte;
                byteArrayCounter++;
                currentByte = 0;
              }
              currentByte +=
                  extractedData << (8 - BITS_TO_STORE * (1 + (bitCounter % 8) / BITS_TO_STORE));
              bitCounter += BITS_TO_STORE;

            }
          }
        }
      }
    }
    try {
      FileOutputStream stream = new FileOutputStream(unusedFile("reveal", fileExt));
      stream.write(dataOut);
      stream.close();
    } catch (FileNotFoundException e) {
      RevealerLayout.statusLabel.setText("Error finding file");
    } catch (IOException e) {
      RevealerLayout.statusLabel.setText("Error writing file");
    }
    RevealerLayout.statusLabel.setText("Done");

  }

  private static void addBytesToList(List<Byte> bList, byte[] bArr) {
    for (byte b : bArr) {
      bList.add(b);
    }
  }

  public static boolean[] byteArrayToBitArray(byte[] bytes) {
    boolean[] bits = new boolean[bytes.length * 8];
    for (int i = 0; i < bytes.length * 8; i++) {
      if ((bytes[i / 8] & (1 << (7 - (i % 8)))) > 0) {
        bits[i] = true;
      }
    }
    return bits;
  }

  private static File unusedFile(String filename, String fileExt) {
    int counter = 1;
    File tempFile;
    do {
      tempFile = new File(filename + counter + "." + fileExt);
      counter++;
    } while (tempFile.exists());
    return tempFile;
  }

  private static void saveImage(BufferedImage img, String fileNameStub) {
    try {
      ImageIO.write(img, "png", unusedFile(fileNameStub, "png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
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
