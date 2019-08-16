package se.mtm;

public class PageIdentifiers {
    /**
     * The start page number of the original document of this page
     */
    private int orgStartPage = -1;

    /**
     * The end page number of the original document of this page
     */
    private int orgEndPage = -1;

    /**
     * The pef page number of this page
     */
    private int pefPage = -1;

    /**
     * Checking if this page is empty.
     */
    private boolean empty = false;

    /**
     * Identified as an index page. (Roman numbers)
     */
    private boolean indexPage;

    public PageIdentifiers() {}

    public PageIdentifiers(int pefPage, int orgStartPage, int orgEndPage, boolean empty, boolean indexPage) {
        this.pefPage = pefPage;
        this.orgStartPage = orgStartPage;
        this.orgEndPage = orgEndPage;
        this.empty = empty;
        this.indexPage = indexPage;
    }

    public int getOrgStartPage() {
        return orgStartPage;
    }

    public void setOrgStartPage(int orgStartPage) {
        this.orgStartPage = orgStartPage;
    }

    public int getOrgEndPage() {
        return orgEndPage;
    }

    public void setOrgEndPage(int orgEndPage) {
        this.orgEndPage = orgEndPage;
    }

    public int getPefPage() {
        return pefPage;
    }

    public void setPefPage(int pefPage) {
        this.pefPage = pefPage;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setIndexPage(boolean indexPage) {
        this.indexPage = indexPage;
    }

    public boolean isIndexPage() {
        return indexPage;
    }
}