window.onload = async e => {
    // subscribe to the changes in theme css variables
    window.addEventListener("message", (e) => {
        if (e.data.type === "ThemeProperties") {
            applyCssVars(e.data);
        }
    });

    const themeCssVars = await getCssVarsAndSubscribeForChanges();
    applyCssVars(themeCssVars);

    const userTokenResponse = await getUserAccessToken(false);
    if (userTokenResponse !== null) {
        populateTeammateList(userTokenResponse);
    } else {
        document.getElementById("authorize-button").style.display = "block";
    }

    document.getElementById("authorize-button").addEventListener('click', onAuthorizeButtonPressed, false);
};

function applyCssVars(cssVars) {
    cssVars.properties.forEach((cssVar, i) =>
        document.documentElement.style.setProperty(cssVar.name, cssVar.value)
    );
}

function getCssVarsAndSubscribeForChanges() {
    return new Promise((resolve) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => resolve(e.data);
        window.parent.postMessage({type:"GetThemePropertiesRequest", subscribeForUpdates: true}, "*", [channel.port2]);
    });
}

function getUserAccessToken(askForConsent) {
    return new Promise((resolve) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => resolve(e.data);
        window.parent.postMessage({type:"GetUserTokenRequest", permissionScope:"global:Profile.View global:Profile.Memberships.View", askForConsent: askForConsent}, "*", [channel.port2]);
    });
}

async function onAuthorizeButtonPressed(event) {
    const userTokenResponse = await getUserAccessToken(true)
    populateTeammateList(userTokenResponse)
}

function populateTeammateList(userTokenResponse) {
    window.fetch(userTokenResponse.serverUrl + '/api/http/team-directory/memberships?profiles=me', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + userTokenResponse.token,
            'Accept': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
                const teamId = data.data[0].team.id;
                window.fetch(document.referrer + '/api/http/team-directory/memberships?teams=' + teamId + '&\$fields=data(member(username,name))', {
                    method: 'GET',
                    headers: {
                        'Authorization': 'Bearer ' + userTokenResponse.token,
                        'Accept': 'application/json'
                    }
                })
                    .then(response => response.json())
                    .then(data => {
                            document.getElementById("authorize-button").style.display = "none";
                            const memberships = data.data;
                            const teammateListDiv = document.getElementById("teammate-list");
                            const ol = document.createElement("p");
                            teammateListDiv.appendChild(ol);

                            memberships.forEach((membership, i) => {
                                const name = membership.member.name;
                                const teammateText = document.createTextNode(name.firstName + ' ' + name.lastName);
                                const li = document.createElement("li");
                                li.appendChild(teammateText);
                                teammateListDiv.appendChild(li);
                            });
                        }
                    );
            }
        );
}
