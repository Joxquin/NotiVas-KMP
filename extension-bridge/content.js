// Content script de enlace para NotiVas Web (Firefox / Zen & Chromium)

const browserAPI = typeof browser !== "undefined" ? browser : chrome;
const appOrigin = "https://joxquin.github.io";

function isTrustedPage() {
  return window.location.origin === appOrigin &&
    window.location.pathname.startsWith("/NotiVas-KMP/") &&
    window.top === window;
}

function announceReady() {
  if (!isTrustedPage()) return;
  window.postMessage({
    type: "NOTIVAS_BRIDGE_AVAILABLE",
    version: "1.0.0",
    timestamp: Date.now()
  }, appOrigin);
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
  if (!isTrustedPage() || event.source !== window || event.origin !== appOrigin) return;

  if (event.data && event.data.type === "NOTIVAS_PING") {
    announceReady();
    return;
  }

  if (event.data && event.data.type === "NOTIVAS_REQUEST_SESSION") {
    const domain = event.data.domain || "tecsup.instructure.com";

    const respond = (response) => {
      if (isTrustedPage()) {
        window.postMessage({
          type: "NOTIVAS_RESPONSE_SESSION",
          requestId: event.data.requestId,
          payload: response
        }, appOrigin);
      }
    };
    const message = { action: "GET_CANVAS_COOKIES", domain: domain };
    if (typeof browser !== "undefined") {
      browser.runtime.sendMessage(message).then(respond).catch(() => {
        respond({ success: false, error: "No se pudo conectar con la extension." });
      });
    } else {
      browserAPI.runtime.sendMessage(message, (response) => {
        if (browserAPI.runtime.lastError) {
          respond({ success: false, error: "No se pudo conectar con la extension." });
        } else {
          respond(response);
        }
      });
    }
  }
});
