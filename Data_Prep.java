/* Author: Jessie Wright
 * 
 * Purpose: For preparing data from the MSLR LeToR data set for our teams specific needs (pairwise LeToR)
 * Data here: http://research.microsoft.com/en-us/projects/mslr/download.aspx
 * - Converts from given format to csv (including qid)
 * - Samples csv (with qid) so that for each query there are a max of n documents at each relevance rating
 * - Converts csv file (with qid) to pairwise difference file (csv no qid)
 * --------------------------------
 * Designed to be used as a [clumsy] command line tool.  Some invalid inputs will cause a crash.  Run with no options to see
 * usage information.
 * --------------------------------
 * If you want to use the final pairwise output as an .arff the header should be:
 * @attribute label {0,1,2,3,4}
 * @attribute attrN numeric  // for n 0 - 94
 * ...
 * @attribute attrN {-1,0,1} // for n 95-99
 * ...
 * @attribute attrN numeric // for n 100-135	
 * 
 * Create the header in mydata.arff then cat <thisprogramoutputfile> >> mydata.arff
 * then you should be able to use this format with weka etc
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
	
	public static void main(String[] args) throws Exception {
		
		System.out.println(args.length);
		for(String s: args){
			System.out.println(s);
		}
		
		
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
			RawMSLRToCSV(args[1], args[2]);
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

	// Processes MSLR10K or MSLR30K input file by stripping field id's ( 0 qid:1
	// 1:3 2:5.2 ... becomes 1,1,3,5.2, ...)
	private static void RawMSLRToCSV(String inputFilePath, String outputFilePath)
			throws Exception {
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
	
	/* counts records with each rating for a given csv (max/min/avg)
	 * 
	 */
	private static void csvRelevanceStats(String inputFilePath) throws Exception{
		
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
			currQID = currRecord.GetQueryId();
			if (currQID != prevQID) {
				int[] scoreCount = {0,0,0,0,0};
				for(Record r: records){
					scoreCount[r.GetRelevance()]++;
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
	
	/*
	 * 
	 */
	private static void sampleMSLRcsvForPairwise(int n, String inputFilePath, String outputFilePath) throws Exception{
		
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
			currQID = currRecord.GetQueryId();
			if (currQID != prevQID) {
				if(queryNum % 500 == 0) System.out.println("sampling query count " + queryNum); 
				int[] rateCount = {0,0,0,0,0};
				for(Record r: records){
					if(rateCount[r.GetRelevance()] < n){
						out.println(r.toString());
						rateCount[r.GetRelevance()]++;
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
	

	/*
	 * Takes csv of MSLR data, generates pairwise difference records for all
	 * records provided for each query in data. Omits 0 difference (i.e. same
	 * rating) and symmetric (i.e. only includes one of a-b and b-a) MAJOR
	 * increase in data size (~40x) so be mindful of input size used
	 */
	private static void generatePairwiseOutput(String inputFilePath,
			String outputFilePath) throws Exception {

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
			currQID = currRecord.GetQueryId();
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
						if (a.GetRelevance() != b.GetRelevance()) {
							// divide up between +1/-1 relevance rankings
							if (j % 2 == 0)
								out.println(a.Difference(b));
							else
								out.println(b.Difference(a));
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

}

/*
 * 1 qid:29992 1:2 2:1 3:1 4:0 5:2 6:1 7:0.50000 8:0.50000 9:0 10:1 11:1066 12:3
 * 13:11 14:6 15:1086 16:19.036284 17:28.261942 18:27.090498 19:29.47176
 * 20:19.017721 21:43 22:1 23:2 24:0 25:46 26:1 27:0 28:0 29:0 30:1 31:42 32:1
 * 33:2 34:0 35:45 36:21.50000 37:0.50000 38:1 39:0 40:23 41:420.25000
 * 42:0.25000 43:1 44:0 45:484 46:0.040338 47:0.333333 48:0.181818 49:0
 * 50:0.042357 51:0.000938 52:0 53:0 54:0 55:0.000921 56:0.03940 57:0.333333
 * 58:0.181818 59:0 60:0.041436 61:0.020169 62:0.166667 63:0.090909 64:0
 * 65:0.021179 66:0.00037 67:0.027778 68:0.008264 69:0 70:0.00041 71:390.814899
 * 72:12.674474 73:26.489059 74:0 75:417.257951 76:9.968513 77:0 78:0 79:0
 * 80:9.966807 81:380.846387 82:12.674474 83:26.489059 84:0 85:407.291145
 * 86:195.40745 87:6.337237 88:13.244529 89:0 90:208.628976 91:34387.599318
 * 92:40.160576 93:175.417555 94:0 95:39466.657481 96:1 97:0 98:0 99:0 100:1
 * 101:0.692028 102:0.632181 103:0.691352 104:0 105:0.69019 106:38.177038
 * 107:13.909696 108:17.007937 109:0 110:38.287432 111:-11.426342 112:-19.574856
 * 113:-18.558078 114:-21.74817 115:-11.393395 116:-12.318409 117:-24.517658
 * 118:-22.783662 119:-25.095158 120:-12.262422 121:-10.416378 122:-20.42216
 * 123:-19.977896 124:-22.59547 125:-10.384561 126:2 127:30 128:131 129:0
 * 130:13556 131:25675 132:2 133:12 134:0 135:0 136:0
 */