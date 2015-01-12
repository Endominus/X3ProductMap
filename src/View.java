import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class View extends JFrame implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static GridBagConstraints GRID_BAG_CONSTRAINTS;
	private JPanel masterPanel;
	private JPanel slavePanel;
	private JPanel buttonPanel;
	private JScrollPane statsPane;
	private JPanel mapPanel;
	private JTextArea statsText;
	private SectorButton selectedButton = null;
	
	public View()
	{
		setTitle("X3 Product Map");
		setSize(1600, 900);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		View.GRID_BAG_CONSTRAINTS = new GridBagConstraints();
		View.GRID_BAG_CONSTRAINTS.insets = new Insets(5, 0, 5, 0);
		View.GRID_BAG_CONSTRAINTS.anchor = GridBagConstraints.CENTER;
		View.GRID_BAG_CONSTRAINTS.fill = GridBagConstraints.BOTH;
		
		InitializePanels();
		
		setVisible(true);
	}

	private void InitializePanels()
	{
		this.masterPanel = new JPanel();
		this.masterPanel.setLayout(new BoxLayout(this.masterPanel, BoxLayout.X_AXIS));
		
		this.slavePanel = new JPanel();
		this.slavePanel.setLayout(new BoxLayout(this.slavePanel, BoxLayout.Y_AXIS));
		
		this.buttonPanel = new JPanel();
		this.buttonPanel.setPreferredSize(new Dimension(400, 450));
		JButton newFactoryButton = new JButton("New Factory");
		newFactoryButton.setPreferredSize(new Dimension(200, 50));
		newFactoryButton.setFont(new Font(null, Font.PLAIN, 20));
		this.buttonPanel.add(newFactoryButton);
		
		this.statsText = new JTextArea();
		this.statsText.setEditable(false);
		this.statsPane = new JScrollPane(this.statsText);
		this.statsPane.setPreferredSize(new Dimension(400, 450));
		this.statsText.setFont(new Font(null, Font.PLAIN, 20));
		//this.statsPane.add(this.statsText);
		//stats.setBounds(0, 450, 800, 450);
		
		this.mapPanel = new JPanel();
		this.mapPanel.setLayout(new GridBagLayout());
		this.mapPanel.setBackground(new Color(0.5f, 0.8f, 0.6f));
		
		//SectorButton sButton = new SectorButton("Argon Prime", 0, 0, 0);
		//this.mapPanel.add(sButton, sButton.GetConstraints());

		this.slavePanel.add(this.buttonPanel);
		this.slavePanel.add(this.statsPane, BorderLayout.CENTER);
		
		this.masterPanel.add(this.slavePanel);
		this.masterPanel.add(this.mapPanel);
		
		add(this.masterPanel);
	}
	
	public void AddSector(String name, int id, int x, int y)
	{
		SectorButton sButton = new SectorButton(name, id, x, y);
		sButton.addActionListener(this);
		this.mapPanel.add(sButton, sButton.GetConstraints());
	}
	
	private class SectorButton extends JButton
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		//String name;
		int ycoord, xcoord;
		int id;
		
		public SectorButton(String name, int id, int x, int y)
		{
			super();
			this.setIcon(new ImageIcon("res/SectorImage.png"));
			this.setBorderPainted(false);
			this.setContentAreaFilled(false);
			//this.name = name;
			this.id = id;
			this.xcoord = x;
			this.ycoord = y;
		}
		
		public GridBagConstraints GetConstraints()
		{
			GridBagConstraints gbc = (GridBagConstraints) View.GRID_BAG_CONSTRAINTS.clone();
			gbc.gridx = this.xcoord;
			gbc.gridy = this.ycoord;
			
			return gbc;
		}

		public int GetSectorID()
		{
			return this.id;
		}

		public void ToggleSelected()
		{
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource().getClass() == SectorButton.class)
		{
			if (this.selectedButton != null)
				this.selectedButton.ToggleSelected();
			this.selectedButton = (SectorButton) e.getSource();
			this.selectedButton.ToggleSelected();
			String stats = Controller.GetStats(this.selectedButton.GetSectorID());
			this.statsText.setText(stats);
		}	
	}
}
