/**
 *  Query represents a collection of Records (document/query relevance + feature vectors for the MSLR
 *  data set)
 */


import java.util.ArrayList;
import java.util.Collections;


public class Query {

	private int queryId;
	private ArrayList<Record> documents;
	private ArrayList<Integer> relevanceList;
	private Boolean ranked;
	
	/**
	 * 
	 * @param queryID
	 */
	public Query(int queryId){
		this.queryId = queryId;
		documents = new ArrayList<Record>();
		ranked = false;
		relevanceList = new ArrayList<Integer>();
		
	}
	/**
	 * used to check whether Record List has been ranked yet or not
	 * @return  
	 */
	public boolean isRanked(){
		
		return ranked;
	}
	
	/**
	 * 
	 * @return queryID
	 */
	public int getQueryId(){
		return queryId;
	}
	
	/**
	 * adds Record to Query
	 * @param toAdd
	 */
	public void addRecord(Record toAdd){
		
		//adding new record after ranking invalidates ranking
		if(ranked) ranked = false;
		
		relevanceList.add(toAdd.getRelevance());
		documents.add(toAdd);
		
	}
	
	/**
	 * 
	 * @param i  index
	 * @return
	 */
	public Record getRecordAt(int i){
		
		return documents.get(i);
	}
	
	/**
	 * records pairwise classification results (from model) for all Records in Query
	 * uses these results to order records in proposed best ordering of Records for Query
	 * 
	 * @param model 
	 */
	void peformRanking(MSLRbinaryModel model){
		
		int[][] pairwiseRel = new int[this.documents.size()][this.documents.size()];
		
				
		for(int i = 0; i < this.documents.size(); i++){
			
			for(int j = i + 1; j < this.documents.size(); j++){
				
				int result = model.predict(documents.get(i).difference(documents.get(j)));
				
				pairwiseRel[i][j] = result;
				pairwiseRel[j][i] = -result;
			}	
		}
		//score each doc
		for(int i = 0; i < this.documents.size(); i++){
			int score = 0;
			for(int j = 0; j < this.documents.size(); j++){
				score += pairwiseRel[i][j];
			}
			documents.get(i).setRankScore(score);
		}
		
		//shuffle to do away with bias of original document order
		Collections.shuffle(documents);

		// sort by rank score
		Collections.sort(documents, documents.get(0).getRankScoreComparator()) ;
		ranked = true;
		
	}
	
	
	/**
	 * Discounted Cumulative Gain 
	 * from wikipedia: DCG = relevance1 + rel2/log(2) + rel3/log(3) + rel4/log(4)....
	 * 
	 * Ideal in this case, means if records were ordered by their objective relevance scores
	 * eg. 4, 3, 3, 2, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0...
	 * @return DCG
	 */
	
	double getIdealDCG(){
		double idealDCG;
		Collections.sort(this.relevanceList);
		Collections.sort(this.relevanceList, Collections.reverseOrder());
		/* for debugging
		System.out.println("Ideal order");
		for(int i = 0; i < relevanceList.size(); i++){
			System.out.print(relevanceList.get(i) + ", ");
		} 
		System.out.println(); */
		idealDCG = relevanceList.get(0);
		for(int i = 1; i < relevanceList.size(); i++){
			idealDCG += relevanceList.get(i)/Math.log(i+1); 
		}
		return idealDCG;
	}
	
	/**
	 * Discounted Cumulative Gain 
	 * from wikipedia: DCG = relevance1 + rel2/log(2) + rel3/log(3) + rel4/log(4)....
	 * 
	 * rankedQuery is ordered by rankScore and evaluated by objective relevance of Records
	 * discounts are applied the further from the head of the list so if a 4 appears further
	 * down the list it will contribute less to the score 	 
	 * 
	 * 2,3,4,1,0,0  will score lower than 4,2,3,1,0,0 which will score lower than 4,3,2,1,0,0
	 *
	 *
	 * @return
	 */
	
	double getResultDCG(){
		double dcg;
		if(this.isRanked()){
			//System.out.println("Actual order:");
			dcg = this.documents.get(0).getRelevance();
			for(int i = 1 ; i < this.documents.size(); i++){
				dcg += this.documents.get(i).getRelevance()/Math.log(i+1);
				// for debugging
				//System.out.print(this.documents.get(i).getRelevance() +"(" + this.documents.get(i).getRankScore() +  ") , ");
			}
			//System.out.println();
			return dcg;
		}
		else {
			System.out.println("Trying to evaluate unranked query");
			return 0.0;
		}
		
	}
	
	/**
	 *  outputs ordered list of documents in query (Relevance, RankScore)
	 */
	void displayQueryDocumentRanking(){
		int count = 0;
		System.out.println("(Relevance, RankScore)");
		for(Record r : documents){
			System.out.print("(" + r.getRelevance() + "," + r.getRankScore() + "),");
			if(count % 10 == 0) System.out.println();
		}	
	}
	
	/**
	 * 
	 */
	
	public String toString(){
		return ("[Query: " + queryId + ", Contains " + documents.size() + (ranked ? " ranked Records]" : " un-ranked Records]"));
	}
}