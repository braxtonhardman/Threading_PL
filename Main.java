import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.Thread; 
import java.util.ArrayList;

class Main { 

    public static Scanner scanner = null; 
    public static PowerGrid grid = new PowerGrid(6, 6);
    public static int numThreads = 4; 
    public static ArrayList<Thread> threads = new ArrayList<>(); 

    public static void main(String[] args) {

           

        try { 
            scanner = new Scanner(System.in);
            // Create a vriable size grid 
            
            optionLoop(); 
        
            for(int i = 0; i < grid.getCols(); i++) { 
                for(int j = 0; j < grid.getRows(); j++){ 
                    if(grid.hasLight(i, j)) { 
                        System.out.print("X ");
                    } else { 
                        System.out.print("- ");
                    }
                }
                System.out.println("");
            }

            scanner.close();
        } catch(Exception e) {  
            System.out.println(e.getMessage()); 
        }
        
    }

    public static void setupCommunication(Thread[] threads)
    {

        /*
            Threads are split by top row and bottom row
        */
        for(int i = 0; i < threads.length; i++) { 
            // recv = new LinkedBlockingQueue<Double>( );
            // other.send = recv;
            
            // send = new LinkedBlockingQueue<Double>();
            // other.recv = send;
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
                        "5. Enter the number of threads (default is 4)");

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
                            grid.setLight(x, y);
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
                            int threadRows = 2; 
                            int threadCols = numThreads / 2;

                            // width of each chunk 
                            int chunkWidth = grid.getCols() / threadCols; 
                            int chunkHeight = grid.getRows() / threadRows; 

                            for(int row = 0; row < threadRows; row++) {
                                for(int col = 0; col < threadCols; col++) {
                                    int startX = col * chunkWidth;
                                    int startY = row * chunkHeight;

                                    System.out.println(startX + " " + startY);
                                    PowerGrid p = new PowerGrid(grid, startX, startY, chunkWidth, chunkHeight);
                                    Thread thread_obj = new Thread(p);

                                    // Copy directly while creating the thread
                                    threads.add(thread_obj);
                                }
                            }

                            break; 
                        case 5: 
                            System.out.println("Enter the number of threads: ");
                            String line = scanner.nextLine();

                    default: 
                        System.out.println("Error: Please select a value between 1 and 4");
                        break;  
                }


            }  else { 
                System.out.println("Error: Please Just Enter the Integer value of the option you want to select."); 
            }


            
    }
}
