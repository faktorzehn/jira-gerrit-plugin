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
    public void setup()
    {
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
            public boolean isSshValid() {
                return false;
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
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(port)
                .usingFilesUnderDirectory("src/test/resources/com/sonymobile/tools/gerrit/gerritevents/SampleResponses"));
        //.usingFilesUnderDirectory("src/test/"));
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
     */
    public void setupStubForGetReviews(String fileName) {
        wireMockServer.stubFor(get(urlEqualTo(query))
                .withBasicAuth(username, password)
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBody(")]}'\n" +
                                "[{\"id\":\"test0%2Ftest0.test~master~Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"project\":\"test0/test0.test\",\"branch\":\"master\",\"attention_set\":{\"309\":{\"account\":{\"_account_id\":309},\"last_update\":\"2022-03-24 09:59:36.000000000\",\"reason\":\"Username0 replied on the change\"}},\"hashtags\":[],\"change_id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"subject\":\"Important Subject :: Set compact theme\",\"status\":\"NEW\",\"created\":\"2022-03-23 17:36:05.000000000\",\"updated\":\"2022-03-24 09:59:54.000000000\",\"submit_type\":\"FAST_FORWARD_ONLY\",\"insertions\":4,\"deletions\":1,\"total_comment_count\":2,\"unresolved_comment_count\":0,\"has_review_started\":true,\"meta_rev_id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"_number\":43449,\"owner\":{\"_account_id\":460},\"messages\":[{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"tag\":\"autogenerated:gerrit:newPatchSet\",\"author\":{\"_account_id\":460},\"real_author\":{\"_account_id\":460},\"date\":\"2022-03-23 17:36:05.000000000\",\"message\":\"Uploaded patch set 1.\",\"_revision_number\":1},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-23 17:36:13.000000000\",\"message\":\"Patch Set 1:\\n\\nBuild Started https://ninja-ci.nicedomain.com/job/test0_Review/1234/\",\"_revision_number\":1},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-23 17:53:12.000000000\",\"message\":\"Patch Set 1: Verified+1\\n\\nBuild Successful \\n\\nhttps://ninja-ci.nicedomain.com/job/test0_Review/1234/ : SUCCESS\",\"_revision_number\":1},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"author\":{\"_account_id\":309},\"real_author\":{\"_account_id\":309},\"date\":\"2022-03-24 09:02:13.000000000\",\"message\":\"Patch Set 1: Code-Review-1\\n\\n(1 comment)\",\"_revision_number\":1},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"tag\":\"autogenerated:gerrit:newPatchSet\",\"author\":{\"_account_id\":460},\"real_author\":{\"_account_id\":460},\"date\":\"2022-03-24 09:58:46.000000000\",\"message\":\"Uploaded patch set 2.\",\"_revision_number\":2},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-24 09:58:54.000000000\",\"message\":\"Patch Set 2:\\n\\nBuild Started https://ninja-ci.nicedomain.com/job/test0_Review/1234/\",\"_revision_number\":2},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"author\":{\"_account_id\":460},\"real_author\":{\"_account_id\":460},\"date\":\"2022-03-24 09:59:36.000000000\",\"message\":\"Patch Set 2:\\n\\n(1 comment)\",\"_revision_number\":2},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"tag\":\"autogenerated:gerrit:newPatchSet\",\"author\":{\"_account_id\":460},\"real_author\":{\"_account_id\":460},\"date\":\"2022-03-24 09:59:48.000000000\",\"message\":\"Patch Set 3: Patch Set 2 was rebased\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-24 09:59:54.000000000\",\"message\":\"Patch Set 3:\\n\\nBuild Started https://ninja-ci.nicedomain.com/job/test0_Review/1234/\",\"_revision_number\":3}],\"current_revision\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"revisions\":{\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\":{\"kind\":\"REWORK\",\"_number\":2,\"created\":\"2022-03-24 09:58:46.000000000\",\"uploader\":{\"_account_id\":460},\"ref\":\"refs/changes/49/43449/2\",\"fetch\":{\"ssh\":{\"url\":\"ssh:/username@nicedomain.com:29418/test0\",\"ref\":\"refs/changes/49/43449/2\"},\"http\":{\"url\":\"https://username@nicedomain.com/a/ipm/test0\",\"ref\":\"refs/changes/49/43449/2\"}}},\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501569\":{\"kind\":\"TRIVIAL_REBASE\",\"_number\":3,\"created\":\"2022-03-24 09:59:48.000000000\",\"uploader\":{\"_account_id\":460},\"ref\":\"refs/changes/49/43449/3\",\"fetch\":{\"ssh\":{\"url\":\"ssh:/username@nicedomain.com:29418/test0\",\"ref\":\"refs/changes/49/43449/3\"},\"http\":{\"url\":\"https://username@nicedomain.com/a/ipm/test0\",\"ref\":\"refs/changes/49/43449/3\"}},\"commit\":{\"parents\":[{\"commit\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\",\"subject\":\"important subject :: Update new policy documentation\"}],\"author\":{\"name\":\"username1\",\"email\":\"username1@nicedomain.com\",\"date\":\"2022-03-23 17:21:08.000000000\",\"tz\":60},\"committer\":{\"name\":\"username1\",\"email\":\"username1@nicedomain.com\",\"date\":\"2022-03-24 09:59:48.000000000\",\"tz\":60},\"subject\":\"nice subject :: Set compact theme\",\"message\":\"nice subject :: Set compact theme\\n\\nChange-Id: Id145cfc4s2d6fek18c3539gbi8me1d3e37501568\\nDepends-On: 43431\\n\"},\"files\":{\"sample/application/src/main/java/test0\":{\"lines_inserted\":4,\"lines_deleted\":1,\"size_delta\":94,\"size\":763}},\"description\":\"Rebase\"},\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\":{\"kind\":\"REWORK\",\"_number\":1,\"created\":\"2022-03-23 17:36:05.000000000\",\"uploader\":{\"_account_id\":460},\"ref\":\"refs/changes/49/43449/1\",\"fetch\":{\"ssh\":{\"url\":\"ssh://username@nicedomain.com:29418/test0\",\"ref\":\"refs/changes/49/43449/1\"},\"http\":{\"url\":\"https:/username@nicedomain.com/a/test0\",\"ref\":\"refs/changes/49/43449/1\"}}}},\"requirements\":[]},{\"id\":\"test~Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"project\":\"content\",\"branch\":\"branch\",\"topic\":\"important topic\",\"attention_set\":{\"462\":{\"account\":{\"_account_id\":462},\"last_update\":\"2022-03-18 13:49:30.000000000\",\"reason\":\"A robot voted negatively on a label\"}},\"hashtags\":[],\"change_id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"subject\":\"subject \",\"status\":\"NEW\",\"created\":\"2022-03-18 13:48:25.000000000\",\"updated\":\"2022-03-24 09:59:25.000000000\",\"submit_type\":\"FAST_FORWARD_ONLY\",\"insertions\":0,\"deletions\":0,\"total_comment_count\":0,\"unresolved_comment_count\":0,\"has_review_started\":true,\"meta_rev_id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"_number\":43209,\"owner\":{\"_account_id\":462},\"messages\":[{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"tag\":\"autogenerated:gerrit:newPatchSet\",\"author\":{\"_account_id\":462},\"real_author\":{\"_account_id\":462},\"date\":\"2022-03-18 13:48:25.000000000\",\"message\":\"Uploaded patch set 1.\",\"_revision_number\":1},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-18 13:48:31.000000000\",\"message\":\"Patch Set 1:\\n\\nBuild Started https://nicedomain.com/6341/\",\"_revision_number\":1},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"tag\":\"autogenerated:gerrit:newPatchSet\",\"author\":{\"_account_id\":462},\"real_author\":{\"_account_id\":462},\"date\":\"2022-03-18 13:48:57.000000000\",\"message\":\"Patch Set 2: Commit message was updated.\",\"_revision_number\":2},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-18 13:49:06.000000000\",\"message\":\"Patch Set 2:\\n\\nBuild Started https://nicedomain.com/6342/\",\"_revision_number\":2},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"tag\":\"autogenerated:gerrit:newPatchSet\",\"author\":{\"_account_id\":462},\"real_author\":{\"_account_id\":462},\"date\":\"2022-03-18 13:49:15.000000000\",\"message\":\"Patch Set 3: Commit message was updated.\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-18 13:49:26.000000000\",\"message\":\"Patch Set 3:\\n\\nBuild Started https://nicedomain.com/6343/\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-18 13:49:30.000000000\",\"message\":\"Patch Set 1: Verified-1\\n\\nBuild Failed \\n\\nhttps://nicedomain.com/6341/ : ABORTED\",\"_revision_number\":1},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-18 13:50:13.000000000\",\"message\":\"Patch Set 2: Verified-1\\n\\nBuild Failed \\n\\nhttps://nicedomain.com/6342/ : FAILURE\",\"_revision_number\":2},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-18 13:58:33.000000000\",\"message\":\"Patch Set 3: Verified+1\\n\\nBuild Successful \\n\\nhttps://nicedomain.com/6343/ : SUCCESS\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-23 13:18:33.000000000\",\"message\":\"Patch Set 3: -Verified\\n\\nBuild Started https://nicedomain.com/6363/\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-23 13:28:59.000000000\",\"message\":\"Patch Set 3: Verified+1\\n\\nBuild Successful \\n\\nhttps://nicedomain.com/6363/ : SUCCESS\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-23 15:13:11.000000000\",\"message\":\"Patch Set 3: -Verified\\n\\nBuild Started https://nicedomain.com/6365/\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-23 15:22:38.000000000\",\"message\":\"Patch Set 3: Verified+1\\n\\nBuild Successful \\n\\nhttps://nicedomain.com/6365/ : SUCCESS\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-24 09:49:28.000000000\",\"message\":\"Patch Set 3: -Verified\\n\\nBuild Started https://nicedomain.com/6366/\",\"_revision_number\":3},{\"id\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"author\":{\"_account_id\":7},\"real_author\":{\"_account_id\":7},\"date\":\"2022-03-24 09:59:25.000000000\",\"message\":\"Patch Set 3: Verified+1\\n\\nBuild Successful \\n\\nhttps://nicedomain.com/6366/ : SUCCESS\",\"_revision_number\":3}],\"current_revision\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"revisions\":{\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\":{\"kind\":\"REWORK\",\"_number\":1,\"created\":\"2022-03-18 13:48:25.000000000\",\"uploader\":{\"_account_id\":462},\"ref\":\"refs/changes/09/43209/1\",\"fetch\":{\"ssh\":{\"url\":\"ssh://username@nicedomain.com/a/content/\",\"ref\":\"refs/changes/09/43209/1\"},\"http\":{\"url\":\"https:/username@nicedomain.com/a/content\",\"ref\":\"refs/changes/09/43209/1\"}}},\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501561\":{\"kind\":\"NO_CODE_CHANGE\",\"_number\":3,\"created\":\"2022-03-18 13:49:15.000000000\",\"uploader\":{\"_account_id\":462},\"ref\":\"refs/changes/09/43209/3\",\"fetch\":{\"ssh\":{\"url\":\"ssh://username@nicedomain.com/a/content/\",\"ref\":\"refs/changes/09/43209/3\"},\"http\":{\"url\":\"https://username@nicedomain.com/a/content\",\"ref\":\"refs/changes/09/43209/3\"}},\"commit\":{\"parents\":[{\"commit\":\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\",\"subject\":\"important subject\"}],\"author\":{\"name\":\"username2\",\"email\":\"username2@nicedomain.com\",\"date\":\"2022-03-18 13:43:45.000000000\",\"tz\":60},\"committer\":{\"name\":\"username2\",\"email\":\"username2@nicedomain.com\",\"date\":\"2022-03-18 13:49:15.000000000\",\"tz\":60},\"subject\":\"subject \",\"message\":\"subject \\n\\nDepends-On: 43207\\nDepends-On: test1:branch\\nChange-Id: Id145cfc4s2d6fek18c3539gbi8me1d3e37501560\\n\"},\"files\":{},\"description\":\"Edit commit message\"},\"Id145cfc4s2d6fek18c3539gbi8me1d3e37501562\":{\"kind\":\"NO_CODE_CHANGE\",\"_number\":2,\"created\":\"2022-03-18 13:48:57.000000000\",\"uploader\":{\"_account_id\":462},\"ref\":\"refs/changes/09/43209/2\",\"fetch\":{\"ssh\":{\"url\":\"ssh://username@nicedomain.com/a/content/\",\"ref\":\"refs/changes/09/43209/2\"},\"http\":{\"url\":\"https://username@nicedomain.com/a/content\",\"ref\":\"refs/changes/09/43209/2\"}},\"description\":\"Edit commit message\"}},\"_more_changes\":true,\"requirements\":[]}]")));
    }


    /**
     *if the url-check returns false, an exception should be thrown
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
            public boolean isSshValid() {
                return false;
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
     *  only connect to rest endpoint(!=ssh) when configuration.getPreferRestConnection is true
     */
    @Test
    public void testPreferRest()
    {
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
     *  test correct fetching of events. Also, dependent on GerritChange and GerritPatchSet
     */
    @Test
    public void testGetReviewsFromGerritOutput()
    {
        setupMockServer();
        setupStubForGetReviews("restApiTest_shortResponse");

        //":" is converted to "%3A1". All requests sent can be seen from wireMockServer.getAllServeEvents()
        try {
            //in reality Gerrit would only send one change (limit:1)
            List<GerritChange> changes = issueReviewsCacheLoader.getReviewsFromGerrit("limit:1");
            System.out.println("Loaded " + changes.size() + " changes:\n" + changes);
            Assert.assertEquals(2, changes.size());
        } catch (GerritQueryException e) {
            Assert.fail(e.getMessage());
        }

        shutdownMockServer();
    }

}
