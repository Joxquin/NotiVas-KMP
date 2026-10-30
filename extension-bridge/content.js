// Content script de enlace para NotiVas Web (Firefox / Zen & Chromium)

const browserAPI = typeof browser !== "undefined" ? browser : chrome;

function announceReady() {
  window.postMessage({
    type: "NOTIVAS_BRIDGE_AVAILABLE",
    version: "1.0.0",
    timestamp: Date.now()
  }, "*");
}

announceReady();
let announceTries = 0;
const announceInterval = setInterval(() => {
  announceReady();
  announceTries++;
  if (announceTries > 10) clearInterval(announceInterval);
}, 500);

// Escuchar peticiones desde la pagina web
window.addEventListener("message", (event) => {
  if (event.source !== window) return;

  if (event.data && event.data.type === "NOTIVAS_PING") {
    announceReady();
    return;
  }

  if (event.data && event.data.type === "NOTIVAS_REQUEST_SESSION") {
    const domain = event.data.domain || "tecsup.instructure.com";

    browserAPI.runtime.sendMessage(
      { action: "GET_CANVAS_COOKIES", domain: domain },
      (response) => {
        window.postMessage({
          type: "NOTIVAS_RESPONSE_SESSION",
          requestId: event.data.requestId,
          payload: response
        }, "*");
      }
    );
  }
});
