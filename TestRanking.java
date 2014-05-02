import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class TestRanking {

	/**
	 * 
	 * 
	 * 
	 * @param args  (path to MSLR.csv input file)
	 *  
	 */
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.out.println("Incorrect arguments supplied, supply filepath to input file");
			return;
		}
		
		try{
			BufferedReader in = new BufferedReader(new FileReader(args[0]));
			
			String line = in.readLine();
			int currQID, prevQID;
			//MSLRmahoutRF bb = new MahoutRF(args[1], args[2]);
			MSLRdummyModel model = new MSLRdummyModel();
			Record currRecord = new Record(line);
			currQID = currRecord.getQueryId();
			Query currQuery = new Query(currQID);
			currQuery.addRecord(currRecord);
			line = in.readLine();
			
			while(line != null){
				prevQID = currQID;
				currRecord = new Record(line);
				currQID = currRecord.getQueryId();
				if(currQID == prevQID) currQuery.addRecord(currRecord);
				else{
					currQuery.peformRanking(model);
					System.out.println(currQuery + ":");
					//currQuery.displayQueryDocumentRanking();
					double idealDCG = currQuery.getIdealDCG();
					double actualDCG = currQuery.getResultDCG();
					double nDCG = actualDCG/idealDCG;
					System.out.println("Ideal DCG: " + idealDCG + " Actual DCG: " + actualDCG + " " + "NDCG: " + nDCG);
					System.out.println("\n");
					currQuery = new Query(currQID);
					currQuery.addRecord(currRecord);
				}
				line = in.readLine();
			}
			in.close();
		}
		catch(FileNotFoundException e){
			System.out.println("File supplied \"" + args[0] +"\" not found" );
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		}
		

	}

}
