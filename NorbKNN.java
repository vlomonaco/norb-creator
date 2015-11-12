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
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

public class NorbKNN {
	
	private int width;
	private int height;
	private String repo;
	private boolean writeDatasetFile; //write dataset file or not
	private String testFile;
	private String trainFile;
	private Instances testSet[][][];
	private Instances trainSet[][][];
	private boolean cumulativeConf;
	
	//this info will be read in the train_conf file
	private int nClass; //number of classes
	private int nObjxClass; //number of objects
	private int nSeqxObjTrain; //number of sequence for each object
	private double[] prob = new double[4]; //probability for elevation, azimuth, lighting and to flip back
	private int seqLen; //length of each sequence
	private int seed; //seed for random number generator
	private int nSeqxObj; //number of sequence for each object in the test set
	private int minDist; //minimum distance from the most similar frame in the training set for each object
	
	public NorbKNN(int width, int height, String repo, String trainFile, String testFile, 
				   boolean writeDatasetFile, boolean cumulativeConf) throws Exception {
		this.width = width;
		this.height = height;
		this.repo = repo;
		this.trainFile = trainFile;
		this.testFile = testFile;
		this.writeDatasetFile = writeDatasetFile; 
		this.cumulativeConf = cumulativeConf;
	}
	
	public NorbKNN() throws Exception{
		//default values
		this(
			32,
			32,
			"all32/L/",
			"train_conf.txt",
			"test_conf.txt",
			false,
			true
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
		trainSet = new Instances[nClass][nObjxClass][nSeqxObjTrain];
		testSet = new Instances[nClass][nObjxClass][nSeqxObj];
		
		//preparing the dateset structure
		for(int clas=0; clas<nClass; clas++)
			for(int obj=0; obj<nObjxClass; obj++){
				//train sets
				for(int seq=0; seq<nSeqxObjTrain; seq++){
					FastVector atts = new FastVector(width*height+1);
			        FastVector classVal = new FastVector();
			        for(int k=0; k<nClass; k++)
			        	classVal.addElement(Integer.toString(k));
			        for(int k=0; k<width*height; k++)
			        	atts.addElement(new Attribute("A"+k));
			        atts.addElement(new Attribute("class", classVal));
			        trainSet[clas][obj][seq] = new Instances("TrainDataset", atts, 0);
			        trainSet[clas][obj][seq].setClassIndex(trainSet[clas][obj][seq].numAttributes() - 1);
				}
				//test sets
				for(int seq=0; seq<nSeqxObj; seq++){
					FastVector atts = new FastVector(width*height+1);
			        FastVector classVal = new FastVector();
			        for(int k=0; k<nClass; k++)
			        	classVal.addElement(Integer.toString(k));
			        for(int k=0; k<width*height; k++)
			        	atts.addElement(new Attribute("A"+k));
			        atts.addElement(new Attribute("class", classVal));
			        testSet[clas][obj][seq] = new Instances("TestDataset", atts, 0);
			        testSet[clas][obj][seq].setClassIndex(testSet[clas][obj][seq].numAttributes() - 1);
				}
			}
		        
        double[] attrValues = new double[trainSet[0][0][0].numAttributes()];
        
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
				        	attrValues[j] = (img[j]& 0xFF);
				        attrValues[trainSet[clas][obj][seq].numAttributes()-1] = clas ;
				        trainSet[clas][obj][seq].add(new Instance(1, attrValues.clone()));
					}

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
				        	attrValues[j] = (img[j]& 0xFF);
				        attrValues[testSet[clas][obj][seq].numAttributes()-1] = clas;
				        testSet[clas][obj][seq].add(new Instance(1, attrValues.clone()));
					}

				}	
		
		reader_train.close();
		reader_test.close();
	}
	
	private double computeAccuracy(Instances testData) throws Exception{
		Instances trainSet4c;
		
		FastVector atts = new FastVector(width*height+1);
        FastVector classVal = new FastVector();
        for(int k=0; k<nClass; k++)
        	classVal.addElement(Integer.toString(k));
        for(int k=0; k<width*height; k++)
        	atts.addElement(new Attribute("A"+k));
        atts.addElement(new Attribute("class", classVal));
        trainSet4c = new Instances("TrainDataset", atts, 0);
        trainSet4c.setClassIndex(trainSet4c.numAttributes() - 1);
        
        LinearNNSearch dist;
        for(int i=0; i<testData.numInstances(); i++)
			for(int clas=0; clas<nClass; clas++)
				for(int obj=0; obj<nObjxClass; obj++)
					for(int seq=0; seq<nSeqxObjTrain; seq++){
						dist = new LinearNNSearch(trainSet[clas][obj][seq]);
						Instance nearest = dist.nearestNeighbour(testData.instance(i));
						trainSet4c.add(nearest);
					}
        //System.out.println(trainSet4c.numInstances());
        //System.out.println(testData.numInstances());
        
		System.out.println("Building the Classifier..");
		
		Classifier ibk = new IBk();		
		ibk.buildClassifier(trainSet4c);
		
		System.out.println("Classifier built..");
		int nTestInst = testSet[0][0][0].numInstances();
		double predClas[] = new double[nTestInst];
		double storedConf[] = new double[nTestInst];
		
		int guessed =0;
		for(int i=0; i<nTestInst; i++){
			double[] confidence = ibk.distributionForInstance(testData.instance(i));
			
			//predClas[i] = ibk.classifyInstance(testData.instance(i)); //is the same as the lines above
			int clas = 0;
			double max = 0;
			
			if(cumulativeConf){
				for(int k=0; k<testData.numClasses(); k++){
					storedConf[k] += confidence[k];
					if(max < storedConf[k]){
						max = storedConf[k];
						clas = k;
					}	
					//System.out.println("storedConf["+k+"]"+": "+storedConf[k]);
					//System.out.println("Conf["+k+"]"+": "+confidence[k]);
				}
			}
			else{
				for(int k=0; k<testData.numClasses(); k++)
					if(max < confidence[k]){
						max = confidence[k];
						clas = k;
					}
			}
			predClas[i] = clas;
			
			System.out.println("real class: " + testData.instance(i).classValue() + " Predicted: " + predClas[i]);
			if(testData.instance(i).classValue() == predClas[i])
				guessed++;
		}
		double accuracy = ((double)guessed/nTestInst)*100;
		//System.out.println("Accuracy: " + accuracy + "%");
		//return the accuracy
		return accuracy;
	}
	
	public double createDatasetAndTest() throws Exception{
		readTrainTestFiles();
		
		//compute accuracy for each sequence in the test set
		double sum = 0;
		int attempts = 0;
		for(int clas=0; clas<nClass; clas++)
			for(int obj=0; obj<nObjxClass; obj++)
				for(int seq=0; seq<nSeqxObj; seq++){
					sum += computeAccuracy(testSet[clas][obj][seq]);
					attempts++;
				}
		return sum/attempts;
	}	
	
    public static void main(String args[]) throws Exception{
    	NorbKNN knn = new NorbKNN();
    	double accuracy = knn.createDatasetAndTest();
    	System.out.println("Accuracy: " + accuracy + "%");
    }
}