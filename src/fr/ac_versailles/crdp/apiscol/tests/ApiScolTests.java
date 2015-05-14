package fr.ac_versailles.crdp.apiscol.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

@Ignore
public class ApiScolTests {
	protected static final String LOGIN = "crdp";
	protected static final String PASSWORD = "foucault";
	protected WebClient webClient;
	protected String testDataDirectory;
	protected boolean overallDeletionAuthorized;
	protected static String editionServiceBaseUrl;
	protected static String metaServiceBaseUrl;
	protected static String contentServiceBaseUrl;
	protected static String thumbsServiceBaseUrl;
	private static String noncePatternStr = "nextnonce=\"([^\"]+)\"";
	private static Pattern noncePattern = Pattern.compile(noncePatternStr);

	public void createClient() {
		webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setUseInsecureSSL(true);
		editionServiceBaseUrl = System.getProperty("edit.ws.url");
		if (StringUtils.isEmpty(editionServiceBaseUrl))
			editionServiceBaseUrl = "http://dev-metaeduc.itop.local";
		metaServiceBaseUrl = System.getProperty("meta.ws.url");
		if (StringUtils.isEmpty(metaServiceBaseUrl))
			metaServiceBaseUrl = "http://dev-metaeduc.itop.local";
		contentServiceBaseUrl = System.getProperty("content.ws.url");
		if (StringUtils.isEmpty(contentServiceBaseUrl))
			contentServiceBaseUrl = "http://dev-metaeduc.itop.local";
		thumbsServiceBaseUrl = System.getProperty("thumbs.ws.url");
		if (StringUtils.isEmpty(thumbsServiceBaseUrl))
			thumbsServiceBaseUrl = "http://dev-metaeduc.itop.local";
		testDataDirectory = System.getProperty("tests.data.dir");
		if (StringUtils.isEmpty(testDataDirectory))
			testDataDirectory = "data/";
		String deletion = System.getProperty("delete.all.contents");
		overallDeletionAuthorized = StringUtils.equals(deletion, "true");
	}

	public void closeClient() {
		webClient.closeAllWindows();
	}

	protected URL getServiceUrl(String path, String baseUrl) {
		URL url = null;
		String completeUrl = path;
		if (!path.startsWith("http")) {
			assertTrue("This path is not correct witout vbase url: "
					+ completeUrl, baseUrl != null);
			completeUrl = new StringBuilder().append(baseUrl)
					.append(completeUrl).toString();
		}

		try {

			url = new URL(completeUrl);
		} catch (MalformedURLException e) {
			assertTrue("This url is maformed : " + completeUrl, false);
		}
		return url;
	}

	protected XmlPage postMaintenanceRequest(String service, String command) {
		URL url = getServiceUrl("/edit/maintenance/" + service + "/" + command,
				editionServiceBaseUrl);
		WebRequest request = new WebRequest(url, HttpMethod.POST);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setEncodingType(FormEncodingType.URL_ENCODED);

		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message "
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	/*
	 * Test on namespaces
	 */
	protected void testNodeHasApiscolNameSpace(Element node) {
		assertTrue(
				"The nameSpaceUri of the element "
						+ node.getLocalName()
						+ "should be http://www.crdp.ac-versailles.fr/2012/apiscol and not "
						+ node.getNamespaceURI(),
				node.getNamespaceURI().equals(
						"http://www.crdp.ac-versailles.fr/2012/apiscol"));
	}

	/*
	 * Atom Documents test
	 */
	protected void testAtomRootElement(XmlPage page) {
		final NodeList nodelist = page.getChildNodes();
		assertTrue(
				"There must be one and only one child node, there is or are : "
						+ nodelist.getLength(), nodelist.getLength() == 1);
		String rootName = nodelist.item(0).getLocalName();
		assertTrue("The root element has to be called entry, not " + rootName,
				rootName.equals("entry"));

	}

	protected String getAtomLinkLocation(XmlPage page, String rel, String type) {
		Element root = getAtomDocumentRootItem(page);
		NodeList links = root.getElementsByTagName("link");
		for (int i = 0; i < links.getLength(); i++) {
			Element link = (Element) links.item(i);
			assertTrue("The link n°" + i + " should have an href attribute",
					link.hasAttribute("href"));
			assertTrue("The link n°" + i + " should have a rel attribute",
					link.hasAttribute("rel"));
			assertTrue("The link n°" + i + " should have a type attribute",
					link.hasAttribute("type"));
			if (link.getAttribute("rel").equals(rel)
					&& link.getAttribute("type").equals(type))
				return link.getAttribute("href").toString();
		}
		assertTrue("There is no attribute with rel " + rel + " an type " + type
				+ " in the document" + page.asXml(), false);
		return null;
	}

	protected void testDocumentLinksAre(XmlPage page, int numLinks) {
		Element root = getAtomDocumentRootItem(page);
		NodeList links = root.getElementsByTagName("link");
		assertTrue(
				"There should be " + numLinks + " links and not "
						+ links.getLength(), links.getLength() == numLinks);
	}

	protected Element getAtomDocumentRootItem(XmlPage page) {
		Element root = (Element) page.getChildNodes().item(0);
		assertTrue("There was no atom xml representation of the document xml",
				root.getLocalName().equals("entry"));
		return root;
	}

	protected String getAtomUpdatedField(XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList updateds = root.getElementsByTagName("updated");
		assertTrue(
				"There should be only one updated and not "
						+ updateds.getLength(), updateds.getLength() == 1);
		String updated = ((Element) updateds.item(0)).getTextContent();
		return updated;
	}

	protected String getAtomId(XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList ids = root.getElementsByTagName("id");
		assertTrue("There should be only one id and not " + ids.getLength(),
				ids.getLength() == 1);
		String id = ((Element) ids.item(0)).getTextContent();
		return id;
	}

	protected void testAtomDocumentTitleIs(String titleString, XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList titles = root.getElementsByTagName("title");
		assertTrue(
				"There should be only one title and not " + titles.getLength(),
				titles.getLength() == 1);
		String title = ((Element) titles.item(0)).getTextContent();
		assertTrue("The title should be " + titleString + " and not " + title,
				title.equals(titleString));
	}

	protected void testAtomDocumentHasAuthorTag(XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList autors = root.getElementsByTagName("author");
		assertTrue("There should be at least one author",
				autors.getLength() >= 1);
	}

	protected void testAtomDocumentAuthorsContains(String author, XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList authors = root.getElementsByTagName("author");
		for (int i = 0; i < authors.getLength(); i++) {
			if (((Element) authors.item(0)).getTextContent().equals(author))
				return;
		}
		assertTrue("One one the authors should be " + author, false);
	}

	protected void testAtomDocumentSummaryContains(String summaryExtract,
			XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList summaries = root.getElementsByTagName("summary");
		assertTrue(
				"There should be only one summary and not "
						+ summaries.getLength(), summaries.getLength() == 1);
		String summary = ((Element) summaries.item(0)).getTextContent();
		System.out.println(summaryExtract);
		System.out.println(summary);
		assertTrue("The summary should contain" + summaryExtract + " it is "
				+ summary, summary.contains(summaryExtract));
	}

	/*
	 * Report document test
	 */
	protected void testReportDocumentMessageContains(String messageString,
			XmlPage page) {
		Element root = getReportDocumentRootItem(page);
		NodeList messages = root.getElementsByTagName("message");
		assertTrue(
				"There should be only one title and not "
						+ messages.getLength(), messages.getLength() == 1);
		String title = ((Element) messages.item(0)).getTextContent();
		assertTrue("The title should contains " + messageString + " but it is "
				+ title, title.contains(messageString));
	}

	protected void testReportDocumentStatusIsDone(XmlPage page) {
		Element root = getReportDocumentRootItem(page);
		NodeList status = root.getElementsByTagName("state");
		assertTrue(
				"There should be only one state and not " + status.getLength(),
				status.getLength() == 1);
		String statusStr = ((Element) status.item(0)).getTextContent();
		assertTrue("The status should be 'done' and not " + statusStr,
				statusStr.equals("done"));
	}

	protected String getReportDocumentStatus(XmlPage page) {
		Element root = getReportDocumentRootItem(page);
		NodeList status = root.getElementsByTagName("state");
		assertTrue(
				"There should be only one state and not " + status.getLength(),
				status.getLength() == 1);
		return ((Element) status.item(0)).getTextContent();
	}

	protected String getReportLinkLocation(XmlPage page, String rel, String type) {
		Element root = getReportDocumentRootItem(page);
		NodeList links = root.getElementsByTagName("link");
		for (int i = 0; i < links.getLength(); i++) {
			Element link = (Element) links.item(i);
			assertTrue("The link n°" + i + " should have an href attribute",
					link.hasAttribute("href"));
			assertTrue("The link n°" + i + " should have a rel attribute",
					link.hasAttribute("rel"));
			assertTrue("The link n°" + i + " should have a type attribute",
					link.hasAttribute("type"));
			if (link.getAttribute("rel").equals(rel)
					&& link.getAttribute("type").equals(type))
				return link.getAttribute("href").toString();
		}
		assertTrue("There is no attribute with rel " + rel + " an type " + type
				+ " in the document", false);
		return null;
	}

	private Element getReportDocumentRootItem(XmlPage page) {
		Element root = (Element) page.getChildNodes().item(0);
		assertTrue(
				"There was no status tag at the root of the xml response but "
						+ root.getLocalName(),
				root.getLocalName().equals("status"));
		return root;
	}

	/**
	 * Resources document tests
	 */
	protected String getMainFileForRessource(XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList contents = root.getElementsByTagName("content");
		assertTrue(
				"There should be only one content and not "
						+ contents.getLength(), contents.getLength() == 1);
		NodeList files = ((Element) contents.item(0))
				.getElementsByTagName("files");
		assertTrue(
				"There should be only one files element child of content and not "
						+ files.getLength(), files.getLength() == 1);
		Element fileElem = ((Element) files.item(0));
		testNodeHasApiscolNameSpace(fileElem);
		assertTrue("Files element should have a 'main' attribute ",
				fileElem.hasAttribute("main"));
		return fileElem.getAttribute("main");
	}

	protected String getWebPageFullyQualifiedUrl(XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList contents = root.getElementsByTagName("content");
		int length = ((Element) contents.item(0)).getElementsByTagName("a")
				.getLength();
		assertTrue(
				"It is a remote resource : content tag should have exactly 1 <a> child and not "
						+ length, length == 1);
		Element link = (Element) ((Element) contents.item(0))
				.getElementsByTagName("a").item(0);
		return link.getAttribute("href");
	}

	protected String getFileDownloadUrl(String fileTitle, XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList contents = root.getElementsByTagName("content");
		NodeList files = ((Element) ((Element) contents.item(0))
				.getElementsByTagName("files").item(0))
				.getElementsByTagName("file");
		for (int i = 0; i < files.getLength(); i++) {
			Element file = (Element) files.item(i);
			Element titleElem = ((Element) file.getElementsByTagName("title")
					.item(0));
			if (StringUtils.equals(titleElem.getTextContent(), fileTitle)) {
				Element linkElem = ((Element) file.getElementsByTagName("link")
						.item(0));
				return linkElem.getAttribute("href");
			}

		}
		return null;
	}

	/**
	 * Dialog with metadata service
	 */
	protected XmlPage postMetadataDocument(String path, URL url) {
		return postMetadataDocument(path, url, false);
	}

	protected XmlPage postMetadataDocument(String path, URL url,
			boolean ignoreFailure) {
		WebRequest request = new WebRequest(url, HttpMethod.POST);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setEncodingType(FormEncodingType.MULTIPART);
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		File file = new File(testDataDirectory + path);
		assertTrue("Test file does not exist " + file.getAbsolutePath(),
				file.exists());
		params.add(new KeyDataPair("file", file, "application/xml", "utf-8"));
		request.setRequestParameters(params);

		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			if (!ignoreFailure)
				assertTrue(
						"The response code should be ok and not"
								+ e.getStatusCode() + " with message "
								+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	protected void testResourceContentSrcEndsWidth(String fileName, XmlPage page) {
		Element root = getAtomDocumentRootItem(page);
		NodeList contents = root.getElementsByTagName("content");
		assertTrue("There should be 1 content and not " + contents.getLength(),
				contents.getLength() == 1);
		Element content = (Element) contents.item(0);
		String src = content.getAttribute("src");
		assertTrue("Content src should end with " + fileName + " and it is "
				+ src, src.endsWith(fileName));
	}

	/**
	 * Read lom metadata files
	 */
	protected void testTechnicalLocationIs(String provided, XmlPage lomDocument) {
		Element technical = getLomDocumentFirstElement("technical", lomDocument);
		NodeList elems = technical.getElementsByTagName("location");
		assertTrue(
				"There should be one technical location node in the metadata document and not "
						+ elems.getLength(), elems.getLength() == 1);
		String found = ((Element) elems.item(0)).getTextContent();
		assertTrue("Technical location should be " + provided + " and not "
				+ found, provided.equals(found));
	}

	private Element getLomDocumentFirstElement(String name, XmlPage lomDocument) {
		Element root = getLomDocumentRootItem(lomDocument);
		NodeList elems = root.getElementsByTagName(name);
		assertTrue(
				"There should be one " + name
						+ " node in the metadata document and not "
						+ elems.getLength(), elems.getLength() == 1);
		return (Element) elems.item(0);
	}

	protected void testTechnicalSizeIs(String provided, XmlPage lomDocument) {
		Element technical = getLomDocumentFirstElement("technical", lomDocument);
		NodeList elems = technical.getElementsByTagName("size");
		assertTrue(
				"There should be one size location node in the metadata document and not "
						+ elems.getLength(), elems.getLength() == 1);
		String found = ((Element) elems.item(0)).getTextContent();
		assertTrue("Technical location should be " + provided + " and not "
				+ found, provided.equals(found));

	}

	protected void testTechnicalFormatIs(String provided, XmlPage lomDocument) {
		Element technical = getLomDocumentFirstElement("technical", lomDocument);
		NodeList elems = technical.getElementsByTagName("format");
		assertTrue(
				"There should be one technical location node in the metadata document ",
				elems.getLength() >= 1);
		String found = ((Element) elems.item(0)).getTextContent();
		assertTrue("Technical location should be " + provided + " and not "
				+ found, provided.equals(found));

	}

	protected Element getLomDocumentRootItem(XmlPage page) {
		Element root = (Element) page.getChildNodes().item(0);
		assertTrue("There was no lom node in the metadata document ", root
				.getLocalName().equals("lom"));
		return root;
	}

	protected void testResourceLanguageIs(XmlPage lomDocument, String provided) {
		Element general = getLomDocumentFirstElement("general", lomDocument);
		NodeList elems = general.getElementsByTagName("language");
		assertTrue(
				"There should be one general language node in the metadata document and not "
						+ elems.getLength(), elems.getLength() == 1);
		String found = ((Element) elems.item(0)).getTextContent();
		assertTrue("Technical location should be " + provided + " and not "
				+ found, provided.equals(found));

	}

	/**
	 * Dialog with pack service
	 */

	protected XmlPage postImsLdDocument(File file, URL url) {
		WebRequest request = new WebRequest(url, HttpMethod.POST);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setEncodingType(FormEncodingType.MULTIPART);
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		assertTrue("Test file does not exist " + file.getAbsolutePath(),
				file.exists());
		params.add(new KeyDataPair("file", file, "application/xml", "utf-8"));
		request.setRequestParameters(params);

		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			System.out.println(url);
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	/**
	 * Dialog with content service
	 */
	protected XmlPage getNewResourcePage(String metadata) {
		return getNewResourcePage(metadata, "asset");
	}

	protected XmlPage getNewResourcePage(String metadata, String type) {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		XmlPage page = createNewRessource(metadata, type);
		return page;
	}

	private XmlPage createNewRessource(String metadata, String type) {
		WebRequest request = new WebRequest(getServiceUrl("/edit/resource",
				editionServiceBaseUrl), HttpMethod.POST);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setEncodingType(FormEncodingType.URL_ENCODED);
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new NameValuePair("mdid", metadata));
		params.add(new NameValuePair("type", type));
		request.setRequestParameters(params);

		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	protected XmlPage postFileDocument(String url, String urn, String filePath,
			String etag) {
		return postFileDocument(url, urn, filePath, false, etag);
	}

	protected XmlPage postFileDocument(String url, String urn, String filePath,
			Boolean ignoreDefault, String etag) {
		try {
			filePath = URLDecoder.decode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		XmlPage page = postContentDocument(filePath, url, urn, etag);
		XmlPage page2 = waitForStatusDone(page);
		if (ignoreDefault)
			return null;
		File file = new File(testDataDirectory + filePath);
		String fileName = file.getName();
		System.out.println(filePath);
		assertTrue(
				"When asking for file tranfer report status, response should not be null	",
				page2 != null);
		controleFileTransferReport(page2, fileName);
		String resourceLink = getReportLinkLocation(page2, "item",
				"application/atom+xml");
		XmlPage page3 = askForResourceRepresentation(resourceLink);
		String resourceLink2 = getAtomLinkLocation(page3, "self", "text/html");
		assertTrue("The new resource represention link " + resourceLink2
				+ " should be equal to the previous " + resourceLink,
				resourceLink.equals(resourceLink2));
		String mainFile = getMainFileForRessource(page3);
		String filePathWithoutFolder = filePath.substring(filePath
				.lastIndexOf(File.separator) + 1);
		assertTrue("The main fin name should be " + filePathWithoutFolder
				+ " and not " + mainFile,
				StringUtils.equals(filePathWithoutFolder, mainFile));
		return page3;
	}

	protected XmlPage sendMetaForResource(String editUri,
			String metadataLinkLocation, String etag) {
		WebRequest request = new WebRequest(getServiceUrl(editUri, null),
				HttpMethod.PUT);
		request.setAdditionalHeader("Accept", "application/xml");
		request.setAdditionalHeader("If-Match", etag);
		request.setRequestBody("mdid=" + metadataLinkLocation);
		request.setEncodingType(FormEncodingType.URL_ENCODED);
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;

	}

	protected XmlPage putManifest(String url, String filePath) {
		try {
			filePath = URLDecoder.decode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		WebRequest request = new WebRequest(getServiceUrl(url, null),
				HttpMethod.PUT);
		request.setAdditionalHeader("Accept", "application/xml");
		request.setRequestBody("mdid=" + url);
		request.setEncodingType(FormEncodingType.URL_ENCODED);

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		File file = new File(testDataDirectory + filePath);
		assertTrue("Test file does not exist " + file.getAbsolutePath(),
				file.exists());
		params.add(new KeyDataPair("file", file, "application/xml", "utf-8"));
		params.add(new NameValuePair("update_archive", "true"));
		request.setRequestParameters(params);
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	protected XmlPage postUrlContent(String url, String urn, String contentUrl,
			String etag) {
		return postUrlContent(url, urn, contentUrl, false, etag);
	}

	protected XmlPage postUrlContent(String url, String urn, String contentUrl,
			boolean ignoreDefault, String etag) {
		XmlPage page = postContentLink(contentUrl, url, urn, etag);

		XmlPage page2 = waitForStatusDone(page, ignoreDefault, 0);
		if (page2 == null)
			return null;
		String resourceLink = getReportLinkLocation(page2, "item",
				"application/atom+xml");
		XmlPage page3 = askForResourceRepresentation(resourceLink);
		String resourceLink2 = getAtomLinkLocation(page3, "self", "text/html");
		assertTrue("The new resource represention link " + resourceLink2
				+ " should be equal to the previous " + resourceLink,
				resourceLink.equals(resourceLink2));

		return page3;
	}

	private XmlPage postContentDocument(String path, String url, String urn,
			String etag) {

		WebRequest request = new WebRequest(getServiceUrl(url, null),
				HttpMethod.POST);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setAdditionalHeader("If-Match", etag);
		request.setEncodingType(FormEncodingType.MULTIPART);
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		File file = new File(testDataDirectory + path);
		assertTrue("Test file does not exist " + file.getAbsolutePath(),
				file.exists());
		params.add(new KeyDataPair("file", file, "application/xml", "utf-8"));
		params.add(new NameValuePair("resid", urn));
		params.add(new NameValuePair("is_archive", "false"));
		params.add(new NameValuePair("update_archive", "true"));
		request.setRequestParameters(params);
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	private XmlPage postContentLink(String contentUrl, String url, String urn,
			String etag) {
		WebRequest request = new WebRequest(getServiceUrl(url, null),
				HttpMethod.POST);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setAdditionalHeader("If-Match", etag);

		request.setRequestBody("resid=" + urn + "&url=" + contentUrl
				+ "&update_archive=true");
		request.setEncodingType(FormEncodingType.URL_ENCODED);
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	private XmlPage waitForStatusDone(XmlPage page, boolean ignoreDefault,
			int numberOfTries) {
		if (numberOfTries > 200)
			if (!ignoreDefault)
				assertTrue(
						"The number of tries for file transfer report to display done status should not be more than 200, it is"
								+ numberOfTries, false);
			else
				return null;
		String status = getReportDocumentStatus(page);
		if (status.equals("done"))
			return page;
		else if (status.equals("pending") || status.equals("initiated")) {
			String link = getReportLinkLocation(page, "self",
					"application/atom+xml");
			WebRequest request = new WebRequest(getServiceUrl(link, null),
					HttpMethod.GET);
			request.setAdditionalHeader("Accept", "application/atom+xml");
			XmlPage page2 = null;
			try {
				page2 = webClient.getPage(request);
			} catch (FailingHttpStatusCodeException e) {
				assertTrue(
						"The response code should be ok and not"
								+ e.getStatusCode() + "with message"
								+ e.getMessage(), false);
				return null;
			} catch (IOException e) {
				assertTrue("No connection to the service", false);
				return null;
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return waitForStatusDone(page2, ignoreDefault, numberOfTries + 1);

		} else {
			// status aborted
			if (!ignoreDefault) {
				assertTrue("The status message should not be " + status
						+ " with message" + page.asXml(), false);
			}

			else
				return null;
		}
		return null;

	}

	private XmlPage waitForStatusDone(XmlPage page2) {
		return waitForStatusDone(page2, false, 0);
	}

	private void controleFileTransferReport(XmlPage page,
			String contentDesignation) {
		testReportDocumentMessageContains(
				"has been succesfully registred and indexed", page);

		testReportDocumentMessageContains(contentDesignation, page);

	}

	protected XmlPage askForResourceRepresentation(String resourceLink) {
		WebRequest request = new WebRequest(getServiceUrl(resourceLink, null),
				HttpMethod.GET);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	protected String getEditMediaUri(XmlPage page) {
		String editUri = getAtomLinkLocation(page, "edit-media",
				"application/atom+xml");
		assertTrue("The edit media URI " + editUri
				+ "should be in the domain of edition service : "
				+ editionServiceBaseUrl,
				editUri.startsWith(editionServiceBaseUrl));
		return editUri;
	}

	protected XmlPage getContentThumbsPage(String thumbsUri) {
		WebRequest request = new WebRequest(getServiceUrl(thumbsUri, null),
				HttpMethod.GET);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	protected void deleteResource(XmlPage page) {
		String editUri = getEditUri(page);
		WebRequest request = new WebRequest(getServiceUrl(editUri, null),
				HttpMethod.DELETE);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setAdditionalHeader("If-Match", getAtomUpdatedField(page));
		XmlPage page2 = null;
		try {
			page2 = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}

	}

	/*
	 * Dialog with thumbs service
	 */
	protected XmlPage getThumbsSuggestionForMetaId(String metadataUri) {
		URL url = getServiceUrl("/thumbs/suggestions?mdid=" + metadataUri,
				thumbsServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		WebRequest request = new WebRequest(url, HttpMethod.GET);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	protected XmlPage chooseThumbForMetadataId(String metadataUri,
			String thumbUri, String etag) {
		URL url = getServiceUrl("/edit/thumb?mdid=" + metadataUri + "&src="
				+ thumbUri, thumbsServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		WebRequest request = new WebRequest(url, HttpMethod.PUT);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setAdditionalHeader(HttpHeaders.IF_MATCH, etag);
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message "
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	protected XmlPage askForAutomaticThumbForMetadataId(String metadataUri,
			String etag) {
		URL url = getServiceUrl("/edit/thumb?mdid=" + metadataUri
				+ "&auto=true", thumbsServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		WebRequest request = new WebRequest(url, HttpMethod.PUT);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setAdditionalHeader(HttpHeaders.IF_MATCH, etag);
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message "
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	public XmlPage getMetadata(String urlString, boolean desc) {
		XmlPage page = null;
		if (desc)
			urlString = urlString + "?desc=true";
		URL url = getServiceUrl(urlString, null);

		WebRequest request = new WebRequest(url, HttpMethod.GET);
		request.setAdditionalHeader("accept", "application/xml");
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return page;

	}

	public XmlPage getPage(String urlString) {
		XmlPage page = null;
		URL url = getServiceUrl(urlString, null);

		WebRequest request = new WebRequest(url, HttpMethod.GET);
		request.setAdditionalHeader("accept", "application/xml");
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return page;

	}

	protected String getFirstThumbSuggestionUri(XmlPage contentThumbsPage) {
		Element root = (Element) contentThumbsPage.getDocumentElement();
		NodeList thumbsNodes = root.getElementsByTagName("thumb");
		Element thumb = (Element) thumbsNodes.item(0);
		Element link = (Element) thumb.getFirstChild();
		return link.getAttribute("href");

	}

	protected String getAnyThumbSuggestionUri(XmlPage contentThumbsPage) {
		int length = contentThumbsPage.getDocumentElement()
				.getElementsByTagName("thumb").getLength();
		int number = (int) (length * Math.random());
		Element root = (Element) contentThumbsPage.getDocumentElement();
		NodeList thumbsNodes = root.getElementsByTagName("thumb");
		Element thumb = (Element) thumbsNodes.item(number);
		Element link = (Element) thumb.getFirstChild();
		return link.getAttribute("href");

	}

	protected XmlPage getThumbForMetadataId(String metadataUri) {
		URL url = getServiceUrl("/thumbs?mdids=[\"" + metadataUri + "\"]",
				thumbsServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		WebRequest request = new WebRequest(url, HttpMethod.GET);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	protected void testThumbUriIs(XmlPage page, String thumbUri,
			String metadataLinkLocation) {
		Element root = page.getDocumentElement();
		NodeList thumbsNodes = root.getElementsByTagName("thumb");
		Element thumb = (Element) thumbsNodes.item(0);
		assertTrue(
				"The thumb element mdid attribute should be "
						+ metadataLinkLocation + "and not"
						+ thumb.getAttribute("mdid"), thumb
						.getAttribute("mdid").equals(metadataLinkLocation));
		NodeList linkNodes = thumb.getElementsByTagName("link");
		Element link = (Element) linkNodes.item(0);
		assertTrue("The link element href attribute should be " + thumbUri
				+ "and not" + link.getAttribute("href"),
				link.getAttribute("href").equals(thumbUri));

	}

	protected void testNoMoreThumb(XmlPage page) {
		Element root = page.getDocumentElement();
		NodeList thumbsNodes = root.getElementsByTagName("thumb");
		Element thumb = (Element) thumbsNodes.item(0);
		NodeList linkNodes = thumb.getElementsByTagName("link");
		Element link = (Element) linkNodes.item(0);
		assertTrue("The link element sould have no href attribute, it has :"
				+ link.getAttribute("href"), !link.hasAttribute("href"));

	}

	protected String testDefaultThumbHasuri(XmlPage page) {
		NodeList thumbsList = page.getDocumentElement().getElementsByTagName(
				"thumb");
		for (int i = 0; i < thumbsList.getLength(); i++) {
			Element element = (Element) thumbsList.item(i);
			if (element.getAttribute("status").equals("default")) {
				assertTrue("Default thumb should have uri ",
						StringUtils.isNotEmpty(element.getTextContent()));
				return element.getTextContent().trim();
			}
		}
		assertTrue(
				"The thumb uri was not found in this response of thumb web service "
						+ page.asXml(), false);
		return null;
	}

	protected String getThumbEtag(XmlPage page) {
		Element root = page.getDocumentElement();
		NodeList thumbs = root.getElementsByTagName("thumb");
		for (int i = 0; i < thumbs.getLength(); i++) {
			Element thumbElement = (Element) thumbs.item(i);
			if (!thumbElement.hasAttribute("mdid"))
				continue;
			assertTrue(
					"The thumb element from thumb web service with mdid attribute should have version attribute",
					thumbElement.hasAttribute("version"));
			return thumbElement.getAttribute("version");

		}
		assertTrue(
				"There sould be a thumb element with version and mdid attributes in the thumb service response",
				false);
		return StringUtils.EMPTY;
	}

	protected String getThumbLink(XmlPage page) {
		Element root = page.getDocumentElement();
		NodeList thumbs = root.getElementsByTagName("thumb");
		for (int i = 0; i < thumbs.getLength(); i++) {
			Element thumbElement = (Element) thumbs.item(i);
			if (!thumbElement.hasAttribute("mdid"))
				continue;
			Element linkElement = (Element) thumbElement.getElementsByTagName(
					"link").item(0);
			assertTrue(
					"The link child of thumb element from thumb web service with mdid attribute should have href attribute",
					linkElement.hasAttribute("href"));
			return linkElement.getAttribute("href");

		}
		assertTrue(
				"There sould be a thumb element with version and mdid attributes in the thumb service response",
				false);
		return StringUtils.EMPTY;
	}

	protected HtmlPage getWebPage(String htmlUri) {
		WebRequest request = new WebRequest(getServiceUrl(htmlUri, null),
				HttpMethod.GET);
		request.setAdditionalHeader("Accept", "application/xhtml+xml");
		HtmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;

	}

	protected XmlPage getXMLPage(String xmlUri) {
		WebRequest request = new WebRequest(getServiceUrl(xmlUri, null),
				HttpMethod.GET);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + "with message"
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;

	}

	String getEditUri(XmlPage page) {
		String editUri = getAtomLinkLocation(page, "edit",
				"application/atom+xml");
		assertTrue("The edit  URI " + editUri
				+ "should be in the domain of edition service : "
				+ editionServiceBaseUrl,
				editUri.startsWith(editionServiceBaseUrl));
		return editUri;
	}

	protected XmlPage deleteMetadataEntry(String uri, String updated) {
		WebRequest request = new WebRequest(getServiceUrl("/edit/meta",
				editionServiceBaseUrl), HttpMethod.DELETE);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair("mdid", uri));
		request.setRequestParameters(list);
		request.setAdditionalHeader("Accept", "application/atom+xml");
		request.setAdditionalHeader("If-Match", updated);
		XmlPage page = null;
		try {
			page = webClient.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			assertTrue(
					"The response code should be ok and not"
							+ e.getStatusCode() + " withmessage "
							+ e.getMessage(), false);
		} catch (IOException e) {
			assertTrue("No connection to the service", false);
		}
		return page;
	}

	public XmlPage postingImsLdFile1(File tempDir) {
		return postingImsLdFile1(tempDir, false);
	}

	public XmlPage postingImsLdFile1(File tempDir, boolean automatedThumbs) {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);

		Document manifest = loadManifest(tempDir);
		extractLearningObjects(manifest, tempDir, url, automatedThumbs);
		extractOtherResources(manifest, tempDir);
		extractPlayMetadata(manifest, tempDir, url);
		File syntheticManifest = dumpXMLToFile(tempDir, manifest);
		System.out.println("manifest synthetic=" + syntheticManifest);
		URL url2 = getServiceUrl("/edit/manifest", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		XmlPage packResponse = postImsLdDocument(syntheticManifest, url2);
		// System.out.println(packResponse.asXml());
		// tempDir.delete();
		return packResponse;

	}

	protected void extractPlayMetadata(Document manifest, File tempDir, URL url) {
		Element root = manifest.getDocumentElement();
		Element learningDesign = (Element) root.getElementsByTagName(
				"imsld:learning-design").item(0);
		NodeList learningDesignChilds = learningDesign.getChildNodes();
		int numberOfMetadata = 0;

		Element metadata = null;
		Node child;
		for (int i = 0; i < learningDesignChilds.getLength(); i++) {

			child = learningDesignChilds.item(i);
			if (!(child instanceof Element))
				continue;
			if (child.getNodeName().equals("imsld:metadata")) {
				numberOfMetadata++;
				metadata = (Element) child;
			}
		}
		assertTrue(
				"There should be 1 metadata element for learning design and not "
						+ numberOfMetadata, numberOfMetadata == 1);
		sendAsMetadataFile(metadata, tempDir, url, root, manifest);
	}

	protected void extractOtherResources(Document manifest, File tempDir) {
		Element root = manifest.getDocumentElement();
		NodeList resources = root.getElementsByTagName("resource");
		for (int i = 0; i < resources.getLength(); i++) {
			Element resourceElement = (Element) resources.item(i);
			if (resourceElement.getElementsByTagName("file").getLength() == 0)
				continue;
			XmlPage newResourcePage = getNewResourcePage("no-metadata");
			String urn = getAtomId(newResourcePage);
			String etag = getAtomUpdatedField(newResourcePage);
			String editUri = getEditMediaUri(newResourcePage);
			String contentLinkLocation = getAtomLinkLocation(newResourcePage,
					"self", "text/html");
			addFilesToResource(urn, editUri, "no-metadata", resourceElement,
					tempDir, etag);
			resourceElement.setAttribute("identifier", contentLinkLocation);
			XmlPage resourcePage = getPage(contentLinkLocation);
			resourceElement.setAttribute(
					"href",
					getFileDownloadUrl(getMainFileForRessource(resourcePage),
							resourcePage));
		}

	}

	protected void extractLearningObjects(Document manifest, File tempDir,
			URL url, boolean automaticThumbs) {

		Element root = manifest.getDocumentElement();
		NodeList los = root.getElementsByTagName("imsld:learning-object");
		for (int i = 0; i < los.getLength(); i++) {
			Element lo = (Element) los.item(i);
			Element item = (Element) lo.getElementsByTagName("imsld:item")
					.item(0);
			String identifierRef = item.getAttribute("identifierref");
			Element metadata = ((Element) lo.getElementsByTagName(
					"imsld:metadata").item(0));
			XmlPage page = sendAsMetadataFile(metadata, tempDir, url, root,
					manifest);
			String metadataLinkLocation = getAtomLinkLocation(page, "self",
					"text/html");
			Element resourceElement = getResourceElement(identifierRef,
					manifest);
			Boolean isRemote = resourceIsRemote(resourceElement);
			// create the new resource
			XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation,
					isRemote ? "url" : "asset");
			String urn = getAtomId(newResourcePage);
			String editUri = getEditMediaUri(newResourcePage);
			String contentLinkLocation = getAtomLinkLocation(newResourcePage,
					"self", "text/html");
			String eTag = getAtomUpdatedField(newResourcePage);
			if (isRemote)
				postUrlContent(editUri, urn,
						resourceElement.getAttribute("href"), eTag);
			addFilesToResource(urn, editUri, metadataLinkLocation,
					resourceElement, tempDir, eTag);
			resourceElement.setAttribute("identifier", contentLinkLocation);
			item.setAttribute("identifierref", contentLinkLocation);
			XmlPage resourcePage = getPage(contentLinkLocation);
			if (isRemote)
				resourceElement.setAttribute("href",
						getWebPageFullyQualifiedUrl(resourcePage));
			else
				resourceElement.setAttribute(
						"href",
						getFileDownloadUrl(
								getMainFileForRessource(resourcePage),
								resourcePage));
			XmlPage page4 = getThumbForMetadataId(metadataLinkLocation);
			askForAutomaticThumbForMetadataId(metadataLinkLocation,
					getThumbEtag(page4));

		}

	}

	private XmlPage sendAsMetadataFile(Element metadata, File tempDir, URL url,
			Element root, Document manifest) {
		Element localMetadata = ((Element) metadata.getElementsByTagName("lom")
				.item(0));
		Document newXmlDocument = null;
		try {
			newXmlDocument = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element newRoot = newXmlDocument.createElement("lom");
		newRoot.setAttribute("xmlns", "http://ltsc.ieee.org/xsd/LOM");
		newRoot.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		newRoot.setAttribute("xsi:schemaLocation",
				"http://ltsc.ieee.org/xsd/LOM http://lom-fr.fr/xsd/lomfrv1.0/std/lomfr.xsd");
		newRoot.setAttribute("xmlns:lomfr", "http://www.lom-fr.fr/xsd/LOMFR");
		newRoot.setAttribute("xmlns:scolomfr",
				"http://www.lom-fr.fr/xsd/SCOLOMFR");
		root.setAttribute("xmlns:adlcp", "http://www.adlnet.org/xsd/adlcp_v1p3");
		newRoot.setAttribute("xmlns:adlcp",
				"http://www.adlnet.org/xsd/adlcp_v1p3");
		newXmlDocument.appendChild(newRoot);
		NodeList nodes = localMetadata.getChildNodes();
		for (int j = 0; j < nodes.getLength(); j++) {
			Node node = nodes.item(j);
			Node copyNode = newXmlDocument.importNode(node, true);
			newRoot.appendChild(copyNode);
		}
		File tempXMLFile = dumpXMLToFile(tempDir, newXmlDocument);
		XmlPage page = postMetadataDocument(tempDir.getName() + File.separator
				+ tempXMLFile.getName(), url);
		String lomLinkLocation = getAtomLinkLocation(page, "describedby",
				"application/lom+xml");
		Node remoteMetadata = manifest.createElement("adlcp:location");
		remoteMetadata.setTextContent(lomLinkLocation);
		metadata.replaceChild(remoteMetadata, localMetadata);
		return page;
	}

	private void addFilesToResource(String urn, String editUri,
			String linkLocation, Element resourceElement, File tempDir,
			String eTag) {
		NodeList files = resourceElement.getElementsByTagName("file");
		for (int i = 0; i < files.getLength(); i++) {
			Element fileElement = (Element) files.item(i);
			String fileName = fileElement.getAttribute("href");
			XmlPage page = postFileDocument(editUri, urn, tempDir.getName()
					+ File.separator + fileName, eTag);
			// TODO mark as main if...
			resourceElement.removeChild(fileElement);
		}

	}

	private Boolean resourceIsRemote(Element resourceElement) {
		NodeList files = resourceElement.getElementsByTagName("file");
		if (files.getLength() > 0)
			return false;
		assertTrue(
				"If this resource has no files, it sould have an href attribute",
				resourceElement.hasAttribute("href"));
		String href = resourceElement.getAttribute("href");
		assertTrue(
				"If this resource has no files, its href attribute should contain a fully qualified URL and not "
						+ href, href.startsWith("http"));
		return true;

	}

	private Element getResourceElement(String identifierRef, Document manifest) {
		Element root = manifest.getDocumentElement();
		NodeList resources = root.getElementsByTagName("resource");
		for (int i = 0; i < resources.getLength(); i++) {
			Element resource = (Element) resources.item(i);
			String identifier = resource.getAttribute("identifier");
			if (identifier.equals(identifierRef))
				return resource;
		}
		return null;
	}

	protected File dumpXMLToFile(File tempDir, Document xmlDocument) {
		UUID randomUUID = UUID.randomUUID();
		File tempOutput = new File(tempDir.getAbsolutePath() + File.separator
				+ randomUUID + ".xml");
		try {
			tempOutput.createNewFile();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DOMSource source = new DOMSource(xmlDocument);
		StreamResult result = new StreamResult(tempOutput);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempOutput;
	}

	protected Document loadManifest(File tempDir) {
		File fXmlFile = new File(tempDir.getAbsolutePath() + File.separator
				+ "imsmanifest.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = dBuilder.parse(fXmlFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}

	protected File unzipImsPackage(String zipFileName) {

		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			File outputFolder = new File(testDataDirectory + File.separator
					+ UUID.randomUUID().toString());
			outputFolder.mkdir();

			File zipFile = new File(testDataDirectory + File.separator
					+ zipFileName);
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator
						+ fileName);
				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
			return outputFolder;

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	protected void waitDuring(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
