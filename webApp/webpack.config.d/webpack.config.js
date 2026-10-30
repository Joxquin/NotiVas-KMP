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
                    res.setHeader('Access-Control-Allow-Headers', '*');
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

                try {
                    const targetParsed = new URL(targetUrlStr);
                    const isHttps = targetParsed.protocol === 'https:';
                    const client = isHttps ? https : http;

                    const proxyHeaders = { ...req.headers };
                    delete proxyHeaders.host;
                    delete proxyHeaders.origin;
                    delete proxyHeaders.referer;
                    delete proxyHeaders['x-target-url'];
                    proxyHeaders['host'] = targetParsed.host;

                    const options = {
                        protocol: targetParsed.protocol,
                        hostname: targetParsed.hostname,
                        port: targetParsed.port || (isHttps ? 443 : 80),
                        path: targetParsed.pathname + targetParsed.search,
                        method: req.method,
                        headers: proxyHeaders,
                        rejectUnauthorized: false
                    };

                    const proxyReq = client.request(options, function(proxyRes) {
                        res.statusCode = proxyRes.statusCode;
                        res.setHeader('Access-Control-Allow-Origin', '*');
                        res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS, HEAD');
                        res.setHeader('Access-Control-Allow-Headers', '*');

                        for (const [key, value] of Object.entries(proxyRes.headers)) {
                            if (key.toLowerCase() !== 'access-control-allow-origin') {
                                res.setHeader(key, value);
                            }
                        }

                        proxyRes.pipe(res);
                    });

                    proxyReq.on('error', function(err) {
                        res.statusCode = 502;
                        res.setHeader('Access-Control-Allow-Origin', '*');
                        res.end('Proxy error: ' + err.message);
                    });

                    req.pipe(proxyReq);
                } catch (err) {
                    res.statusCode = 500;
                    res.setHeader('Access-Control-Allow-Origin', '*');
                    res.end('Server error: ' + err.message);
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
