package se.mtm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PEFCheckTest {

    @DisplayName("Test that an incorrect page number returns -1")
    @Test
    public void testIncorrectPageNumber() {
        PEFCheck pefCheck = new PEFCheck();
        assertEquals(-1, pefCheck.getPageNumber("dsjiads"), "Handle incorrect number");
        assertEquals(-1, pefCheck.getPageNumber("#dsqjiads"), "Handle incorrect number");
    }

    @DisplayName("Test that correct numbers return their values")
    @Test
    public void testCorrectPageNumber() {
        PEFCheck pefCheck = new PEFCheck();
        assertEquals(1, pefCheck.getPageNumber("#a"), "Can handle one number");
        assertEquals(5, pefCheck.getPageNumber("#e"), "Can handle another number");
        assertEquals(10, pefCheck.getPageNumber("#aj"), "Can handle more than one number");
        assertEquals(1000, pefCheck.getPageNumber("#ajjj"), "Can handle thousends");
        assertEquals(1337, pefCheck.getPageNumber("#accg"), "Can be leet.");
    }

    @DisplayName("Test that an incorrect roman numerals returns -1")
    @Test
    public void testIncorrectRomanNumerals() {
        PEFCheck pefCheck = new PEFCheck();
        assertEquals(-1, pefCheck.getPageNumber("_ii"), "Handle incorrect number");
        assertEquals(-1, pefCheck.getPageNumber("__i"), "Handle incorrect number");
    }

    @DisplayName("Test that we can handle roman numerals")
    @Test
    void testCorrectRomanNumerals() {
        PEFCheck pefCheck = new PEFCheck();
        assertEquals(1, pefCheck.getPageNumber("_i"), "Can handle one");
        assertEquals(5, pefCheck.getPageNumber("_v"), "Can handle five");
        assertEquals(10, pefCheck.getPageNumber("_x"), "Can handle more than one number");
        assertEquals(50, pefCheck.getPageNumber("_l"), "Can handle fifty");
        assertEquals(100, pefCheck.getPageNumber("_c"), "Can handle hundreds.");
        assertEquals(500, pefCheck.getPageNumber("_d"), "Can handle five hundreds.");
        assertEquals(1000, pefCheck.getPageNumber("_m"), "Can handle thousands.");
        assertEquals(1337, pefCheck.getPageNumber("__mcccxxxvii"), "Can be leet.");
        assertEquals(1771, pefCheck.getPageNumber("__mdcclxxi"), "Can handle an important year");
        assertEquals(2019, pefCheck.getPageNumber("__mmxix"), "Can handle this year");
    }

    @DisplayName("Test that we can extract index pages")
    @Test
    public void testExtractingIndexPages() {
        PEFCheck pefCheck = new PEFCheck();
        PageIdentifiers pageIdentifiers = pefCheck.getPageIdentifiers(
            "                            _i", false, true
        );

        assertEquals(1, pageIdentifiers.getPefPage(), "Handle the first index page");

        pageIdentifiers = pefCheck.getPageIdentifiers(
            "    __ii", true, true
        );

        assertEquals(2, pageIdentifiers.getPefPage(), "Handle the second index page");

        pageIdentifiers = pefCheck.getPageIdentifiers(
            "    __ix", true, true
        );

        assertEquals(9, pageIdentifiers.getPefPage(), "Handle the ninth index page");
    }

    @DisplayName("Test that we can extract normal pages")
    @Test
    public void testExtractingNormalPages() {
        PEFCheck pefCheck = new PEFCheck();
        PageIdentifiers pageIdentifiers = pefCheck.getPageIdentifiers(
                "    #bc                     #a", false, false
        );

        assertEquals(1, pageIdentifiers.getPefPage(), "Handle the first normal page, PEF page");
        assertEquals(23, pageIdentifiers.getOrgStartPage(), "Handle the first normal page, original start page");
        assertEquals(-1, pageIdentifiers.getOrgEndPage(), "Handle the first normal page, original end page");

        pageIdentifiers = pefCheck.getPageIdentifiers(
                "    #b                #bc--#bd", true, false
        );

        assertEquals(2, pageIdentifiers.getPefPage(), "Handle the second normal page, PEF page");
        assertEquals(23, pageIdentifiers.getOrgStartPage(), "Handle the second normal page, original start page");
        assertEquals(24, pageIdentifiers.getOrgEndPage(), "Handle the second normal page, original end page");

        pageIdentifiers = pefCheck.getPageIdentifiers(
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
        final PEFCheck pefCheck = new PEFCheck();
        Document doc = newDocument();
        final Element invalidPage = doc.createElement("notpage");

        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.processPage(invalidPage, false, false);
        }, "If the page tag is incorrect we should throw InvalidFormatException");

        final Element page = doc.createElement("page");
        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.processPage(page, false, false);
        }, "If the page don't have any rows should throw InvalidFormatException");

        Element row = doc.createElement("row");
        page.appendChild(row);

        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.processPage(page, false, false);
        }, "If the first row is empty we should throw InvalidFormatException");

        row.setTextContent("not the correct data");
        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.processPage(page, false, false);
        }, "If the don't have correct page information we should throw InvalidFormatException");
    }

    @DisplayName("Test that we correct page objects")
    @Test
    public void testExtractingCorrectPageObjects() throws Exception{
        final PEFCheck pefCheck = new PEFCheck();
        Document doc = newDocument();

        Element page = doc.createElement("page");

        page.appendChild(createRow("    #bc                     #a", doc));

        PageIdentifiers pageIdentifiers = pefCheck.processPage(page, false, false);

        assertEquals(1, pageIdentifiers.getPefPage(), "Handle the one page, PEF page");
        assertEquals(23, pageIdentifiers.getOrgStartPage(), "Handle the one page, original start page");
        assertEquals(-1, pageIdentifiers.getOrgEndPage(), "Handle the one page, original end page");
        assertTrue(pageIdentifiers.isEmpty(), "Handle the one page, is empty");

        page.appendChild(createRow("  såg hennes huvud utsträckt", doc));
        pageIdentifiers = pefCheck.processPage(page, false, false);

        assertFalse(pageIdentifiers.isEmpty(), "Handle the one page, is not empty");
    }

    @DisplayName("Test that we handle incorrect sections")
    @Test
    public void testExtractingIncorrectSections() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();
        Document doc = newDocument();
        final Element invalidSection = doc.createElement("notsection");

        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.processPage(invalidSection, false, false);
        }, "If the section tag is incorrect we should throw InvalidFormatException");

        final Element section = doc.createElement("section");
        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.processPage(section, false, false);
        }, "If the section don't have any pages should throw InvalidFormatException");

        Element page = doc.createElement("page");
        section.appendChild(page);

        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.processPage(section, false, false);
        }, "If the first page is empty we should throw InvalidFormatException");
    }

    @DisplayName("Test that we extract normal sections")
    @Test
    public void testExtractingNormalSections() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();
        Document doc = newDocument();

        Element section = doc.createElement("section");

        Element firstPage = doc.createElement("page");
        firstPage.appendChild(createRow("    #e--#g                  #a", doc));
        firstPage.appendChild(createRow("  såg hennes huvud utsträckt", doc));

        Element secondPage = doc.createElement("page");
        secondPage.appendChild(createRow("    #b                      #g", doc));

        section.appendChild(firstPage);
        section.appendChild(secondPage);

        List<PageIdentifiers> identifiers = pefCheck.processSection(section, false);

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
        final PEFCheck pefCheck = new PEFCheck();
        Document doc = newDocument();

        Element section = doc.createElement("section");

        Element firstPage = doc.createElement("page");
        firstPage.appendChild(createRow("                            _i", doc));
        firstPage.appendChild(createRow("  såg hennes huvud utsträckt", doc));

        Element secondPage = doc.createElement("page");
        secondPage.appendChild(createRow("    __ii", doc));

        section.appendChild(firstPage);
        section.appendChild(secondPage);

        List<PageIdentifiers> identifiers = pefCheck.processSection(section, true);

        assertEquals(1, identifiers.get(0).getPefPage(), "Handle index section, PEF page one");
        assertFalse(identifiers.get(0).isEmpty(), "Handle index section, not empty");
        assertEquals(2, identifiers.get(1).getPefPage(), "Handle index section, PEF page two");
        assertTrue(identifiers.get(1).isEmpty(), "Handle index section, empty");
    }

    @DisplayName("Test that we handle incorrect sections")
    @Test
    public void testIsSectionIndexWithIncorrectSection() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();
        Document doc = newDocument();

        Element invalidSection = doc.createElement("notsection");

        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.isIndexSection(invalidSection);
        }, "If the section tag is incorrect we should throw InvalidFormatException");

        final Element section = doc.createElement("section");
        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.isIndexSection(section);
        }, "If the section don't have any pages should throw InvalidFormatException");

        Element page = doc.createElement("page");
        section.appendChild(page);

        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.isIndexSection(section);
        }, "If the first page is empty we should throw InvalidFormatException");
    }

    @DisplayName("Test that we can identity index sections")
    @Test
    public void testIsSectionIndexWithIndexSection() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();
        Document doc = newDocument();

        Element section = doc.createElement("section");

        Element firstPage = doc.createElement("page");
        firstPage.appendChild(createRow("                            _i", doc));
        section.appendChild(firstPage);

        assertTrue(pefCheck.isIndexSection(section), "This should be an index section");
    }

    @DisplayName("Test that we can identify normal sections")
    @Test
    public void testIsSectionIndexWithNormalSection() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();
        Document doc = newDocument();

        Element section = doc.createElement("section");

        Element firstPage = doc.createElement("page");
        firstPage.appendChild(createRow("    #e--#g                  #a", doc));
        section.appendChild(firstPage);

        assertFalse(pefCheck.isIndexSection(section), "This should not be an index section");
    }

    @DisplayName("Test that we can handle incorrect pages when looking for empty pages")
    @Test
    public void testFindIncorrectPagesAndPrintPageNr() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();

        final List<PageIdentifiers> pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(-1, -1, -1, true, false));
        assertThrows(InvalidFormatException.class, () -> {
            pefCheck.hasEmptyPages(pageIdentifiers);
        }, "If the the page number is incorrect we should throw InvalidFormatException");
    }

    @DisplayName("Test that we can identify empty pages")
    @Test
    public void testFindAndPrintPageNr() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();

        List<PageIdentifiers> pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(1, -1, -1, true, false));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream outOrig = System.out;
        System.setOut(new PrintStream(baos));
        assertTrue(pefCheck.hasEmptyPages(pageIdentifiers), "Check that we can find an empty page");
        System.setOut(outOrig);
        assertEquals("--- Empty page #a (1)", baos.toString().trim(), "Check that we report the correct page.");

        pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(1, -1, -1, false, false));
        pageIdentifiers.add(new PageIdentifiers(2, -1, -1, false, false));
        pageIdentifiers.add(new PageIdentifiers(3, -1, -1, false, false));
        pageIdentifiers.add(new PageIdentifiers(4, -1, -1, false, false));

        assertFalse(pefCheck.hasEmptyPages(pageIdentifiers), "Check that we can handle no empty pages");

        pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(1, -1, -1, false, true));
        pageIdentifiers.add(new PageIdentifiers(2, -1, -1, true, true));
        pageIdentifiers.add(new PageIdentifiers(3, -1, -1, false, true));
        pageIdentifiers.add(new PageIdentifiers(4, -1, -1, false, true));

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        assertTrue(pefCheck.hasEmptyPages(pageIdentifiers),
                "Check that we can find one empty within a batch of pages."
        );
        assertEquals("--- Empty page __ii (2)", baos.toString().trim(), "Check that we report the correct page.");
        System.setOut(outOrig);


        pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(10, -1, -1, true, false));

        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        pefCheck.hasEmptyPages(pageIdentifiers);
        System.setOut(outOrig);
        assertEquals("--- Empty page #aj (10)", baos.toString().trim(), "Check that we report the correct page.");
    }

    @DisplayName("Test that we can present pages in both roman and normal pef presentation as well as decimal")
    @Test
    public void testPagePresentation() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();

        assertEquals("_i (1)", pefCheck.getPrintablePageNumber(1, true),
            "Can handle roman number 1"
        );

        assertEquals("__ix (9)", pefCheck.getPrintablePageNumber(9, true),
                "Can handle roman number 9"
        );

        assertEquals("#a (1)", pefCheck.getPrintablePageNumber(1, false),
                "Can handle roman number 1"
        );

        assertEquals("#aj (10)", pefCheck.getPrintablePageNumber(10, false),
                "Can handle pef number 10"
        );
    }

    @DisplayName("Test that we can validate correct page sequences.")
    @Test
    public void testValidationOfPageSequence() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();

        List<PageIdentifiers> pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(1, -1, -1, true, false));
        pageIdentifiers.add(new PageIdentifiers(2, -1, -1, true, false));
        pageIdentifiers.add(new PageIdentifiers(3, -1, -1, true, false));

        assertEquals(3, pefCheck.validatePageSequence(pageIdentifiers, 0),
        "Check that we can validate a correct page sequence from zero"
        );

        pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(42, -1, -1, true, false));
        pageIdentifiers.add(new PageIdentifiers(43, -1, -1, true, false));
        pageIdentifiers.add(new PageIdentifiers(44, -1, -1, true, false));

        assertEquals(44, pefCheck.validatePageSequence(pageIdentifiers, 41),
        "Check that we can validate a correct page sequence from 42"
        );
    }

    @DisplayName("Test that we can validate incorrect page sequences.")
    @Test
    public void testValidationOfIncorrectPageSequence() throws Exception {
        final PEFCheck pefCheck = new PEFCheck();

        List<PageIdentifiers> pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(2, -1, -1, true, false));
        pageIdentifiers.add(new PageIdentifiers(3, -1, -1, true, false));
        pageIdentifiers.add(new PageIdentifiers(4, -1, -1, true, false));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream outOrig = System.out;
        System.setOut(new PrintStream(baos));
        assertEquals(4, pefCheck.validatePageSequence(pageIdentifiers, 0),
                "Check that we can validate a incorrect page sequence from zero"
        );
        System.setOut(outOrig);
        assertEquals("--- Missing page(s) between #j (0) and #b (2)", baos.toString().trim(),
        "Check that we report the missing page before sequence"
        );

        pageIdentifiers = new ArrayList<>();
        pageIdentifiers.add(new PageIdentifiers(1, -1, -1, true, false));
        pageIdentifiers.add(new PageIdentifiers(6, -1, -1, true, false));
        pageIdentifiers.add(new PageIdentifiers(7, -1, -1, true, false));

        baos = new ByteArrayOutputStream();
        outOrig = System.out;
        System.setOut(new PrintStream(baos));
        assertEquals(7, pefCheck.validatePageSequence(pageIdentifiers, 0),
        "Check that we can validate a incorrect page sequence from zero with missing pages in the middle"
        );
        System.setOut(outOrig);
        assertEquals("--- Missing page(s) between #a (1) and #f (6)", baos.toString().trim(),
        "Check that we report the missing page in the middle of the sequence"
        );
    }

    @DisplayName("Test that we can process a file with text and report issues correctly.")
    @Test
    public void testFileProcessing() throws Exception {

        ClassLoader classLoader = PEFCheckTest.class.getClassLoader();
        File bookFile = new File(classLoader.getResource("testfiles/simple-book.pef").getFile());

        final PEFCheck pefCheck = new PEFCheck();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream outOrig = System.out;
        System.setOut(new PrintStream(baos));
        pefCheck.processFile(bookFile);
        System.setOut(outOrig);
        assertEquals(
                "Checking file simple-book.pef\n" +
                        "--- Empty page __iii (3)\n" +
                        "--- Missing page(s) between #a (1) and #d (4)"
                , baos.toString().trim(),
                "Check that we report the missing page before sequence"
        );
    }
}
