package com.github.onsdigital.babbage.api.endpoint;

import com.github.davidcarboni.cryptolite.Password;
import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.search.ElasticSearchServer;
import com.github.onsdigital.search.Indexer;
import com.github.onsdigital.search.error.IndexingInProgressException;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * Created by bren on 09/07/15.
 */
@Api
public class ReIndex {

    private static final String REINDEX_KEY_HASH = "5NpB6/uAgk14nYwHzMbIQRnuI2W63MrBOS2279YlcUUY2kNOhrL+R5UFR3O066bQ";

    @POST
    public Object reIndex(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {

        try {
            String key = request.getParameter("key");
            if (Password.verify(key, REINDEX_KEY_HASH)) {
                System.out.println("Triggering reindex");
                Indexer.loadIndex(ElasticSearchServer.getClient());
                response.setStatus(HttpStatus.OK_200);
                return "Elasticsearch: indexing complete";
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED_401);
                return "Wrong key, make sure you pass in the right key";
            }
        } catch (IndexingInProgressException ex) {
            response.setStatus(HttpStatus.CONFLICT_409);
            return "Indexing already in progress.";
        }
    }

    /**
     * Generates new reindexing key/hash values.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        String key = Random.password(64);
        System.out.println("Key (add to environment): " + key);
        System.out.println("Key hash (for REINDEX_KEY_HASH)" + Password.hash(key));
    }


}
