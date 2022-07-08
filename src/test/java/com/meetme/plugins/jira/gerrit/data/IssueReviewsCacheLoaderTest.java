package com.meetme.plugins.jira.gerrit.data;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.meetme.plugins.jira.gerrit.data.dto.GerritChange;
import com.sonymobile.tools.gerrit.gerritevents.GerritQueryException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * as of now, only getReviewsFromGerrit() with REST has test coverage!
 */
public class IssueReviewsCacheLoaderTest {


    private final String username = "user123";

    private final String password = "password123";

    private final int port = 8765;

    private final String url = "http://localhost:" + port;

    private final String query = "/a/changes/?q=limit%3A1&o=CURRENT_REVISION";

    private WireMockServer wireMockServer;

    private IssueReviewsCacheLoader issueReviewsCacheLoader;

    private GerritConfiguration gerritConfiguration;

    @Before
    public void setup() {
        //initializing ONLY required values
        gerritConfiguration = new GerritConfiguration() {
            @Override
            public URI getHttpBaseUrl() {
                return URI.create(url);
            }

            @Override
            public String getHttpPassword() {
                return password;
            }

            @Override
            public String getHttpUsername() {
                return username;
            }

            @Override
            public boolean getPreferRestConnection() {
                return true;
            }

            @Override
            public String getIssueSearchQuery() {
                return query;
            }

            @Override
            public String getProjectSearchQuery() {
                return null;
            }

            @Override
            public String getSshHostname() {
                return null;
            }

            @Override
            public int getSshPort() {
                return 0;
            }

            @Override
            public File getSshPrivateKey() {
                return null;
            }

            @Override
            public String getSshUsername() {
                return null;
            }

            @Override
            public boolean getShowsEmptyPanel() {
                return false;
            }

            @Override
            public void setHttpBaseUrl(String httpBaseUrl) {

            }

            @Override
            public void setHttpPassword(String httpPassword) {

            }

            @Override
            public void setHttpUsername(String httpUsername) {

            }

            @Override
            public void setPreferRestConnection(boolean preferRestConnection) {

            }

            @Override
            public void setIssueSearchQuery(String query) {

            }

            @Override
            public void setProjectSearchQuery(String query) {

            }

            @Override
            public void setSshHostname(String hostname) {

            }

            @Override
            public void setSshPort(int port) {

            }

            @Override
            public void setSshPrivateKey(File sshPrivateKey) {

            }

            @Override
            public void setSshUsername(String username) {

            }

            @Override
            public void setShowEmptyPanel(boolean show) {

            }

            @Override
            public boolean isSshInvalid() {
                return true;
            }

            @Override
            public boolean isHttpValid() {
                return true;
            }

            @Override
            public List<String> getIdsOfKnownGerritProjects() {
                return null;
            }

            @Override
            public void setIdsOfKnownGerritProjects(List<String> idsOfSelectedGerritProjects) {

            }

            @Override
            public boolean getUseGerritProjectWhitelist() {
                return false;
            }

            @Override
            public void setUseGerritProjectWhitelist(boolean useGerritProjectWhitelist) {

            }
        };
        issueReviewsCacheLoader = new IssueReviewsCacheLoader(gerritConfiguration);
    }

    /**
     * Initialise the mock-REST-API, call
     * {@link com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandlerHttp#queryJava(String, boolean, boolean, boolean)}.
     */
    public void setupMockServer() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(port));
        wireMockServer.start();
    }


    public void shutdownMockServer() {
        wireMockServer.stop();
    }


    /**
     * setup stub for wireMockServer.
     */
    public void setupStubForPreferRest() {
        wireMockServer.stubFor(get(urlEqualTo(query))
                .withBasicAuth(username, password)
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBody(")]}'\n []")));
    }


    /**
     * setup stub for wireMockServer.
     * @param fileName the file to reply with
     */
    public void setupStubForGetReviews(String fileName) {
        wireMockServer.stubFor(get(urlEqualTo(query))
                .withBasicAuth(username, password)
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBodyFile(fileName)));
    }


    /**
     * if the url-check returns false, an exception should be thrown
     * @throws GerritQueryException when Http is invalid
     */
    @Test(expected = GerritConfiguration.NotConfiguredException.class)
    public void testHttpValid() throws GerritQueryException {
        //Http set to NOT valid
        gerritConfiguration = new GerritConfiguration() {
            @Override
            public URI getHttpBaseUrl() {
                return URI.create(url);
            }

            @Override
            public String getHttpPassword() {
                return password;
            }

            @Override
            public String getHttpUsername() {
                return username;
            }

            @Override
            public boolean getPreferRestConnection() {
                return true;
            }

            @Override
            public String getIssueSearchQuery() {
                return query;
            }

            @Override
            public String getProjectSearchQuery() {
                return null;
            }

            @Override
            public String getSshHostname() {
                return null;
            }

            @Override
            public int getSshPort() {
                return 0;
            }

            @Override
            public File getSshPrivateKey() {
                return null;
            }

            @Override
            public String getSshUsername() {
                return null;
            }

            @Override
            public boolean getShowsEmptyPanel() {
                return false;
            }

            @Override
            public void setHttpBaseUrl(String httpBaseUrl) {

            }

            @Override
            public void setHttpPassword(String httpPassword) {

            }

            @Override
            public void setHttpUsername(String httpUsername) {

            }

            @Override
            public void setPreferRestConnection(boolean preferRestConnection) {

            }

            @Override
            public void setIssueSearchQuery(String query) {

            }

            @Override
            public void setProjectSearchQuery(String query) {

            }

            @Override
            public void setSshHostname(String hostname) {

            }

            @Override
            public void setSshPort(int port) {

            }

            @Override
            public void setSshPrivateKey(File sshPrivateKey) {

            }

            @Override
            public void setSshUsername(String username) {

            }

            @Override
            public void setShowEmptyPanel(boolean show) {

            }

            @Override
            public boolean isSshInvalid() {
                return true;
            }

            @Override
            public boolean isHttpValid() {
                return false;
            }

            @Override
            public List<String> getIdsOfKnownGerritProjects() {
                return null;
            }

            @Override
            public void setIdsOfKnownGerritProjects(List<String> idsOfSelectedGerritProjects) {

            }

            @Override
            public boolean getUseGerritProjectWhitelist() {
                return false;
            }

            @Override
            public void setUseGerritProjectWhitelist(boolean useGerritProjectWhitelist) {

            }
        };
        issueReviewsCacheLoader = new IssueReviewsCacheLoader(gerritConfiguration);
        issueReviewsCacheLoader.getReviewsFromGerrit("limit%3A1");
    }


    /**
     * only connect to rest endpoint(!=ssh) when configuration.getPreferRestConnection is true
     */
    @Test
    public void testPreferRest() {
        setupMockServer();
        setupStubForPreferRest();

        //":" is converted to "%3A1". All requests sent can be seen from wireMockServer.getAllServeEvents()
        try {
            issueReviewsCacheLoader.getReviewsFromGerrit("limit:1");
        } catch (GerritQueryException e) {
            Assert.fail(e.getMessage());
        }

        shutdownMockServer();
    }


    /**
     * test correct fetching of events. Also, dependent on GerritChange and GerritPatchSet
     */
    @Test
    public void testGetReviewsFromGerritOutput() {
        setupMockServer();
        setupStubForGetReviews("restApiTest_longResponse");

        //":" is converted to "%3A1". All requests sent can be seen from wireMockServer.getAllServeEvents()
        try {
            //in reality Gerrit would only send one change (limit:1)
            List<GerritChange> changes = issueReviewsCacheLoader.getReviewsFromGerrit("limit:1");
            System.out.println("Loaded " + changes.size() + " changes.");
            //System.out.println(wireMockServer.getStubMappings());
            Assert.assertEquals(2, changes.size());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
            System.err.println(wireMockServer.getStubMappings());
        }
        //System.err.println(wireMockServer.getAllServeEvents());
        shutdownMockServer();
    }

}
