package fr.ac_versailles.crdp.apiscol.tests.gp2;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class MetadataTest extends ApiScolTests {

	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingDocuments() {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		postDocumentWithSeveralFormats(url);
	}

	@Test
	public void testMaintenance() {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);

		XmlPage result1 = postMaintenanceRequest("meta", "optimization");
		testReportDocumentStatusIsDone(result1);
		testReportDocumentMessageContains(
				"Search engine index has been optimized", result1);
		XmlPage result2 = postMaintenanceRequest("meta", "recovery");
		testReportDocumentStatusIsDone(result2);
		testReportDocumentMessageContains(
				"Search engine index has been restored", result2);
		if (overallDeletionAuthorized) {
			XmlPage result3 = postMaintenanceRequest("meta", "deletion");
			testReportDocumentStatusIsDone(result3);
			testReportDocumentMessageContains(
					"All resource have been deleted in metadata repository",
					result3);
		}
	}

	private void postDocumentWithSeveralFormats(URL url) {

		XmlPage page = postMetadataDocument("several_formats.xml", url);
		String titleString = "Darwin";
		testAtomDocumentTitleIs(titleString, page);
		String summaryExtract = "la théorie de Darwin et le développement de cette théorie depuis Darwin : apport de connaissances, matériel de cours, ";
		testAtomDocumentSummaryContains(summaryExtract, page);
		testDocumentLinksAre(page, 7);
		String linkLocation = getAtomLinkLocation(page, "self", "text/html");
		page = getMetadata(linkLocation, true);
		testAtomRootElement(page);

		testAtomDocumentTitleIs(titleString, page);
		testAtomDocumentSummaryContains(summaryExtract, page);
		String author = "Machin Bidule";
		testAtomDocumentHasAuthorTag(page);
		testAtomDocumentAuthorsContains(author, page);
		String linkLocation2 = getAtomLinkLocation(page, "self", "text/html");
		assertTrue(
				"The link location are not the same with post and get request : "
						+ linkLocation + " is not " + linkLocation2,
				linkLocation.equals(linkLocation2));
		XmlPage page2 = deleteMetadataEntry(linkLocation2,
				getAtomUpdatedField(page));
	}

}
