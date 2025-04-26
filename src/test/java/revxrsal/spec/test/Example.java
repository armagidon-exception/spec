package revxrsal.spec.test;

import revxrsal.spec.Specs;

import java.io.File;

public class Example {

    public static void main(String[] args) {
        ServerConfig config = Specs.fromFile(ServerConfig.class, new File("server.yml"));
        System.out.println(config);
        config.save();
    }
}
