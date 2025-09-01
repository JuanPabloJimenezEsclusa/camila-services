import http from 'k6/http';
import { group, check, sleep } from 'k6';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.1.0/index.js";

const BASE_URL_PROTOCOL = __ENV.BASE_URL_PROTOCOL || 'http';
const BASE_URL = __ENV.BASE_URL || 'localhost';
const BASE_URL_PORT = __ENV.BASE_URL_PORT || '8090';
const BASE_PATH = __ENV.BASE_PATH || 'product-dev';
const OAUTH_URL_PROTOCOL = __ENV.OAUTH_URL_PROTOCOL || 'http';
const OAUTH_URL = __ENV.OAUTH_URL || 'localhost';
const OAUTH_URL_PORT = __ENV.OAUTH_URL_PORT || '9191';
const OAUTH_PATH = __ENV.OAUTH_PATH || '/realms/camila-realm/protocol/openid-connect/token';
const OAUTH_GRANT_TYPE = __ENV.OAUTH_GRANT_TYPE || 'client_credentials';
const OAUTH_SCOPE = __ENV.OAUTH_SCOPE || 'camila/read camila/write';
const OAUTH_CLIENT_ID = __ENV.OAUTH_CLIENT_ID || 'camila-client';
const OAUTH_CLIENT_SECRET = __ENV.OAUTH_CLIENT_SECRET || 'Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE';
const THREADS = parseInt(__ENV.THREADS || '100');
const RAMP_UP = parseInt(__ENV.RAMP_UP || '20');
const LOOPS = parseInt(__ENV.LOOPS || '10');

export const options = {
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.05'],
  },
  scenarios: {
    token_initialization: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: '10s',
      exec: 'getToken'
    },
    rest_api_tests: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: `${RAMP_UP}s`, target: THREADS },
        { duration: `${LOOPS * 10}s`, target: THREADS },
        { duration: '10s', target: 0 }
      ],
      startTime: '5s',
      exec: 'testApiEndpoints',
    }
  }
};

let tokenCache = {
  accessToken: ''
};

export function getToken() {
  const tokenUrl = `${OAUTH_URL_PROTOCOL}://${OAUTH_URL}:${OAUTH_URL_PORT}${OAUTH_PATH}`;
  const payload = {
    grant_type: OAUTH_GRANT_TYPE,
    scopes: OAUTH_SCOPE,
    client_id: OAUTH_CLIENT_ID,
    client_secret: OAUTH_CLIENT_SECRET
  };
  const params = {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Accept': 'application/json',
      'Accept-Encoding': 'gzip, deflate'
    },
  };
  const response = http.post(tokenUrl, payload, params);
  check(response, {
    'Status is 200': (r) => r.status === 200,
    'Token obtained': (r) => r.json('access_token') !== '',
  });
  if (response.status === 200) {
    tokenCache.accessToken = response.json('access_token');
  } else {
    console.error(`Failed to obtain token: ${response.status} ${response.body}`);
  }
}

function generateRandomParameters() {
  const rand1 = Math.random();
  const rand2 = Math.random();
  const rand3 = Math.random();
  const rand4 = Math.random();
  const sumaTotal = rand1 + rand2 + rand3 + rand4;
  const salesUnits = (rand1 / sumaTotal).toFixed(4);
  const stock = (rand2 / sumaTotal).toFixed(4);
  const profitMargin = (rand3 / sumaTotal).toFixed(4);
  const daysInStock = (rand4 / sumaTotal).toFixed(4);
  return {
    salesUnits,
    stock,
    profitMargin,
    daysInStock,
    page: Math.floor(Math.random() * 2),
    size: Math.floor(Math.random() * 200) + 1
  };
}

function apiGetJsonRequest(params) {
  const url = `${BASE_URL_PROTOCOL}://${BASE_URL}:${BASE_URL_PORT}/${BASE_PATH}/api/products` +
    `?salesUnits=${params.salesUnits}&stock=${params.stock}&profitMargin=${params.profitMargin}&daysInStock=${params.daysInStock}&page=${params.page}&size=${params.size}`;
  const headers = {
    'Authorization': `Bearer ${tokenCache.accessToken}`,
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'Accept-Language': 'es-ES,es',
    'Accept-Encoding': 'gzip, deflate'
  };
  const response = http.get(url, { headers, tags: { name: 'json_endpoint', type: 'REST', format: 'JSON' } });
  check(response, {
    'JSON status is 200 or 204': (r) => r.status === 200 || r.status === 204,
    'JSON response size < 100KB': (r) => r.body.length < 100000,
  });
  console.log(`Request: ${url} - Response status: ${response.status}`);
  return response;
}

function apiGetNdjsonRequest(params) {
  const url = `${BASE_URL_PROTOCOL}://${BASE_URL}:${BASE_URL_PORT}/${BASE_PATH}/api/products` +
    `?salesUnits=${params.salesUnits}&stock=${params.stock}&profitMargin=${params.profitMargin}&daysInStock=${params.daysInStock}&page=${params.page}&size=${params.size}`;
  const headers = {
    'Authorization': `Bearer ${tokenCache.accessToken}`,
    'Accept': 'application/x-ndjson',
    'Content-Type': 'application/json',
    'Accept-Language': 'es-ES,es',
    'Accept-Encoding': 'gzip, deflate'
  };
  const response = http.get(url, { headers, tags: { name: 'ndjson_endpoint', type: 'REST', format: 'NDJSON' } });
  check(response, {
    'NDJSON status is 200 or 204': (r) => r.status === 200 || r.status === 204,
    'NDJSON response size < 100KB': (r) => r.body.length < 100000,
  });
  console.log(`Request: ${url} - Response status: ${response.status}`);
  return response;
}

function apiGetEventStreamRequest(params) {
  const url = `${BASE_URL_PROTOCOL}://${BASE_URL}:${BASE_URL_PORT}/${BASE_PATH}/api/products` +
    `?salesUnits=${params.salesUnits}&stock=${params.stock}&profitMargin=${params.profitMargin}&daysInStock=${params.daysInStock}&page=${params.page}&size=${params.size}`;
  const headers = {
    'Authorization': `Bearer ${tokenCache.accessToken}`,
    'Accept': 'text/event-stream',
    'Content-Type': 'application/json',
    'Accept-Language': 'es-ES,es',
    'Accept-Encoding': 'gzip, deflate'
  };
  const response = http.get(url, { headers, tags: { name: 'event_stream_endpoint', type: 'REST', format: 'EVENT_STREAM' } });
  check(response, {
    'EventStream status is 200 or 204': (r) => r.status === 200 || r.status === 204,
    'EventStream response size < 100KB': (r) => r.body.length < 100000,
  });
  console.log(`Request: ${url} - Response status: ${response.status}`);
  return response;
}

function apiGraphQLRequest(params) {
  const url = `${BASE_URL_PROTOCOL}://${BASE_URL}:${BASE_URL_PORT}/${BASE_PATH}/api/graphql`;
  const payload = JSON.stringify({
    operationName: null,
    variables: {
      salesUnits: parseFloat(params.salesUnits),
      stock: parseFloat(params.stock),
      profitMargin: parseFloat(params.profitMargin),
      daysInStock: parseFloat(params.daysInStock),
      page: parseInt(params.page),
      size: parseInt(params.size),
      withDetails: false
    },
    query: `query sortProducts($salesUnits: Float, $stock: Float, $profitMargin: Float, $daysInStock: Float, $page: Int, $size: Int, $withDetails: Boolean!) {
      sortProducts(salesUnits: $salesUnits, stock: $stock, profitMargin: $profitMargin, daysInStock: $daysInStock, page: $page, size: $size) {
        id @include(if: $withDetails)
        internalId @include(if: $withDetails)
        category @include(if: $withDetails)
        name
        salesUnits
        stock
      }
    }`
  });
  const headers = {
    'Authorization': `Bearer ${tokenCache.accessToken}`,
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'Accept-Language': 'es-ES,es',
    'Accept-Encoding': 'gzip, deflate'
  };
  const response = http.post(url, payload, { headers, tags: { name: 'graphql_endpoint', type: 'GRAPHQL', format: 'JSON' } });
  check(response, {
    'GraphQL status is 200': (r) => r.status === 200,
    'GraphQL response size < 100KB': (r) => r.body.length < 100000,
  });
  console.log(`Request: ${url} - Payload: ${payload} - Response status: ${response.status}`);
  return response;
}

export function testApiEndpoints() {
  getToken();

  const randomParams = generateRandomParameters();
  group('product-api', () => {
    const apiCalls = [
      () => apiGetJsonRequest(randomParams),
      () => apiGetNdjsonRequest(randomParams),
      () => apiGetEventStreamRequest(randomParams),
      () => apiGraphQLRequest(randomParams)
    ];
    for (let i = apiCalls.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [apiCalls[i], apiCalls[j]] = [apiCalls[j], apiCalls[i]];
    }

    for (let i = 0; i < apiCalls.length; i++) {
      apiCalls[i]();

      if (i < apiCalls.length - 1) {
        sleep(1 + Math.random());
      }
    }
    sleep(Math.random() * 2 + 0.5);
  });
}

export function handleSummary(data) {
  return {
    "/result/summary.html": htmlReport(data),
    stdout: textSummary(data, { indent: " ", enableColors: true }),
  };
}
