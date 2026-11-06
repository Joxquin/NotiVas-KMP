// Service worker / Background script para NotiVas Canvas Bridge (Firefox / Zen & Chromium)

const browserAPI = typeof browser !== "undefined" ? browser : chrome;

browserAPI.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request?.action === "GET_CANVAS_COOKIES") {
    if (!isTrustedSender(sender)) {
      sendResponse({ success: false, error: "Origen no autorizado para vincular Canvas." });
      return false;
    }
    handleFetchCookies(request.domain)
      .then(result => sendResponse(result))
      .catch(err => sendResponse({ success: false, error: err.toString() }));
    return true; // Obligatorio para respuestas asincronas
  }
});

function isTrustedSender(sender) {
  if (sender?.id !== browserAPI.runtime.id || sender.frameId !== 0) return false;
  try {
    const source = new URL(sender.url);
    return source.origin === "https://joxquin.github.io" &&
      source.pathname.startsWith("/NotiVas-KMP/");
  } catch {
    return false;
  }
}

function canvasDomain(rawDomain) {
  if (typeof rawDomain !== "string" || !rawDomain.trim()) {
    throw new Error("Falta el dominio de Canvas.");
  }
  const value = rawDomain.trim();
  const target = new URL(value.includes("://") ? value : `https://${value}`);
  const supported = target.hostname.endsWith(".instructure.com") ||
    target.hostname === "canvas.utp.edu.pe";
  if (!supported || target.protocol !== "https:" || target.port ||
      target.username || target.password || target.pathname !== "/" ||
      target.search || target.hash) {
    throw new Error("La extension admite dominios HTTPS de Instructure y canvas.utp.edu.pe. Para otro campus, usa la vinculacion con el movil.");
  }
  return target.hostname;
}

async function handleFetchCookies(rawDomain) {
  const domain = canvasDomain(rawDomain);
  // Consultar solo las cookies que el navegador enviaria a este campus.
  // Buscar por el dominio padre mezclaria sesiones de otras instituciones.
  const cookies = await queryCookies({ url: `https://${domain}/` });

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
  if (typeof browser !== "undefined") {
    return browser.cookies.getAll(details);
  }
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
