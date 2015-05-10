import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import weka.core.Instance;


public class SeqVerifier {
	
	private static void skipLines(BufferedReader reader, int n) throws IOException{
		for(int i=0; i<n; i++)
			reader.readLine();
	}
	
	public static boolean verifySequentiality(String seq[]) {
		String[] parts;
		int prevStep[] = new int[4];
		for(int i=0; i<seq.length;i++){
			String line = seq[i].substring(0, seq[i].length()-4);
			parts = line.split("_");
				
			if (i != 0){
				int a = Math.abs(Integer.parseInt(parts[0]) - prevStep[0]);
				int b = Math.abs(Integer.parseInt(parts[1]) - prevStep[1]);
				int c = Math.abs(Integer.parseInt(parts[2]) - prevStep[2]);
				int d = Math.abs(Integer.parseInt(parts[3]) - prevStep[3]);
				int changed = 0;
				
				if(a==1)
					changed++;
				if(b==1)
					changed++;
				if(c==2)
					changed++;
				if(d==1)
					changed++;
				
				if( a>1 || b>1 || c>2 || d>1 || changed > 1){
					System.out.println("[SEQUENTIALITY ERROR] at img: " + seq[i] + ", num: "+ i);
					return false;
				}
			}
			
			prevStep[0] = Integer.parseInt(parts[0]);
			prevStep[1] = Integer.parseInt(parts[1]);
			prevStep[2] = Integer.parseInt(parts[2]);
			prevStep[3] = Integer.parseInt(parts[3]);
		}

		return true;
	}
	
	private static int computeDistance(String img1, String img2){
		String parts[], line;
		int img1Val[] = new int[4];
		int img2Val[] = new int[4];
		
		line = img1.substring(0, img1.length()-4);
		parts = line.split("_");
		img1Val[0] = Integer.parseInt(parts[0]);
		img1Val[1] = Integer.parseInt(parts[1]);
		img1Val[2] = Integer.parseInt(parts[2]);
		img1Val[3] = Integer.parseInt(parts[3]);
		
		line = img2.substring(0, img2.length()-4);
		parts = line.split("_");
		img2Val[0] = Integer.parseInt(parts[0]);
		img2Val[1] = Integer.parseInt(parts[1]);
		img2Val[2] = Integer.parseInt(parts[2]);
		img2Val[3] = Integer.parseInt(parts[3]);
		
		int a = Math.abs(img1Val[0] - img2Val[0]);
		int b = Math.abs(img1Val[1] - img2Val[1]);
		int c = Math.abs(img1Val[2] - img2Val[2]);
		int d = Math.abs(img1Val[3] - img2Val[3]);
		int dist = a + b + c/2 + d;
	
		return dist;
	}
	
	public static boolean verifyDistance(String train[], String test[], int minDist){
		for(int i=0; i<test.length; i++){
			for(int j=0; j<train.length; j++){
				int dist = computeDistance(test[i], train[j]);
				if(dist < minDist){
					System.out.println("[DISTANCE ERROR] at images: " + test[i] + ", "+ train[j]+ " with dist: "+ dist);
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) throws IOException {
		String trainFile = "train_conf.txt";
		String testFile = "test_conf.txt";
		File train = new File(trainFile);
		File test = new File(testFile);
		BufferedReader reader_train = new BufferedReader(new FileReader(train));
		BufferedReader reader_test = new BufferedReader(new FileReader(test));

		String[] parts;
		
		//reading train param
		skipLines(reader_train, 4);
		parts = reader_train.readLine().split(" ");
		int nSeqxObjTrain = Integer.parseInt(parts[1]);
		skipLines(reader_train, 7);
		
		//reading test param
		skipLines(reader_test, 2);
		
		parts = reader_test.readLine().split(" ");
		int nClass = Integer.parseInt(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		int nObjxClass = Integer.parseInt(parts[1]);
		
		parts = reader_test.readLine().split(" ");
		int nSeqxObjTest = Integer.parseInt(parts[1]);
		
		skipLines(reader_test, 4);
		
		parts = reader_test.readLine().split(" ");
		int seqLen = Integer.parseInt(parts[1]);
		
		skipLines(reader_test, 1);
		
		parts = reader_test.readLine().split(" ");
		int minDist = Integer.parseInt(parts[1]);
		
		//discard dash line
		reader_test.readLine(); 

		int nSeqObjxTrain;
		String trainSeq[][][][] = new String[nClass][nObjxClass][nSeqxObjTrain][seqLen];
		String testSeq[][][][] = new String[nClass][nObjxClass][nSeqxObjTest][seqLen];
		
		//System.out.println(nClass + " " + nObjxClass + " " + nSeqxObjTrain + " " +nSeqxObjTrain + " " +seqLen);
		
		//reading train file image
		for(int clas=0; clas<nClass; clas++)
			for(int obj=0; obj<nObjxClass; obj++)
				for(int seq=0; seq<nSeqxObjTrain; seq++){
					//discard header line
					skipLines(reader_train, 6);  
					for(int i=0; i<seqLen; i++)
						trainSeq[clas][obj][seq][i]  = reader_train.readLine();
				}	
		
		//reading test file image
		for(int clas=0; clas<nClass; clas++)
			for(int obj=0; obj<nObjxClass; obj++)
				for(int seq=0; seq<nSeqxObjTest; seq++){
					//discard header line
					skipLines(reader_test, 6); 
					for(int i=0; i<seqLen; i++)
						testSeq[clas][obj][seq][i]  = reader_test.readLine();
				}	
		
/*		String seq1[] = new String[4];
		String seq2[] = new String[4];
		seq1[0] = "00_01_30_00.bmp";
		seq1[1] = "00_01_30_01.bmp";
		seq1[2] = "00_01_30_02.bmp";
		seq1[3] = "00_02_30_02.bmp";
		
		seq2[0] = "00_05_20_04.bmp";
		seq2[1] = "00_05_18_04.bmp";
		seq2[2] = "00_06_18_04.bmp";
		seq2[3] = "00_07_18_04.bmp";*/
		
		
//		System.out.println("first train seq:");
//		for(int i=0; i< trainSeq[0][0][0].length; i++)
//			System.out.println(trainSeq[0][0][0][i]);
//		
//		System.out.println("\nfirst test seq:");
//		for(int i=0; i< testSeq[0][0][0].length; i++)
//			System.out.println(testSeq[0][0][0][i]);
		
		System.out.println("Starting verification procedures...");
		boolean ok = true;
		int numTests = 0;
		for(int clas=0; clas<nClass; clas++)
			for(int obj=0; obj<nObjxClass; obj++){
				for(int seqTr=0; seqTr<nSeqxObjTrain; seqTr++){
					if(!verifySequentiality(trainSeq[clas][obj][seqTr])){
						ok=false;
						System.out.println("in [class: "+clas+", obj: "+obj+", nSeq: "+seqTr+"] train_conf.txt");
					}
					numTests++;
				}
						
				for(int seqTe=0; seqTe<nSeqxObjTest; seqTe++){
					if(!verifySequentiality(testSeq[clas][obj][seqTe])){
						ok=false;
						System.out.println("in [class: "+clas+", obj: "+obj+", nSeq: "+seqTe+"] "+ "test_conf.txt");
					}
					numTests++;
				}
				
				for(int seqTr=0; seqTr<nSeqxObjTrain; seqTr++){
					for(int seqTe=0; seqTe<nSeqxObjTest; seqTe++){
						if(!verifyDistance(trainSeq[clas][obj][seqTr], testSeq[clas][obj][seqTe], minDist)){
							ok=false;
							System.out.println("in Train[class: "+clas+", obj: "+obj+", nSeq: "+seqTe+"] and " + 
												  "Test[class: "+clas+", obj: "+obj+", nSeq: "+seqTe+"]");
						}	
					}
					numTests++;
				}
				System.out.print("\r" + numTests + " tests done.");
			}
	
		if(ok) 
			System.out.println("\nTests completed. Everything seems ok.");
					
	}

}
