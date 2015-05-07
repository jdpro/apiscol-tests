package fr.ac_versailles.crdp.apiscol.tests.gp5;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ContentLanguageDetectionTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingGreekDocumentAndCheckMetadata() {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		XmlPage page = postMetadataDocument("cuisson1.xml", url);
		String metadataLinkLocation = getAtomLinkLocation(page, "self",
				"text/html");

		checkLanguageOfFile("languages/german.docx", metadataLinkLocation, "de");
		checkLanguageOfFile("languages/nederlands.pdf", metadataLinkLocation,
				"nl");
		checkLanguageOfFile("languages/greek.doc", metadataLinkLocation, "el");

		XmlPage newMetadataPage = getMetadata(metadataLinkLocation, false);
		deleteMetadataEntry(metadataLinkLocation,
				getAtomUpdatedField(newMetadataPage));

	}

	private void checkLanguageOfFile(String filePath,
			String metadataLinkLocation, String language) {
		XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation);
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		String eTag = getAtomUpdatedField(newResourcePage);
		XmlPage page2 = postFileDocument(editUri, urn, filePath, eTag);
		waitDuring(5000);
		XmlPage page3 = askForResourceRepresentation(metadataLinkLocation);
		String lomLocation = getAtomLinkLocation(page3, "describedby",
				"application/lom+xml");
		XmlPage lomDocument = getPage(lomLocation);
		testResourceLanguageIs(lomDocument, language);
		deleteResource(page2);

	}

}
