import java.util.Comparator;

/**
 * For Learning to Rank Machine Learning Project, SJSU 185C/267
 * 
 * Record represents features of document with respect to query
 * Relevant for MSLR (Microsoft Learning to Rank) dataset
 * 
 * Records are comparable by their rankScore
 * 
 * 
 *
 * @author Jessie Wright
 *
 */



public class Record{
		
		private int relevance;  //objective score from file
		private int queryId;
		private String csvInput; 
		private double[] features;
		private int rankScore; // rank assigned by Query performRanking
		
		
		
		public Record(String csvInputLine){
			String[] fields = csvInputLine.split(",");
			relevance = Integer.parseInt(fields[0]);
			queryId = Integer.parseInt(fields[1]);
			csvInput = csvInputLine;
			features = new double[136];
			for(int i = 0; i < 136; i++){
				features[i] = Double.parseDouble(fields[i+2]);
			}
			rankScore = -99999999;
		}
		
		public void SetRankScore(int rankScore){
			this.rankScore = rankScore;
		}
		
		public int GetRankScore(){
			return this.rankScore;
		}
		
		public void SetRelevance(int newRelevance){
			relevance = newRelevance;
		}
		
		public int GetRelevance(){
			return relevance;
		}
		
		public void SetQueryID(int newQID){
			queryId = newQID;
		}
		
		public int GetQueryId(){
			return queryId;
		}
		
		public double GetFeatureVal(int i){
			if(i >= 0 && i < features.length) return features[i];
			else{
				System.out.println("I should really learn about exception: out of bounds in GetFeatureVal call, i = " + i);
				return 0;
			}
		}
		
		public void SetFeatureVal(int i, double val){
			if(i > 0 && i < features.length)  features[i] = val;
			else{
				System.out.println("I should really learn about exception: out of bounds in SetFeatureVal call");
			}
		}
		// a.Difference(b) -> a - b for printing
		// doesn't output QID
		public String Difference(Record b){
			String toReturn = "";
			toReturn += this.relevance - b.GetRelevance() > 0 ? 1 : -1;
			for(int i = 0; i < features.length; i++){
				
				toReturn += ",";
				//boolean values 95-99 produce categorical {-1, 0 , 1}  cast to int so they are enum for H20
				if(i > 94 && i < 100) toReturn += (int) this.features[i] - b.GetFeatureVal(i);
				else toReturn += this.features[i] - b.GetFeatureVal(i);
			}
			
			return toReturn;
		}
		
		public String toString(){
			return csvInput;
		}

		
		Comparator<Record> getRankScoreComparator(){
			return new Comparator<Record>(){
				public int compare(Record r1, Record r2){
					return r2.GetRankScore() - r1.GetRankScore();
				}
			};
		}
		
		Comparator<Record> getRelevanceComparator(){
			return new Comparator<Record>(){
				public int compare(Record r1, Record r2){
					return r2.GetRelevance() - r1.GetRelevance();
				}
			};
		}

		
		
	
	}
