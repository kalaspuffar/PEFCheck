package se.mtm;

public class PageIdentifiers {
    /**
     * The start page number of the original document of this page
     */
    int orgStartPage = -1;

    /**
     * The end page number of the original document of this page
     */
    int orgEndPage = -1;

    /**
     * The pef page number of this page
     */
    int pefPage = -1;

    /**
     * Checking if this page is empty.
     */
    boolean empty = false;

    public PageIdentifiers(int orgStartPage, int orgEndPage, int pefPage, boolean empty) {
        this.orgStartPage = orgStartPage;
        this.orgEndPage = orgEndPage;
        this.pefPage = pefPage;
        this.empty = empty;
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
}