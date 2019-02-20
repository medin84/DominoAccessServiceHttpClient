package kz.lof.taglib.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONObject;

import kz.lof.taglib.services.DominoAccessServiceHttpClient.CreateDocumentParams;
import kz.lof.taglib.services.DominoAccessServiceHttpClient.DeleteDocumentParams;
import kz.lof.taglib.services.DominoAccessServiceHttpClient.GetDocumentParams;
import kz.lof.taglib.services.DominoAccessServiceHttpClient.UpdateDocumentParams;
import kz.lof.taglib.services.DominoAccessServiceHttpClient.ViewCollectionsParams;
import kz.lof.taglib.services.DominoAccessServiceHttpClient.ViewParams;

public class DominoAccessServiceHttpClientTest {

	// argument prefixes
	public static final String SERVER_ARG = "server=";
	public static final String USERNAME_ARG = "user=";
	public static final String PASSWORD_ARG = "password=";
	public static final String DATABASE_ARG = "database=";
	public static final String VIEWNAME_ARG = "view=";
	public static final String RESOURCES_ARG = "resources=";

	// resource arg names
	public static final String RESOURCE_VIEWS = "views";
	public static final String RESOURCE_VIEWENTRIES = "entries";
	public static final String RESOURCE_DOCUMENTS = "documents";

	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			System.out.println("To run this application specify the following arguments:");
			System.out.println("\t" + SERVER_ARG);
			System.out.println("\t" + USERNAME_ARG);
			System.out.println("\t" + PASSWORD_ARG);
			System.out.println("\t" + DATABASE_ARG);
			System.out.println("\t" + VIEWNAME_ARG);
			System.out.println("\t" + RESOURCES_ARG + "[views, entries, documents] comma separated");
			System.out.println("");
			return;
		}

		String dominoServerAddress = "";
		String userName = "";
		String userPassword = "";
		String databaseName = "";
		String viewName = "";
		String resourcesArg = "";

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			String argVal = "";

			if (arg.contains(SERVER_ARG)) {
				argVal = arg.substring(arg.indexOf(SERVER_ARG) + SERVER_ARG.length());
				dominoServerAddress = argVal.trim();
			} else if (arg.contains(DATABASE_ARG)) {
				argVal = arg.substring(arg.indexOf(DATABASE_ARG) + DATABASE_ARG.length());
				databaseName = argVal.trim();
			} else if (arg.contains(USERNAME_ARG)) {
				argVal = arg.substring(arg.indexOf(USERNAME_ARG) + USERNAME_ARG.length());
				userName = argVal.trim();
			} else if (arg.contains(PASSWORD_ARG)) {
				argVal = arg.substring(arg.indexOf(PASSWORD_ARG) + PASSWORD_ARG.length());
				userPassword = argVal.trim();
			} else if (arg.contains(RESOURCES_ARG)) {
				argVal = arg.substring(arg.indexOf(RESOURCES_ARG) + RESOURCES_ARG.length());
				resourcesArg = argVal.trim();
			} else if (arg.contains(VIEWNAME_ARG)) {
				argVal = arg.substring(arg.indexOf(VIEWNAME_ARG) + VIEWNAME_ARG.length());
				viewName = argVal.trim();
			}
		}

		System.out.println("Running rest test(s) with the following arguments:");
		System.out.println("\tServer Address: " + dominoServerAddress);
		System.out.println("\tuser name = " + userName);
		System.out.println("\tdatabase name = " + databaseName);
		System.out.println("\tview name = " + viewName);
		System.out.println("\tResources to try: " + resourcesArg);
		System.out.println("");

		DominoAccessServiceHttpClient das = new DominoAccessServiceHttpClient(dominoServerAddress, userName,
				userPassword);

		if (resourcesArg.length() > 0) {
			// Build the URI and select the test based on arguments from the command line.
			StringTokenizer tokenizer = new StringTokenizer(resourcesArg, ", ");
			while (tokenizer.hasMoreTokens()) {
				String resourceName = tokenizer.nextToken();
				if (resourceName.equalsIgnoreCase(RESOURCE_VIEWS)) {
					testViewDesign(das, databaseName, viewName);
				} else if (resourceName.equalsIgnoreCase(RESOURCE_VIEWENTRIES)) {
					testViewEntries(das, databaseName, viewName);
				} else if (resourceName.equalsIgnoreCase(RESOURCE_DOCUMENTS)) {
					testDocuments(das, databaseName);
				}
			}
		}
	}

	/*
	 * Get list of views and folders of a database
	 */
	private static void testViewDesign(DominoAccessServiceHttpClient das, String databaseName, String viewName)
			throws HttpException, IOException {
		System.out.println("--------------------------");
		System.out.println("Test of views resource..\n");
		ViewParams params = new ViewParams(databaseName, viewName);
		printResponseBodyToConsole(das, das.getViewDesign(params));
	}

	/*
	 * Get list of entries in a given view
	 */
	private static void testViewEntries(DominoAccessServiceHttpClient das, String databaseName, String viewName)
			throws HttpException, IOException {
		System.out.println("--------------------------");
		System.out.println("Test of view entries resource..\n");
		ViewCollectionsParams params = new ViewCollectionsParams(databaseName, viewName);
		printResponseBodyToConsole(das, das.getViewEntries(params));
	}

	/*
	 * Perform REST style CRUD (Create, Read, Update, and Delete) operations on a
	 * single document.
	 */
	private static void testDocuments(DominoAccessServiceHttpClient das, String databaseName)
			throws HttpException, IOException {
		System.out.println("------------------------------------------------------------");
		System.out.println("Test of documents resource..\n");

		// Use JSON library methods to create a JSON object representing a new document.
		JSONObject jsonItem = new JSONObject();
		jsonItem.put("FirstName", "Aaron");
		jsonItem.put("LastName", "Adams");
		jsonItem.put("EMail", "aaron_adams@renovations.com");
		jsonItem.put("City", "Quincy");
		jsonItem.put("State", "MA");
		jsonItem.put("Id", "CN=Aaron Adams/O=renovations");
		jsonItem.put("Form", "Contact");
		// Create a new document in the database and return the URI of the document.

		CreateDocumentParams params = new CreateDocumentParams(databaseName);
		String documentURI = createDocument(das, databaseName, params.getUrl(), jsonItem);
		String[] urlPart = documentURI.split("/");
		String unid = urlPart[urlPart.length - 1];

		// Read the new document
		HttpMethod getResponse = readDocument(das, databaseName, unid);
		if (getResponse.getStatusCode() == 200) {
			printResponseBodyToConsole(das, getResponse);
		} else {
			System.out.println("Error: GET return code expected 200, actual " + getResponse.getStatusCode());
			System.exit(-1);
		}

		// Update the document, change City from "Quincy" to "Braintree".
		jsonItem = new JSONObject();
		jsonItem.put("City", "Braintree");
		updateDocument(das, databaseName, unid, jsonItem);

		// Read the document after update.
		getResponse = readDocument(das, databaseName, unid);
		if (getResponse.getStatusCode() == 200) {
			printResponseBodyToConsole(das, getResponse);
		} else {
			System.out.println("Error: GET return code expected 200, actual " + getResponse.getStatusCode());
			System.exit(-1);
		}

		// Delete the document.
		deleteDocument(das, databaseName, unid);

		// Attempt to read the document after delete, should be not found.
		getResponse = readDocument(das, databaseName, unid);
		if (getResponse.getStatusCode() == 404) {
			System.out.println("Document not found, as expected.");
		} else {
			System.out.println("Error: GET return code expected 404, actual " + getResponse.getStatusCode());
			System.exit(-1);
		}
	}

	/*
	 * Use org.apache.commons.httpclient.methods.PostMethod to create a document.
	 * The new document URI is returned in the Location response-header field.
	 */
	private static String createDocument(DominoAccessServiceHttpClient das, String databaseName, String url,
			JSONObject jsonItem) throws HttpException, IOException {
		System.out.println("--------------------------");
		System.out.println("Test of createDocument..\n");

		String NewDocURI = null;
		CreateDocumentParams params = new CreateDocumentParams(databaseName);
		params.entity = jsonItem;

		PostMethod post = (PostMethod) das.createDocument(params);
		if (post.getStatusCode() != 201) {
			System.out.println("Error: Return code " + post.getStatusCode() + " on POST of url: " + url);
			System.out.println("Please check the test setup, exiting.");
			System.exit(-1);
		}
		Header newLoc = post.getResponseHeader("Location");
		NewDocURI = newLoc.getValue();
		printResponseBodyToConsole(das, post);
		return NewDocURI;
	}

	/*
	 * Use org.apache.commons.httpclient.methods.GetMethod to read a document.
	 */
	private static HttpMethod readDocument(DominoAccessServiceHttpClient das, String databaseName, String unid)
			throws HttpException, IOException {
		System.out.println("--------------------------");
		System.out.println("Test of readDocument..\n");

		GetDocumentParams params = new GetDocumentParams(databaseName, unid);
		return das.getDocument(params);
	}

	/*
	 * Use org.apache.commons.httpclient.methods.PostMethod with request-header
	 * "X-HTTP-Method-Override" set to "PATCH" to update single items on a document.
	 * 
	 * Note the difference of "PATCH" to "PUT" for the DAS API: PATCH allows the
	 * replacement of a single item in the document, while PUT will replace the
	 * entire document.
	 */
	private static void updateDocument(DominoAccessServiceHttpClient das, String databaseName, String unid,
			JSONObject jsonItem) throws HttpException, IOException {
		System.out.println("--------------------------");
		System.out.println("Test of updateDocument..\n");

		UpdateDocumentParams params = new UpdateDocumentParams(databaseName, unid);
		params.entity = jsonItem;
		PostMethod result = (PostMethod) das.updateDocument(params);
		printResponseBodyToConsole(das, result);
	}

	/*
	 * Use org.apache.commons.httpclient.methods.DeleteMethod to delete a document.
	 */
	private static void deleteDocument(DominoAccessServiceHttpClient das, String databaseName, String unid)
			throws HttpException, IOException {
		System.out.println("--------------------------");
		System.out.println("Test of deleteDocument..\n");

		DeleteDocumentParams params = new DeleteDocumentParams(databaseName, unid);
		DeleteMethod delete = (DeleteMethod) das.deleteDocument(params);
		printResponseBodyToConsole(das, delete);
	}

	/*
	 * Print response body to standard out
	 */
	private static void printResponseBodyToConsole(DominoAccessServiceHttpClient das, HttpMethod myMethod) {
		try {
			System.out.println(myMethod.getName() + " " + myMethod.getStatusLine().toString());
			BufferedReader in = new BufferedReader(new InputStreamReader(myMethod.getResponseBodyAsStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
			in.close();
		} catch (HttpException he) {
			System.err.println("Http error connecting to '" + das + "'");
			System.err.println(he.getMessage());
			System.exit(-1);
		} catch (IOException ioe) {
			System.err.println("Unable to connect to '" + das + "'");
			System.err.println(ioe.getMessage());
			System.exit(-1);
		}
	}
}
