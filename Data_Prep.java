/* @Author: Jessie Wright
 * 
 * Purpose: For preparing data from the MSLR LeToR data set for our teams specific needs (pairwise LeToR)
 * Data here: http://research.microsoft.com/en-us/projects/mslr/download.aspx
 * 
 * - Converts from given format to csv (including qid)
 * - Samples csv (with qid) so that for each query there are a max of n documents at each relevance rating
 * - Converts csv file (with qid) to pairwise difference file (csv no qid)
 * 
 * --------------------------------
 * Designed to be used as a [clumsy] command line tool.  Some invalid inputs will cause a crash.  Run with no options to see
 * usage information.
 * --------------------------------
 * =======================
 * for use with weka/other .arff using libraries, see below
 * 
 * If you want to use the final pairwise output as an .arff the header should be:
 * 		@attribute label {0,1,2,3,4}
 * 		@attribute attrN numeric  // for N 0 - 94
 * 		...
 * 		@attribute attrN {-1,0,1} // for N 95-99
 * 		...
 * 		@attribute attrN numeric // for N 100-135	
 * 
 * Create the header in mydata.arff then cat <thisprogramoutputfile> >> mydata.arff
 * then you should be able to use this format with weka etc
 * ==========================
 */


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Data_Prep {


	private final static String USAGE = "Usage: <opt>(required) <input>(required) <output>(option dependent) <n> (option dependent) \n" +
										"Options:\n" +
										"1: Raw MSLR txt to csv ...  Requires: <input filepath> <output filepath>\n" +
										"2: Get Stats of CSV file ... Requires: <input filepath>\n" +
										"3: Sample CSV for Pairwise Preparation ... Requires: <input filepath> <output filepath> <n> \n" +
										"\t *n is the max number of documents per relevance rating for each query*\n" +
										"4: Prepare Pairwise Data from CSV ... Requires: <input filepath> <output filepath>\n\n";
	
	
	public static void main(String[] args) {
				
		if(args.length < 2 || args.length > 4){
			System.out.print(USAGE);
			return;
		}
		
		int option = Integer.parseInt(args[0]);
		
		switch (option) {
		case 1:
			if (args.length != 3) {
				System.out.print(USAGE);
				return;
			}
			rawMSLRToCSV(args[1], args[2]);
			break;
		case 2:
			if (args.length != 2) {
				System.out.print(USAGE);
				return;
			}
			csvRelevanceStats(args[1]);
			break;
		case 3:
			if (args.length != 4) {
				System.out.print(USAGE);
				return;
			}
			sampleMSLRcsvForPairwise(Integer.parseInt(args[3]), args[1],
					args[2]);
			break;
		
		case 4:
			if (args.length != 3) {
				System.out.print(USAGE);
				return;
			}
			generatePairwiseOutput(args[1], args[2]);
		}
	}
/**
 *		 Processes MSLR10K or MSLR30K input file by stripping field id's ( 0 qid:1
 *		1:3 2:5.2 ... becomes 1,1,3,5.2, ...) 
 * @param inputFilePath existing MSLR10K/30K .txt input file
 * @param outputFilePath destination for csv output
 * 
 */

	private static void rawMSLRToCSV(String inputFilePath, String outputFilePath) {
		
		try{
			BufferedReader in = new BufferedReader(new FileReader(inputFilePath));
			PrintWriter out = new PrintWriter(outputFilePath);
	
			// strip labels, turn to csv
			String line = in.readLine();
			int recordNum = 0;
			while (line != null) {
				String outputLine = "";
				String[] split = line.split(" ");
				outputLine += split[0]; // rating
				for (int i = 1; i < split.length; i++) {
					String[] attr = split[i].split(":");
					outputLine += "," + attr[1];
				}
				out.println(outputLine);
				if (recordNum % 10000 == 0)
					System.out.println("Lines Processed: " + recordNum);
				recordNum++;
				line = in.readLine();
			}
			out.flush();
			in.close();
			out.close();
			System.out.println("csv done");
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	
	/**
	 *  counts records with each rating for a given MSLR csv 
	 *  outputs stats(max/min/avg) to System.out
	 * @param inputFilePath
	 * 
	 */
	private static void csvRelevanceStats(String inputFilePath){
		try{
			BufferedReader in = new BufferedReader(new FileReader(inputFilePath));
			
			System.out.println("Calculating relevance counts.....");
			
			String line = in.readLine();
			int currQID = 0, prevQID;
			Record currRecord;
			ArrayList<Record> records = new ArrayList<Record>();
			int numQueries = 0;
			int[][] ratingCount = new int[20000][];
			while(line != null){
				prevQID = currQID;
				currRecord = new Record(line);
				currQID = currRecord.getQueryId();
				if (currQID != prevQID) {
					int[] scoreCount = {0,0,0,0,0};
					for(Record r: records){
						scoreCount[r.getRelevance()]++;
					}
					ratingCount[numQueries] = scoreCount;
					records.clear();
					numQueries++;
				}
				records.add(currRecord);
				line = in.readLine();	
			}
			System.out.println("File contains " + numQueries + " queries");
			// calculate min/max and average for each rating
			for(int rating = 0; rating < 5; rating++){
				int min = 1000,  max = 0;
				int total = 0;
				for(int query = 0; query < numQueries ; query++){
					total += ratingCount[query][rating];
					if(ratingCount[query][rating] > max) max = ratingCount[query][rating];
					if(ratingCount[query][rating] < min) min = ratingCount[query][rating];
				}
				System.out.println("Rating " + rating + ", min: " + min + ", max: " + max + ", total: " + total +" avg: " + total/numQueries);
				
			}
			in.close();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * For all queries in inputfile, sample file will contain up to n records at each relevance
	 * rating {0,1,2,3,4}  if, fewer than n records of rating exist, all records of rating will
	 * be present in sample
	 * 
	 * @param n  max num records of each relevance for each query in sample
	 * @param inputFilePath mslr csv file (not raw!)
	 * @param outputFilePath 
	 */
	  
	 
	private static void sampleMSLRcsvForPairwise(int n, String inputFilePath, String outputFilePath){
		try{
			BufferedReader in = new BufferedReader(new FileReader(inputFilePath));
			PrintWriter out = new PrintWriter(outputFilePath);
			System.out.println("Begin Sampling....");
			
			String line = in.readLine();
			int currQID = 0, prevQID;
			Record currRecord;
			ArrayList<Record> records = new ArrayList<Record>();
			int queryNum = 1;
			
			while(line != null){
				prevQID = currQID;
				currRecord = new Record(line);
				currQID = currRecord.getQueryId();
				if (currQID != prevQID) {
					if(queryNum % 500 == 0) System.out.println("sampling query count " + queryNum); 
					int[] rateCount = {0,0,0,0,0};
					for(Record r: records){
						if(rateCount[r.getRelevance()] < n){
							out.println(r.toString());
							rateCount[r.getRelevance()]++;
						}
					}
					records.clear();
					queryNum++;
				}
				records.add(currRecord);
				line = in.readLine();
			}
			out.flush();
			in.close();
			out.close();
			System.out.println("Sampling done");
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	

	/**
	 * Takes csv of MSLR data, generates pairwise difference records for all
	 * records provided for each query in data. Omits 0 difference (i.e. same
	 * rating) and symmetric (i.e. only includes one of a-b and b-a) MAJOR
	 * increase in data size (~40x) so be mindful of input size used
	 * 
	 * output to be used for training binary classifier to distinguish winning/losing pairs
	 * 
	 * @param inputFilePath mslr csv data
	 * @param outputFilePath pairwise difference data

	 */
	private static void generatePairwiseOutput(String inputFilePath, String outputFilePath){
		try{
			BufferedReader in = new BufferedReader(new FileReader(inputFilePath));
			PrintWriter out = new PrintWriter(outputFilePath);
			
			System.out.println("Processing Pairwise Output, this can take some time, as message " +
					"will be output for every 100 queries processed");
			
			String line = in.readLine();
			int currQID = 0, prevQID;
			Record currRecord;
			ArrayList<Record> records = new ArrayList<Record>();
			int numQueries = 0;
			while (line != null) {
				prevQID = currQID;
				currRecord = new Record(line);
				currQID = currRecord.getQueryId();
				if (currQID != prevQID) {
					numQueries++;
					if (numQueries % 100 == 0)
						System.out.println("Processing query count: " + numQueries);
					int numRecords = records.size();
					Record a, b;
					for (int i = 0; i < numRecords; i++) {
						a = records.get(i);
						for (int j = i + 1; j < numRecords; j++) {
							b = records.get(j);
							// skip records with same relevance rating
							if (a.getRelevance() != b.getRelevance()) {
								// divide up between +1/-1 relevance rankings
								if (j % 2 == 0)
									out.println(a.difference(b));
								else
									out.println(b.difference(a));
							}
						}
	
					}
					// reset array list for next record
					records.clear();
				}
				records.add(currRecord);
				line = in.readLine();
			}
	
			out.flush();
			in.close();
			out.close();
			System.out.println("pairwise output done");
	
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}

