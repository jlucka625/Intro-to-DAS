import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.lang.Object;
import java.awt.geom.*;
import java.awt.Robot;

class EDTSkeleton
{
	private static final int WIDTH = 600;
	private static final int HEIGHT = 450;
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
	private Graphics2D g2;
	private boolean isJulia = false, isZooming = false, isCentering = false, isCentered = false, zoomingIn = false;
	private double realMin, imgMin, realMax, imgMax;
	private double zrealMin, zimgMin, zrealMax, zimgMax;
	private double mu_real, mu_i;
	private Timer zoomTimer;
	private final int MILLISECONDS_BETWEEN_FRAMES = 17;
	private Robot robot;
	private double dr, di, newCenterX, newCenterY;
	private Point2D.Double point;
	
	public ImageFrame(int width, int height)
	{
                try
                {
                    robot = new Robot();
                }catch(AWTException e){}
                
		WIDTH = width;
		HEIGHT = height;
		setTitle("Mandelbrot and Julia Set Zoomer");
		setSize(width, height);
		
		try
		{
			robot = new Robot();
		}catch(AWTException e){}
		
		//add a menu to the frame
		addMenu();
		
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		
		realMin = -2.0;
		imgMin = 1.5;
		realMax = 2.0;
		imgMax = -1.5;
		
		zrealMin = -2.0;
		zimgMin = 1.5;
		zrealMax = 2.0;
		zimgMax = -1.5;
		
		mu_real = 0.0;
		mu_i = 0.0;
		
		newCenterX = 300.0;
		newCenterY = 225.0;
		
		dr = Math.abs(realMax - realMin);
		di = Math.abs(imgMax - imgMin);
		
		point = new Point2D.Double(0.0, 0.0);
		
		mandelbrot(-2.0, 1.5, 2.0, -1.5);
		
		zoomTimer = new Timer(MILLISECONDS_BETWEEN_FRAMES, new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if(zoomingIn)
				{
					zoomTimer.stop();
					zoomIn();
					zoomTimer.restart();
				}
				else
				{
					zoomTimer.stop();
					zoomOut();
					zoomTimer.restart();
				}
			}
		});		
		
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent event)
			{
				if(event.getButton() == event.BUTTON1)
				{
                                        zoomingIn = true;
				}
				else if(event.getButton() == event.BUTTON3)
				{
                                        zoomingIn = false;
				}
                                isZooming = true;
                                zoomTimer.start();
                                centerImage((double)event.getX(), (double)event.getY() - 25);                            
			}
			public void mouseReleased(MouseEvent event)
			{
				zoomTimer.stop();
			}
		});

		this.setResizable(false);
	}
        private void centerImage(double x, double y)
        {
            double cx = 300.0, cy = 225.0;
            double xdist = Math.abs(300.0 - x)/300.0;
            double ydist = Math.abs(225.0 - y)/225.0;
            
            double realDist = Math.abs(zrealMin - zrealMax)/2.0;
            double imgDist = Math.abs(zimgMin - zimgMax)/2.0;
            
            if(x > cx)
            {
                zrealMin += realDist * xdist;
                zrealMax += realDist * xdist;
            }
            else if(x < cx)
            {
                zrealMin -= realDist * xdist;
                zrealMax -= realDist * xdist;                
            }
            
            if(y > cy)
            {
                zimgMin -= imgDist * ydist;
                zimgMax -= imgDist * ydist;
            }
            else if(y < cy)
            {
                zimgMin += imgDist * ydist;
                zimgMax += imgDist * ydist;                
            }

            if(isJulia)
                julia(zrealMin, zimgMin, zrealMax, zimgMax);
            else
                mandelbrot(zrealMin, zimgMin, zrealMax, zimgMax); 
        }
        private void zoomIn()
        {   
            double diff_real = Math.abs(zrealMin - zrealMax);
            double diff_img = Math.abs(zimgMin - zimgMax);
            
            double tempReal = zrealMin;
            double tempImg = zimgMin;
             			
            zrealMin += diff_real * 0.025;
            zimgMin -= diff_img * 0.025;
            zrealMax = tempReal + (diff_real * 0.975);	
            zimgMax = tempImg - (diff_img * 0.975);
            
            if(isJulia)
                julia(zrealMin, zimgMin, zrealMax, zimgMax);
            else
                mandelbrot(zrealMin, zimgMin, zrealMax, zimgMax); 
        }
        private void zoomOut()
        {
            double diff_real = Math.abs(zrealMin - zrealMax);
            double diff_img = Math.abs(zimgMin - zimgMax); 
            
            zrealMin -= diff_real * 0.025;
            zimgMin += diff_img * 0.025;
            zrealMax += diff_real * 0.025;	
            zimgMax -= diff_img * 0.025;	
            
            if(isJulia)
                julia(zrealMin, zimgMin, zrealMax, zimgMax);
            else
                mandelbrot(zrealMin, zimgMin, zrealMax, zimgMax);            
            
        }
	private void addMenu()
	{
		//setup the frame's menu bar
		JMenu fileMenu = new JMenu("File");

		//Mandelbrot
		JMenuItem mItem = new JMenuItem("Mandelbrot");
		mItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				realMin = -2.0;
				imgMin = 1.5;
				realMax = 2.0;
				imgMax = -1.5;
				
				zrealMin = -2.0;
				zimgMin = 1.5;
				zrealMax = 2.0;
				zimgMax = -1.5;				
				
				dr = Math.abs(realMax - realMin);
				di = Math.abs(imgMax - imgMin);				
				
				mandelbrot(-2.0, 1.5, 2.0, -1.5);
			}
		});
		fileMenu.add(mItem);
		
		//Julia
		JMenuItem jItem = new JMenuItem("Julia");
		jItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				isZooming = false;
				realMin = -2.0;
				imgMin = 1.5;
				realMax = 2.0;
				imgMax = -1.5;
				
				zrealMin = -2.0;
				zimgMin = 1.5;
				zrealMax = 2.0;
				zimgMax = -1.5;		
				
				dr = Math.abs(realMax - realMin);
				di = Math.abs(imgMax - imgMin);		
				
				julia(-2.0, 1.5, 2.0, -1.5);
			}
		});
		fileMenu.add(jItem);
		
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
	
	private void mandelbrot(double initA, double initB, double endA, double endB)
	{
		isJulia = false;
		double horizDistance = Math.abs(endA - initA)/601.0;
		double vertDistance = Math.abs(endB - initB)/451.0;
		
		double w = WIDTH/(Math.abs(initA - endA));
		double h = HEIGHT/(Math.abs(initB - endB));
		
		Color[] colors = interpolateColors();
		
		image = new BufferedImage(600, 450, BufferedImage.TYPE_INT_RGB);
		g2 = (Graphics2D)image.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, 600, 450);
		
		int tMax = 100, t = 0;
		double z_real = 0.0, z_i = 0.0;
		for(int i = 0; i <= 450; i++)
		{
			if(i != 0){initB -= vertDistance;}
			for(int j = 0; j <= 600; j++)
			{
				if(j != 0){initA += horizDistance;}
				z_real = 0.0;
				z_i = 0.0;
				t = 0;
				while(t < tMax)
				{
					double tempZ = z_real;
					z_real = (z_real * z_real) - (z_i * z_i) + initA;
					z_i = (2.0 * tempZ * z_i) + initB;
					if(((z_real * z_real) + (z_i * z_i)) >= 4)
						break;
					else ++t;
				}
				if(t == tMax)
					g2.setColor(Color.BLACK);				
				else if(t % 2 == 0)
					g2.setColor(Color.BLACK);
				else
					g2.setColor(colors[t]);
				
				g2.draw(new Line2D.Double((initA - zrealMin) * w, (initB - zimgMin) * (-h), (initA - zrealMin) * w, (initB - zimgMin) * (-h)));
			}	
			initA = zrealMin;
		}
		g2.setColor(Color.WHITE);
                g2.drawRect(295, 220, 10, 10);
		displayBufferedImage(image);
	}
	
	private void julia(double initA, double initB, double endA, double endB)
	{
		isJulia = true;
		String mu = "";
		if(!isZooming)
		{
			mu = JOptionPane.showInputDialog("Input value of mu (e.g. 0.74 + 0.11i)");
			String[] strs = mu.split("- |\\+");
			mu_real = Double.parseDouble(strs[0]);
			mu_i = Double.parseDouble(strs[1].substring(0, strs[1].length() - 1));
		}
		
		double horizDistance = Math.abs(endA - initA)/601.0;
		double vertDistance = Math.abs(endB - initB)/451.0;
		
		double w = WIDTH/(Math.abs(initA - endA));
		double h = HEIGHT/(Math.abs(initB - endB));
		
		Color[] colors = interpolateColors();
		
		image = new BufferedImage(600, 450, BufferedImage.TYPE_INT_RGB);
		g2 = (Graphics2D)image.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, 600, 450);
		
		int tMax = 100, t = 0;
		double z_real = initA, z_i = initB;
		for(int i = 0; i <= 450; i++)
		{
			if(i != 0){z_i -= vertDistance;}
			for(int j = 0; j <= 600; j++)
			{
				if(j != 0){z_real += horizDistance;}
				t = 0;
				double tempZr = z_real;
				double tempZi = z_i;
				while(t < tMax)
				{
					double tempZ = tempZr;
					tempZr = (tempZr * tempZr) - (tempZi * tempZi) + mu_real;
					tempZi = (2.0 * tempZ * tempZi) + mu_i;
					if(Math.sqrt((tempZr * tempZr) + (tempZi * tempZi)) >= 2)
						break;
					else ++t;
				}
				if(t == tMax)
					g2.setColor(Color.BLACK);				
				else if(t % 2 == 0)
					g2.setColor(Color.BLACK);
				else
					g2.setColor(colors[t]);
								
				g2.draw(new Line2D.Double((z_real - zrealMin) * w, (z_i - zimgMin) * (-h), (z_real - zrealMin) * w, (z_i - zimgMin) * (-h)));
			}
			z_real = initA;
		}
                
		g2.setColor(Color.WHITE);
                g2.drawRect(295, 220, 10, 10);
		displayBufferedImage(image);
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
	
	private Color[] interpolateColors()
	{
		int color_init = 0xffff0000;
		int color_n = 0xff00ff00;
		int color_end = 0xff006400;
		
		Color[] array = new Color[100];
		array[0] = new Color(color_init);
		array[50] = new Color(color_n);
		array[99] = new Color(color_end);
		
		double stepSize = 1/50.0;
		
		double startRed = (double)((color_init >>> 16) & 0xFF);
		double startGreen = (double)((color_init >>> 8) & 0xFF);
		double startBlue = (double)(color_init & 0xFF);
		
		double midRed = (double)((color_n >>> 16) & 0xFF);
		double midGreen = (double)((color_n >>> 8) & 0xFF);
		double midBlue = (double)(color_n & 0xFF);
		
		double endRed = (double)((color_end >>> 16) & 0xFF);
		double endGreen = (double)((color_end >>> 8) & 0xFF);
		double endBlue = (double)(color_end & 0xFF);
		
		double s1r = startRed, s1g = startGreen, s1b = startBlue;
		double dr = (midRed - startRed)*stepSize;
		double dg = (midGreen - startGreen)*stepSize;
		double db = (midBlue - startBlue)*stepSize;
		
		for(int i = 1; i < 50; i++)
		{
			s1r += dr;
			s1g += dg;
			s1b += db;
			
			array[i] = new Color((int)s1r, (int)s1g, (int)s1b);
		}
		
		s1r = midRed;
		s1g = midGreen;
		s1b = midBlue;
		dr = (endRed - midRed)*stepSize;
		dg = (endGreen - midGreen)*stepSize;
		db = (endBlue - midBlue)*stepSize;
		for(int i = 51; i < 99; i++)
		{
			s1r += dr;
			s1g += dg;
			s1b += db;
			
			array[i] = new Color((int)s1r, (int)s1g, (int)s1b);
		}
		return array;
	}
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

