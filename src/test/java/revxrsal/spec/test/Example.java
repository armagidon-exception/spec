package revxrsal.spec.test;

import revxrsal.spec.Specs;

import java.io.File;

public class Example {

    public static void main(String[] args) {
        MyConfiguration config = Specs.fromFile(MyConfiguration.class, new File("test.yml"));
        System.out.println(config);
        config.save();
    }
}
