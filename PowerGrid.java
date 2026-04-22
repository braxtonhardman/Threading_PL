public class PowerGrid implements Runnable {
    private int[][] power_grid;
    private char[][] light_grid;
    private boolean[][] source_grid; 
    private int rows;
    private int cols;

    public PowerGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.power_grid = new int[rows][cols];
        this.light_grid = new char[rows][cols];
        this.source_grid = new boolean[rows][cols];
    }

    @Override 
    public void run() { 

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

    public void setLight(int x, int y) {
        light_grid[x][y] = 'L'; 
    }

    public boolean hasLight(int x, int y) {
        return light_grid[x][y] == 'L';
    }

    public int[][] getPowerGrid() { 
        return this.power_grid; 
    }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}