import * as theme from "./theme.js";
import * as auth from "./auth.js";
import * as team from "./team.js";

window.onload = async e => {
    await theme.getThemePropertiesAndSubscribeForChanges();

    const userTokenData = await auth.getUserAccessTokenData(false);
    if (userTokenData !== null) {
        team.populateTeammateList(userTokenData);
    } else {
        document.getElementById("authorize-button").style.display = "block";
    }

    document.getElementById("authorize-button").addEventListener('click', onAuthorizeButtonPressed, false);
};


async function onAuthorizeButtonPressed(event) {
    const userTokenData = await auth.getUserAccessTokenData(true)
    team.populateTeammateList(userTokenData)
}
