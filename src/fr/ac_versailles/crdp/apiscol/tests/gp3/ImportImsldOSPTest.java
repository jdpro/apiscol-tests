package fr.ac_versailles.crdp.apiscol.tests.gp3;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

public class ImportImsldOSPTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingImsLdFile1() {
		File tempDir = unzipImsPackage("lydia.zip");
		XmlPage packResponse = postingImsLdFile1(tempDir);
		assertTrue("The pack gives a response", packResponse != null);
		tempDir.delete();
	}
}
