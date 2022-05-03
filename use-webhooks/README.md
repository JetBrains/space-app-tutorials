# How to use Space webhooks
This sample application demonstrates how you can use webhooks to receive notifications
about events in Space. The application tracks two types of events:
* Adding a new user to the organization: once a user is added, the application sends a 'welcome' message to the user.
* Adding a user to a team: once a user is added to the team, the application sends an on-boarding message to the user.

## Installation

You can install the application by creating and following an installation link:

https://<my-space-org>.jetbrains.space/extensions/installedApplications/new?name=Webhooks%20example&endpoint=<endpoint-https-url-encoded>

On installation the application server receives an `InitPayload` request from Space. On that request application
registers necessary webhooks in the Space organization that it was installed to.

Application can be installed to many organizations. `clientId`, `clientSecret` and `serverUrl` will be different
for each organization, they are saved into a Postgres database when `InitPayload` is received.