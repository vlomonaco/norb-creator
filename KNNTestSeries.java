
public class KNNTestSeries {
	
	public static void main(String[] args) throws Exception {
		NorbKNN knn = new NorbKNN();
		double accuracy = knn.createDataseAndTest();
		System.out.println("Accuracy: " + accuracy + "%");
	}

}
