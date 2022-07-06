JIRA-Gerrit Rest-Api integration plugin [![CI](https://github.com/faktorzehn/jira-gerrit-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/faktorzehn/jira-gerrit-plugin/actions/workflows/build.yml)
==============================
&copy; Copyright 2012 MeetMe, Inc.
Maintainer: Joe Hansche <jhansche@meetme.com>

This is a fork of the original JIRA-Gerrit integration plugin but with the added functionality
of being able to choose fetching changes from either the pre-existing SSH-endpoint or utilising **Gerrit's 
Rest-Api**. This feature requires a modified gerrit-events-dependency, which source code can be found [here](https://github.com/faktorzehn/gerrit-events).
The added functionality is fully tested.

Overview of the changes
---------
* the ``admin.vm`` template now features Rest-configuration options
* the existing gerrit-events-dependency has been replaced by a [modified one](https://github.com/faktorzehn/gerrit-events), which can establish connections to rest-endpoints. 
  * the edited dependency is kept in a local repository at ```/src/main/ressources/gerrit-events```,
* ``AdminServlet`` can, in addition to the SSH-endpoint, also test its connection to Gerrit's rest-api-endpoint.
* ``IssueReviewsCacheLoader`` can use gerrit-event's new ``GerritQueryHandlerHttp``-class to cache changes.
* both ``GerritChange`` and ``GerritPatchSet`` have "fromJsonRest()" functionality. 

- - - -
- - - -


Licensing
---------
Please see the file named LICENSE.


Install
-------
* Available in the [JIRA Marketplace](https://marketplace.atlassian.com/plugins/com.meetme.plugins.jira.gerrit-plugin)
  for automatic installation from the JIRA "Find New Plugins" administration
  panel.
* Alternatively, you can also download the JAR from the above URL, and install
  it manually from the "Manage Plugins" administration panel.


Getting Started
---------------
* Generate an SSH keypair for the JIRA user
    * Save the private key in a place where you can upload it to JIRA (1)
    * The public key will simply be copied/pasted (2)
* Create a new user in Gerrit that you will use for the integration:

        $ ssh gerrit.company.com -p 29418 gerrit create-account jira --email jira@company.com --full-name JIRA --ssh-key <paste public key (2)>

* In JIRA, navigate to Administration > Plugins > Gerrit Admin
* Enter hostname, port, and user under the SSH section
* Upload the SSH private key generated at (1) above
* Optionally change the Gerrit search query patterns:
    * `tr:%s` - Look for the issue key in a "Bug:" or "Issue:" footer
    * `topic:%s` - Look for the issue key in the change Topic (uploaded using
      `HEAD:refs/for/master/(issueKey)`)
    * `message:%s` - Look for the issue key in the commit message


Features
--------
* "Gerrit Reviews" issue tab panel to show all reviews related to the issue
* "Gerrit Subtask Reviews" issue tab panel to show reviews related to all
  the issue's subtasks
* Workflow condition to require that an issue must (or must not) have any
  open reviews
* Workflow condition to require that an issue must (or must not) have a certain
  approval score (e.g., Code-Review == 2, or ! Verified < 0)
* Workflow function to perform a Gerrit review
    * This is just an argument to the `gerrit review [ChangeId] ..` command, so
      it could be something like `--verified +1` to give a +1 score;
      or `--submit` to submit the change
    * The Gerrit user configured in the admin panel must have access to perform
      all necessary steps

TODO
----
* Unit Tests (partial)
* Possibly per-user SSH key configurations (instead of everything done as
  the JIRA user)
* Possibly allow a second "suexec" SSH key so the JIRA user can spoof the
  acting JIRA user as a Gerrit user
* ~~Extend SshConnection in order to obtain stderr content, to know if a
  command failed~~; this is no longer required, after our patch was accepted
  to the [gerrit-trigger-plugin](https://github.com/jenkinsci/gerrit-trigger-plugin/pull/26)
  project
* Resolve `gerrit-events` dependency so it does not rely on a SNAPSHOT version
