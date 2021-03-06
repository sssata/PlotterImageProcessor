import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;


public class Plotter extends JFrame implements PropertyChangeListener{

	final static int width = 400;
	final static int height = 300;
	static int MAX_WIDTH = 0;
	static int MAX_HEIGHT = 0;
	static int IMAGE_HEIGHT = 600;
	static int IMAGE_WIDTH = 300;
	static double contrast = 1;
	static double brightness = 0;

	JLabel previewWindow;
	JLabel heightLabel;
	JLabel contrastLabel;
	JLabel brightnessLabel;
	JButton openButton;
	JButton processButton;
	JButton saveButton;
	JFormattedTextField heightField;
	JFormattedTextField contrastField;
	JFormattedTextField brightnessField;
	JCheckBox transposeBox;
	
	NumberFormat format;
	
	BufferedImage imageOriginal;
	BufferedImage image;
	
	ImageIcon icon;
	
	boolean [][] imageArray;
	
	JFileChooser fc;
	
	public Plotter(){
		
		super("Plotter Image Processer");
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		MAX_WIDTH = (int) screenSize.getWidth() - 50;
		MAX_HEIGHT = (int) screenSize.getHeight()- 100;
		icon = new ImageIcon("icon.png");
		setIconImage(icon.getImage());
		
		//addMouseListener(this);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new ImageFilter());
		
		
		
		// OPEN BUTTON
		openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener()
	    {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				int returnVal = fc.showOpenDialog(Plotter.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						imageOriginal = ImageIO.read(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Can't open image...");
						e.printStackTrace();
					}
					
					// Scale down image to fit screen
					if(imageOriginal.getWidth() > MAX_WIDTH){
						imageOriginal = toScale(imageOriginal, MAX_WIDTH, imageOriginal.getHeight()*MAX_WIDTH/imageOriginal.getWidth());
					}
					if(imageOriginal.getHeight() > MAX_HEIGHT){
						imageOriginal = toScale(imageOriginal, imageOriginal.getWidth()*MAX_HEIGHT/imageOriginal.getHeight(), MAX_HEIGHT);
					}
					
					previewWindow.setIcon(new ImageIcon(imageOriginal));
				}
				else {
					// no file chosen
				}
				

				
				pack();
			}
	    });
		
		//PROCESS BUTTON
		processButton = new JButton("Process");
		processButton.addActionListener(new ActionListener()
	    {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (imageOriginal != null && IMAGE_HEIGHT != 0){
					
					IMAGE_HEIGHT = ((Number)heightField.getValue()).intValue();
					
					IMAGE_WIDTH = imageOriginal.getWidth()*IMAGE_HEIGHT/imageOriginal.getHeight();
				
					image = toScale(imageOriginal,IMAGE_WIDTH, IMAGE_HEIGHT);
					image = toGreyscale(image);
					image = dither(image);
				
					previewWindow.setIcon(new ImageIcon(image));
					pack();
				
					imageArray = toBooleanArray(image);
					
				}
			}
		});
		
		// SAVE BUTTON
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener()
	    {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				int returnVal = fc.showSaveDialog(Plotter.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try(PrintWriter pw = new PrintWriter(fc.getSelectedFile()+".txt")) {
						
						// WRITE TO FILE
					    
					    if (transposeBox.isSelected()){ // LANDSCAPE MODE
					    	
						    pw.println(IMAGE_HEIGHT + " " + IMAGE_WIDTH);
					    	
						    for (int x=0; x<IMAGE_WIDTH; x++){
						    	for (int y=0; y<IMAGE_HEIGHT; y++){
						    		if (imageArray[x][y]){
						    			pw.print(1);
						    		} else {
						    			pw.print(0);
						    		}
						    		//System.out.println(imageArray[y][x]);
						    	}
						    	pw.println();
						    }
					    }
					    
					    
					    else{ // PORTRAIT MODE
					    	
					    	pw.println(IMAGE_WIDTH + " " + IMAGE_HEIGHT);
					    	
					    	for (int y=0; y<IMAGE_HEIGHT; y++){
					    		for (int x=0; x<IMAGE_WIDTH; x++){
					    			if (imageArray[x][y]){
					    				pw.print(1);
					    			} else {
					    				pw.print(0);
					    			}
					    			//System.out.println(imageArray[x][y]);
					    		}
					    		pw.println();
					    	}
					    }
					    
					    pw.close();
					    
					} catch (IOException e) {
						System.out.println("Can't save file...");
						e.printStackTrace();
					}
					
				} else {
					// no file chosen
				}
			}
		});
		
		// PREVIEW WINDOW
		previewWindow = new JLabel(" ");
		
		
		// HEIGHT INPUT
		heightLabel = new JLabel("Height");
		format = NumberFormat.getNumberInstance();
		heightField = new JFormattedTextField(format);
		heightField.setPreferredSize(new Dimension(60,20));
		heightField.setValue(IMAGE_HEIGHT);
		heightField.addPropertyChangeListener("value", this);

		
		// TRANSPOSE BUTTON
		transposeBox = new JCheckBox("Transpose");
		transposeBox.setSelected(true);
		
		// CONTRAST INPUT
		contrastLabel = new JLabel("Contrast");
		contrastField = new JFormattedTextField(new Double(1.0));
		contrastField.setPreferredSize(new Dimension(40,20));
		contrastField.setValue(contrast);
		contrastField.addPropertyChangeListener("value", this);
		
		// Brightness INPUT
		brightnessLabel = new JLabel("Brightness");
		brightnessField = new JFormattedTextField(new Double(0.0));
		brightnessField.setPreferredSize(new Dimension(40,20));
		brightnessField.setValue(brightness);
		brightnessField.addPropertyChangeListener("value", this);
		
		
		
		// LAYOUTS
		Container pane = getContentPane();
		GroupLayout layout = new GroupLayout(pane);
		pane.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			
				.addGroup(layout.createSequentialGroup()
						.addComponent(openButton)
						.addComponent(processButton)
						.addComponent(saveButton))

				.addGroup(layout.createSequentialGroup()
						.addComponent(heightLabel)
						.addGap(5)
						.addComponent(heightField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						          GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(brightnessLabel)
						.addGap(5)
						.addComponent(brightnessField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						          GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(contrastLabel)
						.addGap(5)
						.addComponent(contrastField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						          GroupLayout.PREFERRED_SIZE)

						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(transposeBox))
						
				.addComponent(previewWindow)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
			
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(openButton)
					.addComponent(processButton)
					.addComponent(saveButton))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(heightLabel)
					.addComponent(heightField)
					.addComponent(transposeBox)
					.addComponent(contrastLabel)
					.addComponent(contrastField)
					.addComponent(brightnessLabel)
					.addComponent(brightnessField))
					
				.addComponent(previewWindow)
		);
		
		pack();
		setLocationRelativeTo(null);
		
	}
	
	public static void main(String Args[]){
		JFrame PlotterFrame = new Plotter();
		//lotterFrame.setSize(width, height);
		PlotterFrame.pack();
		PlotterFrame.setVisible(true);
	}
	
	
	BufferedImage dither(BufferedImage image){
		
        int width = image.getWidth();
        int height = image.getHeight();
        
		short array[][] = new short [width][height];
		
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++) {
				Color c = new Color(image.getRGB(j, i));
				array[j][i] = (short) c.getRed();
			}
		}
        
		for (int i = 0; i < height; i++) {
			
			for (int j = 0; j < width; j++) {
				
				int current = array[j][i];
				int error = 0;
				
				if ((current) > 128){
					image.setRGB(j, i, Color.WHITE.getRGB());
					error = current - 255;
				} else {
					image.setRGB(j, i, Color.BLACK.getRGB());
					error = current - 0;
				}
				

				double divisor = 1.0/32;
				
				// current row
				if (j<width-1)			array[j+1][i] += (short) (error*divisor*5);
				if (j<width-2)			array[j+2][i] += (short) (error*divisor*3);
				
				// next row
				if (i<height-1){
					if (j>1)			array[j-2][i+1] += (short) (error*divisor*2);
					if (j>0) 			array[j-1][i+1] += (short) (error*divisor*4);
					if (true) 			array[j][i+1] += (short) (error*divisor*5);
					if (j<width-1) 		array[j+1][i+1] += (short) (error*divisor*4);
					if (j<width-2) 		array[j+2][i+1] += (short) (error*divisor*2);
				}
				
				// next next row
				if (i<height-2){
					if (j>0) 			array[j-1][i+2] += (short) (error*divisor*2);
					if (true) 			array[j][i+2] += (short) (error*divisor*3);
					if (j<width-1) 		array[j+1][i+2] += (short) (error*divisor*2);
				}
			}
		}
		
		System.out.println(width);

		return image;
	}
	
	
	BufferedImage toGreyscale(BufferedImage image) {

		int width = image.getWidth();
		int height = image.getHeight();

		for (int i = 0; i < height; i++) {

			for (int j = 0; j < width; j++) {
				
				double multiplier = contrast;

				Color c = new Color(image.getRGB(j, i));
				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);
				
				int lum = (int) (Math.round(red+green+blue-128)*multiplier + 128 + brightness*12.8);
				if (lum < 0) lum = 0;
				if (lum > 255) lum = 255;
				
				Color newColor = new Color(lum, lum, lum);

				image.setRGB(j, i, newColor.getRGB());
			}
		}

		/*
		 * ColorAdjust colorAdjust = new ColorAdjust();
		 * colorAdjust.setContrast(0.6); WritableImage contrastImage =
		 * SwingFXUtils.toFXImage(image, null); ImageView imageView = new
		 * ImageView (contrastImage); imageView.setEffect(colorAdjust);
		 * contrastImage image = SwingFXUtils.fromFXImage(contrastImage, null);
		 */
		return image;

	}

	BufferedImage toScale(BufferedImage image, int width, int height){
		
		if (width == 0 || height == 0) return image;
		return toBufferedImage(image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING));
		
	}
	
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
	public static boolean[][] toBooleanArray(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		boolean[][] array = new boolean[width][height];

		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				Color c = new Color(image.getRGB(x, y));

				if (c.getRed() > 127) {
					array[x][y] = true;
				} else {
					array[x][y] = false;
				}
			}
		}

		return array;
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if (source == heightField) {
        	if(((Number)heightField.getValue()).intValue() > 1000){
        		heightField.setValue(1000);
        	}
        }
        if (source == contrastField) {
        	contrast = ((Number)contrastField.getValue()).doubleValue();
        }
        if (source == brightnessField) {
        	brightness = ((Number)brightnessField.getValue()).doubleValue();
        }
	}
	
}

