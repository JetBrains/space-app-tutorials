import {UserTokenData} from "./spaceAuth";
import {httpGet, httpPost} from "./utils";
import {ChatChannel} from "./chatChannel";

const getChannelListImpl = async (query: string, userTokenData?: UserTokenData) => {
    if (userTokenData === undefined) {
        return [];
    }

    const responseRaw = await httpGet(`/homepage/get-channels?query=${query}`, userTokenData.userToken)
    const response = (await responseRaw.json()) as GetChannelsResponse
    return response.channels.map((spaceChannel) => {
        return {
            id: spaceChannel.id,
            label: spaceChannel.name,
            iconUrl: spaceChannel.iconUrl
        } as ChatChannel;
    });
}

const sendMessageImpl = (channelId?: string, messageText?: string, userTokenData?: UserTokenData) => {
    if (channelId === undefined || messageText === undefined || userTokenData === undefined) {
        return
    }

    const call = async () => {
        await httpPost(`/homepage/send-message?channelId=${channelId}&messageText=${messageText}`, userTokenData.userToken, {})
    };
    call().catch(console.error);
}

interface GetChannelsResponse {
    channels: SpaceChannel[]
}

interface SpaceChannel {
    id: string;
    name: string;
    iconUrl?: string;
}

export const onBehalfOfTheAppApiImpl = {
    getChannelList: getChannelListImpl,
    sendMessage: sendMessageImpl,
}
