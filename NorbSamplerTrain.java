import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class NorbSamplerTrain {
	
	static int nClass = 2; //5; number of classes
	static int nObjxClass = 10;//10; //number of objects per class
	static int nSeqxObj = 1;//5; //number of sequence for each object
	static double prob[] = {0.3, 0.3, 0.2, 0.2}; //probability for elevation, azimuth, lighting and to flip back
	static int seqLen = 10; //length of each sequence
	static int seed = 1234;
	static Random rn = new Random(seed);
	
	public static int randInt(int min, int max) {
	    return rn.nextInt((max - min) + 1) + min;
	}
	
	public static int moveElevation(int pastElev) {
	    double dice = rn.nextDouble();
	    if(dice <= 0.5)
	    	if(pastElev < 8)
	    		return pastElev+1;
	    	else
	    		return pastElev-1;
	    
	    else
	    	if(pastElev > 0)
	    		return pastElev-1;
	    	else
	    		return pastElev+1;
	    
	}
	
	public static int moveAzimuth(int pastAzim) {
	    double dice = rn.nextDouble();
	    if(dice <= 0.5)
	    	if(pastAzim < 34)
	    		return pastAzim+2;
	    	else
	    		return pastAzim-2;
	    
	    else
	    	if(pastAzim > 0)
	    		return pastAzim-2;
	    	else
	    		return pastAzim+2;
	    
	}
	
	public static int moveLighting(int pastLight) {
	    double dice = rn.nextDouble();
	    if(dice <= 0.5)
	    	if(pastLight < 5)
	    		return pastLight+1;
	    	else
	    		return pastLight-1;
	    
	    else
	    	if(pastLight > 0)
	    		return pastLight-1;
	    	else
	    		return pastLight+1;
	}
	
	public static boolean flip(int nFrame) {
		if (nFrame == 0)
	    	return false;
	    else
	    	return true;
	}
	
	public static void main(String[] args) throws IOException{
		
		File f = new File("train_conf.txt");
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
					//variables used to remember the previous move
					int exElevation=0, exAzimuth=0, exLighting=0;
					String firstFrame = String.format("%02d_%02d_%02d_%02d.bmp", obj,elevation, azimuth, lighting); 
					writer.write(firstFrame + "\n");
					
					//for each frame in the sequence
					for(int j=0; j<seqLen-1; j++){
						//choosing next move 
						//rollAgain == true means that a new random extraction is needed
						boolean  rollAgain = false; 
						do{
							double dice = rn.nextDouble();
							//move elevation
							if(dice >= 0 && dice < prob[0]){
								exElevation = elevation;
								elevation = moveElevation(elevation);
								rollAgain = false;
							}
							//move azimuth
							else if (dice >= prob[0] && dice < prob[0]+prob[1]){
								exAzimuth = azimuth;
								azimuth = moveAzimuth(azimuth);
								rollAgain = false;
							}
							//move lighting
							else if (dice >= prob[0]+prob[1] && dice < prob[0]+prob[1]+prob[2]){
								exLighting = lighting;
								lighting = moveLighting(lighting);
								rollAgain = false;
							}
							//move back
							else{
								if(!flip(seq))
									rollAgain = true;
								else{
									elevation = exElevation;
									azimuth = exAzimuth;
									lighting = exLighting;
								}			
							}
						}while(rollAgain);
						
						//writing frame
						String frame = String.format("%02d_%02d_%02d_%02d.bmp", obj, elevation, azimuth, lighting); 
						writer.write(frame + "\n");
					}
				}
			}
		}
		writer.close();
	}
}
