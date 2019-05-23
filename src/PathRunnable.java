import java.util.ArrayList;

public class PathRunnable implements Runnable {
	
	private Model model;
	private static int runnableIdIncrement = 0;
	private int runnableID;
	
	//maze variables
	private int[][] maze;
	private int[] startXY, endXY;
	private ArrayList<int[]> route, possiblePaths = new ArrayList<>();
	
	int[] previousPos, currentPos;
	
	public PathRunnable(Model model, ArrayList<int[]> route) {
		this.model = model;
		
		this.maze = Model.initialMaze;
		this.startXY = Model.startXY;
		this.endXY = Model.endXY;
		this.route = route;
		
		//sets an id for current thread
		runnableID = runnableIdIncrement;
		runnableIdIncrement++;
		
		if (route.isEmpty()) {
			//sets previous position and current position
			route.add(startXY);
			route.add(startXY);
		}
	}

	@Override
	public void run() {	
		boolean finish = false;
		while (!finish) {
			previousPos = route.get( route.size() - 2 );
			currentPos = route.get( route.size() - 1 );
			
			//if end point reached
			if (endXY[0] == currentPos[0] && endXY[1] == currentPos[1]) {
				solved();
				finish = true;
				break;
			}
			
			//right
			tryMove(currentPos[0] + 1, currentPos[1]);
			
			//left
			tryMove(currentPos[0] - 1, currentPos[1]);
			
			//below
			tryMove(currentPos[0], currentPos[1] + 1);
			
			//above
			tryMove(currentPos[0], currentPos[1] - 1);
			
			//if route can continue
			if (possiblePaths.size() != 0) {
				
				//create new threads if multiple routes can be taken
				for (int i = 1; i < possiblePaths.size(); i++) {
					model.forkRoute(route, possiblePaths.get(i));
					System.out.println("# " + runnableID + ": Forked");
				}
				
				//moves position of current thread
				route.add(possiblePaths.get(0));
				System.out.println("# " + runnableID + ": Moved to " + possiblePaths.get(0)[0] + ", " + possiblePaths.get(0)[1]);
				
				finish = Model.getSolved();
			
			//if route can't continue finish thread
			} else {
				System.out.println("# " + runnableID + ": Hit a dead end");
				finish = true;
				model.checkSolutionPossible();
			}
			
			possiblePaths.clear();
			
			
		}
		
		System.out.println("# " + runnableID + ": Runnable destroyed");
	}
	
	//if inputed position can be moved to add to possible routes list 
	private void tryMove(int x, int y) {
		int[] widthHeight = Model.widthHeight;
		
		//if position if out of bounds
		if (x < 0 || y < 0 || x >= widthHeight[0] || y >= widthHeight[1]) {
			return;
		}
		
		int[] newPosition = {x, y};
		//if position is valid path and had not been used yet
		if (maze[x][y] == 0 && !Model.routeContainsPosition(newPosition, route)) {
			possiblePaths.add(newPosition);	
		}
	}
	
	//tell the model a solution has been found
	private void solved() {
		model.solved(route, this);
	}
	
	public int getID() {
		return runnableID;
	}

}
