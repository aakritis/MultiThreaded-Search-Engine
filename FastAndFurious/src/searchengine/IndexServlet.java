package searchengine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
/*
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
 */



public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private ArrayList<Thread> threadpool = new ArrayList<Thread>();
	private BlockingQueue<String> words = new ArrayBlockingQueue<String>(20);
	private HashSet<String> stopWordSet  = new HashSet<String>();
	private HashMap<String, Double> doc_vals = new HashMap<String, Double>();
	private static int search_query_count;
	private static int count;

	public IndexServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(final ServletConfig config) throws ServletException {
		count = 0;
		ThreadPool t = new ThreadPool (words, doc_vals);
	} 


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// TODO Auto-generated method stub
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html><html lang='en'>");

		/** head */
		out.println("<head><meta charset=\"UTF-8\"/>" +
				"<title>CIS555</title>" +
				"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css\"/>" +
				"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme.min.css\"/>" +
				"<link rel=\"stylesheet\" href=\"http://cdn.datatables.net/1.10.6/css/jquery.dataTables.css\"/>" +
				"<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js\"></script>" +
				"<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js\"></script>" +
				"<script src=\"http://code.jquery.com/jquery-1.11.1.min.js\"></script>" +
				"<script src=\"http://cdn.datatables.net/1.10.6/js/jquery.dataTables.min.js\"></script>" +

		"<script src=\"http://cdn.datatables.net/plug-ins/1.10.7/integration/bootstrap/3/dataTables.bootstrap.js\"></script>" +


		  "<script type=\"text/javascript\">" +
		  "$(document).ready(function() { $('#example').dataTable( { \"pagingType\": \"full_numbers\",\"bFilter\": false});});" +
				" </script><head>");

		/** body */
		out.println("<body>");

		/** nav-bar */
		out.println("<div class=\"container\">" +
				"<div class=\"row\">" +
				"<div class=\"col-xs-12\">" +
				"<nav role=\"navigation\" class=\"navbar navbar-default topnav\">" +
				"<div class=\"container topnav\">" +
				"<div class=\"navbar-header\"><a href=\"#\" class=\"navbar-brand topnav1\"><b>Search Engine</b></a></div>" +
				"</div>" +
				"</nav>" +
				"</div>" +
				"</div>" +

				"</div>");


		String query = request.getParameter("searchthis");
		//String query = "hello duble";

		//System.out.println(query);
		//String suggest = SpellingSuggest.finalGuess(query);

		out.println("<p>Showing results for " + query + "<br><br>");


		String[] eachWord = query.split("\\s+");
		for (String word : eachWord) {
			words.add(word);
			search_query_count++;
		}
		boolean isQueryProcessed = false;
		while(!isQueryProcessed){
			isQueryProcessed = check_query_processed(false);
		}
		// put page rank

		//Sort the hash map and return the results
		Set<Entry<String, Double>> dict_set = doc_vals.entrySet();
		List<Entry<String, Double>> sort_list = new ArrayList<Entry<String, Double>>(dict_set);
		Collections.sort( sort_list, new Comparator<Map.Entry<String, Double>>(){
			public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 ){
				return (o2.getValue()).compareTo( o1.getValue() );
			}
		});

		int size_sort_list = sort_list.size();

		//Dynamo db table
		DynamoDB dynamoDBURL = new DynamoDB(new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()));
		Table doc_url_table = dynamoDBURL.getTable("doc_to_url");

		out.println("<form name=\"notifyForm\" action=\"/notify\" method=\"post\">" +
				"<table id=\"example\" class=\"table table-striped table-bordered\" cellspacing=\"0\" width=\"100%\">" +
				"<thead><tr>" +

	                "<th>Results</th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "</tr></thead>" +


	        "<tfoot><tr>" +

	                "<th>Results</th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "<th style=\"visibility:hidden\"></th>" +
	                "<th style=\"visibility:hidden\"></th>" +
				"</tr></tfoot>" );

		for(int i=0; i<size_sort_list; i++){
			try {
				Item doc_url_item = doc_url_table.getItem("doc_id",sort_list.get(i).getKey());
				//If there is no mapping for that document ID then 
				if(doc_url_item == null){
					continue;
				}
				String url_doc = doc_url_item.getString("url");
				out.println("<tbody><tr>" +
						"<td><a href='"+url_doc+"'>" + url_doc + "</a></td>" +
						"<td style=\"visibility:hidden\"></td>" +
						"<td style=\"visibility:hidden\"></td>" +
						"<td style=\"visibility:hidden\"></td>" +
						"<td style=\"visibility:hidden\"></td>" +
						"<td style=\"visibility:hidden\"></td></tr>");
			}
			catch(Exception e){
				continue;
			}
		}
		out.println("</tbody></table></form></body></html>");

	}

	public static boolean check_query_processed(boolean b) {
		// TODO Auto-generated method stub
		if(b)
			count++;
		//System.out.println("Count " + count);
		if(count==search_query_count){
			return true;
		}
		return false;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
