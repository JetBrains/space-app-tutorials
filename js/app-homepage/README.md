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

You can send messages to Space web page from the iframe page using `postMessage` method in browser API. The
following message types are supported:

- `GetUserTokenRequest`
- `GetThemePropertiesRequest`
- `RedirectWithConfirmationRequest`
- `ShowConfirmDialogRequest`

Please see [the documentation](https://www.jetbrains.com/help/space/application-homepage.html#communication-between-space-and-iframe)
for details.

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