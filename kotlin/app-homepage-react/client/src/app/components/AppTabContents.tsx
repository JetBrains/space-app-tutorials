import "./AppTabContents.css";
import {SpaceChannelSelection} from "./SpaceChannelSelection";
import {useState} from "@hookstate/core";
import WarningBox from "./WarningBox";
import fetchSpaceUserToken, {UserTokenData} from "../service/spaceAuth";
import {SendMessageSection} from "./SendMessageSection";
import Loader from "./Loader";
import {ChatChannel, copyChannel} from "../service/chatChannel";
import {TabApiImplementation, TabProps} from "./AppTabs";

export interface AppTabContentsProps {
    userTokenData?: UserTokenData;
    apiImpl: TabApiImplementation;
    implementationNote: string;
}

interface AppTabContentsState {
    selectedChannel?: ChatChannel;
}

export function AppTabContents(props: AppTabContentsProps) {
    const state = useState({
        selectedChannel: undefined
    } as AppTabContentsState);

    return (
        <>
            <SpaceChannelSelection
                onChannelSelected={(channel) => {
                    state.selectedChannel.set(copyChannel(channel));
                }}
                userTokenData={props.userTokenData}
                selectedChannel={state.selectedChannel.get()}
                apiImpl={props.apiImpl}
            />
            <SendMessageSection
                selectedChannel={state.selectedChannel.get()}
                userTokenData={props.userTokenData}
                apiImpl={props.apiImpl}
            />
            <span className="implementation-note">{props.implementationNote}</span>
        </>
    )
}
