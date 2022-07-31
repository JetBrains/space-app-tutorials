type IconProps = {
    name: string;
    style: object;
    specialIconForDarkTheme?: boolean;
}

export default function Icon(props: IconProps) {
    let iconName = props.name;
    if (props.specialIconForDarkTheme) {
        iconName = props.name + `-dark`;
    }
    return (
        <img src={`./images/${iconName}.svg`} alt="" style={props.style}/>
    )
}
