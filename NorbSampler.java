import java.util.Date;
import java.util.Random;

public abstract class NorbSampler{
	protected String fileName; //text file name
	protected int nClass; //number of classes
	protected int nObjxClass; //number of objects per class
	protected int nSeqxObj; //number of sequence for each object
	protected double prob[]; //probability for elevation, azimuth, lighting and to flip back
	protected int seqLen; //length of each sequence
	protected int seed; //seed for random generator
	protected boolean forwardDirection[]; // elevation, azimuth, lighting direction as forward or backward (false)
	protected Random rn;
	
	public NorbSampler(String fileName, int nClass, int nObjxClass, int nSeqxObj, double[] prob, int seqLen, int seed){
		this.fileName = fileName; // text file name
		this.nClass = nClass;	//number of classes
		this.nObjxClass = nObjxClass; //number of objects per class
		this.nSeqxObj = nSeqxObj;	//number of sequence for each object
		this.prob = prob; //probability for elevation, azimuth, lighting and to flip back
		this.seqLen = seqLen; //length of each sequence
		this.seed = seed;	//seed for random generator
		rn = new Random(seed);
		forwardDirection = new boolean[3];
		initializeDirection();
	}
	
	public NorbSampler(){
		prob = new double[4];
		forwardDirection = new boolean[3];
		rn = new Random(new Date().getTime());
		initializeDirection();
	}
	
	public int randInt(int min, int max) {
	    return rn.nextInt((max - min) + 1) + min;
	}
	
	private void initializeDirection(){
		for(int i=0; i<3; i++){
			if(rn.nextDouble() > 0.5)
				forwardDirection[i] = true;
			else
				forwardDirection[i] = false;
		}
			
	}
	
	public int moveElevation(int pastElev) {
		possiblyflip(0);
	    if(forwardDirection[0])
	    	if(pastElev < 8)
	    		return pastElev+1;
	    	else{
	    		forwardDirection[0] = !forwardDirection[0]; //bounce
	    		return pastElev-1;
	    	}
	    
	    else
	    	if(pastElev > 0)
	    		return pastElev-1;
	    	else{
	    		forwardDirection[0] = !forwardDirection[0]; //bounce
	    		return pastElev+1;
	    	}
	}
	
	public int moveAzimuth(int pastAzim) {
		possiblyflip(1);
	    if(forwardDirection[1])
	    	if(pastAzim < 34)
	    		return pastAzim+2;
	    	else{
	    		forwardDirection[1] = !forwardDirection[1]; //bounce
	    		return pastAzim-2;	
	    	}
	    
	    else
	    	if(pastAzim > 0)
	    		return pastAzim-2;
	    	else{
	    		forwardDirection[1] = !forwardDirection[1]; //bounce
	    		return pastAzim+2;
	    	}
	    
	}
	
	public int moveLighting(int pastLight) {
		possiblyflip(2);
	    if(forwardDirection[2])
	    	if(pastLight < 5)
	    		return pastLight+1;
	    	else{
	    		forwardDirection[2] = !forwardDirection[2]; //bounce
	    		return pastLight-1;
	    	}
	    
	    else
	    	if(pastLight > 0)
	    		return pastLight-1;
	    	else{
	    		forwardDirection[2] = !forwardDirection[2]; //bounce
	    		return pastLight+1;
	    	}
	}
	
	public void possiblyflip(int i) {
		if (rn.nextDouble() <= prob[3])
	    	forwardDirection[i] = !forwardDirection[i];
	}
}
