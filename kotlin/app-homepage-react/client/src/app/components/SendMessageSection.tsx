import "./SendMessageSection.css";
import Button from "./Button";
import {useState} from "@hookstate/core";
import * as React from "react";
import {ChatChannel} from "../service/chatChannel";
import {httpPost} from "../service/utils";
import {UserTokenData} from "../service/spaceAuth";
import {TabApiImplementation} from "./AppTabs";

export interface SendMessageSectionProps {
    selectedChannel?: ChatChannel;
    userTokenData?: UserTokenData;
    apiImpl: TabApiImplementation;
}

type SendMessageSectionState = {
    messageInputText: string,
}

export function SendMessageSection(props: SendMessageSectionProps) {
    const state = useState({messageInputText: ""} as SendMessageSectionState);

    return (
        <div className="send-message-section">
            <div className="text-field">
                <span className="text-field-label">Compose a message</span>
                <input
                    type="text"
                    className="text-input"
                    value={state.messageInputText.get()}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => state.messageInputText.set(event.target.value)}
                    disabled={props.userTokenData === undefined}
                />
            </div>

            <Button
                buttonText="Send message"
                isDisabled={props.userTokenData === undefined || props.selectedChannel === undefined || state.messageInputText.get() === ""}
                actionHandler={() => props.apiImpl.sendMessage(props.selectedChannel?.id, state.messageInputText.get(), props.userTokenData)}
            />
        </div>
    );
}
