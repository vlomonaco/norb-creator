import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;


public class NorbCreator {
	
	public NorbConverter converter;
	public NorbSamplerTrain samplerTrain;
	public NorbSamplerTest samplerTest;
	public String configFileName;
	
	//convertion params
	public String matlabFile;
	public String destDir;
	public boolean convert;
	public int inputWidth;
	public int inputHeight;
	public int scaleFactor;
	
	//common params
	public String imagesRepo;
	public int nClass;
	public int nObjxClass;
	public double prob[] = new double[4];
	public int seqLen;
	
	//train params
	public boolean doTrain;
	public String trainFileName;
	public int nSeqxObjTrain;
	public int seedTrain;
	
	//test params
	public boolean doTest;
	public String testFileName;
	public int nSeqxObjTest;
	public int seedTest;
	public int minDistance;
	
	//seqExplorer
	public boolean seqExplorer;
	
	public NorbCreator() throws IOException {
    	convert = false;
    	doTrain = false;
    	doTest = false;
    	seqExplorer = false;
	}
	
	private void skipLines(BufferedReader reader, int n) throws IOException{
		for(int i=0; i<n; i++)
			reader.readLine();
	}
	
	public void readConfigFile() throws IOException{
		File r = new File(configFileName);
		BufferedReader reader = new BufferedReader(new FileReader(r));
		String[] parts;
		
		//Skipping header
		skipLines(reader, 7);
		
		parts = reader.readLine().split(" ");
		matlabFile = parts[1];
		
		parts = reader.readLine().split(" ");
		destDir = parts[1];
		
		parts = reader.readLine().split(" ");
		if(parts[1].equals("yes"))
			convert = true;
		else
			convert = false;
		
		parts = reader.readLine().split(" ");
		inputWidth = Integer.parseInt(parts[1]);

		parts = reader.readLine().split(" ");
		inputHeight = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		scaleFactor = Integer.parseInt(parts[1]);
		
		skipLines(reader, 5);
		
		parts = reader.readLine().split(" ");
		imagesRepo = parts[1];
		
		parts = reader.readLine().split(" ");
		nClass = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		nObjxClass = Integer.parseInt(parts[1]);
		
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
		
		skipLines(reader, 5);
		
		parts = reader.readLine().split(" ");
		trainFileName = parts[1];
		
		parts = reader.readLine().split(" ");
		nSeqxObjTrain = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		seedTrain = Integer.parseInt(parts[1]);
		
		skipLines(reader, 5);
		
		parts = reader.readLine().split(" ");
		testFileName = parts[1];
		
		parts = reader.readLine().split(" ");
		nSeqxObjTest = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		seedTest = Integer.parseInt(parts[1]);
		
		parts = reader.readLine().split(" ");
		minDistance = Integer.parseInt(parts[1]);
		
		reader.close();
	}
	
	public void createObjects() throws IOException{
    	if(convert){
    		converter = new NorbConverter(inputWidth, inputHeight, matlabFile, destDir, scaleFactor);
    	}
    	if(doTrain){
    		samplerTrain = new NorbSamplerTrain(trainFileName, nClass, nObjxClass, nSeqxObjTrain, prob, seqLen, seedTrain);
    	}
    	if(doTest){
    		samplerTest = new NorbSamplerTest(testFileName, trainFileName, nSeqxObjTest, minDistance, new boolean[5][10][9][18][6], seedTest);
    	}
    	if(seqExplorer){
    		setLookAndFeel();
            /* Create and display the form */
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new NorbSeqExplorer(imagesRepo).setVisible(true);
                }
            });
    	}
	}

	private void setLookAndFeel(){
      /* Set the Nimbus look and feel */
      //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
      /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
       * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
       */
      try {
          for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
              if ("Nimbus".equals(info.getName())) {
                  javax.swing.UIManager.setLookAndFeel(info.getClassName());
                  break;
              }
          }
      } catch (ClassNotFoundException ex) {
          java.util.logging.Logger.getLogger(NorbSeqExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      } catch (InstantiationException ex) {
          java.util.logging.Logger.getLogger(NorbSeqExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      } catch (IllegalAccessException ex) {
          java.util.logging.Logger.getLogger(NorbSeqExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      } catch (javax.swing.UnsupportedLookAndFeelException ex) {
          java.util.logging.Logger.getLogger(NorbSeqExplorer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
	}
	
	private void usageMsg(){
		System.out.println("[usage] NorbCreator [OPT] configFile");
		System.out.println("OPT: --convert");
		System.out.println("     --sampleTrain");
		System.out.println("     --sampleTest");
		System.out.println("     --seqExplorer");
	}
	
	public static void main(String[] args) throws IOException{
		NorbCreator creator = new NorbCreator();	
		try{
	        if(args[0].equals("--convert")){
	        	creator.configFileName = args[1];	
	        	creator.convert = true;
	        }
	        else if (args[0].equals("--sampleTrain")){
	        	creator.configFileName = args[1];	
	        	creator.doTrain = true;	       
	        }
	        else if (args[0].equals("--sampleTest")){
	        	creator.configFileName = args[1];	
	        	creator.doTest = true;
	        }
	        else if (args[0].equals( "--seqExplorer")){
	        	creator.configFileName = args[1];	
	        	creator.seqExplorer = true;
	        }
	        else{
	        	creator.configFileName = args[0];
	        	creator.doTrain = true;
	        	creator.doTest = true;
	        	creator.seqExplorer = true;
	        }
        }catch(Exception e){
        	//System.out.println(e);
        	creator.usageMsg();
        }	
    	creator.readConfigFile();
    	creator.createObjects();
	}

}
