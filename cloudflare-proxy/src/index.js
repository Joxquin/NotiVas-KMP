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

    const url = new URL(request.url);

    // ==========================================
    // 2. MÓDULO DE VINCULACIÓN EFÍMERA (QR / PIN)
    // ==========================================
    const jsonCors = (data, status = 200) => {
      return new Response(JSON.stringify(data), {
        status,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS",
          "Access-Control-Allow-Headers": "*",
        },
      });
    };

    if (url.pathname.startsWith("/pair/")) {
      const kv = env.NOTIVAS_PAIRING_KV;
      if (!kv) {
        return jsonCors({ error: "NOTIVAS_PAIRING_KV no esta configurado en Cloudflare" }, 500);
      }

      // Endpoint: POST /pair/create
      // Genera un canal efímero para NotiVas Web
      if (url.pathname === "/pair/create" && request.method === "POST") {
        try {
          const body = await request.json();
          const { publicKey } = body;
          if (!publicKey) {
            return jsonCors({ error: "Falta publicKey" }, 400);
          }

          const sessionId = crypto.randomUUID();
          // Generar PIN de 6 dígitos único (ej: 489215)
          const pin = Math.floor(100000 + Math.random() * 900000).toString();

          const sessionData = {
            sessionId,
            pin,
            publicKey,
            status: "pending",
            createdAt: Date.now(),
          };

          // TTL de 120 segundos (2 minutos)
          await kv.put(`session:${sessionId}`, JSON.stringify(sessionData), { expirationTtl: 120 });
          await kv.put(`pin:${pin}`, sessionId, { expirationTtl: 120 });

          return jsonCors({
            sessionId,
            pin,
            expiresIn: 120,
          });
        } catch (e) {
          return jsonCors({ error: "Error al procesar solicitud: " + e.message }, 400);
        }
      }

      // Endpoint: POST /pair/resolve-pin
      // La app móvil consulta la clave pública asociada a un PIN de 6 dígitos
      if (url.pathname === "/pair/resolve-pin" && request.method === "POST") {
        try {
          const body = await request.json();
          const { pin } = body;
          if (!pin) {
            return jsonCors({ error: "Falta pin" }, 400);
          }

          const sessionId = await kv.get(`pin:${pin}`);
          if (!sessionId) {
            return jsonCors({ error: "PIN invalido o expirado" }, 404);
          }

          const sessionRaw = await kv.get(`session:${sessionId}`);
          if (!sessionRaw) {
            return jsonCors({ error: "Sesion no encontrada o expirada" }, 404);
          }

          const session = JSON.parse(sessionRaw);
          return jsonCors({
            sessionId: session.sessionId,
            publicKey: session.publicKey,
            status: session.status,
          });
        } catch (e) {
          return jsonCors({ error: "Error al resolver PIN: " + e.message }, 400);
        }
      }

      // Endpoint: POST /pair/complete
      // La app móvil envía el payload de sesión cifrado con la clave pública
      if (url.pathname === "/pair/complete" && request.method === "POST") {
        try {
          const body = await request.json();
          const { sessionId, encryptedPayload, iv } = body;
          if (!sessionId || !encryptedPayload || !iv) {
            return jsonCors({ error: "Faltan parametros requeridos (sessionId, encryptedPayload, iv)" }, 400);
          }

          const sessionRaw = await kv.get(`session:${sessionId}`);
          if (!sessionRaw) {
            return jsonCors({ error: "Sesion no encontrada o expirada" }, 404);
          }

          const session = JSON.parse(sessionRaw);
          session.status = "completed";
          session.encryptedPayload = encryptedPayload;
          session.iv = iv;

          // Conservar 60 segundos más para que el polling de la web lo recoja
          await kv.put(`session:${sessionId}`, JSON.stringify(session), { expirationTtl: 60 });
          // El PIN ya no es necesario
          await kv.delete(`pin:${session.pin}`);

          return jsonCors({ success: true, message: "Sesion cifrada entregada exitosamente" });
        } catch (e) {
          return jsonCors({ error: "Error al completar sesion: " + e.message }, 400);
        }
      }

      // Endpoint: GET /pair/poll?sessionId=...
      // NotiVas Web espera hasta que la sesión se complete (con long-polling)
      if (url.pathname === "/pair/poll" && request.method === "GET") {
        const sessionId = url.searchParams.get("sessionId");
        if (!sessionId) {
          return jsonCors({ error: "Falta sessionId" }, 400);
        }

        // Long-polling durante un máximo de 25 segundos
        const start = Date.now();
        while (Date.now() - start < 25000) {
          const sessionRaw = await kv.get(`session:${sessionId}`);
          if (!sessionRaw) {
            return jsonCors({ status: "expired" }, 404);
          }

          const session = JSON.parse(sessionRaw);
          if (session.status === "completed") {
            // Eliminar de KV inmediatamente tras entrega
            await kv.delete(`session:${sessionId}`);
            return jsonCors({
              status: "completed",
              encryptedPayload: session.encryptedPayload,
              iv: session.iv,
            });
          }

          // Esperar 1.5 segundos antes de reintentar lectura
          await new Promise((resolve) => setTimeout(resolve, 1500));
        }

        return jsonCors({ status: "pending" });
      }

      return jsonCors({ error: "Ruta de vinculacion no encontrada" }, 404);
    }

    // ==========================================
    // 3. PROXY CORS EXISTENTE
    // ==========================================
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

    // Preparar cabeceras para el destino
    const proxyHeaders = new Headers(request.headers);
    proxyHeaders.delete("host");
    proxyHeaders.delete("origin");
    proxyHeaders.delete("referer");
    proxyHeaders.delete("x-target-url");
    proxyHeaders.set("host", targetParsed.host);

    const forwardedCookie = request.headers.get("x-forwarded-cookie") || request.headers.get("x-proxy-cookie");
    if (forwardedCookie) {
      proxyHeaders.set("cookie", forwardedCookie);
    }

    // Reenviar petición con seguimiento de redirecciones (avatares Canvas/S3)
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

      // Construir respuesta con CORS abierto
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

