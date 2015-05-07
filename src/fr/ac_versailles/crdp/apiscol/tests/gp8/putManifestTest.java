package fr.ac_versailles.crdp.apiscol.tests.gp8;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

@Ignore
public class putManifestTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testUpdateManifestWithPut() {
		URL url = getServiceUrl("/edit/manifest", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		System.out.println("TEST: putManifest");
		XmlPage packResponse = putManifest(
				"https://localhost:8443/edit/manifest/9c369e80-06ab-4e98-aeb3-cf9824e28786",
				"imsmanifest.xml");

		assertTrue("The pack gives a response", packResponse != null);

	}
}
