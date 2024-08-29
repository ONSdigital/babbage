# Regression testing

## Table of contents

1. [Table of contents](#table-of-contents)
1. [Background](#background)
1. [Functionality](#functionality)
    1. [Rendering HTML](#rendering-html)
        1. [Standard content pages](#standard-content-pages)
        1. [Search pages](#search-pages) - SUNSET
            1. [RSS feeds](#rss-feeds)
            1. [Mapping content types to search pages](#mapping-content-types-to-search-pages)
        1. [Error pages](#error-pages)
        1. [Navigation](#navigation)
            1. [Topic API driven navigation](#topic-api-driven-navigation)
        1. [Charts](#charts)
        1. [Tables](#tables)
        1. [Equations](#equations)
        1. [Rendering custom markdown](#rendering-custom-markdown)
    1. [Rendering PDFS](#rendering-pdfs)
    1. [Serving non-HTLM](#serving-non-html)
        1. [JSON](#json)
        1. [Resources](#resources)
        1. [Files](#files)
        1. [PDF](#pdf)
    1. [Calculating cache times and hashes](#calculating-cache-times-and-hashes)
        1. ~[Cache times](#cache-times)~ - DEPRECATED
        1. [Hashes](#hashes)
    1. [Indexing search](#indexing-search) - SUNSET
        1. [Publish pipeline](#publish-pipeline)
        2. [Reindex all endpoint](#reindex-all-endpoint)
    1. [Proxying requests to Zebedee](#proxying-requests-to-zebedee)
    1. [Request filters](#request-filters)
        1. [CORS](#cors)
        1. [Rewriting URLs](#rewriting-urls)
        1. [URL shortcuts](#url-shortcuts)
        1. [URL redirects](#url-redirects)
    1. [Static Files](#static-files)
    1. [Cookies](#cookies)
    1. [Multi-lingual](#multi-lingual)

Sunset functionality is actively being decommissioned.

## Background

Babbage has limited test coverage, both for end to end and for unit testing. This document represents the start of trying to build this up to facilitate ongoing maintenance as well as helping onboarding by describing it's behaviours. By building up regression test packs and then automating, we will increase confidence in updates we do to Babbage. It is reaching end of life, but there will still be ongoing changes to the code base as dependencies are upgraded and functionality is decommissioned.

## Functionality

### Rendering HTML

One of the core pieces of functionality Babbage does is rendering content into HTML from JSON. The JSON is provided by another service - [Zebedee](https://github.com/ONSdigital/zebedee). This is primarily done by using [handlebars.java](https://github.com/jknack/handlebars.java) as well as some mapping classes inside Babbage that ensure the right handlebars templates are used for the right page types.

The rendering in Babbage is then styled and made interactive using the [Sixteens](https://github.com/ONSdigital/sixteens) library. There are no current intentions to use the [dp-design-system](https://github.com/ONSdigital/dp-design-system) or the [ONS design system](https://github.com/ONSdigital/design-system) inside Babbage. It is still important when regression testing that styling and interactivity is still working, as template level changes could impact this, for example if a class was removed.

#### Standard content pages

For standard content pages, there exists a `data.json` in Zebedee with a `type` that corresponds to the handlebars template that will render it.

List of templates:

- article
- article_download
- bulletin
- chart
- compendium_chapter
- compendium_data
- compendium_landing_page
- dataset (versions page)
- equation
- home_page_census[^1]
- list (see [Search pages](#search-pages))
- product_page
- reference_tables
- static_adhoc
- static_article
- static_foi
- static_methodology
- static_methodology_download
- static_qmi
- static_page
- table
- taxonomy_landing_page
- timeseries
- timeseries_dataset

[^1]: Given there is a now a dedicated service for census home page, it's likely this functionality should be removed.

#### Search pages

> [!NOTE]
> This functionality is currently actively being migrated out of Babbage into the [dp-frontend-search-controller](https://github.com/ONSdigital/dp-frontend-search-controller)

Babbage currently uses an instance of Elasticsearch to manage listing / search functionality. This is due to be deprecated. This provides search capabilities as a path extension.

At the root level:

- ~~/datalist~~ **DEPRECATED**
- ~~/publications~~ **DEPRECATED**
- ~~/staticlist~~ **DEPRECATED**
- ~~/timeseriestool~~ **DEPRECATED**
- ~~/topicspecificmethodology~~ **DEPRECATED**

The deprecated paths above remain in the codebase, but are no longer used in live environments. They will be removed in future work.

At the topic level (and pre-filtered for that topic):

- /topicspecificmethodology
- /publications
- /datalist
- /staticlist

Each extension provides a [pre-set different content type filter](#mapping-content-types-to-search-pages).

> [!NOTE]
> Topic level extensions can be added to any level of path, for example:
>
> - /economy/publications
> - /economy/environmentalaccounts/publications
> - /economy/governmentpublicsectorandtaxes/publicspending/publications
> - /methodology/methodologytopicsandstatisticalconcepts/admindatasources/publications

As an extension to the *bulletin* page type:

- /relateddata
- /previousReleases

Each search page type [pre-filters by content type](#mapping-content-types-to-search-pages) as well as providing different user tools for manipulating search results, for example:

- released before date filtergit
- released after data filter
- free text query filter
- content type filter

Each search page can also be *sorted* in different ways:

- release date
- title
- relevance

All user manipulable filters and sorts update the URL to allow deeplinking to a particular view.

##### RSS feeds

Each of the above search pages also provides an RSS feed to subscribe to further updates. This is accessed via adding a query param of `rss`. All of the filters available for users are also available for the RSS feed as well.

##### Mapping content types to search pages

| Babbage URL extension       | Zebedee content types                                            |
|-----------------------------|------------------------------------------------------------------|
| /datalist                   | static_adhoc, timeseries, dataset_landing_page, reference_tables |
| /publications               | article, article_download, bulletin, compenium_landing_page      |
| /staticlist                 | static_page                                                      |
| ~~/timeseriestool~~         | timeseries                                                       |
| /topicspecificmethodologies | static_qmi, static_methodology, static_methodology_download      |

#### Error pages

Babbage also provides rendered error pages for better user experience:

- 404
- 401
- 500
- 501 (renders 500 error page)

#### Navigation

Currently, Babbage requests the navigation tree to render from Zebedee. 

##### Topic API driven navigation

We were investigating moving provision of navigation to a separate microservice - [dp-topic-api](https://github.com/ONSdigital/dp-topic-api). This feature was completed and tested but has not gone live due to other work pending elsewhere. Therefore, both methods of navigation data provision exist with a feature flag to switch between them.

#### Charts

Babbage uses [Highcharts](https://www.highcharts.com/) for rendering charts. Editors have an interface in Florence to edit and preview these charts. Below is a list of variants:

- bar chart
- barline chart
- box and whisker chart
- confidence interval chart
- dual axis chart
- heat map
- line break chart
- line chart
- pie chart
- population chart
- scatter chart
- small multiples chart
- sparkline chart
- table chart

#### Tables

TODO

#### Equations

Babbage uses [Mathjax](https://www.mathjax.org/) for rendering equation markdown as SVGs. Editors have an interface in Florence to edit and preview these equations.

#### Rendering custom markdown

There are several items of custom markdown which we render in special ways.

TODO: find a list of these?

### Rendering PDFs

Babbage renders content as PDFs during the *approval* step of the publishing process. This is handled by handlebars.java and runs through similar templates with additional styles to ensure it works in PDF format.

PDFs are then stored in Zebedee's filestore and served by accessing the `/pdf` extension to a page type that generates PDFs.

### Serving non-HTML

### JSON

Babbage provides a `/data` endpoint to all of it's content URLs to allow clients to view / query the underlying data. This returns the `data.json` from the Zebedee filestore for that path and returns it to the user in JSON format.

### Resources

TODO.

### Files

TODO.

### PDF

See [Rendering PDFs](#rendering-pdfs) - these are then served by adding the extension `/pdf` to the page to retrieve the pdf file stored in the content store.

### Calculating cache times and hashes

#### Cache times

Calculating cache times in Bababge is deprecated and is actively being removed.

Babbage uses the next publish date to calculate a `Cache-Control` `max-age` header. By default it is 15 minutes, but a differential is calculated to the next publish time of that content if something is schedueld to be published. This uses the `publish_dates` Elasticsearch Index.

#### Hashes

Babbage calculates a hash of the content response to serve as an eTag header.

### Indexing search

Babbage uses ElasticSearch for it's [Search Pages](#search-pages). This is updated by either:

- publish pipeline
- reindex endpoint

#### Publish Pipeline

When a collection is published in Zebedee, a notification is sent to Babbage. Babbage then uses the collection details held in the `publish_dates` index in Elasticsearch to reindex those items by querying Zebedee's new content.

#### Reindex All Endpoint

This is triggered manually by a developer (see Operations Guide) and causes Babbage to trigger the ReindexAll endpoint in Zebedee. All content is then reindexed.

### Proxying requests to Zebedee

There are several endpoints in Babbage which just proxy requests to Zebedee:

- /export
- /file
- /generator
- /resource

### Request filters

Requests are passed through 'request filters' to manipulate them in different ways.

#### Cors

Adds CORS headers onto requests.

#### Rewriting URLs

##### URL Shortcuts

TODO

##### URL redirects

TODO

### Static Files

Allows static visualisation files to be served directly from Babbage. Will always start with `/visualisations/`

### Cookies?

TODO

### Multi-lingual

Babbage allows content to be served in two different languages:

- english / en (default)
- welsh / cy

This is set via cookie (`lang`).

Users can switch between the languages by using subdomains triggered in the menu header. If operating in Welsh, Babbage will then:

- use a `data_cy.json` if available for content population
- use welsh template text if available (`LabelsBundle_cy.properties`)
- use welsh navigation
