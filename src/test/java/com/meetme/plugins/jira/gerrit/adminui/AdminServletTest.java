package com.meetme.plugins.jira.gerrit.adminui;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.meetme.plugins.jira.gerrit.data.GerritConfiguration;
import com.meetme.plugins.jira.gerrit.data.GerritConfigurationImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminServletTest {

    public final static int port = 8020;

    private final String username = "user123";

    private final String password = "password123";

    private final String url = "http://localhost:" + port;

    private final String query = "/a/changes/?q=limit%3A1";

    private final HashMap<String, Object> map = new HashMap<>();

    private AdminServlet adminServlet;

    private GerritConfigurationImpl gerritConfiguration;

    private WireMockServer wireMockServer;


    @Before
    public void setupAdminServlet() {
        adminServlet = new AdminServlet(null, null, null, null, null, null);

        gerritConfiguration = new GerritConfigurationImpl(new PluginSettingsFactory() {
            @Override
            public PluginSettings createSettingsForKey(String key) {
                return new PluginSettings() {
                    @Override
                    public Object get(String key) {
                        return map.get(key);
                    }

                    @Override
                    public Object put(String key, Object value) {
                        return map.put(key, value);
                    }

                    @Override
                    public Object remove(String key) {
                        return map.remove(key);
                    }
                };
            }

            @Override
            public PluginSettings createGlobalSettings() {
                return null;
            }
        });
        setupMockServer();
    }


    /**
     * Initialise the mock-REST-API, call
     * {@link com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandlerHttp#queryJava(String, boolean, boolean, boolean)}.
     */
    public void setupMockServer() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(port));
        wireMockServer.start();
    }

    /**
     * Shut down the mock-REST-API.
     */
    @After
    public void shutdownMockServer() {
        wireMockServer.stop();
    }

    /**
     * setup stub for wireMockServer.
     *
     * @param expectedCode the statuscode
     */
    public void setupStubForRequests(int expectedCode) {
        ///a/changes/?q=limit%3A1
        wireMockServer.stubFor(get(urlEqualTo(query))
                .withBasicAuth(username, password)
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(expectedCode)
                        .withBody("\n []")));
    }


    /**
     * test {@link AdminServlet#performConnectionTest(GerritConfiguration, Map)}.
     * As is, only connections to the REST-endpoint are tested
     */
    @Test
    public void testPerformConnectionTest() {
        //setting all required fields
        gerritConfiguration.setPreferRestConnection(true);
        gerritConfiguration.setHttpBaseUrl(url);
        gerritConfiguration.setHttpUsername(username);
        gerritConfiguration.setHttpPassword(password);

        setupStubForRequests(200);
        adminServlet.performConnectionTest(gerritConfiguration, map);
        assertTrue((Boolean) map.get("testResult"));

        setupStubForRequests(400);
        adminServlet.performConnectionTest(gerritConfiguration, map);
        assertFalse((Boolean) map.get("testResult"));
        assertTrue(((String) map.get("testError")).endsWith("(400)"));

        setupStubForRequests(401);
        adminServlet.performConnectionTest(gerritConfiguration, map);
        assertFalse((Boolean) map.get("testResult"));
        assertTrue(((String) map.get("testError")).endsWith("(401)"));
        //System.out.println(map.toString());

        setupStubForRequests(404);
        adminServlet.performConnectionTest(gerritConfiguration, map);
        assertFalse((Boolean) map.get("testResult"));
        assertTrue(((String) map.get("testError")).endsWith("(404)"));
        //System.out.println(map.toString());

        //testing other response code
        setupStubForRequests(418);
        adminServlet.performConnectionTest(gerritConfiguration, map);
        assertFalse((Boolean) map.get("testResult"));
        assertTrue(((String) map.get("testError")).endsWith("(418)"));
        //System.out.println(map.toString());
    }


    /**
     * Useful for testing against real servers
     */
    //@Test
    public void testArbitraryServer() {
        //fill out
        final String username = "";
        final String password = "";
        final String url = "";

        gerritConfiguration.setPreferRestConnection(true);
        gerritConfiguration.setHttpBaseUrl(url);
        gerritConfiguration.setHttpUsername(username);
        gerritConfiguration.setHttpPassword(password);

        adminServlet.performConnectionTest(gerritConfiguration, map);
        System.out.println("Testresult: " + map.get("testResult"));
    }

}