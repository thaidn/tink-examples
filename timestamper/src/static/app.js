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

var app = angular.module('notarizer', []);

// The AngularJS service that will communicate with the remote API.
app.service('witnessService', function($http) {
  var witnessService = {};

  witnessService.getWitnessToken = function(content) {
    return $http.post('/api/notarize', JSON.stringify({'content': content}))
        .then((response) => JSON.stringify(response.data));
  };

  witnessService.getAttestation = function(content, token) {
    try {
      const rawToken = JSON.parse(token);
      return $http
          .post(
              '/api/attest',
              JSON.stringify({'content': content, 'token': rawToken}))
          .then((response) => response.data);
    } catch (e) {
      throw new Error('The token doesn\'t look like valid JSON.');
    }
  };


  witnessService.requestRotation = function(keepOlderKeys) {
    try {
      return $http
          .post(
              '/api/rotate',
              JSON.stringify({'keepOlderKeysAsSecondary': keepOlderKeys}))
          .then((response) => response.data);
    } catch (e) {
      throw new Error('Rotation threw.');
    }
  };

  return witnessService;
});


app.controller(
    'notarizerController', function($scope, witnessService, $location) {
      $scope.requestRotation = function requestRotation(keepOlderKeys) {
        witnessService.requestRotation(keepOlderKeys);
      };

      $scope.requestWitnessing = function requestWitnessing() {
        witnessService.getWitnessToken($scope.content).then((data) => {
          this.replyToken = data;
          this.token = data;
        });
      };

      $scope.requestAcknowledgement = function requestAcknowledgement() {
        witnessService.getAttestation(this.content, this.token)
            .then(
                (data) => {
                  this.replyStatus = String(data.attested);
                  this.date = data.timestamp;
                },
                (reply) => {
                  this.replyStatus = 'error';
                  this.error = reply.data;
                });
      };

      $scope.setAnchor = function setAnchor() {
        $location.search('token', this.token);
        $location.search('content', this.content);
      };

      // Verification on load if there's an URL query.
      if ($location.search()['token']) {
        $scope.token = $location.search()['token'];
        $scope.content = $location.search()['content'];
        $scope.requestAcknowledgement();
      } else {
        this.token = '';
        this.content = '';
      }
    });
