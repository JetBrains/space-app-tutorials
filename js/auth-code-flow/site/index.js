import './constants.js';
import {getAccessTokenFromSpace, getAuthorizationUrl, getNonExpiredAccessToken} from "./auth.js";
import {getMeProfileUsername} from "./getMeProfile.js";

window.onload = async () => {
    let accessToken = getNonExpiredAccessToken();

    if (accessToken == null) {
        const searchParams = new URLSearchParams(window.location.search);
        const authCode = searchParams.get('code');
        const state = searchParams.get('state');

        if (authCode === null || state === null) {
            // no authorization code in url parameters, show "Authorize in Space" button
            const rootDiv = document.getElementById("root");
            const button = document.createElement("button");
            button.textContent = "Authorize in Space";
            button.addEventListener("click", onAuthorizeInSpaceButtonClicked);
            rootDiv.appendChild(button);
            return;
        } else {
            accessToken = await getAccessTokenFromSpace(authCode, state);
            localStorage.setItem("accessToken", accessToken);
            document.location.replace('/');
        }
    }

    const rootDiv = document.getElementById("root");
    const username = await getMeProfileUsername(accessToken);
    const usernameP = document.createElement("p");
    usernameP.textContent = `Username retrieved from Space through Space HTTP API: ${username}`;
    rootDiv.appendChild(usernameP);
}

const onAuthorizeInSpaceButtonClicked = async () => {
    window.location.href = await getAuthorizationUrl();
}
