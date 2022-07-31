import './ChannelSelectField.css';
import * as theme from "../service/theme";
import AsyncSelect from "react-select/async";
import {components, Theme, ValueContainerProps} from "react-select";
import * as utils from "../service/utils";
import {OptionProps} from "react-select/dist/declarations/src/components/Option";
import {StylesConfig} from "react-select/dist/declarations/src/styles";
import {ChatChannel} from "../service/chatChannel";
import {ChannelIcon} from "./ChannelIcon";

export type ChannelSelectFieldProps = {
    defaultChannels: ChatChannel[];
    loadOptions: (query: string, callback: (channels: ChatChannel[]) => void) => void;
    onChange: (channel: ChatChannel) => void;
    value?: ChatChannel;
    isDisabled: boolean;
    style?: object;
}

export const ChannelSelectField = (props: ChannelSelectFieldProps) => {
    return (
        <div className="selectField" style={props.style}>
            <AsyncSelect
                isDisabled={props.isDisabled}
                defaultOptions={props.defaultChannels}
                styles={customStyles}
                placeholder="Type to filter channels"
                loadOptions={utils.debounce((query: string, callback: (channels: ChatChannel[]) => void) => props.loadOptions(query, callback))}
                theme={(selectTheme: Theme) => ({
                    ...selectTheme,
                    borderRadius: 0,
                    colors: {
                        ...selectTheme.colors,
                        primary: theme.getVar('--text-color-20'),
                        primary50: theme.getVar('--list-item-hover-background-color'),
                    },
                } as Theme)}
                components={{Option, ValueContainer}}
                onChange={(newValue) => props.onChange(newValue as ChatChannel)}
                value={props.value}
            />
        </div>
    );
}

type IsMulti = false;

const Option = (props: OptionProps<ChatChannel>) => {
    let classNames = props.isSelected || props.isFocused ? "selectOption selectOptionFocused" : "selectOption selectOptionNotFocused";
    return (
        <div className={classNames}>
            <ChannelIcon iconUrl={(props.data as ChatChannel).iconUrl}/>
            <components.Option {...props}/>
        </div>
    );
};

const ValueContainer = ({
                            children,
                            ...props
                        }: ValueContainerProps<ChatChannel, IsMulti>) => {
    const value = props.selectProps.value as ChatChannel;

    return (
        <div className="selectOption">
            {
                value != null &&
                <ChannelIcon
                    iconUrl={value.iconUrl}
                />
            }
            <components.ValueContainer {...props}>{children}</components.ValueContainer>
        </div>
    )
};

const customStyles: StylesConfig<ChatChannel, IsMulti> = {
    option: (provided, state) => {
        return {
            ...provided,
            color: theme.getVar('--text-color'),
            backgroundColor: state.isFocused ? theme.getVar('--list-item-hover-background-color') : theme.getVar('--background-color'),
        }
    },
    input: (provided) => ({
        ...provided,
        color: theme.getVar('--input-text-color'),
        fontWeight: theme.getVar('--input-font-weight'),
    }),
    dropdownIndicator: (provided) => {
        return {
            ...provided,
            color: theme.getVar('--text-color-20'),
        }
    },
    indicatorSeparator: (provided) => ({
        ...provided,
        backgroundColor: theme.getVar('--text-color-20'),
    }),
    placeholder: (provided) => ({
        ...provided,
        color: theme.getVar('--input-placeholder-color'),
        fontWeight: theme.getVar('--input-font-weight'),
    }),

    control: (provided) => {
        return {
            ...provided,
            borderStyle: 'solid',
            borderWidth: '1px',
            borderColor: theme.getVar('--text-color-20'),
            backgroundColor: theme.getVar('--input-background-color'),
            height: `42px`,
        };
    },
    menu: (provided) => ({
        ...provided,
        backgroundColor: theme.getVar('--background-color'),
    }),
    singleValue: (provided) => ({
        ...provided,
        color: theme.getVar('--input-text-color'),
        fontWeight: theme.getVar('--input-font-weight'),
    }),
}
