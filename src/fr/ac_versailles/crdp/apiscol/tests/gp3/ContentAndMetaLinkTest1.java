package fr.ac_versailles.crdp.apiscol.tests.gp3;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ContentAndMetaLinkTest1 extends ApiScolTests {
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
		String titleString = "Méthodologie de l'enquête statistique";
		testAtomDocumentTitleIs(titleString, page3);
		String summaryExtract = "Introduction générale à l'enquête statistique. La démarche expérimentale en sciences sociales";
		testAtomDocumentSummaryContains(summaryExtract, page3);
		// deleteResource(page);
	}

}
