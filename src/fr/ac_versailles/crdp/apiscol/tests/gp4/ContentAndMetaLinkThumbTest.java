package fr.ac_versailles.crdp.apiscol.tests.gp4;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ContentAndMetaLinkThumbTest extends ApiScolTests {
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
				"http://www.poisson-or.com/", getAtomUpdatedField(newResourcePage));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// test preview
		XmlPage page4 = getThumbsSuggestionForMetaId(metadataLinkLocation);
		String firstThumbSuggestionUri = getAnyThumbSuggestionUri(page4);
		assertTrue("The thumb suggestion may not be empty for metadata "
				+ metadataLinkLocation,
				StringUtils.isNotEmpty(firstThumbSuggestionUri));
		XmlPage page5 = chooseThumbForMetadataId(metadataLinkLocation,
				firstThumbSuggestionUri, getThumbEtag(page4));
		String thumbUri = testDefaultThumbHasuri(page5);
		XmlPage page6 = getThumbForMetadataId(metadataLinkLocation);
		testThumbUriIs(page6, thumbUri, metadataLinkLocation);

		// deleteResource(page);
	}

}
