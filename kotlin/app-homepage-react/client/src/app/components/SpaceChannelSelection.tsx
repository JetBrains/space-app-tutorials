import "./SpaceChannelSelection.css"
import {useState} from "@hookstate/core";
import {ChannelSelectField} from "./ChannelSelectField";
import {UserTokenData} from "../service/spaceAuth";
import {useEffect} from "react";
import Loader from "./Loader";
import {ChatChannel} from "../service/chatChannel";
import {TabApiImplementation} from "./AppTabs";

export type SpaceChannelSelectionProps = {
    onChannelSelected: (channel: ChatChannel) => void;
    userTokenData?: UserTokenData;
    selectedChannel?: ChatChannel;
    apiImpl: TabApiImplementation;
}

export const SpaceChannelSelection = (props: SpaceChannelSelectionProps) => {
    const state = useState(() => {
        return {
            isLoading: props.userTokenData !== undefined,
            defaultChannels: [],
            filteredChannels: [],
            query: "",
            selectedChannel: props.selectedChannel,
        } as ChannelSelectionState;
    })

    useEffect(() => {
        let active = true;
        if (props.userTokenData === undefined) {
            return;
        }
        load(props.userTokenData);
        return () => {
            active = false
        }

        async function load(userTokenData: UserTokenData) {
            state.isLoading.set(true);
            const channels = await props.apiImpl.getChannelList("", userTokenData);
            if (!active) return;
            state.defaultChannels.set(channels);
            state.isLoading.set(false);
        }
    }, [props.userTokenData?.userToken]);

    return <>
        <div className="channel-select-container">
            <div className="channel-select-field">
                <span className="channel-select-field-header">Pick a channel</span>
                <div className="select-field-with-loader">
                    <ChannelSelectField
                        defaultChannels={state.defaultChannels.get()}
                        loadOptions={(query, callback) => getSpaceChannels(props.apiImpl, query, callback, props.userTokenData)}
                        onChange={(selectedOption) => {
                            props.onChannelSelected(selectedOption);
                        }}
                        value={state.selectedChannel.get()}
                        isDisabled={props.userTokenData === undefined}
                        style={{width: '400px'}}
                    />
                    {
                        state.isLoading.get() &&
                        <Loader style={{marginRight: '16 px'}}/>
                    }
                </div>
            </div>
        </div>
    </>
}

type ChannelSelectionState = {
    isLoading: boolean,
    defaultChannels: ChatChannel[],
    filteredChannels: ChatChannel[],
    query: string,
    selectedChannel?: ChatChannel,
}

function getSpaceChannels(apiImpl: TabApiImplementation, query: string, callback: (channels: ChatChannel[]) => void, userTokenData?: UserTokenData) {
    const call = async () => {
        let channels = await apiImpl.getChannelList(query, userTokenData)
        callback(channels);
    };
    call().catch(console.error);
}
