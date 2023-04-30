export type HttpStatusCode =
  | 100
  | 101
  | 102
  | 200
  | 201
  | 202
  | 203
  | 204
  | 205
  | 206
  | 207
  | 208
  | 226
  | 300
  | 301
  | 302
  | 303
  | 304
  | 305
  | 306
  | 307
  | 308
  | 400
  | 401
  | 402
  | 403
  | 404
  | 405
  | 406
  | 407
  | 408
  | 409
  | 410
  | 411
  | 412
  | 413
  | 414
  | 415
  | 416
  | 417
  | 418
  | 421
  | 422
  | 423
  | 424
  | 425
  | 426
  | 428
  | 429
  | 431
  | 451
  | 500
  | 501
  | 502
  | 503
  | 504
  | 505
  | 506
  | 507
  | 508
  | 510
  | 511;

export type HttpVerb =
  | "GET"
  | "POST"
  | "PUT"
  | "DELETE"
  | "HEAD"
  | "OPTIONS"
  | "TRACE"
  | "CONNECT"
  | "PATCH";

export type FetchJsonOptions<T> = {
  method: HttpVerb;
  url: string;
  headers?: { [key: string]: string };
  token?: string;
  requestData?: any;
  responseDataMapper: { [key in HttpStatusCode]?: (responseData: any) => T };
};

export async function fetchJson<T>(options: FetchJsonOptions<T>): Promise<T> {
  const authHeaders: HeadersInit = options.token
    ? { Authorization: `Bearer ${options.token}` }
    : {};
  const contentTypeHeaders: HeadersInit = options.requestData
    ? { "Content-Type": "application/json" }
    : {};
  const headers = { ...authHeaders, ...contentTypeHeaders, ...options.headers };
  const body = options.requestData ? JSON.stringify(options.requestData) : undefined;
  const response = await fetch(options.url, {
    method: options.method,
    headers,
    body,
  });

  const statusResponseDataMapper = options.responseDataMapper[response.status as HttpStatusCode];
  if (statusResponseDataMapper) {
    let responseData;

    try {
      responseData = await response.json();
    } catch (cause) {
      throw new Error(
        `Fetching JSON for ${options.method} ${options.url} failed. Response cannot be parsed as JSON.`,
        { cause }
      );
    }

    try {
      return statusResponseDataMapper(responseData);
    } catch (cause) {
      throw new Error(
        `Fetching JSON for ${options.method} ${options.url} failed. Response data mapper failed.`,
        {
          cause,
        }
      );
    }
  } else {
    throw new Error(
      `Fetching JSON for ${options.method} ${options.url} failed. No response data mapper found for ${response.status}.`
    );
  }
}
