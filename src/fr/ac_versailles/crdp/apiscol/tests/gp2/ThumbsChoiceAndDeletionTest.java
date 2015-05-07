package fr.ac_versailles.crdp.apiscol.tests.gp2;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ThumbsChoiceAndDeletionTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingDocumentWithMetadataChoosingThumbAndDeletingMeta() {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		XmlPage metadataPage = postMetadataDocument("appel-hanovre-87451.xml",
				url);
		String metadataLinkLocation = getAtomLinkLocation(metadataPage, "self",
				"text/html");
		XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation);
		String eTag = getAtomUpdatedField(newResourcePage);
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		postFileDocument(editUri, urn, "hancall_fr.pdf", eTag);
		waitDuring(45000);
		XmlPage page4 = getThumbsSuggestionForMetaId(metadataLinkLocation);
		String firstThumbSuggestionUri = getFirstThumbSuggestionUri(page4);
		assertTrue("The first thumb suggestion may not be empty for metadata "
				+ metadataLinkLocation,
				StringUtils.isNotEmpty(firstThumbSuggestionUri));
		XmlPage page5 = chooseThumbForMetadataId(metadataLinkLocation,
				firstThumbSuggestionUri, getThumbEtag(page4));
		String thumbUri = testDefaultThumbHasuri(page5);
		XmlPage page6 = getThumbForMetadataId(metadataLinkLocation);
		testThumbUriIs(page6, thumbUri, metadataLinkLocation);
		XmlPage page3 = getMetadata(metadataLinkLocation, true);
		deleteResource(page3);
		waitDuring(5000);		
		XmlPage page7 = getThumbForMetadataId(metadataLinkLocation);
		testNoMoreThumb(page7);
	}

}
