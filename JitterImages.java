import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;


public class JitterImages {
	
	static int width = 32;
	static int height = 32;
	static String source = "../data/sampled_norb/train100/32/L/";
	static String dest = "../data/sampled_norb/x32_100_only_jittered/L/";
	
	public static byte getBackground(byte[] data){
    	int currentBackground = Integer.MAX_VALUE;
        for (int j = 0; j < width; j++)
        	if ((data[j] & 0xFF) < currentBackground) currentBackground = data[j] & 0xFF;
        return (byte) currentBackground;
	}
	
	public static void Saccade(byte[] source, byte[] target, int saccadePos, boolean crossFirst)
    {
        int[] crossFirstOrder = { 1, 5, 3, 7, 4, 8, 2, 6 };
        int dx = 0, dy = 0;

        switch (crossFirst ? crossFirstOrder[saccadePos - 1] : saccadePos)
        {
            //  4  3  2
            //  5  *  1
            //  6  7  8
            case 1: dx = 1; dy = 0; break;
            case 2: dx = 1; dy = 1; break;
            case 3: dx = 0; dy = 1; break;
            case 4: dx = -1; dy = 1; break;
            case 5: dx = -1; dy = 0; break;
            case 6: dx = -1; dy = -1; break;
            case 7: dx = 0; dy = -1; break;
            case 8: dx = 1; dy = -1; break;
        }

        int widthToCopy = width;
        int heightToCopy = height;
        int sourceOffset = 0;
        int targetOffset = 0;

        if (dx == 1)
        {
            widthToCopy--;
            targetOffset++;
        }
        else if (dx == -1)
        {
            widthToCopy--;
            sourceOffset++;
        }

        if (dy == 1)
        {
            heightToCopy--;
            sourceOffset += width;
        }
        else if (dy == -1)
        {
            heightToCopy--;
            targetOffset += width;
        }

        //Clear the target (add background uniform background)
        Arrays.fill(target, getBackground(source));

        for (int h = 0; h < heightToCopy; h++){
        	for (int w = 0; w < widthToCopy; w++)
        		target[targetOffset++] = source[sourceOffset++];
            targetOffset += (width - widthToCopy);
            sourceOffset += (width - widthToCopy);
        }
        
    }
	
	public static void main(String[] args) throws IOException {
		
		//create dest folder if doesn't exist
		File destF = new File(dest);
		destF.mkdirs();
		
		//create subfolder
		for(int clas=0; clas<5; clas++){
			File sub_destF = new File(dest+"/"+clas +"/");
			sub_destF.mkdirs();
		}
			
		//for each sub-directory
		for(int clas=0; clas<5; clas++){
			
			String working_dir = source + Integer.toString(clas)+ "/";
		    File directory = new File(working_dir);
		    // get all the files from a directory
		    File[] fList = directory.listFiles();
		    for (File file : fList) {
				byte [] in_img = new byte[width*height];
				byte [] out_img = new byte[width*height];
				
				BufferedImage image = ImageIO.read(file);
				Raster raster = image.getData();
				for (int i = 0; i < width; i++) {
				    for (int j = 0; j < height; j++) {
				        in_img[i + j*width] = (byte)raster.getSample(i, j, 0);
				    }
				}

				for(int direction = 1; direction < 9; direction++){
					Saccade(in_img, out_img, direction, false);
				
					BufferedImage image2w = new BufferedImage(width, height,BufferedImage.TYPE_BYTE_GRAY);
					image2w.getRaster().setDataElements(0, 0, width, height, out_img);
					String N = file.getName();
					ImageIO.write(image2w, "BMP", new File(dest + "/" + clas +"/" + 
															N.substring(0, N.length()-4) + "j" + direction + ".bmp"));	
				}
				
				/*write the original image too
				BufferedImage image2w = new BufferedImage(width, height,BufferedImage.TYPE_BYTE_GRAY);
				image2w.getRaster().setDataElements(0, 0, width, height, in_img);
				ImageIO.write(image2w, "BMP", new File(dest + "/" + clas +"/" + file.getName()));*/
				
				System.out.println("Img '"+file.getName()+"' jittered...");
				
		    }
		}
	    
	}

}
