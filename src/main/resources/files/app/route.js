//Route configuration
(function() {

	'use strict';


	angular.module('onsApp')
		.config(['$routeProvider', '$locationProvider', '$httpProvider', RotueConfigration])
		.factory('OnsHttpInterceptor', ['$q', '$location', OnsHttpInterceptor])


	function RotueConfigration($routeProvider, $locationProvider, $httpProvider) {

		$routeProvider.
		when('/about', {
			templateUrl: 'app/templates/about/about.html',
			controller: "AboutCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('About')
				}]
			}
		}).
		when('/accessibility', {
			templateUrl: 'app/templates/accessibility/accessibility.html',
			controller: "AccessibilityCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Accessibility')
				}]
			}
		}).
		when('/alpha', {
			templateUrl: 'app/templates/alphapage/alphapage.html',
			controller: "AlphaPageCtlr",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Protoype')
				}]
			}
		}).
		when('/calendar', {
			templateUrl: 'app/templates/calendar/calendar.html',
			controller: "CalendarCtlr",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Release Calendar')
				}]
			}
		}).
		when(':collectionPath*\/collection', {
			templateUrl: 'app/templates/collection/collection.html',
			controller: "CollectionCtrl",
			controllerAs: "collection",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Collections')
				}]
			}
		}).
		when('/contactus', {
			templateUrl: 'app/templates/contact/contactus.html',
			controller: "ContactUsCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Contact Us')
				}]
			}
		}).
		when('/copyright', {
			templateUrl: 'app/templates/copyright/copyright.html',
			controller: "CopyrightCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Copyright')
				}]
			}
		}).
		when('/dataversions', {
			templateUrl: 'app/templates/dataversions/dataversions.html',
			controller: "DataversionsCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Data Versions')
				}]
			}
		}).
		when('/localstats', {
			templateUrl: 'app/templates/localstats/localstats.html',
			controller: "LocalStatsCtlr",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Local Statistics')
				}]
			}
		}).
		when('/methodology', {
			templateUrl: 'app/templates/methodology/methodology.html',
			controller: "MethodologyCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Methodology')
				}]
			}
		}).
		when('/nationalstats', {
			templateUrl: 'app/templates/nationalstats/nationalstats.html',
			controller: "NationalStatsCtlr",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('National Statistics')
				}]
			}
		}).
		when('/previous', {
			templateUrl: 'app/templates/previoustatic/previoustatic.html',
			controller: "PreviousCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Previs Versions')
				}]
			}
		}).
		when('/privacy', {
			templateUrl: 'app/templates/privacy/privacy.html',
			controller: "PrivacyCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Privacy')
				}]
			}
		}).
		when('/release', {
			templateUrl: 'app/templates/release/release.html',
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Release')
				}]
			}
		}).
		when('/search', {
			resolve: {
				searchResponse: ['PageUtil', 'DataLoader', search],
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Search Results')
				}]
			},
			templateUrl: 'app/templates/search-results/search-results.html',
			controller: 'SearchController',
		}).
		when('/survey', {
			templateUrl: 'app/templates/survey/survey.html',
			controller: "SurveyCtrl",
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('Survey')
				}]
			}
		}).
		when('/404', {
			templateUrl: '/app/templates/error-pages/error404.html',
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('404')
				}]
			}
		}).
		when('/500', {
			templateUrl: '/app/templates/error-pages/error500.html',
			resolve: {
				title: ['PageUtil', function(PageUtil) {
					PageUtil.setTitle('500')
				}]
			}
		}).
		otherwise(resolveTaxonomyTemplate())

		function resolveTaxonomyTemplate() {
			var routeConfig = {
				resolve: {
					data: ['Taxonomy', 'PageUtil',
						function(Taxonomy, PageUtil) {
							var promise = Taxonomy.loadData()
							promise.then(function(data) {
								PageUtil.setTitle(data.name)
							})
							return promise
						}
					]
				},
				templateUrl: 'app/templates/taxonomy/taxonomy.html',
				controller: 'TaxonomyController',
				controllerAs: 'taxonomy'


			}

			return routeConfig
		}

		function search(PageUtil, DataLoader, $log) {
			var q = PageUtil.getUrlParam('q')
				// var type = PageUtil.getUrlParam('type')
				// var pageNumber = PageUtil.getUrlParam('page')
				// var searchString = "?q=" + q + getTypeString(type) + (pageNumber ? "&page=" + pageNumber : "")
			var searchString = PageUtil.getUrl()
			return DataLoader.load("/search" + searchString)
				.then(function(data) {
					//If cdid search is made go directly to timeseries page for searched cdid
					if (data.type && data.type === 'timeseries') {
						PageUtil.goToPage(data.uri, true)
						return
					}
					return data
				}, function() {
					$log.error('Failed loading search results')
				})
		}

		function setTitle(title, PageUtil) {
			PageUtil.setTitle(title)
		}

		$httpProvider.responseInterceptors.push('OnsHttpInterceptor')

	}

	function OnsHttpInterceptor($q, $location) {
		return function(promise) {
			// pass success (e.g. response.status === 200) through
			return promise.then(function(response) {
					return response
				},
				// otherwise deal with any error scenarios
				function(response) {
					if (response.status === 500) {
						$location.url('/500')
					}
					if (response.status === 404) {
						$location.url('/404')
					}
					return $q.reject(response)
				});
		};
	}


})()