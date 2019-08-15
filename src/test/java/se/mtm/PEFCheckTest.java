package se.mtm;

import com.sun.media.sound.InvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

    @DisplayName("Test that we incorrect page objects")
    @Test
    public void testExtractingIncorrectPageObjects() throws Exception{
        final PEFCheck pefValidator = new PEFCheck();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
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
        PEFCheck pefValidator = new PEFCheck();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        Element page = doc.createElement("page");
        Element row = doc.createElement("row");
        page.appendChild(row);
        row.setTextContent("    #bc                     #a");
        PageIdentifiers pageIdentifiers = pefValidator.processPage(page, false, false);

        assertEquals(1, pageIdentifiers.getPefPage(), "Handle the one page, PEF page");
        assertEquals(23, pageIdentifiers.getOrgStartPage(), "Handle the one page, original start page");
        assertEquals(-1, pageIdentifiers.getOrgEndPage(), "Handle the one page, original end page");
        assertTrue(pageIdentifiers.isEmpty(), "Handle the one page, is empty");

        Element row2 = doc.createElement("row");
        page.appendChild(row2);
        row2.setTextContent("  såg hennes huvud utsträckt");
        pageIdentifiers = pefValidator.processPage(page, false, false);

        assertFalse(pageIdentifiers.isEmpty(), "Handle the one page, is not empty");
    }
}
