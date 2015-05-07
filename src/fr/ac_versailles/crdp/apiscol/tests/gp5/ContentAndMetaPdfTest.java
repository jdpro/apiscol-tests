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
public class ContentAndMetaPdfTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingPdfDocumentWithMetadata() {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		XmlPage page = postMetadataDocument("appel-hanovre-87451.xml", url);
		String metadataLinkLocation = getAtomLinkLocation(page, "self",
				"text/html");
		XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation);
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		String eTag = getAtomUpdatedField(newResourcePage);
		XmlPage page2 = postFileDocument(editUri, urn, "hancall_fr.pdf", eTag);
		XmlPage page3 = getMetadata(metadataLinkLocation, true);
		String titleString = "L'appel de Hanovre - 11 février 2000";
		testAtomDocumentTitleIs(titleString, page3);
		String summaryExtract = "Appel lancé par les maires européens à l'aube du XXIe siècle. Contient des principes et valeurs pour l'action locale vers la durabilité. Met en avant le rôle prépondérant des maires européens";
		testAtomDocumentSummaryContains(summaryExtract, page3);
		// inutile si pas previews
		// waitDuring(45000);
		// XmlPage page4 = getThumbsSuggestionForMetaId(metadataLinkLocation);
		// String firstThumbSuggestionUri = getFirstThumbSuggestionUri(page4);
		// assertTrue("The first thumb suggestion may not be empty for metadata "
		// + metadataLinkLocation,
		// StringUtils.isNotEmpty(firstThumbSuggestionUri));
		// XmlPage page5 = chooseThumbForMetadataId(metadataLinkLocation,
		// firstThumbSuggestionUri, getThumbEtag(page4));
		// String thumbUri = testDefaultThumbHasuri(page5);
		// XmlPage page6 = getThumbForMetadataId(metadataLinkLocation);
		// testThumbUriIs(page6, thumbUri, metadataLinkLocation);

		// deleteResource(page);
	}

}
