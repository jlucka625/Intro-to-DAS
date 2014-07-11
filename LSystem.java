import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import java.lang.Object;
import java.awt.geom.*;
import javax.swing.SwingUtilities;

class EDTSkeleton
{
	private static final int WIDTH = 400;
	private static final int HEIGHT = 400;
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

class ImageFrame extends JFrame
{
	private final int WIDTH;
	private final int HEIGHT;
	private BufferedImage image = null;
	private JFileChooser chooser;
	private String[] rules;
	private Graphics2D g2;
	private int background, foreground;
	private double delta; 
	private int scalingFactor;
	private String initiator;
	
	public ImageFrame(int width, int height)
	{
		WIDTH = width;
		HEIGHT = height;
		setTitle("CAP 3027 2012 - HW06 - Jonathan Lucka");
		setSize(width, height);
		
		//add a menu to the frame
		addMenu();
		
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
	}	
	
	private void addMenu()
	{
		//setup the frame's menu bar
		JMenu fileMenu = new JMenu("File");

		//Load L-System
		JMenuItem loadItem = new JMenuItem("Load L-System");
		loadItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				loadLS();
			}
		});
		fileMenu.add(loadItem);
		
		//Configure image
		JMenuItem configItem = new JMenuItem("Configure image");
		configItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				configureImage();
			}
		});
		fileMenu.add(configItem);
		
		//Display L-System
		JMenuItem displayItem = new JMenuItem("Display L-System");
		displayItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				displayLS();
			}
		});
		fileMenu.add(displayItem);
		
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
	private void loadLS()
	{
		//Check for description file
		File file = null;
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			file = chooser.getSelectedFile();
		}
		else
		{
			return;
		}
		
		//Determine amount of lines
		Scanner scan = null;
		try
		{
			scan = new Scanner(file);
		}catch(FileNotFoundException exception){}
		int count = 0;
		while(scan.hasNextLine())
		{
			scan.nextLine();
			count++;
		}
		rules = new String[count - 3];
		
		//Read file
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(file));
		}catch(FileNotFoundException exception){}
		try{
		    //Delta
			delta = Double.parseDouble(reader.readLine());
			
			//Segment Length
			scalingFactor = Integer.parseInt(reader.readLine());
			
			//Initiator
			initiator = reader.readLine();
			
			//Production Rules
			for(int i = 0; i < rules.length; i++)
			{
				rules[i] = reader.readLine();
			}
		}catch(IOException ioe){}
	}
	
	private void configureImage()
	{
		int width = 0, height = 0;
		String bg = "", fg = "";
		try
		{
			height = Integer.parseInt(JOptionPane.showInputDialog("Input height"));
			width = Integer.parseInt(JOptionPane.showInputDialog("Input width"));
			bg = JOptionPane.showInputDialog("Input background color");
			fg = JOptionPane.showInputDialog("Input foreground color");
		}catch(NumberFormatException n){}	
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		background = (int)Long.parseLong(bg.substring( 0, bg.length()), 16);
		foreground = (int)Long.parseLong(fg.substring( 0, fg.length()), 16);
		
		g2 = (Graphics2D)image.createGraphics();
		g2.setColor(new Color(background));
		g2.fillRect(0,0, width, height);
		g2.setColor(new Color(foreground));
	}
	
	private void displayLS()
	{
		if(g2 == null)
		{
			JOptionPane.showMessageDialog(this, "Please configure image");
			return;
		}
		if(rules == null)
		{
			JOptionPane.showMessageDialog(this, "Please load L-System");	
			return;
		}
		
		g2.setColor(new Color(background));
		g2.fillRect(0,0, image.getWidth(), image.getHeight());
		g2.setColor(new Color(foreground));
		
		int n = 0;
		double x = 0.0, y = 0.0, sLength = 0.0, bearing = 0.0;
		try
		{
			n = Integer.parseInt(JOptionPane.showInputDialog("Input number of generations"));
			x = Double.parseDouble(JOptionPane.showInputDialog("Input inital relative X position (-1.0, 1.0)"));
			y = Double.parseDouble(JOptionPane.showInputDialog("Input inital relative Y position (-1.0, 1.0)"));
			bearing = Double.parseDouble(JOptionPane.showInputDialog("Input bearing(degrees)"));
			sLength = Double.parseDouble(JOptionPane.showInputDialog("Input relative segment length (1.0 = 1/2 image size)"));
		}catch(NumberFormatException exp){}	
		
		sLength *= image.getHeight()/2;
		bearing *= (Math.PI/180.0);
		double d = delta * (Math.PI/180.0);
		double r = sLength/(Math.pow(scalingFactor, n));
		x = (1 + x) * image.getWidth()/2;
		y = (1 + y) * image.getHeight()/2;
		double tempX = 0, tempY = 0;
		
		String g = "";
		if(n == 0)
		{
			g = initiator;
		}
		else
		{
			g = generateString(n);
		}
		
		/*
		F move forward, one unit, drawing a line segment
		f move forward, one unit, w/o drawing a line segment
		- turn right theta
		+ turn left theta
		[ push turtle's current state onto the stack
		] pop (and restore) turtle's state from the stack
		R move forward, one unit, drawing a line segment (space filling to the right)
		r move forward, one unit, w/o drawing a line segment (space filling to the right)
		L move forward, one unit, drawing a line segment (space filling to the left)
		l move forward, one unit, w/o drawing a line segment (space filling to the left)
		*/
			
		Stack<Point2D.Double> pointStack = new Stack<Point2D.Double>();
		Stack<Double> angleStack = new Stack<Double>();
		BufferedReader reader = new BufferedReader(new StringReader(g));
		Line2D.Double line = new Line2D.Double(x, y, x, y);
		int temp = 0;
		while(temp != -1)
		{
			try
			{
				temp = reader.read();
			}catch(IOException e){}
			if((char)temp == 'F')
			{
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);
			}
			else if((char)temp == 'f')
			{
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);
			}
			else if((char)temp == '-')
			{
				bearing -= d;
			}
			else if((char)temp == '+')
			{
				bearing += d;			
			}
			else if((char)temp == '[')
			{
				pointStack.push(new Point2D.Double(x, y));
				angleStack.push(bearing);
			}
			else if((char)temp == ']')
			{
				Point2D.Double point = pointStack.peek();
				pointStack.pop();
				x = point.x;
				y = point.y;
				
				bearing = angleStack.peek();
				angleStack.pop();
			}
			else if((char)temp == 'R')
			{
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);
				bearing -= delta;
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);
				bearing += (2 * delta);
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);
				bearing -= delta;
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);
			}
			else if((char)temp == 'r')
			{
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);
				bearing -= delta;
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);
				bearing += (2 * delta);
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);
				bearing -= delta;
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);
				
			}
			else if((char)temp == 'L')
			{
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);
				bearing += delta;
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);
				bearing -= (2 * delta);
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);
				bearing += delta;
				line.setLine(x, y, x += r * Math.cos(bearing), y -= r * Math.sin(bearing));
				g2.draw(line);			
			}
			else if((char)temp == 'l')
			{
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);
				bearing += delta;
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);
				bearing -= (2 * delta);
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);
				bearing += delta;
				x += r * Math.cos(bearing);
				y -= r * Math.sin(bearing);			
			}
		}
		displayBufferedImage(image);
	}
	
	private String generateString(int n)
	{
		BufferedReader reader = null;
		char[] dictionary = new char[rules.length];
		String[] tempRules = new String[rules.length];
		int keyIndex = 0;
		String output = "";
		int temp = 0;
		for(int i = 0; i < rules.length; i++)
		{
			reader = new BufferedReader(new StringReader(rules[i]));
			try
			{
		    	dictionary[i] = (char)reader.read();
		    	tempRules[i] = rules[i].substring(4, rules[i].length());
			}catch(IOException e){}
		}
		reader = new BufferedReader(new StringReader(initiator));
		for(int i = 0; i < n; i++)
		{
            String tempOutput = "";
			while(temp != -1)
			{
				try{
			   		temp = reader.read();
			   	}catch(IOException e){}
				for(int j = 0; j < dictionary.length; j++)
				{
					if((char)temp == dictionary[j])
					{
						keyIndex = j;
						break;
					}
					else keyIndex = -1;
				}
				if(keyIndex != -1)
					tempOutput += tempRules[keyIndex];
				else if(temp != -1)
					tempOutput += (char)temp;
			}
			temp = 0;
			reader = new BufferedReader(new StringReader(tempOutput));
			output = tempOutput;
		}
		return output;
	}
	
	private void saveImage()
	{
		if(g2 == null)
		{
			JOptionPane.showMessageDialog(this, "Please configure image");
			return;
		}
		if(rules == null)
		{
			JOptionPane.showMessageDialog(this, "Please load L-System");	
			return;
		}
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
	
	//Display BufferedImage 
	public void displayBufferedImage(BufferedImage image)
	{
		//Many ways to display BufferedImage... here is one in particular
		this.setContentPane(new JLabel(new ImageIcon(image)));
		/*icon = new ImageIcon(image);
		label = new JLabel(icon);
		this.setContentPane(label);*/
		/**  One problem is that if this method is called more than once, it does not
		reuse the existing JScrollPane, JLabel, or ImageIcon. 
		
		JFrames are a type of container. Anytime a container's subcomponents are modified
		(added or removed from the container, or layout-related information changed) after
		the container has been displayed, one should call the validate() method -- which causes
		the container to lay out its subcomponents again. 	**/
		this.validate(); //picks up changes, lays out all of the components;
	}
}