import "./AppTabs.css";
import {useState} from "@hookstate/core";
import {AppTab} from "./AppTab";
import {UserTokenData} from "../service/spaceAuth";
import {ChatChannel} from "../service/chatChannel";
import {OnBehalfOfUserTabContents} from "./OnBehalfOfUserTabContents";
import {OnBehalfOfAppTabContents} from "./OnBehalfOfAppTabContents";

enum ActiveTab {
    OnBehalfOfTheUser = 0,
    OnBehalfOfTheApp
}

interface AppTabsState {
    activeTab: ActiveTab
}

export interface TabApiImplementation {
    getChannelList: (query: string, userTokenData?: UserTokenData) => Promise<ChatChannel[]>;
    sendMessage: (channelId?: string, messageText?: string, userTokenData?: UserTokenData) => void;
}

export interface TabProps {
    apiImpl: TabApiImplementation;
}

export function AppTabs() {
    const state = useState(initialState);

    return (
        <>
            <div className="tab-group">
                <AppTab
                    name="On behalf of the user"
                    isActive={state.activeTab.get() === ActiveTab.OnBehalfOfTheUser}
                    onClick={() => state.activeTab.set(ActiveTab.OnBehalfOfTheUser)}
                />

                <AppTab
                    name="On behalf of the app"
                    isActive={state.activeTab.get() === ActiveTab.OnBehalfOfTheApp}
                    onClick={() => state.activeTab.set(ActiveTab.OnBehalfOfTheApp)}
                />
            </div>

            {
                state.activeTab.get() === ActiveTab.OnBehalfOfTheUser
                    ? <OnBehalfOfUserTabContents/>
                    : <OnBehalfOfAppTabContents/>
            }
        </>
    );
}

function initialState(): AppTabsState {
    return {
        activeTab: ActiveTab.OnBehalfOfTheUser
    }
}
