import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.lang.Object;
import java.awt.geom.*;
import java.util.Random;
import java.util.ArrayList;

class EDTSkeleton
{
	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
				public void run()
				{
					createAndShowGUI();
				}
		});
	}
	private static void createAndShowGUI()
	{
		JFrame frame = new ImageFrame(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

class Cell
{
	private boolean justBorn, justDied, isAlive, isDead;
	private int x, y;
	
	public Cell(int x, int y, boolean justBorn, boolean justDied, boolean isAlive, boolean isDead)
	{
		this.x = x;
		this.y = y;
		this.justBorn = justBorn;
		this.justDied = justDied;
		this.isAlive = isAlive;
		this.isDead = isDead;
	}
	
	public void setX(int x){this.x = x;}
	public void setY(int y){this.y = y;}
	public int getX(){return x;}
	public int getY(){return y;}
	
	public void toggleBirth(boolean b){justBorn = b;}
	public void toggleDeath(boolean b){justDied = b;}	
	public void toggleDead(boolean b){isDead = b;}
	public void toggleAlive(boolean b){isAlive = b;}
	public boolean getBirth(){return justBorn;}
	public boolean getDeath(){return justDied;}
	public boolean getIsDead(){return isDead;}
	public boolean getIsAlive(){return isAlive;}
}

class ImagePanel extends JPanel 
{
	private BufferedImage image;
	private Graphics2D g2d;
	public ImagePanel(BufferedImage image)
	{
		this.image = image;
		g2d = image.createGraphics();
	}
	public void setImage(BufferedImage src)
	{
		g2d.setPaintMode();
		g2d.drawImage(src, 0, 0, null);
		repaint();	
	}
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}
}

class ImageFrame extends JFrame
{
	private final int WIDTH;
	private final int HEIGHT;
	private BufferedImage image = null;
	private JFileChooser chooser;
	private Graphics2D g2;
	private ImagePanel panel;
	private final int MILLISECONDS_BETWEEN_GENERATIONS = 500;
	private Timer timer;
	private ArrayList<Cell> cells;
	private ArrayList<Integer> indexes;
	public boolean isPaused, isPopulated;
	
	public ImageFrame(int width, int height)
	{
		WIDTH = width;
		HEIGHT = height;
		setTitle("CAP 3027 2012 - HW09 - Jonathan Lucka");
		setSize(width, height);
		cells = new ArrayList<Cell>();
		indexes = new ArrayList<Integer>();
		
		isPaused = true;
		isPopulated = false;
		
		image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		g2 = (Graphics2D)image.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, 600, 600);	

		panel = new ImagePanel(image);
		final JButton button = new JButton("Start");
		
		timer = new Timer(MILLISECONDS_BETWEEN_GENERATIONS, new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				generate();
			}
		});		
		
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{			
				if(isPopulated)
				{
					if(isPaused)
					{
						isPaused = false;
						timer.start();
						button.setText("Pause");
					}
					else
					{
						isPaused = true;
						timer.stop();
						button.setText("Start");
					}
				}
			}
		});		
		
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent event)
			{
				if(isPaused)
				{
					toggleCellState(event.getPoint());
				}
			}

		});

		//add a menu to the frame
		addMenu();
		
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		
		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.getContentPane().add(button, BorderLayout.SOUTH);
			
		this.setResizable(false);
	}

	

	
	private void addMenu()
	{
		//setup the frame's menu bar
		JMenu fileMenu = new JMenu("File");		

		//Randomly populated world
		JMenuItem rItem = new JMenuItem("Randomly populated world");
		rItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				if(isPaused)
					populate(false);
			}
		});
		fileMenu.add(rItem);
		
		//Empty world
		JMenuItem eItem = new JMenuItem("Empty world");
		eItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				if(isPaused)
					populate(true);
			}
		});
		fileMenu.add(eItem);
		
		//Save image
		JMenuItem saveItem = new JMenuItem("Save image");
		saveItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				saveImage();
			}
		});
		fileMenu.add(saveItem);
		
		//Exit
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				System.exit(0);
			}
		});
		fileMenu.add(exitItem);
		
		//attach a menu to a menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
	}
	
	private void populate(boolean isEmpty)
	{
		cells.clear();
		isPopulated = true;
		Random rand = new Random();
		double p;
		double probability = 0.0;
		if(!isEmpty)
		{
			try
			{
				probability = Double.parseDouble(JOptionPane.showInputDialog("Input probability [0.0, 1.0]"));			
			}
			catch(NumberFormatException exp){}
		}
		
		g2 = (Graphics2D)image.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, 600, 600);
		
		for(int i = 0; i < 600; i += 2)
		{
			for(int j = 0; j < 600; j += 2)
			{
				p = rand.nextDouble();
				if(p <= probability)
				{
					g2.setColor(Color.BLUE);
					g2.fillRect(i, j, 2, 2);
					cells.add(new Cell(i, j, false, false, true, false));
				}
				else
				{
					g2.setColor(Color.BLACK);
					g2.fillRect(i, j, 2, 2);
					cells.add(new Cell(i, j, false, false, false, true));				
				}
			}
		}
		panel.setImage(image);
	}
	
	private void generate()
	{
		int count = 0;
		for(Integer idx : indexes)
		{
			if(cells.get(idx).getDeath())
			{
				cells.get(idx).toggleDeath(false);
				cells.get(idx).toggleDead(true);
				g2.setColor(Color.BLACK);
				g2.fillRect(cells.get(idx).getX(), cells.get(idx).getY(), 2, 2);
			}
			if(cells.get(idx).getBirth())
			{
				cells.get(idx).toggleBirth(false);
				cells.get(idx).toggleAlive(true);
				g2.setColor(Color.BLUE);
				g2.fillRect(cells.get(idx).getX(), cells.get(idx).getY(), 2, 2);
			}
		}
		for(int i = 0; i < 600; i += 2)
		{
			for(int j = 0; j < 600; j += 2)
			{
				count = 0;
			
				//toroidal cases
				if(i == 0)
				{
					count += neighborExists(598, j);
					count += neighborExists(598, j + 2);
					count += neighborExists(598, j - 2);
				}
				if(i == 598)
				{
					count += neighborExists(0, j);
					count += neighborExists(0, j + 2);
					count += neighborExists(0, j - 2);
				}
				if(j == 0)
				{
					count += neighborExists(i, 598);
					count += neighborExists(i + 2, 598);
					count += neighborExists(i - 2, 598);
				}
				if(j == 598)
				{
					count += neighborExists(i, 0);
					count += neighborExists(i + 2, 0);
					count += neighborExists(i - 2, 0);			
				}
				count += neighborExists(i + 2, j);
				count += neighborExists(i - 2, j);
				count += neighborExists(i + 2, j + 2);
				count += neighborExists(i - 2, j - 2);
				count += neighborExists(i - 2, j + 2);
				count += neighborExists(i + 2, j - 2);
				count += neighborExists(i, j + 2);
				count += neighborExists(i, j - 2);
				int index = (j/2) + 150 * i;
			
				if(count < 2 || count > 3)
				{
					if(cells.get(index).getIsAlive())
					{
						cells.get(index).toggleBirth(false);
						cells.get(index).toggleAlive(false);
						cells.get(index).toggleDeath(true);
						cells.get(index).toggleDead(false);
						g2.setColor(Color.RED);
						g2.fillRect(i, j, 2, 2);
						indexes.add(index);
					}
				}
				else if(count == 3)
				{
					if(cells.get(index).getIsDead())
					{
						cells.get(index).toggleDeath(false);
						cells.get(index).toggleDead(false);
						cells.get(index).toggleBirth(true);
						cells.get(index).toggleAlive(false);
						g2.setColor(Color.GREEN);
						g2.fillRect(i, j, 2, 2);
						indexes.add(index);
					}	
				}
			}
		}
		panel.setImage(image);
	}
	
	private int neighborExists(int i, int j)
	{
		int index = (j/2) + 150 * i;
		if(index > 0 && index < 90000)
		{
			if((cells.get(index).getIsAlive() || cells.get(index).getDeath()) && !cells.get(index).getBirth())
			{
				return 1;
			}
		}
		return 0;
	}
	
	private void toggleCellState(Point p)
	{
		int x = (int)p.getX();
		int y = (int)p.getY() - 47;
		if(x % 2 == 1){ x--; }
		if(y % 2 == 1){ y--; }
		
		int index = (y/2) + 150 * x;
		if(cells.get(index).getBirth() == true || cells.get(index).getIsAlive() == true)
		{
			g2.setColor(Color.BLACK);
			g2.fillRect(x, y, 2, 2);	
			cells.get(index).toggleBirth(false);
			cells.get(index).toggleAlive(false);
			cells.get(index).toggleDead(true);
		}
		else if(cells.get(index).getDeath() == true || cells.get(index).getIsDead() == true)
		{
			g2.setColor(Color.BLUE);
			g2.fillRect(x, y, 2, 2);	
			cells.get(index).toggleAlive(true);
			cells.get(index).toggleDead(false);
			cells.get(index).toggleDeath(false);		
		}
		panel.setImage(image);
	}
	
	private void saveImage()
	{
		File outputFile = new File("output.png");
		try
		{
   			javax.imageio.ImageIO.write( image, "png", outputFile );
		}
		catch ( IOException e )
		{
   			JOptionPane.showMessageDialog(ImageFrame.this, "Error saving file", "oops!", JOptionPane.ERROR_MESSAGE );
		}	
	}
}

