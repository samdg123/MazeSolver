import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Model {
	
	//declaring maze variables
	public static int[] widthHeight, startXY, endXY;
	public static int[][] initialMaze;
	
	//thread pool for the paths to run in 
	private ThreadPoolExecutor eService;
	private ArrayList<PathRunnable> runnableList = new ArrayList<>();
	private static boolean solved = false;
	
	public Model(File file) {
		try {
			setupMaze(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*number of threads the pool is set to, low number seems to work fine with sample mazes,
		but a higher number seems to produce a more efficient route*/ 
		final int numThreads = 10;
		
		//initialises thread pool and adds initial path thread
		eService = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
		
		PathRunnable firstRunnable = new PathRunnable(this, new ArrayList<>());
		eService.submit(firstRunnable);
		runnableList.add(firstRunnable);
	}
	
	//if more than on route is available start a new thread
	public void forkRoute(ArrayList<int[]> route, int[] nextPosition) {
		if (solved) {
			return;
		}
		
		ArrayList<int[]> forkedRoute = new ArrayList<>(route);
		forkedRoute.add(nextPosition);
		
		PathRunnable pathRunnable = new PathRunnable(this, forkedRoute);
		eService.submit(pathRunnable);
		runnableList.add(pathRunnable);
	}
	
	//if puzzle gets solved
	public synchronized void solved(ArrayList<int[]> route, PathRunnable solvedRunnable) {
		//stops other threads and forks
		eService.shutdownNow();
		solved = true;
		
		/*/gives other threads time to finish
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
			
		System.out.println("# " + solvedRunnable.getID() + ": Solved the maze!");
		displaySolvedMaze(route);
		
	}
	
	//prints solution to console
	private void displaySolvedMaze(ArrayList<int[]> route) {
		addGapInConsole();
		
		//Iterates through each coordinate of maze
		for (int y = 0; y < widthHeight[1]; y++) {
			for (int x = 0; x < widthHeight[0]; x++) {
				
				//if position is wall
				if (initialMaze[x] [y] == 1) {
					System.out.print("#");
					
					//if position is part of the route
				} else if (routeContainsPosition(new int[]{x, y}, route)) {
					
					//if pos is start point
					if (x == startXY[0] && y == startXY[1]) {
						System.out.print("S");
						
					//if pos is end point
					} else if (x == endXY[0] && y == endXY[1]) {
						System.out.print("E");
						
					//if pos is neither
					} else {
						System.out.print("X");						
					}
				
				//if pos is valid path, but not used
				} else {
					System.out.print(" ");
				}
			}
			System.out.println();
		}	
	}

	//adds gap of 10 blank lines
	private void addGapInConsole() {
		for (int i = 0; i < 10; i++) {
			System.out.println("");
		}
	}
	
	//returns true if position is part of route
	static boolean routeContainsPosition(int[] position, ArrayList<int[]> route) {
		for (int[] coordinate : route) {
			if (coordinate[0] == position[0] && coordinate[1] == position[1]) {
				return true;
			}
		}
		return false;
	}
	
	//useful method to display if the maze has been parsed properly
	private void displayInitialMaze() {
		for (int y = 0; y < widthHeight[1]; y++) {
			for (int x = 0; x < widthHeight[0]; x++) {
				System.out.print(initialMaze[x] [y]);
			}
			System.out.println();
		}
	}
	
	//if all threads unsuccessful 
	public void checkSolutionPossible() {
			
		if (!solved && eService.getActiveCount() == 1) {
			addGapInConsole();
			System.out.println("No solution found");
		}
	}
	
	//sets maze variables from text file
	private void setupMaze(File file) throws IOException {
		//reading maze file
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		widthHeight = setIntsFromString(br.readLine());
		startXY = setIntsFromString(br.readLine());
		endXY = setIntsFromString(br.readLine());
		
		//initialises and declares maze array and iterator variables
		initialMaze = new int[widthHeight[0]] [widthHeight[1]];
		int currentY = 0;
		String mazeLine;

		//parses maze file to 2D int array
		while ((mazeLine = br.readLine()) != null) {
			//removes spaces
			mazeLine = mazeLine.replace(" ", "");
			
			int currentX = 0;
			//for each letter in row add to maze array as int
			for (char c : mazeLine.toCharArray()) {
				initialMaze[currentX] [currentY] = Character.getNumericValue(c);
				
				currentX++;
			}	
			currentY++;
		}
		
		br.close();
	}
	
	//sets 1D int array from string containing two numbers 
	private int[] setIntsFromString(String string) {
		int[] ints = new int[2];
		int spaceIndex = string.indexOf(" ");	
		
		ints[0] = Integer.parseInt(string.substring(0, spaceIndex));
		ints[1] = Integer.parseInt(string.substring(spaceIndex + 1));
			
		return ints;
	}
	
	public static boolean getSolved() {
		return solved;
	}

}
