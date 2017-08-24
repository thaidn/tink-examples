// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.timestamper;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/** The class embedding the webserver for our Timestamper app. */
public class Server {
  @Option(
      name = "--keyset",
      metaVar = "path/to/keyset.cfg",
      required = true,
      usage = "The path to the keyset, generate new if does not exist")
  File keyset;

  @Option(
      name = "--key-type",
      metaVar = "AES-GCM | AES-CBC-HMAC",
      required = true,
      usage = "The key type of the keys in the keyset, case-insensitive")
  String keyType;

  private void run() throws Exception {
    System.out.println("Initializing Tink...");
    // Initialize the Tink runtime.
    CryptoService.init(keyset, keyType);
    System.out.println("Web server starting on http://localhost:8000/");
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/api/", new ApiHandler());
    server.createContext("/", new StaticFileHandler());
    server.start();
  }

  /** The HTTP handler that will reply to API requests. */
  static class ApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
      String requestPath = t.getRequestURI().getPath();
      ApiActions apiHandler;

      try {
        if (t.getRequestMethod().equals("POST") && requestPath.equals("/api/notarize")) {
          apiHandler = ApiActions.CreateNewToken.fromJson(readRequestBody(t));
        } else if (t.getRequestMethod().equals("POST") && requestPath.equals("/api/attest")) {
          apiHandler = ApiActions.AttestTokenIsValid.fromJson(readRequestBody(t));
        } else if (t.getRequestMethod().equals("POST") && requestPath.equals("/api/rotate")) {
          apiHandler = ApiActions.RotateKey.fromJson(readRequestBody(t));
        } else {
          replyWith(t, 404, "text/plain", "Not found");
          return;
        }
        replyWith(t, 200, "application/json", apiHandler.execute().toJson());
      } catch (Exception e) {
        // This is for demo purposes. Exposing exceptions in prod is generally a bad idea: even if
        // Tink won't make your cryptography insecure this way, this might expose business logic
        // too, like for instance not having crypto keys for a non-existing user.
        replyWith(t, 500, "text/plain", "Exception happened:" + e);
      }

    }
  }

  /** The HTTP handler that will reply to static files requests. */
  static class StaticFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
      String requestPath = t.getRequestURI().getPath();

      if (requestPath.equals("/") || requestPath.equals("/index.html")) {
        replyWith(t, 200, "text/html", readResource("/static/index.html"));
      } else if (requestPath.equals("/app.js")) {
        replyWith(t, 200, "application/javascript", readResource("/static/app.js"));
      } else {
        replyWith(t, 404, "text/plain", "Not found");
      }
    }
  }

  /** Send a HTTP response with the given parameters. */
  static void replyWith(HttpExchange t, int code, String contentType, String response)
      throws IOException {
    Headers headers = t.getResponseHeaders();
    headers.add("contentType", contentType);
    t.sendResponseHeaders(code, response.length());
    OutputStream os = t.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  /** Read a request body and output it as string. */
  static String readRequestBody(HttpExchange t) throws IOException {
    InputStream is = t.getRequestBody();
    String value = new Scanner(is).useDelimiter("\\A").next();
    is.close();
    return value;
  }

  /** Read a resource and output it as string. */
  static String readResource(String name) throws IOException {
    InputStream is = Server.class.getResourceAsStream(name);
    String value = new Scanner(is).useDelimiter("\\A").next();
    is.close();
    return value;
  }

  public static void main(String[] args) throws Exception {
    Server server = new Server();
    CmdLineParser parser = new CmdLineParser(server);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.out.println(e);
      e.getParser().printUsage(System.out);
      System.exit(1);
    }
    server.run();
  }
}
