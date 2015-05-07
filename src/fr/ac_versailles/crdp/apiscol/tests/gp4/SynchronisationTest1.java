package fr.ac_versailles.crdp.apiscol.tests.gp4;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class SynchronisationTest1 extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingRdfDocumentWithMetadataAndChangingMeta() {
		URL url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);

		XmlPage newResourcePage = getNewResourcePage("bad-metadata");
		String urn = getAtomId(newResourcePage);
		String editMediaUri = getEditMediaUri(newResourcePage);
		String eTag = getAtomUpdatedField(newResourcePage);
		XmlPage page2 = postFileDocument(editMediaUri, urn,
				"eau_dans_tous_ses_etats.rtf", eTag);
		XmlPage page = postMetadataDocument("eau_dans_tous_ses_etats.xml", url);
		String metadataLinkLocation = getAtomLinkLocation(page, "self",
				"text/html");
		String editUri = getAtomLinkLocation(newResourcePage, "edit",
				"application/atom+xml");
		String eTag2 = getAtomUpdatedField(page2);
		XmlPage sendingMetaResponsePage = sendMetaForResource(editUri,
				metadataLinkLocation, eTag2);
		String mainFileUrl = getFileDownloadUrl(getMainFileForRessource(sendingMetaResponsePage),
				sendingMetaResponsePage);
		String metaLocationRenewed = getAtomLinkLocation(
				sendingMetaResponsePage, "describedby", "application/atom+xml");
		assertTrue("The describedby link for this resource should now be "
				+ metadataLinkLocation + " and not " + metaLocationRenewed,
				metaLocationRenewed.equals(metadataLinkLocation));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XmlPage page3 = getMetadata(metadataLinkLocation, true);
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
