import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class NorbSamplerTest extends NorbSampler{
	
	//test only parameters
	private int minDist; //minimum distance from the most similar frame in the training set for each object
	private boolean [][][][][] usedFrames; //class, instance, elevation, azimuth, lighting
	private double sumDist; //average distance from training set
	private String TrainFileName; //file from which to read
	
	//this info will be read in the train_conf file
	private int nSeqxObjTrain; //number of sequence for each object
	private int seedTrain; //seed for random number generator
	
	public NorbSamplerTest() throws IOException{
		this(	"test_conf.txt", //name of the test config to be created
				"train_conf.txt", //name of the train config to be read
				1,	//number of sequence to be created for each object
				0, //minimum distance from the most similar frame in the training set for each object
				new boolean[5][10][9][18][6], //class, instance, elevation, azimuth, lighting
				2 //seed for the random generator
				);
	}
	
	public NorbSamplerTest(String fileName, String TrainFileName, int nSeqxObj, int minDist, boolean [][][][][] usedFrames, int seed) throws IOException{
		super(	//zero elements will be updated later reading the training file
				fileName, //name of the train config file
				0,	//number of classes
				0, //number of objects per class
				nSeqxObj,	//number of sequence for each object
				new double[4],	//probability for elevation, azimuth, lighting and to flip back
				0, //length of each sequence
				0);	//seed for random generator
		
		this.TrainFileName = TrainFileName; //name of the test config to be created
		this.nSeqxObj = nSeqxObj; //number of sequence to be created for each object
		this.minDist = minDist; //minimum distance from the most similar frame in the training set for each object
		this.usedFrames = usedFrames; //class, instance, elevation, azimuth,lighting

		writeTestConfig();
	}
	
	public void skipLines(BufferedReader reader, int n) throws IOException{
		for(int i=0; i<n; i++)
			reader.readLine();
	}
	
	public void readTrainFile() throws IOException {
		File r = new File(TrainFileName);
		BufferedReader reader = new BufferedReader(new FileReader(r));
		String[] parts;
		
		//Skipping header
		reader.readLine();reader.readLine();
		
		parts = reader.readLine().split(" ");
		nClass = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		nObjxClass = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		nSeqxObjTrain = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		prob[0] = Double.parseDouble(parts[1]);
		
		parts = reader.readLine().split(" ");
		prob[1] = Double.parseDouble(parts[1]);
		
		parts = reader.readLine().split(" ");
		prob[2] = Double.parseDouble(parts[1]);
		
		parts = reader.readLine().split(" ");
		prob[3] = Double.parseDouble(parts[1]);
		
		parts = reader.readLine().split(" ");
		seqLen = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		seedTrain = Integer.parseInt(parts[1]);
		seed = seedTrain+1;
		setSeed(seed);
		
		//discard dash line
		reader.readLine();
		
		//initialize matrix 
		for(int c=0; c<5; c++)
			for(int i=0; i<10; i++)
				for(int j=0; j<9; j++)
					for(int k=0; k<18; k++)
						for(int g=0; g<6; g++)
							usedFrames[c][i][j][k][g] = false;
		
		for(int clas=0; clas<nClass; clas++)
			for(int obj=0; obj<nObjxClass; obj++)
				for(int seq=0; seq<nSeqxObjTrain; seq++){
					//discard blank line and header lines
					skipLines(reader, 6);
					for(int i=0; i<seqLen; i++){
						String line = reader.readLine().substring(0,11);
						parts = line.split("_");
						System.out.println(line);
						usedFrames[clas][Integer.parseInt(parts[0])][Integer.parseInt(parts[1])]
								[Integer.parseInt(parts[2])/2][Integer.parseInt(parts[3])] = true;
						System.out.println("Class: "+clas+", Used: "+Integer.parseInt(parts[0])+" "+ Integer.parseInt(parts[1])+
								" "+ Integer.parseInt(parts[2])+ " "+ Integer.parseInt(parts[3]));
					}
				}	
		reader.close();
	}
	
	public boolean usedInTrain(int clas, int obj, int elevation, int azimuth, int lighting){
		System.out.println(String.format("Trying: %02d_%02d_%02d_%02d.bmp", obj,elevation, azimuth, lighting)); 
		boolean found = false;
		int dist=0; 
		for(int d=0; !found || dist>d; d++ ){
			//System.out.println("d: "+ d);
			for(int i=elevation-d; i<=elevation+d; i++ ){
				if(i<0)
					i=0;
				if(i>8)
					break;
				//System.out.println("cosa succede: "+ i);
				for(int j=azimuth/2-d; j<=azimuth/2+d; j++){
					if(j<0)
						j=0;
					if(j>17)
						break;
					for(int k=lighting-d; k<=lighting+d; k++ ){
						if(k<0)
							k=0;
						if(k>5)
							break;
						//System.out.println("index: "+ obj +" "+ i +" "+ j +" "+ k);
						if(usedFrames[clas][obj][i][j][k]){
							int ris = Math.abs(i-elevation) + Math.abs(j-azimuth/2) + Math.abs(k-lighting);
							//System.out.println("hei...used-->"+obj+" "+ i+" "+j+" "+k+"--->"+ris);
							if(found){
								if(dist > ris)
									dist = ris;
							}
							else{
								//System.out.println("found: "+ obj +" "+ i +" "+ j +" "+ k);
								found = true;
								dist = ris;
							}
						}
					}
				}
			}
		}
//		String img = String.format("%02d_%02d_%02d_%02d.bmp", obj,elevation, azimuth, lighting);
//		if(img.equals("00_01_34_03.bmp")){
//			System.out.println("trovato");
//			System.out.println(usedFrames[0][0][3][17][3]);
//		}
			
		
		if(dist >= minDist){
			sumDist += dist;
			System.out.println("Accepted! dist: "+ dist);
			return false; //not used in train, we can proceed
		}
		else{
			System.out.println("Rejected! dist: "+ dist);
			return true;
		}
	}
	
	private void writeSeqAttempt(BufferedWriter writer, ArrayList<String> seq) throws IOException {
		for(int i=0; i<seq.size(); i++)
			writer.write(seq.get(i));
	}
	
	public void writeTestConfig() throws IOException{
		File w = new File(fileName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(w));
		readTrainFile();
		
		//writing configuration parameters
		writer.write("Config Type: Test\n");
		writer.write("------------------\n");
		writer.write("nClass: "+ nClass +"\n");
		writer.write("nObjxClass: "+ nObjxClass +"\n");
		writer.write("nSeqxObj: "+ nSeqxObj + "\n");
		writer.write("elevationProb: "+ prob[0] +"\n");
		writer.write("azimuthProb: "+ prob[1] +"\n");
		writer.write("lightingProb: "+ prob[2] +"\n");
		writer.write("flipProb: "+ prob[3] +"\n");
		writer.write("seqLen: "+ seqLen +"\n");
		writer.write("seed: "+ seed +"\n");
		writer.write("minDistance: "+ minDist + "\n");
		writer.write("------------------\n");
		
		//for each class
		for(int clas=0; clas<nClass; clas++ ){
			
			//for each object
			for(int obj=0; obj<nObjxClass; obj++){
				
				//for each sequence
				for(int seq=0; seq<nSeqxObj; seq++){
					//support array for temporary seq
					ArrayList<String> seqAttempt = new ArrayList<String>();
					//if we reach 100 attempts we choose another starting point
					int numAttempts = 0;
					
					seqAttempt.add("\n---------------\n");
					seqAttempt.add("Class:    "+clas+"\nObject:   "+ obj + "\nSequence: " + seq + "\n");
					seqAttempt.add("---------------\n");
					
					//choosing starting point
					int elevation, azimuth, lighting;
					int exElevation, exAzimuth, exLighting;
					String firstFrame;
					do{
						//0 to 8, which mean cameras are 30,35,40,45,50,55,60,65,70 degrees from the horizontal 
						elevation = randInt(0,8);
						//0,2,4,...,34, multiply by 10 to get the azimuth in degrees
						azimuth = randInt(0,17)*2;
						//0 to 5 (lighting)
						lighting = randInt(0,5); 
						firstFrame = String.format("%02d_%02d_%02d_%02d.bmp", obj,elevation, azimuth, lighting); 
						//System.out.println("trying: "+firstFrame+" matrix: "+usedFrames[obj][elevation][azimuth/2][lighting]);
					}while(usedInTrain(clas ,obj, elevation, azimuth, lighting));
					seqAttempt.add(firstFrame + "\n");
					
					//testing
					//System.out.println("second round..");
					//usedInTrain(0, 4, 8, 4);
					
					//rollAgain == true means that a new random extraction is needed
					boolean rollAgain;
					
					//for each frame in the sequence
					for(int j=0; j<seqLen-1; j++){
						//choosing next move 
						do{
							double dice = rn.nextDouble();
							//move elevation
							if(dice >= 0 && dice < prob[0]){
								exElevation = elevation;
								elevation = moveElevation(elevation);
								if(usedInTrain(clas, obj, elevation, azimuth, lighting)){
									rollAgain = true;
									elevation = exElevation;
									numAttempts++;
								}	
								else{
									rollAgain = false;
									numAttempts = 0;
								}
								
							}
							//move azimuth
							else if (dice >= prob[0] && dice < prob[0]+prob[1]){
								exAzimuth = azimuth;
								azimuth = moveAzimuth(azimuth);
								if(usedInTrain(clas, obj, elevation, azimuth, lighting)){
									rollAgain = true;
									azimuth = exAzimuth;
									numAttempts++;
								}
								else{
									rollAgain = false;
									numAttempts = 0;
								}
								
							}
							//move lighting
							else {
								exLighting = lighting;
								lighting = moveLighting(lighting);
								if(usedInTrain(clas, obj, elevation, azimuth, lighting)){
									rollAgain = true;
									lighting = exLighting;
									numAttempts++;
								}
								else{
									rollAgain = false;
									numAttempts = 0;
								}
							}
							//System.out.println(String.format("%02d_%02d_%02d_%02d.bmp", obj, elevation, azimuth, lighting));
							//writer.close(); System.exit(0);
						}while(rollAgain && numAttempts != 100);
						
						if(numAttempts == 100)
							break;

						//writing frame
						String frame = String.format("%02d_%02d_%02d_%02d.bmp", obj, elevation, azimuth, lighting); 
						seqAttempt.add(frame + "\n");
					}
					if(numAttempts == 100){
						System.out.println("Warning: max num attempts reached!");
						seq--; //we restart to build the same sequence.
						continue;
					}
					writeSeqAttempt(writer, seqAttempt);
				}
			}
		}
		System.out.println("sumDist: "+sumDist);
		//System.out.println((nClass*nObj*nSeqxObj*seqLen));
		double avgDist = sumDist/(nClass*nObjxClass*nSeqxObj*seqLen);
		writer.write("---------------\n");
		writer.write("average distance: "+ avgDist + "\n");
		writer.close();
		
		SeqVerifier ver = new SeqVerifier(TrainFileName, fileName);
		if(!ver.verifyAll())
		{
			System.out.println("ERROR IN THE SEQUENCE!");
			System.exit(-1);
		}
		
	}
	
    public static void main(String args[]) throws IOException{
    	NorbSamplerTest sampler = new NorbSamplerTest();
    }
}
