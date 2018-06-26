package jwblangley.steg.run;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class Utils {

  public static File unusedFile(String filename, String fileExt) {
    int counter = 1;
    File tempFile;
    do {
      tempFile = new File(filename + counter + "." + fileExt);
      counter++;
    } while (tempFile.exists());
    return tempFile;
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
    //TODO: test this
    final int SIZE_OF_LONG = 8;
    byte[] arr = new byte[SIZE_OF_LONG];
    for (int i = SIZE_OF_LONG - 1; i >= 0; i--) {
      arr[i] = (byte) (((0xFF << (i * 8)) & x) >> (i * 8));
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

}
