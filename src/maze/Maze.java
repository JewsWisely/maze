package maze;

/*Name: Joel Anglister

  With help from: Jack
  
  What I learned: I credit Jack for teaching me a new method on how to create the maze itself, but I implemented it slightly than he did.
  The maze creation goes like this: choose a random square, then look at all the places it can go. Pick a random direction, go there, and
  repeat the process. If you can't go anywhere, backtrack to a square that does have somewhere to go. This method results in a very squiggly
  maze, so I named it the squiggly method. I made another method using the same principles as the last one, except when it finds the next
  square it can go to, it continues in that direction for a random amount of times. This results in a more linear maze, and it reminded me
  of a circuit board, so that's what I called it.
  
  Another thing that I learned to do is solve the maze using a queue. This is a breadth first search, as opposed to depth first. In a depth
  first search, you go as far as you can in one direction and then backtrack when you reach a dead end. In the breadth first search, you
  search in every direction at the same time, and record where each square came from. Then, once you reach the end, you traverse through the
  previous squares, starting at the end, and it will bring you to the beginning. This is done using a queue; at each square, all neighbors
  are pushed onto the queue, and will therefore be processed in that order. This means that the solver can branch out, rather than be restricted
  to one direction.
  
  Comparison of recursive vs stack solve:
  The recursive method and stack method are very similar: they are both depth-first, so they solve in the same way, and they both utilize
  a stack. Recursion relies on the implicit call stack while the other method defines its own stack. The only thing that really differs is
  the way that each method loops through buttons. In the recursive one, it analyzes the next button with a recursive call to the method with new
  coordinates, while the stack one uses a while loop and else ifs.
*/
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Maze extends JPanel
{
   public static final int SIZE = 100; //actual size of maze is SIZE - 2
   public static final int DELAY = (int)(Math.sqrt(SIZE));
   
   private MyButton[][] maze;
   private JButton create, solve;
   private MyButton start, end;
   private boolean clear = false;
   
   public Maze()
   {
      setLayout(new BorderLayout());
      
      Listener listener = new Listener();
   
      JPanel center = new JPanel();
      center.setLayout(new GridLayout(SIZE, SIZE));
      
      JPanel bottom = new JPanel();
      bottom.setLayout(new GridLayout(1, 2));
      
      maze = new MyButton[SIZE][SIZE];
      create = new JButton("Create Maze");
      solve = new JButton("Solve");
      
      for(int r = 0; r < SIZE; r++)
         for(int c = 0; c < SIZE; c++)
         {
            maze[r][c] = new MyButton(r, c);
            maze[r][c].setBorderPainted(false);
            maze[r][c].setBackground(Color.BLACK);
            maze[r][c].addActionListener(listener);
            maze[r][c].setEnabled(false);
            center.add(maze[r][c]);
         }
         
      bottom.add(create);
      bottom.add(solve);
      
      create.addActionListener(listener);
      solve.addActionListener(listener);
      
      add(center, BorderLayout.CENTER);
      add(bottom, BorderLayout.SOUTH);
   }
   
   //pre: 
   //post: return whether r and c are within the bounds of the maze (there is a 1 block border of invalid squares)
   private boolean inBounds(int r, int c)
   {
      return r > 0 && c > 0 && r < SIZE - 1 && c < SIZE - 1;
   }
   
   //pre: a new maze is about to be created
   //post: completely resets the board, including color and clickability of buttons
   private void clear()
   {
      //turn every non-black square black and disable it
      for(int r = 1; r < SIZE - 1; r++)
         for(int c = 1; c < SIZE - 1; c++)
            if(maze[r][c].getBackground() != Color.BLACK)
            {
               maze[r][c].setBackground(Color.BLACK);
               maze[r][c].setEnabled(false);
            }
   }
   
   //pre: valid row, column, and color
   //post: return ArrayList of MyButtons who are neighbors to maze[r][c] and who are of the specified color
   private ArrayList<MyButton> possibleMoves(int r, int c, Color color)
   {
      ArrayList<MyButton> moves = new ArrayList<MyButton>();
      if(inBounds(r + 1, c) && maze[r + 1][c].getBackground() == color)
         moves.add(maze[r + 1][c]);
      if(inBounds(r - 1, c) && maze[r - 1][c].getBackground() == color)
         moves.add(maze[r - 1][c]);
      if(inBounds(r, c + 1) && maze[r][c + 1].getBackground() == color)
         moves.add(maze[r][c + 1]);
      if(inBounds(r, c - 1) && maze[r][c - 1].getBackground() == color)
         moves.add(maze[r][c - 1]);
      return moves;
   }
   
   //pre: board is clear and maze size is not too large (otherwise stack overflow)
   //post: creates a squiggly maze
   private void createSquiggly(int r, int c)
   {
      //if the button doesn't exist or has over one white neighbor, return
      if(!inBounds(r, c) || possibleMoves(r, c, Color.WHITE).size() > 1)
         return;
      
      //set the button white, animate
      maze[r][c].setEnabled(true);
      maze[r][c].setBackground(Color.WHITE);
      do{}while(System.currentTimeMillis() % DELAY / 4 != 0);
      maze[r][c].paintImmediately(0, 0, 100, 100);
      
      //determine all possibilities of where the next button can go, and (try to) put them there in random order 
      //since it's recursive, it will do one button and repeat the whole process for that buttpn before moving to the next button in the ArrayList
      ArrayList<MyButton> possibleMoves = possibleMoves(r, c, Color.BLACK);
      for(int random = (int)(Math.random() * possibleMoves.size()); possibleMoves.size() > 0; random = (int)(Math.random() * possibleMoves.size()))
      {
         MyButton button = possibleMoves.remove(random);
         createSquiggly(button.getRow(), button.getCol());
      }
   }
   
   //pre: board is clear
   //post: creates a straighter maze
   private void createCircuitBoard(int r, int c)
   {
      //turn first button white, animate, create new stack of buttons and boolean backtrack
      maze[r][c].setEnabled(true);
      maze[r][c].setBackground(Color.WHITE);
      maze[r][c].paintImmediately(0, 0, 100, 100);
      Stack<MyButton> stack = new Stack<MyButton>();
      boolean backtrack = false;
      
      //try to put SIZE * SIZE buttons, obviously won't be that amount but better safe than not fill up entire board
      for(int ree = 0; ree < SIZE * SIZE; ree++)
      {
         //if backtrack is true, pop buttons from the stack until you reach a button that has more than one possible move
         //this is mainly to eliminate threat of stack overflow, since the entire board will fill up regardless of what is popped
         if(backtrack)
         {
            for(int i = 0; i < stack.size(); i++)
            {
               stack.pop();
               if(stack.size() == 0)
                  return;
               r = stack.peek().getRow();
               c = stack.peek().getCol();
               if(possibleMoves(r, c, Color.BLACK).size() >= 1)
                  break;
            }
            backtrack = false;
         }
         
         ArrayList<MyButton> possibleMoves = possibleMoves(r, c, Color.BLACK);
         
         //backtrack if there are no possible moves from current button OR if stack has 100 buttons on it
         if(possibleMoves.size() == 0 || stack.size() == 100)
            backtrack = true;
         
         if(!backtrack)
         {
            //choose a random valid button, find out the cardinal direction that leads from current button to randomly chosen button
            int index = (int)(Math.random() * possibleMoves.size()), dr = 0, dc = 0;
            MyButton button = possibleMoves.get(index);
            if(inBounds(r + 1, c) && maze[r + 1][c] == button)
               dr = 1;
            else if(inBounds(r - 1, c) && maze[r - 1][c] == button)
               dr = -1;
            else if(inBounds(r, c + 1) && maze[r][c + 1] == button)
               dc = 1;
            else if(inBounds(r, c - 1) && maze[r][c - 1] == button)
               dc = -1;
            
            //go anywhere from 1 to 11 steps in given direction, keeping boundary rules and ensuring that the next space has at most 1 white neighbor
            for(int i = 0; i < (int)(Math.random() * 10 + 1); i++)
            {
               if(inBounds(r + dr, c + dc) && possibleMoves(r + dr, c + dc, Color.WHITE).size() <= 1)
               {
                  stack.push(maze[r + dr][c + dc]);
                  r += dr;
                  c += dc;
                  maze[r][c].setEnabled(true);
                  maze[r][c].setBackground(Color.WHITE);
                  do{}while(System.currentTimeMillis() % DELAY / 4 != 0);
                  maze[r][c].paintImmediately(0, 0, 100, 100);
               }
               else
               {
                  backtrack = true;
                  break;
               }
            }
         }
      }
   }
   
   //pre: maze is unsolved and a start and end have been chosen
   //post: solve the maze using recursion (depth first), return whether it was solved
   private boolean solve(int r, int c)
   {
      //turn button orange and animate
      maze[r][c].setBackground(Color.ORANGE);
      do{}while(System.currentTimeMillis() % DELAY != 0);
      maze[r][c].paintImmediately(0, 0, 100, 100);
      
      //if end is not reached, try going in every direction, ensuring boundary rules and color rules (only white squares count)
      if(maze[r][c] != end)
      {
         if(inBounds(r + 1, c) && maze[r + 1][c].getBackground() == Color.WHITE && solve(r + 1, c))
            return true;
         if(inBounds(r - 1, c) && maze[r - 1][c].getBackground() == Color.WHITE && solve(r - 1, c))
            return true;
         if(inBounds(r, c + 1) && maze[r][c + 1].getBackground() == Color.WHITE && solve(r, c + 1))
            return true;
         if(inBounds(r, c - 1) && maze[r][c - 1].getBackground() == Color.WHITE && solve(r, c - 1))
            return true;
         
         //if there is nowhere to go, turn the button red, animate, and backtrack (return false)
         else
         {
            maze[r][c].setBackground(Color.RED);
            do{}while(System.currentTimeMillis() % DELAY != 0);
            maze[r][c].paintImmediately(0, 0, 100, 100);
            return false;
         }
      }
      else
         return true;
      
   }
   
   //pre: maze in unsolved and a start and end have been chosen
   //post: solve the maze using a stack (depth first), return whether it was solved
   private boolean stackSolve(int r, int c)
   {
      Stack<MyButton> stack = new Stack<MyButton>();
      stack.push(maze[r][c]);
      while(maze[r][c] != end)
      {
         //do all of the following as long as the end has not yet been found
         //turn button orange, animate
         maze[r][c].setBackground(Color.ORANGE);
         do{}while(System.currentTimeMillis() % DELAY != 0);
         maze[r][c].paintImmediately(0, 0, 100, 100);
         
         //check if you can go in every direction. if so, push that button onto the stack and don't check any other direction
         if(inBounds(r + 1, c) && maze[r + 1][c].getBackground() == Color.WHITE)
            stack.push(maze[++r][c]);
         else if(inBounds(r, c + 1) && maze[r][c + 1].getBackground() == Color.WHITE)
            stack.push(maze[r][++c]);
         else if(inBounds(r - 1, c) && maze[r - 1][c].getBackground() == Color.WHITE)
            stack.push(maze[--r][c]);
         else if(inBounds(r, c - 1) && maze[r][c - 1].getBackground() == Color.WHITE)
            stack.push(maze[r][--c]);
            
         //if there is nowhere to go, turn button red, animate, pop from the stack
         else
         {
            maze[r][c].setBackground(Color.RED);
            do{}while(System.currentTimeMillis() % DELAY != 0);
            maze[r][c].paintImmediately(0, 0, 100, 100);
            stack.pop();
            if(stack.isEmpty())
               return false;
            r = stack.peek().getRow();
            c = stack.peek().getCol();
         }
      }
      return true;
   }
   
   //pre: maze in unsolved and a start and end have been chosen
   //post: solve the maze using a queue (breadth first), return whether it was solved
   private boolean queueSolve()
   {
      //create queue, add the start button
      Queue<MyButton> q = new LinkedList<MyButton>();
      q.add(start);
      int r, c;
      while(!q.contains(end))
      {
         //do the following while the end square is not yet on the queue
         //if no possible solution, return false
         if(q.isEmpty())
            return false;
            
         r = q.peek().getRow();
         c = q.peek().getCol();
         
         //turn button red, animate
         maze[r][c].setBackground(Color.RED);
         do{}while(System.currentTimeMillis() % DELAY != 0);
         maze[r][c].paintImmediately(0, 0, 100, 100);
         
         //add all valid neighbors to queue; they will ALL be checked. set all valid neighbors' parent to the current button
         if(inBounds(r + 1, c) && maze[r + 1][c].getBackground() == Color.WHITE)
         {
            q.add(maze[r + 1][c]);
            maze[r + 1][c].setParent(maze[r][c]);
         }
         if(inBounds(r, c + 1) && maze[r][c + 1].getBackground() == Color.WHITE)
         {
            q.add(maze[r][c + 1]);
            maze[r][c + 1].setParent(maze[r][c]);
         }
         if(inBounds(r - 1, c) && maze[r - 1][c].getBackground() == Color.WHITE)
         {
            q.add(maze[r - 1][c]);
            maze[r - 1][c].setParent(maze[r][c]);
         }
         if(inBounds(r, c - 1) && maze[r][c - 1].getBackground() == Color.WHITE)
         {
            q.add(maze[r][c - 1]);
            maze[r][c - 1].setParent(maze[r][c]);
         }
         
         //remove the current button (the one whose neighbors were just added to the queue)
         q.remove();
      }
      
      end.setBackground(Color.ORANGE);
      end.paintImmediately(0, 0, 100, 100);
      
      r = end.getRow();
      c = end.getCol();
      
      //traverse the through all parents until the start is reached, beginning with the end button
      while(maze[r][c] != start)
      {
         maze[r][c].setBackground(Color.ORANGE);
         do{}while(System.currentTimeMillis() % DELAY != 0);
         maze[r][c].paintImmediately(0, 0, 100, 100);
         int tempRow = maze[r][c].parent.getRow();
         c = maze[r][c].parent.getCol();
         r = tempRow;
      }
      return true;
   }
   
   private class Listener implements ActionListener
   {
      int clicks = 0;
      public void actionPerformed(ActionEvent e)
      {
         //create button
         //the first time the maze is created, the board doesn't need to be cleared 
         //if the size of the maze > 200, create a more linear maze. otherwise, 50/50 chance for either type of maze
         if(e.getSource() == create)
         {
            if(clear)
               clear();
            clear = true;
            if(SIZE > 200 || Math.random() < .5)
               createCircuitBoard((int)(Math.random() * (SIZE - 2) + 1), (int)(Math.random() * (SIZE - 2) + 1));
            else
               createSquiggly((int)(Math.random() * (SIZE - 2) + 1), (int)(Math.random() * (SIZE - 2) + 1));
         }
         
         //solve button
         //as long as start and end have been chosen, display UI to choose solve method
         //turn the start and end buttons white so they are recognized as valid by the solver, but don't animate so that it remains orange on the screen
         //after the maze has been solved, revert start and end to orange
         else if(e.getSource() == solve)
         {
            if(start == null || end == null)
               return;
            Object[] options = {"Recursion", "Stack", "Queue"};
            int choice = JOptionPane.showOptionDialog(null, "How do you want to solve the maze?", "Maze Solver", 0, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            start.setBackground(Color.WHITE);
            end.setBackground(Color.WHITE);
            switch(choice)
            {
               case 0:
                  solve(start.getRow(), start.getCol());
                  break;
               case 1:
                  stackSolve(start.getRow(), start.getCol());
                  break;
               case 2:
                  queueSolve();
                  break;
               default:
                  solve(start.getRow(), start.getCol());
            }
            start.setBackground(Color.ORANGE);
            end.setBackground(Color.ORANGE);     
            start = null;
            end = null;
         }
         
         //first click (set start button)
         else if(clicks == 0)
         {
            if(start != null)
               start.setBackground(Color.WHITE);
            start = (MyButton)e.getSource();
            start.setBackground(Color.ORANGE);
            clicks++;
         }
         
         //second click (set end button)
         else
         {
            if(end != null)
               end.setBackground(Color.WHITE);
            end = (MyButton)e.getSource();
            end.setBackground(Color.ORANGE);
            clicks = 0;
         }
      }
   }
   
   private class MyButton extends JButton
   {
      private int row, col;
      private MyButton parent = null; //used for queue solve
      
      public MyButton(int r, int c)
      {
         row = r;
         col = c;
      }
      
      public int getRow()
      {
         return row;
      }
      
      public int getCol()
      {
         return col;
      }
      
      public void setParent(MyButton button)
      {
         parent = button;
      }
   }
   
   public static void main(String[] args)
   {
      JFrame frame = new JFrame("Joel Anglister Maze v3.0");
      frame.setSize(750, 750);
      frame.setLocation(200, 20);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setContentPane(new Maze());
      frame.setVisible(true);
   }
}