export function getUserAccessTokenData(askForConsent) {
    return new Promise((resolve) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => resolve(e.data);
        window.parent.postMessage({
            type: "GetUserTokenRequest",
            permissionScope: "global:Profile.View global:Profile.Memberships.View",
            askForConsent: askForConsent
        }, "*", [channel.port2]);
    });
}
