
public interface MSLRcontinuousModel {
	
	
	/*
	 * takes a string representation of a pairwise difference between documents (a-b)
	 * returns confidence that first document is better than second (prob a wins = 0.7 means prob b wins 0.3)
	 */
	double predict(String difference);
}
