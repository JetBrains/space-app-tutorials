export let isDark: boolean = false;
export let cssVars = new Map<String, String>();

export function getVar(name: string): string {
    return cssVars.get(name) as string
}

interface ThemeProperties {
    properties: ThemeProperty[];
    isDark: boolean;
}

interface ThemeProperty {
    name: string;
    value: string;
}

export async function initCssVars() {
    // subscribe to the changes in theme css variables
    window.addEventListener("message", (e) => {
        if (e.data.properties !== undefined) {
            applyCssVars(e.data);
        }
    });

    const themeCssVars = await getCssVarsAndSubscribeForChanges() as ThemeProperties;
    applyCssVars(themeCssVars);
}

function applyCssVars(themeProperties: ThemeProperties) {
    isDark = themeProperties.isDark;
    let newCssVars = new Map<String, String>();
    themeProperties.properties.forEach((cssVar) => {
        document.documentElement.style.setProperty(cssVar.name, cssVar.value);
        newCssVars.set(cssVar.name, cssVar.value);
    })
    cssVars = newCssVars;
}

function getCssVarsAndSubscribeForChanges() {
    return new Promise((resolve) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => resolve(e.data);
        window.parent.postMessage({type: "GetThemePropertiesRequest", subscribeForUpdates: true}, "*", [channel.port2]);
    });
}
