mills
=====

Algorithms to solve Three Man Morris.

The Board itself consists of three rings of 8 positions.
Each position may be void or occupied by a black or white stone.
Thus we get 3^24 = 282,429,536,481 positions at all which is about 2^38.34.
To 'solve' it we need some kind of score for each possible position.

A first attempt is to break it down into separate partitions.
A simple solution is to break it down into separate subsets of positions 
with a fixed population count of stones on the board: PopCount(#black, #white)
During the game the nuber of stones first increases up to PopCount(9,9) (opening).
Then players move around and may close mills. Each closed mill takes an opposite stone.
This decreases the PopCount again until any Player reaches n=3. At this stage it can jump around.
The final endgame is reached with PopCount(3,3) while both players can jump.
Either the winner is able to close a mill and reduce the opposite stones below 3 the game ends drawn.

To 'solve' the game each position is associated to a score telling if the player to move is able to win
or loose or if all possible moves will drawn (which is mostly the case).
The score associated is either zero, if all possible moves are drawn, or the move count 
until the player wins or looses the game definitely. Even scores indicate a won position 
while odd scores indicate a lost position.

For each PopCount(#black, #white) it must be taken into account which player will move next.
Thus there a separate score tables for black and white for each position of a given PopCount(nb,nw).
However since S(5,6,W) is equivalent S(6,5,B) the score tables to evaluate reduce to #b<=#w.
Thus with 9 stones each we only need 45 different score table instead of 100.
Since all 9 score table with n<3 are lost anyway we end up with 36 different score table to evaluate.

Score tables a calculated back to front starting with the end game of (3,3).
Based on (3,3) the situations (4,3) and (3,4) may be evaluated until (9,9).
Finally the opening has to be traced back down to (0,1).

To reduce the amount of calculations the symmetry of each position may be considered.
The empty board can be rotated (*4) and mirrored (*2) to get 8 different orientations.
In addition the outer and the inner ring may be swapped without consequences on the final result.
For (3:3) all rings may be interchanged without any consequences on the result.
This optimization is currently not realized since the complication of the algorithm is unreasonable.

One of the first tasks is to find an index function for a score table of given population count.
This index function gives an index for each possible occupation while all positions 
which may be converted into each other by a symmetry operation result in the same index.
 







   
  



