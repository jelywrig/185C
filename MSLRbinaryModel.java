/**
 * 
 * @author Jessie Wright
 * 
 *
 */



public interface MSLRbinaryModel
{
	

	/**
	 * 
	 * 
	 * 
	 * @param difference  MSLR feature difference (a-b)
	 * @return 1 a is better -1 b is better
	 */
	public int predict(String difference);
	


	

}
