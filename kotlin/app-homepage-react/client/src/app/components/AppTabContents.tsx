import "./AppTabContents.css";
import {SpaceChannelSelection} from "./SpaceChannelSelection";
import {useState} from "@hookstate/core";
import {UserTokenData} from "../service/spaceAuth";
import {SendMessageSection} from "./SendMessageSection";
import {ChatChannel, copyChannel} from "../service/chatChannel";
import {TabApiImplementation} from "./AppTabs";
import {openInNewTab} from "../service/utils";

export interface AppTabContentsProps {
    userTokenData?: UserTokenData;
    apiImpl: TabApiImplementation;
    implementationNote: string;
    sourceCodeHRef: string;
    sourceCodeLinkText: string;
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
            <span className="implementation-note">
                {props.implementationNote}
                {" See "}
                <span className="source-code-link" onClick={() => openInNewTab(props.sourceCodeHRef)}>
                    {props.sourceCodeLinkText}
                </span>
            </span>
        </>
    )
}
