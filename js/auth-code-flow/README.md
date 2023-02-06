# Auth Code Flow in client-side javascript app

This is a client-side Space application (runs in the browser) that uses Auth Code Flow to make requests on behalf of the user. 
You can read more about Auth Code Flow itself in [Space documentation](https://www.jetbrains.com/help/space/authorization-code.html).

The Authorization Flow happens in several steps:

### Getting Auth Code

User clicks `Authorize in Space` button and is redirected to a special Space endpoint, where the user approves
permissions for the application. The Space then redirects the user back, passing Auth Code and state in URL parameters.

### Getting access token

Using the Auth Code from the URL parameters, application makes an HTTP request to exchange Auth Code for access token.
See `auth.js`.

### Using access token

The application uses access token to retrieve current user's username through Space HTTP API. The access token
is cached in local storage for subsequent use. Before using the cached version, token expiration is checked. If the 
token expires soon, a new one is retrieved from Space — the whole Auth Code Flow is repeated.

The access token is a JWT token, its expiration time can be found in `exp` claim. See `jwt.js`.

First time the flow happens, the user is shown authorization page in Space and provides their consent. On subsequent
runs the redirect happens without user interaction (as long as Space cookies are valid and user is still logged 
into Space).

# Running the application

To run the application for your Space organization:

1. Host the static files from the `site` directory and make them accessible on the Internet. To test this locally,
   you can:

- clone the repository to your local machine
- `cd `into `js/auth-code-flow/site` directory
- run `npx http-server -c-1` (`-c-1` disables caching)
- run `ngrok http 8080`

2. Replace the `SPACE_ORG_URL` placeholder in `constants.js` with your organization url
3. Create an application in JetBrains Space (Extensions -> Installed to organization -> New application)
4. Take Client ID and Client secret from the "Authentication" tab and replace the placeholder values for `CLIENT_ID`
   and `CLIENT_SECRET` in `constants.js`.
5. Replace the `APPLICATION_URL` placeholder in `constants.js` with your ngrok URL, for example: `https://e123-94-158-242-146.eu.ngrok.io`
6. Uncheck the `Client Credentials Flow` checkbox on the `Authentication` tab of the application in Space. Client ID and 
Client secret are public for your application. Thus, anyone can make requests through Client Credentials flow if you 
don't turn it off. Do not leave the flow on and do not grant any permissions to the application
itself (you can check the "Authorization" tab for granted permissions).

Instead, with Auth Code Flow get user access token and use it to make requests to Space API on behalf of the user.

6. Set up Auth Code Flow for the application in Space:

- go to "Authentication" tab
- select "Authorization Code Flow" checkbox
- add your ngrok url to the "Code Flow redirect URIs" field, for example: `https://e123-94-158-242-146.eu.ngrok.io`
- select "Require PKCE" checkbox

It is strongly recommended to use PKCE with Auth Code Flow.

7. Open the ngrok url in browser, you should see the running application.
