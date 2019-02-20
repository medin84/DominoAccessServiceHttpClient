package kz.lof.taglib.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class DominoAccessServiceHttpClient {

	private static final String API_VIEW_URI = "/api/data/collections/name/";
	private static final String API_DOCUMENTS_URI = "/api/data/documents/";
	private static final String API_DOCUMENT_URI = "/api/data/documents/unid/";

	private HttpClient client;
	private UsernamePasswordCredentials authCredentials;
	private String dominoServerAddress;
	private String baseURI;

	public DominoAccessServiceHttpClient(String dominoServerAddress, String userName, String userPassword) {
		client = new HttpClient();
		authCredentials = new UsernamePasswordCredentials(userName, userPassword);
		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(AuthScope.ANY, authCredentials);

		this.dominoServerAddress = dominoServerAddress;
		baseURI = dominoServerAddress;
	}

	public GetMethod getViewDesign(ViewParams params) throws HttpException, IOException {
		return doGet(params.getUrl());
	}

	public GetMethod getViewEntries(ViewCollectionsParams params) throws HttpException, IOException {
		return doGet(params.getUrl());
	}

	public GetMethod getDocuments(DocumentsParams params) throws HttpException, IOException {
		return doGet(params.getUrl());
	}

	public GetMethod getDocument(GetDocumentParams params) throws HttpException, IOException {
		return doGet(params.getUrl());
	}

	public PostMethod createDocument(CreateDocumentParams params) throws HttpException, IOException {
		return doPost(params.getUrl(), params.entity.toString(), false);
	}

	public PostMethod updateDocument(UpdateDocumentParams params) throws HttpException, IOException {
		return doPost(params.getUrl(), params.entity.toString(), true);
	}

	public PutMethod replaceDocument(ReplaceDocumentParams params) throws HttpException, IOException {
		return doPut(params.getUrl(), params.entity.toString());
	}

	public DeleteMethod deleteDocument(DeleteDocumentParams params) throws HttpException, IOException {
		return doDelete(params.getUrl());
	}

	public GetMethod getAttachment(GetAttachmentParams params) throws HttpException, IOException {
		return doGet(params.getUrl());
	}

	private GetMethod doGet(String url) throws HttpException, IOException {
		GetMethod get = new GetMethod(baseURI + url);
		get.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
		get.setRequestHeader("Accept", "application/json");
		client.executeMethod(get);
		return get;
	}

	private PostMethod doPost(String url, String entity, boolean isUpdate) throws HttpException, IOException {
		PostMethod post = new PostMethod(baseURI + url);
		post.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
		post.setRequestHeader("Accept", "application/json");
		if (isUpdate) {
			post.setRequestHeader("X-HTTP-Method-Override", "PATCH");
		}
		post.setRequestEntity(new StringRequestEntity(entity, null, null));

		client.executeMethod(post);
		if (isUpdate && post.getStatusCode() != 200) {
			throw new HttpException("Error: Return code " + post.getStatusCode() + " on POST of url: " + url);
		}
		if (!isUpdate && post.getStatusCode() != 201) {
			throw new HttpException("Error: Return code " + post.getStatusCode() + " on POST of url: " + url);
		}

		return post;
	}

	private PutMethod doPut(String url, String entity) throws HttpException, IOException {
		PutMethod put = new PutMethod(baseURI + url);
		put.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
		put.setRequestHeader("Accept", "application/json");
		put.setRequestEntity(new StringRequestEntity(entity, null, null));

		client.executeMethod(put);
		if (put.getStatusCode() != 200) {
			throw new HttpException("Error: Return code " + put.getStatusCode() + " on PUT of url: " + url);
		}

		return put;
	}

	private DeleteMethod doDelete(String url) throws HttpException, IOException {
		DeleteMethod delete = new DeleteMethod(baseURI + url);
		delete.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
		delete.setRequestHeader("Accept", "application/json");

		client.executeMethod(delete);
		if (delete.getStatusCode() != 200) {
			throw new HttpException("Error: Return code " + delete.getStatusCode() + " on Delete of url: " + url);
		}

		return delete;
	}

	@Override
	public String toString() {
		return "User: " + authCredentials.getUserName() + ", server: " + baseURI;
	}

	private static void assertNotEmpty(String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value is empty");
		}
	}

	private static String encode(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * GET /api/data - Gets a list of databases.
	 */
	public static class DatabaseParams {

		public String database;

		public DatabaseParams(String database) {
			assertNotEmpty(database);
			this.database = database;
		}

		public String getDatabase() {
			return database;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public String getUrl() {
			return "/api/data";
		}

		@Override
		public String toString() {
			return getUrl();
		}
	}

	/**
	 * GET /{database}/api/data/collections/name/{viewName}/design - Gets
	 * information on the columns in a view or folder.
	 */
	public static class ViewParams extends DatabaseParams {

		public String database;
		public String viewName;

		public ViewParams(String database, String viewName) {
			super(database);
			assertNotEmpty(viewName);
			this.viewName = viewName;
		}

		public String getViewName() {
			return viewName;
		}

		public void setViewName(String viewName) {
			this.viewName = viewName;
		}

		public String getUrl() {
			return "/" + getDatabase() + API_VIEW_URI + getViewName() + "/design";
		}
	}

	/**
	 * GET /{database}/api/data/collections/name/{viewName} - Gets a list of view
	 * entries by view name.
	 */
	public static class ViewCollectionsParams extends ViewParams {

		public int start;
		public int count;
		public int page;
		public int ps;
		public boolean entryCount;
		public String search;
		public int searchMaxDocs;
		public String sortColumn;
		public String sortOrder;
		public String startKeys;
		public String keys;
		public boolean keysExactMatch;
		public int expandLevel = -1;
		public String category;
		public String parentId;

		public ViewCollectionsParams(String database, String viewName) {
			super(database, viewName);
		}

		public String getUrl() {
			StringBuilder params = new StringBuilder();
			if (start > 0) {
				params.append("&start=").append(start);
			}
			if (count > 0) {
				params.append("&count=").append(count);
			}
			if (page > 0) {
				params.append("&page=").append(page);
			}
			if (ps > 0) {
				params.append("&ps=").append(ps);
			}
			if (entryCount) {
				params.append("&entrycount=true");
			}
			if (search != null && !search.isEmpty()) {
				params.append("&search=").append(encode(search));
			}
			if (searchMaxDocs > 0) {
				params.append("&searchmaxdocs=").append(searchMaxDocs);
			}
			if (sortColumn != null && !sortColumn.isEmpty()) {
				params.append("&sortcolumn=").append(encode(sortColumn));
			}
			if (sortOrder != null && !sortOrder.isEmpty()) {
				params.append("&sortorder=").append(sortOrder);
			}
			if (startKeys != null && !startKeys.isEmpty()) {
				params.append("&startkeys=").append(encode(startKeys));
			}
			if (keys != null && !keys.isEmpty()) {
				params.append("&keys=").append(encode(keys));
			}
			if (keysExactMatch) {
				params.append("&keysexactmatch=true");
			}
			if (expandLevel > -1) {
				params.append("&expandlevel=").append(expandLevel);
			}
			if (category != null && !category.isEmpty()) {
				params.append("&category=").append(encode(category));
			}
			if (parentId != null && !parentId.isEmpty()) {
				params.append("&parentid=").append(parentId);
			}

			return "/" + getDatabase() + API_VIEW_URI + getViewName()
					+ (params.length() > 0 ? "?" + params.toString() : "");
		}
	}

	/**
	 * /{database}/api/data/documents
	 */

	// GET - Gets a list of documents.
	public static class DocumentsParams extends DatabaseParams {

		public String search;
		public int searchMaxDocs = 100;
		public boolean compact;
		public Date since;

		public DocumentsParams(String database) {
			super(database);
		}

		public String getUrl() {
			StringBuilder params = new StringBuilder();
			if (search != null && !search.isEmpty()) {
				params.append("&search=").append(encode(search));
			}
			if (searchMaxDocs > 0) {
				params.append("&searchmaxdocs=").append(searchMaxDocs);
			}
			if (compact) {
				params.append("&compact=true");
			}
			if (since != null) {
				params.append("&since=").append(since);
			}

			return "/" + getDatabase() + API_DOCUMENTS_URI + (params.length() > 0 ? "?" + params.toString() : "");
		}
	}

	// POST - Creates a new document.
	public static class CreateDocumentParams extends DatabaseParams {

		public String form;
		public String parentId;
		public boolean computeWithForm;

		public Object entity;

		public CreateDocumentParams(String database) {
			super(database);
		}

		public String getUrl() {
			StringBuilder params = new StringBuilder();
			if (form != null && !form.isEmpty()) {
				params.append("&form=").append(encode(form));
			}
			if (parentId != null && !parentId.isEmpty()) {
				params.append("&parentid=").append(encode(parentId));
			}
			if (computeWithForm) {
				params.append("&computewithform=true");
			}

			return "/" + getDatabase() + API_DOCUMENTS_URI + (params.length() > 0 ? "?" + params.toString() : "");
		}
	}

	/**
	 * /{database}/api/data/documents/unid/{docUnid}
	 */

	// GET - Reads a document.
	public static class GetDocumentParams extends DatabaseParams {

		public String unid;
		public boolean hidden;
		public boolean multipart;
		public boolean strongType = true;
		public boolean lowerCaseFields;
		public String fields;
		public boolean markRead;
		public boolean attachmentLinks = true;
		public boolean ifModifiedSince;

		public GetDocumentParams(String database, String unid) {
			super(database);
			assertNotEmpty(unid);
			this.unid = unid;
		}

		public String getUrl() {
			StringBuilder params = new StringBuilder();
			if (hidden) {
				params.append("&hidden=true");
			}
			if (multipart) {
				params.append("&multipart=true");
			}
			if (strongType) {
				params.append("&strongtype=true");
			}
			if (lowerCaseFields) {
				params.append("&lowercasefields=true");
			}
			if (fields != null && !fields.isEmpty()) {
				params.append("&fields=").append(encode(fields));
			}
			if (markRead) {
				params.append("&markread=true");
			}
			if (attachmentLinks) {
				params.append("&attachmentlinks=true");
			}
			if (ifModifiedSince) {
				params.append("&ifmodifiedsince=true");
			}

			return "/" + getDatabase() + API_DOCUMENT_URI + unid + (params.length() > 0 ? "?" + params.toString() : "");
		}
	}

	// PATCH (POST with header X-HTTP-Method-Override=PATCH) - Updates selected
	// items in a document.
	public static class UpdateDocumentParams extends DatabaseParams {

		public String unid;
		public String form;
		public boolean computeWithForm;
		public boolean ifUnmodifiedSince;

		public Object entity;

		public UpdateDocumentParams(String database, String unid) {
			super(database);
			assertNotEmpty(unid);
			this.unid = unid;
		}

		public String getUrl() {
			StringBuilder params = new StringBuilder();
			if (form != null && !form.isEmpty()) {
				params.append("&form=").append(encode(form));
			}
			if (computeWithForm) {
				params.append("&computewithform=true");
			}
			if (ifUnmodifiedSince) {
				params.append("&ifunmodifiedsince=true");
			}

			return "/" + getDatabase() + API_DOCUMENT_URI + unid + (params.length() > 0 ? "?" + params.toString() : "");
		}
	}

	// PUT - Replaces all items in a document.
	public static class ReplaceDocumentParams extends DatabaseParams {

		public String unid;
		public String form;
		public boolean computeWithForm;
		public boolean ifUnmodifiedSince;

		public Object entity;

		public ReplaceDocumentParams(String database, String unid) {
			super(database);
			assertNotEmpty(unid);
			this.unid = unid;
		}

		public String getUrl() {
			StringBuilder params = new StringBuilder();
			if (form != null && !form.isEmpty()) {
				params.append("&form=").append(encode(form));
			}
			if (computeWithForm) {
				params.append("&computewithform=true");
			}
			if (ifUnmodifiedSince) {
				params.append("&ifunmodifiedsince=true");
			}

			return "/" + getDatabase() + API_DOCUMENT_URI + unid + (params.length() > 0 ? "?" + params.toString() : "");
		}
	}

	// DELETE - Deletes a document.
	public static class DeleteDocumentParams extends DatabaseParams {

		public String unid;
		public boolean ifUnmodifiedSince;

		public DeleteDocumentParams(String database, String unid) {
			super(database);
			assertNotEmpty(unid);
			this.unid = unid;
		}

		public String getUrl() {
			String params = (ifUnmodifiedSince ? "?ifunmodifiedsince=true" : "");

			return "/" + getDatabase() + API_DOCUMENT_URI + unid + params;
		}
	}

	/**
	 * GET /{database}/api/data/documents/unid/{docUnid}/{itemName}/{fileName} -
	 * Reads an attachment.
	 */
	public static class GetAttachmentParams extends DatabaseParams {

		public String unid;
		public String itemName;
		public String fileName;

		public GetAttachmentParams(String database, String unid, String itemName, String fileName) {
			super(database);
			assertNotEmpty(unid);
			assertNotEmpty(itemName);
			assertNotEmpty(fileName);
			this.unid = unid;
			this.itemName = itemName;
			this.fileName = fileName;
		}

		public String getUrl() {
			return "/" + getDatabase() + API_DOCUMENT_URI + unid + "/" + itemName + "/" + encode(fileName);
		}
	}
}
