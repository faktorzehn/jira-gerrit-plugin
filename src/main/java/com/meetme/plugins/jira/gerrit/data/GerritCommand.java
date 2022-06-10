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
package com.meetme.plugins.jira.gerrit.data;

import com.atlassian.core.user.preferences.Preferences;
import com.jcraft.jsch.ChannelExec;
import com.meetme.plugins.jira.gerrit.data.dto.GerritChange;
import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnection;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshConnectionFactory;
import com.sonymobile.tools.gerrit.gerritevents.ssh.SshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class GerritCommand {
    private static final Logger log = LoggerFactory.getLogger(GerritCommand.class);
    private final static String BASE_COMMAND = "gerrit review"; // !=gerrit query
    private GerritConfiguration config;
    private Preferences userPreferences;

    public GerritCommand(GerritConfiguration config, Preferences userPreferences) {
        this.config = config;
        this.userPreferences = userPreferences;
    }

    public boolean doReview(GerritChange change, String args) throws IOException {
        final String command = getCommand(change, args);
        return runCommand(command);
    }

    public boolean doReviews(List<GerritChange> changes, String args) throws IOException {
        String[] commands = new String[changes.size()];
        int i = 0;

        for (GerritChange change : changes) {
            commands[i++] = getCommand(change, args);
        }

        return runCommands(commands);
    }

    private boolean runCommand(String command) throws IOException {
        return runCommands(new String[] { command });
    }

    @SuppressWarnings("deprecation")
    private String getCommand(GerritChange change, String args) {

        // TODO: escape args? Or build manually with String reviewType,int reviewScore,etc..?
        return String.format("%s %s,%s %s", BASE_COMMAND, change.getNumber(), change.getPatchSet().getNumber(), args);
    }

    //Rest support not needed, unused method
    private boolean runCommands(String[] commands) throws IOException {
        boolean success = true;
        SshConnection ssh = null;

        try {
            Authentication auth = getAuthentication();
            ssh = SshConnectionFactory.getConnection(config.getSshHostname(), config.getSshPort(), auth);

            for (String command : commands) {
                if (!runCommand(ssh, command)) {
                    log.warn("runCommand " + command + " returned false");
                    success = false;
                }
            }
        } finally {
            log.info("Disconnecting from SSH");

            if (ssh != null) {
                ssh.disconnect();
            }
        }

        if (log.isDebugEnabled()) {
            log.trace("runCommands " + commands.length + " -> success = " + success);
        }
        return success;
    }

    private Authentication getAuthentication() {
        Authentication auth = null;

        if (userPreferences != null) {
            // Attempt to get a per-user authentication mechanism, so JIRA can act as the user.
            try {
                String privateKey = userPreferences.getString("gerrit.privateKey");
                String username = userPreferences.getString("gerrit.username");

                if (privateKey != null && username != null && !privateKey.isEmpty() && !username.isEmpty()) {
                    File privateKeyFile = new File(privateKey);

                    if (privateKeyFile.exists() && privateKeyFile.canRead()) {
                        auth = new Authentication(privateKeyFile, username);
                    }
                }
            } catch (Exception exc) {
                auth = null;
            }
        }

        if (auth == null) {
            auth = new Authentication(config.getSshPrivateKey(), config.getSshUsername());
        }

        return auth;
    }

    private boolean runCommand(SshConnection ssh, String command) throws SshException, IOException {
        boolean success;
        ChannelExec channel = null;

        log.info("Running command: " + command);

        BufferedReader reader = null;

        try {
            channel = ssh.executeCommandChannel(command);

            String incomingLine;

            InputStreamReader out = new InputStreamReader(channel.getInputStream());
            reader = new BufferedReader(out);

            while ((incomingLine = reader.readLine()) != null) {
                // We don't expect any response anyway..
                // But we can get the response and return it if we need to
                log.trace("Incoming line: " + incomingLine);
            }

            reader.close();

            InputStreamReader err = new InputStreamReader(channel.getErrStream());
            reader = new BufferedReader(err);

            while ((incomingLine = reader.readLine()) != null) {
                // We don't expect any response anyway..
                // But we can get the response and return it if we need to
                log.warn("Error: " + incomingLine);
            }
            err.close();
            reader.close();

            int exitStatus = channel.getExitStatus();
            success = exitStatus == 0;
            log.info("Command exit status: " + exitStatus + ", success=" + success);
        } finally {
            if(channel!=null && channel.isConnected()) {
                channel.disconnect();
            }
            if(reader!=null) {
                reader.close();
            }
        }
        return success;
    }
}
