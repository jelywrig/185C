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
		private double rankScore; // rank assigned by Query performRanking
		
		
		/**
		 *  use file converter from DataPrep to prepare a valid record
		 *  MSLR dataset
		 *  relevance, queryid, feat1, feat2, ...., feat136
		 * @param csvInputLine represents MSLR record
		 */
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
		/**
		 *  rankScore is only relevant in a  collection of records, can be calculated
		 *  within a Query by performRanking()
		 * @param rankScore
		 */
		public void setRankScore(double rankScore){
			this.rankScore = rankScore;
		}
		
		/**
		 * 
		 * @return generated rankScore, -999999 if not ranked
		 */
		public double getRankScore(){
			return this.rankScore;
		}
		/**
		 * 
		 * @param newRelevance
		 */
		
		public void setRelevance(int newRelevance){
			relevance = newRelevance;
		}
		
		/**
		 * 
		 * @return relevance
		 */
		public int getRelevance(){
			return relevance;
		}
		
		/**
		 * 
		 * @param newQID
		 */
		public void setQueryId(int newQID){
			queryId = newQID;
		}
		
		/**
		 * 
		 * @return queryId
		 */
		public int getQueryId(){
			return queryId;
		}
		
		/**
		 * 
		 * @param i  feature index (0-135)
		 * @return value of feature (if invalid index, 0)
		 */
		public double getFeatureVal(int i){
			
			try{
				return features[i];
			}
			catch(ArrayIndexOutOfBoundsException e){
				System.out.println("Tried to access invalid feature num " + i + ", valid: 0-135\nZero returned");
				return 0;
			}
			
		}
		
		/**
		 * 
		 * @param i index of feature to be set (0-135 valid)
		 * @param val value to be applied
		 */
		public void setFeatureVal(int i, double val){
			try{
				features[i] = val;
			}
			catch(ArrayIndexOutOfBoundsException e){
				System.out.println("Tried to access invalid feature num " + i + ", valid: 0-135\nNo value assigned");
			}
		}
		
		/**
		 *  a.Difference(b) -> a - b (String for outputfile or feeding to machine learning model)
		 * @param b  Record to subtract from currRecord
		 * @return difference between records for feeding to machine learning models
		 */

		public String difference(Record b){
			String toReturn = "";
			toReturn += this.relevance - b.getRelevance() > 0 ? 1 : -1;
			for(int i = 0; i < features.length; i++){
				
				toReturn += ",";
				//boolean values 95-99 produce categorical {-1, 0 , 1}  cast to int so they are enum for H20
				if(i > 94 && i < 100) toReturn += (int) this.features[i] - b.getFeatureVal(i);
				else toReturn += this.features[i] - b.getFeatureVal(i);
			}
			
			return toReturn;
		}
		
		/**
		 * returns rawcsvInput representation
		 */
		public String toString(){
			return csvInput;
		}

		/**
		 * 
		 * @return Comparator on rankScore
		 */
		Comparator<Record> getRankScoreComparator(){
			return new Comparator<Record>(){
				public int compare(Record r1, Record r2){
					return (int)Math.ceil(r2.getRankScore() - r1.getRankScore());
				}
			};
		}
		/**
		 * 
		 * @return Comparator on Relevance
		 */
		Comparator<Record> getRelevanceComparator(){
			return new Comparator<Record>(){
				public int compare(Record r1, Record r2){
					return (int) Math.ceil(r2.getRelevance() - r1.getRelevance());
				}
			};
		}

		
		
	
	}
