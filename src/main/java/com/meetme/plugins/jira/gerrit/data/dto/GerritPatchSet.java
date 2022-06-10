/*
 * Copyright 2012 MeetMe, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.meetme.plugins.jira.gerrit.data.dto;

import com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritChangeKind;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.PatchSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * full credits to https://github.com/lauyoo46 for his parsing methods
 * {@link com.meetme.plugins.jira.gerrit.data.dto.GerritPatchSet#fromJsonHTTP(JSONObject)} and
 * {@link com.meetme.plugins.jira.gerrit.data.dto.GerritPatchSet#extractApprovals(JSONObject)}
 * https://github.com/lauyoo46/jira-gerrit-plugin
 */
public class GerritPatchSet extends PatchSet {
    private static final Logger log = LoggerFactory.getLogger(GerritPatchSet.class);

    private List<GerritApproval> approvals;

    public GerritPatchSet() {
        super();
    }

    public GerritPatchSet(JSONObject json) {
        super(json);
    }

    public GerritPatchSet(JSONObject json, boolean preferRest) {
        if(!preferRest) {
            fromJson(json);
        }
        else {
            fromJsonHTTP(json);
        }
    }


    @Override
    public void fromJson(JSONObject json) {
        log.debug("GerritPatchSet from json: " + json.toString(4, 0));
        super.fromJson(json);

        if (json.containsKey(GerritEventKeys.APPROVALS)) {
            JSONArray eventApprovals = json.getJSONArray(GerritEventKeys.APPROVALS);
            approvals = new ArrayList<GerritApproval>(eventApprovals.size());

            for (int i = 0; i < eventApprovals.size(); i++) {
                GerritApproval approval = new GerritApproval(eventApprovals.getJSONObject(i));
                approvals.add(approval);
            }
        } else {
            log.warn("GerritPatchSet contains no approvals key.");
        }
    }

    private void fromJsonHTTP(JSONObject json) {
        log.debug("GerritPatchSet from json HTTP: {}", json.toString(4, 0));

        String revision = GerritJsonEventFactory.getString(json, "current_revision");
        JSONObject jsonRevision = json.getJSONObject("revisions").getJSONObject(revision);
        this.setNumber(GerritJsonEventFactory.getString(jsonRevision, "_number"));
        this.setRevision(revision);
        this.setDraft(GerritJsonEventFactory.getBoolean(jsonRevision, "isDraft"));

        String dateCreated = jsonRevision.getString("created");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.setCreatedOn(sdf.parse(dateCreated));
        } catch (ParseException e) {
            log.error("Error when trying to format date! " , e);
        }
        if (jsonRevision.containsKey("kind")) {
            this.setKind(GerritChangeKind.fromString(GerritJsonEventFactory.getString(jsonRevision, "kind")));
        }
        this.setRef(GerritJsonEventFactory.getString(jsonRevision, "ref"));
        if (jsonRevision.containsKey("uploader")) {
            this.setUploader(new Account(jsonRevision.getJSONObject("uploader")));
        }
        extractApprovals(json);
    }

    private void extractApprovals(JSONObject json) {
        if (json.containsKey(GerritEventKeys.APPROVALS)) {
            JSONArray eventApprovals = json.getJSONArray(GerritEventKeys.APPROVALS);
            approvals = new ArrayList<>(eventApprovals.size());

            for (int i = 0; i < eventApprovals.size(); i++) {
                GerritApproval approval = new GerritApproval(eventApprovals.getJSONObject(i));
                approvals.add(approval);
            }
        } else {
            log.warn("GerritPatchSet contains no approvals key.");
        }
    }

    public List<GerritApproval> getApprovals() {
        return approvals;
    }

    public Map<String, List<GerritApproval>> getApprovalsByLabel() {
        Map<String, List<GerritApproval>> map = new HashMap<>();
        List<GerritApproval> l;

        for (GerritApproval approval : approvals) {
            String type = approval.getType();

            l = map.computeIfAbsent(type, k -> new ArrayList<>());

            l.add(approval);
        }

        return map;
    }

    public List<GerritApproval> getApprovalsForLabel(String label) {
        List<GerritApproval> filtered = new ArrayList<>();

        if (approvals != null) {
            for (GerritApproval approval : approvals) {
                if (approval.getType().equals(label)) {
                    filtered.add(approval);
                }
            }
        }
        return filtered;
    }

    public void setApprovals(List<GerritApproval> approvals) {
        this.approvals = approvals;
    }
}
