import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
 
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class NorbKNN {
	
	private int width;
	private int height;
	private String repo;
	private boolean writeDatasetFile; //write dataset file or not
	private String testFile;
	private String trainFile;
	private Instances dataset;
	
	//this info will be read in the train_conf file
	private int nClass; //number of classes
	private int nObjxClass; //number of objects
	private int nSeqxObjTrain; //number of sequence for each object
	private double[] prob = new double[4]; //probability for elevation, azimuth, lighting and to flip back
	private int seqLen; //length of each sequence
	private int seed; //seed for random number generator
	private int nSeqxObj; //number of sequence for each object in the test set
	private int minDist; //minimum distance from the most similar frame in the training set for each object
	
	public NorbKNN(int width, int height, String repo, String trainFile, String testFile, boolean writeDatasetFile) throws Exception {
		this.width = width;
		this.height = height;
		this.repo = ".."+File.separator+"Data"+File.separator+"all32"+File.separator+"L"+File.separator;
		this.trainFile = trainFile;
		this.testFile = testFile;
		this.writeDatasetFile = writeDatasetFile; 
	}
	
	public NorbKNN() throws Exception{
		//default values
		this(
			32,
			32,
			".."+File.separator+"Data"+File.separator+"all32"+File.separator+"L"+File.separator,
			"train_conf.txt",
			"test_conf.txt",
			false
			);
	}
	
	private void skipLines(BufferedReader reader, int n) throws IOException{
		for(int i=0; i<n; i++)
			reader.readLine();
	}
	
	private byte[] extractImg(int clas, String fileName) throws IOException{
		BufferedImage image = ImageIO.read(new File(repo + clas + File.separator + fileName));
		Raster raster = image.getData();
		byte[] img = new byte[width*height];
		
		for (int j = 0; j < width; j++) {
		    for (int k = 0; k < height; k++) {
		        img[j + k*width] = (byte)raster.getSample(j, k, 0);
		    }
		}
		return img;
	}
	private void writeDataset(String datasetFileName, BufferedReader reader_train, BufferedReader reader_test) throws IOException{
		File dataset = new File(datasetFileName);
		BufferedWriter writer_dataset = new BufferedWriter(new FileWriter(dataset));
		
		//frist lines in the dataset file
		writer_dataset.write("@relation 2classify\n\n");
		for(int i=0; i<width*height*seqLen; i++)
			writer_dataset.write("@attribute A"+i+" numeric\n");
		writer_dataset.write("@attribute class {");
		for(int i=0; i<nClass; i++){
			if(i!=nClass-1)
				writer_dataset.write(i+",");
			else
				writer_dataset.write(i+"}\n\n");
		}
		writer_dataset.write("@data\n");
		
		byte[] img;
		
		//reading train file image
		for(int clas=0; clas<nClass; clas++)
			for(int obj=0; obj<nObjxClass; obj++)
				for(int seq=0; seq<nSeqxObjTrain; seq++){
					//discard header line
					reader_train.readLine(); 
					for(int i=0; i<seqLen; i++){
						String line = reader_train.readLine();
						System.out.println("[Train] processing: "+line);
						img = extractImg(clas, line);
						for(int k=0; k<width*height; k++)
							writer_dataset.write((img[k]& 0xFF)+",");
					}
					writer_dataset.write(clas+"\n");
				}	
		
		//reading test file image
		for(int clas=0; clas<nClass; clas++)
			for(int obj=0; obj<nObjxClass; obj++)
				for(int seq=0; seq<nSeqxObj; seq++){
					//discard header line
					reader_test.readLine(); 
					for(int i=0; i<seqLen; i++){
						String line = reader_test.readLine();
						System.out.println("[Test] processing: "+line);
						img = extractImg(clas, line);
						for(int k=0; k<width*height; k++)
							writer_dataset.write((img[k]& 0xFF)+",");
					}
					writer_dataset.write(clas+"\n");
				}	
		writer_dataset.close();
	}
	
	private void readTrainTestFiles() throws IOException {

		File train = new File(trainFile);
		File test = new File(testFile);
		BufferedReader reader_train = new BufferedReader(new FileReader(train));
		BufferedReader reader_test = new BufferedReader(new FileReader(test));

		String[] parts;
		
		//reading train param
		skipLines(reader_train, 4);
		parts = reader_train.readLine().split(" ");
		nSeqxObjTrain = Integer.parseInt(parts[1]);
		skipLines(reader_train, 7);
		
		//reading test param
		skipLines(reader_test, 2);
		
		parts = reader_test.readLine().split(" ");
		nClass = Integer.parseInt(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		nObjxClass = Integer.parseInt(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		nSeqxObj = Integer.parseInt(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		prob[0] = Double.parseDouble(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		prob[1] = Double.parseDouble(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		prob[2] = Double.parseDouble(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		prob[3] = Double.parseDouble(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		seqLen = Integer.parseInt(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		seed = Integer.parseInt(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		minDist = Integer.parseInt(parts[1]);
		
		//discard dash line
		reader_test.readLine(); 
		
		if(writeDatasetFile)
			writeDataset("2classify.txt", reader_train, reader_test);
		else{
			FastVector atts = new FastVector(width*height+1);
	        FastVector classVal = new FastVector();
	        for(int i=0; i<nClass; i++)
	        	classVal.addElement(Integer.toString(i));
	        for(int i=0; i<width*height*seqLen; i++)
	        	atts.addElement(new Attribute("A"+i));
	        atts.addElement(new Attribute("class", classVal));
	        dataset = new Instances("TrainDataset", atts, 0);
	        dataset.setClassIndex(dataset.numAttributes() - 1);
	        
	        double[] attrValues = new double[dataset.numAttributes()];
	        byte[] img = new byte[width*height];
			//reading train file image
			for(int clas=0; clas<nClass; clas++)
				for(int obj=0; obj<nObjxClass; obj++)
					for(int seq=0; seq<nSeqxObjTrain; seq++){
						//discard header line
						skipLines(reader_train, 6); 
						for(int i=0; i<seqLen; i++){
							String line = reader_train.readLine();
							System.out.println("[Train] processing: "+line);
							img = extractImg(clas, line);
					        for(int j=0; j<width*height; j++)
					        	attrValues[j+i*width*height] = (img[j]& 0xFF);
						}
				        attrValues[dataset.numAttributes()-1] = clas ;
				        dataset.add(new Instance(1, attrValues.clone()));
					}	
			
			//reading test file image
			for(int clas=0; clas<nClass; clas++)
				for(int obj=0; obj<nObjxClass; obj++)
					for(int seq=0; seq<nSeqxObj; seq++){
						//discard header line
						skipLines(reader_test, 6); 
						for(int i=0; i<seqLen; i++){
							String line = reader_test.readLine();
							System.out.println("[Test] processing: "+line);
							img = extractImg(clas, line);
					        for(int j=0; j<width*height; j++)
					        	attrValues[j+i*width*height] = (img[j]& 0xFF);
						}
				        attrValues[dataset.numAttributes()-1] = clas;
				        dataset.add(new Instance(1, attrValues.clone()));
					}	
		}
		
		reader_train.close();
		reader_test.close();
	}
	public double createDatasetAndTest() throws Exception{
		readTrainTestFiles();
		//System.exit(1);
		
		if(writeDatasetFile){ //else this work is already done
			BufferedReader dataFile = new BufferedReader(new FileReader("2classify.txt"));
			dataset = new Instances(dataFile);
			System.out.println("data size: "+ dataset.numInstances());
			dataset.setClassIndex(dataset.numAttributes() - 1);
			dataFile.close();
		}
		//System.out.println(dataset);
 
		//do not use the test sequences
		int nTrainInst = nClass*nObjxClass*nSeqxObjTrain;
		int nTestInst = nClass*nObjxClass*nSeqxObj;
		System.out.println("nTrainInst: "+ nTrainInst);
		System.out.println("nTestInst: "+ nTestInst);
		Instance[] test = new Instance[nTestInst];
		for(int i=nTrainInst, j=0; i<nTrainInst + nTestInst; i++, j++){
			//System.out.println("Processing: "+j);
			test[j] = dataset.instance(nTrainInst);
			dataset.delete(nTrainInst);
		}
		
		System.out.println("Building the Classifier..");
		
		Classifier ibk = new IBk();		
		ibk.buildClassifier(dataset);
		
		System.out.println("Classifier built..");
		double predClas[] = new double[nTestInst];
		int guessed =0;
		for(int i=0; i<nTestInst; i++){
			predClas[i] = ibk.classifyInstance(test[i]);
			System.out.println("real class: " + test[i].classValue() + " Predicted: " + predClas[i]);
			if(test[i].classValue() == predClas[i])
				guessed++;
		}
		double accuracy = ((double)guessed/nTestInst)*100;
		//System.out.println("Accuracy: " + accuracy + "%");
		//return the accuracy
		return accuracy;
	}	
	
    public static void main(String args[]) throws Exception{
    	NorbKNN knn = new NorbKNN();
    	double accuracy = knn.createDatasetAndTest();
    	System.out.println("Accuracy: " + accuracy + "%");
    }
}