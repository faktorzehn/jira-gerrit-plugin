package com.meetme.plugins.jira.gerrit.data;

import com.meetme.plugins.jira.gerrit.data.dto.GerritChange;

import com.atlassian.cache.CacheException;
import com.atlassian.cache.CacheLoader;
import com.sonymobile.tools.gerrit.gerritevents.GerritQueryException;
import com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandler;
import com.sonymobile.tools.gerrit.gerritevents.GerritQueryHandlerHttp;
import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshException;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

public class IssueReviewsCacheLoader implements CacheLoader<String, List<GerritChange>> {
    private final Logger log = LoggerFactory.getLogger(IssueReviewsCacheLoader.class);
    private final GerritConfiguration configuration;
    //private final List<Float> avgLoadTime;

    public IssueReviewsCacheLoader(GerritConfiguration configuration) {
        this.configuration = configuration;
        //this.avgLoadTime = new ArrayList<>();
    }

    @Nonnull
    @Override
    public List<GerritChange> load(@Nonnull String key) {

        String query = String.format(Locale.US, configuration.getIssueSearchQuery(), key);

        try {
            return getReviewsFromGerrit(query);
        } catch (GerritQueryException e) {
            log.error("Error querying for issues", e);
            throw new CacheException("Error querying for issues: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("CommentedOutCode") //optional benchmarking
    protected List<GerritChange> getReviewsFromGerrit(String searchQuery) throws GerritQueryException {
        //long startTime = System.nanoTime();

        List<GerritChange> changes;
        List<JSONObject> reviews = new ArrayList<>();

        if (configuration.getPreferRestConnection()) {
            if (!configuration.isHttpValid()) {
                throw new GerritConfiguration.NotConfiguredException("Not configured for HTTP access");
            }

            GerritQueryHandlerHttp.Credential credential = new GerritQueryHandlerHttp.Credential() {
                @Override
                public Principal getUserPrincipal() {
                    return new Principal() {
                        @Override
                        public String getName() {
                            return configuration.getHttpUsername();
                        }
                    };
                }

                @Override
                public String getPassword() {
                    return configuration.getHttpPassword();
                }
            };

            GerritQueryHandlerHttp query = new GerritQueryHandlerHttp(configuration.getHttpBaseUrl().toString(), credential, "");
            try {
                reviews = query.queryJava(searchQuery, false, true, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (configuration.isSshInvalid()) {
                throw new GerritConfiguration.NotConfiguredException("Not configured for SSH access");
            }

            Authentication auth = new Authentication(configuration.getSshPrivateKey(), configuration.getSshUsername());
            GerritQueryHandler query = new GerritQueryHandler(configuration.getSshHostname(), configuration.getSshPort(), null, auth);

            try {
                reviews = query.queryJava(searchQuery, false, true, false);
            } catch (SshException e) {
                throw new GerritQueryException("An ssh error occurred while querying for reviews.", e);
            } catch (IOException e) {
                throw new GerritQueryException("An error occurred while querying for reviews.", e);
            }
        }

        changes = new ArrayList<>(reviews.size());

        for (JSONObject obj : reviews) {
            if (obj.has("type") && "stats".equalsIgnoreCase(obj.getString("type"))) {
                // The final JSON object in the query results is just a set of statistics
                if (log.isDebugEnabled()) {
                    log.trace("Results from QUERY: " + obj.optString("rowCount", "(unknown)") + " rows; runtime: "
                            + obj.optString("runTimeMilliseconds", "(unknown)") + " ms");
                }
                continue;
            }
            changes.add(new GerritChange(obj, configuration.getPreferRestConnection()));
        }

        Collections.sort(changes);
        //long endTime = System.nanoTime();
        //avgLoadTime.add(((float) endTime - (float) startTime) / 1000000f);
        //log.debug("Average query execution time: " + avgLoadTime.stream().mapToDouble(a -> a).average().getAsDouble() + "ms");

        return changes;
    }
}
