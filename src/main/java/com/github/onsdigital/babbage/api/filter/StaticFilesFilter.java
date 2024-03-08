package com.github.onsdigital.babbage.api.filter;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.response.BabbageContentBasedBinaryResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.onsdigital.babbage.api.error.ErrorHandler.handle;

public class StaticFilesFilter implements Filter {

    private static final String visualisationRoot = "visualisations";

    @Override
    public boolean filter(HttpServletRequest request, HttpServletResponse response) {

        try {
            String uri = request.getRequestURI();
            Path requestPath = Paths.get(uri);

            if (requestPath.getNameCount() < 2) // requires two parts to the path to be a valid visualisation: /visualisation/code/
                return true; // there is no path, do not try and handle it.


            // only go in here if we have a URI that starts /visualisation/....
            Path endpoint = requestPath.getName(0); // /visualisations
            if (endpoint.toString().equalsIgnoreCase(visualisationRoot)) {

                Path uid = Paths.get(uri).getName(1);  // dvc123
                String path = Paths.get("/" + visualisationRoot + "/" + uid).relativize(requestPath).toString();

                String visualisationPath = String.format("/%s/%s/content/%s", visualisationRoot, uid, path);
                ContentResponse contentResponse = ContentClient.getInstance().getResource(visualisationPath);
                new BabbageContentBasedBinaryResponse(
                        contentResponse,
                        contentResponse.getDataStream(),
                        contentResponse.getMimeType()).apply(request, response);

                return false; // we have the response we require, do not continue to process this request.
            }
        } catch (Throwable t) {
            try {
                handle(request, response, t);
            } catch (IOException e) {
                return true;
            }
        }

        return true; // continue onto other filters / handlers
    }

    /**
     * Takes the path of the index pages and 'merges' it with the relative path of the file.
     *
     * @param indexPage
     * @param file
     * @return
     */
    public static String resolveRelativePath(String indexPage, String file) {
        Path index = Paths.get(indexPage);
        Path path = Paths.get(file);

        Path result = Paths.get("");

        if (index.getNameCount() == path.getNameCount()) {
            return file;
        }

        if (index.getParent() != null) {
            for (Path indexPart : index.getParent()) {
                if (!path.getName(0).equals(indexPart)) {
                    result = result.resolve(indexPart);
                }
            }
        }

        result = result.resolve(path);
        return result.toString();
    }

    private String getIndexPagePath(Path uid) throws ContentReadException, IOException {
        String path;
        String jsonPath = String.format("/%s/%s", visualisationRoot, uid);
        ContentResponse contentResponse = ContentClient.getInstance().getContent(jsonPath);
        JsonObject obj = JsonParser.parseString(contentResponse.getAsString()).getAsJsonObject();
        JsonElement indexPage = obj.get("indexPage");
        path = indexPage == null ? "" : indexPage.getAsString();
        return path;
    }
}