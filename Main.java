import java.util.Scanner;

class Main { 

    public static Scanner scanner = null; 
    public static PowerGrid grid = new PowerGrid(6, 6);

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
                        "");

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
                    case 4: // Alrogithim 
                        int max_size = grid.getCols() * grid.getRows(); 

                        for(int i = 0; i < max_size; i++) { 
                        
                            
                        }
                        
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
