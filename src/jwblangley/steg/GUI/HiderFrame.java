package jwblangley.steg.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import jwblangley.steg.run.Run;

public class HiderFrame extends JFrame implements ActionListener {

	public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	private JButton baseImageButton, fileButton;
	public JLabel statusLabel;

	public HiderFrame() {
		super("Stegonagraphy - jwblangley");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());

		Font font = new Font("Calibri", Font.PLAIN, HEIGHT / 50);

		baseImageButton = new JButton("Choose Base");
		baseImageButton.setPreferredSize(new Dimension(WIDTH / 5, WIDTH / 10));
		baseImageButton.addActionListener(this);
		baseImageButton.setFont(font);
		getContentPane().add(baseImageButton, BorderLayout.LINE_START);

		fileButton = new JButton("Choose File");
		fileButton.setPreferredSize(new Dimension(WIDTH / 5, WIDTH / 10));
		fileButton.addActionListener(this);
		fileButton.setFont(font);
		fileButton.setEnabled(false);
		getContentPane().add(fileButton, BorderLayout.LINE_END);

		statusLabel = new JLabel();
		statusLabel.setFont(new Font("Calibri", Font.PLAIN, HEIGHT / 50));
		statusLabel.setHorizontalAlignment(JLabel.CENTER);
		statusLabel.setPreferredSize(new Dimension(WIDTH / 3, HEIGHT / 25));
		getContentPane().add(statusLabel, BorderLayout.PAGE_END);

		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		JFileChooser fileChooser = new JFileChooser();
		if (ae.getSource() == baseImageButton) {
			FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
			fileChooser.setFileFilter(imageFilter);
		}
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			if (ae.getSource() == baseImageButton) {
				baseImageSelected(fileChooser.getSelectedFile());
			} else {
				fileSelected(fileChooser.getSelectedFile());
			}
		}

	}

	private void baseImageSelected(File imageFile) {
		try {
			Run.baseImage = ImageIO.read(imageFile);
			if (Run.baseImage.getWidth() * Run.baseImage.getHeight() > (Run.extHeaderBits + Run.sizeHeaderBits) / 3 /* 3 colour channels */) {

				Run.maxFileSize = ((Run.baseImage.getWidth() * Run.baseImage.getHeight()) - (Run.extHeaderBits + Run.sizeHeaderBits) / (3*Run.bitsToStore)) * (3*Run.bitsToStore) / 8; // bits to store per channel and 3 channels per pixel
				Run.maxFileSize = Math.min(Run.maxFileSize, Run.ABSOLUTE_FILE_SIZE_LIMIT);
				statusLabel.setText("Max File size:\t".toUpperCase() + humanReadableByteCount(Run.maxFileSize, false));
			} else {
				statusLabel.setText("base image too small".toUpperCase());
				return;
			}
		} catch (IOException e) {
			statusLabel.setText("Cannot process this image type".toUpperCase());
		}
		fileButton.setEnabled(true);
	}

	private void fileSelected(File source) {
		if (source.length() < Run.maxFileSize) {
			statusLabel.setText("Working...".toUpperCase());
			statusLabel.paintImmediately(statusLabel.getVisibleRect());
			Run.sourceFile = source;
			Run.compileHide();
		}else{
			statusLabel.setText("file too large".toUpperCase());
		}
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
