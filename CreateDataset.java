import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;


public class CreateDataset {
	
	private String trainFileName; //file from which to read
	private String testFileName; //file from which to read
	private String validFileName; //file from which to read
	private String destTrainDir;
	private String destTestDir;
	private String destValidDir;
	private String imagesPool;
	private int numSeqxTrain; //to consider: They need to be less or equal to the number of seq present.
	private int numSeqxTest;
	private int numImgxSeqValid;
	
	public CreateDataset(String trainFileName, int numSeqxTrain, String testFileName, int numSeqxTest, 
			String validFileName,int numImgxSeqValid, String destTrainDir,
			String destTestDir, String destValidDir, String imagesPool) throws IOException{
		this.trainFileName = trainFileName;
		this.numSeqxTrain = numSeqxTrain;
		this.testFileName = testFileName;
		this.numSeqxTest = numSeqxTest;
		this.validFileName = validFileName;
		this.numImgxSeqValid = numImgxSeqValid;
		this.destTrainDir = destTrainDir;
		this.destTestDir = destTestDir;
		this.destValidDir = destValidDir;
		this.imagesPool = imagesPool;
		System.out.println("[Train]");
		convert(trainFileName, destTrainDir, numSeqxTrain, 20);
		System.out.println("\n[Test]");
		convert(testFileName, destTestDir, numSeqxTest, 20);
		System.out.println("\n[Valid]");
		convert(validFileName, destValidDir, 10, numImgxSeqValid);
		System.out.println("\n");
	}
	
	public CreateDataset() throws IOException{
		this(
			"/home/vincenzo/MEGA/Università/Magistrale/Thesis/mytest/TheanoTests/train_conf.txt", //name of the test config 
			2,
			"/home/vincenzo/MEGA/Università/Magistrale/Thesis/mytest/TheanoTests/test_conf_1.txt", //name of the train config 
			10,
			"/home/vincenzo/MEGA/Università/Magistrale/Thesis/mytest/TheanoTests/test_conf_1.txt",
			5,
			"/home/vincenzo/MEGA/Università/Magistrale/Thesis/prova/train/L",
			"/home/vincenzo/MEGA/Università/Magistrale/Thesis/prova/test/L",
			"/home/vincenzo/MEGA/Università/Magistrale/Thesis/prova/valid/L",
			"/home/vincenzo/all32/L"
			);
	}
	
	public void convert(String fileName, String dest, int numSeq, int maxNumImg) throws IOException{
		File r = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(r));
		String line, clas="", seq, nClas;
		String[] parts;
		boolean convert = false;
		int tot=0, counter = 0, skipped = 0, img_in_seq = 0;
		while ((line = reader.readLine()) != null){
			//System.out.println(line);
			if(line.startsWith("nClass")){
				//we create different directory for each class
				parts = line.split(" ");
				nClas = parts[1];
				//System.out.println("nClas updated: "+nClas);
				File destF = new File(dest);
				destF.mkdirs();
				
				//create subfolder
				for(int i=0; i<Integer.parseInt(nClas); i++){
					File sub_destF = new File(dest+"/"+i +"/");
					sub_destF.mkdirs();
				}
			}
			else if(line.startsWith("Class")){
				//we save the class for future use
				parts = line.split("    ");
				clas = parts[1];
				//System.out.println("Clas updated: "+clas);
			}
			else if(line.startsWith("Sequence")){
				//we switch the conversion phase accordingly
				parts = line.split(" ");
				seq = parts[1];
				//System.out.println("Seq: "+seq);
				if (Integer.parseInt(seq) < numSeq)
					convert = true;
				else
					convert = false;
				img_in_seq=0;
			}
			else if(line.endsWith("bmp") && convert && img_in_seq < maxNumImg){
				try{
					Files.copy(new File(imagesPool+"/"+clas+"/"+line).toPath(),
							new File(dest+"/"+clas+"/"+line).toPath());
				}
				catch(java.nio.file.FileAlreadyExistsException e){
					skipped++;
				}
				img_in_seq++;
				tot++;
				counter=tot-skipped;
				System.out.print("\r\t Images [processed: "+tot + " | copied "+counter+
						" | skipped (already copied): "+ skipped + "]");
			}
		}
	}
	
	public static void main(String[] args) throws IOException {

		CreateDataset converter = new CreateDataset();

	}

}
