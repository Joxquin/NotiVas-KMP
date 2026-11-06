const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const bridgeDir = path.resolve(__dirname, "..");
const origin = "https://joxquin.github.io";
const appUrl = `${origin}/NotiVas-KMP/`;
const sessionCookies = [{ name: "canvas_session", value: "test-session" }];

function background({ firefox = false, cookies = sessionCookies } = {}) {
  let listener;
  const queries = [];
  const api = {
    runtime: { id: "test-extension", onMessage: { addListener(fn) { listener = fn; } } },
    cookies: {
      getAll(details, callback) {
        queries.push(details);
        if (firefox) {
          assert.equal(callback, undefined, "Firefox uses the Promise API");
          return Promise.resolve(cookies);
        }
        callback(cookies);
      }
    }
  };
  const context = { URL, [firefox ? "browser" : "chrome"]: api };
  vm.runInNewContext(fs.readFileSync(path.join(bridgeDir, "background.js"), "utf8"), context);
  return {
    queries,
    send(domain, overrides = {}) {
      return new Promise(resolve => listener(
        { action: "GET_CANVAS_COOKIES", domain },
        { id: api.runtime.id, url: appUrl, frameId: 0, ...overrides },
        resolve
      ));
    }
  };
}

function content({ href = appUrl, firefox = false, iframe = false } = {}) {
  const posted = [];
  const sent = [];
  let listener;
  const window = {
    location: new URL(href),
    postMessage(message, targetOrigin) { posted.push({ message, targetOrigin }); },
    addEventListener(type, fn) { if (type === "message") listener = fn; }
  };
  window.top = iframe ? {} : window;
  const api = {
    runtime: {
      sendMessage(message, callback) {
        sent.push(message);
        const response = { success: true, cookieHeader: "canvas_session=test-session" };
        if (firefox) {
          assert.equal(callback, undefined);
          return Promise.resolve(response);
        }
        callback(response);
      }
    }
  };
  vm.runInNewContext(fs.readFileSync(path.join(bridgeDir, "content.js"), "utf8"), {
    window,
    [firefox ? "browser" : "chrome"]: api,
    setInterval() { return 1; },
    clearInterval() {}
  });
  return {
    posted, sent,
    async request(overrides = {}) {
      listener({
        source: window,
        origin,
        data: { type: "NOTIVAS_REQUEST_SESSION", domain: "tecsup.instructure.com", requestId: 42 },
        ...overrides
      });
      await Promise.resolve();
    }
  };
}

test("manifest only exposes the bridge to the official HTTPS app", () => {
  const manifest = JSON.parse(fs.readFileSync(path.join(bridgeDir, "manifest.json"), "utf8"));
  assert.deepEqual(manifest.content_scripts[0].matches, [`${appUrl}*`]);
  assert.deepEqual(manifest.host_permissions, ["https://*.instructure.com/*", "https://canvas.utp.edu.pe/*"]);
  assert.equal(manifest.content_scripts[0].all_frames, undefined);
});

test("background rejects untrusted senders without reading cookies", async t => {
  for (const sender of [
    { url: "https://attacker.example/" },
    { url: `${origin}/another-project/` },
    { url: `${origin}/NotiVas-KMP-lookalike/` },
    { url: "http://joxquin.github.io/NotiVas-KMP/" },
    { url: "https://joxquin.github.io.attacker.example/NotiVas-KMP/" },
    { url: "file:///NotiVas-KMP/index.html" },
    { url: undefined },
    { id: "another-extension" },
    { frameId: 1 }
  ]) {
    await t.test(JSON.stringify(sender), async () => {
      const worker = background();
      assert.equal((await worker.send("tecsup.instructure.com", sender)).success, false);
      assert.equal(worker.queries.length, 0);
    });
  }
});

test("background rejects unrelated domains and ambiguous URLs", async t => {
  for (const domain of [
    "accounts.google.com", "instructure.com", "fakeinstructure.com",
    "tecsup.instructure.com.attacker.example", "http://tecsup.instructure.com",
    "https://user:pass@tecsup.instructure.com", "tecsup.instructure.com:444",
    "tecsup.instructure.com/login", "tecsup.instructure.com?other=1",
    "tecsup.instructure.com#other", "", null
  ]) {
    await t.test(String(domain), async () => {
      const worker = background();
      assert.equal((await worker.send(domain)).success, false);
      assert.equal(worker.queries.length, 0);
    });
  }
});

test("Canvas and UTP work with Chrome callback and Firefox Promise APIs", async t => {
  for (const firefox of [false, true]) {
    for (const domain of ["tecsup.instructure.com", "canvas.utp.edu.pe", "https://TECSUP.instructure.com/"]) {
      await t.test(`${firefox ? "Firefox" : "Chrome"}: ${domain}`, async () => {
        const worker = background({ firefox });
        const response = await worker.send(domain);
        assert.equal(response.success, true);
        assert.equal(response.hasSession, true);
        assert.equal(response.cookieHeader, "canvas_session=test-session");
        assert.equal(worker.queries.length, 1);
        assert.equal(worker.queries[0].url, `https://${response.domain}/`);
        assert.equal(worker.queries[0].domain, undefined);
      });
    }
  }
});

test("missing campus cookies never falls back to another institution", async () => {
  const worker = background({ cookies: [] });
  assert.equal((await worker.send("tecsup.instructure.com")).success, false);
  assert.equal(worker.queries.length, 1);
  assert.equal(worker.queries[0].url, "https://tecsup.instructure.com/");
});

test("content script rejects foreign origins and frame messages", async () => {
  const script = content();
  await script.request({ origin: "https://attacker.example" });
  await script.request({ source: {} });
  assert.equal(script.sent.length, 0);
  assert.equal(script.posted.length, 1);
});

test("content script stays inactive outside the app or in an iframe", async t => {
  for (const options of [
    { href: "https://attacker.example/NotiVas-KMP/" },
    { href: `${origin}/another-project/` },
    { iframe: true }
  ]) {
    await t.test(JSON.stringify(options), async () => {
      const script = content(options);
      await script.request();
      assert.equal(script.sent.length, 0);
      assert.equal(script.posted.length, 0);
    });
  }
});

test("trusted responses are bound to the app origin on both browser APIs", async t => {
  for (const firefox of [false, true]) {
    await t.test(firefox ? "Firefox" : "Chrome", async () => {
      const script = content({ firefox });
      await script.request();
      assert.equal(script.sent.length, 1);
      assert.equal(script.posted.length, 2);
      assert.ok(script.posted.every(entry => entry.targetOrigin === origin));
      const response = script.posted[1].message;
      assert.equal(response.type, "NOTIVAS_RESPONSE_SESSION");
      assert.equal(response.requestId, 42);
      assert.equal(response.payload.success, true);
    });
  }
});
