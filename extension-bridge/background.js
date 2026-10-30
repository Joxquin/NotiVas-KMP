// Service worker / Background script para NotiVas Canvas Bridge (Firefox / Zen & Chromium)

const browserAPI = typeof browser !== "undefined" ? browser : chrome;

browserAPI.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "GET_CANVAS_COOKIES") {
    handleFetchCookies(request.domain)
      .then(result => sendResponse(result))
      .catch(err => sendResponse({ success: false, error: err.toString() }));
    return true; // Obligatorio para respuestas asincronas
  }
});

async function handleFetchCookies(rawDomain) {
  const domain = (rawDomain || "instructure.com").replace(/^https?:\/\//, "").split("/")[0];
  const url = `https://${domain}`;

  // 1. Intentar primero con la URL completa (captura cookies de subdominio, dominio padre y host-only)
  let cookies = await queryCookies({ url: url });

  // 2. Si no encontro, intentar por dominio exacto
  if (!cookies || cookies.length === 0) {
    cookies = await queryCookies({ domain: domain });
  }

  // 3. Si aun no encontro, intentar con el dominio base instructure.com
  if (!cookies || cookies.length === 0) {
    cookies = await queryCookies({ domain: "instructure.com" });
  }

  if (!cookies || cookies.length === 0) {
    return {
      success: false,
      error: `No se encontraron cookies para ${domain}. Asegurate de tener la sesion iniciada en Canvas en este navegador.`
    };
  }

  const cookieString = cookies
    .map(c => `${c.name}=${c.value}`)
    .join("; ");

  const csrfCookie = cookies.find(c => c.name === "_csrf_token");
  const csrfToken = csrfCookie ? decodeURIComponent(csrfCookie.value) : null;
  const hasSession = cookies.some(c => c.name === "canvas_session" || c.name === "log_session_id");

  return {
    success: true,
    domain: domain,
    hasSession: hasSession,
    cookieHeader: cookieString,
    csrfToken: csrfToken,
    cookieCount: cookies.length,
    cookiesFound: cookies.map(c => c.name)
  };
}

function queryCookies(details) {
  return new Promise((resolve) => {
    try {
      browserAPI.cookies.getAll(details, (cookies) => {
        if (browserAPI.runtime.lastError) {
          resolve([]);
        } else {
          resolve(cookies || []);
        }
      });
    } catch (e) {
      resolve([]);
    }
  });
}
