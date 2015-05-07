package fr.ac_versailles.crdp.apiscol.tests.gp1;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

@Ignore
public class PreviewGenerationTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void postPdfAndCheckPreview() {
		postContentAndMetadataAndCheckPreview("cuisson1.xml",
				"allais_contes_humoristiques_1.pdf");
	}

	@Test
	public void postMsDocAndCheckPreview() {
		postContentAndMetadataAndCheckPreview("cuisson1.xml", "apollinaire.doc");
	}

	private void postContentAndMetadataAndCheckPreview(String metadataFileName,
			String resourceFileName) {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		XmlPage metadataPage = postMetadataDocument(metadataFileName, url);
		String metadataLinkLocation = getAtomLinkLocation(metadataPage, "self",
				"text/html");
		XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation);
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		String eTag = getAtomUpdatedField(newResourcePage);
		XmlPage page2 = postFileDocument(editUri, urn, resourceFileName, eTag);
		XmlPage page3 = askForResourceRepresentation(metadataLinkLocation);
		try {
			Thread.sleep(45000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String htmlUri = getAtomLinkLocation(page3, "self", "text/html");
		HtmlPage webPage = getWebPage(htmlUri);
		List<HtmlElement> divs = webPage.getDocumentElement()
				.getElementsByTagName("div");
		assertTrue("There should be at least one div in response page !",
				divs.size() > 0);
		boolean previewAreaFound = false;
		String previewUri = null;
		for (Iterator<HtmlElement> iterator = divs.iterator(); iterator
				.hasNext();) {
			HtmlElement htmlElement = (HtmlElement) iterator.next();
			if (htmlElement.hasAttribute("class")
					&& htmlElement.getAttribute("class").equals("preview-area")) {
				previewAreaFound = true;
				DomNodeList<HtmlElement> links = htmlElement
						.getElementsByTagName("a");
				assertTrue(
						"There should be only one link as child of class preview-area div.",
						links.size() == 1);
				HtmlElement link = (HtmlElement) links.item(0);
				previewUri = link.getAttribute("href");
			}
		}
		assertTrue(
				"There should be a  div of class preview area in resource html page !",
				previewAreaFound);
		assertTrue("Preview link sould not be empty",
				StringUtils.isNotEmpty(previewUri));
		HtmlPage previewPage = getWebPage(previewUri);
		// previewPage.setStrictErrorChecking(false);
		List<HtmlElement> imgs = previewPage.getDocumentElement()
				.getElementsByTagName("img");
		assertTrue(
				"There should be div in response page and not " + imgs.size(),
				imgs.size() == 10);
		for (Iterator<HtmlElement> iterator = imgs.iterator(); iterator
				.hasNext();) {
			HtmlElement htmlElement = (HtmlElement) iterator.next();
			assertTrue("src attribute of this image should end with png "
					+ htmlElement.asXml(), htmlElement.getAttribute("src")
					.endsWith(".png"));
		}
		deleteResource(page2);
		XmlPage newMetadataPage = getMetadata(metadataLinkLocation, false);
		deleteMetadataEntry(metadataLinkLocation,
				getAtomUpdatedField(newMetadataPage));

	}
}
