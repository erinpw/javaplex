% This script shows an Euler characteristic example - Section 7.1
clc; clear; close all;

% get a new ExplicitSimplexStream
stream = api.Plex4.createExplicitSimplexStream();

dimension = 9;

% construct simplicial sphere
stream.addElement(0:(dimension + 1));
stream.ensureAllFaces();
stream.removeElementIfPresent(0:(dimension + 1));
stream.finalizeStream();

% get the default persistence computation over Z/2Z
persistence = api.Plex4.getModularSimplicialAlgorithm(dimension + 1, 2);

% compute and print the intervals
n_sphere_intervals = persistence.computeAnnotatedIntervals(stream)