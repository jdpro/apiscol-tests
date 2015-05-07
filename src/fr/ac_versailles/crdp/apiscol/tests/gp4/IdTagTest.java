package fr.ac_versailles.crdp.apiscol.tests.gp4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

@Ignore
public class IdTagTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testIdTag() {
		System.out.println("***begin of testIdTag (ApiscolInstance name)***");
		File tempDir = unzipImsPackage("solution_aqueuse_imsld.zip");
		XmlPage packResponse = postingImsLdFile1(tempDir);
		assertTrue("The pack gives a response", packResponse != null);
		Element root = getAtomDocumentRootItem(packResponse);
		NodeList nlId = root.getElementsByTagName("id");
		assertTrue("The pack should have one <id> tag", nlId.getLength() == 1);
		String idValue = nlId.item(0).getTextContent();
		assertFalse("ApiscolInstance can't be null.", idValue.contains("null"));
		assertTrue("urn should contain :pack:", idValue.contains(":pack:"));
		System.out.println("***end of testIdTag***");
		// tempDir.delete();
	}
}
