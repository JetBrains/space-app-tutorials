import './index.css';
import {App} from "./app/App";
import React from "react";
import ReactDOM from "react-dom/client";
import * as theme from './app/service/theme';

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);
root.render(
    <React.StrictMode>
        <App/>
    </React.StrictMode>
);

window.onload = async () => {
    await theme.initCssVars();
}
