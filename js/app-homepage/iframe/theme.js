export async function getThemePropertiesAndSubscribeForChanges() {
    // subscribe to the changes in theme css variables
    window.addEventListener("message", (e) => {
        if (e.data.properties !== undefined) {
            applyCssVars(e.data.properties);
        }
    });

    const themeCssVars = await getThemePropertiesFromSpace();
    applyCssVars(themeCssVars.properties);
}

function applyCssVars(cssVars) {
    cssVars.forEach((cssVar, i) =>
        document.documentElement.style.setProperty(cssVar.name, cssVar.value)
    );
}

function getThemePropertiesFromSpace() {
    return new Promise((resolve) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = e => resolve(e.data);
        window.parent.postMessage({type: "GetThemePropertiesRequest", subscribeForUpdates: true}, "*", [channel.port2]);
    });
}
