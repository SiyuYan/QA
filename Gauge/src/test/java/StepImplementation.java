import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class StepImplementation {



     @Step("Say <greeting> to <product name>")
    public void helloWorld(String greeting, String name) {
        // Step implementation
        System.out.println("greeting: "+ greeting);
        System.out.println("name: "+name);

    }
     @Step("Create following <race> characters <table>")
    public void createCharacters(String type, Table table) {
        // Step implementation
         System.out.println("type: "+ type);
        System.out.println("table: "+table);
    }

}
