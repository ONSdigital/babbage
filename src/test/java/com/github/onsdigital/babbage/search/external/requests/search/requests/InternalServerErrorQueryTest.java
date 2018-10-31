package com.github.onsdigital.babbage.search.external.requests.search.requests;

import com.github.onsdigital.babbage.search.external.MockedHttpRequest;
import com.github.onsdigital.babbage.search.external.MockedSearchErrorResponse;
import com.github.onsdigital.babbage.search.external.SearchClient;
import com.github.onsdigital.babbage.search.external.requests.search.exceptions.InvalidSearchResponse;
import com.github.onsdigital.babbage.search.model.SearchResult;
import com.github.onsdigital.babbage.util.TestsUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class InternalServerErrorQueryTest {

    private final String searchTerm = "Who ya gonna call?";
    private final ListType listType = ListType.ONS;
    private final int page = 1;
    private final int pageSize = 10;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private SearchClient searchClient;

    private ContentQuery contentQuery;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        contentQuery = new ContentQuery(searchTerm, listType, page, pageSize);
        TestsUtil.setPrivateField(contentQuery, "searchClient", searchClient);

        MockedSearchErrorResponse mockedResponse = new MockedSearchErrorResponse();

        MockedHttpRequest mockedHttpRequest = new MockedHttpRequest(contentQuery.targetUri().build(), mockedResponse);

        when(searchClient.get(contentQuery.targetUri()))
                .thenReturn(mockedHttpRequest);

        when(searchClient.post(contentQuery.targetUri()))
                .thenReturn(mockedHttpRequest);
    }

    @Test
    public void testInternalServerError() throws Exception {

        exception.expect(InvalidSearchResponse.class);
        SearchResult result = contentQuery.call();
    }

}
