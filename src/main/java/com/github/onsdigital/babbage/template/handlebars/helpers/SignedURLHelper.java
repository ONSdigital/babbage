package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;

import java.io.IOException;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;

/**
 * Created by iankent on 03/02/17.
 */
public enum SignedURLHelper implements BabbageHandlebarsHelper<String> {

    //Sign a URL
    sign_url {
        @Override
        public CharSequence apply(String context, Options options) throws IOException {
            if (options.isFalsy(context)) {
                return null;
            }

            String uri = context;
            String term = options.param(0);
            if(term == null || term.length() == 0) {
                term = options.param(1);
            }
            Long page = options.param(2);
            if(page == null) {
                page = 1L;
            }
            Integer index = options.param(3);
            String listType = options.param(4);
            Integer pageSize;
            if (options.param(5) == null) {
                pageSize = 10;
            } else {
                pageSize = Integer.valueOf(options.param(5));
            }

            String token = "";
            try {
                token = JWT.create()
                        .withClaim("uri", uri)
                        .withClaim("term", term)
                        .withClaim("page", (int)(long)page)
                        .withClaim("index", index)
                        .withClaim("listType", listType)
                        .withClaim("pageSize", pageSize)
                        .sign(Algorithm.HMAC256(appConfig().babbage().getRedirectSecret()));
            } catch (JWTCreationException exception){
                throw new RuntimeException("Failed to create JWT", exception);
            }

            return token;
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    }

}
