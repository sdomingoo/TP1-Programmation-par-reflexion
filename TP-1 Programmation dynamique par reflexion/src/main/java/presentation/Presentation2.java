
package presentation;

import dao.IDao;
import metier.IMetier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Presentation2 {

    public static void main(String[] args) {
        try {
            // 1. Lire les noms des classes
            String[] classNames = loadClassNames("config.txt");

            System.out.println("[CONTAINER] Classe DAO configurée : "
                    + classNames[0]);
            System.out.println("[CONTAINER] Classe Métier configurée : "
                    + classNames[1]);

            // 2. Instancier le DAO
            IDao dao = (IDao) instantiateClass(classNames[0]);

            System.out.println("[CONTAINER] DAO instancié : "
                    + dao.getClass().getName());

            // 3. Instancier le Métier
            IMetier metier =
                    (IMetier) instantiateClass(classNames[1]);

            System.out.println("[CONTAINER] Métier instancié : "
                    + metier.getClass().getName());

            // 4. Injecter le DAO par réflexion
            injectDependency(metier, "setDao", dao);

            // 5. Effectuer le calcul
            double resultat = metier.calcul();

            System.out.println("Résultats = " + resultat);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] loadClassNames(String fileName)
            throws IOException {

        List<String> classNames = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(fileName))) {

            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Ignorer les lignes vides
                if (!line.isEmpty()) {
                    classNames.add(line);
                }
            }
        }

        if (classNames.size() < 2) {
            throw new IllegalArgumentException(
                    "config.txt doit contenir les noms de "
                            + "DaoImpl et MetierImpl sur deux lignes."
            );
        }

        return new String[] {
                classNames.get(0),
                classNames.get(1)
        };
    }

    private static Object instantiateClass(String className)
            throws Exception {

        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de classe est vide."
            );
        }

        Class<?> clazz = Class.forName(className);

        Constructor<?> constructor =
                clazz.getDeclaredConstructor();

        return constructor.newInstance();
    }

    private static void injectDependency(
            Object target,
            String setterName,
            Object dependency) throws Exception {

        Class<?> targetClass = target.getClass();

        // Recherche du setter compatible avec le type de dépendance
        Method setterMethod = null;

        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(setterName)
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0]
                    .isAssignableFrom(dependency.getClass())) {

                setterMethod = method;
                break;
            }
        }

        if (setterMethod == null) {
            throw new NoSuchMethodException(
                    "Méthode " + setterName
                            + " introuvable dans "
                            + targetClass.getName()
            );
        }

        setterMethod.invoke(target, dependency);
    }
}
