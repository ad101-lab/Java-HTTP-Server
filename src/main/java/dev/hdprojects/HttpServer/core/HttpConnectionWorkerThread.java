package dev.hdprojects.HttpServer.core;

import dev.hdprojects.http.GenerateHttpHeader;
import dev.hdprojects.http.HttpParser;
import dev.hdprojects.website.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnectionWorkerThread extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);

    private String webRoot;
    private Socket socket;

    public HttpConnectionWorkerThread(Socket socket, String webRoot) {
        this.webRoot = webRoot;
        this.socket = socket;
    }

    @Override
    public void run() {

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {

            // Create the streams to write to
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            // Create an instance of the HttpParser
            LOGGER.info("Starting HTTP Parser ... ");
            HttpParser httpParser = new HttpParser(inputStream);
            httpParser.parseHttpRequest();
            HtmlParser htmlParser = new HtmlParser(new File(webRoot + httpParser.getRequestedPage()), "", "", "", "");
            LOGGER.info("Done With HTTP Parser.");

            // Set HTML variable
            String html = htmlParser.toString();

            /* 13, 10 ASCII */
            final String CRLF = "\r\n";

            // Get the length of the html
            int contentLength = html.getBytes().length;

            // Generate an HTTP Header and response
            LOGGER.info("Generating HTTP Header ... ");
            GenerateHttpHeader httpHeader = new GenerateHttpHeader(200, contentLength, "text/html", "hd");
            String response = httpHeader.toString() + html + CRLF + CRLF;
            LOGGER.info("Done Generating HTTP Header.");

            // Send the response
            LOGGER.info("Sending response ... ");
            outputStream.write(response.getBytes());

            LOGGER.info("Connection processing finished");
        } catch (IOException e) {
            LOGGER.error("Problem with communication", e);
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {}
            }

            if(outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }

            if(socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
    }
}
