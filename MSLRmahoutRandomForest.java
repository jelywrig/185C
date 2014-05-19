import java.io.IOException;
import java.util.Random;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.mahout.classifier.df.data.DataConverter;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.Instance;
import org.apache.mahout.classifier.df.DecisionForest;
import org.apache.mahout.common.RandomUtils;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.common.RandomUtils;


public class MSLRmahoutRandomForest implements MSLRbinaryModel {
	private Dataset descriptor;
	private DataConverter converter;
	private DecisionForest forest;
	private Random rng;
	
	
	public MSLRmahoutRandomForest(String pathToForest, String pathToDataDescriptor) {
		try{
			Configuration conf = new Configuration();
			descriptor = Dataset.load(conf, new Path(pathToDataDescriptor));
			converter = new DataConverter(descriptor);
			forest = DecisionForest.load(conf, new Path(pathToForest));
			rng = RandomUtils.getRandom();
		} catch(Exception e){
			System.out.println("Exception RF constructor: " + e.getMessage());
		}
	}
	
	
	@Override
	public int predict(String difference) {
		
		Instance instance = converter.convert(difference);
		double prediction = forest.classify(descriptor, rng, instance);
		
		return (prediction > 0 )? -1 : 0 ;
	}

}
