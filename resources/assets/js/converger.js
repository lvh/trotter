var app = angular.module('main', ['ui.bootstrap']);

function ConvergerCtrl(scope, q, http) {
  window.theControllerScope = scope;
  scope.showConvergeOutput = false;
  scope.groups = {};
  scope.nodes = [];

  http.get('/api/starting-configuration')
    .success(function(data) {
      scope.desiredConfiguration = data;
    });

  var getNodes = function() {
    http.get('/api/nodes')
      .success(function(data) {
	validNodes = _.filter(data, function(node) {
	  return (node['group-name'] != null && node['running?'] == true);
	});
	scope.nodes = validNodes;

	var newgroups = _.chain(scope.desiredConfiguration)
	    .keys()
	    .map(function(k) {
	      var wrap = {};
	      wrap[k] = [];
	      return wrap;
	    })
	    .reduce(function(memo,item) {
	      return _.extend(memo, item);
	    }, {})
	    .value();

	_.each(validNodes, function(node) {
	  newgroups[node['group-name']].push(node);
	});

	scope.groups = newgroups;
      });
  };

  scope.toggle = function(phase) {
    phase.revealDetails = ! phase.revealDetails;
  }

  scope.converge = function() {
    scope.showConvergeOutput = false;
    var payload = {
      desired: scope.desiredConfiguration,
      phases: ["install"]
    };

    http.put('/api/configuration', payload)
      .success(function (data) {
	scope.convergeOutput =  data;
	scope.showConvergeOutput = true;
	console.log(data);
      });
  };

  getNodes();
  setInterval(getNodes, 5000);
}

app.controller('ConvergerCtrl', ['$scope','$q', '$http', ConvergerCtrl]);
