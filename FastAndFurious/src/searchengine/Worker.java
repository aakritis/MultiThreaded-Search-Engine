package searchengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class Worker extends Thread{

	private HashMap<String, Double> docValue;
	BlockingQueue<String> search_words;
	String[] feature_space = {"a", "b", "i", "h1", "h2", "h3", "h4", "h5", "h6","title", "meta", "u" ,"tf_idf"};
	double[] feature_values = {2, 2, 2, 4, 3, 1, 1, 1, 1,5, 3, 1, 50};
	static AmazonDynamoDBClient dynamoDB;

	public Worker(BlockingQueue<String> search_words,
			HashMap<String, Double> docValue) {
		// TODO Auto-generated constructor stub
		this.docValue = docValue;
		this.search_words = search_words;
		/*try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	private void init() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default]
		 * credential profile by reading from the credentials file located at
		 * (/home/cis455/.aws/credentials).
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (/home/cis455/.aws/credentials), and is in valid format.",
							e);
		}
		dynamoDB = new AmazonDynamoDBClient(credentials);
		/*Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);*/
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			//Take the head from the blocking queue
			String word = null;
			synchronized(search_words){
				try {
					word = search_words.take();
					//System.out.println("[WordCheck]" + Thread.currentThread() + word);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("Exception while fetching words from bloking queue");
					e.printStackTrace();
				}
			}

			//Get the docIDs from the DynamoDB by querying the word.
			DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
					new ProfileCredentialsProvider()));
			Table word_doc_table = dynamoDB.getTable("word_to_doc");
			Table worddoc_feature_table = dynamoDB.getTable("word_to_docFeatures");
			Item word_doc_item = word_doc_table.getItem("word",word);
			if(word_doc_item==null){
				IndexServlet.check_query_processed(true);
				continue;
			}
			List<Object> doc_list_obj = word_doc_item.getList("docids");
			if(doc_list_obj==null){
				IndexServlet.check_query_processed(true);
				continue;
			}

			//Temp coded added
			int cnt=0;
			for(Object docID:doc_list_obj){
				cnt++;
				if(cnt==50)
					break;
				if(docID==null){
					continue;
				}
				double valOfDoc = 0;
				String document_id = docID.toString();				
				//Get the features and positions from the DynamoDB by querying the word_docID
				Item features = worddoc_feature_table.getItem("word_docid",word + "_" + docID.toString());
				if(features==null){
					continue;
				}
				//Get the features Map from the Item features
				Map<String, Object> featuresMap = features.getMap("featres");
				if(featuresMap==null){
					continue;
				}
				//Calculate the aggregate score by assigning weights to each feature
				int i = 0;
				for(String feature: feature_space){
					if(featuresMap.containsKey(feature)){
						valOfDoc += Double.parseDouble(featuresMap.get(feature).toString())*feature_values[i];	
					}
					i++;
				}
				synchronized(docValue){
					if(docValue.containsKey(document_id))
						docValue.put(document_id, docValue.get(document_id)+valOfDoc);
					else
						docValue.put(document_id, valOfDoc);
				}
				//System.out.println("[value]:" + valOfDoc + "  [docid]:" + docID);
			}

			/*
			String tableName = "word_to_docFeatures";
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                .withAttributeValueList(new AttributeValue().withS(word));
            scanFilter.put("word_docid", condition);
            ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
            ScanResult scanResult = dynamoDB.scan(scanRequest);
            int size = scanResult.getCount();
            int scansize = scanResult.getScannedCount();
            System.out.println("[Debug] Size:" + size + " ScanSize:" + scansize);
            for (int i=0; i< size; i++) {	
            	System.out.println("Result: " + scanResult.getItems().get(i).get("featres"));
            }*/			
			IndexServlet.check_query_processed(true);
		}	
	}
}