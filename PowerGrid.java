import java.util.concurrent.*;

public class PowerGrid implements Runnable {
    private int[][] power_grid;
    private char[][] light_grid;
    private boolean[][] source_grid; 
    private int rows, cols, totalRows, totalCols, startX, startY; 
    private boolean left, right, top, bottom; 

    private int[] ghost_left;    // values from left neighbor's right column
    private int[] ghost_right;   // values from right neighbor's left column
    private int[] ghost_top;     // values from top neighbor's bottom row
    private int[] ghost_bottom;  // values from bottom neighbor's top row

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


    public void setComm(PowerGrid left, PowerGrid right, PowerGrid top, PowerGrid bottom) { 
        // Set all the sends to recievs from other power grids.
        if(left != null) { 
            this.left = true; 
            this.recv_left = left.getSendRight(); 
        }
        if(right != null) { 
            this.right = true; 
            this.recv_right = right.getSendLeft(); 
        }
        if(top != null) { 
            this.top = true; 
            this.recv_vert = top.getSendVert();
        }
        if(bottom != null) { 
            this.bottom = true; 
            this.recv_vert = bottom.getSendVert();
        }

        // System.out.println("----SetupComm----");
        // System.out.println("Send Left: " + this.recv_left + " Send Right: " + this.recv_right + " Send Vert: " + this.recv_vert);

    }

    private void write() throws InterruptedException { 
        if(left) {
            // first column = x=0, all y
            for(int j = 0; j < rows; j++) send_left.put(power_grid[j][0]);
        }
        if(right) {
            for(int j = 0; j < rows; j++) send_right.put(power_grid[j][cols-1]);
        }
        if(top) {
            // first row = y=0, all x
            for(int i = 0; i < cols; i++) send_vert.put(power_grid[0][i]);
        }
        if(bottom) {
            for(int i = 0; i < cols; i++) send_vert.put(power_grid[rows-1][i]);
        }
    }

    private void read() throws InterruptedException { 
        if(left) {
            for(int j = 0; j < rows; j++) { 
                    
                ghost_left[j] = recv_left.take();
            }
        }
        if(right) { 

            for(int j = 0; j < rows; j++) { 
                ghost_right[j] = recv_right.take();
            }
        }
        if(top) { 

            for(int i = 0; i < cols; i++) { 
                ghost_top[i] = recv_vert.take();
            }
        }
        if(bottom) { 

            for(int i = 0; i < cols; i++) { 
                ghost_bottom[i] = recv_vert.take();
            }
        }
    }

    public LinkedBlockingQueue<Integer> getSendLeft() { 
        return this.send_left; 
    }

    public LinkedBlockingQueue<Integer> getSendRight() { 
        return this.send_right; 

    }

    public LinkedBlockingQueue<Integer> getSendVert() { 
        return this.send_vert; 

    }

    public LinkedBlockingQueue<Integer> getRecvLeft() { 
        return this.recv_left; 
    }

    public LinkedBlockingQueue<Integer> getRecvRight() { 
        return this.recv_right;
    }

    public LinkedBlockingQueue<Integer> getRecvVert() { 
        return this.recv_vert;
    }

    /*
        Constructor is for creation of threads 
    */
    public PowerGrid(PowerGrid grid, int startX, int startY, int width, int height) { 

        this.totalRows = grid.getRows(); 
        this.totalCols = grid.getCols(); 

        this.startX = startX;
        this.startY = startY; 

        this.send_left = new LinkedBlockingQueue<>();
        this.send_right = new LinkedBlockingQueue<>();
        this.send_vert = new LinkedBlockingQueue<>();
     

        // System.out.println("----Power Grid Output----");
        // System.out.println("Old Grid Size: " + grid.getCols() + "x" + grid.getRows());
        // System.out.println("New Grid Size: " + width + "x" + height);
        // System.out.println("StartX: " + startX + " StartY: " + startY); 
        this.rows = height;
        this.cols = width;

        this.ghost_left   = new int[rows];
        this.ghost_right  = new int[rows];
        this.ghost_top    = new int[cols];
        this.ghost_bottom = new int[cols];
        // System.out.println("Cols: " + this.cols + " Rows: " + this.rows);
        int[][] new_power_grid      = new int[this.rows][this.cols]; 
        char[][] new_light_grid     = new char[this.rows][this.cols];
        boolean[][] new_source_grid = new boolean[this.rows][this.cols];

        for(int i = 0; i < width; i++) { 
            for(int j = 0; j < height; j++) {
                new_power_grid[j][i]   = grid.getPower(startX + i, startY + j);
                new_light_grid[j][i]   = grid.getLight(startX + i, startY + j);
                new_source_grid[j][i]  = grid.getSourcePower(startX + i, startY + j);
            }
        }

        System.out.println("Grid constructed");

        this.power_grid  = new_power_grid; 
        this.light_grid  = new_light_grid; 
        this.source_grid = new_source_grid;

        // outputCurrentGrid();
    }   

    @Override 
    public void run() {     
        try {
            int totalRuns = totalRows * totalCols; 
            write();
            read();
            for(int run = 0; run < totalRuns; run++) { 
                // Exchange information each iteration
                write(); 
                read(); 
                
                for(int i = 0; i < getCols(); i++) {
                    for(int j = 0; j < getRows(); j++) {
                        if(!getSourcePower(i, j)) {
                            int leftVal   = (i - 1 >= 0)    ? getPower(i-1, j) : (left   ? ghost_left[j]   : 0);
                            int rightVal  = (i + 1 < cols)  ? getPower(i+1, j) : (right  ? ghost_right[j]  : 0);
                            int topVal    = (j - 1 >= 0)    ? getPower(i, j-1) : (top    ? ghost_top[i]    : 0);
                            int bottomVal = (j + 1 < rows)  ? getPower(i, j+1) : (bottom ? ghost_bottom[i] : 0);

                            int max = Math.max(Math.max(leftVal, rightVal), Math.max(topVal, bottomVal));
                            int newPower = Math.max(0, max - 1);

                            if(containsLight(i, j) && newPower > 5) { 
                                setLight(i, j, 'X');
                            }

                            setPower(i, j, newPower);
                        }
                    }
                }
            }   
        } catch(Exception e) { 
            e.printStackTrace();
        }
    } 

    public int getStartX() { 
        return this.startX;
    }

    public int getStartY() { 
        return this.startY; 
    }

    public void outputCurrentGrid() { 
        for(int j = 0; j < getRows(); j++) {        // y is outer loop
            for(int i = 0; i < getCols(); i++) {    // x is inner loop
                if(containsLight(i, j)) { 
                    if(hasLight(i, j)) { 
                        System.out.print("X ");
                    } else { 
                        System.out.print("- ");
                    }
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
        boolean[][] new_source_grid= new boolean[newRows][newCols];
        
        // Copy old data over, only up to the smaller of old/new dimensions
        for (int i = 0; i < Math.min(rows, newRows); i++) {
            for (int j = 0; j < Math.min(cols, newCols); j++) {
                new_power_grid[i][j] = power_grid[i][j];
                new_light_grid[i][j] = light_grid[i][j];
                new_source_grid[i][j]  = source_grid[i][j];
            }
        }
    
        // Replace old grids
        this.power_grid = new_power_grid;
        this.light_grid = new_light_grid;
        this.source_grid = new_source_grid;
        this.rows = newRows;
        this.cols = newCols;
    }

    public void setPower(int x, int y, int power) {
        power_grid[y][x] = power;
    }

    public int getPower(int x, int y) {
        return power_grid[y][x];
    }

    public void setSourcePower(int x, int y) { 
        source_grid[y][x] = true; 
    }

    public boolean getSourcePower(int x, int y) { 
        return this.source_grid[y][x]; 
    }

    public char getLight(int x, int y) { 
        return this.light_grid[y][x]; 
    }

    public boolean containsLight(int x, int y) { 
        return light_grid[y][x] != '\0';
    }   

    public void setLight(int x, int y, char val) {
        light_grid[y][x] = val; 
    }

    public boolean hasLight(int x, int y) {
        return light_grid[y][x] == 'X';
    }

    public int[][] getPowerGrid() { 
        return this.power_grid; 
    }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}