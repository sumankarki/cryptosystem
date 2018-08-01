import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.Random;

public class Helper {

	//Miller-Rabin primality testing, Algorithm used from Wikipedia
	private static boolean MillerRabin(long n){
		
		if(n==0 || n==1) { // 0 or 1, then not prime
			return false;
		} else if(n == 2) { // 2 is prime
			return true;
		} else if(n%2 == 0) { //if even then not prime
			return false;
		} else {
			//Writing (n-1) = 2^s * d, so computing value of s and d
			long temp = n - 1;
			int counter = 0;
			while(temp % 2 == 0){
				counter++;
				temp = temp/2;
			}
			long s = counter;
			long d = temp;
			
			for(int i=0; i<3; i++){				
				//Picking 'a' randomly in range (2, n-2)
				Random rand = new Random();
				long a = Math.abs(rand.nextInt()) % (n-2) + 1;
				long x = modCalc(a, d, n);
				if(x == 1 || x == n-1)
					continue;
				else {
					int j=1;
					while(j<=s-1 && x!=n-1){
						x = modCalc(x, 2, n);
						if(x == 1){
							return false;
						}
						j++;
					}
					if(x!=n-1)
						return false;
				}
			}
			return true;
		}
	}
	
	//Calculate mod operation for x^y mod n, it is for big numbers
	private static int modCalc(long x, long y, long n){
		return BigInteger.valueOf(x).modPow(BigInteger.valueOf(y), BigInteger.valueOf(n)).intValue();
	}
	
	//Check if the given number is primitive root of prime, Used algorithm from https://e-maxx-eng.appspot.com/algebra/primitive-root.html
	public static boolean isPrimitiveRoot(long prime, long a){
		if(gcd(a, prime) != 1)
			return false;
		
		long phi = prime - 1; //Euler totient for prime is, phi(p) = p-1
		
		//For all prime factors (q) from 1 to phi, if a^(phi/q) mod p != 1, then a is primitive root, otherwise not
		//First check for 2, and then we can increase the counter by 2
		System.out.println("phi/2: "+phi/2);
		int result = modCalc(a, (phi/2), prime);
		if(result==1)
			return false;
		
		for(int q=3; q<phi/2; q+=2){
			//Only if q is prime and divides phi
			if(MillerRabin(q) && phi % q == 0){
				result = modCalc(a, (phi/q), prime);
				if(result == 1){
					return false;
				}
			}
		}
		return true;
	}
	
	//Computes greatest common divisor of given two long values
	public static long gcd(long a, long b){
		if(a == 0)
			return b;
		if(b == 0)
			return a;
		return gcd(b, a%b);
	}
	
	//Generates keys in current location
	public static void generateKeys(){
		System.out.println();
		System.out.println("******* Generating Keys *******");
		
		long prime = 0;
		int tempPrime = 0;
		
		//Test tempPrime%12 gives five and computed prime is actually prime
		boolean loop = true;
		while(loop){
			tempPrime = randomPrime();
			//Check if testPrime is prime using Miller-Rabin
			if(MillerRabin(tempPrime)) {
				if(tempPrime % 12 == 5){
					prime = Math.abs(2*tempPrime + 1);
					//Check if prime is prime using Miller-Rabin
					if(MillerRabin(prime)) {
						loop = false;
					}					
				}
			}
		}
		
		//Select primitive root i.e., secret key, d, such that 1 <= d <= p-2
		Random rand = new Random();
		long d = Math.abs(rand.nextInt()) % (prime - 2);
		d = (d%2 == 0)? d+1: d;
		
		//For public key i.e., e2, e2 = 2^d mod p;
		long e2 = modCalc(2, d, prime);
		
		String keys = ""+prime+ " 2 "+d;
		//Private key group: prime, 2, d ( p g d)
		writeInFile(keys, "prikey.txt", false);
		System.out.println("Private key: "+keys);
		
		//Write key in file PUBKEY and PRIKEY
		keys = ""+prime+ " 2 "+e2;
		//Public key group:  prime, 2, e2 ( p g e2)
		writeInFile(keys, "pubkey.txt", false);
		System.out.println("Public key: "+keys);		
		
		System.out.println("******* Keys Generated Successfully *******");
		System.out.println();
	}
	
	//Generate random prime number, Copied from stackoverflow
	private static int randomPrime(){
		BigInteger prime;
		int bitLength = 30;
		Random rnd = new Random();
		prime = BigInteger.probablePrime(bitLength, rnd);
		return Math.abs(prime.intValue());
	}
	
	//Writes in file for given file name and line
	private static void writeInFile(String line, String fileName, boolean append){
		try {
            FileWriter fileWriter = new FileWriter(fileName, append);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(line);
            bufferedWriter.close();
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
	}
	
	
	//Encrypts file name
	public static void encryptFile(){
		System.out.println();
		System.out.println("******* Encryption for ptext.txt started *******");
		
		String fileName = "pubkey.txt";
		try {
			File plainFile = new File(fileName);
			RandomAccessFile file = new RandomAccessFile(plainFile, "r");
			
			//First read the public key from pubkey.txt
			String keyLine = file.readLine();
			file.close();
			if(keyLine.length() > 0){
				String[] keys = keyLine.split(" ");
				long prime = Long.parseLong(keys[0]);
				long g = Long.parseLong(keys[1]);
				long e2 = Long.parseLong(keys[2]);
				
				//If cipher text file exists, clear it first.
				fileName = "ctext.txt";
				plainFile = new File(fileName);
				if(plainFile.exists())
					plainFile.delete();
				
				//Read plain text file
				fileName = "ptext.txt";
				plainFile = new File(fileName);
				file = new RandomAccessFile(plainFile, "r");
				int blocks = (int) Math.ceil(plainFile.length() / 2.0);	
				for (int i = 0; i < blocks; i++) {
					byte[] block = new byte[2];
					file.read(block);
					long P = bytesToLong(block);
					String chars = new String(block);
					if(chars.trim().length() > 0) {
						System.out.println("P = "+chars+", Ascii Value(bytes): "+P);
						CipherText C = encryptPlain(g, e2, prime, P);
						writeInFile(""+C.C1+","+C.C2+"\n", "ctext.txt", true);
						System.out.println("C1: "+C.C1+", C2: "+C.C2);
						System.out.println();
					}
				}
				file.close();
			} else {
				System.err.println("Something wrong with public key file.");
			}
		} catch (Exception e) {
			System.err.println("Plain text file read error: "+e.getMessage());
			System.exit(0);
		}
		
		System.out.println("******* Encryption for ptext.txt completed *******");
		System.out.println();
	}
	
	//Encrypt and return Values
	public static CipherText encryptPlain(long e1, long e2, long prime, long P) {
		CipherText c = new CipherText();
		Random rand = new Random();
		long r = Math.abs(rand.nextInt()) % (prime - 1);
		c.C1 = modCalc(e1, r, prime);
		c.C2 = (modCalc(e2, r, prime) * (P % prime)) % prime; 
		return c;
	}
	
	
	//Decrypt file
	public static void decryptFile(){
		System.out.println();
		System.out.println("******* Decryption for ctext.txt started *******");
		
		String fileName = "prikey.txt";
		try {
			File plainFile = new File(fileName);
			RandomAccessFile file = new RandomAccessFile(plainFile, "r");
			
			//First read the public key from pubkey.txt
			String keyLine = file.readLine();
			file.close();
			if(keyLine.trim().length() > 0){
				String[] keys = keyLine.split(" ");
				long prime = Long.parseLong(keys[0]);
				long g = Long.parseLong(keys[1]);
				long d = Long.parseLong(keys[2]);
				
				//Delete dtext.txt if exists
				plainFile = new File("dtext.txt");
				if(plainFile.exists())
					plainFile.delete();
				
				//Read cipher text file line by line
				fileName = "ctext.txt";
				plainFile = new File(fileName);
				file = new RandomAccessFile(plainFile, "r");
				String cipherLine;
				while((cipherLine = file.readLine()) != null) {
					if(cipherLine.trim().length() > 0) {
						CipherText C = new CipherText();
						//Split line by comma
						String[] ctexts = cipherLine.split(",");
						if(ctexts.length == 2){
							C.C1 = Long.parseLong(ctexts[0]);
							C.C2 = Long.parseLong(ctexts[1]);
							System.out.println("C1: "+C.C1+", C2: "+C.C2);
							long P = decryptCipher(d, prime, C);
							byte[] b = longToBytes(P);
							System.out.println("Ascii Value (bytes) = "+P+" , P: "+(new String(b).trim()));
							//Write in file using byte by byte
							//Last bytes be null and might create problem
							if(b.length == 2 && b[1] == (byte)0){
								writeInFile(""+(char)b[0], "dtext.txt", true);
							}
							writeInFile(new String(b), "dtext.txt", true);
							System.out.println();
						}
					}
				}
				file.close();
			} else {
				System.err.println("Something wrong with public key file.");
			}
		} catch (Exception e) {
			System.err.println("Cipher text file read error: "+e.getMessage());
			System.exit(0);
		}
		System.out.println("******* Decryption for ctext.txt completed *******");
		System.out.println();
	}
	
	//Decrypt cipher text
	public static long decryptCipher(long d, long prime, CipherText C) {
		return (modCalc(C.C1, prime-1-d, prime) * (C.C2 % prime)) % prime;
	}
	
	//Converts long to bytes for character, copied from stackoverflow
	public static byte[] longToBytes(long l) {
	    byte[] result = new byte[2];
	    for (int i = 1; i >= 0; i--) {
	        result[i] = (byte)(l & 0xFF);
	        l >>= 8;
	    }
	    return result;
	}

	//Converts bytes character to long for character, copied from stackoverflow
	public static long bytesToLong(byte[] b) {
	    long result = 0;
	    for (int i = 0; i < b.length; i++) {
	        result <<= 8;
	        result |= (b[i] & 0xFF);
	    }
	    return result;
	}
	
	//Class to store ciphertext value
	public static class CipherText{
		long C1;
		long C2;
	}
}
