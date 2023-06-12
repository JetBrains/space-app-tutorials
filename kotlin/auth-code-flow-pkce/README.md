# Authorization Code Flow

This sample application demonstrates how you can use Authorization Code Flow with PKCE extension to authenticate user in Space and authorize the app to act on their behalf.

The application is a website. On the home page, the user can press "Authorize in Space and show username" button, in which case they will be redirected to Space. After they authenticate and give consent for the app to view member profiles, they are taken back to the website, where they are greeted by their username. If the authentication and authorization was already done, the redirection happens instantly.

## Installing the app into Space

1. Open your Space instance.
2. On the main menu, click **Extensions** and choose **Installed**.
3. Click **New application**.
4. Give the application a name, say, `auth-code-demo` and click **Create**.
5. Open the **Authentication** tab. Note that the **Client Credentials Flow** is enabled for all applications by default. The app doesn't need it, so you should disable it. Enable the **Authorization Code Flow** instead.
6. Check the **Require PKCE** checkbox.
7. Copy the application's **Client ID** and **Client secret**, and pass them as environment variables (`JB_SPACE_CLIENT_ID` and `JB_SPACE_CLIENT_SECRET`) to the app.
8. Change `spaceUrl` constant, or extract it to be configurable.

## Running application locally

Run `./gradlew run` command to start the application.

Make sure that `spaceUrl` constant refers to your organization, and that `JB_SPACE_CLIENT_ID` and `JB_SPACE_CLIENT_SECRET` environment variables are passed.
