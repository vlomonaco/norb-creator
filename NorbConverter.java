import java.io.DataInputStream;
import java.io.FileInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

class NorbConverter{
		
	//data
    private int inputWidth;
    private int inputHeight;
    private String dataset;
    private String targetDir;
    private int scaleFactor;
    private int outputWidth;
    private int outputHeight;
    
    public NorbConverter(int inputWidth, int inputHeight, String dataset, String targetDir, int scaleFactor){
    	this.inputWidth = inputWidth;
    	this.inputHeight = inputHeight;
    	this.dataset = dataset;
    	this.targetDir = targetDir;
    	this.scaleFactor = scaleFactor;
        outputWidth = inputWidth / scaleFactor;
        outputHeight = inputHeight / scaleFactor;
        extractAndScaleImages();
        
    }
        
    public NorbConverter(){
        /*static String dataset = ".."+File.separator+"Data"+File.separator+"Matlab"+File.separator+
    			"smallnorb-5x01235x9x18x6x2x96x96-testing-";*/
    	//default values
    	this(	96,
    			96, 
    			".."+File.separator+"Data"+File.separator+"Matlab"+File.separator+"smallnorb-5x46789x9x18x6x2x96x96-training-",
    			".."+File.separator+"Data"+File.separator+"all32",
    			3
    		);
	}

	private int conv(int i) {
		//return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
		return i>>24;
	}

    private boolean skipPattern(int instance, int elevation, int azimuth, int lighting){
            // if (lighting!=0) return true;
            // if (elevation != 0) return true;
            return false;
    }
	
	private void headerProcessing(DataInputStream f, String type)
	{
		int dataType, ndim, count, size;
		try {
		    	dataType = conv(f.readInt()); 
		    	//System.out.println("dataType: "+ dataType);
		        ndim = conv(f.readInt());
		        //System.out.println("ndim: "+ ndim);
		        count = conv(f.readInt()); 
		        size = conv(f.readInt()); 
		        size = conv(f.readInt());  
		        if(type.equals("dat")){
		        	size = conv(f.readInt());  
		        //System.out.println("dat passed");
			}
			//System.exit(-1);
		} catch (Exception e) { /* do nothing */ }
	}

    private byte[] downscaleFrameResolution(byte[] frameBuffer){
		int[] tempBuffer = new int[inputWidth * inputHeight];
		byte[] image = new byte[outputWidth * outputHeight];
            
		int targetOffset = 0;
		int sourceOffest = 0;
		
		// first pass (average over rows)
		for (int i = 0; i < inputHeight; i++){
			for (int j = 0; j < outputWidth; j++){
				int val = 0;
				for (int k = 0; k < scaleFactor; k++)
					val += frameBuffer[sourceOffest++]& 0xFF;
				tempBuffer[targetOffset] = val;
				targetOffset++;
			}
		}
		
		// Second pass (average over columns) su Frame stesso
		targetOffset = 0;
		sourceOffest = 0;
		for (int j = 0; j < outputWidth; j++){
			sourceOffest = targetOffset = j;
		    for (int i = 0; i < outputHeight; i++){
				int val = 0;
				for (int k = 0; k < scaleFactor; k++){
					val += tempBuffer[sourceOffest];
					sourceOffest += outputWidth;
				}
				//System.out.println("original: "+(double)val / (double)(scaleFactor * scaleFactor));
				//System.out.println("rounded: "+ (byte)(Math.round((double)val / (double)(scaleFactor * scaleFactor))));
				//System.out.println("rounded (no byte): "+ Math.round((double)val / (double)(scaleFactor * scaleFactor)));
				//System.exit(-1);
				image[targetOffset] = (byte)(Math.round((double)val / (double)(scaleFactor * scaleFactor)));
				targetOffset += outputWidth;
			}
		}
		return image;
    }
    
    public void extractAndScaleImages(){
		try{
			byte[] buffer = new byte[inputWidth * inputHeight];
			int patternCount = 24300;
			DataInputStream datFile = new DataInputStream(new FileInputStream(dataset + "dat.mat"));
			DataInputStream catFile = new DataInputStream(new FileInputStream(dataset + "cat.mat"));
			DataInputStream infoFile = new DataInputStream(new FileInputStream(dataset + "info.mat"));
			  
			// Header processing
			headerProcessing(datFile, "dat");
			headerProcessing(catFile, "cat");
			headerProcessing(infoFile, "info");
			//System.out.println("HeaderProcessing finished..");

			byte[] I;
			int[] classCounters = new int[5];
			for(int i = 0; i<5; i++)	
				classCounters[i] = 0;
			String dirName;
			String fileName;
			int patternCreated = 0;
            
			// Read patterns
			for (int p = 0; p < patternCount; p++)
			{
				int patternClass = conv(catFile.readInt()); 
				// 0 to 9
				int instance = conv(infoFile.readInt());   
				// 0 to 8, which mean cameras are 30, 35,40,45,50,
				//55,60,65,70 degrees from the horizontal 
				int elevation = conv(infoFile.readInt());   
				// 0,2,4,...,34, multiply by 10 to get 
				//the azimuth in degrees
				int azimuth = conv(infoFile.readInt());     
				// 0 to 5
				int lighting = conv(infoFile.readInt());    
				fileName = String.format("%02d_%02d_%02d_%02d.bmp",instance, elevation, azimuth, lighting);
				//System.out.println(instance+ " "+ elevation +" "+azimuth+ " "+lighting +" "+patternClass +" "+ fileName);

				// LEFT image
				dirName = String.format(targetDir+"/L/%d", patternClass);
				File theDir = new File(dirName);
				theDir.mkdirs();
				datFile.read(buffer);
				if (!skipPattern(instance, elevation, azimuth, lighting))
				{
					//System.out.println("Before downscale..");
					I = downscaleFrameResolution(buffer);
					//System.out.println("After downscale..");
					BufferedImage image = new BufferedImage(outputWidth, outputHeight,BufferedImage.TYPE_BYTE_GRAY); 
					//System.out.println("After bufferedImage..");
					image.getRaster().setDataElements(0, 0, outputWidth, outputHeight, I);
					ImageIO.write(image, "BMP", new File(dirName + "/" + fileName));
					patternCreated++;
				}

				// RIGHT image
				dirName = String.format(targetDir+"/R/%d", patternClass);
				theDir = new File(dirName);
				theDir.mkdirs();

				datFile.read(buffer);
				if (!skipPattern(instance, elevation, azimuth, lighting))
				{
					I = downscaleFrameResolution(buffer);
					BufferedImage image = new BufferedImage(outputWidth, outputHeight,BufferedImage.TYPE_BYTE_GRAY);
					image.getRaster().setDataElements(0, 0, outputWidth, outputHeight, I); 
					ImageIO.write(image, "BMP", new File(dirName + "/" + fileName));
				}
				//System.out.println("pattern class: "+patternClass);
				classCounters[patternClass]++;
				//System.out.println("here..");
				System.out.print(String.format("\r%dx2 "+ "pattern created", patternCreated));
			}

			datFile.close();
			catFile.close();
			infoFile.close();
		} catch (Exception e) { System.out.println(e); }  
    }
    
    public static void main(String args[]){
    	NorbConverter converter = new NorbConverter();
    }
}