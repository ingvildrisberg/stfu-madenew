package server.utility;

import server.config.Config;
import com.google.gson.Gson;

public class Crypter {

    // XOR-kypteringsmetoden: (CR)
    //https://github.com/KyleBanks/XOREncryption/blob/master/Java%20(Android%20compatible)/XOREncryption.java

    /**
     *
     * @param input
     * @return Input
     */
        public static String encrypt(String input) {

        if (Config.getEncryption()) {
            char[] key = {'J', 'M', 'F'};
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < input.length(); i++) {
                output.append((char) (input.charAt(i) ^ key[i % key.length]));
            }

            String encrypted = new Gson().toJson(output.toString());
            return encrypted;
        } else {
            return input;
        }
    }

    public String decrypt(String input) {
            if (Config.getEncryption()) {
                char[] key = {'J', 'M', 'F'};
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    output.append((char) (input.charAt(i) ^ key[i % key.length]));
                }

                String decrypted = output.toString();
                return decrypted;
            } else {
                return input;
            }
    }
}





