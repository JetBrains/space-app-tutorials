import "./AppTab.css";

export interface AppTabProps {
    name: string;
    isActive: boolean;
    onClick: () => void;
}

export function AppTab(props: AppTabProps) {
    return (
        <div onClick={() => props.onClick()}
             className={props.isActive ? `tab-container tab-container-active` : `tab-container tab-container-inactive`}>
            <span className="tab-name">{props.name}</span>
        </div>
    )
}
