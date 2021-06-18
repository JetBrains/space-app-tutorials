# How to use Space webhooks
This sample application demonstrates how you can use webhooks to receive notifications
about events in Space. The purpose of the application is to track two types of events:
* Adding a new user to the organization: Once a user is added, the application sends a 'welcome' message to the user.
* Adding a user to a team: Once a user is added to a team, the application sends an onboarding message to the user.

## Prerequisites
In order the application to work, you must [create a webhook](https://jetbrains.com/help/space/add-webhooks.html#creating-a-webhook) and [subscribe it](https://jetbrains.com/help/space/add-webhooks.html#subscribing-to-events) to the following events: `Member joined organization`, `TeamMembership created`, `TeamMembership updated`