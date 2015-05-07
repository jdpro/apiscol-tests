package fr.ac_versailles.crdp.apiscol.tests.gp1;

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
public class TagNameTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testTagName() {
		File tempDir = unzipImsPackage("solution_aqueuse_imsld.zip");
		XmlPage packResponse = postingImsLdFile1(tempDir);
		assertTrue("The pack gives a response", packResponse != null);
		System.out.println("***Checking <tags> name in the pack response***");
		Element root = getAtomDocumentRootItem(packResponse);
		String rootName = root.getTagName();
		assertTrue("The root name should be <entry>, not " + rootName,
				rootName.equals("entry"));
		NodeList nl = root.getChildNodes();
		assertTrue(
				"There should be 11 childs under <entry> and here there are "
						+ nl.getLength(), nl.getLength() == 11);
		String[] tabNameNl = { "updated", "id", "author", "title", "content",
				"link", "link", "link", "link", "link", "link" };
		for (int i = 0; i < nl.getLength(); i++) {
			String name = nl.item(i).getNodeName();
			assertTrue("the node number " + (i + 1) + " name should be <"
					+ tabNameNl[i] + ">, not " + name,
					name.equals(tabNameNl[i]));
		}
		System.out.println("***end of <tags> name check***");
		tempDir.delete();
	}
}
