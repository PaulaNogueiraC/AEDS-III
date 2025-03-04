import java.util.Scanner;

public class Main 
{
    public static void main(String[] args) 
    {
        Scanner sc = new Scanner(System.in);

        int option;

        do {
            System.out.println("\n-- Control Menu --");
            System.out.println("0- Exit Program");
            System.out.println("1- Import movies from CSV");
            System.out.println("2- Add a new movie");
            System.out.println("3- Modify movie details");
            System.out.println("4- Remove a movie");
            System.out.println("5- Display movie information");
            System.out.print("Select an option: ");

            option = sc.nextInt();

            switch (option) 
            {
                case 0:
                    System.out.println("Exiting program...");
                    break;
                case 1:
                    System.out.println("Importing movies...");
                    break;
                case 2:
                    System.out.println("Adding a new movie...");
                    break;
                case 3:
                    System.out.println("Modifying movie details...");
                    break;
                case 4:
                    System.out.println("Removing a movie...");
                    break;
                case 5:
                    System.out.println("Displaying movie information...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (option != 0);

        sc.close();
    }
}
