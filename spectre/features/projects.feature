Feature: View projects
  Background:
    Given there are projects with tests
    When we visit the projects page

  Scenario: showing the projects
    Then we should see the projects
