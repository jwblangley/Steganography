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

public class RevealerFrame extends JFrame implements ActionListener {

	public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width, HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	private JButton fileButton;
	public JLabel statusLabel;

	public RevealerFrame() {
		super("Stegonagraphy - jwblangley");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());

		Font font = new Font("Calibri", Font.PLAIN, HEIGHT / 50);

		fileButton = new JButton("Choose Image");
		fileButton.setPreferredSize(new Dimension(WIDTH / 3, WIDTH / 10));
		fileButton.addActionListener(this);
		fileButton.setFont(font);
		getContentPane().add(fileButton, BorderLayout.CENTER);

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
		FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
		fileChooser.setFileFilter(imageFilter);
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			try {
				statusLabel.setText("Working...".toUpperCase());
				statusLabel.paintImmediately(statusLabel.getVisibleRect());
				Run.toBeRevealedImage = ImageIO.read(fileChooser.getSelectedFile());
				Run.compileReveal();
			} catch (IOException e) {
				statusLabel.setText("Error Reading File".toUpperCase());
				e.printStackTrace();
			}
		}

	}
}
