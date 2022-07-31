import './App.css';
import {AppTabs} from "./components/AppTabs";

export const App = () => {
    return <>
        <div className="app">
            <span className="app-header">
                Demo application
            </span>
            <span className="app-description">
                Interaction between the app iframe, app server and Space
            </span>

            <AppTabs/>
        </div>
    </>
}
