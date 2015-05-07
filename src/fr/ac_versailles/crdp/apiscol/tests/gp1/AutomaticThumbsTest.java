package fr.ac_versailles.crdp.apiscol.tests.gp1;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

@Ignore
public class AutomaticThumbsTest extends ApiScolTests {
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
		postContentAndMetadataAndCheckPreview("enqstat.xml",
				"methodo_enquete_stat.pptx");
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
		newResourcePage = postFileDocument(editUri, urn, resourceFileName, eTag);
		XmlPage page2 = getThumbForMetadataId(metadataLinkLocation);
		askForAutomaticThumbForMetadataId(metadataLinkLocation,
				getThumbEtag(page2));

		String thumbLink;
		page2 = getThumbForMetadataId(metadataLinkLocation);
		thumbLink = getThumbLink(page2);
		deleteResource(newResourcePage);
		XmlPage newMetadataPage = getMetadata(metadataLinkLocation, false);
		deleteMetadataEntry(metadataLinkLocation,
				getAtomUpdatedField(newMetadataPage));
		assertTrue("The thumb web service should have returned an uri ",
				StringUtils.isNotEmpty(thumbLink));
	}

	private String downloadFile(String metadataLinkLocation) {
		WebRequest request = null;
		InputStream inputStream = null;
		try {
			request = new WebRequest(new URL(metadataLinkLocation));
		} catch (MalformedURLException e) {
			assertTrue("The submitted uri is not valid :"
					+ metadataLinkLocation, false);
		}
		try {
			inputStream = new HttpWebConnection(webClient).getResponse(request)
					.getContentAsStream();
		} catch (IOException e) {
			assertTrue("Impossible to download file from this uri :"
					+ metadataLinkLocation, false);
		}
		String md5_1 = null;
		try {
			md5_1 = org.apache.commons.codec.digest.DigestUtils
					.md5Hex(inputStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return md5_1;
	}
}
