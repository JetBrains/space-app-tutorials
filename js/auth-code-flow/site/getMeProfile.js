import {SPACE_ORG_URL} from "./constants.js";

export async function getMeProfileUsername(accessToken) {
    const response = await window.fetch(`${SPACE_ORG_URL}/api/http/team-directory/profiles/me`, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Authorization': `Bearer ${accessToken}`,
        }
    });
    const json = await response.json();
    return json.username;
}