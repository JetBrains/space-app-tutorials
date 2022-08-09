import WarningBox from "./WarningBox";
import {AppTabContents} from "./AppTabContents";
import {onBehalfOfTheUserApiImpl} from "../service/onBehalfOfTheUserApiImpl";
import {requestUserToken, userPermissionScope, UserTokenData} from "../service/spaceAuth";
import Loader from "./Loader";
import {useState} from "@hookstate/core";

interface OnBehalfOfUserTabContentsState {
    userTokenData?: UserTokenData;
}

export function OnBehalfOfUserTabContents() {
    const state = useState(() => loadInitialTabState());

    if (state.promised) {
        return (<Loader/>);
    }

    return (
        <>
            {
                state.userTokenData.get() === undefined &&
                <WarningBox text="Authorize the app to view channels and send messages on behalf of the current user"
                            isActionable={true}
                            onAction={() => requestUserToken(
                                true,
                                userPermissionScope,
                                (newUserTokenData) => state.userTokenData.set(newUserTokenData)
                            )}
                />
            }
            <AppTabContents
                key="OnBehalfOfTheUser"
                userTokenData={state.userTokenData.get()}
                apiImpl={onBehalfOfTheUserApiImpl}
                implementationNote="HTTP requests are made from the iframe to Space API with a user access token. See [onBehalfOfTheUserApiImpl.ts]."
            />
        </>
    )
}

function loadInitialTabState(): Promise<OnBehalfOfUserTabContentsState> {
    return new Promise((resolve) => {
        requestUserToken(
            false,
            userPermissionScope,
            (newUserTokenData) => {
                resolve({
                    userTokenData: newUserTokenData,
                })
            });
    });
}
