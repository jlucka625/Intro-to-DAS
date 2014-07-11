import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import java.lang.Object;
import java.awt.geom.Line2D;
import javax.swing.SwingUtilities;

class EDTSkeleton
{
	private static final int WIDTH = 800;
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
                frame.setResizable(false);
		frame.setVisible(true);
	}
}

class Stem
{
	private Line2D.Double[] lines;
	private double[] xprime;
	private double[] yprime;
	private double theta;
	private double r;
	public Stem(Line2D.Double[] lines, double[] xprime, double[] yprime, double theta, double r)
	{
		this.lines = lines;
		this.xprime = xprime;
		this.yprime = yprime;
		this.theta = theta;
		this.r = r;
	}
	
	public void setXPrime(int index, double x){xprime[index] = x;}
	public double getXPrime(int index){return xprime[index];}
	
	public void setYPrime(int index, double y){yprime[index] = y;}
	public double getYPrime(int index){return yprime[index];}
	
	public void setTheta(double t){theta = t;}
	public double getTheta(){return theta;}

	public void setR(double growth){r = growth;}
	public double getR(){return r;}
	
	public Line2D.Double getLines(int index){return lines[index];}
	public void setLines(int index, Line2D.Double line){lines[index] = line;}
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
	private JLabel label;
	private ImageIcon icon;
        private ImagePanel panel;
        private Color start, end;
        private JSlider stemCountSlider, stepCountSlider, directionSlider, rotationSlider, growthSlider;
	
	public ImageFrame(int width, int height)
	{
		WIDTH = width;
		HEIGHT = height;
		setTitle("Random Walk Growth Plant");
		setSize(width, height);
                panel = new ImagePanel(new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB));
		//add a menu to the frame
              
                start = new Color(255,0,0);
                end = new Color(0,0,255);
                
		addMenu();
                this.getContentPane().add(panel, BorderLayout.CENTER);
                this.getContentPane().add(setupOptionsPanel(), BorderLayout.WEST);
	}	
        private JPanel setupOptionsPanel()
        {
            JPanel optionsPanel = new JPanel();
            optionsPanel.setSize(600, 600);
            optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
            
            //top section
            JPanel stemPanel = new JPanel();
            stemPanel.setLayout(new BoxLayout(stemPanel, BoxLayout.X_AXIS));
            
            stemCountSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
            stemCountSlider.setBorder(BorderFactory.createTitledBorder("Number of Stems"));
            stemCountSlider.setMajorTickSpacing(20);
            stemCountSlider.setMinorTickSpacing(5);
            stemCountSlider.setPaintTicks(true);
            stemCountSlider.setPaintLabels(true);
            
            stepCountSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 25);
            stepCountSlider.setBorder(BorderFactory.createTitledBorder("Number of Growth Cycles"));
            stepCountSlider.setMajorTickSpacing(10);
            stepCountSlider.setMinorTickSpacing(5);
            stepCountSlider.setPaintTicks(true);
            stepCountSlider.setPaintLabels(true);
            
            optionsPanel.add(stemCountSlider);
            optionsPanel.add(stepCountSlider);
            
            //optionsPanel.add(stemPanel);
            
            //middle section
            JPanel probPanel = new JPanel();
            probPanel.setLayout(new BoxLayout(probPanel, BoxLayout.X_AXIS));
            
            directionSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
            directionSlider.setBorder(BorderFactory.createTitledBorder("Direction Change Probability"));
            directionSlider.setMajorTickSpacing(20);
            directionSlider.setMinorTickSpacing(5);
            directionSlider.setPaintTicks(true);
            directionSlider.setPaintLabels(true);
            
            rotationSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
            rotationSlider.setBorder(BorderFactory.createTitledBorder("Angle of Growth"));
            rotationSlider.setMajorTickSpacing(20);
            rotationSlider.setMinorTickSpacing(5);
            rotationSlider.setPaintTicks(true);
            rotationSlider.setPaintLabels(true);   
            
            optionsPanel.add(directionSlider);
            optionsPanel.add(rotationSlider);
            
            //optionsPanel.add(probPanel);
            
            //lower section
            final JPanel finalPanel = new JPanel();
            finalPanel.setLayout(new BoxLayout(finalPanel, BoxLayout.X_AXIS));
            
            JButton startColorButton = new JButton("Start Color");
            startColorButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    start = JColorChooser.showDialog(finalPanel, "Choose Start Color", start);
                }
            });
            
            JButton endColorButton = new JButton("End Color");
            endColorButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    end = JColorChooser.showDialog(finalPanel, "Choose End Color", end);
                }
            });
            
            growthSlider = new JSlider(JSlider.HORIZONTAL, 0, 5, 2);
            growthSlider.setBorder(BorderFactory.createTitledBorder("Growth Length"));
            growthSlider.setMajorTickSpacing(1);
            growthSlider.setPaintTicks(true);
            growthSlider.setPaintLabels(true);
            
            optionsPanel.add(growthSlider);
            finalPanel.add(startColorButton);
            finalPanel.add(endColorButton);
            
            optionsPanel.add(finalPanel);
            
            return optionsPanel;
        }
	private void addMenu()
	{
		//setup the frame's menu bar
		JMenu fileMenu = new JMenu("File");

		JMenuItem plantItem = new JMenuItem("Directed random walk plant");
		plantItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				doLongJob();
			}
		});
		fileMenu.add(plantItem);
		
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
	
	private void doLongJob()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				randomWalkPlant();
			}
		}).start();
	}
	
	private void randomWalkPlant()
	{
		/*int size = 0, numStems = 0, numSteps = 0, growth = 0, ARGB1 = 0, ARGB2 = 0;
		double transmissionProbability = 0.0, maxRotation = 0.0;*/
		/*String color1 = "", color2 = "";
		try
		{
			size = Integer.parseInt(JOptionPane.showInputDialog("Input size"));
			numStems = Integer.parseInt(JOptionPane.showInputDialog("Input number of stems"));
			numSteps = Integer.parseInt(JOptionPane.showInputDialog("Input number of steps"));
			transmissionProbability = Double.parseDouble(JOptionPane.showInputDialog("Input transmission probability [0.0,1.0]"));
			maxRotation = Double.parseDouble(JOptionPane.showInputDialog("Input maximum rotation increment [0.0,1.0]"));
			growth = Integer.parseInt(JOptionPane.showInputDialog("Input growth segment increment"));
			color1 = JOptionPane.showInputDialog("Input start color");
			color2 = JOptionPane.showInputDialog("Input end color");
		}catch(NumberFormatException n){}*/
		
		/*ARGB1 = (int)Long.parseLong(color1.substring( 2, color1.length()), 16);
		ARGB2 = (int)Long.parseLong(color2.substring( 2, color2.length()), 16);*/
                int size = 600;
                int numSteps = stepCountSlider.getValue();
                int numStems = stemCountSlider.getValue();
                int growth = growthSlider.getValue();
                double transmissionProbability = (double)directionSlider.getValue()/100;
                double maxRotation = (double)rotationSlider.getValue()/100;
                int ARGB1 = start.getRGB();
                int ARGB2 = end.getRGB();
                
		Color[] colors = interpolateColors(numSteps + 1, ARGB1, ARGB2);
		BasicStroke[] strokes = interpolateStrokes(numSteps + 1);
		
		//Create buffered image
		final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		
		SwingUtilities.invokeLater(new Runnable ()
		{
			public void run()
			{
				//displayBufferedImage(image);	
                                panel.setImage(image);
			}
		});
		
		//Create graphics2D object and set image background to white
		Graphics2D g2d = (Graphics2D)image.createGraphics();
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0, size, size);
		
		//initializing variables
		double theta = (Math.PI)/2;
		double r = 1.0;
		double x = size/2, y = size/2;
		double beta = (1.0 - transmissionProbability);
		int direction = 1; //starting left
		Random rand = new Random();
		
		//plotting initial line segment
		g2d.setColor(colors[numSteps]);
		double xprime = (x + r * Math.cos(theta));
		double yprime = (y - r * Math.sin(theta));
		Line2D.Double line = new Line2D.Double(x, y, xprime, yprime);
		g2d.setStroke(strokes[numSteps]);
		g2d.draw(line);
		
		double bias;
		double tempX = xprime;
		double tempY = yprime;
		
		Stem[] stems = new Stem[numStems];
		for(int i = 0; i < numSteps; i++)
		{
			for (int j = 0; j < numStems; j++)
			{
				//determine initial direction
				int k = rand.nextInt(2);
				if(k == 0){
					direction = 1;
				}
				else direction = -1;
				
				//determine bias
				if(direction == -1){bias = transmissionProbability;}
				else{bias = beta;}
			
				//flip biased coin
				if(rand.nextDouble() > bias){direction = 1;}
				else{direction = -1;}
			
				//compute offset
				if(i == 0)
				{
					r = r + growth;
					theta = theta + (maxRotation * rand.nextDouble() * direction);	
				}
				else
				{
					stems[j].setR(stems[j].getR() + growth);
					stems[j].setTheta(stems[j].getTheta() + (maxRotation * rand.nextDouble() * direction));
				}
				
				if(i == 0)
				{
					g2d.setColor(colors[numSteps - i - 1]);
					stems[j] = new Stem(new Line2D.Double[numSteps + 1], new double[numSteps + 1], new double[numSteps + 1], theta, r);
					stems[j].setLines(i, new Line2D.Double(xprime, yprime, (xprime = xprime + r * Math.cos(theta)), (yprime = yprime - r * Math.sin(theta))));
					stems[j].setXPrime(i, xprime);
					stems[j].setYPrime(i, yprime);
					xprime = tempX;
					yprime = tempY;
					g2d.setStroke(strokes[numSteps - i - 1]);		
					g2d.draw(stems[j].getLines(i));
					r = 1.0;
					theta = (Math.PI)/2;
				}
				else
				{
					//draw growth segment
					double sx = stems[j].getXPrime(i - 1);
					double sy = stems[j].getYPrime(i - 1);
					double sr = stems[j].getR();
					double st = stems[j].getTheta();
			    	g2d.setColor(colors[numSteps - i - 1]);
					stems[j].setLines(i, new Line2D.Double(sx, sy, sx + sr * Math.cos(st), sy - sr * Math.sin(st)));
					stems[j].setXPrime(i, sx + sr * Math.cos(st));	
					stems[j].setYPrime(i, sy - sr * Math.sin(st));	
					g2d.setStroke(strokes[numSteps - i - 1]);
					g2d.draw(stems[j].getLines(i));
				}
			}
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0,0, size, size);
			for(int j = i; j > 0; j--)
			{
				for(int p = 0; p < numStems; p++)
				{
					g2d.setColor(colors[numSteps - i + j]);
					g2d.setStroke(strokes[numSteps - i + j]);
					g2d.draw(stems[p].getLines(j));
				}
			}
			SwingUtilities.invokeLater(new Runnable ()
			{
				public void run()
				{
					/*icon.setImage( image );
					label.repaint();
					validate();*/
                                        panel.setImage(image);
				}
			});
			try{Thread.sleep(50);}
			catch(InterruptedException e){}
		}
		
		RenderingHints hint =  new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHints( hint );
		
		//displayBufferedImage(image);
	}
	
	//Display BufferedImage 
	public void displayBufferedImage(BufferedImage image)
	{
		//Many ways to display BufferedImage... here is one in particular
		//this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));
		icon = new ImageIcon(image);
		label = new JLabel(icon);
		this.setContentPane(label);
		/**  One problem is that if this method is called more than once, it does not
		reuse the existing JScrollPane, JLabel, or ImageIcon. 
		
		JFrames are a type of container. Anytime a container's subcomponents are modified
		(added or removed from the container, or layout-related information changed) after
		the container has been displayed, one should call the validate() method -- which causes
		the container to lay out its subcomponents again. 	**/
		this.validate(); //picks up changes, lays out all of the components;
	}
	
	//interpolates from two colors chosen by user and stores each step of interpolation into an array
	private Color[] interpolateColors(int numSteps, int ARGB1, int ARGB2)
	{
		Color[] array = new Color[numSteps + 1];
		array[0] = new Color(ARGB1);
		
		double stepSize = 1/(double)numSteps;
		
		double startRed = (double)((ARGB1 >>> 16) & 0xFF);
		double startGreen = (double)((ARGB1 >>> 8) & 0xFF);
		double startBlue = (double)(ARGB1 & 0xFF);
		
		double endRed = (double)((ARGB2 >>> 16) & 0xFF);
		double endGreen = (double)((ARGB2 >>> 8) & 0xFF);
		double endBlue = (double)(ARGB2 & 0xFF);
		
		double s1r = startRed, s1g = startGreen, s1b = startBlue;
		int dr, dg, db;
		
		for(int i = 1; i < numSteps + 1; i++)
		{
			s1r += (endRed - startRed)*stepSize;
			s1g += (endGreen - startGreen)*stepSize;
			s1b += (endBlue - startBlue)*stepSize;
			
			dr = (int)s1r;
			dg = (int)s1g;
			db = (int)s1b;
			
			array[i] = new Color(dr, dg, db);
		}
		return array;
	}
	//interpolates from widths 6.0f to 0.5f and stores each step of interpolation into an array	
	private BasicStroke[] interpolateStrokes(int numSteps)
	{
		float start = 6.0f, end = 0.5f;
		BasicStroke[] array = new BasicStroke[numSteps + 1];
		array[0] = new BasicStroke(start);
		
		float stepSize = 1/(float)numSteps;
		float delta = start;
		int k;
		for(int i = 1; i < numSteps + 1; i++)
		{
			delta += (end - start)*stepSize;
			array[i] = new BasicStroke(delta);
		}
		return array;
	}
}
