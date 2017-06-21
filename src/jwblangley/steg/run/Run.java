package jwblangley.steg.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;

import jwblangley.steg.GUI.HiderFrame;
import jwblangley.steg.GUI.RevealerFrame;

public class Run {

	public static File sourceFile;
	public static BufferedImage baseImage, toBeRevealedImage;
	public static long maxFileSize;

	public static final int bitsToStore = 2 /* per colour channel, 1,2 or 4 */, sizeHeaderBits = 56; // allows up to 2^56 bytes (72 petabytes) //56 is divisible by 1,2,4 (bits per pixel)
	public static final int extHeaderBits = 12 * 8; // allows file extensions to be up to 12 characters long (divides by 1,2,4)
	public static final long ABSOLUTE_FILE_SIZE_LIMIT = Integer.MAX_VALUE; // mas storage for array
	public static HiderFrame hider;
	public static RevealerFrame revealer;
	public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;

	private static JButton hiderButton, revealerButton;

	public static void main(String[] args) {
		Font font = new Font("Calibri", Font.PLAIN, HEIGHT / 50);
		JFrame menuFrame = new JFrame("Stegonagraphy - jwblangley");
		menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menuFrame.getContentPane().setLayout(new BorderLayout());

		hiderButton = new JButton("Hide File");
		hiderButton.setPreferredSize(new Dimension(WIDTH / 5, WIDTH / 10));
		hiderButton.setFont(font);
		hiderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				hider = new HiderFrame();
				hiderButton.setEnabled(false);
				revealerButton.setEnabled(false);
			}
		});
		menuFrame.getContentPane().add(hiderButton, BorderLayout.LINE_START);

		revealerButton = new JButton("Reveal File");
		revealerButton.setPreferredSize(new Dimension(WIDTH / 5, WIDTH / 10));
		revealerButton.setFont(font);
		revealerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				revealer = new RevealerFrame();
				hiderButton.setEnabled(false);
				revealerButton.setEnabled(false);
			}
		});
		menuFrame.getContentPane().add(revealerButton, BorderLayout.LINE_END);

		menuFrame.pack();
		menuFrame.setLocationRelativeTo(null);
		menuFrame.setResizable(false);
		menuFrame.setVisible(true);

		// hider = new HiderFrame();
		// revealer = new Revealer();
	}

	public static void compileHide() {
		BufferedImage resultImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_RGB);

		try {
			byte[] dataArray = Files.readAllBytes(sourceFile.toPath());

			long fileLength = sourceFile.length();

			String headerBitString = "";

			// file ext header
			Pattern p = Pattern.compile(".+\\.(.+)$");
			Matcher m = p.matcher(sourceFile.getPath());
			m.matches();
			String fileExt = m.group(1);
			for (char l : fileExt.toCharArray()) {
				headerBitString = headerBitString + new String(new char[8 - Integer.toBinaryString((int) (l)).length()]).replace("\0", "0") + Integer.toBinaryString((int) (l));
			}
			for (int i = 0; i < extHeaderBits / 8 - fileExt.length(); i++) {
				headerBitString = headerBitString + new String(new char[8]).replace("\0", "0");
			}

			// file Size header
			headerBitString = headerBitString + new String(new char[sizeHeaderBits - Long.toBinaryString(fileLength).length()]).replace("\0", "0") + Long.toBinaryString(fileLength);

			int counter = 0, dataCounter = 0;
			for (int y = 0; y < resultImage.getHeight(); y++) {
				for (int x = 0; x < resultImage.getWidth(); x++) {
					Color originalPixel = new Color(baseImage.getRGB(x, y));
					int[] pixelRGB = new int[] { originalPixel.getRed(), originalPixel.getGreen(), originalPixel.getBlue() };
					for (int c = 0; c < 3; c++) {
						if (counter < extHeaderBits + sizeHeaderBits) {
							// header
							String bitSequence = headerBitString.substring(counter, counter + bitsToStore);
							byte addValue = 0;
							for (int i = 0; i < bitsToStore; i++) {
								addValue += (Integer.parseInt(bitSequence.charAt(i) + "") << bitsToStore - i - 1);
							}
							pixelRGB[c] = (((pixelRGB[c] >> bitsToStore) << bitsToStore) + addValue);
							counter += bitsToStore;
						} else {
							// data bits
							int byteIndex = (dataCounter * bitsToStore) / 8;
							if (byteIndex >= dataArray.length) {
								continue;
							}
							int bitIndex = (dataCounter * bitsToStore) % 8;
							String byteString = new String(new char[8 - Integer.toBinaryString(Byte.toUnsignedInt(dataArray[byteIndex])).length()]).replace("\0", "0") + Integer.toBinaryString(Byte.toUnsignedInt(dataArray[byteIndex]));
							String bitSequence = byteString.substring(bitIndex, bitIndex + bitsToStore);
							byte addValue = 0;
							for (int i = 0; i < bitsToStore; i++) {
								addValue += (Integer.parseInt(bitSequence.charAt(i) + "") << bitsToStore - i - 1);
							}
							pixelRGB[c] = (((pixelRGB[c] >> bitsToStore) << bitsToStore) + addValue);
							dataCounter++;
						}
					}
					resultImage.setRGB(x, y, new Color(pixelRGB[0], pixelRGB[1], pixelRGB[2]).getRGB());
				}
			}
			saveImage(resultImage, "out");
			hiderButton.setEnabled(true);
			revealerButton.setEnabled(true);
			hider.statusLabel.setText("done".toUpperCase());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void compileReveal() {
		String fileExt = "";
		long fileSize = 0;
		byte[] dataOut = null;

		int bitCounter = 0, currentByte = 0, byteArrayCounter = 0;
		outer: for (int y = 0; y < toBeRevealedImage.getHeight(); y++) {
			for (int x = 0; x < toBeRevealedImage.getWidth(); x++) {
				for (int c = 0; c < 3; c++) {
					Color pixel = new Color(toBeRevealedImage.getRGB(x, y));
					int[] pixelRGB = new int[] { pixel.getRed(), pixel.getGreen(), pixel.getBlue() };
					int extractedData = pixelRGB[c] & ((bitsToStore << 1) - 1);
					if (bitCounter < extHeaderBits) {
						// file Ext
						if (bitCounter != 0 && bitCounter % 8 == 0) {
							fileExt = fileExt + (char) currentByte;
							currentByte = 0;
						}
						currentByte += extractedData << (8 - bitsToStore * (1 + (bitCounter % 8) / bitsToStore));
						bitCounter += bitsToStore;
					} else {
						if (bitCounter == extHeaderBits) {
							fileExt = fileExt.replaceAll("\0", "");
						}
						if (bitCounter < extHeaderBits + sizeHeaderBits) {
							// fileSize
							fileSize <<= 2;
							fileSize += extractedData;
							bitCounter += bitsToStore;
						} else {
							if (byteArrayCounter >= fileSize) {
								break outer;
							}
							if (bitCounter == extHeaderBits + sizeHeaderBits) {
								dataOut = new byte[(int) fileSize];
								currentByte = 0;
							}
							if (bitCounter != extHeaderBits + sizeHeaderBits && bitCounter % 8 == 0) {
								dataOut[byteArrayCounter] = (byte) currentByte;
								byteArrayCounter++;
								currentByte = 0;
							}
							currentByte += extractedData << (8 - bitsToStore * (1 + (bitCounter % 8) / bitsToStore));
							bitCounter += bitsToStore;

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
			revealer.statusLabel.setText("error finding file".toUpperCase());
		} catch (IOException e) {
			revealer.statusLabel.setText("error writing file".toUpperCase());
		}
		hiderButton.setEnabled(true);
		revealerButton.setEnabled(true);
		revealer.statusLabel.setText("done".toUpperCase());

	}

	public static boolean[] byteArrayToBitArray(byte[] bytes) {
		boolean[] bits = new boolean[bytes.length * 8];
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[i / 8] & (1 << (7 - (i % 8)))) > 0)
				bits[i] = true;
		}
		return bits;
	}

	static File unusedFile(String filename, String fileExt) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
