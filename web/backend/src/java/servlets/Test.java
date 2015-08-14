package servlets;

import misc.BCrypt;

public class Test {

    public static void main(String[] args) {

	System.out.println(BCrypt.hashpw("abcd1234", BCrypt.gensalt()));
    }
}