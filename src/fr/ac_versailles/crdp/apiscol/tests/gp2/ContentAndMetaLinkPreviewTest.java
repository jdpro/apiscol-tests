package fr.ac_versailles.crdp.apiscol.tests.gp2;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ContentAndMetaLinkPreviewTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingLinkWithMetadata() {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		XmlPage page = postMetadataDocument("enqstat.xml", url);
		String metadataLinkLocation = getAtomLinkLocation(page, "self",
				"text/html");
		XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation,
				"url");
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		XmlPage page2 = postUrlContent(editUri, urn,
				"http://www.poisson-or.com/",
				getAtomUpdatedField(newResourcePage));

		XmlPage page3 = getMetadata(metadataLinkLocation, true);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// test preview
		String previewLink = getAtomLinkLocation(page2, "preview", "text/html");
		HtmlPage previewHtmlPage = getWebPage(previewLink);
		String htmlRepresentationLink = getAtomLinkLocation(page3, "self",
				"text/html");
		HtmlPage htmlRepresentationPage = getWebPage(htmlRepresentationLink);
		DomNodeList<DomElement> divs = htmlRepresentationPage
				.getElementsByTagName("div");
		boolean previewDivFound = false;
		for (Iterator<DomElement> iterator = divs.iterator(); iterator
				.hasNext();) {
			DomElement domElement = iterator.next();
			if (StringUtils.contains(domElement.getAttribute("class"),
					"preview-area")) {
				previewDivFound = true;
				assertTrue(
						"The first div of the prewiew shoud be a link with link to preview url "
								+ previewLink + ", it is "
								+ domElement.getFirstElementChild().asXml(),
						domElement.getFirstElementChild().getAttribute("href")
								.contains(previewLink));
			}

		}
		assertTrue("No preview area div was found in web page "
				+ htmlRepresentationLink, previewDivFound);
		deleteResource(page2);
		XmlPage newMetadataPage = getMetadata(metadataLinkLocation, false);
		deleteMetadataEntry(metadataLinkLocation,
				getAtomUpdatedField(newMetadataPage));
	}

}
