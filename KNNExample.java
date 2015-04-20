import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;


import javax.imageio.ImageIO;
 
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
 
public class KNNExample {
	
	static int width = 32;
	static int height = 32;
	static String repo = ".."+File.separator+"Data"+File.separator+"train32"+File.separator;
 
	public static void main(String[] args) throws Exception {
		
		byte [] img1 = new byte[width*height];
		byte [] img2 = new byte[width*height];
		byte [] img3 = new byte[width*height];
		
		BufferedImage image = ImageIO.read(new File(repo + "00_00_00_00.bmp"));
		Raster raster = image.getData();
		for (int i = 0; i < width; i++) {
		    for (int j = 0; j < height; j++) {
		        img1[i + j*width] = (byte)raster.getSample(i, j, 0);
		    }
		}
		
		image = ImageIO.read(new File(repo + "00_00_02_02.bmp"));
		raster = image.getData();
		for (int i = 0; i < width; i++) {
		    for (int j = 0; j < height; j++) {
		        img2[i + j*width] = (byte)raster.getSample(i, j, 0);
		    }
		}
		
		image = ImageIO.read(new File(repo + "00_00_04_00.bmp"));
		raster = image.getData();
		for (int i = 0; i < width; i++) {
		    for (int j = 0; j < height; j++) {
		        img3[i + j*width] = (byte)raster.getSample(i, j, 0);
		    }
		}
		
        FastVector atts = new FastVector(width*height+1);
        FastVector classVal = new FastVector();
        classVal.addElement("0");
        classVal.addElement("1");
        for(int i=0; i<width*height; i++)
        	atts.addElement(new Attribute("A"+i));
        atts.addElement(new Attribute("class", classVal));
        Instances data = new Instances("ExampleDataset", atts, 0);
        data.setClassIndex(data.numAttributes() - 1);
        
        double[] instanceValue1 = new double[data.numAttributes()];
        double[] instanceValue2 = new double[data.numAttributes()];
        double[] instanceValue3 = new double[data.numAttributes()];
        
        for(int i=0; i<width*height; i++)
        	instanceValue1[i] = (img1[i]& 0xFF);
        for(int i=0; i<width*height; i++)
        	instanceValue2[i] = (img2[i]& 0xFF);
        for(int i=0; i<width*height; i++)
        	instanceValue3[i] = (img3[i]& 0xFF);
        instanceValue2[data.numAttributes()-1] = 1.0;
		
		data.add(new Instance(1, instanceValue1));
		data.add(new Instance(1, instanceValue2));
		
		System.out.println(data);
 
		Classifier ibk = new IBk();		
		ibk.buildClassifier(data);
 
		Instance test = new Instance(1, instanceValue3);
		test.setDataset(data);
		double predClas = ibk.classifyInstance(test);
		System.out.println("real class: " + test.classValue() + " Predicted: " + predClas);
	}
}