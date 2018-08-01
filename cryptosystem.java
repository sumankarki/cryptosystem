import java.util.Scanner;


public class cryptosystem {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String input = "/";
		do{
			System.out.println("******* Cryptosystem *******");
			System.out.println();
			System.out.println("Enter '1' for key generation.");
			System.out.println("Enter '2' for encryption.");
			System.out.println("Enter '3' for decryption.");
			System.out.println("Type 'Exit' for program termination.");
			System.out.println();
			System.out.println("Enter your choice: ");
			
			Scanner scanner = new Scanner(System.in);
			input = scanner.nextLine();
			
			if(input.trim().length() > 0){
				//System.out.println("Result: "+ Helper.isPrimitiveRoot(31, 1));
				if(input.equals("1"))
					Helper.generateKeys();
				else if(input.equals("2"))
					Helper.encryptFile();
				else if(input.equals("3"))
					Helper.decryptFile();
			} else {
				System.out.println("Please enter valid input.");
				System.out.println();
			}			
		}while(!input.toLowerCase().equals("exit"));
		
		System.out.println();
		System.out.println("******* Thank you for using the program *******");
	}
}
