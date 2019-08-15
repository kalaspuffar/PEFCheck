package se.mtm;

import com.sun.media.sound.InvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PEFCheckTest {

    @DisplayName("Test that an incorrect page number returns -1")
    @Test
    public void testIncorrectPageNumber() {
        PEFCheck pefValidator = new PEFCheck();
        assertEquals(-1, pefValidator.getPageNumber("dsjiads"), "Handle incorrect number");
        assertEquals(-1, pefValidator.getPageNumber("#dsqjiads"), "Handle incorrect number");
    }

    @DisplayName("Test that correct numbers return their values")
    @Test
    public void testCorrectPageNumber() {
        PEFCheck pefValidator = new PEFCheck();
        assertEquals(1, pefValidator.getPageNumber("#a"), "Can handle one number");
        assertEquals(5, pefValidator.getPageNumber("#e"), "Can handle another number");
        assertEquals(10, pefValidator.getPageNumber("#aj"), "Can handle more than one number");
        assertEquals(1000, pefValidator.getPageNumber("#ajjj"), "Can handle thousends");
        assertEquals(1337, pefValidator.getPageNumber("#accg"), "Can be leet.");
    }

    @DisplayName("Test that an incorrect roman numerals returns -1")
    @Test
    public void testIncorrectRomanNumerals() {
        PEFCheck pefValidator = new PEFCheck();
        assertEquals(-1, pefValidator.getPageNumber("_ii"), "Handle incorrect number");
        assertEquals(-1, pefValidator.getPageNumber("__i"), "Handle incorrect number");
    }

    @DisplayName("Test that we can handle roman numerals")
    @Test
    void testCorrectRomanNumerals() {
        PEFCheck pefValidator = new PEFCheck();
        assertEquals(1, pefValidator.getPageNumber("_i"), "Can handle one");
        assertEquals(5, pefValidator.getPageNumber("_v"), "Can handle five");
        assertEquals(10, pefValidator.getPageNumber("_x"), "Can handle more than one number");
        assertEquals(50, pefValidator.getPageNumber("_l"), "Can handle fifty");
        assertEquals(100, pefValidator.getPageNumber("_c"), "Can handle hundreds.");
        assertEquals(500, pefValidator.getPageNumber("_d"), "Can handle five hundreds.");
        assertEquals(1000, pefValidator.getPageNumber("_m"), "Can handle thousands.");
        assertEquals(1337, pefValidator.getPageNumber("__mcccxxxvii"), "Can be leet.");
        assertEquals(1771, pefValidator.getPageNumber("__mdcclxxi"), "Can handle an important year");
        assertEquals(2019, pefValidator.getPageNumber("__mmxix"), "Can handle this year");
    }

    @DisplayName("Test that we can extract index pages")
    @Test
    public void testExtractingIndexPages() {
        PEFCheck pefValidator = new PEFCheck();
        PageIdentifiers pageIdentifiers = pefValidator.getPageIdentifiers(
            "                            _i", false, true
        );

        assertEquals(1, pageIdentifiers.getPefPage(), "Handle the first index page");

        pageIdentifiers = pefValidator.getPageIdentifiers(
            "    __ii", true, true
        );

        assertEquals(2, pageIdentifiers.getPefPage(), "Handle the second index page");

        pageIdentifiers = pefValidator.getPageIdentifiers(
            "    __ix", true, true
        );

        assertEquals(9, pageIdentifiers.getPefPage(), "Handle the ninth index page");
    }

    @DisplayName("Test that we can extract normal pages")
    @Test
    public void testExtractingNormalPages() {
        PEFCheck pefValidator = new PEFCheck();
        PageIdentifiers pageIdentifiers = pefValidator.getPageIdentifiers(
                "    #bc                     #a", false, false
        );

        assertEquals(1, pageIdentifiers.getPefPage(), "Handle the first normal page, PEF page");
        assertEquals(23, pageIdentifiers.getOrgStartPage(), "Handle the first normal page, original start page");
        assertEquals(-1, pageIdentifiers.getOrgEndPage(), "Handle the first normal page, original end page");

        pageIdentifiers = pefValidator.getPageIdentifiers(
                "    #b                #bc--#bd", true, false
        );

        assertEquals(2, pageIdentifiers.getPefPage(), "Handle the second normal page, PEF page");
        assertEquals(23, pageIdentifiers.getOrgStartPage(), "Handle the second normal page, original start page");
        assertEquals(24, pageIdentifiers.getOrgEndPage(), "Handle the second normal page, original end page");

        pageIdentifiers = pefValidator.getPageIdentifiers(
                "    #daj            #bjh--#baj", true, false
        );

        assertEquals(410, pageIdentifiers.getPefPage(), "Handle the 410th normal page, PEF page");
        assertEquals(208, pageIdentifiers.getOrgStartPage(), "Handle the 410th normal page, original start page");
        assertEquals(210, pageIdentifiers.getOrgEndPage(), "Handle the 410th normal page, original end page");
    }

    private Document newDocument() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.newDocument();
    }

    private Element createRow(String text, Document doc) {
        Element el = doc.createElement("row");
        el.setTextContent(text);
        return el;
    }

    @DisplayName("Test that we handle incorrect page objects")
    @Test
    public void testExtractingIncorrectPageObjects() throws Exception {
        final PEFCheck pefValidator = new PEFCheck();
        Document doc = newDocument();
        final Element invalidPage = doc.createElement("notpage");

        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.processPage(invalidPage, false, false);
        }, "If the page tag is incorrect we should throw InvalidFormatException");

        final Element page = doc.createElement("page");
        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.processPage(page, false, false);
        }, "If the page don't have any rows should throw InvalidFormatException");

        Element row = doc.createElement("row");
        page.appendChild(row);

        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.processPage(page, false, false);
        }, "If the first row is empty we should throw InvalidFormatException");

        row.setTextContent("not the correct data");
        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.processPage(page, false, false);
        }, "If the don't have correct page information we should throw InvalidFormatException");
    }

    @DisplayName("Test that we correct page objects")
    @Test
    public void testExtractingCorrectPageObjects() throws Exception{
        final PEFCheck pefValidator = new PEFCheck();
        Document doc = newDocument();

        Element page = doc.createElement("page");

        page.appendChild(createRow("    #bc                     #a", doc));

        PageIdentifiers pageIdentifiers = pefValidator.processPage(page, false, false);

        assertEquals(1, pageIdentifiers.getPefPage(), "Handle the one page, PEF page");
        assertEquals(23, pageIdentifiers.getOrgStartPage(), "Handle the one page, original start page");
        assertEquals(-1, pageIdentifiers.getOrgEndPage(), "Handle the one page, original end page");
        assertTrue(pageIdentifiers.isEmpty(), "Handle the one page, is empty");

        page.appendChild(createRow("  såg hennes huvud utsträckt", doc));
        pageIdentifiers = pefValidator.processPage(page, false, false);

        assertFalse(pageIdentifiers.isEmpty(), "Handle the one page, is not empty");
    }

    @DisplayName("Test that we handle incorrect sections")
    @Test
    public void testExtractingIncorrectSections() throws Exception {
        final PEFCheck pefValidator = new PEFCheck();
        Document doc = newDocument();
        final Element invalidSection = doc.createElement("notsection");

        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.processPage(invalidSection, false, false);
        }, "If the section tag is incorrect we should throw InvalidFormatException");

        final Element section = doc.createElement("section");
        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.processPage(section, false, false);
        }, "If the section don't have any pages should throw InvalidFormatException");

        Element page = doc.createElement("page");
        section.appendChild(page);

        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.processPage(section, false, false);
        }, "If the first page is empty we should throw InvalidFormatException");
    }

    @DisplayName("Test that we extract normal sections")
    @Test
    public void testExtractingNormalSections() throws Exception {
        final PEFCheck pefValidator = new PEFCheck();
        Document doc = newDocument();

        Element section = doc.createElement("section");

        Element firstPage = doc.createElement("page");
        firstPage.appendChild(createRow("    #e--#g                  #a", doc));
        firstPage.appendChild(createRow("  såg hennes huvud utsträckt", doc));

        Element secondPage = doc.createElement("page");
        secondPage.appendChild(createRow("    #b                      #g", doc));

        section.appendChild(firstPage);
        section.appendChild(secondPage);

        List<PageIdentifiers> identifiers = pefValidator.processSection(section, false);

        assertEquals(1, identifiers.get(0).getPefPage(), "Handle normal section, PEF page one");
        assertEquals(5, identifiers.get(0).getOrgStartPage(), "Handle normal section, original start page one");
        assertEquals(7, identifiers.get(0).getOrgEndPage(), "Handle normal section, original end page one");
        assertFalse(identifiers.get(0).isEmpty(), "Handle normal section, not empty");
        assertEquals(2, identifiers.get(1).getPefPage(), "Handle normal section, PEF page two");
        assertEquals(7, identifiers.get(1).getOrgStartPage(), "Handle normal section, original start page two");
        assertEquals(-1, identifiers.get(1).getOrgEndPage(), "Handle normal section, original end page two");
        assertTrue(identifiers.get(1).isEmpty(), "Handle normal section, empty");
    }

    @DisplayName("Test that we extract index sections")
    @Test
    public void testExtractingIndexSections() throws Exception {
        final PEFCheck pefValidator = new PEFCheck();
        Document doc = newDocument();

        Element section = doc.createElement("section");

        Element firstPage = doc.createElement("page");
        firstPage.appendChild(createRow("                            _i", doc));
        firstPage.appendChild(createRow("  såg hennes huvud utsträckt", doc));

        Element secondPage = doc.createElement("page");
        secondPage.appendChild(createRow("    __ii", doc));

        section.appendChild(firstPage);
        section.appendChild(secondPage);

        List<PageIdentifiers> identifiers = pefValidator.processSection(section, true);

        assertEquals(1, identifiers.get(0).getPefPage(), "Handle index section, PEF page one");
        assertFalse(identifiers.get(0).isEmpty(), "Handle index section, not empty");
        assertEquals(2, identifiers.get(1).getPefPage(), "Handle index section, PEF page two");
        assertTrue(identifiers.get(1).isEmpty(), "Handle index section, empty");
    }

    @DisplayName("Test that we handle incorrect sections")
    @Test
    public void testIsSectionIndexWithIncorrectSection() throws Exception {
        final PEFCheck pefValidator = new PEFCheck();
        Document doc = newDocument();

        Element invalidSection = doc.createElement("notsection");

        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.isIndexSection(invalidSection);
        }, "If the section tag is incorrect we should throw InvalidFormatException");

        final Element section = doc.createElement("section");
        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.isIndexSection(section);
        }, "If the section don't have any pages should throw InvalidFormatException");

        Element page = doc.createElement("page");
        section.appendChild(page);

        assertThrows(InvalidFormatException.class, () -> {
            pefValidator.isIndexSection(section);
        }, "If the first page is empty we should throw InvalidFormatException");
    }

    @DisplayName("Test that we can identity index sections")
    @Test
    public void testIsSectionIndexWithIndexSection() throws Exception {
        final PEFCheck pefValidator = new PEFCheck();
        Document doc = newDocument();

        Element section = doc.createElement("section");

        Element firstPage = doc.createElement("page");
        firstPage.appendChild(createRow("                            _i", doc));
        section.appendChild(firstPage);

        assertTrue(pefValidator.isIndexSection(section), "This should be an index section");
    }

    @DisplayName("Test that we can identify normal sections")
    @Test
    public void testIsSectionIndexWithNormalSection() throws Exception {
        final PEFCheck pefValidator = new PEFCheck();
        Document doc = newDocument();

        Element section = doc.createElement("section");

        Element firstPage = doc.createElement("page");
        firstPage.appendChild(createRow("    #e--#g                  #a", doc));
        section.appendChild(firstPage);

        assertFalse(pefValidator.isIndexSection(section), "This should not be an index section");
    }
}
