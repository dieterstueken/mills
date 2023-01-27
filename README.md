Algorithms to solve Nine Men's Morris.
=====

The implementation is inspired by the paper:

http://library.msri.org/books/Book29/files/gasser.pdf

This is a try to realize the sketched algorithms using Java. 

The board itself consists of three rings of 8 positions.
Each position may be void or occupied by a black or white stone.
Thus we get 3^24 = 282,429,536,481 positions at all which is about 2^38.34.
To 'solve' it we need some kind of score for each possible position.

A first attempt is to break it down into separate partitions.
A simple solution is to break it down into separate subsets of positions 
with a fixed population count of stones on the board: PopCount(#black, #white)
During the game the number of stones first increases up to PopCount(9,9) (opening).
Then players move around and may close mills. Each closed mill takes an opposite stone.
This decreases the PopCount again until any Player reaches n=3. At this stage it can jump around.
The final endgame is reached with PopCount(3,3) while both players can jump.
Either the winner is able to close a mill and reduce the opposite stones below 3 the game ends drawn.

To 'solve' the game each position is associated to a score telling if the player to move is able to win
or loose or if all possible moves will be drawn (which is mostly the case).
The score associated is either zero, if all possible moves are drawn, or the move count 
until the player wins or looses the game definitely. Even scores indicate a won position 
while odd scores indicate a lost position.

For each PopCount(#black, #white) it must be taken into account which player will move next.
Thus, there a separate score tables for black and white for each position of a given PopCount(nb,nw).
However, since S(5,6,W) is equivalent S(6,5,B) the score tables to evaluate reduce to #b<=#w.
Thus, with 9 stones each we only need 45 different score table instead of 100.
Since all 9 score table with n<3 are lost anyway we end up with 36 different score table to evaluate.

Score tables a calculated back to front starting with the end game of (3,3).
Based on (3,3) the situations (4,3) and (3,4) may be evaluated until (9,9).
Finally, the opening has to be traced back down to (0,1).

To reduce the amount of calculations the symmetry of each position may be considered.
The empty board can be rotated (*4) and mirrored (*2) to get 8 different orientations.
In addition, the outer and the inner ring may be swapped without consequences on the final result.
For (3:3) all rings may be interchanged without any consequences on the result.
This optimization is currently not realized since the complication of the algorithm is unreasonable.

One of the first tasks is to find an index function.
This index will assign a unique index to each possible position.
It must also be taken into account, that up to eight positions may be equivalent due to symmetry operations.

The whole universe counts 8947989348 different positions. Unfortunately this is even more than 2^33.
Thus, a unique position cannot be represented by a 32-bit integer.

A solution is to separate the problem into different levels. Each level is represented by its occupation count.
With nine stones each we get 100 possible occupations 
([PopCount](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/PopCount.java)) 
up to (9,9) inclusive. 

Class PopCount is an example of a set of precalculated instances
([Popcount.TABLE](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/PopCount.java#L237)).
For 0<=n<=9 a [
PopCount.index(nb, nw)](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/PopCount.java#L44) 
can be calculated to access precalculated instances of a PopCount object.

Thus, a PopCount object is equivalent to an integer. Both can be converted into each other without generating additional objects.
Any object representable by an integer may implement the interface 
[Indexed](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/util/Indexed.java).

This interface allows compacted representations as List, Map or Set, Indexed Objects are inherently comparable.
There is a special class
[ListSet](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/util/ListSet.java) 
which represents an ordered List of elements also as an SortedSet.

### Boards and Rings

The board consists of three rings of eight positions each. 
To break down the situation further a representation of a single ring seems helpful.
Each position is addressed by a 
[Sector](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Sector.java).
Each Serctor may be occupied by a stone owned by a 
[Player](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Player.java)
of Black(1) or White(2) while
Void(0) positions are occupied by 
[Player.None](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Player.java#L18).

The occupied positions of a ring are represented by a bitmask of eight bits for each 
[Sector](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Sector.java).
So we get 256 different occupation 
[Pattern](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Pattern.java) 
represented by a List of [Pattern.PATTERNS](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Pattern.java#L204).

Sectors are grouped into radial sectors (0-4) and positions on the edges (4-7).
This groups the edge sectors onto the lower four bits of a Pattern and may be clipped using `&=0x0f`.
This simplifies the handling of radial mills later on.

As we have two 
[Player.None](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Player.java#L18)
a Ring is represented by two 
[Patterns](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Patterns.java)
for black and white each.
Assuming there are no duplicate occupations on each Sector, we end up with 3^8=6561  different
[Patterns](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Patterns.java).

A Pattern is extended to a class
[RingEntry](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/ring/RingEntry.java) 
which carries a lot of additional precalculated values. All possible instances of 
[RingEntry](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/ring/RingEntry.java) 
form a List of 
[Entries.TABLE](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/ring/Entries.java#L22).
This is one of the most important objects.

Each RingEntry may be transformed into an equivalent position by performing one of eight symmetry operations.
A symmetry permutation is represented by the
[enum Perm](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/bits/Perm.java).

Each RingEntry has a array of eight related entries for each permutation.
To brear cyclic references this array is represented by short indexes 
which are converted into RingEntry objects on runtime after Entries.TABLE becomes available.
This is also the reason to place static Entries.TABLE into a separate class
to avoid deadlocks during class loading.

Important information is how one of the eight permutations affect the RingEntry.
Especially if the entry (resp. its index) can be reduced by any of the eight permutations.  

This is represented by a mask of bits (represented by a byte) like 
[RingEntry.mlt](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/ring/RingEntry.java#L37).

Several other mask are representing other questions, i.e. if the index stays stable
 ([RingEntry.meq](https://github.com/dieterstueken/mills/blob/master/core/src/main/java/mills/ring/RingEntry.java#L34)). 

line 1
line 2
line 3
