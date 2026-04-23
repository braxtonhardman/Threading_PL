import java.util.concurrent.*;

public class PowerGrid implements Runnable {
    private int[][] power_grid;
    private char[][] light_grid;
    private boolean[][] source_grid; 
    private int rows;
    private int cols;

    private LinkedBlockingQueue<Integer> recv_left = null;
    private LinkedBlockingQueue<Integer> recv_right = null;
    private LinkedBlockingQueue<Integer> recv_vert = null;

    private LinkedBlockingQueue<Integer> send_left = null;
    private LinkedBlockingQueue<Integer> send_right = null;
    private LinkedBlockingQueue<Integer> send_vert = null;

    public PowerGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.power_grid = new int[rows][cols];
        this.light_grid = new char[rows][cols];
        this.source_grid = new boolean[rows][cols];
    }

    /*
        Constructor is for creation of threads 
    */
    public PowerGrid(PowerGrid grid, int startX, int startY, int width, int height) { 
        int x = grid.getCols() - startX; 
        int y = grid.getRows() - startY; 
        int[][] new_power_grid = new int[x][y]; 
        char[][] new_light_grid = new char[x][y];
        boolean[][] new_source_grid = new boolean[x][y];
        for(int i = startX; i < grid.getCols(); i++) { 
            for(int j = startY; j < grid.getRows(); j++) { 
                new_power_grid[i][j] = grid.getPower(i, j);
                new_light_grid[i][j] = grid.getLight(i, j);
                new_source_grid[i][j] = grid.getSourcePower(i, j);
            }
        }

        this.power_grid = new_power_grid; 
        this.light_grid = new_light_grid; 
        this.source_grid = new_source_grid; 
    }   

    @Override 
    public void run() {     

        boolean changed = true;
        while(changed) {
            changed = false;
            for(int i = 0; i < getCols(); i++) {
                for(int j = 0; j < getRows(); j++) {
                    if(!getSourcePower(i, j)) {
                        int left   = (i - 1 >= 0)         ? getPower(i-1, j) : 0;
                        int right  = (i + 1 < getCols())  ? getPower(i+1, j) : 0;
                        int top    = (j - 1 >= 0)         ? getPower(i, j-1) : 0;
                        int bottom = (j + 1 < getRows())  ? getPower(i, j+1) : 0;
    
                        int max = Math.max(Math.max(left, right), Math.max(top, bottom));
                        int newPower = Math.max(0, max - 1); // floor at 0
                        
                        // Check if position is light
                        if(hasLight(i, j)) { 
                            if(newPower > 5) { 
                                setLight(i, j);
                            }
                        }

                        if(newPower != getPower(i, j)) {
                            setPower(i, j, newPower);
                            changed = true;
                        }

                        
                    }
                }
            }
        }


        outputCurrentGrid(); 
    }   

    public void outputCurrentGrid() { 
        // Outputs the current grid 
        for(int i = 0; i < getCols(); i++) { 
            for(int j = 0; j < getRows(); j++){ 
                if(hasLight(i, j)) { 
                    System.out.print("X ");
                } else { 
                    System.out.print(getPower(i, j) + " ");
                }
            }
            System.out.println("");
        }
    }
    public void resize(int newRows, int newCols) {
        int[][] new_power_grid = new int[newRows][newCols];
        char[][] new_light_grid = new char[newRows][newCols];
    
        // Copy old data over, only up to the smaller of old/new dimensions
        for (int i = 0; i < Math.min(rows, newRows); i++) {
            for (int j = 0; j < Math.min(cols, newCols); j++) {
                new_power_grid[i][j] = power_grid[i][j];
                new_light_grid[i][j] = light_grid[i][j];
            }
        }
    
        // Replace old grids
        this.power_grid = new_power_grid;
        this.light_grid = new_light_grid;
        this.rows = newRows;
        this.cols = newCols;
    }

    public void setPower(int x, int y, int power) {
        power_grid[x][y] = power;
    }

    public int getPower(int x, int y) {
        return power_grid[x][y];
    }

    public void setSourcePower(int x, int y) { 
        source_grid[x][y] = true; 
    }

    public boolean getSourcePower(int x, int y) { 
        return this.source_grid[x][y]; 
    }

    public char getLight(int x, int y) { 
        return this.light_grid[x][y]; 
    }

    public void setLight(int x, int y) {
        light_grid[x][y] = 'X'; 
    }

    public boolean hasLight(int x, int y) {
        return light_grid[x][y] == 'X';
    }

    public int[][] getPowerGrid() { 
        return this.power_grid; 
    }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}