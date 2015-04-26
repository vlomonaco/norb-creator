import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class NorbSamplerTrain extends NorbSampler {
	
	public NorbSamplerTrain() throws IOException{
		//default values
		this(	"train_conf.txt",
				2,	//number of classes
				10, //number of objects per class
				1,	//number of sequence for each object
				new double[]{0.35, 0.55, 0.1, 0.05},	//probability for elevation, azimuth, lighting and to flip back
				30, //length of each sequence
				1);	//seed for random generator
	}
	
	public NorbSamplerTrain(String fileName, int nClass, int nObjxClass, int nSeqxObj, double[] prob, int seqLen, int seed) throws IOException{
		super(	
				fileName, //name of the train config file
				nClass,	//number of classes
				nObjxClass, //number of objects per class
				nSeqxObj,	//number of sequence for each object
				prob,	//probability for elevation, azimuth, lighting and to flip back
				seqLen, //length of each sequence
				seed);	//seed for random generator
		
		writeTrainConfig();
	}

	public void writeTrainConfig() throws IOException{
		File f = new File(fileName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		
		//writing configuration parameters
		writer.write("Config Type: Train\n");
		writer.write("------------------\n");
		writer.write("nClass: "+ nClass +"\n");
		writer.write("nObjxClass: "+ nObjxClass +"\n");
		writer.write("nSeqxObj: "+ nSeqxObj +"\n");
		writer.write("elevationProb: "+ prob[0] +"\n");
		writer.write("azimuthProb: "+ prob[1] +"\n");
		writer.write("lightingProb: "+ prob[2] +"\n");
		writer.write("flipProb: "+ prob[3] +"\n");
		writer.write("seqLen: "+ seqLen +"\n");
		writer.write("seed: "+ seed +"\n");
		writer.write("------------------\n");
		
		//for each class
		for(int clas=0; clas<nClass; clas++ ){
			
			//for each object
			for(int obj=0; obj<nObjxClass; obj++){
				
				//for each sequence
				for(int seq=0; seq<nSeqxObj; seq++){
					writer.write("\n---------------\n");
					writer.write("Class:    "+clas+"\nObject:   "+ obj + "\nSequence: " + seq + "\n");
					writer.write("---------------\n");
					
					//choosing starting point
					// 0 to 8, which mean cameras are 30,35,40,45,50,55,60,65,70 degrees from the horizontal 
					int elevation = randInt(0,8);
					// 0,2,4,...,34, multiply by 10 to get the azimuth in degrees
					int azimuth = randInt(0,17)*2;
					// 0 to 5 (lighting)
					int lighting = randInt(0,5); 

					String firstFrame = String.format("%02d_%02d_%02d_%02d.bmp", obj,elevation, azimuth, lighting); 
					writer.write(firstFrame + "\n");
					
					//for each frame in the sequence
					for(int j=0; j<seqLen-1; j++){
						//choosing next move 

						double dice = rn.nextDouble();
						//move elevation
						if(dice >= 0 && dice < prob[0])
							elevation = moveElevation(elevation);
							
						//move azimuth
						else if (dice >= prob[0] && dice < prob[0]+prob[1])
							azimuth = moveAzimuth(azimuth);
							
						//move lighting
						else 
							lighting = moveLighting(lighting);
						
						//writing frame
						String frame = String.format("%02d_%02d_%02d_%02d.bmp", obj, elevation, azimuth, lighting); 
						writer.write(frame + "\n");
					}
				}
			}
		}
		writer.close();
	}
	
    public static void main(String args[]) throws IOException{
    	NorbSamplerTrain sampler = new NorbSamplerTrain();
    }
}
