import java.util.Scanner;
import java.lang.Thread; 
import java.util.ArrayList;

class Main { 

    public static Scanner scanner = null; 
    public static PowerGrid grid = new PowerGrid(20, 20);
    public static int numThreads = 4; 
    public static PowerGrid[][] thread_objects = null;
    public static ArrayList<Thread> threads = new ArrayList<>(); 

    public static void main(String[] args) {

           

        try { 
            scanner = new Scanner(System.in);
            // Create a vriable size grid 
            
            optionLoop();  

            scanner.close();
        } catch(Exception e) {  
            System.out.println(e.getMessage()); 
        }
        
    }
   
    public static void optionLoop() { 
            String s[]; 
            String input; 
            int x, y, power; 
            System.out.print("1. Add power source\n" + //
                        "2. Add light\n" + //
                        "3. Set x/y size (default is 6/6)\n" + //
                        "4. Run the program\n" + //
                        "5. Enter the number of threads (default is 4)\n");

            input = scanner.nextLine();

            if(input.matches("\\d+")) { 
                int selection = Integer.valueOf(input);

                switch(selection) { 
                    case 1: // Add Power Source
                        System.out.println("Enter X Y and Power:");
                        s = scanner.nextLine().split(" ");

                        if(s.length != 3) { 
                            System.out.println("Error: Expected 3 argumetns for X Y and Power");
                        } else { 
                            x = Integer.valueOf(s[0]);
                            y = Integer.valueOf(s[1]);
                            power = Integer.valueOf(s[2]);

                            // Set position in grid. 
                            grid.setPower(x, y, power); 
                            grid.setSourcePower(x, y);
                            optionLoop();
                        }
                        break;
                    case 2: // Add Light 
                        System.out.println("Enter X Y:");
                        s = scanner.nextLine().split(" ");
                        if(s.length != 2) { 
                            System.out.println("Error: Expected 2 arguments for X Y");
                        } else { 
                            x = Integer.valueOf(s[0]);
                            y = Integer.valueOf(s[1]);
                            grid.setLight(x, y, '-');
                        }

                        optionLoop();
                        break;
                    case 3: // Resize the grid 
                        System.out.println("Enter X Y: "); 
                        s = scanner.nextLine().split(" "); 
                        if(s.length != 2) { 
                            System.out.println("Error: Expected 2 arguments for X Y"); 
                        } else { 
                            x = Integer.valueOf(s[0]);
                            y = Integer.valueOf(s[1]);
                            grid.resize(x, y); 
                        }

                        optionLoop();
                        break;
                        case 4:

                            
                            // Divide into subgrids for each thread. 
                            // System.out.println("----Main Subgrid Seperation----");

                            int threadCols = numThreads / 2; 
                            int chunkWidth = grid.getCols() / threadCols; 
                            int chunkHeight = grid.getRows() / threadCols; 
                            int extraWidth = grid.getCols() % threadCols; 
                            int extraHeight = grid.getRows() % threadCols; 
                            // System.out.println("Thread Cols: " + threadCols);

                            thread_objects = new PowerGrid[2][threadCols];

                            // Initalizes subgrids for each thread storing in threads arraylist 
                            for(int row = 0; row < 2; row++) {
                                for(int col = 0; col < threadCols; col++) {
                                    int startX = col * chunkWidth;
                                    int startY = row * chunkWidth;

                                    // For odd number grids append to the last grid of each row 
                                    // System.out.println("Grid: " + row + " " + col);
                                    PowerGrid p = null;
                                    if(row == 1) { 
                                        if(extraHeight != 0  && col == (threadCols - 1)) { 
                                            p = new PowerGrid(grid, startX, startY, chunkWidth + extraWidth, chunkHeight + extraHeight);
                                        } else if (extraHeight != 0) { 
                                            p = new PowerGrid(grid, startX, startY, chunkWidth, chunkHeight + extraHeight); 
                                        } else { 
                                            p = new PowerGrid(grid, startX, startY, chunkWidth, chunkHeight); 
                                        }
                                    } else { 
                                        // row = 0 -> can only be the last end extend width
                                        if(extraWidth != 0 && col == (threadCols - 1)) { 
                                            p = new PowerGrid(grid, startX, startY, chunkWidth + extraWidth, chunkHeight);
                                        } else { 
                                            p = new PowerGrid(grid, startX, startY, chunkWidth, chunkHeight);
                                        }
                                    }
                                    
                                    if(p != null) { 
                                        thread_objects[row][col] = p;
                                    } else { 
                                        System.out.println("Error: Thread was undefined when creating subgrids");
                                        System.exit(0);
                                    }

                                    
                                }
                            }   

                            /*
                                Set up communication between objects
                                is in nice 2d array so know the position of each object
                            */ 
                            for(int row = 0; row < 2; row++) { 
                                for(int col = 0; col < threadCols; col++) { 
                                    // For each position check left right top bottom 
                                    PowerGrid left, right, top, bottom; 
                                    // Top bottom check 
                                    if(row == 0) { 
                                        top = null; 
                                        bottom = thread_objects[row + 1][col];  
                                    } else { 
                                        bottom = null;
                                        top = thread_objects[row - 1][col];
                                    }

                                    // Left Right Check 
                                    if(col == 0) { 
                                        left = null;
                                        right = thread_objects[row][col +1];
                                    } else if(col == threadCols -1) { 
                                        right = null; 
                                        left = thread_objects[row][col - 1];
                                    } else { 
                                        // Has all 3 
                                        left = thread_objects[row][col - 1];
                                        right = thread_objects[row][col + 1]; 
                                    }


                                    PowerGrid p = thread_objects[row][col];
                                    p.setComm(left, right, top, bottom);
                                    
                                }
                            }

                            /*
                                Add Threads to an arrayList
                            */
                            for(int row = 0; row < 2; row++) { 
                                for(int col = 0; col < threadCols; col++) { 
                                    Thread temp = new Thread(thread_objects[row][col]);
                                    threads.add(temp); 
                                }
                            }

                            /*
                                Start all threads
                            */
                            System.out.println("Starting threads...");
                            for(Thread t : threads) {
                                t.start();
                            }

                            /*
                                Wait tell all threads are finished 
                            */
                            for(Thread t : threads) {
                                try { 
                                    t.join();
                                } catch (Exception e) { 
                                    e.printStackTrace();
                                    System.exit(0);
                                }
                            }

                            /*
                                Combine all grids backinto one and print
                            */  
                            for(int row = 0; row < 2; row++) {
                                for(int col = 0; col < threadCols; col++) {
                                    PowerGrid chunk = thread_objects[row][col];
                                    int startX = chunk.getStartX();
                                    int startY = chunk.getStartY();

                                    for(int i = 0; i < chunk.getCols(); i++) {
                                        for(int j = 0; j < chunk.getRows(); j++) {
                                            grid.setPower(startX + i, startY + j, chunk.getPower(i, j));
                                            
                                            if(chunk.containsLight(i, j)) { 
                                                if(chunk.hasLight(i, j)) {
                                                    grid.setLight(startX + i, startY + j, 'X');
                                                } else { 
                                                    grid.setLight(startX + i, startY + j, '-');
                                                }
                                            }
                                          
                                        }
                                    }
                                }
                            }

                            grid.outputCurrentGrid();
                            break; 
                        case 5: 
                            System.out.println("Enter the number of threads: ");
                            String line = scanner.nextLine();
                            try { 
                                numThreads = Integer.valueOf(line);
                            } catch (Exception e) { 
                                e.printStackTrace();
                                System.exit(0);
                            }
                            optionLoop();
                            break;

                    default: 
                        System.out.println("Error: Please select a value between 1 and 4");
                        break;  
                }


            }  else { 
                System.out.println("Error: Please Just Enter the Integer value of the option you want to select."); 
            }


            
    }
}
