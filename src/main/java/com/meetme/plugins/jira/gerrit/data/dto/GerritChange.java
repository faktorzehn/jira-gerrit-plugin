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

import com.meetme.plugins.jira.gerrit.tabpanel.GerritEventKeys;

import com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritChangeStatus;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change;

import com.sonymobile.tools.gerrit.gerritevents.dto.rest.Topic;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.meetme.plugins.jira.gerrit.tabpanel.GerritEventKeys.LAST_UPDATED;

/**
 * @author Joe Hansche
 *
 * full credits to https://github.com/lauyoo46 for his parsing methods {@link GerritChange#fromJsonRest(JSONObject)},
 * {@link GerritChange#convertApprovals(JSONObject)} and {@link GerritChange#addApproval(JSONArray, JSONObject, String)}
 * https://github.com/lauyoo46/jira-gerrit-plugin
 */
public class GerritChange extends Change implements Comparable<GerritChange> {

    private Date lastUpdated;

    private GerritPatchSet patchSet;

    private boolean isOpen;

    private String status;

    private boolean preferRest;

    public GerritChange() {
        super();
    }

    public GerritChange(JSONObject obj, boolean preferRest) {
        this.preferRest = preferRest;
        if(!preferRest) {
           fromJson(obj);
        }
        else {
            fromJsonRest(obj);
        }
    }

    /**
     * Sorts {@link GerritChange}s in order by their Gerrit change number.
     * <p>
     * TODO: To be completely accurate, the changes should impose a dependency-tree ordering (via
     * <tt>--dependencies</tt> option) to GerritQuery! It is possible for an earlier ChangeId to be
     * refactored such that it is then dependent on a <i>later</i> change!
     */
    @Override
    @SuppressWarnings("deprecation")
    public int compareTo(GerritChange obj) {
        if (this != obj && obj != null) {
            int aNum = Integer.parseInt(this.getNumber());
            int bNum = Integer.parseInt(obj.getNumber());

            if (aNum == bNum) {
                return 0;
            } else {
                return aNum < bNum ? -1 : 1;
            }
        }

        return 0;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);

        this.lastUpdated = new Date(1000 * json.getLong(LAST_UPDATED));

        if (json.containsKey(GerritEventKeys.CURRENT_PATCH_SET)) {
            this.patchSet = new GerritPatchSet(json.getJSONObject(GerritEventKeys.CURRENT_PATCH_SET));
        }

        if (json.containsKey(GerritEventKeys.STATUS)) {
            this.setStatus(json.getString(GerritEventKeys.STATUS));
        }

        this.isOpen = json.getBoolean(GerritEventKeys.OPEN);
    }

    public void fromJsonRest(JSONObject json) {

        this.setProject(GerritJsonEventFactory.getString(json, "project"));
        this.setBranch(GerritJsonEventFactory.getString(json, "branch"));
        this.setId(GerritJsonEventFactory.getString(json, "change_id"));
        this.setNumber(GerritJsonEventFactory.getString(json, "_number"));
        this.setSubject(GerritJsonEventFactory.getString(json, "subject"));
        this.setWip(GerritJsonEventFactory.getBoolean(json, "wip", false));
        this.setPrivate(GerritJsonEventFactory.getBoolean(json, "private", false));
        if (json.containsKey("owner")) {
            this.setOwner(new Account(json.getJSONObject("owner")));
        }
        if(json.containsKey("current_revision")) {
            String revision = GerritJsonEventFactory.getString(json, "current_revision");
            JSONObject jsonRevision = json.getJSONObject("revisions").getJSONObject(revision);
            JSONObject jsonCommit = jsonRevision.getJSONObject("commit");
            if (jsonCommit.containsKey("message")) {
                String commitMessage = GerritJsonEventFactory.getString(jsonCommit, "message");
                this.setCommitMessage(commitMessage);
            }
        }
        if (json.containsKey("topic")) {
            String topicName = GerritJsonEventFactory.getString(json, "topic");
            if (StringUtils.isNotEmpty(topicName)) {
                this.setTopicObject(new Topic(topicName));
            }
        }
        if(json.containsKey("url")) {
            JSONObject urlJson = json.getJSONObject("url");
            String scheme = GerritJsonEventFactory.getString(urlJson, "scheme");
            String schemeSpecificPart = GerritJsonEventFactory.getString(urlJson, "schemeSpecificPart");
            String number = GerritJsonEventFactory.getString(json, "_number");
            //NOT implemented atm
            /*if (!scheme.contains(com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.URL_PREFIX)) {
                scheme = com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.URL_PREFIX + scheme;
            }
             */
            String url = scheme + ":" + schemeSpecificPart + "/" + number;
            this.setUrl(url);
        }
        String dateUpdated = GerritJsonEventFactory.getString(json,"updated");
        String dateCreated = GerritJsonEventFactory.getString(json, "created");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.setCreatedOn(sdf.parse(dateCreated));
            this.lastUpdated = sdf.parse(dateUpdated);
            super.setLastUpdated(lastUpdated);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        convertApprovals(json);
        this.patchSet = new GerritPatchSet(json, preferRest);

        if (json.containsKey(GerritEventKeys.STATUS)) {
            this.setStatus(GerritChangeStatus.valueOf(json.getString(com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.STATUS)));
            super.setStatus(this.getStatus());
        }

        String stringStatus = this.getStatus().toString();
        this.isOpen = stringStatus.equals("NEW");

    }

    private void convertApprovals(JSONObject json)
    {
        if (json.containsKey("labels")) {

            ArrayList<String> approvalLabels = new ArrayList<>();
            approvalLabels.add("Verified");
            approvalLabels.add("Code-Review");
            approvalLabels.add("Validated");
            approvalLabels.add("Priority");

            JSONObject jsonApprovals = json.getJSONObject("labels");
            JSONArray approvals = new JSONArray();

            for (String label: approvalLabels) {
                addApproval(approvals, jsonApprovals, label);
            }
            if(!approvals.isEmpty()) {
                json.element(GerritEventKeys.APPROVALS, approvals);
            }
        }
    }

    private void addApproval(JSONArray approvals, JSONObject jsonApprovals, String label) {

        JSONObject approvalForLabel = new JSONObject();
        if(jsonApprovals.containsKey(label)) {
            JSONArray dataForLabelArray = jsonApprovals.getJSONObject(label).getJSONArray("all");
            for (int i = 0; i < dataForLabelArray.size(); ++i) {
                JSONObject dataForLabelObject = dataForLabelArray.getJSONObject(i);
                if (!dataForLabelObject.getString("username").equals("builderbot")
                        && !dataForLabelObject.getString("value").equals("0")) {
                    String value = dataForLabelObject.getString("value");
                    String date = dataForLabelObject.getString("date");
                    String name = dataForLabelObject.getString("name");
                    String username = dataForLabelObject.getString("username");
                    JSONObject approvalBy = new JSONObject();
                    approvalBy.element("name", name);
                    approvalBy.element("username", username);
                    approvalForLabel.element("type", label);
                    approvalForLabel.element("description", label);
                    approvalForLabel.element("value", value);
                    approvalForLabel.element("grantedOn", date);
                    approvalForLabel.element("by", approvalBy);
                    approvals.element(approvalForLabel);
                }
            }
        }
    }


    public Date getLastUpdated() {
        return lastUpdated;
    }

    public GerritPatchSet getPatchSet() {
        return patchSet;
    }

    public String getStatusString() {
        return status;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void setPatchSet(GerritPatchSet patchSet) {
        this.patchSet = patchSet;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
