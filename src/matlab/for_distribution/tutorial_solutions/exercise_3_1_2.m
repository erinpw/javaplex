% Exercise 3.1.2

% We use 9 vertices, which we think of as a 3x3 grid numbered as a 
% telephone keypad. We identify opposite sides, with left and right sides 
% identified with a twist. For a picture, see 
% "javaplex_tutorial_solutions.pdf".

clc; clear; close all;

% get a new ExplicitSimplexStream
stream = api.Plex4.createExplicitSimplexStream();

% add simplices
for i = 1:9
    stream.addVertex(i);
end

stream.addElement([1, 2]);
stream.addElement([2, 3]);
stream.addElement([3, 1]);
stream.addElement([7, 8]);
stream.addElement([8, 9]);
stream.addElement([9, 4]);
stream.addElement([4, 5]);
stream.addElement([5, 6]);
stream.addElement([6, 7]);

stream.addElement([1, 7]);
stream.addElement([4, 7]);
stream.addElement([4, 1]);
stream.addElement([2, 8]);
stream.addElement([8, 5]);
stream.addElement([5, 2]);
stream.addElement([3, 9]);
stream.addElement([9, 6]);
stream.addElement([6, 3]);

stream.addElement([2, 7]);
stream.addElement([3, 8]);
stream.addElement([8, 4]);
stream.addElement([1, 9]);
stream.addElement([9, 5]);
stream.addElement([5, 1]);
stream.addElement([4, 6]);
stream.addElement([6, 2]);
stream.addElement([3, 7]);

stream.addElement([1, 2, 7]);
stream.addElement([2, 7, 8]);
stream.addElement([2, 3, 8]);
stream.addElement([3, 8, 9]);
stream.addElement([1, 3, 9]);
stream.addElement([1, 4, 9]);
stream.addElement([4, 7, 8]);
stream.addElement([4, 5, 8]);
stream.addElement([5, 8, 9]);
stream.addElement([5, 6, 9]);
stream.addElement([6, 4, 9]);
stream.addElement([4, 6, 7]);

stream.addElement([1, 4, 5]);
stream.addElement([1, 2, 5]);
stream.addElement([2, 5, 6]);
stream.addElement([2, 3, 6]);
stream.addElement([3, 6, 7]);
stream.addElement([1, 3, 7]);

stream.finalizeStream();

% get homology algorithm over Z/2Z
Z2_persistence = api.Plex4.getModularSimplicialAlgorithm(3, 2);

% compute and print the intervals
Z2_intervals = Z2_persistence.computeIntervals(stream)

% get homology algorithm over Z/3Z
Z3_persistence = api.Plex4.getModularSimplicialAlgorithm(3, 3);

% compute and print the intervals
Z3_intervals = Z3_persistence.computeIntervals(stream)