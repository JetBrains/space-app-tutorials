import {UserTokenData} from "./spaceAuth";
import {ChatChannel} from "./chatChannel";
import {httpGet, httpPost} from "./utils";

const getChannelListOnBehalfOfTheUser = async (query: string, userTokenData?: UserTokenData) => {
    if (userTokenData === undefined) {
        return [];
    }

    const response = await httpGet(
        `${userTokenData.spaceServerUrl}/api/http/chats/channels/all-channels?query=${query}&publicOnly=true&withArchived=false`,
        userTokenData.userToken,
    )

    const channels = (await response.json()).data as SpaceChatChannel[];
    return channels.map((spaceChannel) => {
        return {
            id: spaceChannel.channelId,
            label: spaceChannel.name,
            iconUrl: spaceChannel.icon,
        } as ChatChannel;
    });
}

type SpaceChatChannel = {
    channelId: string;
    name: string;
    icon: string;
}

const sendMessageOnBehalfOfTheUser = (channelId?: string, messageText?: string, userTokenData?: UserTokenData) => {
    if (userTokenData === undefined) {
        return;
    }
    if (channelId === undefined || messageText === undefined) {
        return;
    }

    const call = async () => {
        await httpPost(
            `${userTokenData.spaceServerUrl}/api/http/chats/messages/send-message`,
            userTokenData.userToken,
            {
                content: {
                    className: "ChatMessage.Text",
                    text: messageText
                },
                channel: `id:${channelId}`
            }
        );
    };
    call().catch(console.error);
}

export const onBehalfOfTheUserApiImpl = {
    getChannelList: getChannelListOnBehalfOfTheUser,
    sendMessage: sendMessageOnBehalfOfTheUser,
}
