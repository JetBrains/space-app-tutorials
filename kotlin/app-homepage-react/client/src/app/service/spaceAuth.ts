export interface UserTokenData {
    userToken: string;
    spaceServerUrl: string;
}

export default async function fetchSpaceUserToken(askForConsent: boolean = false, permissionScope: string = ""): Promise<UserTokenData | undefined> {
    // read more about getting user token in Space documentation:
    // https://www.jetbrains.com/help/space/application-homepage.html#getusertokenrequest-get-space-user-token

    const response = await new Promise((resolve) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => resolve(e.data);
        window.parent.postMessage({
            type: "GetUserTokenRequest",
            permissionScope: permissionScope,
            askForConsent: askForConsent
        }, "*", [channel.port2]);
    }) as GetUserTokenResponse;

    if (response === null) {
        return undefined;
    }

    return {
        userToken: response.token,
        spaceServerUrl: response.serverUrl
    } as UserTokenData;
}

export function requestUserToken(askForConsent: boolean, permissionScope: string, callback: (userTokenData?: UserTokenData) => void) {
    const call = async () => {
        const userTokenData = await fetchSpaceUserToken(
            askForConsent,
            permissionScope,
        );
        callback(userTokenData);
    }
    call().catch(console.error);
}

export function requestAppPermissions(permissionScope: string, callback: (success: boolean) => void) {
    const call = async () => {
        const success = await doRequestAppPermissions(permissionScope);
        callback(success);
    }
    call().catch(console.error);
}

interface GetUserTokenResponse {
    token: string;
    serverUrl: string;
}

async function doRequestAppPermissions(permissionScope: string): Promise<boolean> {
    return await new Promise((resolve) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => resolve(e.data);
        window.parent.postMessage({
            type: "ApprovePermissionsRequest",
            permissionScope: permissionScope,
        }, "*", [channel.port2]);
    }) as boolean;
}

/**
 * Permissions scopes are the same in this demo application
 */
export const userPermissionScope = "global:Channel.ViewChannel global:Channel.PostMessages"
export const appPermissionScope = "global:Channel.ViewChannel global:Channel.PostMessages"
