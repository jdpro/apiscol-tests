package fr.ac_versailles.crdp.apiscol.tests.gp4;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ContentTest extends ApiScolTests {

	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testPostingMsDocDocuments() {
		XmlPage newResourcePage = getNewResourcePage("bad metadata");
		String eTag = getAtomUpdatedField(newResourcePage);
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		XmlPage page = postFileDocument(editUri, urn, 
				"apollinaire.doc", eTag);
		deleteResource(page);
	}

	@Test
	public void testPostingUbzDocuments() {
		XmlPage newResourcePage = getNewResourcePage("bad metadata2");
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		String eTag = getAtomUpdatedField(newResourcePage);
		XmlPage page = postFileDocument(editUri, urn, 
				"Mode de cuisson sauter Cuisson sauter-01. Le contexte.ubz", eTag);
		String thumbsUri = getAtomLinkLocation(page, "icon",
				"application/atom+xml");
		XmlPage contentThumbsPage = getContentThumbsPage(thumbsUri);
		testThumbListContains("button_over.png", contentThumbsPage);
		deleteResource(page);
	}

	void testThumbListContains(String iconName, XmlPage contentThumbsPage) {

		Element root = (Element) contentThumbsPage.getChildNodes().item(0);
		assertTrue("There was no thumbs root Element ", root.getLocalName()
				.equals("thumbs"));
		testNodeHasApiscolNameSpace(root);
		NodeList thumbsNodes = root.getElementsByTagName("thumb");
		assertTrue(
				"There should be at list one thumb node in content response !",
				thumbsNodes.getLength() > 0);
		testNodeHasApiscolNameSpace((Element) thumbsNodes.item(0));
		for (int i = 0; i < thumbsNodes.getLength(); i++) {
			Element thumb = (Element) thumbsNodes.item(i);
			Element link = (Element) thumb.getFirstChild();
			assertTrue("Thumb node n° " + i + " should have a link child",
					link != null && link.getLocalName().equals("link"));
			String href = link.getAttribute("href");
			assertTrue("Thumb node n° " + i + "should have an href attribute",
					StringUtils.isNotEmpty(href));
			if (href.contains(iconName))
				return;
		}
		assertTrue(
				"Icon " + iconName + " was not found in content thumbs list",
				false);
	}

}
