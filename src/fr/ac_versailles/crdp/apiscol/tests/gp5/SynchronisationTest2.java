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
public class SynchronisationTest2 extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingRtfDocumentWithMetadata() {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		XmlPage page = postMetadataDocument("eau_dans_tous_ses_etats.xml", url);
		String metadataLinkLocation = getAtomLinkLocation(page, "self",
				"text/html");
		XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation);
		String urn = getAtomId(newResourcePage);
		String editMediaUri = getEditMediaUri(newResourcePage);
		String eTag = getAtomUpdatedField(newResourcePage);
		XmlPage page2 = postFileDocument(editMediaUri, urn,
				"eau_dans_tous_ses_etats.rtf", eTag);
		waitDuring(45000);
		String mainFileUrl = getFileDownloadUrl(getMainFileForRessource(page2),
				page2);
		XmlPage page3 = getMetadata(metadataLinkLocation, true);
		String titleString = "L'eau dans tous ses états";
		testAtomDocumentTitleIs(titleString, page3);
		String summaryExtract = "dans le système solaire, la température, la pression";
		testAtomDocumentSummaryContains(summaryExtract, page3);
		// ne fonctionne qu'avec preview activé.
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
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

		// test synchronisation
		testResourceContentSrcEndsWidth("eau_dans_tous_ses_etats.rtf", page3);
		String lomLocation = getAtomLinkLocation(page3, "describedby",
				"application/lom+xml");
		XmlPage lomDocument = getPage(lomLocation);
		testTechnicalFormatIs("application/rtf", lomDocument);
		testTechnicalLocationIs(mainFileUrl, lomDocument);
		testTechnicalSizeIs("834", lomDocument);
		// deleteResource(page);
	}

}
