export default {
  async fetch(request, env, ctx) {
    // 1. Manejo de Preflight OPTIONS para CORS
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS",
          "Access-Control-Allow-Headers": request.headers.get("Access-Control-Request-Headers") || "*",
          "Access-Control-Max-Age": "86400",
        },
      });
    }

    // 2. Extraer la URL de destino
    const url = new URL(request.url);
    let targetUrlStr = request.headers.get("x-target-url") || url.searchParams.get("url");

    if (!targetUrlStr) {
      return new Response("Falta el parametro ?url= o la cabecera x-target-url", {
        status: 400,
        headers: { "Access-Control-Allow-Origin": "*" },
      });
    }

    let targetParsed;
    try {
      targetParsed = new URL(targetUrlStr);
      // Combinar los parámetros de búsqueda adicionales de la URL externa
      for (const [k, v] of url.searchParams.entries()) {
        if (k !== "url") {
          targetParsed.searchParams.set(k, v);
        }
      }
      targetUrlStr = targetParsed.toString();
    } catch (e) {
      return new Response("URL destino invalida: " + targetUrlStr, {
        status: 400,
        headers: { "Access-Control-Allow-Origin": "*" },
      });
    }

    // 3. Preparar cabeceras para el destino
    const proxyHeaders = new Headers(request.headers);
    proxyHeaders.delete("host");
    proxyHeaders.delete("origin");
    proxyHeaders.delete("referer");
    proxyHeaders.delete("x-target-url");
    proxyHeaders.set("host", targetParsed.host);

    // 4. Reenviar petición con seguimiento de redirecciones (avatares Canvas/S3)
    try {
      const fetchOptions = {
        method: request.method,
        headers: proxyHeaders,
        redirect: "follow",
      };

      if (request.method !== "GET" && request.method !== "HEAD") {
        fetchOptions.body = request.body;
      }

      const response = await fetch(targetUrlStr, fetchOptions);

      // 5. Construir respuesta con CORS abierto
      const responseHeaders = new Headers(response.headers);
      responseHeaders.set("Access-Control-Allow-Origin", "*");
      responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS");
      responseHeaders.set("Access-Control-Allow-Headers", "*");

      // Eliminar restricciones restrictivas de destino
      responseHeaders.delete("content-security-policy");

      return new Response(response.body, {
        status: response.status,
        statusText: response.statusText,
        headers: responseHeaders,
      });
    } catch (err) {
      return new Response("Proxy error: " + err.message, {
        status: 502,
        headers: { "Access-Control-Allow-Origin": "*" },
      });
    }
  },
};
