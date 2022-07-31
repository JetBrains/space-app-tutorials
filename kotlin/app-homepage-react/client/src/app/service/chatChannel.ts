export interface ChatChannel {
    id: string;
    label: string;
    iconUrl?: string;
}

export function copyChannel(chatChannel: ChatChannel): ChatChannel {
    return {
        id: chatChannel.id,
        label: chatChannel.label,
        iconUrl: chatChannel.iconUrl
    }
}
