package jwblangley.steg.run;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class Utils {

  public static File unusedFile(String filename, String fileExt, boolean dot) {
    int counter = 1;
    File tempFile = new File(filename + (dot ? "." : "") + fileExt);
    while (tempFile.exists()) {
      tempFile = new File(filename + "_" + counter + (dot ? "." : "") + fileExt);
      counter++;
    }
    return tempFile;
  }

  public static File unusedFile(String filename, String fileExt) {
    return unusedFile(filename, fileExt, true);
  }

  public static File noOverrideFile(String filename) {
    File file = new File(filename);
    if (!file.exists()) {
      return file;
    }
    int dotIndex = filename.indexOf(".");
    if (dotIndex < 0) {
      // File name without dot
      return unusedFile(filename, "", false);
    }
    String name = filename.substring(0, dotIndex);
    String fileExt = filename.substring(dotIndex + 1);
    return unusedFile(name, fileExt);
  }

  public static void saveImage(BufferedImage img, String fileNameStub) {
    try {
      ImageIO.write(img, "png", unusedFile(fileNameStub, "png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void addBytesToList(List<Byte> bList, byte[] bArr) {
    for (byte b : bArr) {
      bList.add(b);
    }
  }

  public static byte[] longToByteArray(long x) {
    // Big endian
    final int SIZE_OF_LONG = 8;
    byte[] arr = new byte[SIZE_OF_LONG];
    for (int i = SIZE_OF_LONG - 1; i >= 0; i--) {
      arr[SIZE_OF_LONG - 1 - i] = (byte) (((0xFF << (i * 8)) & x) >> (i * 8));
    }
    return arr;
  }

  public static byte[] byteListToByteArray(List<Byte> bList) {
    byte[] bArr = new byte[bList.size()];
    for (int i = 0; i < bList.size(); i++) {
      bArr[i] = bList.get(i);
    }
    return bArr;
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

  public static int numOfBitsToMask(int numBits) {
    return (1 << numBits) - 1;
  }

}
