import './ChannelIcon.css';
import Icon from "./Icon";

export type ChannelIconProps = {
    iconUrl?: string
}

export const ChannelIcon = (props: ChannelIconProps) => {
    return (
        <div className="channelIconContainer">
            {
                props.iconUrl
                    ? <img className="channelIcon" src={props.iconUrl} alt=""/>
                    : <Icon name="space-channel" specialIconForDarkTheme={true} style={{width: 32, height: 32}}/>
            }
        </div>
    )
}
