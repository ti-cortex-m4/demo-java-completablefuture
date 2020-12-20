package demo.uml;

import de.elnarion.util.plantuml.generator.PlantUMLClassDiagramGenerator;
import de.elnarion.util.plantuml.generator.classdiagram.VisibilityType;
import de.elnarion.util.plantuml.generator.config.PlantUMLConfig;
import de.elnarion.util.plantuml.generator.config.PlantUMLConfigBuilder;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class Test {

    public static void main(String[] args) throws Exception {
        List<String> scanpackages = new ArrayList<>();
        scanpackages.add("demo.uml.classes");

        PlantUMLConfig config = new PlantUMLConfigBuilder(scanpackages)
                .withMaximumMethodVisibility(VisibilityType.PUBLIC)
                .withClassLoader(Test.class.getClassLoader())
                .withRemoveFields(true)
                .build();

        PlantUMLClassDiagramGenerator generator = new PlantUMLClassDiagramGenerator(config);
        String result = generator.generateDiagramText();

        try (PrintStream out = new PrintStream(new FileOutputStream("Future.puml"))) {
            out.print(result);
        }

    }
}
