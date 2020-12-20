package demo;

import de.elnarion.util.plantuml.generator.PlantUMLClassDiagramGenerator;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class Test {

    public static void main(String[] args) throws Exception {
        List<String> scanpackages = new ArrayList<>();
        scanpackages.add("java.util.concurrent");
        String whitelistRegExp = ".*Future.*";
        List<String> hideClasses = new ArrayList<>();
        PlantUMLClassDiagramGenerator generator = new PlantUMLClassDiagramGenerator(
                        Future.class.getClassLoader(),
                        whitelistRegExp,
                        hideClasses,
                        false,
                        false,
                        scanpackages);
        String result = generator.generateDiagramText();

        try (PrintStream out = new PrintStream(new FileOutputStream("Future.puml"))) {
            out.print(result);
        }

    }
}
