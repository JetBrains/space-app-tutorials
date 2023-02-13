import {APPLICATION_REDIRECT_URL, CLIENT_ID, CLIENT_SECRET, SPACE_ORG_URL} from "./constants.js";
import {parseJwt} from "./jwt.js";

export async function getAuthorizationUrl() {
    const redirectUri = encodeURIComponent(APPLICATION_REDIRECT_URL);
    const scope = encodeURIComponent('global:Profile.View global:Profile.Memberships.View');
    const authCodeRequestKey = window.crypto.randomUUID();
    const codeVerifier = `${window.crypto.randomUUID()}.${window.crypto.randomUUID()}`;
    localStorage.setItem(authCodeRequestKey, codeVerifier);

    const hashed = await sha256(codeVerifier);
    const codeChallenge = base64urlencode(hashed);

    return `${SPACE_ORG_URL}/oauth/auth?response_type=code&redirect_uri=${redirectUri}&request_credentials=default&client_id=${CLIENT_ID}&scope=${scope}&state=${authCodeRequestKey}&access_type=online&code_challenge=${codeChallenge}&code_challenge_method=S256`;
}

export async function getAccessTokenFromSpace(authCode, state) {
    const codeVerifier = localStorage.getItem(state);
    if (codeVerifier === null) {
        throw `State ${state} not found in local storage`;
    }

    const auth = window.btoa(`${CLIENT_ID}:${CLIENT_SECRET}`);

    const response = await window.fetch(`${SPACE_ORG_URL}/oauth/token`, {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Authorization': `Basic ${auth}`,
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `grant_type=authorization_code&code=${authCode}&redirect_uri=${APPLICATION_REDIRECT_URL}&code_verifier=${codeVerifier}`
    });
    const json = await response.json();
    return json.access_token;
}

export function getNonExpiredAccessToken() {
    let accessToken = localStorage.getItem("accessToken");
    if (accessToken === null || accessToken === undefined) {
        return null;
    }

    let accessTokenJson = parseJwt(accessToken);
    let expiresAt = accessTokenJson.exp;
    let currentSeconds = new Date().getTime() / 1000;
    if (expiresAt - currentSeconds < 10) {
        return null;
    }

    return accessToken;
}

async function sha256(message) {
    // encode as UTF-8
    const msgBuffer = new TextEncoder().encode(message);

    // hash the message
    return await window.crypto.subtle.digest('SHA-256', msgBuffer);
}

function base64urlencode(a) {
    let str = "";
    const bytes = new Uint8Array(a);
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
        str += String.fromCharCode(bytes[i]);
    }
    return btoa(str)
        .replace(/\+/g, "-")
        .replace(/\//g, "_")
        .replace(/=+$/, "");
}
