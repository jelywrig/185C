/**
 * 
 * place holder model
 *
 */



public class MSLRdummyModel implements MSLRbinaryModel {

	@Override
	public int predict(String difference) {
		
		return (Math.random() < 0.5)? -1 : 1;
	}
	
	

}
