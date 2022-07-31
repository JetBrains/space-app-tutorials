import "./WarningBox.css";
import Icon from "./Icon";

type WarningBoxProps = {
    text: string;
    isActionable: boolean;
    onAction: () => void;
    style?: object;
}

export default function WarningBox(props: WarningBoxProps) {
    let spanClassNames = props.isActionable ? "warning-span warning-span-actionable" : "warning-span";
    return (
        <div className="warning-box" style={props.style}>
            <div className="warning-content-container">
                <Icon name="warning" style={{marginRight: '10px'}}/>
                <span className={spanClassNames} onClick={() => warningTextClicked(props)}>{props.text}</span>
            </div>
        </div>
    );
}

function warningTextClicked(props: WarningBoxProps) {
    if (props.isActionable) {
        props.onAction();
    }
}
