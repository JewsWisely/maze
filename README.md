# Maze
This project is a maze generator / solver. You can first generate a maze,
then click two white cells in the maze to mark them as start and end points
respectively, then click "solve" to find the unique solution. The methods 
of solving are recursively or with a stack, both of which implement DFS, or
queue, which implements BFS.

# Future Work
In the future, I can condense DFS into just one category, since it makes no
difference to the user whether it's done with stack or recursion, and
consider adding other solving methods like Dijkstra's and A*. There are also
much better ways to handle the animation and maze generation, and they should
certainly be decoupled in the future.