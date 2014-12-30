import java.awt.Button;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class View extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public View()
	{
		setTitle("X3 Product Map");
		setSize(800, 450);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		InitializePanels();
		
		setVisible(true);
	}

	private void InitializePanels()
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		
		JPanel secondaryPanel = new JPanel();
		secondaryPanel.setLayout(new BoxLayout(secondaryPanel, BoxLayout.Y_AXIS));
		
		JPanel buttons = new JPanel();
		JButton newFactoryButton = new JButton("New Factory");
		newFactoryButton.setPreferredSize(new Dimension(200, 50));
		newFactoryButton.setFont(new Font(null, Font.PLAIN, 20));
		buttons.add(newFactoryButton);
		
		JPanel stats = new JPanel();
		JLabel name = new JLabel("Argon Prime");
		name.setFont(new Font(null, Font.PLAIN, 20));
		stats.add(name);
		//stats.setBounds(0, 450, 800, 450);
		
		JPanel map = new JPanel();
		map.setLayout(new GridBagLayout());
		GridBagConstraints sectorContraints = new GridBagConstraints();
		sectorContraints.gridx = 0;
		sectorContraints.gridy = 0;
		sectorContraints.insets = new Insets(5, 5, 5, 5);
		sectorContraints.anchor = GridBagConstraints.CENTER;
		sectorContraints.fill = GridBagConstraints.BOTH;
		
		JButton sectorButton = new JButton();
		//TODO Move this into the project folder and make the reference into a constant
		sectorButton.setIcon(new ImageIcon("C:\\Users\\Endominus\\Pictures\\SectorImage.png"));
		sectorButton.setBorderPainted(false);
		sectorButton.setContentAreaFilled(false);
		map.add(sectorButton, sectorContraints);
		
		secondaryPanel.add(buttons);
		secondaryPanel.add(stats);
		
		mainPanel.add(secondaryPanel);
		mainPanel.add(map);
		
		add(mainPanel);
	}

}
