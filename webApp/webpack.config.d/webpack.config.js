;(function(config) {
    if (config.devServer) {
        config.devServer.headers = [
            { key: 'Cross-Origin-Opener-Policy', value: 'same-origin' },
            { key: 'Cross-Origin-Embedder-Policy', value: 'credentialless' }
        ];

        const http = require('http');
        const https = require('https');
        const { URL } = require('url');

        const originalSetup = config.devServer.setupMiddlewares;
        config.devServer.setupMiddlewares = function(middlewares, devServer) {
            const proxyHandler = function(req, res, next) {
                if (!req.url.startsWith('/api-proxy')) {
                    return next ? next() : undefined;
                }

                if (req.method === 'OPTIONS') {
                    res.setHeader('Access-Control-Allow-Origin', '*');
                    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS, HEAD');
                    res.setHeader('Access-Control-Allow-Headers', req.headers['access-control-request-headers'] || '*');
                    res.statusCode = 204;
                    return res.end();
                }

                let targetUrlStr = req.headers['x-target-url'];
                if (!targetUrlStr) {
                    const u = new URL(req.url, 'http://localhost:8080');
                    targetUrlStr = u.searchParams.get('url');
                    if (targetUrlStr) {
                        try {
                            const parsedTarget = new URL(targetUrlStr);
                            for (const [k, v] of u.searchParams.entries()) {
                                if (k !== 'url') parsedTarget.searchParams.set(k, v);
                            }
                            targetUrlStr = parsedTarget.toString();
                        } catch (_) {}
                    }
                }

                if (!targetUrlStr) {
                    res.statusCode = 400;
                    res.setHeader('Access-Control-Allow-Origin', '*');
                    return res.end('Missing target URL');
                }

                let initialHost = '';
                try {
                    initialHost = new URL(targetUrlStr).host;
                } catch (_) {}

                const sendProxyRequest = function(currentUrlStr, currentMethod, bodyBuffer, redirectCount) {
                    if (redirectCount > 5) {
                        res.statusCode = 508;
                        res.setHeader('Access-Control-Allow-Origin', '*');
                        return res.end('Too many redirects');
                    }

                    let targetParsed;
                    try {
                        targetParsed = new URL(currentUrlStr);
                    } catch (e) {
                        res.statusCode = 400;
                        res.setHeader('Access-Control-Allow-Origin', '*');
                        return res.end('Invalid URL: ' + currentUrlStr);
                    }

                    const isHttps = targetParsed.protocol === 'https:';
                    const client = isHttps ? https : http;

                    const proxyHeaders = { ...req.headers };
                    delete proxyHeaders.host;
                    delete proxyHeaders.origin;
                    delete proxyHeaders.referer;
                    delete proxyHeaders['x-target-url'];
                    proxyHeaders['host'] = targetParsed.host;

                    const forwardedCookie = req.headers['x-forwarded-cookie'] || req.headers['x-proxy-cookie'];
                    if (forwardedCookie) {
                        proxyHeaders['cookie'] = forwardedCookie;
                    }

                    if (redirectCount > 0 && targetParsed.host !== initialHost) {
                        delete proxyHeaders['authorization'];
                        delete proxyHeaders['cookie'];
                    }

                    if (bodyBuffer && bodyBuffer.length > 0) {
                        proxyHeaders['content-length'] = bodyBuffer.length;
                    } else {
                        delete proxyHeaders['content-length'];
                    }

                    const options = {
                        protocol: targetParsed.protocol,
                        hostname: targetParsed.hostname,
                        port: targetParsed.port || (isHttps ? 443 : 80),
                        path: targetParsed.pathname + targetParsed.search,
                        method: currentMethod,
                        headers: proxyHeaders,
                        rejectUnauthorized: false
                    };

                    const proxyReq = client.request(options, function(proxyRes) {
                        const statusCode = proxyRes.statusCode || 200;
                        if (statusCode >= 300 && statusCode < 400 && proxyRes.headers.location) {
                            proxyRes.resume();
                            const nextUrl = new URL(proxyRes.headers.location, currentUrlStr).toString();
                            const nextMethod = (statusCode === 301 || statusCode === 302 || statusCode === 303) ? 'GET' : currentMethod;
                            const nextBody = (nextMethod === 'GET' || nextMethod === 'HEAD') ? null : bodyBuffer;
                            return sendProxyRequest(nextUrl, nextMethod, nextBody, redirectCount + 1);
                        }

                        res.statusCode = statusCode;
                        res.setHeader('Access-Control-Allow-Origin', '*');
                        res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS, HEAD');
                        res.setHeader('Access-Control-Allow-Headers', '*');

                        const hopByHop = new Set([
                            'connection',
                            'keep-alive',
                            'proxy-authenticate',
                            'proxy-authorization',
                            'te',
                            'trailer',
                            'transfer-encoding',
                            'upgrade',
                            'access-control-allow-origin',
                            'content-security-policy'
                        ]);

                        for (const [key, value] of Object.entries(proxyRes.headers)) {
                            const lowerKey = key.toLowerCase();
                            if (!hopByHop.has(lowerKey)) {
                                res.setHeader(key, value);
                            }
                        }

                        proxyRes.pipe(res);
                    });

                    proxyReq.on('error', function(err) {
                        if (!res.headersSent) {
                            res.statusCode = 502;
                            res.setHeader('Access-Control-Allow-Origin', '*');
                            res.end('Proxy error: ' + err.message);
                        }
                    });

                    if (bodyBuffer && bodyBuffer.length > 0 && currentMethod !== 'GET' && currentMethod !== 'HEAD') {
                        proxyReq.write(bodyBuffer);
                    }
                    proxyReq.end();
                };

                try {
                    const chunks = [];
                    req.on('data', function(chunk) {
                        chunks.push(chunk);
                    });
                    req.on('end', function() {
                        const bodyBuffer = chunks.length > 0 ? Buffer.concat(chunks) : null;
                        sendProxyRequest(targetUrlStr, req.method, bodyBuffer, 0);
                    });
                    req.on('error', function(err) {
                        if (!res.headersSent) {
                            res.statusCode = 400;
                            res.setHeader('Access-Control-Allow-Origin', '*');
                            res.end('Request stream error: ' + err.message);
                        }
                    });
                } catch (err) {
                    if (!res.headersSent) {
                        res.statusCode = 500;
                        res.setHeader('Access-Control-Allow-Origin', '*');
                        res.end('Server error: ' + err.message);
                    }
                }
            };

            if (devServer && devServer.app && devServer.app.use) {
                devServer.app.use('/api-proxy', proxyHandler);
            }
            if (Array.isArray(middlewares)) {
                middlewares.unshift({
                    name: 'canvas-api-proxy',
                    middleware: proxyHandler
                });
            }

            if (originalSetup) {
                return originalSetup(middlewares, devServer);
            }
            return middlewares;
        };
    }
})(config);
