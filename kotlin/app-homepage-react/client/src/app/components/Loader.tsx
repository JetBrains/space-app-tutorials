import Icon from "./Icon";

export interface LoaderProps {
    style?: object;
}

export default function Loader(props: LoaderProps) {
    const style = {...{alignSelf: 'center', width: '50px', height: '50px'}, ...props.style};
    return (
        <Icon name="loader" style={style}/>
    );
}
