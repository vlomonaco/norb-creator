import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Random;


public class KNNTestSeries {
	
	public static void main(String[] args) throws Exception {
		//number of test with different ninDistance
		int numTestsDist = 1;
		//number of test with different seqLen
		int numTestsLen = 5;
		//number of test to be averaged
		int numTestsAvg = 30;
		//where to save and load different conf files
		String repo = "../KNNTests/";
		//random generator
		Random rn = new Random(new Date().getTime());
		//writer
		File f = new File(repo+"knnAccuracyResult.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		//saved accuracy for the variance computation
		double[] accuracy = new double[numTestsAvg];
		
		NorbCreator creator = new NorbCreator();
		creator.configFileName = "config.txt";
		creator.doTrain = true;
		creator.doTest = true;
		creator.readConfigFile();
		
		long startTime = System.nanoTime(); //timing
		
		for(int i=0; i<numTestsDist; i++){
			creator.minDistance = (i+1)*2;
			for(int j=0; j<numTestsLen; j++){
				creator.seqLen = (j+1)*20;
				double sumAccuracy = 0;
				for(int k=0; k<numTestsAvg; k++){
					creator.seedTrain = rn.nextInt();
					creator.seedTest = rn.nextInt();
					creator.trainFileName = repo + "train_config_"+Integer.toString((i+1)*2)+"_"+Integer.toString((j+1)*20)+"_"+k+".txt";
					creator.testFileName = repo + "test_config_"+Integer.toString((i+1)*2)+"_"+Integer.toString((j+1)*20)+"_"+k+".txt";
					creator.createObjects();
					NorbKNN knn = new NorbKNN(32, 32,".."+File.separator+"Data"+File.separator+"all32"+File.separator+"L"+File.separator,
											  creator.trainFileName, creator.testFileName, false);
		
					accuracy[k] = knn.createDatasetAndTest();
					System.out.println("Accuracy: " + accuracy[k] + "%");
					sumAccuracy += accuracy[k];
				}
				double sumVariance=0;
				double average = sumAccuracy/numTestsAvg;
				for(int var=0; var<numTestsAvg; var++){
					sumVariance+= ((accuracy[var]-average)*(accuracy[var]-average));
				}
				double variance = (sumVariance/numTestsAvg);
					
				writer.write("Avg("+numTestsAvg+" runs) accuracy with minDist "+(i+1)*2+" and seqLen "+(j+1)*20+": " +(sumAccuracy/numTestsAvg)+"% with variance "+ Math.sqrt(variance)+"\n");
				writer.flush();
			}
		}
		writer.close();
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
		System.out.println("Computed in "+ duration/1000000000 + " sec" );
	}

}
