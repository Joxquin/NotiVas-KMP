// WebWorkerSQLiteDriver worker implementation for WebAssembly/JS targets
importScripts("https://cdnjs.cloudflare.com/ajax/libs/sql.js/1.13.0/sql-wasm.js");

let SQL = null;
const databases = new Map();
const statements = new Map();

let nextDatabaseId = 0;
let nextStatementId = 0;

// IndexedDB persistence helper
const DB_STORE_NAME = "notivas_sqlite_store";
const DB_KEY = "database_bytes";

function openIndexedDb() {
    return new Promise((resolve, reject) => {
        const req = indexedDB.open("notivas_db_persistence", 1);
        req.onupgradeneeded = () => {
            req.result.createObjectStore(DB_STORE_NAME);
        };
        req.onsuccess = () => resolve(req.result);
        req.onerror = () => reject(req.error);
    });
}

async function loadPersistedDb() {
    try {
        const db = await openIndexedDb();
        return new Promise((resolve) => {
            const tx = db.transaction(DB_STORE_NAME, "readonly");
            const store = tx.objectStore(DB_STORE_NAME);
            const req = store.get(DB_KEY);
            req.onsuccess = () => resolve(req.result || null);
            req.onerror = () => resolve(null);
        });
    } catch (e) {
        return null;
    }
}

async function savePersistedDb(database) {
    try {
        const bytes = database.export();
        const db = await openIndexedDb();
        return new Promise((resolve, reject) => {
            const tx = db.transaction(DB_STORE_NAME, "readwrite");
            const store = tx.objectStore(DB_STORE_NAME);
            const req = store.put(bytes, DB_KEY);
            req.onsuccess = () => resolve();
            req.onerror = () => reject(req.error);
        });
    } catch (e) {
        console.warn("Failed to persist SQLite database to IndexedDB", e);
    }
}

async function openRequest(id, requestData) {
    try {
        const newDatabaseId = nextDatabaseId++;
        const savedData = await loadPersistedDb();
        const newDatabase = savedData ? new SQL.Database(savedData) : new SQL.Database();
        databases.set(newDatabaseId, newDatabase);
        postMessage({ 'id': id, data: { 'databaseId': newDatabaseId } });
    } catch (error) {
        postMessage({ 'id': id, error: error.message || String(error) });
    }
}

function prepareRequest(id, requestData) {
    try {
        const newStatementId = nextStatementId++;
        const database = databases.get(requestData.databaseId);
        if (!database) {
            postMessage({ 'id': id, error: "Invalid database ID: " + requestData.databaseId });
            return;
        }
        const statement = database.prepare(requestData.sql);
        statements.set(newStatementId, { stmt: statement, dbId: requestData.databaseId, sql: requestData.sql });

        const resultData = {
            'statementId': newStatementId,
            'parameterCount': 256,
            'columnNames': statement.getColumnNames()
        };

        postMessage({ 'id': id, data: resultData });
    } catch (error) {
        postMessage({ 'id': id, error: error.message || String(error) });
    }
}

function stepRequest(id, requestData) {
    const entry = statements.get(requestData.statementId);
    if (!entry) {
        postMessage({ 'id': id, error: "Invalid statement ID: " + requestData.statementId });
        return;
    }
    const statement = entry.stmt;

    try {
        const resultData = {
            'rows': [],
            'columnTypes': []
        };

        statement.reset();

        if (requestData.bindings && requestData.bindings.length > 0) {
            statement.bind(requestData.bindings);
        }

        while (statement.step()) {
            const row = statement.get();

            if (resultData.columnTypes.length === 0) {
                for (let i = 0; i < row.length; i++) {
                    const val = row[i];
                    if (val === null) {
                        resultData.columnTypes.push(5); // NULL
                    } else if (typeof val === 'number') {
                        resultData.columnTypes.push(Number.isInteger(val) ? 1 : 2); // INTEGER or FLOAT
                    } else if (typeof val === 'string') {
                        resultData.columnTypes.push(3); // TEXT
                    } else if (val instanceof Uint8Array) {
                        resultData.columnTypes.push(4); // BLOB
                    } else {
                        resultData.columnTypes.push(5);
                    }
                }
            }

            resultData.rows.push(row);
        }

        // Auto-save mutations to IndexedDB asynchronously
        const isMutation = /^\s*(INSERT|UPDATE|DELETE|CREATE|DROP|ALTER)/i.test(entry.sql);
        if (isMutation) {
            const db = databases.get(entry.dbId);
            if (db) {
                savePersistedDb(db);
            }
        }

        postMessage({ 'id': id, data: resultData });
    } catch (error) {
        postMessage({ 'id': id, error: error.message || String(error) });
    }
}

function closeRequest(id, requestData) {
    if (requestData.statementId !== undefined && requestData.statementId !== null) {
        const entry = statements.get(requestData.statementId);
        if (entry) {
            try {
                entry.stmt.free();
                statements.delete(requestData.statementId);
            } catch (error) {
                postMessage({ 'id': id, error: error.message || String(error) });
                return;
            }
        }
    }

    if (requestData.databaseId !== undefined && requestData.databaseId !== null) {
        const database = databases.get(requestData.databaseId);
        if (database) {
            try {
                savePersistedDb(database);
                database.close();
                databases.delete(requestData.databaseId);
            } catch (error) {
                postMessage({ 'id': id, error: error.message || String(error) });
                return;
            }
        }
    }
}

const commandMap = {
    'open': openRequest,
    'prepare': prepareRequest,
    'step': stepRequest,
    'close': closeRequest,
};

function handleMessage(e) {
    const requestMsg = e.data;
    if (!requestMsg || typeof requestMsg !== 'object') return;
    if (!('data' in requestMsg) || requestMsg.data === null) {
        postMessage({ 'id': requestMsg.id, 'error': "Invalid request: missing data" });
        return;
    }
    const cmd = requestMsg.data.cmd;
    const handler = commandMap[cmd];
    if (handler) {
        handler(requestMsg.id, requestMsg.data);
    } else {
        postMessage({ 'id': requestMsg.id, 'error': "Invalid request: unknown command " + cmd });
    }
}

const messageQueue = [];
onmessage = (e) => {
    if (!SQL) {
        messageQueue.push(e);
    } else {
        handleMessage(e);
    }
};

initSqlJs({
    locateFile: file => "https://cdnjs.cloudflare.com/ajax/libs/sql.js/1.13.0/" + file
}).then(instance => {
    SQL = instance;
    while (messageQueue.length > 0) {
        handleMessage(messageQueue.shift());
    }
}).catch(err => {
    console.error("Failed to initialize sql.js in worker:", err);
});

