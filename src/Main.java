import java.io.File;

public class Main {
    public static void main(String[] args) {
        new Kraken()
            .sink(new File("resources/controllers.yml"))
            .sink(new File("resources/parameters.yml"))
            .sink(new File("resources/services.yml"))
        .dive();
    }
}
