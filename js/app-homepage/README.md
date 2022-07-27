# Application Homepage in HTML/CSS/JS

A sample application that can display UI in an iframe on Application page, written in HTML/CSS/JS. To run the
application serve the HTML/CSS/JS as a static content via HTTPS. One way to do this locally for testing is to serve
the content via `npx http-server` and run `ngrok http 8080` to have a public HTTPS URL.

This is a single-org application, meaning that it can only be installed manually to specific Space organization. To
distribute an application to many organizations, a server which accepts requests from Space instances is required.
You can find the example of such application in `kotlin/app-homepage`.

Read more about single- and multi-org
applications [in the documentation](https://www.jetbrains.com/help/space/distribute-your-application.html).

## Messages between iframe and Space UI

You can send messages to Space web page from the iframe page using `postMessage` method in browser API. The response
is sent back via a `port` in provided `MessageChannel`. See `iframe/auth.js` for example, where user token is
retrieved.

The iframe can also make HTTP requests to Space HTTP API after obtaining user token. See `iframe/team.js` for example,
where team members are retrieved from Space.

## Messages iframe can send to Space webpage

All the messages should be sent in the same way:

1. Create a `MessageChannel`
2. Subscribe to the response in the channel
3. Call `postMessage`

For example, to get user token from Space:

```javascript
function getUserAccessTokenData(askForConsent) {
    return new Promise((resolve) => {
        // 1. Create a MessageChannel
        const channel = new MessageChannel();
        // 2. Subscribe to response
        channel.port1.onmessage = e => resolve(e.data);
        // 3. Call postMessage
        window.parent.postMessage({
            type: "GetUserTokenRequest",
            permissionScope: "global:Profile.View global:Profile.Memberships.View",
            askForConsent: askForConsent
        }, "*", [channel.port2]);
    });
}
```

The first parameter of `postMessage` is the data object, which is the message contents. Each such object must contain
a `type` field. Other fields in the object depend on the `type`.

The following message types are currently supported:

- `GetUserTokenRequest`
- `GetThemePropertiesRequest`
- `RedirectWithConfirmationRequest`
- `ShowConfirmDialogRequest`

### GetUserTokenRequest

Requests an access token that can be used for the following:
- access Space HTTP API on behalf of the user
- extract information from within the access token: get user identifier in Space, `clientId` of the installed 
application and Space URL (Space URL is also returned explicitly in the response for GetUserTokenRequest). The 
token is a JWT token, the information can be retrieved from its claims.

Input parameters:
- `permissionScope`. Required. A string with permissions that the token should have.

Examples:
* Viewing profiles and viewing team memberships: `global:Profile.View global:Profile.Memberships.View`
* View issues in the `MY-PROJECT-KEY` project: `project:key:MY-PROJECT-KEY:Project.Issues.View`
* View messages in the channel with id `42P9E54DAkJW`: `channel:42P9E54DAkJW:Channel.ViewMessages`

When passing several permissions, separate them with a space. To learn more about permissions and permission scopes,
see [Request Permissions documentation topic](https://www.jetbrains.com/help/space/request-permissions.html).

- `askForConsent`. Required. Possible values: `true`, `false`.

When `true` is passed, a dialog is displayed in Space UI asking the current user to grant the application 
the permission to act on their behalf. The dialog lists all the permissions passed in `permissionScope` that 
are not already granted by the user. If user grants the permissions, a token is generated and returned to the
iframe.

When `false` is passed, the dialog is not displayed. The token is only generated and returned to the iframe, if
the current Space user has previously granted all the permissions requested in `permissionScope` to the app.

This parameter allows the application to first ask Space silently for the token and then, if the token is not
returned, present a UI to the user, explaining the need to authorize the application in Space. This logic is
implemented in `iframe/app.js`.

Response: an object or `null`. The object is returned if user token has been successfully generated. The object
contains two fields:
- `token`. The access token that can be used as a bearer authorization token in Space HTTP API calls (see example in `iframe/team.js`)
- `serverUrl`. URL of the current Space organization. Use this server url to make HTTP API requests to Space.

### GetThemePropertiesRequest

Space web UI has its colors and fonts. Colors can change when the user changes the UI theme (light/dark).

An application may want to use the same colors and fonts in the iframe that Space itself uses, so that the user
experience is smoother when working with the app.

CSS styles are not available, only color and font variables are, so this mechanism cannot be used to mimic Space UI
completely. Currently, there is no convenient automatic way to get Space CSS styles (you can look in the browser dev 
tools), but it may be added in the future.

Input parameters:
- `subscribeForUpdates`. Not required. Possible values: `true`, `false`. Default: `false`.

When `true` is passed, the application iframe is subscribed for the changes in the current selected theme. When the
user changes the theme (light/dark), the iframe will receive a message with the updated CSS variables, and can update
its UI on the fly. You can see the example of corresponding code in `iframe/theme.js`.

Response: an object with the following properties:
- `properties`: an array of objects. Each object has two properties: `name` and `value`. The `name` is the name of the
CSS variable and the `value` is its `value`. If you'd like to use the variables in your application, you need to loop
through the array and set the variables for the iframe document. You can look at the example of such code in `iframe/theme.js`.
- `isDark`: a boolean value, `true` or `false`. Designates whether the currently selected UI theme is dark.

### RedirectWithConfirmationRequest

Use this message to redirect the top-level window to a different webpage. The iframe is prohibited to do the redirect
on its own, for security purposes. Doing the redirect may be needed when authorizing a user in a third-party system.

A confirmation dialog will be shown before the redirect occurs.

Input parameters:
- `redirectUrl`. Required. The URL that the top-level window should be redirected to.
- `newTab`. Not required. Whether to open the link in a new tab.
- `okButtonText`. Not required. Change the default text ("Proceed") of the OK button in the confirmation dialog.

Response: no response is sent. The code in the iframe should now `await` for it.

### ShowConfirmDialogRequest

Displays a confirmation dialog in Space UI. Can be used instead of implementing a confirmation dialog on your own.

Input parameters:
- `question`. Required. Text for the header of the dialog.
- `okButtonKind`. Not required. Allows to change the default style of the "OK" button.

Possible values: `NORMAL`, `SECONDARY`, `DANGER`, `SUCCESS`, `WARNING`, `PRIMARY`, `PRIMARY_SUCCESS`, `PRIMARY_WARNING`, `ALTERNATIVE`, `ALTERNATIVE_DANGER`, `ALTERNATIVE_SUCCESS`, `ALTERNATIVE_WARNING`

- `okButtonText`. Not required. Allows to change the default text of the "OK" button.
- `description`. Not required. An additional text that will be shown in the dialog.

## Running app locally: step by step

### Prerequisites

- npm (node package manager)
- ngrok (https://ngrok.com/download)

### Serve static content locally

- clone the repository to your local machine
- `cd `into `js/app-homepage` directory
- run `npx http-server -c-1` (`-c-1` disables caching)
- run `ngrok http 8080`

### Register app in Space organization

- open your Space in a web-browser or desktop app
- go to `Extensions` menu (menubar on the left). If the menu isn't there, you can add it via `...` menu.
- create an application by clicking `New application` button
- fill in application name, for example, `Application Homepage test` and click `Create`
- click `Go to Application Settings`
- open `Endpoint` tab
- fill in the `Endpoint URL` with your https ngrok URL and click `Save`
- open `Authentication` tab
- select `Authroization Code Flow`. Fill in the `Code Flow redirect URIs` with your
  ngrok URL. We won't be using redirects in this application, but the parameter is mandatory for now. Click `Save`.
- Now your application needs to declare that it supports `Application Homepage` UI extension. Right now this is only
  possible to do through an HTTP API, so we will do this using `API Playground`.
    - Open the `API Playground` using menu item in the sidebar on the left.
    - Navigate to `Applications` -> `Set UI extensions` method.
    - For input parameter `contextIdentifer` choose `global` in dropdown list
    - Add an extension parameter and choose `ApplicationHomepageUiExtensionIn` from the list
    - On the right choose `Authorize as` -> `Application Homepage test` (or name of your application)
    - Click `Execute`
- Go back your application page, there should be a new `Homepage` tab there